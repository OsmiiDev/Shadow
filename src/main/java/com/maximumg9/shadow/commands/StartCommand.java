package com.maximumg9.shadow.commands;

import com.maximumg9.shadow.GamePhase;
import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.Tickable;
import com.maximumg9.shadow.ducks.ShadowProvider;
import com.maximumg9.shadow.util.IndirectPlayer;
import com.maximumg9.shadow.util.TimeUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
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

        if(shadow.phase != GamePhase.LOCATION_SELECTED) return false;

        return true;
    }

    public static int start(CommandContext<ServerCommandSource> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        Shadow shadow = ((ShadowProvider) server).shadow$getShadow();

        shadow.addTickable(new StartTicker(shadow));

        for(IndirectPlayer player : shadow.getOnlinePlayers()) {
            ServerPlayerEntity entity = player.getEntity().get();
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
        TitleFadeS2CPacket titleFadeS2CPacket = new TitleFadeS2CPacket(0, 20, 20);
        TitleS2CPacket title = new TitleS2CPacket(Text.literal(TimeUtil.ticksToText(this.ticksLeft,false)));

        for(IndirectPlayer player : this.shadow.getOnlinePlayers()) {
            ServerPlayerEntity entity = player.getEntity().get();
            entity.networkHandler.sendPacket(titleFadeS2CPacket);
            entity.networkHandler.sendPacket(title);
        }

        ticksLeft--;
    }

    @Override
    public void onEnd() {
        shadow.phase = GamePhase.PLAYING;
    }

    @Override
    public boolean shouldEnd() {
        return ticksLeft <= 0;
    }
}
