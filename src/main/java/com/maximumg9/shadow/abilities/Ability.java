package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.screens.ItemRepresentable;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import org.jetbrains.annotations.Nullable;

public abstract class Ability implements ItemRepresentable {
    IndirectPlayer player;

    public Ability(IndirectPlayer player) {
        this.player = player;
    }

    public abstract String getID();

    public IndirectPlayer getPlayer() {
        return this.player;
    }
    public abstract void apply();
    public void init() {}
    public void deInit() {}
    public void onNight() {}
    public  void onDay() {}

    @FunctionalInterface
    public interface Factory {
        Ability create(@Nullable IndirectPlayer player);
    }
}
