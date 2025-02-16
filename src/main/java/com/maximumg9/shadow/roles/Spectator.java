package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.util.IndirectPlayer;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.List;

public class Spectator extends Role {
    public Spectator(Shadow shadow, IndirectPlayer player) {
        super(shadow, player, List.of());
    }

    @Override
    public Faction getFaction() {
        return Faction.SPECTATOR;
    }

    @Override
    public String getRawName() {
        return "Spectator";
    }

    @Override
    public TextColor getColor() {
        return TextColor.fromFormatting(Formatting.GRAY);
    }
}
