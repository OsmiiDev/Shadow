package com.maximumg9.shadow.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

public abstract class NBTUtil {
    public static NbtCompound getCustomData(@NotNull ItemStack stack) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        if(component == null) return new NbtCompound();
        return component.copyNbt();
    }

    public static void applyToStackCustomData(@NotNull ItemStack stack, UnaryOperator<NbtCompound> operator) {
        stack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()),(component) -> {
            NbtCompound compound = component.copyNbt();
            return NbtComponent.of(operator.apply(compound));
        });
    }

    public static @Nullable Identifier getID(@NotNull ItemStack stack) {
        String id = getCustomData(stack).getString(ID_NAME);
        return id.isEmpty() ? null : Identifier.tryParse(id);
    }

    public static final String ID_NAME = "id";

    public static void addID(@NotNull ItemStack stack, Identifier id) {
        applyToStackCustomData(stack, (nbt) -> {
            nbt.putString(ID_NAME,id.toString());
            return nbt;
        });
    }
}
