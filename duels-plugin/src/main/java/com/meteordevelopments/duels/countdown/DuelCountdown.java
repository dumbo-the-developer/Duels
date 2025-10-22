package com.meteordevelopments.duels.countdown;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.arena.ArenaImpl;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.config.Lang;
import com.meteordevelopments.duels.data.UserData;
import com.meteordevelopments.duels.data.UserManagerImpl;
import com.meteordevelopments.duels.match.DuelMatch;
import com.meteordevelopments.duels.util.StringUtil;
import com.meteordevelopments.duels.util.compat.Titles;
import com.meteordevelopments.duels.util.function.Pair;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import space.arim.morepaperlib.scheduling.ScheduledTask;

public class DuelCountdown extends BukkitRunnable {

    protected final Config config;
    protected final Lang lang;
    protected final UserManagerImpl userManager;
    protected final ArenaImpl arena;
    protected final DuelMatch match;
    
    private final List<String> messages;
    private final List<String> titles;

    private final Map<UUID, Pair<String, Integer>> info = new HashMap<>();
    private int index = 0;

    private final AtomicReference<ScheduledTask> scheduledTask = new AtomicReference<>();

    protected DuelCountdown(final DuelsPlugin plugin, final ArenaImpl arena, final DuelMatch match, final List<String> messages, final List<String> titles) {
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.userManager = plugin.getUserManager();
        this.arena = arena;
        this.match = match;
        this.titles = titles;
        this.messages = messages;
    }

    public DuelCountdown(final DuelsPlugin plugin, final ArenaImpl arena, final DuelMatch match) {
        this(plugin, arena, match, plugin.getConfiguration().getCdDuelMessages(), plugin.getConfiguration().getCdDuelTitles());
        match.getAllPlayers().forEach(player -> {
            final Player opponent = arena.getOpponent(player);
            final UserData user = userManager.get(Objects.requireNonNull(opponent));

            if (user == null) {
                return;
            }

            info.put(player.getUniqueId(), new Pair<>(opponent.getName(), user.getRatingUnsafe(match.getKit())));
        });
    }

    protected void sendMessage(final String rawMessage, final String message, final String title) {
        final String kitName = match.getKit() != null ? match.getKit().getName() : lang.getMessage("GENERAL.none");

        arena.getPlayers().forEach(player -> {
            config.playSound(player, rawMessage);

            final Pair<String, Integer> info = this.info.get(player.getUniqueId());

            if (info != null) {
                player.sendMessage(message
                    .replace("%opponent%", info.getKey())
                    .replace("%opponent_rating%", String.valueOf(info.getValue()))
                    .replace("%kit%", kitName)
                    .replace("%arena%", arena.getName())
                );
            } else {
                player.sendMessage(message);
            }

            if (title != null) {
                Titles.send(player, title, null, 0, 20, 50);
            }
        });
    }

    @Override
    public void run() {
        if (!arena.isUsed() || index >= messages.size()) {
            arena.setCountdown(null);

            // Cancel the MorePaperLib task
            ScheduledTask task = scheduledTask.get();
            if (task != null) {
                task.cancel();
            }

            return;
        }

        final String rawMessage = messages.get(index);
        final String message = StringUtil.color(rawMessage);
        final String title = (titles.size() >= index + 1) ? titles.get(index) : null;
        sendMessage(rawMessage, message, title);
        index++;
    }

    public void startCountdown(long delay, long period) {
        ScheduledTask task = DuelsPlugin.getMorePaperLib()
                .scheduling()
                .asyncScheduler()
                .runAtFixedRate(
                        this,
                        Duration.ofMillis(delay * 50),
                        Duration.ofMillis(period * 50)
                );
        scheduledTask.set(task); // Store the task reference
    }
}
