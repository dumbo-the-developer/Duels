package com.meteordevelopments.duels.duel;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.api.event.match.MatchEndEvent.Reason;
import com.meteordevelopments.duels.api.event.match.MatchStartEvent;
import com.meteordevelopments.duels.arena.ArenaImpl;
import com.meteordevelopments.duels.arena.ArenaManagerImpl;
import com.meteordevelopments.duels.match.DuelMatch;
import com.meteordevelopments.duels.arena.fireworks.FireworkUtils;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.config.Lang;
import com.meteordevelopments.duels.data.UserManagerImpl;
import com.meteordevelopments.duels.hook.hooks.*;
import com.meteordevelopments.duels.party.Party;
import com.meteordevelopments.duels.party.PartyManagerImpl;
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
import com.meteordevelopments.duels.util.inventory.InventoryUtil;
import com.meteordevelopments.duels.util.validator.ValidatorUtil;
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

    private final DuelsPlugin plugin;
    private final Config config;
    private final Lang lang;
    private final UserManagerImpl userDataManager;
    private final PartyManagerImpl partyManager;
    private final ArenaManagerImpl arenaManager;
    private final PlayerInfoManager playerManager;
    private final InventoryManager inventoryManager;

    private QueueManager queueManager;
    private Teleport teleport;
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
        this.partyManager = plugin.getPartyManager();
        this.arenaManager = plugin.getArenaManager();
        this.playerManager = plugin.getPlayerManager();
        this.inventoryManager = plugin.getInventoryManager();

        plugin.doSyncAfter(() -> Bukkit.getPluginManager().registerEvents(new DuelListener(), plugin), 1L);
    }

    public void handleMatchEnd(DuelMatch match, ArenaImpl arena, Player loser, Location deadLocation, Player winner) {
        DuelsPlugin.getMorePaperLib().scheduling().regionSpecificScheduler(arena.first().getLocation()).runDelayed(() -> {
            if (arena.size() == 0) {
                match.getAllPlayers().forEach(matchPlayer -> {
                    handleTie(matchPlayer, arena, match, false);
                    lang.sendMessage(matchPlayer, "DUEL.on-end.tie");
                });
                plugin.doSyncAfter(() -> inventoryManager.handleMatchEnd(match), 1L);
                arena.endMatch(null, null, Reason.TIE);
                return;
            }

            if (config.isSpawnFirework()) {
                DuelsPlugin.getMorePaperLib().scheduling().regionSpecificScheduler(deadLocation).run(() -> {
                    final Firework firework = (Firework) deadLocation.getWorld().spawnEntity(deadLocation, EntityType.FIREWORK);
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

            final Set<Player> winners = match.getAlivePlayers();
            winners.forEach(w -> inventoryManager.create(w, false));
            userDataManager.handleMatchEnd(match, winners);
            plugin.doSyncAfter(() -> inventoryManager.handleMatchEnd(match), 1L);
            DuelsPlugin.getMorePaperLib().scheduling().entitySpecificScheduler(loser).runDelayed(() -> {
                for (Player alivePlayer : winners) {
                    handleWin(alivePlayer, loser, arena, match);
                }

                if (config.isEndCommandsEnabled() && !(!match.isFromQueue() && config.isEndCommandsQueueOnly())) {
                    try {
                        for (final String command : config.getEndCommands()) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                                    .replace("%winner%", winner.getName()).replace("%loser%", loser.getName())
                                    .replace("%kit%", match.getKit() != null ? match.getKit().getName() : "").replace("%arena%", arena.getName())
                                    .replace("%bet_amount%", String.valueOf(match.getBet()))
                            );
                        }
                    } catch (Exception ex) {
                        Log.warn(DuelManager.this, "Error while running match end commands: " + ex.getMessage());
                    }
                }

                arena.endMatch(winner.getUniqueId(), loser.getUniqueId(), Reason.OPPONENT_DEFEAT);
            }, null, config.getTeleportDelay() * 20L);
        }, 1L);
    }

    @Override
    public void handleLoad() {
        this.queueManager = plugin.getQueueManager();
        this.teleport = plugin.getTeleport();
        this.vault = plugin.getHookManager().getHook(VaultHook.class);
        this.essentials = plugin.getHookManager().getHook(EssentialsHook.class);
        this.mcMMO = plugin.getHookManager().getHook(McMMOHook.class);
        this.myPet = plugin.getHookManager().getHook(MyPetHook.class);

        if (config.getMaxDuration() > 0) {
            this.durationCheckTask = plugin.doSyncRepeat(() -> {
                for (final ArenaImpl arena : arenaManager.getArenasImpl()) {
                    final DuelMatch match = arena.getMatch();

                    // Only handle undecided matches (size > 1)
                    if (match == null || match.getDurationInMillis() < (config.getMaxDuration() * 60 * 1000L) || arena.isEndGame()) {
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
            final DuelMatch match = arena.getMatch();

            if (match == null) {
                continue;
            }

            final int size = arena.size();
            final boolean winnerDecided = size == 1;

            if (winnerDecided) {
                for (final Player winner : match.getAlivePlayers()) {
                    lang.sendMessage(winner, "DUEL.on-end.plugin-disable");
                    handleWin(winner, arena.getOpponent(winner), arena, match);
                }
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
    private void handleTie(final Player player, final ArenaImpl arena, final DuelMatch match, boolean alive) {
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
     * @param winner   Player determined to be the winner
     * @param opponent Player that opposed the winner
     * @param arena    Arena the match is taking place
     * @param match    Match the player is in
     */
    private void handleWin(final Player winner, final Player opponent, final ArenaImpl arena, final DuelMatch match) {
        arena.remove(winner);

        final String opponentName = opponent != null ? opponent.getName() : lang.getMessage("GENERAL.none");

        if (vault != null && match.getBet() > 0) {
            final int amount = match.getBet() * 2;
            vault.add(amount, winner);
            lang.sendMessage(winner, "DUEL.reward.money.message", "name", opponentName, "money", amount);

            final String title = lang.getMessage("DUEL.reward.money.title", "name", opponentName, "money", amount);

            if (title != null) {
                Titles.send(winner, title, null, 0, 20, 50);
            }
        }

        if (mcMMO != null) {
            mcMMO.enableSkills(winner);
        }

        final PlayerInfo info = playerManager.get(winner);
        final List<ItemStack> items = match.getItems();

        if (!winner.isDead()) {
            playerManager.remove(winner);

            if (!(match.isOwnInventory() && config.isOwnInventoryDropInventoryItems())) {
                PlayerUtil.reset(winner);
            }

            if (info != null) {
                teleport.tryTeleport(winner, info.getLocation());
                info.restore(winner);
            }

            if (InventoryUtil.addOrDrop(winner, items)) {
                lang.sendMessage(winner, "DUEL.reward.items.message", "name", opponentName);
            }
        } else if (info != null) {
            info.getExtra().addAll(items);
        }
    }

    private void refundItems(final Collection<Player> players, final Map<UUID, List<ItemStack>> items) {
        if (items != null) {
            players.forEach(player -> InventoryUtil.addOrDrop(player, items.getOrDefault(player.getUniqueId(), Collections.emptyList())));
        }
    }

    public boolean startMatch(final Collection<Player> first, final Collection<Player> second, final Settings settings, final Map<UUID, List<ItemStack>> items, final Queue source) {
        final Collection<Player> players = new ArrayList<>(first.size() + second.size());
        players.addAll(first);
        players.addAll(second);

        if (!ValidatorUtil.validate(plugin.getValidatorManager().getMatchValidators(), players, settings)) {
            refundItems(players, items);
            return false;
        }

        final KitImpl kit = settings.getKit();
        final ArenaImpl arena = settings.getArena() != null ? settings.getArena() : arenaManager.randomArena(kit);

        if (arena == null || !arena.isAvailable()) {
            lang.sendMessage(players, "DUEL.start-failure." + (settings.getArena() != null ? "arena-in-use" : "no-arena-available"));
            refundItems(players, items);
            return false;
        }

        if (kit != null && !arenaManager.isSelectable(kit, arena)) {
            lang.sendMessage(players, "DUEL.start-failure.arena-not-applicable", "kit", kit.getName(), "arena", arena.getName());
            refundItems(players, items);
            return false;
        }

        final int bet = settings.getBet();

        if (bet > 0 && vault != null) {
            if (!vault.has(bet, players)) {
                lang.sendMessage(players, "DUEL.start-failure.not-enough-money", "bet_amount", bet);
                refundItems(players, items);
                return false;
            }

            vault.remove(bet, players);
        }

        // Acquire a short Redis lock on arena to avoid cross-server double assignment
        String lockKey = null;
        String lockVal = java.util.UUID.randomUUID().toString();
        try {
            if (plugin.getRedisService() != null) {
                lockKey = "duels:arena:lock:" + arena.getName();
                if (!plugin.getRedisService().tryAcquireLock(lockKey, 10, lockVal)) {
                    lang.sendMessage(players, "DUEL.start-failure.arena-in-use");
                    refundItems(players, items);
                    if (bet > 0 && vault != null) {
                        vault.add(bet, players);
                    }
                    return false;
                }
            }

            final DuelMatch match;
            try {
                match = arena.startMatch(kit, items, settings, source);
                addPlayers(first, match, arena, kit, arena.getPosition(1));
                addPlayers(second, match, arena, kit, arena.getPosition(2));
            } catch (Exception ex) {
                refundItems(players, items);
                if (bet > 0 && vault != null) {
                    vault.add(bet, players);
                }
                lang.sendMessage(players, "DUEL.start-failure.arena-in-use");
                return false;
            }

        if (config.isCdEnabled()) {
            arena.startCountdown();
        }

            final MatchStartEvent event = new MatchStartEvent(match, players.toArray(new Player[0]));
            Bukkit.getPluginManager().callEvent(event);
            return true;
        } finally {
            if (lockKey != null && plugin.getRedisService() != null) {
                plugin.getRedisService().releaseLock(lockKey, lockVal);
            }
        }
    }

    public void startMatch(final Player sender, final Player target, final Settings settings, final Map<UUID, List<ItemStack>> items, final Queue source) {
        final Party senderParty = partyManager.get(sender);
        final Party targetParty = partyManager.get(target);

        if (senderParty != null && targetParty != null) {
            if (!settings.getSenderParty().equals(senderParty) || !settings.getTargetParty().equals(targetParty)) {
                lang.sendMessage(Arrays.asList(sender, target), "DUEL.party-start-failure.party-changed");
                return;
            }

            startMatch(settings.getSenderParty().getOnlineMembers(), settings.getTargetParty().getOnlineMembers(), settings, items, source);
        } else if (senderParty != null || targetParty != null) {
            lang.sendMessage(Arrays.asList(sender, target), "DUEL.party-start-failure.party-changed");
        } else {
            startMatch(Collections.singleton(sender), Collections.singleton(target), settings, items, source);
        }
    }

    private void addPlayers(final Collection<Player> players, final DuelMatch match, final ArenaImpl arena, final KitImpl kit, final Location location) {
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
            teleport.tryTeleport(player, location);

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

            final DuelMatch match = arena.getMatch();

            if (match == null) {
                return;
            }

            // Skip death handling for ROUNDS3 as it's handled in EntityDamageEvent
            if (match.getKit() != null && match.getKit().hasCharacteristic(KitImpl.Characteristic.ROUNDS3)) {
                event.setDeathMessage(null);
                event.getDrops().clear();
                event.setKeepLevel(true);
                event.setDroppedExp(0);
                event.setKeepInventory(true);
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

            if (config.isSendDeathMessages()) {
                final Player killer = player.getKiller();

                if (killer != null) {
                    final double health = Math.ceil(killer.getHealth()) * 0.5;
                    arena.broadcast(lang.getMessage("DUEL.on-death.with-killer", "name", player.getName(), "killer", killer.getName(), "health", health));
                } else {
                    arena.broadcast(lang.getMessage("DUEL.on-death.no-killer", "name", player.getName()));
                }
            }

            final int prevSize = match.size();
            arena.remove(player);

            if (prevSize < 2 || match.size() >= prevSize) {
                return;
            }

            final Location deadLocation = player.getEyeLocation().clone();
            handleMatchEnd(match, arena, player, deadLocation, match.getAlivePlayers().iterator().next());
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final EntityDamageEvent event) {
            if (!(event.getEntity() instanceof Player player)) {
                return;
            }

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
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.updateInventory();
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
                Objects.requireNonNull(Objects.requireNonNull(arenaManager.get(player)).getMatch()).droppedItems.add(event.getItemDrop());
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