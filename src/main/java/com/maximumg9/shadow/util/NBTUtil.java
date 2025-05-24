package com.maximumg9.shadow.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.function.UnaryOperator;

public abstract class NBTUtil {
    public static NbtCompound getCustomData(ItemStack stack) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        if(component == null) return new NbtCompound();
        return component.copyNbt();
    }

    public static void applyToStackCustomData(ItemStack stack, UnaryOperator<NbtCompound> operator) {
        stack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()),(component) -> {
            NbtCompound compound = component.copyNbt();
            return NbtComponent.of(operator.apply(compound));
        });
    }
}
