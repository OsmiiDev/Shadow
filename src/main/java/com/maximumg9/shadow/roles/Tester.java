package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.abilities.TestAbility;
import com.maximumg9.shadow.util.IndirectPlayer;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Tester extends Role {
    private static final List<Ability.Factory> ABILITY_FACTORIES = List.of(TestAbility::new);

    public Tester(@Nullable Shadow shadow, @Nullable IndirectPlayer player) {
        super(shadow, player, ABILITY_FACTORIES);
    }

    @Override
    public Faction getFaction() {
        return Faction.NEUTRAL;
    }

    @Override
    public String getRawName() {
        return "Tester";
    }

    @Override
    public TextColor getColor() {
        return TextColor.fromFormatting(Formatting.YELLOW);
    }
}
