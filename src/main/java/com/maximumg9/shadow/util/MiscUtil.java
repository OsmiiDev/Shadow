package com.maximumg9.shadow.util;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.ducks.ShadowProvider;
import net.minecraft.server.MinecraftServer;

public class MiscUtil {
    public static String padLeft(String original, char padding, int desiredLength) {
        if(original.length() >= desiredLength) return original;
        StringBuilder builder = new StringBuilder(original);
        for (int i = 0; i < desiredLength - builder.length(); i++) {
            builder.insert(0, padding);
        }
        return builder.toString();
    }


    public static Shadow getShadow(MinecraftServer server) {
        return ((ShadowProvider) server).shadow$getShadow();
    }
}
