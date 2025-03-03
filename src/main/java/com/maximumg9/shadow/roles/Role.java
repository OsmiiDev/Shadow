package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.util.IndirectPlayer;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.util.ArrayList;
import java.util.List;

public abstract class Role {

    Role(Shadow shadow, IndirectPlayer player, List<Ability.Factory> abilityFactories) {
        this.player = player;
        abilityFactories.forEach((factory) -> abilities.add(factory.create(shadow,player)));
    }

    private final IndirectPlayer player;

    private final List<Ability> abilities = new ArrayList<>();

    public abstract Faction getFaction();

    public List<Ability> getAbilities() {
        return this.abilities;
    }

    public abstract String getRawName();

    public abstract TextColor getColor();

    public void init() {}

    public IndirectPlayer getPlayer() {
        return this.player;
    }

    public Text getName() {
        return Text
                .literal(getRawName())
                .styled(
                        (style) -> style.withColor(getColor())
                );
    }
}
