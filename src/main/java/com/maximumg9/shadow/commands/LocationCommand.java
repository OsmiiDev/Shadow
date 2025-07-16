package com.maximumg9.shadow.commands;

import com.maximumg9.shadow.GamePhase;
import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.util.FakeStructureWorldAccess;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructureSet;
import net.minecraft.structure.StructureSetKeys;
import net.minecraft.structure.StructureStart;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;
import net.minecraft.world.GameMode;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.chunk.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureKeys;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;
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
                            Shadow shadow = getShadow(server);
                            try {
                                shadow.resetState();

                                shadow.state.currentLocation = BlockPos.ofFloored(ctx.getSource().getPosition());
                            } catch (Throwable t) {
                                shadow.ERROR(t);
                            }

                            return 1;
                        })
                )
                    .then(
                        literal("skip")
                            .executes((ctx) -> {
                                MinecraftServer server = ctx.getSource().getServer();
                                Shadow shadow = getShadow(server);
                                try {
                                    shadow.state.playedStrongholdPositions.add(shadow.state.strongholdChunkPosition);

                                    shadow.resetState();

                                    shadow.saveAsync();

                                    return findAndGotoLocation(ctx);
                                } catch (Throwable t) {
                                    LogUtils.getLogger().error("error while forcing a location", t);
                                    shadow.ERROR(t);
                                }
                                return 0;
                            })
                    )
                .executes((ctx) -> {
                    int ret = 0;
                    try {
                        ret = LocationCommand.findAndGotoLocation(ctx);
                    } catch(Exception e) {
                        getShadow(ctx.getSource().getServer()).ERROR(e);
                    }
                    return ret;
                })
        );
    }

    public static int findAndGotoLocation(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource src = ctx.getSource();
        ServerWorld overworld = src.getServer().getOverworld();
        MinecraftServer server = src.getServer();
        Shadow shadow = getShadow(server);

        BlockBox frames = findLocation(ctx);

        if(!shadow.state.phase.canSelectLocation) return -1;

        if(frames == null) {
            shadow.ERROR("Portal frames not found");
            return -1;
        }

        clearPortalEyes(overworld, frames);

        int xRange = shadow.config.worldBorderSize / 2;
        int zRange = shadow.config.worldBorderSize / 2;

        frames = frames.expand(
            xRange - frames.getBlockCountX(),
            0,
            zRange - frames.getBlockCountZ()
        );

        int x = overworld.getRandom().nextBetween(frames.getMinX(),frames.getMaxX());
        int z = overworld.getRandom().nextBetween(frames.getMinZ(),frames.getMaxZ());

        shadow.state.currentLocation = new BlockPos(x,0,z);
        Vec3d teleportPos = shadow.state.currentLocation.toBottomCenterPos();

        arrangePlayersInCircle(overworld,teleportPos,server.getPlayerManager().getPlayerList());

        overworld.getWorldBorder().setCenter(shadow.state.currentLocation.getX(),shadow.state.currentLocation.getZ());
        overworld.getWorldBorder().setSize(shadow.config.worldBorderSize);

        List<Entity> nonPlayers = new ArrayList<>();

        server.getWorlds().forEach(
            world ->
                world.collectEntitiesByType(
                    TypeFilter.instanceOf(Entity.class),
                    (entity) -> entity.getType() != EntityType.PLAYER,
                    nonPlayers
                )
        );

        nonPlayers.forEach(Entity::discard);

        src.sendFeedback(
            () ->
                Text.literal(
                        "Searched and Went to Location"
                ),
        true
        );

        shadow.state.phase = GamePhase.LOCATION_SELECTED;

        shadow.saveAsync();

        for(IndirectPlayer player : shadow.getOnlinePlayers()) {
            player.getPlayerOrThrow().changeGameMode(GameMode.ADVENTURE);
            player.frozen = true;
        }

        return 1;
    }

    // Returns BlockBox containing all portal frames
    public static BlockBox findLocation(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource src = ctx.getSource();
        MinecraftServer server = src.getServer();
        ServerWorld overworld = server.getOverworld();
        Shadow shadow = getShadow(server);

        Optional<RegistryEntry.Reference<Structure>> strongholdStructure = overworld.getRegistryManager().get(RegistryKeys.STRUCTURE).getEntry(StructureKeys.STRONGHOLD);

        if(strongholdStructure.isEmpty()) {
            shadow.ERROR("Strongholds aren't generating (maybe you have a mod or datapack that modifies stronghold generation?)");
            return null;
        }

        StructureSet strongholds = overworld
                .getRegistryManager()
                .get(RegistryKeys.STRUCTURE_SET)
                .get(StructureSetKeys.STRONGHOLDS);

        if(strongholds == null) {
            shadow.ERROR("Strongholds aren't generating (maybe you have a mod or datapack that modifies stronghold generation?)");
            return null;
        }
        StructurePlacement strongholdPlacement = strongholds.placement();

        if(!(strongholdPlacement instanceof ConcentricRingsStructurePlacement)) {
            shadow.ERROR("Strongholds are not in concentric rings (maybe you have a mod or datapack that modifies stronghold generation?)");
            return null;
        }

        List<ChunkPos> placementPositions = new ArrayList<>(
                Objects.requireNonNull(
                        overworld
                                .getChunkManager()
                                .getStructurePlacementCalculator()
                                .getPlacementPositions((ConcentricRingsStructurePlacement) strongholdPlacement)
                )
        );

        placementPositions.removeAll(shadow.state.playedStrongholdPositions);

        ChunkPos startChunkPos = placementPositions.getFirst();

        StructureStart start = strongholdStructure.get().value().createStructureStart(
                overworld.getRegistryManager(),
                overworld.getChunkManager().getChunkGenerator(),
                overworld.getChunkManager().getChunkGenerator().getBiomeSource(),
                overworld.getChunkManager().getNoiseConfig(),
                overworld.getStructureTemplateManager(),
                overworld.getSeed(),
                startChunkPos,
                0,
                overworld,
                (biome) -> true
        );

        shadow.state.strongholdChunkPosition = startChunkPos;

        shadow.saveAsync();

        FakeStructureWorldAccess fakeStructureWorldAccess = new FakeStructureWorldAccess(overworld);

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
                overworld
                        .getRegistryManager()
                        .get(RegistryKeys.STRUCTURE)
                        .stream()
                        .filter(
                                structureType -> structureType.getFeatureGenerationStep() == strongholdStructure.get().value().getFeatureGenerationStep()
                        ).toList();

        int index = generationStepStructures.indexOf(strongholdStructure.get().value());

        Instant startTime = Instant.now();

        src.sendMessage(
                Text.literal(
                        "Generating portal room"
                ).styled((style) -> style.withColor(Formatting.AQUA))
        );

        portalRoom.getBoundingBox().streamChunkPos().forEach(
            chunkPos -> {
                ChunkSectionPos bottomSection = ChunkSectionPos.from(chunkPos, overworld.getBottomSectionCoord());

                BlockPos lv3 = bottomSection.getMinPos();

                long popSeed = random.setPopulationSeed(overworld.getSeed(), lv3.getX(), lv3.getZ());

                int generationStep = strongholdStructure.get().value().getFeatureGenerationStep().ordinal();

                random.setDecoratorSeed(popSeed, index, generationStep);

                portalRoom.generate(
                        fakeStructureWorldAccess,
                        overworld.getStructureAccessor(),
                        overworld.getChunkManager().getChunkGenerator(),
                        random,
                        new BlockBox(chunkPos.getStartX(), overworld.getBottomY(), chunkPos.getStartZ(), chunkPos.getEndX(), overworld.getTopY(), chunkPos.getEndZ()),
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

            int y = 1 + getTopYForBoundingBox(world,player.getBoundingBox(player.getPose()).offset(x,0,z), Heightmap.Type.MOTION_BLOCKING);

            player.teleport(world,x,y,z,currentAngle * 180/MathHelper.PI,0);
        }
    }

    public static int getTopYForBoundingBox(ServerWorld world, Box bb, Heightmap.Type heightMap) {
        int minX = MathHelper.floor(bb.minX);
        int maxX = MathHelper.floor(bb.maxX);
        int minZ = MathHelper.floor(bb.minZ);
        int maxZ = MathHelper.floor(bb.maxZ);

        LogUtils.getLogger().info("minX: {},maxX: {},minZ: {},maxZ: {}", bb.minX,bb.maxX,bb.minZ,bb.maxZ);

        int highest = world.getChunk(ChunkSectionPos.getSectionCoord(minX),ChunkSectionPos.getSectionCoord(minZ)).sampleHeightmap(heightMap,minX,minZ);
        int nhighest;

        if(minX != maxX) {
            nhighest = world.getChunk(ChunkSectionPos.getSectionCoord(maxX),ChunkSectionPos.getSectionCoord(minZ)).sampleHeightmap(heightMap,maxX,minZ);
            if(nhighest > highest) {
                highest = nhighest;
            }
        }
        if(minZ != maxZ) {
            nhighest = world.getChunk(ChunkSectionPos.getSectionCoord(minX),ChunkSectionPos.getSectionCoord(maxZ)).sampleHeightmap(heightMap,minX,maxZ);
            if(nhighest > highest) {
                highest = nhighest;
            }

            if(minX != maxX) {
                nhighest = world.getChunk(ChunkSectionPos.getSectionCoord(maxX),ChunkSectionPos.getSectionCoord(maxZ)).sampleHeightmap(heightMap,maxX,maxZ);
                if(nhighest > highest) {
                    highest = nhighest;
                }
            }
        }

        LogUtils.getLogger().info("y: {}",highest);

        return highest;
    }
}
