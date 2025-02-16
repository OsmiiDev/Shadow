package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.abilities.TestAbility;
import com.maximumg9.shadow.util.IndirectPlayer;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ShadowRole extends Role {

    private static final List<Ability.Factory> ABILITY_FACTORIES = List.of();

    public ShadowRole(@Nullable Shadow shadow, @Nullable IndirectPlayer player) {
        super(shadow,player,ABILITY_FACTORIES);
    }

    @Override
    public Faction getFaction() {
        return Faction.SHADOW;
    }

    @Override
    public String getRawName() {
        return "Shadow";
    }

    @Override
    public TextColor getColor() {
        return TextColor.fromFormatting(Formatting.RED);
    }
}
