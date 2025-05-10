package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.screens.ItemRepresentable;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import org.jetbrains.annotations.Nullable;

public interface Ability extends ItemRepresentable {
    String getID();
    IndirectPlayer getPlayer();
    void apply();
    void init();
    void deInit();

    @FunctionalInterface
    interface Factory {
        Ability create(@Nullable IndirectPlayer player);
    }
}
