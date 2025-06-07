package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.abilities.SheriffBow;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Sheriff extends AbstractVillager {
    private static final List<Ability.Factory> ABILITY_FACTORIES = List.of(SheriffBow::new);

    public Sheriff(@Nullable IndirectPlayer player) {
        super(player,ABILITY_FACTORIES);
    }

    public static final RoleFactory<Sheriff> FACTORY = new Sheriff.Factory();

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void deInit() {
        super.deInit();
    }

    @Override
    public String getRawName() {
        return "Sheriff";
    }

    @Override
    public TextColor getColor() {
        return TextColor.fromFormatting(Formatting.GOLD);
    }

    private static final ItemStack ITEM_STACK;
    static {
        ITEM_STACK = new ItemStack(Items.BOW);
        ITEM_STACK.set(DataComponentTypes.ITEM_NAME, new Sheriff(null).getName());
        ITEM_STACK.set(
                DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP,
                Unit.INSTANCE
        );
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        ItemStack item = ITEM_STACK.copy();
        item.addEnchantment(
            registries
                .getWrapperOrThrow(RegistryKeys.ENCHANTMENT)
                .getOrThrow(Enchantments.UNBREAKING),
            1
        );
        return item;
    }

    private static class Factory implements RoleFactory<Sheriff> {
        @Override
        public Sheriff makeRole(@Nullable IndirectPlayer player) {
            return new Sheriff(player);
        }
    }
}
