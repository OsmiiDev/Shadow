package com.maximumg9.shadow.abilities;

import com.maximumg9.shadow.util.TimeUtil;
import com.maximumg9.shadow.util.indirectplayer.CancelPredicates;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.text.Text;

public abstract class CooldownAbility extends Ability {
    public CooldownAbility(IndirectPlayer player) {
        super(player);
    }

    private long lastActivated = initialCooldown();

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void deInit() {

        super.deInit();
    }

    public abstract Text getName();

    @Override
    public void apply() {
        long currentTicks = this.getShadow().getServer().getOverworld().getTime();
        long endTime = lastActivated + this.getShadow().config.maxCooldownManager.getMaxCooldown(this.getID(),defaultCooldown());

        long timeLeft = endTime-currentTicks;

        if(timeLeft <= 0) {
            boolean result = applyWithCooldown();

            if(result) {
                resetCooldown();
            }
        } else {
            onCooldownFail(timeLeft);
        }
    }

    void resetCooldown() {
        lastActivated = this.getShadow().getServer().getOverworld().getTime();
        onResetCooldown();
    }

    // Override these two following ones if needed

    void onResetCooldown() {
        this.player.sendMessage(
            Text.literal("Your ")
                .append(this.getName())
                .append(Text.literal(" cooldown has been reset")),
            CancelPredicates.cancelOnPhaseChange(this.getShadow().state.phase)
        );
    }

    void onCooldownFail(long ticksLeft) {
        this.player.sendMessage(
            Text.literal("Your ")
                .append(this.getName())
                .append(Text.literal(" ability is still on cooldown for "))
                .append(TimeUtil.ticksToText(ticksLeft,true)),
            CancelPredicates.cancelOnPhaseChange(this.getShadow().state.phase)
        );
    }

    abstract int defaultCooldown();

    abstract int initialCooldown();

    // if true is returned, reset cooldown, otherwise don't reset cooldown
    abstract boolean applyWithCooldown();
}
