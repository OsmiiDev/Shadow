package com.maximumg9.shadow.mixins;

import com.maximumg9.shadow.GamePhase;
import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.ducks.ShadowProvider;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @org.spongepowered.asm.mixin.Shadow public ServerPlayerEntity player;

    @org.spongepowered.asm.mixin.Shadow public abstract void requestTeleport(double x, double y, double z, float yaw, float pitch);

    @org.spongepowered.asm.mixin.Shadow
    private static double clampHorizontal(double d) {
        return 0;
    }

    @org.spongepowered.asm.mixin.Shadow
    private static double clampVertical(double d) {
        return 0;
    }

    @Inject(method="onPlayerMove",at=@At("HEAD"), cancellable = true)
    public void restrictMovementOnLocationSelect(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        Shadow shadow = ((ShadowProvider) this.player.server).shadow$getShadow();

        if(shadow.phase != GamePhase.LOCATION_SELECTED) return;
        if(!this.player.hasPermissionLevel(2)) return;
        if(this.player.isInCreativeMode() || this.player.isSpectator()) return;

        double x = clampHorizontal(packet.getX(this.player.getX()));
        double y = clampVertical(packet.getY(this.player.getY()));
        double z = clampHorizontal(packet.getZ(this.player.getZ()));

        float yaw = MathHelper.wrapDegrees(packet.getYaw(this.player.getYaw()));
        float pitch = MathHelper.wrapDegrees(packet.getPitch(this.player.getPitch()));

        if(
            this.player.getX() == x &&
            this.player.getY() == y &&
            this.player.getZ() == z
        ) return;

        this.requestTeleport(this.player.getX(), this.player.getY(), this.player.getZ(), yaw, pitch);
        ci.cancel();
    }
}
