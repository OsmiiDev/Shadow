package com.maximumg9.shadow.screens;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class DecisionScreenHandler<V extends ItemRepresentable> extends ScreenHandler {

    public final HashMap<Integer,V> decisionResultHashMap = new HashMap<>();

    public final Consumer<V> resultCallback;

    private final SimpleInventory inventory;

    protected DecisionScreenHandler(int syncId, Consumer<V> resultCallback, List<V> values) {
        super(ScreenHandlerType.GENERIC_9X1, syncId);
        this.resultCallback = resultCallback;

        this.inventory = new SimpleInventory(9);

        initSlots();

        int i=0;
        for(V value : values) {
            this.getSlot(i).insertStack(value.getAsItem());
            decisionResultHashMap.put(i,value);
        }
    }

    public void initSlots() {
        for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(inventory, k, 0, 0));
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return null;
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if(player instanceof ServerPlayerEntity sPlayer) {
            V value = decisionResultHashMap.get(slotIndex);
            sPlayer.closeHandledScreen();
            this.resultCallback.accept(value);
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    public static class Factory<V extends ItemRepresentable> implements NamedScreenHandlerFactory {

        public final Consumer<V> resultCallback;
        private final Text name;
        private final List<V> values;

        public Factory(Text name, Consumer<V> resultCallback, List<V> values) {
            this.name = name;
            this.resultCallback = resultCallback;
            this.values = values;
        }

        @Override
        public Text getDisplayName() {
            return name;
        }

        @Override
        public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
            return new DecisionScreenHandler<>(syncId, this.resultCallback, values);
        }
    }
}
