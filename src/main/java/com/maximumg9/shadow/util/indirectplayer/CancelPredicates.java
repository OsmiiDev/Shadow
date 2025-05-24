package com.maximumg9.shadow.util.indirectplayer;

import com.maximumg9.shadow.GamePhase;

import java.util.function.Predicate;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

public abstract class CancelPredicates {
    public static Predicate<IndirectPlayer> cancelOnPhaseChange(GamePhase currentPhase) {
        return (player) -> currentPhase != getShadow(player.server).state.phase;
    }

    public static final Predicate<IndirectPlayer> ALWAYS_CANCEL = (p) -> true;
    public static final Predicate<IndirectPlayer> NEVER_CANCEL = (p) -> false;
}
