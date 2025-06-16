package com.maximumg9.shadow.screens;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.roles.Role;
import com.maximumg9.shadow.roles.RoleSlot;
import com.maximumg9.shadow.roles.Roles;
import com.maximumg9.shadow.util.Delay;
import com.maximumg9.shadow.util.MiscUtil;
import com.mojang.logging.LogUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

public class RoleSlotScreenHandler extends ScreenHandler {
    private static final int SIZE = 9 * 6;

    private final RoleSlot slot;
    private final Inventory inventory;
    private final ScreenHandlerContext context;

    private int page = 0;

    private RoleSlotScreenHandler(int syncID, RoleSlot slot, ScreenHandlerContext context) {
        super(ScreenHandlerType.GENERIC_9X6, syncID);
        this.context = context;

        this.slot = slot;

        this.inventory = new SimpleInventory(SIZE);

        initSlots();

        buildUI();
    }

    private void initSlots() {
        for(int k = 0; k < SIZE; ++k) {
            this.addSlot(new Slot(inventory, k, 0, 0));
        }
    }
    // What does the UI look like? Well here it is:
    // ,--------------------------------------------,
    // | Up | Up | Up | Up | Up | Up | Up | Up |Next|
    // |Role|Role|Role|Role|Role|Role|Role|Role|    |
    // |Down|Down|Down|Down|Down|Down|Down|Down|    |
    // | Up | Up | Up | Up | Up | Up | Up | Up |    |
    // |Role|Role|Role|Role|Role|Role|Role|Role|    |
    // |Down|Down|Down|Down|Down|Down|Down|Down|Exit|
    // '--------------------------------------------'

    // The role weight items are made by buildPage
    // The Next and Exit items are made by buildUI

    private static final Text NEXT_PAGE_TEXT = Text.literal("Next Page").styled((style) -> style.withColor(Formatting.GOLD));
    private static final Text LAST_PAGE_TEXT = Text.literal("Last Page").styled((style) -> style.withColor(Formatting.GOLD));
    private static final Text EXIT_SCREEN_TEXT = Text.literal("Return to role menu").styled((style) -> style.withColor(Formatting.RED));

    private void buildUI() {
        if(this.page < (Roles.values().length / 9) / 2) {
            ItemStack nextPageStack = Items.SPECTRAL_ARROW.getDefaultStack();
            nextPageStack.set(DataComponentTypes.ITEM_NAME, NEXT_PAGE_TEXT);
            this.inventory.setStack(8,nextPageStack);
        }
        if(this.page > 0) {
            ItemStack nextPageStack = Items.SPECTRAL_ARROW.getDefaultStack();
            nextPageStack.set(DataComponentTypes.ITEM_NAME, LAST_PAGE_TEXT);
            this.inventory.setStack(9 + 8,nextPageStack);
        }

        ItemStack exitStack = Items.BARRIER.getDefaultStack();
        exitStack.set(DataComponentTypes.ITEM_NAME, EXIT_SCREEN_TEXT);
        this.inventory.setStack(SIZE - 1, exitStack);

        buildPage();
    }

    private static final Text UP_TEXT = Text.literal("Increase Weight").styled((style) -> style.withColor(Formatting.GREEN));
    private static final Text DOWN_TEXT = Text.literal("Decrease Weight").styled((style) -> style.withColor(Formatting.RED));

    private void buildPage() {
        int startRoleIndicies = page * 2 * 9;

        for(int row = 0; row < 2; row++) {
            for(int column = 0; column < 8; column++) {
                int roleIndex = startRoleIndicies + row * 8 + column;

                if(roleIndex >= Roles.values().length) break;

                Roles role = Roles.values()[roleIndex];
                Role proxyRole = role.factory.makeRole(null);

                int weight = slot.getWeight(role);

                ItemStack upStack = Items.GREEN_CONCRETE.getDefaultStack();
                upStack.set(DataComponentTypes.ITEM_NAME, UP_TEXT);
                this.inventory.setStack((row*3) * 9 + column,upStack);

                ItemStack roleStack = MiscUtil.getItemWithContext(proxyRole,this.context);
                roleStack.set(
                    DataComponentTypes.LORE,
                    MiscUtil.makeLore(
                        Text.literal("Weight: ")
                            .styled((style) -> style.withColor(Formatting.WHITE).withItalic(false))
                            .append(
                                Text.literal(String.valueOf(weight))
                            )
                    )
                );
                this.inventory.setStack((row*3 + 1)* 9 + column,roleStack);

                ItemStack downStack = Items.RED_CONCRETE.getDefaultStack();
                downStack.set(DataComponentTypes.ITEM_NAME, DOWN_TEXT);
                this.inventory.setStack((row*3 + 2) * 9 + column,downStack);
            }
        }
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if(player instanceof ServerPlayerEntity sPlayer) {
            // Yeah, I'm a bit of a math guy
            if(slotIndex % 9 < 8) {
                // Oh, also sorry to whoever works with this next, but I just wanted to do it this way :P

                int operation = (slotIndex / 9) % 3;

                int roleRow = (slotIndex / 9) / 3;

                int roleColumn = (slotIndex - (slotIndex / 9)) % 8;

                int roleIndex = roleRow * 8 + roleColumn;

                if(roleIndex >= Roles.values().length) return;

                Roles role = Roles.values()[roleIndex];
                LogUtils.getLogger().info(role.name);

                if(operation == 0) {
                    this.slot.setWeight(role,this.slot.getWeight(role) + 1);
                } else if(operation == 2 && this.slot.weightSum() > 1) {
                    this.slot.setWeight(role,this.slot.getWeight(role) - 1);
                }
                // nothing for clicking the role icon for now!

                this.buildPage();
                this.syncState();
            } else if(slotIndex == 8) {
                if(this.page >= (Roles.values().length / 9) / 2) return;
                this.page++;
                this.buildUI();
                this.syncState();
            } else if(slotIndex == 9 + 8) {
                if(this.page <= 0) return;
                this.page--;
                this.buildUI();
                this.syncState();
            } else if(slotIndex == SIZE - 1) {
                sPlayer.closeHandledScreen();
            }
        }
    }

    @Override
    public void onClosed(PlayerEntity player) {
        if(player instanceof ServerPlayerEntity sPlayer) {
            Shadow shadow = getShadow(sPlayer.getServer());
            shadow.saveAsync();

            shadow.addTickable(Delay.instant(
                    () -> shadow.config.roleManager.showRoleListIndex(sPlayer, true)
            ));
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    public record Factory(Text name, RoleSlot slot) implements NamedScreenHandlerFactory {

        @Override
        public Text getDisplayName() {
            return name;
        }

        @Override
        public @NotNull ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
            return new RoleSlotScreenHandler(
                syncId, slot,
                ScreenHandlerContext.create(player.getWorld(),player.getBlockPos())
            );
        }
    }
}
