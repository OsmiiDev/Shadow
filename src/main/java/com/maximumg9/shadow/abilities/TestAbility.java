package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.util.IndirectPlayer;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class TestAbility implements Ability {

    public static final Ability.Factory FACTORY = TestAbility::new;

    public TestAbility(@Nullable Shadow shadow, @Nullable IndirectPlayer player) {
        this.shadow = shadow;
        this.player = player;
    }

    private final Shadow shadow;
    private final IndirectPlayer player;

    @Override
    public String getID() {
        return "test";
    }

    @Override
    public IndirectPlayer getPlayer() {
        return this.player;
    }

    private void sendAbilityMessage(Text message) {
        this.player.getEntity().ifPresent(
                serverPlayerEntity ->
                        serverPlayerEntity.sendMessage(message)
        );
    }

    @Override
    public void apply() {
        sendAbilityMessage(Text.literal(""));
    }

    @Override
    public ItemStack getAsItem() {
        return new ItemStack(Registries.ITEM.getEntry(Items.NAME_TAG),
                1,
                ComponentChanges.builder()
                        .add(DataComponentTypes.ITEM_NAME,Text.literal("Test"))
                        .build()
        );
    }
}
