package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.roles.Role;
import com.maximumg9.shadow.screens.DecisionScreenHandler;
import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.NBTUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

public class NetherStarItem extends Item {
    public NetherStarItem(Item.Settings settings) {
        super(settings);
    }

    public static Identifier ABILITY_STAR_ID = MiscUtil.shadowID("ability_star");

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if(!(world instanceof ServerWorld)) return TypedActionResult.pass(stack);
        if(!NBTUtil.hasID(stack,ABILITY_STAR_ID)) return TypedActionResult.pass(stack);

        Shadow shadow = getShadow(world.getServer());

        Role role = shadow.getIndirect((ServerPlayerEntity) user).role;

        if(role == null) return TypedActionResult.fail(stack);

        user.openHandledScreen(new DecisionScreenHandler.Factory<>(
                Text.literal("Ability Menu"),
                (ability, clicker) -> {
                    if(ability != null) {
                        ability.apply();
                    }
                },
                role.getAbilities()
        ));

        return TypedActionResult.success(stack,false);
    }
}
