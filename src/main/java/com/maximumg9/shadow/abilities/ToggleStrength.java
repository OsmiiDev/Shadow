package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class ToggleStrength extends Ability {
    private static final ItemStack ITEM_STACK;
    private boolean hasStrength = false;

    static {
        ITEM_STACK = PotionContentsComponent.createStack(Items.POTION, Potions.LONG_STRENGTH);
        ITEM_STACK.set(
                DataComponentTypes.ITEM_NAME,
                Text.literal("Toggle Strength")
                        .styled(style -> style.withColor(Formatting.GOLD))
        );
        ITEM_STACK.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                Text.literal("Toggle Strength I")
                    .styled(style -> style.withColor(Formatting.GRAY)),
                Text.literal("During the night you also get Haste V and Speed II"),
                AbilityText()
            )
        );
    }

    public ToggleStrength(IndirectPlayer player) {
        super(player);
    }

    @Override
    public Identifier getID() {
        return MiscUtil.shadowID("toggle_strength");
    }

    @Override
    public void apply() {
        hasStrength = !hasStrength;
        if(hasStrength) {
            this.player.giveEffectNow(
                    new StatusEffectInstance(
                            StatusEffects.STRENGTH,
                            -1,
                            0,
                            false,
                            false,
                            true
                    )
            );
            if(this.player.getShadow().isNight()) {
                this.player.giveEffectNow(
                        new StatusEffectInstance(
                                StatusEffects.HASTE,
                                -1,
                                4,
                                false,
                                false,
                                true
                        )
                );
                this.player.giveEffectNow(
                        new StatusEffectInstance(
                                StatusEffects.SPEED,
                                -1,
                                1,
                                false,
                                false,
                                true
                        )
                );
            }

            this.player.sendMessageNow(Text.literal("Turned Strength On!"));
        } else {
            this.player.removeEffectNow(StatusEffects.STRENGTH);

            if(this.player.getShadow().isNight()) {
                this.player.removeEffect(
                        StatusEffects.HASTE,
                        CancelPredicates.NEVER_CANCEL
                );
                this.player.removeEffect(
                        StatusEffects.SPEED,
                        CancelPredicates.NEVER_CANCEL
                );
                this.player.giveEffect(
                        new StatusEffectInstance(
                                StatusEffects.HASTE,
                                -1,
                                1,
                                false,
                                false,
                                true
                        ),
                        CancelPredicates.IS_DAY
                );
            }
            this.player.sendMessageNow(Text.literal("Turned Strength Off!"));
        }
    }

    @Override
    public void deInit() {
        this.player.removeEffect(
                StatusEffects.STRENGTH,
                CancelPredicates.NEVER_CANCEL
        );
        this.player.removeEffect(
                StatusEffects.SPEED,
                CancelPredicates.NEVER_CANCEL
        );
        this.player.removeEffect(
                StatusEffects.HASTE,
                CancelPredicates.NEVER_CANCEL
        );
    }

    @Override
    public void onNight() {
        if(hasStrength) {
            this.player.giveEffect(
                    new StatusEffectInstance(
                            StatusEffects.HASTE,
                            -1,
                            4,
                            false,
                            false,
                            true
                    ),
                    CancelPredicates.IS_DAY
            );
            this.player.giveEffectNow(
                    new StatusEffectInstance(
                            StatusEffects.SPEED,
                            -1,
                            1,
                            false,
                            false,
                            true
                    )
            );
        }
        super.onNight();
    }

    @Override
    public void onDay() {
        this.player.removeEffect(
                StatusEffects.SPEED,
                CancelPredicates.NEVER_CANCEL
        );
        this.player.removeEffect(
                StatusEffects.HASTE,
                CancelPredicates.NEVER_CANCEL
        );
        this.player.giveEffect(
                new StatusEffectInstance(
                        StatusEffects.HASTE,
                        -1,
                        1,
                        false,
                        false,
                        true
                ),
                CancelPredicates.IS_DAY
        );
        super.onDay();
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }
}
