package me.realized.duels.util.collection;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamUtil {

    public static <T> Stream<T> asStream(final Iterable<T> iterable) {
        return asStream(iterable.spliterator());
    }

    public static <T> Stream<T> asStream(final Iterator<T> iterator) {
        return asStream(Spliterators.spliteratorUnknownSize(iterator, 0));
    }

    public static <T> Stream<T> asStream(final Spliterator<T> spliterator) {
        return StreamSupport.stream(spliterator, false);
    }
}
