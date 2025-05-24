package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
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

    public static final RoleFactory<Spectator> FACTORY = new Factory();
    private static class Factory implements RoleFactory<Spectator> {
        @Override
        public Spectator makeRole(@Nullable IndirectPlayer player) {
            return new Spectator(player);
        }
    }

    private static final ItemStack ITEM = new ItemStack(Items.LIGHT);
    static {
        ITEM.set(DataComponentTypes.ITEM_NAME,new Spectator(null).getName());
    }

    @Override
    public ItemStack getAsItem() { return ITEM.copy(); }
}
