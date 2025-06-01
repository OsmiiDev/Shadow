package com.maximumg9.shadow.mixins;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.world.ServerChunkLoadingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

@Mixin(ServerChunkLoadingManager.EntityTracker.class)
public class EntityTrackerMixin {
    @Redirect(
        method = "sendToOtherNearbyPlayers",
        at = @At(
            value = "INVOKE",
                target = "Lnet/minecraft/server/network/PlayerAssociatedNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"
        )
    )
    public void sendToOtherNearbyPlayers(PlayerAssociatedNetworkHandler instance, Packet<?> packet) {
        if(packet.getPacketId() != PlayPackets.SET_ENTITY_DATA) {
            instance.sendPacket(packet);
            return;
        }
        Shadow shadow = getShadow(instance.getPlayer().getServer());
        if(!shadow.isNight()) {
            instance.sendPacket(packet);
            return;
        }
        IndirectPlayer player = shadow.getIndirect(instance.getPlayer());

        if(!(packet instanceof EntityTrackerUpdateS2CPacket originalPacket)) {
            instance.sendPacket(packet);
            return;
        }

        if(player.role == null || player.role.cantSeeGlowingDuringNight()) {
            EntityTrackerUpdateS2CPacket noGlowingPacket = new EntityTrackerUpdateS2CPacket(
                originalPacket.id(),
                originalPacket.trackedValues()
                    .stream()
                    .map(
                        (entry) -> {
                            if(entry.id() == Entity.FLAGS.id()) {
                                DataTracker.SerializedEntry<Byte> bEntry = (DataTracker.SerializedEntry<Byte>) entry;

                                return new DataTracker.SerializedEntry<>(
                                        bEntry.id(),
                                        bEntry.handler(),
                                        (byte) (bEntry.value() & ~(1 << Entity.GLOWING_FLAG_INDEX))
                                );
                            }
                            return entry;
                        }
                    )
                    .toList()
            );
            instance.sendPacket(noGlowingPacket);
        } else {
            instance.sendPacket(originalPacket);
        }
    }
}
