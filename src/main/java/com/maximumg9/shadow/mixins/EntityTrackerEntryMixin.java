package com.maximumg9.shadow.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {
    @ModifyArg(method = "sendPackets",at= @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/EntityTrackerUpdateS2CPacket;<init>(ILjava/util/List;)V"))
    public List<DataTracker.SerializedEntry<?>> newEntityTrackerPacket(List<DataTracker.SerializedEntry<?>> changedEntries, @Local(argsOnly = true) ServerPlayerEntity player) {
        Shadow shadow = getShadow(player.getServer());
        if(!shadow.isNight()) {
            return changedEntries;
        }
        IndirectPlayer Iplayer = shadow.getIndirect(player);

        if(Iplayer.role == null || Iplayer.role.cantSeeGlowingDuringNight()) {
            changedEntries.replaceAll(
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
            );
        }
        return changedEntries;
    }
}
