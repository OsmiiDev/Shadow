package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.abilities.SeeGlowing;
import com.maximumg9.shadow.abilities.ToggleStrength;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.stream.Stream;

public abstract class AbstractShadow extends Role {
    private static final List<Ability.Factory> ABILITY_FACTORIES = List.of(ToggleStrength::new, SeeGlowing::new);

    AbstractShadow(IndirectPlayer player, List<Ability.Factory> abilityFactories) {
        super(
            player,
            Stream.concat(
                abilityFactories.stream(),
                ABILITY_FACTORIES.stream()
            ).toList()
        );
    }

    @Override
    public void init() {
        super.init();

        this.player.sendMessage(
            Text.literal("The other Shadows are:")
                .styled((style) -> style.withColor(Formatting.RED))
                .append(
                    Texts.join(
                        this.player.getShadow()
                            .getOnlinePlayers()
                            .stream()
                            .filter(
                                (player) ->
                                    player.role != null &&
                                    player.role.getFaction() == Faction.SHADOW &&
                                    player.playerUUID != this.player.playerUUID
                            ).map(IndirectPlayer::getName).map(
                                (name) -> name.copy().styled(
                                    (style) -> style.withColor(Formatting.RED)
                                )
                            ).toList(),
                        Text.literal(",").styled((style -> style.withColor(Formatting.GRAY)))
                    )
                ),
            CancelPredicates.cancelOnPhaseChange(this.player.getShadow().state.phase)
        );
    }

    @Override
    public Faction getFaction() { return Faction.SHADOW; }

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
}
