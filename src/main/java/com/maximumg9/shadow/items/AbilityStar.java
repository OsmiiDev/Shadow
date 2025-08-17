package com.maximumg9.shadow.items;

import com.maximumg9.shadow.ItemUseCallback;
import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.abilities.AbilityResult;
import com.maximumg9.shadow.roles.Role;
import com.maximumg9.shadow.screens.DecisionScreenHandler;
import com.maximumg9.shadow.util.MiscUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Unique;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

public class AbilityStar implements ItemUseCallback {
    @Unique
    public static Identifier ID = MiscUtil.shadowID("ability_star");
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if(!(world instanceof ServerWorld)) {
            return null;
        }

        ItemStack stack = user.getStackInHand(hand);

        Shadow shadow = getShadow(world.getServer());

        Role role = shadow.getIndirect((ServerPlayerEntity) user).role;

        if(role == null) {
            return TypedActionResult.fail(stack);
        }

        user.openHandledScreen(new DecisionScreenHandler.Factory<>(
            Text.literal("Ability Menu"),
            (ability, clicker) -> {
                if(ability != null) {
                    AbilityResult result = ability.triggerApply();
                    if(result.close) {
                        clicker.closeHandledScreen();
                    }
                }
            },
            role.getAbilities(),
            false
        ));


        return TypedActionResult.success(stack, false);
    }
}
