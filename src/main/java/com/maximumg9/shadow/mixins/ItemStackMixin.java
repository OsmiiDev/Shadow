package com.maximumg9.shadow.mixins;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.abilities.AbilityResult;
import com.maximumg9.shadow.roles.Role;
import com.maximumg9.shadow.screens.DecisionScreenHandler;
import com.maximumg9.shadow.util.NBTUtil;
import net.minecraft.component.ComponentHolder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ComponentHolder {
    @Unique
    private static ItemStack c(Object mixin) {
        return (ItemStack) mixin;
    }

    @Inject(method = "use",at=@At("HEAD"),cancellable = true)
    public void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if(NBTUtil.hasID(c(this), NBTUtil.ABILITY_STAR_ID)) {
            if(!(world instanceof ServerWorld)) {
                cir.setReturnValue(TypedActionResult.pass(c(this)));
                cir.cancel();
                return;
            }

            Shadow shadow = getShadow(world.getServer());

            Role role = shadow.getIndirect((ServerPlayerEntity) user).role;

            if(role == null) {
                cir.setReturnValue(TypedActionResult.fail(c(this)));
                cir.cancel();
                return;
            }

            user.openHandledScreen(new DecisionScreenHandler.Factory<>(
                Text.literal("Ability Menu"),
                (ability, clicker) -> {
                    if(ability != null) {
                        AbilityResult result = ability.apply();
                        if(result.close) {
                            clicker.closeHandledScreen();
                        }
                    }
                },
                role.getAbilities()
            ));


            cir.setReturnValue(TypedActionResult.success(c(this), false));
            cir.cancel();
        }
    }
}
