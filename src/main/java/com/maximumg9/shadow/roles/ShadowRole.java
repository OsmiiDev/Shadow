package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.abilities.ToggleStrength;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ShadowRole extends Role {
    private static final List<Ability.Factory> ABILITY_FACTORIES = List.of(ToggleStrength::new);

    public ShadowRole(@Nullable IndirectPlayer player) {
        super(player,ABILITY_FACTORIES);
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

    public static final RoleFactory<ShadowRole> FACTORY = new Factory();
    private static class Factory implements RoleFactory<ShadowRole> {
        @Override
        public ShadowRole makeRole(@Nullable IndirectPlayer player) {
            return new ShadowRole(player);
        }

        public ShadowRole fromNBT(NbtCompound nbt, @Nullable IndirectPlayer player) {
            ShadowRole role = new ShadowRole(player);

            role.readNbt(nbt);

            return role;
        }
    }
}
