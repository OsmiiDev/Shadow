package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.text.Text;

public class ToggleStrength extends Ability {
    private static final ItemStack ITEM_STACK = PotionContentsComponent.createStack(Items.POTION, Potions.LONG_STRENGTH);
    private boolean hasStrength = false;

    public ToggleStrength(IndirectPlayer player) {
        super(player);
    }

    @Override
    public String getID() {
        return "toggle_strength";
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
    public ItemStack getAsItem() {
        return ITEM_STACK.copy();
    }
}
