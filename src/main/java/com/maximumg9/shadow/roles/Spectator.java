package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Spectator extends Role {
    public Spectator(@Nullable IndirectPlayer player) {
        super(player, List.of());
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

    public static final RoleFactory<Spectator> FACTORY = new Factory();
    private static class Factory implements RoleFactory<Spectator> {
        @Override
        public Spectator makeRole(@Nullable IndirectPlayer player) {
            return new Spectator(player);
        }

        public Spectator fromNBT(NbtCompound nbt, @Nullable IndirectPlayer player) {
            Spectator role = new Spectator(player);

            role.readNbt(nbt);

            return role;
        }
    }
}
