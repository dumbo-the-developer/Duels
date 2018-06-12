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
import javax.annotation.Nonnull;
import me.realized.duels.api.kit.Kit;

public interface User {

    /**
     * @return UUID of this user
     */
    @Nonnull
    UUID getUuid();


    /**
     * This value is updated on login.
     * Method is thread-safe.
     *
     * @return Name of this user
     */
    @Nonnull
    String getName();


    /**
     * Method is thread-safe.
     *
     * @return total wins of this user
     */
    int getWins();


    /**
     * Sets new total wins for this user
     */
    void setWins(final int wins);


    /**
     * Method is thread-safe.
     *
     * @return total losses of this user
     */
    int getLosses();


    /**
     * Sets new total wins for this user
     */
    void setLosses(final int losses);


    /**
     * @return true if this user has requests enabled, otherwise false
     */
    boolean canRequest();


    /**
     * Method is thread-safe.
     *
     * @param kit Kit to check for rating
     * @return Rating for this kit or the default rating specified in the configuration
     */
    int getRating(@Nonnull final Kit kit);


    /**
     * Method is thread-safe.
     *
     * @param kit Kit to reset the rating to default.
     */
    void resetRating(@Nonnull final Kit kit);


    /**
     * @return List of recent matches for this user
     */
    List<? extends MatchInfo> getMatches();
}
