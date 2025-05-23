package com.maximumg9.shadow.mixins;

import com.maximumg9.shadow.Eye;
import com.maximumg9.shadow.Shadow;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Inject(method="sendPickup",at=@At("HEAD"))
    private void pickupEnderEye(Entity item, int count, CallbackInfo ci) {
        Shadow shadow = getShadow(Objects.requireNonNull(item.getServer()));

        if(item instanceof ItemEntity) {
            List<Eye> eyesCopy = new ArrayList<>(shadow.state.eyes);
            for(Eye eye : eyesCopy) {
                if(eye.item().equals(item.getUuid())) {
                    eye.destroy(shadow);
                    shadow.state.eyes.remove(eye);
                }
            }
        }
    }
}
