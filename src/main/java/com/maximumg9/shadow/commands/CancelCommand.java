package com.maximumg9.shadow.commands;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.ducks.ShadowProvider;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class CancelCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("$cancel")
                        .requires((source) -> source.hasPermissionLevel(3))
                        .executes((ctx) -> {
                            Shadow shadow = ((ShadowProvider) ctx.getSource().getServer()).shadow$getShadow();

                            shadow.cancelGame();

                            ctx.getSource().sendFeedback(
                                    () -> Text.literal("Cancelled Game"),
                                    true);

                            return 0;
                        })
        );
    }
}
