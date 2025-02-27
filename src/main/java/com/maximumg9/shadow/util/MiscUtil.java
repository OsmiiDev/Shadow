package com.maximumg9.shadow.util;

public class MiscUtil {
    public static <T> Class<? extends T> GetClass(T object) {
        return (Class<? extends T>) object.getClass();
    }
}
