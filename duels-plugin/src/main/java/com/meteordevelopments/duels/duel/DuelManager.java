package com.meteordevelopments.duels.duel;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.api.event.match.MatchEndEvent.Reason;
import com.meteordevelopments.duels.api.event.match.MatchStartEvent;
import com.meteordevelopments.duels.arena.ArenaImpl;
import com.meteordevelopments.duels.arena.ArenaManagerImpl;
import com.meteordevelopments.duels.arena.MatchImpl;
import com.meteordevelopments.duels.arena.fireworks.FireworkUtils;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.config.Lang;
import com.meteordevelopments.duels.data.MatchData;
import com.meteordevelopments.duels.data.UserData;
import com.meteordevelopments.duels.data.UserManagerImpl;
import com.meteordevelopments.duels.hook.hooks.*;
import com.meteordevelopments.duels.util.*;
import com.meteordevelopments.duels.hook.hooks.worldguard.WorldGuardHook;
import com.meteordevelopments.duels.inventories.InventoryManager;
import com.meteordevelopments.duels.kit.KitImpl;
import com.meteordevelopments.duels.player.PlayerInfo;
import com.meteordevelopments.duels.player.PlayerInfoManager;
import com.meteordevelopments.duels.queue.Queue;
import com.meteordevelopments.duels.queue.QueueManager;
import com.meteordevelopments.duels.setting.Settings;
import com.meteordevelopments.duels.teleport.Teleport;
import com.meteordevelopments.duels.util.compat.CompatUtil;
import com.meteordevelopments.duels.util.compat.Titles;
import com.meteordevelopments.duels.util.function.Pair;
import com.meteordevelopments.duels.util.inventory.InventoryUtil;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import space.arim.morepaperlib.scheduling.ScheduledTask;

import java.util.*;

public class DuelManager implements Loadable {

    private static final Calendar GREGORIAN_CALENDAR = new GregorianCalendar();

    private final DuelsPlugin plugin;
    private final Config config;
    private final Lang lang;
    private final UserManagerImpl userDataManager;
    private final ArenaManagerImpl arenaManager;
    private final PlayerInfoManager playerManager;
    private final InventoryManager inventoryManager;

    private QueueManager queueManager;
    private Teleport teleport;
    private CombatTagPlusHook combatTagPlus;
    private PvPManagerHook pvpManager;
    private CombatLogXHook combatLogX;
    private VaultHook vault;
    private EssentialsHook essentials;
    private McMMOHook mcMMO;
    private WorldGuardHook worldGuard;
    private MyPetHook myPet;

    private ScheduledTask durationCheckTask;

    public DuelManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.userDataManager = plugin.getUserManager();
        this.arenaManager = plugin.getArenaManager();
        this.playerManager = plugin.getPlayerManager();
        this.inventoryManager = plugin.getInventoryManager();

