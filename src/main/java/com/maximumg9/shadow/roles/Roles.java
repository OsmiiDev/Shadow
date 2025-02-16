package com.maximumg9.shadow.roles;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public enum Roles {
    SPECTATOR(new Spectator(null,null).getRawName(), Spectator::new),
    VILLAGER(new Villager(null,null).getRawName(), Villager::new),
    SHADOW(new ShadowRole(null,null).getRawName(), ShadowRole::new);

    public final String name;
    public final RoleFactory factory;

    Roles(String name, RoleFactory factory) {
        this.name = name;
        this.factory = factory;
    }

    public static CompletableFuture<Suggestions> suggest(CommandContext<?> ctx, SuggestionsBuilder builder) {
        Arrays.stream(values())
            .filter(
                (role) ->
                    Objects.requireNonNull(role.name)
                    .startsWith(builder.getRemaining())
            ).forEach(
                (role) ->
                    builder.suggest(role.name)
            );

        return builder.buildFuture();
    }

    @NotNull
    public static RoleFactory getRole(CommandContext<?> ctx, String name) {
        String roleName = StringArgumentType.getString(ctx, name);

        List<Roles> possibleRoles = Arrays.stream(values()).filter((role) -> role.name.equals(roleName)).toList();

        if(possibleRoles.size() != 1) {
            throw new IllegalStateException("Roles are broken, multiple roles with same name");
        }

        return possibleRoles.getFirst().factory;
    }
}
