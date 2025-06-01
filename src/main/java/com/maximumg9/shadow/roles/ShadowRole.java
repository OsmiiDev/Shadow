package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.abilities.ToggleStrength;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
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
    public Faction getFaction() { return Faction.SHADOW; }

    @Override
    public String getRawName() { return "Shadow"; }

    @Override
    public void onNight() {
        this.player.sendSubtitle(
            Text.literal("It is now night, your power grows, it's your opportunity to kill")
                .styled((style) -> style.withColor(Formatting.GOLD)),
            CancelPredicates.IS_DAY
        );
        super.onNight();
    }

    @Override
    public void onDay() {
        this.player.sendSubtitle(
                Text.literal("It's now day")
                        .styled((style) -> style.withColor(Formatting.YELLOW)),
                CancelPredicates.IS_NIGHT
        );
        super.onDay();
    }

    @Override
    public TextColor getColor() { return TextColor.fromFormatting(Formatting.RED); }

    @Override
    public boolean cantSeeGlowingDuringNight() {
        return false;
    }

    public static final RoleFactory<ShadowRole> FACTORY = new Factory();

    private static class Factory implements RoleFactory<ShadowRole> {
        @Override
        public ShadowRole makeRole(@Nullable IndirectPlayer player) {
            return new ShadowRole(player);
        }
    }

    private static final ItemStack ITEM = new ItemStack(Items.NETHERITE_SWORD);
    static {
        ITEM.set(DataComponentTypes.ITEM_NAME,new ShadowRole(null).getName());
    }

    @Override
    public ItemStack getAsItem() { return ITEM.copy(); }
}
