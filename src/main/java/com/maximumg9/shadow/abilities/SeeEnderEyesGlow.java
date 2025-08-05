package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class SeeEnderEyesGlow extends Ability {
    private static final ItemStack ITEM_STACK;

    static {
        ITEM_STACK = new ItemStack(Items.ENDER_EYE);
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            Text.literal("See ender eyes glow")
                .styled(style -> style.withColor(Formatting.GREEN))
        );
        ITEM_STACK.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                Text.literal("See ender eyes glow")
                    .styled(style -> style.withColor(Formatting.GRAY)),
                    PassiveText()
            )
        );
    }

    public SeeEnderEyesGlow(IndirectPlayer player) {
        super(player);
    }

    @Override
    public Identifier getID() {
        return MiscUtil.shadowID("see_ender_eyes_glow");
    }

    @Override
    public boolean enderEyesGlow() {
        return true;
    }

    @Override
    public AbilityResult apply() {
        return AbilityResult.NO_CLOSE;
    }

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }
}
