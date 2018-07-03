package me.realized.duels.duel;

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.event.match.MatchEndEvent.Reason;
import me.realized.duels.api.event.match.MatchStartEvent;
import me.realized.duels.arena.Arena;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.arena.Match;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.data.MatchData;
import me.realized.duels.data.UserData;
import me.realized.duels.data.UserManager;
import me.realized.duels.extra.Teleport;
import me.realized.duels.hooks.EssentialsHook;
import me.realized.duels.hooks.McMMOHook;
import me.realized.duels.hooks.VaultHook;
import me.realized.duels.inventories.InventoryManager;
import me.realized.duels.kit.Kit;
import me.realized.duels.player.PlayerInfo;
import me.realized.duels.player.PlayerInfoManager;
import me.realized.duels.queue.QueueManager;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.PlayerUtil;
import me.realized.duels.util.RatingUtil;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.TextBuilder;
import me.realized.duels.util.compat.Players;
import me.realized.duels.util.compat.Titles;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

public class DuelManager implements Loadable {

    private final DuelsPlugin plugin;
    private final Config config;
    private final Lang lang;
    private final UserManager userDataManager;
    private final ArenaManager arenaManager;
    private final PlayerInfoManager playerManager;
    private final InventoryManager inventoryManager;

    private QueueManager queueManager;
    private Teleport teleport;
    private VaultHook vault;
    private EssentialsHook essentials;
    private McMMOHook mcMMO;
    private int durationCheckTask;

