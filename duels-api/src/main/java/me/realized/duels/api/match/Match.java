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

package me.realized.duels.api.match;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.realized.duels.api.kit.Kit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface Match {

    /**
     * {@link System#currentTimeMillis()} subtracted by the result of this method will give the duration of the current match in milliseconds.
     *
     * @return start of this match in milliseconds.
     */
    long getStart();


    /**
     * @return The kit used in this match. If players are using their own inventories, this will return null
     */
    @Nullable
    Kit getKit();


    /**
     * @param player Player to get the bet items
     * @return List of items the player bet for this match.
     */
    List<ItemStack> getItems(@Nonnull final Player player);


    /**
     * @return The bet amount for this match or 0 if no bet was specified
     */
    int getBet();

}
