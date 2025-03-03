package com.maximumg9.shadow.commands;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.ducks.ShadowProvider;
import com.maximumg9.shadow.roles.Roles;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RoleCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("$roles")
                .requires((source) -> source.hasPermissionLevel(3))
                .then(
                    literal("weight")
                        .then(
                            argument("slotID",integer())
                                .then(
                                    argument("role",string())
                                        .suggests(Roles::suggest)
                                        .then(
                                            argument("newWeight",integer())
                                                .executes(RoleCommand::setWeight)
                                        )
                                )
                        )
                )
                .executes((ctx) -> {
                    ServerCommandSource src = ctx.getSource();

                    if(!src.isExecutedByPlayer()) {return -1;}

                    Shadow shadow = ((ShadowProvider)src.getServer()).shadow$getShadow();
                    shadow.roleManager.showRoleBook(src.getPlayer());

                    ctx.getSource().sendFeedback(
                        () -> Text.literal("Opened Role Book"),
                        true
                    );

                    return 2;
                })
        );
    }

    private static int setWeight(CommandContext<ServerCommandSource> ctx) {
        Shadow shadow = ((ShadowProvider) ctx.getSource().getServer()).shadow$getShadow();

        int slotID = getInteger(ctx,"slotID");
        Roles role = Roles.getRole(ctx,"role");
        int newWeight = getInteger(ctx,"newWeight");

        shadow.roleManager.getSlot(slotID).setWeight(role,newWeight);

        ctx.getSource().sendFeedback(
            () -> Text.literal(
                "Set the Weight for " +
                    role.name + " in Slot " +
                    (slotID + 1) + " to " +
                    newWeight
            ), true
        );

        return newWeight;
    }
}
