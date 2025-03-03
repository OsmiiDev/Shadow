package com.maximumg9.shadow.util;


import com.maximumg9.shadow.GamePhase;
import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.ducks.ShadowProvider;
import com.maximumg9.shadow.roles.Role;
import com.maximumg9.shadow.roles.Spectator;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 This is meant to represent a player who existed at some time, even if the player does not exist now
 */
public class IndirectPlayer {

    public IndirectPlayer(ServerPlayerEntity base) {
        this.playerUUID = base.getUuid();
        this.server = base.server;
        Shadow shadow = ((ShadowProvider) this.server).shadow$getShadow();
        this.role = new Spectator(shadow,this);
        this.participating = shadow.state.phase != GamePhase.PLAYING;
    }

    private final UUID playerUUID;
    private final MinecraftServer server;
    @Nullable
    public Role role;
    public boolean participating;

    public Optional<ServerPlayerEntity> getEntity() {
        return Optional.ofNullable(server.getPlayerManager().getPlayer(this.playerUUID));
    }

    public void setTitleTimes(int fadeInTicks, int stayTicks, int fadeOutTicks) {
        Optional<ServerPlayerEntity> player = getEntity();

        if(player.isEmpty()) { return; }

        TitleFadeS2CPacket packet = new TitleFadeS2CPacket(fadeInTicks, stayTicks, fadeOutTicks);

        player.get().networkHandler.sendPacket(packet);
    }

    public void sendTitle(Text title) {
        Optional<ServerPlayerEntity> player = getEntity();

        if(player.isEmpty()) { return; }

        TitleS2CPacket packet = new TitleS2CPacket(title);

        player.get().networkHandler.sendPacket(packet);
    }

    public void sendMessage(Text chatMessage) {
        Optional<ServerPlayerEntity> player = getEntity();

        if(player.isEmpty()) { return; }

        player.get().sendMessage(chatMessage);
    }

    public void clearPlayerData() {
        Optional<ServerPlayerEntity> possiblePlayer = getEntity();

        if(possiblePlayer.isEmpty()) { return; }

        ServerPlayerEntity player = possiblePlayer.get();

        player.getInventory().clear();
        player.getEnderChestInventory().clear();
        player.setHealth(player.getMaxHealth());
        player.getHungerManager().setFoodLevel(20);
        player.getHungerManager().setSaturationLevel(5f);
        AttributeContainer attributes = player.getAttributes();

        attributes.custom.values().forEach(EntityAttributeInstance::clearModifiers);
    }

    public boolean exists() {
        return getEntity().isPresent();
    }
}
