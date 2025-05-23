package com.maximumg9.shadow.mixins;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.ducks.ShadowProvider;
import com.maximumg9.shadow.roles.Faction;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Objects;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

@Mixin(EnderDragonEntity.class)
public class DragonDeathMixin extends MobEntity {
    protected DragonDeathMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        Shadow shadow = getShadow(Objects.requireNonNull(this.getServer()));

        shadow.endGame(shadow.getOnlinePlayers().stream().filter((player) -> {
            assert player.role != null;
            return player.role.getFaction() == Faction.VILLAGER;
        }).toList(), Faction.VILLAGER, null);
    }
}
