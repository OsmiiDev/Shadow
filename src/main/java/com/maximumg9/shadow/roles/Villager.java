package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Villager extends Role {
    @Override
    public ItemStack getAsItem() { return ITEM.copy(); }

    public Villager(@Nullable IndirectPlayer player) {
        super(player, List.of());
    }

    @Override
    public Faction getFaction() {
        return Faction.VILLAGER;
    }
    @Override
    public String getRawName() {
        return "Villager";
    }

    @Override
    public void onNight() {
        this.player.sendSubtitle(
                Text.literal("It is now night, the shadows are more powerful so be careful")
                        .styled((style) -> style.withColor(Formatting.GREEN)),
                CancelPredicates.IS_DAY
        );
        this.player.giveEffect(
                new StatusEffectInstance(
                        StatusEffects.DARKNESS,
                        -1,0,
                        true,false,
                        true
                ),
                CancelPredicates.IS_DAY
        );
        super.onNight();
    }

    @Override
    public void onDay() {
        this.player.sendSubtitle(
                Text.literal("It's now day")
                        .styled((style) -> style.withColor(Formatting.YELLOW)),
                CancelPredicates.IS_NIGHT
        );
        this.player.removeEffect(
                StatusEffects.DARKNESS,
                CancelPredicates.IS_NIGHT
        );
        super.onDay();
    }

    @Override
    public TextColor getColor() {
        return TextColor.fromFormatting(Formatting.GREEN);
    }

    public static final RoleFactory<Villager> FACTORY = new Factory();
    private static class Factory implements RoleFactory<Villager> {
        @Override
        public Villager makeRole(@Nullable IndirectPlayer player) {
            return new Villager(player);
        }
    }

    private static final ItemStack ITEM = new ItemStack(Items.EMERALD);
    static {
        ITEM.set(DataComponentTypes.ITEM_NAME,new Villager(null).getName());
    }
}
