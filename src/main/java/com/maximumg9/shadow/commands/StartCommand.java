package com.maximumg9.shadow.commands;

import com.maximumg9.shadow.GamePhase;
import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.Tickable;
import com.maximumg9.shadow.ducks.ShadowProvider;
import com.maximumg9.shadow.roles.RoleManager;
import com.maximumg9.shadow.util.IndirectPlayer;
import com.maximumg9.shadow.util.TimeUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

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
        if(!check(ctx)) return -1;
        return start(ctx);
    }

    public static boolean check(CommandContext<ServerCommandSource> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        Shadow shadow = ((ShadowProvider) server).shadow$getShadow();

        return shadow.state.phase == GamePhase.LOCATION_SELECTED;
    }

    public static int start(CommandContext<ServerCommandSource> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        Shadow shadow = ((ShadowProvider) server).shadow$getShadow();

        shadow.addTickable(new StartTicker(shadow));

        for(IndirectPlayer player : shadow.getOnlinePlayers()) {
            ServerPlayerEntity entity = player.getEntity().get();

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
    Shadow shadow;
    int ticksLeft = 10 * 20;

    StartTicker(Shadow shadow) {
        this.shadow = shadow;
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
        shadow.saveAsync();

        try {
            shadow.roleManager.pickRoles();
        } catch (RoleManager.TooManyPlayersException e) {
            shadow.getOnlinePlayers().forEach((player) -> {
                player.sendMessage(Text.literal("Too many players to start game, consider increasing role slot count in config"));
            });
            return;
        }


        shadow.getOnlinePlayers().forEach((player) -> {
            player.getEntity().get().getInventory().clear();

            player.clearPlayerData();

            player.setTitleTimes(10,40,10);
            if(player.role == null) throw new IllegalStateException("Null role accidentally chosen");
            player.sendTitle(player.role.getFaction().name);
        });
    }

    @Override
    public boolean shouldEnd() {
        return ticksLeft <= 0;
    }
}
