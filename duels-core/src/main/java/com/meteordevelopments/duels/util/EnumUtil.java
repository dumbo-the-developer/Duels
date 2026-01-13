package com.meteordevelopments.duels.util;

import java.lang.reflect.Field;
import java.util.Arrays;

public final class EnumUtil {

    private EnumUtil() {
    }


    public static <E> E getByName(final String name, Class<E> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class cannot be null");
        }

        // Handle enums
        if (clazz.isEnum()) {
            return Arrays.stream(clazz.getEnumConstants())
                    .filter(type -> type.toString().equalsIgnoreCase(name))
                    .findFirst()
                    .orElse(null);
        }

        // Handle static classes with public static fields
        try {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.getType() == clazz && field.getName().equalsIgnoreCase(name)) {
                    return (E) field.get(null); // Get the static field value
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access static field in " + clazz.getName(), e);
        }

        // No match found
        return null;
    }


}
