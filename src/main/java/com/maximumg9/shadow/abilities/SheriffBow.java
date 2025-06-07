package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.util.NBTUtil;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;

import java.util.List;

public class SheriffBow extends Ability {
    private static final ItemStack ITEM_STACK;
    static {
        ITEM_STACK = new ItemStack(Items.BOW,1);
        ITEM_STACK.set(
            DataComponentTypes.LORE,
            new LoreComponent(
                List.of(
                    Text.literal("A bow that can instantly kill whoever you shoot it at.")
                            .styled(style -> style.withColor(Formatting.GRAY).withItalic(false)),
                    Text.literal("If shot a villager, the owner and shooter die too.")
                            .styled(style -> style.withColor(Formatting.GRAY).withItalic(false)),
                    Text.literal("[ITEM]")
                            .styled(style -> style.withColor(Formatting.LIGHT_PURPLE).withItalic(false))
                )
            )
        );
        ITEM_STACK.set(
            DataComponentTypes.ITEM_NAME,
            Text.literal("Sheriff Bow").styled((style) -> style.withColor(Formatting.GOLD))
        );
        ITEM_STACK.set(
                DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP,
                Unit.INSTANCE
        );
    }

    public SheriffBow(IndirectPlayer player) {
        super(player);
    }

    private static ItemStack createSheriffBow(IndirectPlayer player) {
        ItemStack item = new ItemStack(Items.BOW);
        NBTUtil.addID(item, ITEM_ID);
        NBTUtil.applyToStackCustomData(item,compound -> {
            compound.putUuid("owner", player.playerUUID);
            return compound;
        });
        item.set(
            DataComponentTypes.ITEM_NAME,
            Text.literal("Sheriff Bow").styled(style -> style.withColor(Formatting.GOLD))
        );
        return item;
    }

    @Override
    public void init() {
        player.giveItem(createSheriffBow(player), CancelPredicates.cancelOnPhaseChange(player.getShadow().state.phase));
        super.init();
    }

    @Override
    public void deInit() {
        player.scheduleOnLoad(
            (player) ->
                player.getInventory()
                    .remove((item) -> player.getUuid().equals(NBTUtil.getCustomData(item).get("owner"))
                            ,
            1,
                player.playerScreenHandler.getCraftingInput()),
            CancelPredicates.cancelOnPhaseChange(player.getShadow().state.phase));
        super.deInit();
    }
    private static final String ID = "sheriff_bow";
    public static final Identifier ITEM_ID = Identifier.of("shadow", ID);

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public void apply() {}

    @Override
    public ItemStack getAsItem(RegistryWrapper.WrapperLookup registries) {
        ItemStack item = ITEM_STACK.copy();
        item.addEnchantment(registries.getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING),1);
        return item;
    }
}
