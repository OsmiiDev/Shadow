package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.screens.ItemRepresentable;
import com.maximumg9.shadow.util.IndirectPlayer;
import net.minecraft.text.Text;

public interface Ability extends ItemRepresentable {
    String getID();
    IndirectPlayer getPlayer();
    void apply();

    @FunctionalInterface
    interface Factory {
        Ability create(Shadow shadow, IndirectPlayer player);
    }
}
