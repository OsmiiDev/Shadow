package com.maximumg9.shadow.commands;

import com.maximumg9.shadow.ducks.ShadowProvider;
import com.maximumg9.shadow.util.ItemData;
import com.maximumg9.shadow.util.NBTUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class EnderEyeItem extends net.minecraft.item.EnderEyeItem {
    public EnderEyeItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if(!(world instanceof ServerWorld)) return super.use(world, user, hand);

        ItemStack item = user.getStackInHand(hand);
        EnderEyeData data = EnderEyeData.read(item);

        if(!data.isParticipationEye) return super.use(world, user, hand);

        EnderEyeData newData = new EnderEyeData(!data.participating,true);
        newData.write(item);

        ((ShadowProvider) world.getServer())
            .shadow$getShadow()
            .getIndirect((ServerPlayerEntity) user)
            .participating = newData.participating;

        return TypedActionResult.success(item);
    }

    public record EnderEyeData(boolean participating, boolean isParticipationEye) implements ItemData {

        private static final Text PARTICIPATING_TEXT = Text.literal("Participating").styled((style) -> style.withColor(Formatting.GREEN));
        private static final Text NOT_PARTICIPATING_TEXT = Text.literal("Not Participating").styled((style) -> style.withColor(Formatting.RED));

        @Override
        public void write(ItemStack stack) {
            NBTUtil.applyToStackCustomData(stack, (compound) -> {
                compound.putBoolean("participating",participating);
                compound.putBoolean("is_participation_eye",isParticipationEye);

                return compound;
            });

            if(this.isParticipationEye) {
                stack.apply(DataComponentTypes.ITEM_NAME, Text.literal("This text should never appear"),(text) -> {
                    if(participating) {
                        return PARTICIPATING_TEXT;
                    } else {
                        return NOT_PARTICIPATING_TEXT;
                    }
                });
            }
        }

        public static EnderEyeData read(ItemStack stack) {
            NbtCompound customData = NBTUtil.getCustomData(stack);

            boolean participating = customData.getBoolean("participating");
            boolean isParticipationEye = customData.getBoolean("is_participation_eye");

            return new EnderEyeData(participating,isParticipationEye);
        }
    }
}


