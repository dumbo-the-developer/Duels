package me.realized.duels.duel;

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.event.match.MatchEndEvent;
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
import me.realized.duels.hooks.FactionsHook;
import me.realized.duels.hooks.VaultHook;
import me.realized.duels.inventories.InventoryManager;
import me.realized.duels.kit.Kit;
import me.realized.duels.player.PlayerInfo;
import me.realized.duels.player.PlayerInfoManager;
import me.realized.duels.setting.Setting;
import me.realized.duels.spectate.SpectateManager;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.PlayerUtil;
import me.realized.duels.util.RatingUtil;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.TextBuilder;
import me.realized.duels.util.compat.Players;
import me.realized.duels.util.metadata.MetadataUtil;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
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
    private final SpectateManager spectateManager;
    private final InventoryManager inventoryManager;

    private Teleport teleport;
    private VaultHook vault;
    private EssentialsHook essentials;
    private FactionsHook factions;
    private int durationCheckTask;

    public DuelManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.userDataManager = plugin.getUserManager();
        this.arenaManager = plugin.getArenaManager();
        this.playerManager = plugin.getPlayerManager();
        this.spectateManager = plugin.getSpectateManager();
        this.inventoryManager = plugin.getInventoryManager();
        plugin.getServer().getPluginManager().registerEvents(new DuelListener(), plugin);
    }

    @Override
    public void handleLoad() {
        this.teleport = plugin.getTeleport();
        this.vault = plugin.getHookManager().getHook(VaultHook.class);
        this.essentials = plugin.getHookManager().getHook(EssentialsHook.class);
        this.factions = plugin.getHookManager().getHook(FactionsHook.class);

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

                    final MatchEndEvent endEvent = new MatchEndEvent(arena.getMatch(), null, null, Reason.MAX_TIME_REACHED);
                    plugin.getServer().getPluginManager().callEvent(endEvent);
                    arena.endMatch();
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
                    handleWinner(player, arena, match);
                }
            }

            arena.getPlayers().forEach(player -> lang.sendMessage(player, "DUEL.on-end.plugin-disable"));

            final MatchEndEvent endEvent = new MatchEndEvent(arena.getMatch(), null, null, Reason.PLUGIN_DISABLE);
            plugin.getServer().getPluginManager().callEvent(endEvent);
            arena.endMatch();
        }

        Players.getOnlinePlayers().stream().filter(Player::isDead).forEach(player -> {
            final PlayerInfo info = playerManager.removeAndGet(player);

            if (info != null) {
                player.spigot().respawn();
                teleport.tryTeleport(player, info.getLocation());
                PlayerUtil.reset(player);
                info.restore(player);
            }
        });
    }

    private void handleTiePlayer(final Player player, final Arena arena, final Match match, boolean alive) {
        arena.remove(player);

        if (vault != null) {
            vault.add(match.getBet(), player);
        }

        if (alive && !config.isUseOwnInventoryEnabled()) {
            PlayerUtil.reset(player);
        }

        final PlayerInfo info = playerManager.removeAndGet(player);

        if (alive) {
            if (info != null) {
                teleport.tryTeleport(player, info.getLocation(), failed -> {
                    failed.setHealth(0);
                    failed.sendMessage(ChatColor.RED + "Teleportation failed! You were killed to prevent staying in the arena.");
                });
                info.restore(player);
            } else {
                // todo: teleport to lobby
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

    private void handleWinner(final Player player, final Arena arena, final Match match) {
        arena.remove(player);

        if (vault != null) {
            vault.add(match.getBet() * 2, player);
        }

        final List<ItemStack> items = match.getItems();
        final PlayerInfo info = playerManager.get(player);

        if (player.isDead()) {
            addItems(info, items);
        } else if (player.isOnline()) {
            playerManager.remove(player);

            if (!config.isUseOwnInventoryEnabled()) {
                PlayerUtil.reset(player);
            }

            if (info != null) {
                teleport.tryTeleport(player, info.getLocation(), failed -> {
                    failed.setHealth(0);
                    failed.sendMessage(ChatColor.RED + "Teleportation failed! You were killed to prevent staying in the arena.");
                });
                info.restore(player);
            } else {
                // todo: teleport to lobby
            }

            items.stream().filter(Objects::nonNull).forEach(item -> player.getInventory().addItem(item));
        }
    }

    private void addItems(final PlayerInfo info, final List<ItemStack> items) {
        if (info != null) {
            info.getInventory().addAll(items);
        }
    }

    public void startMatch(final Player first, final Player second, final Setting setting, final Map<UUID, List<ItemStack>> items) {
        final Kit kit = setting.getKit();

        if (!config.isUseOwnInventoryEnabled() && kit == null) {
            lang.sendMessage("DUEL.no-selected-kit", first, second);
            refundItems(items, first, second);
            return;
        }

        final Arena arena = setting.getArena() != null ? setting.getArena() : arenaManager.randomArena(kit);

        if (arena == null || !arena.isAvailable() || (kit != null && kit.isArenaSpecific() && !kit.canUse(arena))) {
            lang.sendMessage(setting.getArena() != null ? "DUEL.arena-in-use" : "DUEL.no-available-arenas", first, second);
            refundItems(items, first, second);
            return;
        }

        final int bet = setting.getBet();

        if (bet > 0 && vault != null && vault.getEconomy() != null) {
            if (!vault.has(bet, first, second)) {
                lang.sendMessage("DUEL.not-enough-money", first, second);
                refundItems(items, first, second);
                return;
            }

            vault.remove(bet, first, second);
        }

        arena.startMatch(kit, items, setting.getBet());
        addPlayers(arena, kit, arena.getPositions(), first, second);

        if (config.isCdEnabled()) {
            arena.startCountdown();
        }

        final MatchStartEvent event = new MatchStartEvent(arena.getMatch(), first, second);
        plugin.getServer().getPluginManager().callEvent(event);
    }

    private void addPlayers(final Arena arena, final Kit kit, final Map<Integer, Location> locations, final Player... players) {
        int position = 0;

        for (final Player player : players) {
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
            arena.add(player);
        }
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

            builder.add(StringUtil.color(color + matchPlayer.getName()), Action.RUN_COMMAND, "/inventoryview " + matchPlayer.getUniqueId());
        }

        builder.send(match.getPlayers());
    }

    private void handleUsers(final Kit kit, final UserData winner, final UserData loser, final MatchData match, final Player... players) {
        if (winner != null && loser != null) {
            winner.addWin();
            loser.addLoss();
            winner.addMatch(match);
            loser.addMatch(match);

            if (kit != null) {
                int winnerRating = winner.getRating(kit);
                int loserRating = loser.getRating(kit);
                final int change = RatingUtil.getChange(config.getKFactor(), winnerRating, loserRating);
                winner.setRating(kit.getName(), winnerRating = winnerRating + change);
                loser.setRating(kit.getName(), loserRating = loserRating - change);

                for (final Player player : players) {
                    lang.sendMessage(player, "DUEL.rating-change",
                        "winner", winner.getName(), "loser", loser.getName(), "change", change, "winner_rating", winnerRating, "loser_rating", loserRating);
                }
            }
        }
    }

    private boolean isSimilar(final Location first, final Location second) {
        return Math.abs(first.getX() - second.getX()) + Math.abs(first.getY() - second.getY()) + Math.abs(first.getZ() - second.getZ()) < 5;
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

            MetadataUtil.put(plugin, player, FactionsHook.METADATA_KEY, true);
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

                    final MatchEndEvent endEvent = new MatchEndEvent(arena.getMatch(), null, null, Reason.TIE);
                    plugin.getServer().getPluginManager().callEvent(endEvent);
                    arena.endMatch();
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

                match.getPlayers().forEach(matchPlayer -> lang.sendMessage(matchPlayer, "DUEL.on-end.opponent-defeat",
                    "winner", winner.getName(), "loser", player.getName(), "health", health, "kit", kit, "arena", arena.getName()));
                handleInventories(match);

                final long duration = System.currentTimeMillis() - match.getStart();
                final long time = new GregorianCalendar().getTimeInMillis();
                final MatchData matchData = new MatchData(winner.getName(), player.getName(), kit, time, duration, health);
                handleUsers(match.getKit(), userDataManager.get(winner), userDataManager.get(player), matchData, winner, player);
                plugin.doSyncAfter(() -> {
                    handleWinner(winner, arena, match);

                    if (config.isEndCommandsEnabled()) {
                        for (final String command : config.getEndCommands()) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{WINNER}", winner.getName()).replace("{LOSER}", player.getName()));
                        }
                    }

                    final MatchEndEvent endEvent = new MatchEndEvent(arena.getMatch(), winner.getUniqueId(), player.getUniqueId(), Reason.OPPONENT_DEFEAT);
                    plugin.getServer().getPluginManager().callEvent(endEvent);
                    arena.endMatch();
                }, config.getTeleportDelay() * 20L);
            }, 1L);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void on(final PlayerRespawnEvent event) {
            final Player player = event.getPlayer();
            MetadataUtil.remove(plugin, player, FactionsHook.METADATA_KEY);

            final PlayerInfo info = playerManager.removeAndGet(player);

            if (info != null) {
                event.setRespawnLocation(info.getLocation());

                if (essentials != null) {
                    essentials.setBackLocation(player, event.getRespawnLocation());
                }

                plugin.doSyncAfter(() -> {
                    if (!config.isUseOwnInventoryEnabled()) {
                        PlayerUtil.reset(player);
                    }

                    info.restore(player);
                }, 1L);
            }
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final EntityDamageEvent event) {
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

        @EventHandler(ignoreCancelled = true)
        public void on(final EntityDamageByEntityEvent event) {
            final Player player;

            if (event.getDamager() instanceof Player) {
                player = (Player) event.getDamager();
            } else if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player) {
                player = (Player) ((Projectile) event.getDamager()).getShooter();
            } else {
                return;
            }

            final Arena arena = arenaManager.get(player);

            if (arena == null || (event.getEntity() instanceof Player && arena.has((Player) event.getEntity()))) {
                return;
            }

            event.setCancelled(true);
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
            lang.sendMessage(event.getPlayer(), "DUEL.prevent-item-drop");
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final PlayerPickupItemEvent event) {
            if (!config.isPreventItemPickup() || !arenaManager.isInMatch(event.getPlayer())) {
                return;
            }

            event.setCancelled(true);
            lang.sendMessage(event.getPlayer(), "DUEL.prevent-item-pickup");
        }


        @EventHandler(ignoreCancelled = true)
        public void on(final PlayerCommandPreprocessEvent event) {
            final String command = event.getMessage().substring(1).split(" ")[0].toLowerCase();

            if (!arenaManager.isInMatch(event.getPlayer()) || (config.isBlockAllCommands() ? config.getWhitelistedCommands().contains(command)
                : !config.getBlacklistedCommands().contains(command))) {
                return;
            }

            event.setCancelled(true);
            lang.sendMessage(event.getPlayer(), "DUEL.prevent-command", "command", event.getMessage());
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void on(PlayerTeleportEvent event) {
            final Player player = event.getPlayer();
            final Location to = event.getTo();
            final boolean inMatch = arenaManager.isInMatch(player);

            if (!inMatch && !spectateManager.isSpectating(player)) {
                final Set<Player> players = arenaManager.getPlayers();
                players.addAll(spectateManager.getPlayers());

                for (final Player target : players) {
                    if (player.equals(target) || !target.isOnline() || !isSimilar(target.getLocation(), to)) {
                        continue;
                    }

                    event.setCancelled(true);
                    lang.sendMessage(player, "ERROR.patch-prevent-teleportation");
                    return;
                }
            }

            if (!config.isLimitTeleportEnabled() || event.getCause() == TeleportCause.ENDER_PEARL || !inMatch) {
                return;
            }

            final Location from = event.getFrom();

            if (from.getWorld().equals(to.getWorld()) && from.distance(to) <= config.getDistanceAllowed()) {
                return;
            }

            event.setCancelled(true);
            lang.sendMessage(player, "DUEL.prevent-teleportation");
        }
    }
}