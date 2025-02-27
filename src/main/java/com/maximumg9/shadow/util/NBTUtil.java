package com.maximumg9.shadow.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.function.UnaryOperator;

public class NBTUtil {
    public static NbtCompound getCustomData(ItemStack stack) {
        return stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt();
    }

    public static void applyToStackCustomData(ItemStack stack, UnaryOperator<NbtCompound> operator) {
        stack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()),(component) -> {
            NbtCompound compound = component.copyNbt();
            return NbtComponent.of(operator.apply(compound));
        });
    }
}
