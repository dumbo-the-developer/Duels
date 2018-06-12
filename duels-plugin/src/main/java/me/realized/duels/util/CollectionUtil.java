package me.realized.duels.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class CollectionUtil {

    private CollectionUtil() {}

    public static <T> List<T> convert(final Collection<?> collection, Class<T> clazz) {
        final List<T> result = new ArrayList<>();
        collection.forEach(element -> {
            if (clazz.isInstance(element)) {
                result.add(clazz.cast(element));
            }
        });

        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> convert(final List<?> list, Class<T> clazz) {
        list.removeIf(object -> !clazz.isInstance(object));
        return (List<T>) list;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> convert(final Map<?, ?> map, final Class<K> key, final Class<V> value) {
        map.entrySet().removeIf(entry -> !key.isInstance(entry.getKey()) || !value.isInstance(entry.getValue()));
        return (Map<K, V>) map;
    }
}
