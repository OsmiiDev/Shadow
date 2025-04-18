package com.maximumg9.shadow.commands;

import com.maximumg9.shadow.GamePhase;
import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.ducks.ShadowProvider;
import com.maximumg9.shadow.roles.Roles;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Objects;

import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.command.argument.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DebugCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("$debug")
                .requires((source) -> source.hasPermissionLevel(3))
                .then(
                    literal("setRole")
                        .then(
                            argument("player", player())
                                .then(
                                    argument("role", string())
                                        .suggests(Roles::suggest)
                                            .executes( (ctx) -> {
                                                ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx,"player");
                                                Roles role = Roles.getRole(ctx,"role");
                                                Shadow shadow = ((ShadowProvider) ctx.getSource().getServer()).shadow$getShadow();

                                                IndirectPlayer indirectPlayer = shadow.getIndirect(player);

                                                indirectPlayer.role = role.factory.makeRole(indirectPlayer);


                                                ctx.getSource().sendFeedback(() ->
                                                    Text.literal("Set ")
                                                        .append(player.getName())
                                                        .append(Text.literal("'s Role to "))
                                                        .append(
                                                            Objects.requireNonNull(indirectPlayer.role)
                                                                .getName()
                                                        ),
                                                    true);

                                                return 1;
                                            }
                                        )
                                )
                        )
                )
                .then(
                    literal("setPhase")
                        .then(
                            argument("phase", string())
                                .suggests(GamePhase::suggest)
                                .executes( (ctx) -> {
                                    GamePhase phase = GamePhase.getPhase(ctx,"phase");
                                    Shadow shadow = ((ShadowProvider) ctx.getSource().getServer()).shadow$getShadow();

                                    shadow.state.phase = phase;
                                    shadow.saveAsync();

                                    ctx.getSource().sendFeedback(() -> Text.of("Set Game Phase to " + phase.name()), true);

                                    return 1;
                                })
                        )
                )
        );
    }
}
