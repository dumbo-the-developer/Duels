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

package me.realized.duels.data;

import lombok.Getter;

public class MatchData {

    @Getter
    private final String winner;
    @Getter
    private final String loser;
    @Getter
    private final long time;
    @Getter
    private final long duration;
    @Getter
    private final double health;

    public MatchData(final String winner, final String loser, final long time, final long duration, final double health) {
        this.winner = winner;
        this.loser = loser;
        this.time = time;
        this.duration = duration;
        this.health = health;
    }

    @Override
    public String toString() {
        return "MatchData{" +
            "winner='" + winner + '\'' +
            ", loser='" + loser + '\'' +
            ", time=" + time +
            ", duration=" + duration +
            ", health=" + health +
            '}';
    }
}
