package com.maximumg9.shadow.util;


import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.ducks.ShadowProvider;
import com.maximumg9.shadow.roles.Role;
import com.maximumg9.shadow.roles.Spectator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

/**
 This is meant to represent a player who existed at some time, even if the player does not exist now
 */
public class IndirectPlayer {

    private IndirectPlayer(ServerPlayerEntity base) {
        this.playerUUID = base.getUuid();
        this.server = base.server;
        Shadow shadow = ((ShadowProvider) this.server).shadow$getShadow();
        this.role = new Spectator(shadow,this);
    }

    private final UUID playerUUID;
    private final MinecraftServer server;
    public Role role;

    public Optional<ServerPlayerEntity> getEntity() {
        return Optional.ofNullable(server.getPlayerManager().getPlayer(this.playerUUID));
    }

    public boolean exists() {
        return getEntity().isPresent();
    }

    private static final HashMap<UUID,IndirectPlayer> indirectPlayers = new HashMap<>();

    public static IndirectPlayer get(ServerPlayerEntity base) {
        return indirectPlayers.computeIfAbsent(base.getUuid(),(uuid) -> new IndirectPlayer(base));
    }
}
