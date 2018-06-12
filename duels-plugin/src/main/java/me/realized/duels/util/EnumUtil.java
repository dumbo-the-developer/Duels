package me.realized.duels.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class EnumUtil {

    private EnumUtil() {}

    public static <E extends Enum> E getByName(final String name, Class<E> clazz) {
        return clazz.cast(Arrays.stream(clazz.getEnumConstants()).filter(type -> type.name().equalsIgnoreCase(name)).findFirst().orElse(null));
    }

    public static List<String> getNames(final Class<? extends Enum> clazz) {
        return Arrays.stream(clazz.getEnumConstants()).map(Enum::name).collect(Collectors.toList());
    }
}
