package com.maximumg9.shadow.mixins;

import com.maximumg9.shadow.GamePhase;
import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.ducks.ShadowProvider;
import com.maximumg9.shadow.roles.Faction;
import com.maximumg9.shadow.roles.Spectator;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

@Mixin(ServerPlayerEntity.class)
public abstract class PlayerDeathMixin extends PlayerEntity {
    @org.spongepowered.asm.mixin.Shadow
    @Final public MinecraftServer server;

    @org.spongepowered.asm.mixin.Shadow public abstract boolean changeGameMode(GameMode gameMode);

    public PlayerDeathMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "onDeath",at=@At("HEAD"))
    public void onDeath(DamageSource damageSource, CallbackInfo ci) {
        Shadow shadow = ((ShadowProvider) this.server).shadow$getShadow();

        GameRules.BooleanRule showDeathMessage = this.getWorld().getGameRules().get(GameRules.SHOW_DEATH_MESSAGES);
        if(shadow.state.phase == GamePhase.PLAYING) {
            showDeathMessage.set(false,this.server);

            MutableText name = Team.decorateName(this.getScoreboardTeam(), this.getName());

            name
                .append(Text.of(" died. They were a "))
                .append(
                    Objects.requireNonNull(
                        shadow
                            .indirectPlayerManager
                            .getIndirect((ServerPlayerEntity) (Object) this)
                            .role
                    ).getName()
                );

            this.server.getPlayerManager().broadcast(name, false);

            checkWin(shadow);

        } else {
            showDeathMessage.set(true,this.server);
        }

        IndirectPlayer player = shadow.getIndirect((ServerPlayerEntity) ((Object) this));

        player.role = new Spectator(player);
    }

    @Inject(method="onSpawn",at=@At("TAIL"))
    public void onSpawn(CallbackInfo ci) {
        Shadow shadow = ((ShadowProvider) this.server).shadow$getShadow();
        IndirectPlayer iPlayer = shadow.getIndirect((ServerPlayerEntity) (Object) this);
        if(iPlayer.role.getFaction() == Faction.SPECTATOR) {
            this.changeGameMode(GameMode.SPECTATOR);
        }
    }

    @Unique
    public void checkWin(Shadow shadow) {
        long villagers = shadow.getOnlinePlayers().stream().filter((player) -> player.role.getFaction() == Faction.VILLAGER).count();
        long shadows = shadow.getOnlinePlayers().stream().filter((player) -> player.role.getFaction() == Faction.SHADOW).count();

        if(villagers == 0 && shadows == 0) {
            shadow.endGame(List.of(),null,null);
        }

        if(villagers == 0) {
            shadow.endGame(shadow.getOnlinePlayers().stream().filter((player) -> player.role.getFaction() == Faction.SHADOW).toList(), Faction.SHADOW, null);
        }
        if(shadows == 0) {
            shadow.endGame(shadow.getOnlinePlayers().stream().filter((player) -> player.role.getFaction() == Faction.VILLAGER).toList(), Faction.VILLAGER, null);
        }
    }
}
