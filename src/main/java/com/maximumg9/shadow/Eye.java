package com.maximumg9.shadow;

import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.UUID;

public record Eye(RegistryKey<World> worldKey, UUID item, UUID display, BlockPos position) {
    public void destroy(Shadow shadow) {
        ServerWorld world = shadow.getServer().getWorld(worldKey);

        if(world == null) return;

        Chunk chunk = world.getChunk(position);

        Entity itemEntity = world.getEntity(item);
        if(itemEntity != null) {
            itemEntity.remove(Entity.RemovalReason.DISCARDED);
            shadow.ERROR("Tried to remove eye item that doesn't exist @" + position.toShortString());
        }

        Entity displayEntity = world.getEntity(display);
        if(displayEntity != null) {
            displayEntity.remove(Entity.RemovalReason.DISCARDED);
        } else {
            shadow.ERROR("Tried to remove eye display that doesn't exist @" + position.toShortString());
        }
    }
}
