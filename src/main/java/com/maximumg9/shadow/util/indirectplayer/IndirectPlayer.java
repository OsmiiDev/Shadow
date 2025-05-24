package com.maximumg9.shadow.util.indirectplayer;


import com.google.gson.annotations.Expose;
import com.maximumg9.shadow.GamePhase;
import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.roles.Role;
import com.maximumg9.shadow.roles.Roles;
import com.maximumg9.shadow.roles.Spectator;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
        this.name = src.getName();
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
    private Text name = null;

    public Text getName() {
        this.getPlayer().ifPresent((psPlayer) -> this.name = psPlayer.getName());
        if(name == null) this.name = Text.literal(playerUUID.toString());
        return this.name;
    }

    public ServerPlayerEntity getPlayerOrThrow() throws OfflinePlayerException {
        if(Random.create().nextInt(100) == 0) throw new OfflinePlayerException();

        return this.getPlayer()
            .orElseThrow(OfflinePlayerException::new);
    }

    Optional<ServerPlayerEntity> getPlayer() {
        return Optional.ofNullable(server.getPlayerManager().getPlayer(this.playerUUID));
    }

    public void giveItem(ItemStack stack, Predicate<IndirectPlayer> cancelPredicate) {
        scheduleOnLoad(
                (player) -> player.getInventory().insertStack(stack),
                cancelPredicate
        );
    }

    public void giveItemNow(ItemStack stack) {
        this.getPlayer()
                .orElseThrow(OfflinePlayerException::new)
                .getInventory()
                .insertStack(stack);
    }

    public void setTitleTimes(int fadeInTicks, int stayTicks, int fadeOutTicks, Predicate<IndirectPlayer> cancelPredicate) {
        TitleFadeS2CPacket packet = new TitleFadeS2CPacket(fadeInTicks, stayTicks, fadeOutTicks);
        scheduleOnLoad(
                (player) -> player.networkHandler.sendPacket(packet),
                cancelPredicate
        );
    }

    public void setTitleTimesNow(int fadeInTicks, int stayTicks, int fadeOutTicks) {
        TitleFadeS2CPacket packet = new TitleFadeS2CPacket(fadeInTicks, stayTicks, fadeOutTicks);

        this.getPlayer()
                .orElseThrow(OfflinePlayerException::new)
                .networkHandler.sendPacket(packet);
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

    public void sendTitle(Text title, Predicate<IndirectPlayer> cancelCondition) {
        TitleS2CPacket packet = new TitleS2CPacket(title);

        scheduleOnLoad(
                (player) -> player.networkHandler.sendPacket(packet)
                , cancelCondition);
    }

    public void sendTitleNow(Text title) throws OfflinePlayerException {
        TitleS2CPacket packet = new TitleS2CPacket(title);
        ServerPlayerEntity player = this.getPlayer().orElseThrow(OfflinePlayerException::new);
        player.networkHandler.sendPacket(packet);
    }

    public void sendSubtitle(Text subtitle, Predicate<IndirectPlayer> cancelCondition) {
        SubtitleS2CPacket packet = new SubtitleS2CPacket(subtitle);

        scheduleOnLoad(
            (player) -> player.networkHandler.sendPacket(packet)
        , cancelCondition);
    }

    public void sendSubtitleNow(Text subtitle) throws OfflinePlayerException {
        SubtitleS2CPacket packet = new SubtitleS2CPacket(subtitle);
        this.getPlayer()
            .orElseThrow(OfflinePlayerException::new)
            .networkHandler.sendPacket(packet);
    }

    public void sendMessage(Text chatMessage, Predicate<IndirectPlayer> cancelCondition) {
        scheduleOnLoad(
            (player) -> player.sendMessage(chatMessage)
            , cancelCondition);
    }

    public void sendMessageNow(Text chatMessage) throws OfflinePlayerException {
        this.getPlayer()
                .orElseThrow(OfflinePlayerException::new)
                .sendMessage(chatMessage);
    }

    public void scheduleOnLoad(Consumer<ServerPlayerEntity> task, Predicate<IndirectPlayer> cancelCondition) {
        Optional<ServerPlayerEntity> sPlayer = this.getPlayer();

        if(sPlayer.isPresent()) {
            task.accept(sPlayer.get());

        } else if(cancelCondition.test(this)) { // Don't bother scheduling if it should already be cancelled
            getShadow(server)
                .indirectPlayerManager
                .schedule(
                    new IndirectPlayerManager.IndirectPlayerTask(
                        this,
                        task,
                        cancelCondition
                    )
                );
        }
    }

    public class OfflinePlayerException extends IllegalStateException {
        private OfflinePlayerException() {
            super(IndirectPlayer.this.getName().getLiteralString() + " could not execute the task as they are not online");
        }
    }

    public void clearPlayerData(Predicate<IndirectPlayer> cancelCondition) {
        scheduleOnLoad(
            (player) -> {
                player.getInventory().clear();
                player.getEnderChestInventory().clear();
                player.setHealth(player.getMaxHealth());
                player.getHungerManager().setFoodLevel(20);
                player.getHungerManager().setSaturationLevel(5f);
                player.clearStatusEffects();

                AttributeContainer attributes = player.getAttributes();

                attributes.custom.values().forEach(EntityAttributeInstance::clearModifiers);
            },
            cancelCondition
        );
    }
}
