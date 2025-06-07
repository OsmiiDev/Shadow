package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Spectator extends Role {
    public Spectator(@Nullable IndirectPlayer player) {
        super(player, List.of());
    }

    @Override
    public Faction getFaction() {
        return Faction.SPECTATOR;
    }

    @Override
    public String getRawName() {
        return "Spectator";
    }

    @Override
    public TextColor getColor() {
        return TextColor.fromFormatting(Formatting.GRAY);
    }

    @Override
    public void onNight() {
        this.player.sendSubtitle(
                Text.literal("It is now night")
                        .styled((style) -> style.withColor(Formatting.GRAY)),
                CancelPredicates.IS_DAY
        );
        super.onNight();
    }

    @Override
    public boolean cantSeeGlowingDuringNight() {
        return false;
    }

    @Override
    public void onDay() {
        this.player.sendSubtitle(
                Text.literal("It's now day")
                        .styled((style) -> style.withColor(Formatting.YELLOW)),
                CancelPredicates.IS_NIGHT
        );
        super.onDay();
    }

    public static final RoleFactory<Spectator> FACTORY = new Factory();
    private static class Factory implements RoleFactory<Spectator> {
        @Override
        public Spectator makeRole(@Nullable IndirectPlayer player) {
            return new Spectator(player);
        }
    }

    private static final ItemStack ITEM_STACK = new ItemStack(Items.LIGHT);
    static {
        ITEM_STACK.set(DataComponentTypes.ITEM_NAME,new Spectator(null).getName());
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) { return ITEM_STACK.copy(); }
}
