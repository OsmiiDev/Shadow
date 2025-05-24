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

public class ToggleStrength implements Ability {
    private static final ItemStack ITEM_STACK = PotionContentsComponent.createStack(Items.POTION, Potions.LONG_STRENGTH);

    private final IndirectPlayer player;

    private boolean hasStrength = false;

    public ToggleStrength(IndirectPlayer player) {
        this.player = player;
    }

    @Override
    public String getID() {
        return "toggle_strength";
    }

    @Override
    public IndirectPlayer getPlayer() {
        return player;
    }

    @Override
    public void apply() {
        hasStrength = !hasStrength;
        if(hasStrength) {
            this.player.getPlayerOrThrow().addStatusEffect(
                    new StatusEffectInstance(
                            StatusEffects.STRENGTH,
                            -1,
                            0,
                            false,
                            false,
                            true
                    )
            );
            this.player.sendMessageNow(Text.literal("Turned Strength On!"));
        } else {
            this.player.getPlayerOrThrow().removeStatusEffect(StatusEffects.STRENGTH);
            this.player.sendMessageNow(Text.literal("Turned Strength Off!"));
        }
    }

    @Override
    public void init() {

    }

    @Override
    public void deInit() {
        this.player.scheduleOnLoad(
                (player) -> player.removeStatusEffect(StatusEffects.STRENGTH),
                CancelPredicates.NEVER_CANCEL
        );
    }

    @Override
    public ItemStack getAsItem() {
        return ITEM_STACK.copy();
    }
}
