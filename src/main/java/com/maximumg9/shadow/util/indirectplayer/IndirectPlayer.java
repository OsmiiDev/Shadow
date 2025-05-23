package com.maximumg9.shadow.util.indirectplayer;


import com.google.gson.annotations.Expose;
import com.maximumg9.shadow.GamePhase;
import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.roles.Role;
import com.maximumg9.shadow.roles.Spectator;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

/**
 This is meant to represent a player who existed at some time, even if the player does not exist now
 */
public class IndirectPlayer {

    public IndirectPlayer(ServerPlayerEntity base) {
        this.playerUUID = base.getUuid();
        this.server = base.server;
        Shadow shadow = getShadow(this.server);
        this.role = new Spectator(this);
        this.participating = shadow.state.phase != GamePhase.PLAYING;
        this.name = base.getName();
    }

    IndirectPlayer(MinecraftServer server, UUID uuid) {
        this.server = server;
        this.playerUUID = uuid;
    }

    IndirectPlayer(IndirectPlayer src) {
        this.playerUUID = src.playerUUID;
        this.server = src.server;
        this.role = src.role;
        this.participating = src.participating;
        this.frozen = src.frozen;
        this.name = src.name;
    }
    @Expose
    final UUID playerUUID;
    final MinecraftServer server;
    @Nullable
    @Expose
    public Role role;
    @Expose
    public boolean participating;
    @Expose
    public boolean frozen;
    private Text name = Text.literal("Unknown");

    public Text getName() {
        this.getEntity().ifPresent((psPlayer) -> this.name = psPlayer.getName());
        return this.name;
    }

    public Optional<ServerPlayerEntity> getEntity() {
        return Optional.ofNullable(server.getPlayerManager().getPlayer(this.playerUUID));
    }

    public void setTitleTimes(int fadeInTicks, int stayTicks, int fadeOutTicks) {
        Optional<ServerPlayerEntity> player = getEntity();

        if(player.isEmpty()) { return; }

        TitleFadeS2CPacket packet = new TitleFadeS2CPacket(fadeInTicks, stayTicks, fadeOutTicks);

        player.get().networkHandler.sendPacket(packet);
    }

    static IndirectPlayer load(MinecraftServer server, NbtCompound nbt) {
        IndirectPlayer player = new IndirectPlayer(server, nbt.getUuid("playerUUID"));
        player.frozen = nbt.getBoolean("frozen");
        player.participating = nbt.getBoolean("participating");
        if(nbt.contains("role", NbtElement.COMPOUND_TYPE)) {
            player.role = Role.load(nbt.getCompound("role"), player);
        } else {
            player.role = null;
        }

        return player;
    }

    NbtCompound save(NbtCompound nbt) {
        nbt.putUuid("playerUUID",this.playerUUID);
        nbt.putBoolean("frozen",this.frozen);
        nbt.putBoolean("participating", this.participating);

        if(this.role != null) {
            nbt.put("role", this.role.writeNbt(new NbtCompound()));
        }
        return nbt;
    }

    public void sendTitle(Text title) {
        Optional<ServerPlayerEntity> player = getEntity();

        if(player.isEmpty()) { return; }

        TitleS2CPacket packet = new TitleS2CPacket(title);

        player.get().networkHandler.sendPacket(packet);
    }
    public void sendSubtitle(Text subtitle) {
        Optional<ServerPlayerEntity> player = getEntity();

        if(player.isEmpty()) { return; }

        SubtitleS2CPacket packet = new SubtitleS2CPacket(subtitle);

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
}
