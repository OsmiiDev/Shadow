package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.roles.Faction;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class Cull extends Ability {
    private static final ItemStack ITEM_STACK;

    static {
        ITEM_STACK = Items.NETHERITE_SWORD.getDefaultStack();
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            Text.literal("Cull")
                .styled(style -> style.withColor(Formatting.RED))
        );
    }

    public Cull(IndirectPlayer player) {
        super(player);
    }

    private boolean usedThisNight = false;

    public static final Identifier ID = MiscUtil.shadowID("cull");
    @Override
    public Identifier getID() { return ID; }

    @Override
    public void onDay() {
        usedThisNight = false;
        super.onDay();
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        ItemStack stack = ITEM_STACK.copy();
        stack.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                Text.literal("Damage all non-shadows within ")
                    .styled(style -> style.withColor(Formatting.GRAY))
                    .append(
                        Text.literal(String.valueOf(this.getShadow().config.cullRadius))
                    ).append(Text.literal(" blocks of you")),
                Text.literal("For each non-shadow within range"),
                Text.literal("damage increases by ")
                    .append(Text.literal("2").styled(style -> style.withColor(Formatting.RED)))
                    .append(Text.literal(" hearts")),
                Text.literal("(max of 9.5 hearts)"),
                AbilityText()
            )
        );
        return stack;
    }

    @Override
    public AbilityResult apply() {
        ServerPlayerEntity p = this.player.getPlayerOrThrow();

        if(usedThisNight) {
            this.player.sendMessageNow(
                Text.literal("Cull")
                    .styled(style -> style.withColor(Formatting.RED))
                    .append(
                        Text.literal(" has already been used this night")
                    )
            );
            return AbilityResult.CLOSE;
        }

        List<ServerPlayerEntity> realTargets = p.getServerWorld().getPlayers(
            (player) -> {
                IndirectPlayer indirect = getShadow().getIndirect(player);
                return player.squaredDistanceTo(p) <= this.player.getShadow().config.cullRadius * this.player.getShadow().config.cullRadius
                    && (indirect.role == null || indirect.role.getFaction() != Faction.SHADOW);
            }
        );

        if(realTargets.isEmpty()) {
            this.player.sendMessageNow(
                Text.literal("No targets to hit")
            );
            return AbilityResult.CLOSE;
        }

        List<ServerPlayerEntity> fakeTargets = p.getServerWorld().getPlayers(
            (player) -> {
                IndirectPlayer indirect = getShadow().getIndirect(player);
                return player.squaredDistanceTo(p) <= this.player.getShadow().config.cullRadius * this.player.getShadow().config.cullRadius
                    && indirect.role != null && indirect.role.getFaction() == Faction.SHADOW;
            }
        );

        float damage = Math.min(realTargets.size() * 4,19);

        realTargets.forEach((player) ->
            player.damage(
                new DamageSource(
                    p.getServerWorld()
                        .getRegistryManager()
                        .get(RegistryKeys.DAMAGE_TYPE)
                        .getEntry(DamageTypes.MAGIC)
                        .get()
                ),
                damage
            )
        );

        fakeTargets.forEach((player) ->
            player.damage(
                new DamageSource(
                    p.getServerWorld()
                        .getRegistryManager()
                        .get(RegistryKeys.DAMAGE_TYPE)
                        .getEntry(DamageTypes.MAGIC)
                        .get()
                ),
                0.001f
            )
        );

        this.player.sendMessageNow(
            Text.literal("Damaged ")
                .styled(style -> style.withColor(Formatting.GRAY))
                .append(
                    Texts.join(
                        realTargets.stream()
                            .map(PlayerEntity::getName)
                            .map(
                                (text) ->
                                    text.copy().styled(style -> style.withColor(Formatting.YELLOW))
                            )
                            .toList(),
                        Text.literal(",")
                    )
                ).append(
                    Text.literal(" for ")
                ).append(
                    Text.literal(String.valueOf(damage/2))
                        .styled(style -> style.withColor(Formatting.RED))
                ).append(
                    Text.literal(" hearts.")
                )
        );

        usedThisNight = true;
        return AbilityResult.CLOSE;
    }
}
