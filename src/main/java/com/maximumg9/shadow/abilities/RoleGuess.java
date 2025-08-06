package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.roles.Faction;
import com.maximumg9.shadow.roles.Role;
import com.maximumg9.shadow.roles.Roles;
import com.maximumg9.shadow.screens.DecisionScreenHandler;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;

public class RoleGuess extends CooldownAbility {
    private static final ItemStack ITEM_STACK;
    private static final Text NAME = Text.literal("Role Guess")
        .styled(style -> style.withColor(Formatting.RED));

    static {
        ITEM_STACK = new ItemStack(Items.WRITABLE_BOOK,1);
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            NAME
        );
        ITEM_STACK.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                Text.literal("Guess a role ")
                    .append(Text.literal("IF")
                        .styled(style -> style.withBold(true)))
                    .append(Text.literal(":")),
                Text.literal("Shadows alive x 2 >= villagers alive ")
                    .append(Text.literal("AND")
                        .styled(style -> style.withBold(true))),
                Text.literal("with an 8 minute cooldown"),
                Text.literal("IF YOU GUESS INCORRECTLY, YOU DIE")
                    .styled(style -> style.withColor(Formatting.RED).withBold(true)),
                Ability.AbilityText()
            )
        );
    }

    private final List<Roles> unguessableRoles;
    private final List<Faction> unguessableFactions;

    public RoleGuess(IndirectPlayer player) {
        super(player);
        if(player.role != null) {
            this.unguessableRoles = List.of(player.role.getRole());
        } else {
            this.unguessableRoles = List.of();
        }
        this.unguessableFactions = List.of(Faction.SPECTATOR);
    }

    public RoleGuess(IndirectPlayer player, List<Roles> unguessableRoles, List<Faction> unguessableFactions) {
        super(player);
        this.unguessableRoles = unguessableRoles;
        this.unguessableFactions = unguessableFactions;
    }

    @Override
    public Text getName() {
        return NAME;
    }

    @Override
    int defaultCooldown() {
        return 20 * 60 * 8;
    }

    @Override
    int initialCooldown() {
        return 20 * 60 * 3;
    }

    @Override
    public AbilityResult apply() {
        this.player.getPlayerOrThrow().openHandledScreen(
            new DecisionScreenHandler.Factory<>(
                Text.literal("Person to guess"),
                (target, p) -> {
                    if(target == null) {
                        p.sendMessage(
                            Text.literal("Failed to select player to guess")
                                .styled(style -> style.withColor(Formatting.RED))
                        );
                        return;
                    }
                    p.openHandledScreen(
                        new DecisionScreenHandler.Factory<>(
                            Text.literal("Role to guess"),
                            (guessedRole, pl) -> {
                                if(guessedRole == null) {
                                    pl.sendMessage(
                                        Text.literal("Failed to select role to guess")
                                            .styled(style -> style.withColor(Formatting.RED))
                                    );
                                    return;
                                }
                                if(target.role == null) {
                                    pl.sendMessage(
                                        Text.literal("That person's role is null")
                                            .styled(style -> style.withColor(Formatting.RED))
                                    );
                                    return;
                                }
                                if(guessedRole.getRole() == target.role.getRole()) {
                                    Text roleName = target.role.getName();

                                    RegistryEntry<DamageType> magicDamage = target
                                        .getShadow()
                                        .getServer()
                                        .getRegistryManager()
                                        .get(RegistryKeys.DAMAGE_TYPE)
                                        .entryOf(DamageTypes.MAGIC);
                                    target.damage(
                                        new DamageSource(magicDamage),
                                        Float.MAX_VALUE,
                                        CancelPredicates.cancelOnPhaseChange(
                                            this.getShadow().state.phase
                                        )
                                    );
                                    pl.sendMessage(
                                        Text.literal("Killed target ")
                                            .styled(style -> style.withColor(Formatting.RED))
                                            .append(
                                                target.getName()
                                            )
                                            .append(Text.literal(" ("))
                                            .append(roleName)
                                            .append(Text.literal(")"))
                                    );
                                } else {
                                    RegistryEntry<DamageType> magicDamage = target
                                        .getShadow()
                                        .getServer()
                                        .getRegistryManager()
                                        .get(RegistryKeys.DAMAGE_TYPE)
                                        .entryOf(DamageTypes.MAGIC);
                                    this.player.damage(
                                        new DamageSource(magicDamage),
                                        Float.MAX_VALUE,
                                        CancelPredicates.cancelOnPhaseChange(
                                            this.getShadow().state.phase
                                        )
                                    );
                                }
                            },
                            Arrays.stream(Roles.values())
                                .filter(role -> !this.unguessableRoles.contains(role))
                                .<Role>map(role -> role.factory.makeRole(null))
                                .toList()
                        )
                    );
                },
                this.getShadow().indirectPlayerManager
                    .getAllPlayers()
                    .stream()
                    .filter((p) -> p.role != null && !unguessableFactions.contains(p.role.getFaction()))
                    .toList()
            )
        );
        return AbilityResult.NO_CLOSE;
    }

    public static final Identifier ID = MiscUtil.shadowID("role_guess");
    @Override
    public Identifier getID() { return ID; }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }
}
