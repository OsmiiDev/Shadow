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
    public TextColor getColor() {
        return TextColor.fromFormatting(Formatting.GREEN);
    }

    public static final RoleFactory<Villager> FACTORY = new Factory();
    private static class Factory implements RoleFactory<Villager> {
        @Override
        public Villager makeRole(@Nullable IndirectPlayer player) {
            return new Villager(player);
        }

        @Override
        public Villager fromNBT(NbtCompound nbt, @Nullable IndirectPlayer player) {
            Villager role = new Villager(player);

            role.readNbt(nbt);

            return role;
        }
    }

    private static final ItemStack ITEM = new ItemStack(Items.EMERALD);
    static {
        ITEM.set(DataComponentTypes.ITEM_NAME,new Villager(null).getName());
    }
}