        plugin.doSyncAfter(() -> Bukkit.getPluginManager().registerEvents(new DuelListener(), plugin), 1L);
    }

    @Override
    public void handleLoad() {
        this.queueManager = plugin.getQueueManager();
        this.teleport = plugin.getTeleport();
        this.combatTagPlus = plugin.getHookManager().getHook(CombatTagPlusHook.class);
        this.pvpManager = plugin.getHookManager().getHook(PvPManagerHook.class);
        this.combatLogX = plugin.getHookManager().getHook(CombatLogXHook.class);
        this.vault = plugin.getHookManager().getHook(VaultHook.class);
        this.essentials = plugin.getHookManager().getHook(EssentialsHook.class);
        this.mcMMO = plugin.getHookManager().getHook(McMMOHook.class);
        this.worldGuard = plugin.getHookManager().getHook(WorldGuardHook.class);
        this.myPet = plugin.getHookManager().getHook(MyPetHook.class);

        if (config.getMaxDuration() > 0) {
            this.durationCheckTask = plugin.doSyncRepeat(() -> {
                for (final ArenaImpl arena : arenaManager.getArenasImpl()) {
                    final MatchImpl match = arena.getMatch();

                    // Only handle undecided matches (size > 1)
                    if (match == null
                            || match.getDurationInMillis() < (config.getMaxDuration() * 60 * 1000L)
                            || arena.size() <= 1) {
                        continue;
                    }

                    Set<Player> members = match.getAllPlayers();

                    for (final Player player : members) {

                        handleTie(player, arena, match, true);
                        lang.sendMessage(player, "DUEL.on-end.tie");
                    }

                    arena.endMatch(null, null, Reason.MAX_TIME_REACHED);
                }
            }, 1L, 20L);
        }
    }

    @Override
    public void handleUnload() {
        if (config.getMaxDuration() > 0) {
            plugin.cancelTask(durationCheckTask);
        }

        /*
        3 Cases:
        1. size = 2: Match outcome is yet to be decided (INGAME phase)
        2. size = 1: Match ended with a winner and is in ENDGAME phase
        3. size = 0: Match ended in a tie (or winner killed themselves during ENDGAME phase) and is in ENDGAME phase
        */
        for (final ArenaImpl arena : arenaManager.getArenasImpl()) {
            final MatchImpl match = arena.getMatch();

            if (match == null) {
                continue;
            }

            final int size = arena.size();
            final boolean winnerDecided = size == 1;

            if (winnerDecided) {
                final Player winner = arena.first();
                lang.sendMessage(winner, "DUEL.on-end.plugin-disable");
                handleWin(winner, arena.getOpponent(winner), arena, match);
            } else {
                final boolean ongoing = size > 1;

                for (final Player player : match.getAllPlayers()) {
                    lang.sendMessage(player, "DUEL.on-end.plugin-disable");
                    handleTie(player, arena, match, ongoing);
                }
            }

            arena.endMatch(null, null, Reason.PLUGIN_DISABLE);
        }
    }

    /**
     * Resets the player's inventory and balance in the case of a tie game.
     *
     * @param player Player to reset state
     * @param arena  Arena the match is taking place
     * @param match  Match the player is in
     * @param alive  Whether the player was alive in the match when the method was called.
     */
    private void handleTie(final Player player, final ArenaImpl arena, final MatchImpl match, boolean alive) {
        arena.remove(player);

        // Reset player balance if there was a bet placed.
        if (vault != null && match.getBet() > 0) {
            vault.add(match.getBet(), player);
        }

        if (mcMMO != null) {
            mcMMO.enableSkills(player);
        }

        final PlayerInfo info = playerManager.get(player);
        final List<ItemStack> items = match.getItems(player);

        if (alive) {

            playerManager.remove(player);

            if (!(match.isOwnInventory() && config.isOwnInventoryDropInventoryItems())) {
                PlayerUtil.reset(player);
            }

            if (info != null) {
                teleport.tryTeleport(player, info.getLocation());
                info.restore(player);
            } else {
                // If somehow PlayerInfo is not found...
                teleport.tryTeleport(player, playerManager.getLobby());
            }

            // Give back bet items
            InventoryUtil.addOrDrop(player, items);
        } else if (info != null) {
            // If player remained dead during ENDGAME phase, add the items to cached PlayerInfo of the player.
            info.getExtra().addAll(items);
        } else {
            InventoryUtil.addOrDrop(player, items);
        }
    }

    /**
     * Rewards the duel winner with money and items bet on the match.
     *
     * @param player   Player determined to be the winner
     * @param opponent Player that opposed the winner
     * @param arena    Arena the match is taking place
     * @param match    Match the player is in
     */
    private void handleWin(final Player player, final Player opponent, final ArenaImpl arena, final MatchImpl match) {
        arena.remove(player);
        final String opponentName = opponent != null ? opponent.getName() : lang.getMessage("GENERAL.none");

        // Handle vault rewards if bet exists
        if (vault != null && match.getBet() > 0) {
            final int amount = match.getBet() * 2;
            vault.add(amount, player);
            lang.sendMessage(player, "DUEL.reward.money.message", "name", opponentName, "money", amount);

            final String title = lang.getMessage("DUEL.reward.money.title", "name", opponentName, "money", amount);
            if (title != null) {
                Titles.send(player, title, null, 0, 20, 50);
            }
        }

        // Enable mcMMO skills after the match
        if (mcMMO != null) {
            mcMMO.enableSkills(player);
        }

        final PlayerInfo info = playerManager.get(player);
        final List<ItemStack> items = match.getItems();


        // If the player is still alive, reset their state and teleport
        if (!player.isDead()) {
            playerManager.remove(player);

            if (!(match.isOwnInventory() && config.isOwnInventoryDropInventoryItems())) {
                DuelsPlugin.getMorePaperLib().scheduling().entitySpecificScheduler(player).run(() -> PlayerUtil.reset(player), null);
            }

            // Restore player's inventory and teleport them after the match
            if (info != null) {
                DuelsPlugin.getMorePaperLib().scheduling().entitySpecificScheduler(player).run(() -> {
                    teleport.tryTeleport(player, info.getLocation());
                    info.restore(player);
                }, null);
            }

            // Add or drop items to the player's inventory after teleportation
            DuelsPlugin.getMorePaperLib().scheduling().entitySpecificScheduler(player).run(() -> {
                if (InventoryUtil.addOrDrop(player, items)) {
                    lang.sendMessage(player, "DUEL.reward.items.message", "name", opponentName);
                }
            }, null);
        } else if (info != null) {
            info.getExtra().addAll(items);
        }
    }

    public void startMatch(final Player first, final Player second, final Settings settings, final Map<UUID, List<ItemStack>> items, final Queue source) {
        final KitImpl kit = settings.getKit();

        if (!settings.isOwnInventory() && kit == null) {
            lang.sendMessage(Arrays.asList(first, second), "DUEL.start-failure.mode-unselected");
            refundItems(items, first, second);
            return;
        }

        if (first.isDead() || second.isDead()) {
            lang.sendMessage(Arrays.asList(first, second), "DUEL.start-failure.player-is-dead");
            refundItems(items, first, second);
            return;
        }

        if (isBlacklistedWorld(first) || isBlacklistedWorld(second)) {
            lang.sendMessage(Arrays.asList(first, second), "DUEL.start-failure.in-blacklisted-world");
            refundItems(items, first, second);
            return;
        }

        if (isTagged(first) || isTagged(second)) {
            lang.sendMessage(Arrays.asList(first, second), "DUEL.start-failure.is-tagged");
            refundItems(items, first, second);
            return;
        }

        if (config.isCancelIfMoved() && (notInLoc(first, settings.getBaseLoc(first)) || notInLoc(second, settings.getBaseLoc(second)))) {
            lang.sendMessage(Arrays.asList(first, second), "DUEL.start-failure.player-moved");
            refundItems(items, first, second);
            return;
        }

        if (config.isDuelzoneEnabled() && worldGuard != null && (notInDz(first, settings.getDuelzone(first)) || notInDz(second, settings.getDuelzone(second)))) {
            lang.sendMessage(Arrays.asList(first, second), "DUEL.start-failure.not-in-duelzone");
            refundItems(items, first, second);
            return;
        }

        if (config.isPreventCreativeMode() && (first.getGameMode() == GameMode.CREATIVE || second.getGameMode() == GameMode.CREATIVE)) {
            lang.sendMessage(Arrays.asList(first, second), "DUEL.start-failure.in-creative-mode");
            refundItems(items, first, second);
            return;
        }

        final ArenaImpl arena = settings.getArena() != null ? settings.getArena() : arenaManager.randomArena(kit);

        if (arena == null || !arena.isAvailable()) {
            lang.sendMessage(Arrays.asList(first, second), "DUEL.start-failure." + (settings.getArena() != null ? "arena-in-use" : "no-arena-available"));
            refundItems(items, first, second);
            return;
        }

        if (kit != null && !arenaManager.isSelectable(kit, arena)) {
            lang.sendMessage(Arrays.asList(first, second), "DUEL.start-failure.arena-not-applicable", "kit", kit.getName(), "arena", arena.getName());
            refundItems(items, first, second);
            return;
        }

        final int bet = settings.getBet();

        if (bet > 0 && vault != null && vault.getEconomy() != null) {
            if (!vault.has(bet, first, second)) {
                lang.sendMessage(Arrays.asList(first, second), "DUEL.start-failure.not-enough-money", "bet_amount", bet);
                refundItems(items, first, second);
                return;
            }

            vault.remove(bet, first, second);
        }

        final MatchImpl match = arena.startMatch(kit, items, settings.getBet(), source);
        addPlayers(match, arena, kit, arena.getPositions(), first, second);

        if (config.isCdEnabled()) {
            final Map<UUID, Pair<String, Integer>> info = new HashMap<>();
            info.put(first.getUniqueId(), new Pair<>(second.getName(), getRating(kit, userDataManager.get(second))));
            info.put(second.getUniqueId(), new Pair<>(first.getName(), getRating(kit, userDataManager.get(first))));
            arena.startCountdown(kit != null ? kit.getName() : lang.getMessage("GENERAL.none"), info);
        }

        final MatchStartEvent event = new MatchStartEvent(match, first, second);
        Bukkit.getPluginManager().callEvent(event);
    }

    private void refundItems(final Map<UUID, List<ItemStack>> items, final Player... players) {
        if (items != null) {
            Arrays.stream(players).forEach(player -> InventoryUtil.addOrDrop(player, items.getOrDefault(player.getUniqueId(), Collections.emptyList())));
        }
    }

    private boolean isBlacklistedWorld(final Player player) {
        return config.getBlacklistedWorlds().contains(player.getWorld().getName());
    }

    private boolean isTagged(final Player player) {
        return (combatTagPlus != null && combatTagPlus.isTagged(player))
                || (pvpManager != null && pvpManager.isTagged(player))
                || (combatLogX != null && combatLogX.isTagged(player));
    }

    private boolean notInLoc(final Player player, final Location location) {
        if (location == null) {
            return false;
        }

        final Location source = player.getLocation();
        return !source.getWorld().equals(location.getWorld())
                || source.getBlockX() != location.getBlockX()
                || source.getBlockY() != location.getBlockY()
                || source.getBlockZ() != location.getBlockZ();
    }

    private boolean notInDz(final Player player, final String duelzone) {
        return duelzone != null && !duelzone.equals(worldGuard.findDuelZone(player));
    }

    private int getRating(final KitImpl kit, final UserData user) {
        return user != null ? user.getRating(kit) : config.getDefaultRating();
    }

    private void addPlayers(final MatchImpl match, final ArenaImpl arena, final KitImpl kit, final Map<Integer, Location> locations, final Player... players) {
        int position = 0;

        for (final Player player : players) {
            if (match.getSource() == null) {
                queueManager.remove(player);
            }

            if (player.getAllowFlight()) {
                player.setFlying(false);
                player.setAllowFlight(false);
            }

            player.closeInventory();
            playerManager.create(player, match.isOwnInventory() && config.isOwnInventoryDropInventoryItems());
            teleport.tryTeleport(player, locations.get(++position));

            if (kit != null) {
                PlayerUtil.reset(player);
                kit.equip(player);
            }

            if (config.isStartCommandsEnabled() && !(match.getSource() == null && config.isStartCommandsQueueOnly())) {
                try {
                    for (final String command : config.getStartCommands()) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
                    }
                } catch (Exception ex) {
                    Log.warn(this, "Error while running match start commands: " + ex.getMessage());
                }
            }

            if (myPet != null) {
                myPet.removePet(player);
            }

            if (essentials != null) {
                essentials.tryUnvanish(player);
            }

            if (mcMMO != null) {
                mcMMO.disableSkills(player);
            }

            arena.add(player);
        }
    }

    private void handleInventories(final MatchImpl match) {
        if (!config.isDisplayInventories()) {
            return;
        }

        String color = lang.getMessage("DUEL.inventories.name-color");
        final TextBuilder builder = TextBuilder.of(lang.getMessage("DUEL.inventories.message"));
        final Set<Player> players = match.getAllPlayers();
        final Iterator<Player> iterator = players.iterator();

        while (iterator.hasNext()) {
            final Player player = iterator.next();
            builder.add(StringUtil.color(color + player.getName()), Action.RUN_COMMAND, "/duel _ " + player.getUniqueId());

            if (iterator.hasNext()) {
                builder.add(StringUtil.color(color + ", "));
            }
        }

        builder.send(players);
    }

    private void handleStats(final MatchImpl match, final UserData winner, final UserData loser, final MatchData matchData) {
        if (winner != null && loser != null) {
            winner.addWin();
            loser.addLoss();
            winner.addMatch(matchData);
            loser.addMatch(matchData);

            final KitImpl kit = match.getKit();
            int winnerRating = kit == null ? winner.getRating() : winner.getRating(kit);
            int loserRating = kit == null ? loser.getRating() : loser.getRating(kit);
            int change = 0;

            if (config.isRatingEnabled() && !(!match.isFromQueue() && config.isRatingQueueOnly())) {
                change = NumberUtil.getChange(config.getKFactor(), winnerRating, loserRating);
                winner.setRating(kit, winnerRating = winnerRating + change);
                loser.setRating(kit, loserRating = loserRating - change);
            }

            final String message = lang.getMessage("DUEL.on-end.opponent-defeat",
                    "winner", winner.getName(),
                    "loser", loser.getName(),
                    "health", matchData.getHealth(),
                    "kit", matchData.getKit(),
                    "arena", match.getArena().getName(),
                    "winner_rating", winnerRating,
                    "loser_rating", loserRating,
                    "change", change
            );

            if (message == null) {
                return;
            }

            if (config.isArenaOnlyEndMessage()) {
                match.getArena().broadcast(message);
            } else {
                Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(message));
            }
        }
    }

    private class DuelListener implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST)
        public void on(final PlayerDeathEvent event) {
            final Player player = event.getEntity();
            final ArenaImpl arena = arenaManager.get(player);

            if (arena == null) {
                return;
            }

            if (mcMMO != null) {
                mcMMO.enableSkills(player);
            }

            final MatchImpl match = arena.getMatch();

            if (match == null) {
                return;
            }

            final Inventory top = player.getOpenInventory().getTopInventory();

            if (top.getType() == InventoryType.CRAFTING) {
                top.clear();
            }

            if (!(match.isOwnInventory() && config.isOwnInventoryDropInventoryItems())) {
                event.getDrops().clear();
                event.setKeepLevel(true);
                event.setDroppedExp(0);
                event.setKeepInventory(false);
            }

            inventoryManager.create(player, true);
            arena.remove(player);

            // Call end task only on the first death
            if (arena.size() <= 0) {
                return;
            }

            DuelsPlugin.getMorePaperLib().scheduling().regionSpecificScheduler(arena.first().getLocation()).runDelayed(() -> {
                if (arena.size() == 0) {
                    match.getAllPlayers().forEach(matchPlayer -> {
                        handleTie(matchPlayer, arena, match, false);
                        lang.sendMessage(matchPlayer, "DUEL.on-end.tie");
                    });
                    plugin.doSyncAfter(() -> handleInventories(match), 1L);
                    arena.endMatch(null, null, Reason.TIE);
                    return;
                }

                final Player winner = arena.first();
                inventoryManager.create(winner, false);

                if (config.isSpawnFirework()) {
                    DuelsPlugin.getMorePaperLib().scheduling().regionSpecificScheduler(winner.getLocation()).run(() -> {
                        final Firework firework = (Firework) winner.getWorld().spawnEntity(winner.getEyeLocation(), EntityType.FIREWORK);
                        final FireworkMeta meta = firework.getFireworkMeta();
                        String colourName = config.getFireworkColour();
                        String typeName = config.getFireworkType();
                        Color colour = FireworkUtils.getColor(colourName);
                        FireworkEffect.Type type = FireworkUtils.getType(typeName);
                        meta.setPower(0);
                        meta.addEffect(FireworkEffect.builder().withColor(colour).with(type).withTrail().build());
                        firework.setFireworkMeta(meta);
                    });
                }

                final double health = Math.ceil(winner.getHealth()) * 0.5;
                final String kitName = match.getKit() != null ? match.getKit().getName() : lang.getMessage("GENERAL.none");
                final long duration = System.currentTimeMillis() - match.getStart();
                final long time = GREGORIAN_CALENDAR.getTimeInMillis();
                final MatchData matchData = new MatchData(winner.getName(), player.getName(), kitName, time, duration, health);
                handleStats(match, userDataManager.get(winner), userDataManager.get(player), matchData);
                plugin.doSyncAfter(() -> handleInventories(match), 1L);
                DuelsPlugin.getMorePaperLib().scheduling().entitySpecificScheduler(player).runDelayed(() -> {
                    handleWin(winner, player, arena, match);

                    if (config.isEndCommandsEnabled() && !(!match.isFromQueue() && config.isEndCommandsQueueOnly())) {
                        try {
                            for (final String command : config.getEndCommands()) {
                                int delay = 0;
                                String cmd = command;
                                if (command.contains("{delay:")) {
                                    int delayStart = command.indexOf("{delay:") + 7;
                                    int delayEnd = command.indexOf("}", delayStart);
                                    if (delayEnd > delayStart) {
                                        String delayStr = command.substring(delayStart, delayEnd);
                                        try {
                                            delay = Integer.parseInt(delayStr);
                                        } catch (NumberFormatException e) {
                                            Log.warn(DuelManager.this, "Invalid delay format in command: " + command);
                                        }
                                        // Remove the delay part from the command string
                                        cmd = command.substring(0, delayStart - 7) + command.substring(delayEnd + 1);
                                    }
                                }

                                String finalCommand = cmd.replace("%winner%", winner.getName())
                                        .replace("%loser%", player.getName())
                                        .replace("%kit%", kitName)
                                        .replace("%arena%", arena.getName())
                                        .replace("%bet_amount%", String.valueOf(match.getBet()));

                                if (delay > 0) {
                                    int ticks = Math.max(delay / 50, 1);
                                    DuelsPlugin.getMorePaperLib().scheduling().globalRegionalScheduler().runDelayed(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand), ticks);
                                } else {
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
                                }
                            }
                        } catch (Exception ex) {
                            Log.warn(DuelManager.this, "Error while running match end commands: " + ex.getMessage());
                        }
                    }

                    arena.endMatch(winner.getUniqueId(), player.getUniqueId(), Reason.OPPONENT_DEFEAT);
                }, null, config.getTeleportDelay() * 20L);
            }, 1L);
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final EntityDamageEvent event) {
            if (!(event.getEntity() instanceof Player)) {
                return;
            }

            final Player player = (Player) event.getEntity();
            final ArenaImpl arena = arenaManager.get(player);

            if (arena == null || !arena.isEndGame()) {
                return;
            }

            event.setCancelled(true);
        }

        @EventHandler
        public void on (final PlayerInteractEvent event) {
            if (!config.isDisableEnderpearlInEndgame()){
                return;
            }

            final Player player = event.getPlayer();
            final ArenaImpl arena = arenaManager.get(player);

            if (arena == null || !arena.isEndGame()) {
                return;
            }

            event.setCancelled(true);
        }

        @EventHandler
        public void on(final PlayerMoveEvent event) {
            if (!config.isDisableMovementInEndgame()){
                return;
            }

            final Player player = event.getPlayer();
            final ArenaImpl arena = arenaManager.get(player);

            if (arena == null || !arena.isEndGame()) {
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
            Player player = event.getPlayer();
            if (!arenaManager.isInMatch(event.getPlayer())) {
                return;
            }
            if(config.isPreventItemDrop()) {
                event.setCancelled(true);
                lang.sendMessage(event.getPlayer(), "DUEL.prevent.item-drop");
            }else if(config.isClearItemsAfterMatch()) {
                arenaManager.get(player).getMatch().droppedItems.add(event.getItemDrop());
            }

        }

        @EventHandler(ignoreCancelled = true)
        public void on(final PlayerPickupItemEvent event) {
            // Fix players not being able to use the Loyalty enchantment in a duel if item pickup is disabled in config.
            if (!CompatUtil.isPre1_13() && event.getItem().getItemStack().getType() == Material.TRIDENT) {
                return;
            }

            if (!config.isPreventItemPickup() || !arenaManager.isInMatch(event.getPlayer())) {
                return;
            }

            event.setCancelled(true);
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final PlayerCommandPreprocessEvent event) {
            final String command = event.getMessage().substring(1).split(" ")[0].toLowerCase();

            if (!arenaManager.isInMatch(event.getPlayer())
                    || (config.isBlockAllCommands() ? config.getWhitelistedCommands().contains(command) : !config.getBlacklistedCommands().contains(command))) {
                return;
            }

            event.setCancelled(true);
            lang.sendMessage(event.getPlayer(), "DUEL.prevent.command", "command", event.getMessage());
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final PlayerTeleportEvent event) {
            final Player player = event.getPlayer();
            final Location to = event.getTo();

            if (!config.isLimitTeleportEnabled()
                    || event.getCause() == TeleportCause.ENDER_PEARL
                    || event.getCause() == TeleportCause.SPECTATE
                    || !arenaManager.isInMatch(player)) {
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