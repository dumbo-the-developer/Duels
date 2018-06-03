/*
 * This file is part of Duels, licensed under the MIT License.
 *
 * Copyright (c) Realized
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
