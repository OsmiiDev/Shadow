package com.maximumg9.shadow.commands;

import com.maximumg9.shadow.GamePhase;
import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.ducks.ShadowProvider;
import com.maximumg9.shadow.util.FakeStructureWorldAccess;
import com.maximumg9.shadow.util.IndirectPlayer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructureSetKeys;
import net.minecraft.structure.StructureStart;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.*;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.chunk.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureKeys;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static net.minecraft.server.command.CommandManager.literal;

public class LocationCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("$location")
                .requires((source) -> source.hasPermissionLevel(3))
                .then(
                    literal("force")
                        .executes((ctx) -> {
                            MinecraftServer server = ctx.getSource().getServer();
                            Shadow shadow = ((ShadowProvider) server).shadow$getShadow();

                            shadow.cancelGame();

                            return 1;
                        })
                )
                    .then(
                        literal("skip")
                            .executes((ctx) -> {
                                MinecraftServer server = ctx.getSource().getServer();
                                Shadow shadow = ((ShadowProvider) server).shadow$getShadow();

                                shadow.cancelGame();

                                shadow.saveAsync();

                                return findAndGotoLocation(ctx);
                            })
                    )
                .executes(LocationCommand::findAndGotoLocation)
        );
    }

    public static int findAndGotoLocation(CommandContext<ServerCommandSource> ctx) {
        BlockBox frames = findLocation(ctx);

        ServerCommandSource src = ctx.getSource();
        ServerWorld overworld = src.getServer().getOverworld();
        MinecraftServer server = src.getServer();
        Shadow shadow = ((ShadowProvider) server).shadow$getShadow();

        if(shadow.state.phase != GamePhase.NOT_PLAYING) return -1;

        if(frames == null) {
            shadow.ERROR("Portal frames not found");
            return -1;
        }

        clearPortalEyes(overworld, frames);

        frames = frames.expand(shadow.config.worldBorderSize / 2, 0, shadow.config.worldBorderSize / 2);

        int x = overworld.getRandom().nextBetween(frames.getMinX(),frames.getMaxX());
        int z = overworld.getRandom().nextBetween(frames.getMinZ(),frames.getMaxZ());

        shadow.state.currentLocation = new BlockPos(x,0,z);
        Vec3d teleportPos = shadow.state.currentLocation.toBottomCenterPos();

        arrangePlayersInCircle(overworld,teleportPos,server.getPlayerManager().getPlayerList());

        overworld.getWorldBorder().setCenter(shadow.state.currentLocation.getX(),shadow.state.currentLocation.getZ());
        overworld.getWorldBorder().setSize(shadow.config.worldBorderSize);

        ServerWorld nether = src.getServer().getWorld(ServerWorld.NETHER);

        if(nether == null) {
            shadow.ERROR("Nether does not exist");
            return -1;
        }

        nether.getWorldBorder().setCenter(shadow.state.currentLocation.getX()/8.0,shadow.state.currentLocation.getX()/8.0);
        nether.getWorldBorder().setSize(shadow.config.worldBorderSize);

        src.sendFeedback(
            () ->
                Text.literal(
                        "Searched and Went to Location"
                ),
        true
        );

        shadow.state.phase = GamePhase.LOCATION_SELECTED;

        for(IndirectPlayer player : shadow.getOnlinePlayers()) {
            player.frozen = true;
        }

        shadow.saveAsync();

        return 1;
    }

    // Returns BlockBox containing all portal frames
    public static BlockBox findLocation(CommandContext<ServerCommandSource> ctx) {
        ServerWorld world = ctx.getSource().getWorld();
        MinecraftServer server = world.getServer();
        Shadow shadow = ((ShadowProvider) server).shadow$getShadow();
        ServerCommandSource src = ctx.getSource();

        RegistryEntry.Reference<Structure> strongholdStructure = world.getRegistryManager().get(RegistryKeys.STRUCTURE).getEntry(StructureKeys.STRONGHOLD).get();

        StructurePlacement strongholdPlacement = world.getRegistryManager().get(RegistryKeys.STRUCTURE_SET).get(StructureSetKeys.STRONGHOLDS).placement();

        if(!(strongholdPlacement instanceof ConcentricRingsStructurePlacement)) {
            shadow.ERROR("Strongholds are not in concentric rings (maybe you have a mod or datapack that modifies stronghold generation?)");
            return null;
        }

        List<ChunkPos> placementPositions = new ArrayList<>(
                Objects.requireNonNull(
                        world
                                .getChunkManager()
                                .getStructurePlacementCalculator()
                                .getPlacementPositions((ConcentricRingsStructurePlacement) strongholdPlacement)
                )
        );

        placementPositions.removeAll(shadow.state.playedStrongholdPositions);

        ChunkPos startChunkPos = placementPositions.getFirst();

        StructureStart start = strongholdStructure.value().createStructureStart(
                world.getRegistryManager(),
                world.getChunkManager().getChunkGenerator(),
                world.getChunkManager().getChunkGenerator().getBiomeSource(),
                world.getChunkManager().getNoiseConfig(),
                world.getStructureTemplateManager(),
                world.getSeed(),
                startChunkPos,
                0,
                world,
                (biome) -> true
        );

        shadow.state.strongholdChunkPosition = startChunkPos;

        shadow.saveAsync();

        FakeStructureWorldAccess fakeStructureWorldAccess = new FakeStructureWorldAccess(world);

        List<StrongholdGenerator.PortalRoom> portalRooms = start.getChildren().stream()
                .filter((piece) -> piece instanceof StrongholdGenerator.PortalRoom)
                .map((piece) -> (StrongholdGenerator.PortalRoom) piece)
                .toList();

        if(portalRooms.isEmpty()) {
            shadow.ERROR("Stronghold somehow has NO PORTAL ROOMS??");
            return null;
        }
        if(portalRooms.size() > 1) {
            shadow.ERROR("Stronghold somehow has MORE THAN ONE PORTAL ROOM? Picking only first");
        }

        StrongholdGenerator.PortalRoom portalRoom = portalRooms.getFirst();

        BlockBox firstChildBox = (start.getChildren().getFirst()).getBoundingBox();
        BlockPos firstChildCenter = firstChildBox.getCenter();
        BlockPos firstChildBottomCenter = new BlockPos(firstChildCenter.getX(), firstChildBox.getMinY(), firstChildCenter.getZ());

        ChunkRandom random = new ChunkRandom(new Xoroshiro128PlusPlusRandom(RandomSeed.getSeed()));

        List<Structure> generationStepStructures =
                world
                        .getRegistryManager()
                        .get(RegistryKeys.STRUCTURE)
                        .stream()
                        .filter(
                                structureType -> structureType.getFeatureGenerationStep() == strongholdStructure.value().getFeatureGenerationStep()
                        ).toList();

        int index = generationStepStructures.indexOf(strongholdStructure.value());

        Instant startTime = Instant.now();

        src.sendMessage(
                Text.literal(
                        "Generating portal room"
                ).styled((style) -> style.withColor(Formatting.AQUA))
        );

        portalRoom.getBoundingBox().streamChunkPos().forEach(
            chunkPos -> {
                ChunkSectionPos bottomSection = ChunkSectionPos.from(chunkPos, world.getBottomSectionCoord());

                BlockPos lv3 = bottomSection.getMinPos();

                long popSeed = random.setPopulationSeed(world.getSeed(), lv3.getX(), lv3.getZ());

                int generationStep = strongholdStructure.value().getFeatureGenerationStep().ordinal();

                random.setDecoratorSeed(popSeed, index, generationStep);

                portalRoom.generate(
                        fakeStructureWorldAccess,
                        world.getStructureAccessor(),
                        world.getChunkManager().getChunkGenerator(),
                        random,
                        new BlockBox(chunkPos.getStartX(), world.getBottomY(), chunkPos.getStartZ(), chunkPos.getEndX(), world.getTopY(), chunkPos.getEndZ()),
                        chunkPos,
                        firstChildBottomCenter
                );
            }
        );

        long timeInMillis = Duration.between(startTime,Instant.now()).toMillis();

        src.sendMessage(
                Text.literal(
                        "Finished generating portal rooms in (" + timeInMillis + "ms)"
                ).styled((style) -> style.withColor(Formatting.AQUA))
        );

        LogUtils.getLogger().info("Portal generating in {}ms", timeInMillis);

        Optional<BlockBox> possibleBox = BlockBox.encompassPositions(fakeStructureWorldAccess.getPortalFrames());

        return possibleBox.orElse(null);
    }

    public static void clearPortalEyes(ServerWorld world, BlockBox portalBoundingBox) {
        for (BlockPos current : BlockPos.iterate(
                portalBoundingBox.getMinX(),
                portalBoundingBox.getMinY(),
                portalBoundingBox.getMinZ(),
                portalBoundingBox.getMaxX(),
                portalBoundingBox.getMaxY(),
                portalBoundingBox.getMaxZ()
        )) {
            BlockState currentBlockState = world.getBlockState(current);
            if (currentBlockState.isOf(Blocks.END_PORTAL_FRAME)) {
                world.setBlockState(current, currentBlockState.with(Properties.EYE, false));
            }
        }
    }

    public static void arrangePlayersInCircle(ServerWorld world, Vec3d centerPos, List<ServerPlayerEntity> players) {
        int radius = players.size();

        float angleIncrement = MathHelper.TAU / players.size();

        float currentAngle = 0;

        for (ServerPlayerEntity player : players) {
            currentAngle += angleIncrement;

            float xOffset = MathHelper.cos(currentAngle) * radius;
            float zOffset = MathHelper.sin(currentAngle) * radius;

            double x = xOffset + centerPos.x;
            double z = zOffset + centerPos.z;

            int y = getTopYForBoundingBox(world,player.getBoundingBox(player.getPose()).offset(x,0,z), Heightmap.Type.MOTION_BLOCKING);

            player.teleport(world,x,y,z,currentAngle * MathHelper.PI/180,0);
        }
    }

    public static int getTopYForBoundingBox(ServerWorld world, Box bb, Heightmap.Type heightMap) {
        int minX = MathHelper.floor(bb.minX);
        int maxX = MathHelper.floor(bb.maxX);
        int minZ = MathHelper.floor(bb.minY);
        int maxZ = MathHelper.floor(bb.maxZ);

        world.getChunk(minX/16,minZ/16);

        int highest = world.getTopY(heightMap, minX, minZ);

        if(minX != maxX) {
            world.getChunk(maxX/16,minZ/16);
            highest = world.getTopY(heightMap, maxX, minZ);
        }
        if(minZ != maxZ) {
            world.getChunk(minX/16,maxZ/16);
            highest = world.getTopY(heightMap, minX, maxZ);
            if(minX != maxX) {
                world.getChunk(maxX/16,maxZ/16);
                highest = world.getTopY(heightMap, maxX, maxZ);
            }
        }

        return highest;
    }
}
