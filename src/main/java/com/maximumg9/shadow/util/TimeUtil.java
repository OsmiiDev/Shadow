package com.maximumg9.shadow.util;

public class TimeUtil {
    public static String ticksToText(int ticks, boolean leadingZeros) {
        return secondToText(ticks/20, leadingZeros);
    }

    public static String padLeft(String original, char padding, int desiredLength) {
        if(original.length() >= desiredLength) return original;
        StringBuilder builder = new StringBuilder(original);
        for (int i = 0; i < desiredLength - builder.length(); i++) {
            builder.insert(0, padding);
        }
        return builder.toString();
    }

    public static String secondToText(int seconds, boolean leadingZeros) {
        if(leadingZeros)
            return
                padLeft(String.valueOf(seconds/60),'0',2) +
                ":" +
                padLeft(String.valueOf(seconds % 60),'0',2);

        if(seconds < 60) return String.valueOf(seconds);
        return
            (seconds / 60) +
            ":" +
            padLeft(String.valueOf(seconds % 60),'0',2);
    }
}
