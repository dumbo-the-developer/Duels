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

package me.realized.duels.arena;

import com.google.common.collect.Lists;
import java.util.List;
import me.realized.duels.util.StringUtil;
import org.bukkit.scheduler.BukkitRunnable;

class Countdown extends BukkitRunnable {

    private final Arena arena;
    private final List<String> messages;

    Countdown(final Arena arena, final List<String> messages) {
        this.arena = arena;
        this.messages = Lists.newArrayList(messages);
    }

    @Override
    public void run() {
        final String message = StringUtil.color(messages.remove(0));
        arena.getPlayers().forEach(player -> player.sendMessage(message));

        if (messages.isEmpty()) {
            cancel();
            arena.setCountdown(null);
        }
    }
}