    public DuelManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.userDataManager = plugin.getUserManager();
        this.arenaManager = plugin.getArenaManager();
        this.playerManager = plugin.getPlayerManager();
        this.inventoryManager = plugin.getInventoryManager();
        plugin.getServer().getPluginManager().registerEvents(new DuelListener(), plugin);
    }

    @Override
    public void handleLoad() {
        this.queueManager = plugin.getQueueManager();
        this.teleport = plugin.getTeleport();
        this.vault = plugin.getHookManager().getHook(VaultHook.class);
        this.essentials = plugin.getHookManager().getHook(EssentialsHook.class);
        this.mcMMO = plugin.getHookManager().getHook(McMMOHook.class);

        if (config.getMaxDuration() > 0) {
            this.durationCheckTask = plugin.doSyncRepeat(() -> {
                final long now = System.currentTimeMillis();

                for (final Arena arena : arenaManager.getArenas()) {
                    final Match match = arena.getMatch();

                    if (match == null || now - match.getStart() < (config.getMaxDuration() * 60 * 1000) || arena.size() <= 1) {
                        continue;
                    }

                    for (final Player player : match.getPlayers()) {
                        handleTiePlayer(player, arena, match, true);
                        lang.sendMessage(player, "DUEL.on-end.tie");
                    }

                    arena.endMatch(null, null, Reason.MAX_TIME_REACHED);
                }
            }, 0L, 20L);
        }
    }

    @Override
    public void handleUnload() {
        plugin.cancelTask(durationCheckTask);

        for (final Arena arena : arenaManager.getArenas()) {
            final Match match = arena.getMatch();

            if (match == null) {
                continue;
            }

            final int size = arena.size();
            final boolean ongoing = size > 1;
            final boolean tie = size == 0;

            for (final Player player : match.getPlayers()) {
                // This case wouldn't be called if ongoing = true
                if (match.isDead(player)) {
                    continue;
                }

                // Now checks for non-dead players. ongoing = false is the case after winner is decided
                if (ongoing) {
                    handleTiePlayer(player, arena, match, true);
                } else if (!tie) {
                    handleWinner(player, getOther(player, match.getPlayers()), arena, match);
                }
            }

            arena.getPlayers().forEach(player -> lang.sendMessage(player, "DUEL.on-end.plugin-disable"));
            arena.endMatch(null, null, Reason.PLUGIN_DISABLE);
        }

        Players.getOnlinePlayers().stream().filter(Player::isDead).forEach(player -> {
            final PlayerInfo info = playerManager.removeAndGet(player);

            if (info != null) {
                player.spigot().respawn();
                teleport.tryTeleport(player, info.getLocation());
                PlayerUtil.reset(player);
                info.restore(player, true);
            }
        });
    }

    private Player getOther(final Player player, final Set<Player> players) {
        final Set<Player> copy = new HashSet<>(players);
        copy.remove(player);

        if (copy.isEmpty()) {
            return null;
        }

        return copy.iterator().next();
    }

    private void handleTiePlayer(final Player player, final Arena arena, final Match match, boolean alive) {
        arena.remove(player);

        if (vault != null && match.getBet() > 0) {
            vault.add(match.getBet(), player);
        }

        if (mcMMO != null) {
            mcMMO.enableSkills(player);
        }

        if (alive && !config.isUseOwnInventoryEnabled()) {
            PlayerUtil.reset(player);
        }

        final PlayerInfo info = playerManager.get(player);

        if (alive) {
            playerManager.remove(player);

            if (info != null) {
                teleport.tryTeleport(player, info.getLocation(), failed -> {
                    failed.setHealth(0);
                    failed.sendMessage(StringUtil.color("&cTeleportation failed! You were killed to prevent staying in the arena."));
                });
                info.restore(player, false);
            } else {
                // If somehow PlayerInfo is not found...
                teleport.tryTeleport(player, playerManager.getLobby());
            }

            match.getItems(player).stream().filter(Objects::nonNull).forEach(item -> player.getInventory().addItem(item));
        } else {
            final List<ItemStack> items = match.getItems(player);

            if (player.isDead()) {
                addItems(info, items);
            } else {
                // Note to self: Unsure if this case will ever be called, since player has to respawn instantly after a tick.
                items.stream().filter(Objects::nonNull).forEach(item -> player.getInventory().addItem(item));
            }
        }
    }

    private void handleWinner(final Player player, final Player opponent, final Arena arena, final Match match) {
        arena.remove(player);

        if (vault != null && match.getBet() > 0) {
            final int amount = match.getBet() * 2;
            vault.add(amount, player);
            lang.sendMessage(player, "DUEL.reward.money.message", "name", opponent != null ? opponent.getName() : "none", "money", amount);

            final String title = lang.getMessage("DUEL.reward.money.title", "name", opponent != null ? opponent.getName() : "none", "money", amount);

            if (title != null) {
                Titles.send(player, title, null, 0, 20, 50);
            }
        }

        if (mcMMO != null) {
            mcMMO.enableSkills(player);
        }

        final List<ItemStack> items = match.getItems();
        final PlayerInfo info = playerManager.get(player);

        if (player.isDead()) {
            addItems(info, items);
        } else if (player.isOnline()) {
            playerManager.remove(player);

            if (info != null) {
                if (!config.isUseOwnInventoryEnabled()) {
                    PlayerUtil.reset(player);
                }

                teleport.tryTeleport(player, info.getLocation(), failed -> {
                    failed.setHealth(0);
                    failed.sendMessage(StringUtil.color("&cTeleportation failed! You were killed to prevent staying in the arena."));
                });
                info.restore(player, false);
            }

            boolean added = false;

            for (final ItemStack item : items) {
                if (item == null) {
                    continue;
                }

                if (!added) {
                    added = true;
                }

                player.getInventory().addItem(item);
            }

            if (added) {
                lang.sendMessage(player, "DUEL.reward.items.message", "name", opponent != null ? opponent.getName() : "none");
            }
        }
    }

    private void addItems(final PlayerInfo info, final List<ItemStack> items) {
        if (info != null) {
            info.getExtra().addAll(items);
        }
    }

    public void startMatch(final Player first, final Player second, final Settings settings, final Map<UUID, List<ItemStack>> items, final boolean fromQueue) {
        final Kit kit = settings.getKit();

        if (!config.isUseOwnInventoryEnabled() && kit == null) {
            lang.sendMessage("DUEL.start-failure.no-kit-selected", first, second);
            refundItems(items, first, second);
            return;
        }

        if (config.isCancelIfMoved() && (notInLoc(first, settings.getLocations()[0]) || notInLoc(second, settings.getLocations()[1]))) {
            lang.sendMessage("DUEL.start-failure.player-moved", first, second);
            refundItems(items, first, second);
            return;
        }

        final Arena arena = settings.getArena() != null ? settings.getArena() : arenaManager.randomArena(kit);

        if (arena == null || !arena.isAvailable() || (kit != null && kit.isArenaSpecific() && !kit.canUse(arena))) {
            lang.sendMessage("DUEL.start-failure." + (settings.getArena() != null ? "arena-in-use" : "no-arena-available"), first, second);
            refundItems(items, first, second);
            return;
        }

        final int bet = settings.getBet();

        if (bet > 0 && vault != null && vault.getEconomy() != null) {
            if (!vault.has(bet, first, second)) {
                lang.sendMessage(first, "DUEL.start-failure.not-enough-money", "bet_amount", bet);
                lang.sendMessage(second, "DUEL.start-failure.not-enough-money", "bet_amount", bet);
                refundItems(items, first, second);
                return;
            }

            vault.remove(bet, first, second);
        }

        arena.startMatch(kit, items, settings.getBet(), fromQueue);
        addPlayers(fromQueue, arena, kit, arena.getPositions(), first, second);

        if (config.isCdEnabled()) {
            final Map<UUID, OpponentInfo> info = new HashMap<>();
            info.put(first.getUniqueId(), new OpponentInfo(second.getName(), getRating(kit, userDataManager.get(second))));
            info.put(second.getUniqueId(), new OpponentInfo(first.getName(), getRating(kit, userDataManager.get(first))));
            arena.startCountdown(kit != null ? kit.getName() : "none", info);
        }

        final MatchStartEvent event = new MatchStartEvent(arena.getMatch(), first, second);
        plugin.getServer().getPluginManager().callEvent(event);
    }

    private void refundItems(final Map<UUID, List<ItemStack>> items, final Player... players) {
        if (items != null) {
            Arrays.stream(players).forEach(player -> {
                final List<ItemStack> list = items.get(player.getUniqueId());

                if (list != null) {
                    list.stream().filter(Objects::nonNull).forEach(item -> player.getInventory().addItem(item));
                }
            });
        }
    }

    private boolean notInLoc(final Player player, final Location location) {
        if (location == null) {
            return false;
        }

        final Location source = player.getLocation();
        return source.getBlockX() != location.getBlockX() || source.getBlockY() != location.getBlockY() || source.getBlockZ() != location.getBlockZ();
    }

    private int getRating(final Kit kit, final UserData user) {
        return user != null ? user.getRating(kit) : config.getDefaultRating();
    }

    private void addPlayers(final boolean fromQueue, final Arena arena, final Kit kit, final Map<Integer, Location> locations, final Player... players) {
        int position = 0;

        for (final Player player : players) {
            if (!fromQueue) {
                queueManager.remove(player);
            }

            inventoryManager.remove(player);

            if (player.getAllowFlight()) {
                player.setFlying(false);
                player.setAllowFlight(false);
            }

            player.closeInventory();
            playerManager.put(player, new PlayerInfo(player, !config.isUseOwnInventoryEnabled()));

            if (!config.isUseOwnInventoryEnabled()) {
                PlayerUtil.reset(player);

                if (kit != null) {
                    kit.equip(player);
                }
            }

            teleport.tryTeleport(player, locations.get(++position));

            if (essentials != null) {
                essentials.tryUnvanish(player);
            }

            if (mcMMO != null) {
                mcMMO.disableSkills(player);
            }

            arena.add(player);
        }
    }

    private void handleInventories(final Match match) {
        String color = lang.getMessage("DUEL.inventories.name-color");
        color = color != null ? color : "";
        boolean start = true;
        final TextBuilder builder = TextBuilder.of(lang.getMessage("DUEL.inventories.message"));

        for (final Player matchPlayer : match.getPlayers()) {
            if (!start) {
                builder.add(StringUtil.color(color + ", "));
            } else {
                start = false;
            }

            builder.add(StringUtil.color(color + matchPlayer.getName()), Action.RUN_COMMAND, "/duel _ " + matchPlayer.getUniqueId());
        }

        builder.send(match.getPlayers());
    }

    private void handleEndUsers(final Match match, final UserData winner, final UserData loser, final MatchData matchData, final Object... args) {
        if (winner != null && loser != null) {
            winner.addWin();
            loser.addLoss();
            winner.addMatch(matchData);
            loser.addMatch(matchData);

            final Kit kit = match.getKit();
            final Object[] append = {"winner_rating", config.getDefaultRating(), "loser_rating", config.getDefaultRating(), "change", 0};

            if (config.isRatingEnabled() && kit != null && !(!match.isFromQueue() && config.isQueueMatchesOnly())) {
                int winnerRating = winner.getRating(kit);
                int loserRating = loser.getRating(kit);
                final int change = RatingUtil.getChange(config.getKFactor(), winnerRating, loserRating);
                winner.setRating(kit.getName(), winnerRating = winnerRating + change);
                loser.setRating(kit.getName(), loserRating = loserRating - change);
                append[1] = winnerRating;
                append[3] = loserRating;
                append[5] = change;
            }

            Bukkit.broadcastMessage(lang.getMessage("DUEL.on-end.opponent-defeat", ArrayUtils.addAll(args, append)));
        }
    }

    private void cancelDamage(final EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        final Player player = (Player) event.getEntity();
        final Arena arena = arenaManager.get(player);

        if (arena == null) {
            return;
        }

        if (arena.size() > 1) {
            return;
        }

        event.setCancelled(true);
    }

    public class OpponentInfo {

        @Getter
        private final String name;
        @Getter
        private final int rating;

        OpponentInfo(final String name, final int rating) {
            this.name = name;
            this.rating = rating;
        }
    }

    // Separating out the listener fixes weird error with 1.7 spigot
    private class DuelListener implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST)
        public void on(final PlayerDeathEvent event) {
            final Player player = event.getEntity();
            final Arena arena = arenaManager.get(player);

            if (arena == null) {
                return;
            }

            if (mcMMO != null) {
                mcMMO.enableSkills(player);
            }

            inventoryManager.create(player);
            event.setKeepInventory(config.isUseOwnInventoryEnabled() && config.isUseOwnInventoryKeepItems());

            if (!config.isUseOwnInventoryEnabled()) {
                event.setKeepLevel(true);
                event.setDroppedExp(0);
                event.getDrops().clear();
            }

            arena.remove(player);

            // Call end task only on the first death
            if (arena.size() <= 0) {
                return;
            }

            plugin.doSyncAfter(() -> {
                final Match match = arena.getMatch();

                if (match == null) {
                    return;
                }

                if (arena.size() == 0) {
                    match.getPlayers().forEach(matchPlayer -> {
                        handleTiePlayer(matchPlayer, arena, match, false);
                        lang.sendMessage(matchPlayer, "DUEL.on-end.tie");
                    });
                    handleInventories(match);
                    arena.endMatch(null, null, Reason.TIE);
                    return;
                }

                final Player winner = arena.first();
                inventoryManager.create(winner);

                final Firework firework = (Firework) winner.getWorld().spawnEntity(winner.getEyeLocation(), EntityType.FIREWORK);
                final FireworkMeta meta = firework.getFireworkMeta();
                meta.addEffect(FireworkEffect.builder().withColor(Color.RED).with(FireworkEffect.Type.BALL_LARGE).withTrail().build());
                firework.setFireworkMeta(meta);

                final double health = Math.ceil(winner.getHealth()) * 0.5;
                final String kit = match.getKit() != null ? match.getKit().getName() : "none";
                final long duration = System.currentTimeMillis() - match.getStart();
                final long time = new GregorianCalendar().getTimeInMillis();
                final MatchData matchData = new MatchData(winner.getName(), player.getName(), kit, time, duration, health);
                handleEndUsers(match, userDataManager.get(winner), userDataManager.get(player), matchData,
                    "winner", winner.getName(), "loser", player.getName(), "health", health, "kit", kit, "arena", arena.getName());
                handleInventories(match);

                plugin.doSyncAfter(() -> {
                    handleWinner(winner, player, arena, match);

                    if (config.isEndCommandsEnabled()) {
                        for (final String command : config.getEndCommands()) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%winner%", winner.getName()).replace("%loser%", player.getName()));
                        }
                    }

                    arena.endMatch(winner.getUniqueId(), player.getUniqueId(), Reason.OPPONENT_DEFEAT);
                }, config.getTeleportDelay() * 20L);
            }, 1L);
        }


        @EventHandler(priority = EventPriority.HIGHEST)
        public void on(final PlayerRespawnEvent event) {
            final Player player = event.getPlayer();
            final PlayerInfo info = playerManager.removeAndGet(player);

            if (info != null) {
                event.setRespawnLocation(info.getLocation());

                if (essentials != null) {
                    essentials.setBackLocation(player, event.getRespawnLocation());
                }

                plugin.doSyncAfter(() -> info.restore(player, true), 1L);
            }
        }


        @EventHandler(ignoreCancelled = true)
        public void on(final EntityDamageEvent event) {
            cancelDamage(event);
        }


        @EventHandler(ignoreCancelled = true)
        public void on(final EntityDamageByEntityEvent event) {
            cancelDamage(event);
        }


        @EventHandler
        public void on(final PlayerQuitEvent event) {
            final Player player = event.getPlayer();

            if (!arenaManager.isInMatch(player)) {
                return;
            }

            player.setHealth(0);
        }


        @EventHandler(ignoreCancelled = true)
        public void on(final PlayerDropItemEvent event) {
            if (!config.isPreventItemDrop() || !arenaManager.isInMatch(event.getPlayer())) {
                return;
            }

            event.setCancelled(true);
            lang.sendMessage(event.getPlayer(), "DUEL.prevent.item-drop");
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final PlayerPickupItemEvent event) {
            if (!config.isPreventItemPickup() || !arenaManager.isInMatch(event.getPlayer())) {
                return;
            }

            event.setCancelled(true);
        }


        @EventHandler(ignoreCancelled = true)
        public void on(final PlayerCommandPreprocessEvent event) {
            final String command = event.getMessage().substring(1).split(" ")[0].toLowerCase();

            if (!arenaManager.isInMatch(event.getPlayer()) || (config.isBlockAllCommands() ? config.getWhitelistedCommands().contains(command)
                : !config.getBlacklistedCommands().contains(command))) {
                return;
            }

            event.setCancelled(true);
            lang.sendMessage(event.getPlayer(), "DUEL.prevent.command", "command", event.getMessage());
        }

        @EventHandler(ignoreCancelled = true)
        public void on(PlayerTeleportEvent event) {
            final Player player = event.getPlayer();
            final Location to = event.getTo();

            if (!config.isLimitTeleportEnabled() || event.getCause() == TeleportCause.ENDER_PEARL || !arenaManager.isInMatch(player)) {
                return;
            }

            final Location from = event.getFrom();

            if (from.getWorld().equals(to.getWorld()) && from.distance(to) <= config.getDistanceAllowed()) {
                return;
            }

            event.setCancelled(true);
            lang.sendMessage(player, "DUEL.prevent.teleportation");
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final InventoryOpenEvent event) {
            if (!config.isPreventInventoryOpen()) {
                return;
            }

            final Player player = (Player) event.getPlayer();

            if (!arenaManager.isInMatch(player)) {
                return;
            }

            event.setCancelled(true);
            lang.sendMessage(player, "DUEL.prevent.inventory-open");
        }
    }
}