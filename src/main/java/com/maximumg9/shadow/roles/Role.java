package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.abilities.NetherStarItem;
import com.maximumg9.shadow.screens.ItemRepresentable;
import com.maximumg9.shadow.util.NBTUtil;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
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

    final IndirectPlayer player;

    private final List<Ability> abilities = new ArrayList<>();

    public abstract Faction getFaction();

    public List<Ability> getAbilities() {
        return this.abilities;
    }

    public abstract String getRawName();

    public abstract TextColor getColor();

    public boolean cantSeeGlowingDuringNight() {
        return this.abilities.stream().noneMatch(Ability::allowSeeGlowing);
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putString("role",this.getRawName());
        return nbt;
    }

    public void onNight() {
        // Cursed forcing to send an update on the flags
        this.player.getPlayer().ifPresent(
                (p) -> p.getDataTracker().set(
                        Entity.FLAGS,
                        p.getDataTracker().get(Entity.FLAGS),
                        true
                )
        );
        this.abilities.forEach(Ability::onNight);
    }

    public void onDay() {
        this.player.getPlayer().ifPresent(
                (p) -> p.getDataTracker().set(
                        Entity.FLAGS,
                        p.getDataTracker().get(Entity.FLAGS),
                        true
                )
        );
        this.abilities.forEach(Ability::onDay);
    }

    public void readNbt(NbtCompound nbt) {}

    public void init() {
        player.giveItemNow(
                this.player.getShadow().config.food.foodGiver.apply(
                        this.player.getShadow().config.foodAmount
                )
        );

        player.giveItemNow(
            NBTUtil.flagAsInvisible(
                NBTUtil.addID(
                    Items.NETHER_STAR.getDefaultStack(),
                    NetherStarItem.ABILITY_STAR_ID)
            )
        );

        this.player.getPlayer().ifPresent(
            (p) -> p.getDataTracker().set(
                Entity.FLAGS,
                (byte) (p.getDataTracker().get(Entity.FLAGS) |
                        (1 << Entity.GLOWING_FLAG_INDEX)),
                true
            )
        );
        this.player.giveEffect(
            new StatusEffectInstance(
                StatusEffects.HASTE,
                -1,1,
                false,false,
                true
            ),
            CancelPredicates.NEVER_CANCEL
        );
        this.player.giveEffect(
            new StatusEffectInstance(
                StatusEffects.FIRE_RESISTANCE,
                10 * 20,0,
                false,false,
                true
            ),
            CancelPredicates.cancelOnPhaseChange(this.player.getShadow().state.phase)
        );
        this.abilities.forEach(Ability::init);
    }

    public void deInit() {
        this.abilities.forEach(Ability::deInit);
    }

    public Text getName() {
        return Text
                .literal(getRawName())
                .styled(
                        (style) -> style.withColor(getColor())
                );
    }
}
