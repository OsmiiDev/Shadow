package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.util.IndirectPlayer;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Villager extends Role {

    public Villager(@Nullable Shadow shadow,@Nullable IndirectPlayer player) {
        super(shadow, player, List.of());
    }

    @Override
    public Faction getFaction() {
        return Faction.VILLAGER;
    }
    @Override
    public String getRawName() {
        return "Villager";
    }

    @Override
    public TextColor getColor() {
        return TextColor.fromFormatting(Formatting.GREEN);
    }
}
