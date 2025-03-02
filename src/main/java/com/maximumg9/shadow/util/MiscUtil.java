package com.maximumg9.shadow.util;

public class MiscUtil {
    public static String padLeft(String original, char padding, int desiredLength) {
        if(original.length() >= desiredLength) return original;
        StringBuilder builder = new StringBuilder(original);
        for (int i = 0; i < desiredLength - builder.length(); i++) {
            builder.insert(0, padding);
        }
        return builder.toString();
    }
}
