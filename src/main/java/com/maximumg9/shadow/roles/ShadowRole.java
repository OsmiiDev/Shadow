package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ShadowRole extends AbstractShadow {
    public ShadowRole(@Nullable IndirectPlayer player) {
        super(player,List.of());
    }

    @Override
    public String getRawName() { return "Shadow"; }

    @Override
    public TextColor getColor() { return TextColor.fromFormatting(Formatting.RED); }

    public static final RoleFactory<ShadowRole> FACTORY = new Factory();

    private static class Factory implements RoleFactory<ShadowRole> {
        @Override
        public ShadowRole makeRole(@Nullable IndirectPlayer player) {
            return new ShadowRole(player);
        }
    }

    private static final ItemStack ITEM_STACK = new ItemStack(Items.NETHERITE_SWORD);
    static {
        ITEM_STACK.set(DataComponentTypes.ITEM_NAME,new ShadowRole(null).getName());
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) { return ITEM_STACK.copy(); }
}
