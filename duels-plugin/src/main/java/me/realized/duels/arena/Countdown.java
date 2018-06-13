package me.realized.duels.arena;

import com.google.common.collect.Lists;
import java.util.List;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.compat.Titles;
import org.bukkit.scheduler.BukkitRunnable;

class Countdown extends BukkitRunnable {

    private final Arena arena;
    private final List<String> messages;
    private final List<String> titles;

    Countdown(final Arena arena, final List<String> messages, final List<String> titles) {
        this.arena = arena;
        this.messages = Lists.newArrayList(messages);
        this.titles = Lists.newArrayList(titles);
    }

    @Override
    public void run() {
        final String message = StringUtil.color(messages.remove(0));
        final String title = !titles.isEmpty() ? titles.remove(0) : null;

        arena.getPlayers().forEach(player -> {
            player.sendMessage(message);

            if (title != null) {
                Titles.send(player, title, null, 0, 20, 50);
            }
        });

        if (messages.isEmpty()) {
            cancel();
            arena.setCountdown(null);
        }
    }
}
