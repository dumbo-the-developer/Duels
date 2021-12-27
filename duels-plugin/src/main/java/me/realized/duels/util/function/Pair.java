package me.realized.duels.util.function;

import lombok.Getter;
import lombok.Setter;

public class Pair<K, V> {

    @Getter
    @Setter
    private K key;
    @Getter
    @Setter
    private V value;

    public Pair(final K key, final V value) {
        this.key = key;
        this.value = value;
    }
}
