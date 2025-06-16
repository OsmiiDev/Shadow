package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.util.MiscUtil;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SeeGlowing extends Ability {
    private static final ItemStack ITEM_STACK;

    static {
        ITEM_STACK = new ItemStack(Items.ENDER_EYE);
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            Text.literal("See Glowing During Night")
                .styled(style -> style.withColor(Formatting.GOLD))
        );
        ITEM_STACK.set(
            DataComponentTypes.LORE,
            MiscUtil.makeLore(
                Text.literal("See glowing during the night when others can't")
                    .styled(style -> style.withColor(Formatting.GRAY)),
                Text.literal("[PASSIVE]").styled((style) -> style.withColor(Formatting.BLUE))
            )
        );
    }

    public SeeGlowing(IndirectPlayer player) {
        super(player);
    }

    @Override
    public String getID() {
        return "see_glowing";
    }

    @Override
    public void apply() {}

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        return ITEM_STACK.copy();
    }
}
