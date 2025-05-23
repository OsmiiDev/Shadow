package com.maximumg9.shadow.commands;

import com.maximumg9.shadow.*;
import com.maximumg9.shadow.ducks.ShadowProvider;
import com.maximumg9.shadow.roles.Faction;
import com.maximumg9.shadow.util.TimeUtil;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.Heightmap;
import net.minecraft.world.dimension.DimensionType;

import java.util.Optional;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;
import static net.minecraft.server.command.CommandManager.literal;

public class StartCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("$start")
                        .then(
                            literal("force")
                                    .executes(StartCommand::start)
                        )
                        .executes(StartCommand::checkAndStart
                        )
        );
    }

    public static int checkAndStart(CommandContext<ServerCommandSource> ctx) {
        if(!check(ctx)) {
            ctx.getSource().sendError(Text.literal("Phase is not location selected"));
            return -1;
        }
        return start(ctx);
    }

    public static boolean check(CommandContext<ServerCommandSource> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        Shadow shadow = getShadow(server);

        return shadow.state.phase == GamePhase.LOCATION_SELECTED;
    }

    public static int start(CommandContext<ServerCommandSource> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        Shadow shadow = getShadow(server);

        shadow.addTickable(new StartTicker(shadow));

        for(IndirectPlayer player : shadow.getOnlinePlayers()) {
            Optional<ServerPlayerEntity> psPlayer = player.getEntity();
            assert psPlayer.isPresent();
            ServerPlayerEntity entity = psPlayer.get();

            player.clearPlayerData();

            ItemStack eyeStack = new ItemStack(Items.ENDER_EYE);

            EnderEyeItem.EnderEyeData data = new EnderEyeItem.EnderEyeData(player.participating,true);

            data.write(eyeStack);

            entity.giveItemStack(eyeStack);
        }

        return 0;
    }
}

class StartTicker implements Tickable {
    final Shadow shadow;
    int ticksLeft = 10 * 20;

    StartTicker(Shadow shadow) {
        this.shadow = shadow;

        for(IndirectPlayer player : shadow.getOnlinePlayers()) {
            player.setTitleTimes(10,20,10);
            player.sendTitle(Text.literal("Spawning Ender Eyes").styled((style) -> style.withColor(Formatting.DARK_GREEN)));
        }

        spawnEnderEyes();
    }

    @Override
    public void tick() {
        if(ticksLeft % 20 == 0) {
            for(IndirectPlayer player : this.shadow.getOnlinePlayers()) {
                player.setTitleTimes(5,10,5);
                player.sendTitle(Text.literal(TimeUtil.ticksToText(this.ticksLeft,false)));
            }
        }

        ticksLeft--;
    }

    @Override
    public void onEnd() {
        shadow.state.phase = GamePhase.PLAYING;

        if(!shadow.config.roleManager.pickRoles()) return;

        MutableText otherShadowText = Text.literal("The other shadows are: ").styled((style -> style.withColor(Formatting.RED)));

        shadow.getOnlinePlayers().stream().filter((player) -> player.role != null && player.role.getFaction() == Faction.SHADOW).forEachOrdered(
            (player) ->
                otherShadowText
                    .append(player.getName())
                        .styled((style -> style.withColor(Formatting.GOLD)))
                    .append(Text.literal(","))
                        .styled((style) -> style.withColor(Formatting.RED))
        );

        for(IndirectPlayer player : shadow.getOnlinePlayers()) {
            if(player.role == null) {
                shadow.ERROR("Null role chosen");
                shadow.cancelGame();
                return;
            }

            if(player.role.getFaction() == Faction.SPECTATOR) {
                player.getEntity().get().changeGameMode(GameMode.SPECTATOR);
            } else {
                player.getEntity().get().changeGameMode(GameMode.SURVIVAL);
            }

            player.clearPlayerData();

            giveDefaultItems(player);
            player.role.giveItems();

            player.frozen = false;

            player.setTitleTimes(10,40,10);
            player.sendTitle(player.role.getFaction().name);

            if(player.role.getFaction() == Faction.SHADOW) {
                player.sendMessage(otherShadowText);
            }
        }

        shadow.saveAsync();
    }

