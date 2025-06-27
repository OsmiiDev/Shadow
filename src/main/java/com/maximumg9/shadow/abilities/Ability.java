package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.screens.ItemRepresentable;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public abstract class Ability implements ItemRepresentable {
    final IndirectPlayer player;

    public Ability(IndirectPlayer player) {
        this.player = player;
    }

    Shadow getShadow() { return player.getShadow(); }

    public abstract Identifier getID();

    public IndirectPlayer getPlayer() {
        return this.player;
    }
    public abstract void apply();
    public void init() {}
    public void deInit() {}
    public void onNight() {}
    public void onDay() {}
    public boolean allowSeeGlowing() { return false; }

    @FunctionalInterface
    public interface Factory {
        Ability create(@Nullable IndirectPlayer player);
    }
}
