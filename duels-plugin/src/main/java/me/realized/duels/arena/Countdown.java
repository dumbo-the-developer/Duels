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
