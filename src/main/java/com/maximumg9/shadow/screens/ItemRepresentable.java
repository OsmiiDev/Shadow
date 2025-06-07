package com.maximumg9.shadow.screens;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;

public interface ItemRepresentable {
    ItemStack getAsItem(RegistryWrapper.WrapperLookup registries);
}
