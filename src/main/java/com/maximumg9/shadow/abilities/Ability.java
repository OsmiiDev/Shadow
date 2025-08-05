package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.screens.ItemRepresentable;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
    public AbilityResult triggerApply() {
        return apply();
    }
    public abstract AbilityResult apply();
    public void init() {}
    public void deInit() {}
    public void onNight() {}
    public void onDay() {}
    public boolean allowSeeGlowing() { return false; }
    public boolean enderEyesGlow() { return false; }


    private static final Text PASSIVE_TEXT = Text.literal("[PASSIVE]")
        .styled((style) -> style.withColor(Formatting.BLUE));
    private static final Text ITEM_TEXT = Text.literal("[ITEM]")
        .styled(style -> style.withColor(Formatting.GOLD));
    private static final Text ABILITY_TEXT = Text.literal("[ABILITY]")
        .styled((style) -> style.withColor(Formatting.DARK_PURPLE));

    static MutableText PassiveText() { return PASSIVE_TEXT.copy();}
    static MutableText ItemText() { return ITEM_TEXT.copy(); }
    static MutableText AbilityText() { return ABILITY_TEXT.copy();}

    @FunctionalInterface
    public interface Factory {
        Ability create(@Nullable IndirectPlayer player);
    }
}
