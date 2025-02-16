package com.maximumg9.shadow.screens;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class DecisionScreenHandler<V extends ItemRepresentable> extends ScreenHandler {

    public final HashMap<Integer,V> decisionResultHashMap = new HashMap<>();

    public final Consumer<V> resultCallback;

    protected DecisionScreenHandler(int syncId, Consumer<V> resultCallback) {
        super(ScreenHandlerType.GENERIC_9X1, syncId);
        this.resultCallback = resultCallback;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return null;
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        V value = decisionResultHashMap.get(slotIndex);
        this.resultCallback.accept(value);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return false;
    }

    public static class Factory<V extends ItemRepresentable> implements NamedScreenHandlerFactory {

        public final Consumer<V> resultCallback;
        private final Text name;

        public Factory(Text name, Consumer<V> resultCallback, List<V> values) {
            this.name = name;
            this.resultCallback = resultCallback;
        }

        @Override
        public Text getDisplayName() {
            return name;
        }

        @Override
        public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
            return new DecisionScreenHandler<>(syncId, this.resultCallback);
        }
    }
}
