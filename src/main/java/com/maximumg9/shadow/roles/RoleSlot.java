package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.util.MiscUtil;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.random.Random;

import java.util.Arrays;

public class RoleSlot {
    private final int[] weights = new int[Roles.values().length];

    private final int index;

    public RoleSlot(int index) {
        this.index = index;
        this.weights[Roles.VILLAGER.ordinal()] = 1;
    }

    public void setWeight(Roles role, int weight) {
        weights[role.ordinal()] = weight;
    }

    public Roles pickRandomRole(Random random) {
        int value = random.nextBetween(1, Arrays.stream(weights).sum());

        int currentSum = weights[0];
        int i=0;
        while(currentSum < value) {
            if(i > weights.length) return null;
            currentSum += weights[i];
            i++;
        }
        i--;

        return Roles.values()[i];
    }

    public Text getText() {
        MutableText text = Text.literal("");

        Roles[] roles = Roles.values();

        for (int i = 0; i < this.weights.length; i++) {
            Roles role = roles[i];

            Text name = role.factory.makeRole(null).getName();

            int iFrickingLoveLambdasBro = i;

            Text increaseText = Text.literal("+").styled(
                (style) ->
                    style
                        .withColor(
                            Formatting.GREEN
                        )
                        .withClickEvent(
                            new ClickEvent(
                                ClickEvent.Action.RUN_COMMAND,
                                "/$roles weight " +
                                    index + " " +
                                    role.name + " " +
                                    (1 + this.weights[iFrickingLoveLambdasBro])
                            )
                        )
            );

            Text decreaseText = Text.literal("-").styled(
                (style) ->
                    style
                        .withColor(
                                Formatting.RED
                        )
                        .withClickEvent(
                            new ClickEvent(
                                ClickEvent.Action.RUN_COMMAND,
                                "/$roles weight " +
                                    index + " " +
                                    role.name + " " +
                                    Math.max(0,this.weights[iFrickingLoveLambdasBro]-1)
                            )
                        )
            );

            text.append(increaseText)
                .append(" ")
                .append(
                    MiscUtil.padLeft(
                        String.valueOf(this.weights[i]),
                        ' ',2
                    )
                )
                .append(" ")
                .append(decreaseText)
                .append(" ")
                .append(name)
                .append("\n");
        }

        return text;
    }
}
