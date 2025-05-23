package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.screens.ItemRepresentable;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Role implements ItemRepresentable {

    Role(IndirectPlayer player, List<Ability.Factory> abilityFactories) {
        this.player = player;
        abilityFactories.forEach((factory) -> abilities.add(factory.create(player)));
    }

    public static Role load(NbtCompound nbt, IndirectPlayer player) {
        String roleName = nbt.getString("name");
        if(Objects.equals(roleName, "")) return null;
        Roles role = Roles.getRole(roleName);

        return role.factory.fromNBT(nbt, player);
    }

    private final IndirectPlayer player;

    private final List<Ability> abilities = new ArrayList<>();

    public abstract Faction getFaction();

    public List<Ability> getAbilities() {
        return this.abilities;
    }

    public abstract String getRawName();

    public abstract TextColor getColor();

    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putString("role",this.getRawName());
        return nbt;
    }

    public void giveItems() {}

    public void readNbt(NbtCompound nbt) {}

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
