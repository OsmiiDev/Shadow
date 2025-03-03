package com.maximumg9.shadow;

import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Objects;
import java.util.UUID;

public record Eye(RegistryKey<World> worldKey, UUID item, UUID display, BlockPos position) {
    public void destroy(Shadow shadow) {
        ServerWorld world = shadow.getServer().getWorld(worldKey);

        if(world == null) return;

        world.getChunk(position);

        Objects.requireNonNull(world.getEntity(item)).remove(Entity.RemovalReason.DISCARDED);
        Objects.requireNonNull(world.getEntity(display)).remove(Entity.RemovalReason.DISCARDED);
    }
}
