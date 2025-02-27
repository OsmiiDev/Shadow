package com.maximumg9.shadow.roles;

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
    SPECTATOR(Spectator::new),
    VILLAGER(Villager::new),
    SHADOW(ShadowRole::new),
    TESTER("TEST_ONLY", Tester::new, false);

    public final String name;
    public final RoleFactory factory;
    public final boolean normallyVisibile;

    Roles(RoleFactory factory) {
        this(factory.makeRole(null,null).getRawName(),factory,true);
    }

    Roles(String name, RoleFactory factory, boolean normallyVisibile) {
        this.name = name;
        this.factory = factory;
        this.normallyVisibile = normallyVisibile;
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
