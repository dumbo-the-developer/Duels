package me.realized.duels.api.util;

import javax.annotation.Nonnull;
import lombok.Getter;

public class Pair<K, V extends Comparable<V>> implements Comparable<Pair<K, V>> {

    @Getter
    private final K key;
    @Getter
    private final V value;

    public Pair(final K key, final V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public int compareTo(@Nonnull final Pair<K, V> other) {
        return value.compareTo(other.value);
    }

    @Override
    public String toString() {
        return "{key=" + key + ", value=" + value + "}";
    }
}
