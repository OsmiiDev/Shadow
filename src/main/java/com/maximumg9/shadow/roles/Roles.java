package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.Shadow;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public enum Roles {
    SPECTATOR(Spectator.FACTORY),
    VILLAGER(Villager.FACTORY),
    SHADOW(ShadowRole.FACTORY);

    public final String name;
    public final RoleFactory<?> factory;

    Roles(RoleFactory<?> factory) {
        this(factory.makeRole(null).getRawName(),factory);
    }

    Roles(String name, RoleFactory<?> factory) {
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

    public static Roles getRole(String roleName) {
        List<Roles> possibleRoles = Arrays.stream(values()).filter((role) -> role.name.equals(roleName)).toList();

        if(possibleRoles.isEmpty()) {
            throw new IllegalArgumentException("no role with such name");
        }

        if(possibleRoles.size() > 1) {
            throw new IllegalStateException("Roles are broken, multiple roles with same argumentName");
        }

        return possibleRoles.getFirst();
    }

    @NotNull
    public static Roles getRole(CommandContext<?> ctx, String argumentName) {
        String roleName = StringArgumentType.getString(ctx, argumentName);
        return getRole(roleName);
    }
}
