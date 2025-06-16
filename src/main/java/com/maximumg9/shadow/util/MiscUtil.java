package com.maximumg9.shadow.util;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.ducks.ShadowProvider;
import com.maximumg9.shadow.screens.ItemRepresentable;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;

public abstract class MiscUtil {
    public static String padLeft(String original, char padding, int desiredLength) {
        if(original.length() >= desiredLength) return original;
        StringBuilder builder = new StringBuilder(original);
        for (int i = 0; i < desiredLength - builder.length(); i++) {
            builder.insert(0, padding);
        }
        return builder.toString();
    }

    public static Identifier shadowID(String id) {
        return Identifier.of("shadow",id);
    }

    private static final Style DEFAULT_STYLE = Style.EMPTY.withColor(Formatting.WHITE).withItalic(false);

    public static LoreComponent makeLore(MutableText... texts) {
        List<Text> lore = Arrays.stream(texts)
            .map(
                (text) -> (Text) text.setStyle(text.getStyle().withParent(DEFAULT_STYLE))
            ).toList();
        return new LoreComponent(lore, lore);
    }

    public static Shadow getShadow(MinecraftServer server) {
        return ((ShadowProvider) server).shadow$getShadow();
    }

    public static ItemStack getItemWithContext(ItemRepresentable item, ScreenHandlerContext context) {
        return context.get(
                        (world, blockPos) -> world.getRegistryManager()
                )
                .map(item::getAsItem)
                .orElse(getErrorItem());
    }

    public static ItemStack getErrorItem() {
        ItemStack item = Items.BARRIER.getDefaultStack();
        item.set(
                DataComponentTypes.ITEM_NAME,
                Text.literal("ERROR")
                        .styled(style -> style.withColor(Formatting.RED))
        );
        return item;
    }
}
