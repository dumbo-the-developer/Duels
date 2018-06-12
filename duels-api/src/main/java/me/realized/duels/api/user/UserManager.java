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

package me.realized.duels.api.user;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.Getter;
import org.bukkit.entity.Player;

public interface UserManager {

    @Nullable
    User get(@Nonnull final UUID uuid);

    @Nullable
    User get(@Nonnull final Player player);

    List<SortedEntry<String, Integer>> getTopWins();

    List<SortedEntry<String, Integer>> getTopLosses();

    <V extends Comparable<V>> List<SortedEntry<String, V>> sorted(@Nonnull final Function<User, V> function);

    class SortedEntry<K, V extends Comparable<V>> implements Comparable<SortedEntry<K, V>> {

        @Getter
        private final K key;
        @Getter
        private final V value;

        public SortedEntry(@Nonnull final K key, @Nonnull final V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public int compareTo(@Nonnull final SortedEntry<K, V> other) {
            return value.compareTo(other.value);
        }

        @Override
        public String toString() {
            return "SortedEntry{" + "key=" + key + ", value=" + value + '}';
        }
    }
}