    private void giveDefaultItems(IndirectPlayer player) {
        Optional<ServerPlayerEntity> psPlayer = player.getEntity();

        if(psPlayer.isPresent()) {
            PlayerInventory inventory = psPlayer.get().getInventory();

            inventory.insertStack(shadow.config.food.foodGiver.apply(shadow.config.foodAmount));

            inventory.insertStack(Items.NETHER_STAR.getDefaultStack());
        }
    }

    private static final int NETHER_LAVA_HEIGHT = 32;

    private void spawnEnderEyes() {
        BlockPos center = shadow.state.currentLocation;

        ServerWorld overworld = this.shadow.getServer().getOverworld();

        for (int i = 0; i < this.shadow.config.overworldEyes; i++) {
            int radius = this.shadow.config.worldBorderSize/2;

            int x = center.getX() + this.shadow.random.nextBetween(-radius,radius);
            int z = center.getZ() + this.shadow.random.nextBetween(-radius,radius);
            int y = overworld.getTopY(Heightmap.Type.MOTION_BLOCKING,x,z);

            spawnEye(overworld,new BlockPos(x,y,z));
        }

        ServerWorld nether = this.shadow.getServer().getWorld(ServerWorld.NETHER);

        if(nether == null) {
            shadow.ERROR("The nether does not exist");
            return;
        }

        double netherScaleFactor = DimensionType.getCoordinateScaleFactor(overworld.getDimension(),nether.getDimension());

        for (int i = 0; i < this.shadow.config.netherRoofEyes; i++) {
            int radius = this.shadow.config.worldBorderSize/2;

            int x = (int) (center.getX() * netherScaleFactor) + this.shadow.random.nextBetween(-radius,radius);
            int z = (int) (center.getZ() * netherScaleFactor) + this.shadow.random.nextBetween(-radius,radius);

            spawnEye(nether,new BlockPos(x,nether.getLogicalHeight() + 1,z));
        }

        for (int i = 0; i < this.shadow.config.netherEyes; i++) {
            int radius = this.shadow.config.worldBorderSize/2;

            BlockPos.Mutable pos = new BlockPos.Mutable();

            while(true) {
                int x = (int) (center.getX() * netherScaleFactor) + this.shadow.random.nextBetween(-radius,radius);
                int y = this.shadow.random.nextBetween(NETHER_LAVA_HEIGHT, nether.getHeight());
                int z = (int) (center.getZ() * netherScaleFactor) + this.shadow.random.nextBetween(-radius,radius);

                pos.set(x,y,z);

                BlockState state;

                while((state = nether.getBlockState(pos)).isAir()) {pos.move(Direction.DOWN);}

                pos.move(Direction.UP);

                BlockState currentState = nether.getBlockState(pos);

                if(
                        state.getFluidState().getBlockState().getBlock() != Blocks.LAVA &&
                        state.getBlock() != Blocks.FIRE &&
                        currentState.isAir()
                ) {
                    break;
                }
            }

            spawnEye(nether, pos);
        }
    }

    private void spawnEye(ServerWorld world, BlockPos pos) {
        pos.add(0,1,0);

        Vec3d blockCenter = pos.toCenterPos();
        ItemEntity item = new ItemEntity(world, blockCenter.x, blockCenter.y, blockCenter.z, new ItemStack(Items.ENDER_EYE),0,0.2,0);
        item.setInvulnerable(true);

        DisplayEntity.ItemDisplayEntity display = EntityType.ITEM_DISPLAY.create(world);
        assert display != null;
        display.getStackReference(0).set(new ItemStack(Items.ENDER_EYE));
        display.updatePosition(blockCenter.x,blockCenter.y,blockCenter.z);

        world.spawnEntity(item);
        world.spawnEntity(display);

        Eye eye = new Eye(world.getRegistryKey(),item.getUuid(),display.getUuid(),pos);

        shadow.state.eyes.add(eye);
    }

    @Override
    public boolean shouldEnd() {
        return ticksLeft <= 0;
    }
}
