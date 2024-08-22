package me.realized.duels.arena;

import com.google.common.collect.Lists;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.config.Config;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.compat.Titles;
import me.realized.duels.util.function.Pair;
import org.bukkit.Location;
import org.bukkit.Material;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class Countdown implements Runnable {

    private final Config config;
    private final ArenaImpl arena;
    private final String kit;
    private final Map<UUID, Pair<String, Integer>> info;
    private final List<String> messages;
    private final List<String> titles;

    private boolean finished;

    Countdown(final DuelsPlugin plugin, final ArenaImpl arena, final String kit, final Map<UUID, Pair<String, Integer>> info, final List<String> messages, final List<String> titles) {
        this.config = plugin.getConfiguration();
        this.arena = arena;
        this.kit = kit;
        this.info = info;
        this.messages = Lists.newArrayList(messages);
        this.titles = Lists.newArrayList(titles);
    }

    @Override
    public void run() {
        if (finished) {
            return;
        }

        final String rawMessage = messages.remove(0);
        final String message = StringUtil.color(rawMessage);
        final String title = !titles.isEmpty() ? titles.remove(0) : null;

        arena.getPlayers().forEach(player -> {
            config.playSound(player, rawMessage);

            final Pair<String, Integer> info = this.info.get(player.getUniqueId());

            if (info != null) {
                player.sendMessage(message
                        .replace("%opponent%", info.getKey())
                        .replace("%opponent_rating%", String.valueOf(info.getValue()))
                        .replace("%kit%", kit)
                        .replace("%arena%", arena.getName())
                );
            } else {
                player.sendMessage(message);
            }

            if (title != null) {
                Titles.send(player, title, null, 20, 40, 75);
            }

        });

        if (!arena.isUsed() || messages.isEmpty()) {
            arena.setCountdown(null);
            finished = true;
        }
    }

    private void placeBarrierBlocks(Location location) {
        placeBarrierBlock(location.clone().add(1, 0, 0)); // Right
        placeBarrierBlock(location.clone().add(-1, 0, 0)); // Left
        placeBarrierBlock(location.clone().add(0, 2, 0)); // Top
        placeBarrierBlock(location.clone().add(0, 0, 1)); // Front
        placeBarrierBlock(location.clone().add(0, 0, -1)); // Back
    }

    private void placeBarrierBlock(Location location) {
        location.getBlock().setType(Material.BARRIER);
    }

    private void removeBarrierBlocks(Location location) {
        removeBarrierBlock(location.clone().add(1, 0, 0)); // Right
        removeBarrierBlock(location.clone().add(-1, 0, 0)); // Left
        removeBarrierBlock(location.clone().add(0, 2, 0)); // Top
        removeBarrierBlock(location.clone().add(0, 0, 1)); // Front
        removeBarrierBlock(location.clone().add(0, 0, -1)); // Back
    }

    private void removeBarrierBlock(Location location) {
        location.getBlock().setType(Material.AIR);
    }

    public void startCountdown(long delay, long period) {
        DuelsPlugin.getMorePaperLib().scheduling().asyncScheduler().runAtFixedRate(this, Duration.ofMillis(delay * 50), Duration.ofMillis(period * 50));
    }
}
