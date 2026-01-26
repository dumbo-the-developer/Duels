package com.meteordevelopments.duels.duel;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.api.event.match.MatchEndEvent.Reason;
import com.meteordevelopments.duels.api.event.match.MatchStartEvent;
import com.meteordevelopments.duels.arena.ArenaImpl;
import com.meteordevelopments.duels.arena.ArenaManagerImpl;
import com.meteordevelopments.duels.match.DuelMatch;
import com.meteordevelopments.duels.match.team.TeamDuelMatch;
import com.meteordevelopments.duels.arena.fireworks.FireworkUtils;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.config.Lang;
import com.meteordevelopments.duels.data.UserManagerImpl;
import com.meteordevelopments.duels.hook.hooks.*;
import com.meteordevelopments.duels.party.Party;
import com.meteordevelopments.duels.party.PartyManagerImpl;
import com.meteordevelopments.duels.util.*;
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
import com.meteordevelopments.duels.util.SchedulerAdapter.TaskWrapper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class DuelManager implements Loadable {
    
    private static final Pattern DELAY_PATTERN = Pattern.compile("\\{delay:(\\d+)\\}");

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
    private MyPetHook myPet;

    private TaskWrapper durationCheckTask;
    // CRITICAL: Use synchronized collections for thread-safety
    // Even though runTaskTimer runs on main thread on Paper, Folia might use async scheduler
    private final Map<ArenaImpl, TaskWrapper> activeCheckRoutines = Collections.synchronizedMap(new HashMap<>());
    private final Set<ArenaImpl> activeCheckRoutineFlags = Collections.synchronizedSet(new HashSet<>());

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
        // CRITICAL: Synchronized cleanup to prevent race conditions
        synchronized (activeCheckRoutineFlags) {
            activeCheckRoutineFlags.remove(arena);
            final TaskWrapper routineTask = activeCheckRoutines.remove(arena);
            if (routineTask != null && !routineTask.isCancelled()) {
                routineTask.cancel();
            }
        }

        // CRITICAL: Check if arena has players before accessing first() to prevent NPE
        final Player firstPlayer = arena.first();
        final Location taskLocation = firstPlayer != null ? firstPlayer.getLocation() : (deadLocation != null ? deadLocation : arena.getPosition(1));
        if (taskLocation == null || taskLocation.getWorld() == null) {
            Log.warn(this, "Cannot schedule match end task - no valid location available");
            return;
        }

        DuelsPlugin.getSchedulerAdapter().runTask(taskLocation, () -> {
            if (arena.size() == 0) {
                match.getAllPlayers().forEach(matchPlayer -> {
                    handleTie(matchPlayer, arena, match, false);
                    if (config.isMessageOnEndTie()) {
                        lang.sendMessage(matchPlayer, "DUEL.on-end.tie");
                    }
                });
                plugin.doSyncAfter(() -> inventoryManager.handleMatchEnd(match), 1L);
                arena.endMatch(null, null, Reason.TIE);
                return;
            }

            if (config.isSpawnFirework()) {
                DuelsPlugin.getSchedulerAdapter().runTask(deadLocation, () -> {
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
            
            // Execute pre-end commands (run when winner is announced, before handleWin)
            if (config.isPreEndCommandsEnabled() && !(!match.isFromQueue() && config.isPreEndCommandsQueueOnly())) {
                try {
                    for (final String command : config.getPreEndCommands()) {
                        String processedCommand = command
                                .replace("%winner%", winner.getName()).replace("%loser%", loser.getName())
                                .replace("%kit%", match.getKit() != null ? match.getKit().getName() : "").replace("%arena%", arena.getName())
                                .replace("%bet_amount%", String.valueOf(match.getBet()));
                        
                        executeCommandWithDelay(processedCommand);
                    }
                } catch (Exception ex) {
                    Log.warn(DuelManager.this, "Error while running pre-end commands: " + ex.getMessage());
                }
            }
            
            plugin.doSyncAfter(() -> inventoryManager.handleMatchEnd(match), 1L);
            
            // FIXED: Use global scheduler - completely entity-independent
            // CRITICAL FIX: Always preserve PlayerInfo for inventory restoration, even if players are offline
            // PlayerInfo will be preserved for offline players and restored when they rejoin
            DuelsPlugin.getSchedulerAdapter().runTaskLater(() -> {
                // Check if match still exists and is not finished
                if (arena.getMatch() == null || arena.getMatch() != match || match.isFinished()) {
                    return; // Match already ended or cleared
                }
                
                // CRITICAL FIX: Handle loser - restore if online, preserve PlayerInfo if offline
                final PlayerInfo loserInfo = playerManager.get(loser);
                if (loser.isOnline()) {
                    // Handle the loser (remove from arena and restore state)
                    // This is especially important for ROUNDS3 where the loser never actually dies
                    if (match.getKit() != null && match.getKit().hasCharacteristic(KitImpl.Characteristic.ROUNDS3)) {
                        handleLoss(loser, arena, match);
                    }
                } else if (loserInfo != null) {
                    // Loser is offline - preserve PlayerInfo for restoration on rejoin
                    // Don't remove PlayerInfo - it will be restored when player rejoins
                    // Add bet items to extra items for restoration
                    final List<ItemStack> items = match.getItems(loser);
                    if (items != null && !items.isEmpty()) {
                        loserInfo.getExtra().addAll(items);
                    }
                }
                
                // CRITICAL FIX: Handle winners - restore if online, preserve PlayerInfo if offline
                for (Player alivePlayer : winners) {
                    if (alivePlayer.isOnline()) {
                        handleWin(alivePlayer, loser, arena, match);
                    } else {
                        // Winner is offline - preserve PlayerInfo for restoration on rejoin
                        final PlayerInfo winnerInfo = playerManager.get(alivePlayer);
                        if (winnerInfo != null) {
                            // Add reward items to extra items for restoration
                            final List<ItemStack> items = match.getItems();
                            if (items != null && !items.isEmpty()) {
                                winnerInfo.getExtra().addAll(items);
                            }
                            // Don't remove PlayerInfo - it will be restored when player rejoins
                        }
                    }
                }

                if (config.isEndCommandsEnabled() && !(!match.isFromQueue() && config.isEndCommandsQueueOnly())) {
                    try {
                        for (final String command : config.getEndCommands()) {
                            String processedCommand = command
                                    .replace("%winner%", winner.getName()).replace("%loser%", loser.getName())
                                    .replace("%kit%", match.getKit() != null ? match.getKit().getName() : "").replace("%arena%", arena.getName())
                                    .replace("%bet_amount%", String.valueOf(match.getBet()));
                            
                            executeCommandWithDelay(processedCommand);
                        }
                    } catch (Exception ex) {
                        Log.warn(DuelManager.this, "Error while running match end commands: " + ex.getMessage());
                    }
                }

                // CRITICAL: Check if match still exists before ending (might have been cleared already)
                if (arena.getMatch() != null && arena.getMatch() == match) {
                    arena.endMatch(winner.getUniqueId(), loser.getUniqueId(), Reason.OPPONENT_DEFEAT);
                }
            }, config.getTeleportDelay() * 20L);
        });
    }

    public void handleTeamMatchEnd(TeamDuelMatch match, ArenaImpl arena, Location deadLocation, TeamDuelMatch.Team winningTeam) {
        // CRITICAL: Synchronized cleanup to prevent race conditions
        synchronized (activeCheckRoutineFlags) {
            activeCheckRoutineFlags.remove(arena);
            final TaskWrapper routineTask = activeCheckRoutines.remove(arena);
            if (routineTask != null && !routineTask.isCancelled()) {
                routineTask.cancel();
            }
        }

        // Safety check: Ensure deadLocation is valid before scheduling
        if (deadLocation == null || deadLocation.getWorld() == null) {
            Log.warn(this, "Cannot schedule team match end task - deadLocation is null or world is null");
            return;
        }
        DuelsPlugin.getSchedulerAdapter().runTaskLater(deadLocation, () -> {
            if (config.isSpawnFirework()) {
                DuelsPlugin.getSchedulerAdapter().runTask(deadLocation, () -> {
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

            final Set<Player> winners = match.getWinningTeamPlayers();
            final Set<Player> losers = match.getLosingTeamPlayers();
            
            // Restore gamemode for all players immediately using individual schedulers
            final Set<Player> allPlayers = match.getAllPlayers();
            for (Player player : allPlayers) {
                DuelsPlugin.getSchedulerAdapter().runTask(player, () -> {
                    player.setGameMode(GameMode.SURVIVAL);
                    player.setAllowFlight(false);
                    player.setFlying(false);
                });
            }
            
            // Create inventory snapshots for display
            allPlayers.forEach(p -> inventoryManager.create(p, false));
            userDataManager.handleTeamMatchEnd(match, winners, losers);
            
            // Execute pre-end commands (run when winners are announced, before handleTeamWin)
            if (config.isPreEndCommandsEnabled() && !(!match.isFromQueue() && config.isPreEndCommandsQueueOnly())) {
                try {
                    for (final String command : config.getPreEndCommands()) {
                        String winnerNames = winners.stream().map(Player::getName).collect(Collectors.joining(", "));
                        String loserNames = losers.stream().map(Player::getName).collect(Collectors.joining(", "));
                        String processedCommand = command
                                .replace("%winner%", winnerNames).replace("%loser%", loserNames)
                                .replace("%kit%", match.getKit() != null ? match.getKit().getName() : "").replace("%arena%", arena.getName())
                                .replace("%bet_amount%", String.valueOf(match.getBet()));
                        
                        executeCommandWithDelay(processedCommand);
                    }
                } catch (Exception ex) {
                    Log.warn(DuelManager.this, "Error while running pre-end commands: " + ex.getMessage());
                }
            }
            
            plugin.doSyncAfter(() -> inventoryManager.handleMatchEnd(match), 1L);
            
            // Schedule teleportation and restoration for all players after delay
            // Safety check: Ensure deadLocation is still valid
            if (deadLocation == null || deadLocation.getWorld() == null) {
                Log.warn(this, "Cannot schedule teleportation task - deadLocation is null or world is null");
                return;
            }
            DuelsPlugin.getSchedulerAdapter().runTaskLater(deadLocation, () -> {
                // Handle losers (including dead spectators)
                for (Player loser : losers) {
                    if (mcMMO != null) {
                        mcMMO.enableSkills(loser);
                    }
                    
                    final PlayerInfo loserInfo = playerManager.get(loser);
                    if (loserInfo != null && !loser.isDead()) {
                        playerManager.remove(loser);
                        
                        // Schedule player operations on entity-specific scheduler for Folia compatibility
                        DuelsPlugin.getSchedulerAdapter().runTask(loser, () -> {
                            if (!(match.isOwnInventory() && config.isOwnInventoryDropInventoryItems())) {
                                PlayerUtil.reset(loser);
                            }
                            
                            // FIXED: Wait for teleport to complete before restoring to prevent dimension change errors
                            teleport.tryTeleport(loser, loserInfo.getLocation(), () -> {
                                DuelsPlugin.getSchedulerAdapter().runTask(loser, () -> {
                                    if (loser.isOnline()) {
                                        loserInfo.restore(loser);
                                    }
                                });
                            });
                        });
                    }
                }
                
                // Handle winners
                for (Player winner : winners) {
                    handleTeamWin(winner, losers, arena, match);
                }

                if (config.isEndCommandsEnabled() && !(!match.isFromQueue() && config.isEndCommandsQueueOnly())) {
                    try {
                        for (final String command : config.getEndCommands()) {
                            String winnerNames = winners.stream().map(Player::getName).collect(Collectors.joining(", "));
                            String loserNames = losers.stream().map(Player::getName).collect(Collectors.joining(", "));
                            String processedCommand = command
                                    .replace("%winner%", winnerNames).replace("%loser%", loserNames)
                                    .replace("%kit%", match.getKit() != null ? match.getKit().getName() : "").replace("%arena%", arena.getName())
                                    .replace("%bet_amount%", String.valueOf(match.getBet()));
                            
                            executeCommandWithDelay(processedCommand);
                        }
                    } catch (Exception ex) {
                        Log.warn(DuelManager.this, "Error while running match end commands: " + ex.getMessage());
                    }
                }

                // FIXED: Use global scheduler for arena.endMatch() to avoid Folia region conflicts
                // This prevents errors when entities are in different regions or already removed by other plugins
                DuelsPlugin.getSchedulerAdapter().runTask(() -> {
                    arena.endMatch(winners.iterator().next().getUniqueId(), losers.iterator().next().getUniqueId(), Reason.OPPONENT_DEFEAT);
                });
            }, config.getTeleportDelay() * 20L);
        }, 1L);
    }

    private void handleTeamWin(final Player winner, final Set<Player> losers, final ArenaImpl arena, final DuelMatch match) {
        // Don't remove the winner in team matches - they should stay alive
        // arena.remove(winner);

        final String opponentNames = losers.stream().map(Player::getName).collect(Collectors.joining(", "));

        if (vault != null && match.getBet() > 0) {
            final int amount = match.getBet() * 2;
            vault.add(amount, winner);
            lang.sendMessage(winner, "DUEL.reward.money.message", "name", opponentNames, "money", amount);

            final String title = lang.getMessage("DUEL.reward.money.title", "name", opponentNames, "money", amount);

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
                // FIXED: Wait for teleport to complete before restoring to prevent dimension change errors
                teleport.tryTeleport(winner, info.getLocation(), () -> {
                    DuelsPlugin.getSchedulerAdapter().runTask(winner, () -> {
                        if (winner.isOnline()) {
                            if (match.isOwnInventory()) {
                                // Preserve any XP gained during the duel when using own inventory
                                info.restoreWithoutExperience(winner);
                            } else {
                                // Restore saved experience for non-own-inventory matches
                                info.restore(winner);
                            }
                        }
                    });
                });
            }

            if (InventoryUtil.addOrDrop(winner, items)) {
                lang.sendMessage(winner, "DUEL.reward.items.message", "name", opponentNames);
            }
        } else if (info != null) {
            info.getExtra().addAll(items);
        }
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
            this.durationCheckTask = (TaskWrapper) plugin.doSyncRepeat(() -> {
                for (final ArenaImpl arena : arenaManager.getArenasImpl()) {
                    final DuelMatch match = arena.getMatch();

                    // Only handle undecided matches (size > 1)
                    if (match == null || match.getDurationInMillis() < (config.getMaxDuration() * 60 * 1000L) || arena.isEndGame()) {
                        continue;
                    }

                    // Iterate over a snapshot to avoid modifying the underlying set while processing
                    final List<Player> members = new ArrayList<>(match.getAllPlayers());
                    
                    // FIXED: Handle each player safely with proper scheduler and online checks
                    for (final Player player : members) {
                        // CRITICAL: Check online status first to prevent entity scheduler errors
                        if (!player.isOnline()) {
                            // Offline player: Do minimal cleanup on global scheduler (no entity operations)
                            try {
                                arena.remove(player);
                                
                                // Store items for when they rejoin
                                final PlayerInfo offlineInfo = playerManager.get(player);
                                final List<ItemStack> offlineItems = match.getItems(player);
                                if (offlineInfo != null && offlineItems != null) {
                                    offlineInfo.getExtra().addAll(offlineItems);
                                }
                                
                                // Clean up manager
                                playerManager.remove(player);
                            } catch (Exception ex) {
                                Log.warn(this, "Error handling offline player " + player.getName() + " in max duration timeout: " + ex.getMessage());
                            }
                            continue;
                        }
                        
                        // Online player: Use entity scheduler for all entity operations
                        final boolean alive = !match.isDead(player);
                        
                        try {
                            DuelsPlugin.getSchedulerAdapter().runTask(player, () -> {
                                try {
                                    // Double-check player is still online inside scheduler
                                    if (!player.isOnline()) {
                                        return;
                                    }
                                    
                                    handleTie(player, arena, match, alive);
                                    if (config.isMessageOnEndTie()) {
                                        lang.sendMessage(player, "DUEL.on-end.tie");
                                    }
                                } catch (Exception ex) {
                                    Log.warn(this, "Error handling tie for player " + player.getName() + ": " + ex.getMessage());
                                }
                            });
                        } catch (Exception ex) {
                            // Fallback: If entity scheduler fails, try on global scheduler with minimal operations
                            Log.warn(this, "Failed to schedule entity task for " + player.getName() + ", using fallback: " + ex.getMessage());
                            try {
                                arena.remove(player);
                                playerManager.remove(player);
                            } catch (Exception ex2) {
                                Log.warn(this, "Fallback cleanup failed for " + player.getName() + ": " + ex2.getMessage());
                            }
                        }
                    }

                    // FIXED: Always end match on global scheduler after processing all players
                    DuelsPlugin.getSchedulerAdapter().runTaskLater(() -> {
                        try {
                            arena.endMatch(null, null, Reason.MAX_TIME_REACHED);
                        } catch (Exception ex) {
                            Log.warn(this, "Error ending match in arena " + arena.getName() + ": " + ex.getMessage());
                        }
                    }, 3L); // 3 tick delay to ensure all entity scheduler tasks have started
                }
            }, 1L, 20L);
        }
    }

    @Override
    public void handleUnload() {
        if (config.getMaxDuration() > 0) {
            plugin.cancelTask(durationCheckTask);
        }

        // CRITICAL: Cancel all active check-for-players-routine tasks to prevent memory leaks
        synchronized (activeCheckRoutineFlags) {
            for (final ArenaImpl arena : new ArrayList<>(activeCheckRoutineFlags)) {
                final TaskWrapper routineTask = activeCheckRoutines.remove(arena);
                if (routineTask != null && !routineTask.isCancelled()) {
                    routineTask.cancel();
                }
                activeCheckRoutineFlags.remove(arena);
            }
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
                    if (config.isMessageOnEndPluginDisable()) {
                        lang.sendMessage(winner, "DUEL.on-end.plugin-disable");
                    }
                    handleWin(winner, arena.getOpponent(winner), arena, match);
                }
            } else {
                final boolean ongoing = size > 1;

                for (final Player player : match.getAllPlayers()) {
                    if (match.isDead(player)) continue;

                    if (config.isMessageOnEndPluginDisable()) {
                        lang.sendMessage(player, "DUEL.on-end.plugin-disable");
                    }
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
                // FIXED: Wait for teleport to complete before restoring to prevent dimension change errors
                teleport.tryTeleport(player, info.getLocation(), () -> {
                    DuelsPlugin.getSchedulerAdapter().runTask(player, () -> {
                        if (player.isOnline()) {
                            info.restore(player);
                        }
                    });
                });
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
     * Emergency hard reset for a stuck arena. Resets all players and ends match as tie.
     * This method safely handles both online and offline players using proper Folia schedulers.
     * 
     * @param arena Arena to reset
     * @return True if reset was successful, false if arena has no active match
     */
    public boolean hardResetArena(final ArenaImpl arena) {
        final DuelMatch match = arena.getMatch();
        if (match == null || !arena.isUsed()) {
            return false;
        }
        
        // CRITICAL: Synchronized cleanup to prevent race conditions
        synchronized (activeCheckRoutineFlags) {
            activeCheckRoutineFlags.remove(arena);
            final TaskWrapper routineTask = activeCheckRoutines.remove(arena);
            if (routineTask != null && !routineTask.isCancelled()) {
                routineTask.cancel();
            }
        }
        
        // Get all players in the match
        final List<Player> members = new ArrayList<>(match.getAllPlayers());
        
        if (members.isEmpty()) {
            return false;
        }
        
        // Handle each player safely with proper schedulers
        for (final Player player : members) {
            if (!player.isOnline()) {
                // Offline player: Do minimal cleanup on global scheduler
                try {
                    arena.remove(player);
                    
                    // Store items for when they rejoin
                    final PlayerInfo offlineInfo = playerManager.get(player);
                    final List<ItemStack> offlineItems = match.getItems(player);
                    if (offlineInfo != null && offlineItems != null) {
                        offlineInfo.getExtra().addAll(offlineItems);
                    }
                    
                    // Clean up manager
                    playerManager.remove(player);
                } catch (Exception ex) {
                    Log.warn(this, "Error handling offline player " + player.getName() + " in hardreset: " + ex.getMessage());
                }
                continue;
            }
            
            // Online player: Use entity scheduler for all entity operations
            final boolean alive = !match.isDead(player);
            
            try {
                DuelsPlugin.getSchedulerAdapter().runTask(player, () -> {
                    try {
                        // Double-check player is still online inside scheduler
                        if (!player.isOnline()) {
                            return;
                        }
                        
                        handleTie(player, arena, match, alive);
                        if (config.isMessageOnEndTie()) {
                            lang.sendMessage(player, "DUEL.on-end.tie");
                        }
                    } catch (Exception ex) {
                        Log.warn(this, "Error handling tie for player " + player.getName() + " in hardreset: " + ex.getMessage());
                    }
                });
            } catch (Exception ex) {
                // Fallback: If entity scheduler fails, try on global scheduler with minimal operations
                Log.warn(this, "Failed to schedule entity task for " + player.getName() + ", using fallback: " + ex.getMessage());
                try {
                    arena.remove(player);
                    playerManager.remove(player);
                } catch (Exception ex2) {
                    Log.warn(this, "Fallback cleanup failed for " + player.getName() + ": " + ex2.getMessage());
                }
            }
        }
        
        // Execute hardstop commands if enabled
        if (config.isHardstopCommandsEnabled()) {
            try {
                // Build comma-separated list of player names
                String playersList = members.stream()
                    .filter(Player::isOnline)
                    .map(Player::getName)
                    .collect(Collectors.joining(", "));
                
                if (playersList.isEmpty()) {
                    playersList = "None";
                }
                
                for (final String command : config.getHardstopCommands()) {
                    String processedCommand = command
                        .replace("%arena%", arena.getName())
                        .replace("%players%", playersList)
                        .replace("%kit%", match.getKit() != null ? match.getKit().getName() : "")
                        .replace("%bet_amount%", String.valueOf(match.getBet()));
                    
                    executeCommandWithDelay(processedCommand);
                }
            } catch (Exception ex) {
                Log.warn(this, "Error while running hardstop commands: " + ex.getMessage());
            }
        }
        
        // Always end match on global scheduler after processing all players
        DuelsPlugin.getSchedulerAdapter().runTaskLater(() -> {
            try {
                arena.endMatch(null, null, Reason.TIE);
            } catch (Exception ex) {
                Log.warn(this, "Error ending match in arena " + arena.getName() + " during hardreset: " + ex.getMessage());
            }
        }, 3L); // 3 tick delay to ensure all entity scheduler tasks have started
        
        return true;
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
                // FIXED: Wait for teleport to complete before restoring to prevent dimension change errors
                teleport.tryTeleport(winner, info.getLocation(), () -> {
                    DuelsPlugin.getSchedulerAdapter().runTask(winner, () -> {
                        if (winner.isOnline()) {
                            if (match.isOwnInventory()) {
                                // Preserve any XP gained during the duel when using own inventory
                                info.restoreWithoutExperience(winner);
                            } else {
                                // Restore saved experience for non-own-inventory matches
                                info.restore(winner);
                            }
                        }
                    });
                });
            }

            if (InventoryUtil.addOrDrop(winner, items)) {
                lang.sendMessage(winner, "DUEL.reward.items.message", "name", opponentName);
            }
        } else if (info != null) {
            info.getExtra().addAll(items);
        }
    }

    /**
     * Handles the loser of a match, removing them from the arena and restoring their state.
     * This is primarily used for ROUNDS3 matches where the loser never actually dies.
     *
     * @param loser The player who lost the match
     * @param arena Arena the match is taking place
     * @param match Match the player is in
     */
    private void handleLoss(final Player loser, final ArenaImpl arena, final DuelMatch match) {
        arena.remove(loser);

        if (mcMMO != null) {
            mcMMO.enableSkills(loser);
        }

        final PlayerInfo info = playerManager.get(loser);

        if (!loser.isDead()) {
            playerManager.remove(loser);

            if (!(match.isOwnInventory() && config.isOwnInventoryDropInventoryItems())) {
                PlayerUtil.reset(loser);
            }

            if (info != null) {
                // FIXED: Wait for teleport to complete before restoring to prevent dimension change errors
                teleport.tryTeleport(loser, info.getLocation(), () -> {
                    DuelsPlugin.getSchedulerAdapter().runTask(loser, () -> {
                        if (loser.isOnline()) {
                            if (match.isOwnInventory()) {
                                // Preserve any XP gained during the duel when using own inventory
                                info.restoreWithoutExperience(loser);
                            } else {
                                // Restore saved experience for non-own-inventory matches
                                info.restore(loser);
                            }
                        }
                    });
                });
            } else {
                teleport.tryTeleport(loser, playerManager.getLobby());
            }
        } else if (info != null) {
            // If player is dead, their items will be handled when they respawn
            final List<ItemStack> items = match.getItems(loser);
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

        final DuelMatch match = arena.startMatch(kit, items, settings, source);
        addPlayers(first, match, arena, kit, arena.getPosition(1));
        addPlayers(second, match, arena, kit, arena.getPosition(2));

        if (config.isCdEnabled()) {
            arena.startCountdown();
        }

        final MatchStartEvent event = new MatchStartEvent(match, players.toArray(new Player[players.size()]));
        Bukkit.getPluginManager().callEvent(event);
        
        if (config.isCheckForPlayersRoutineEnabled()) {
            startCheckForPlayersRoutine(match, arena);
        }

        return true;
    }

    public boolean startMatch(final Player sender, final Player target, final Settings settings, final Map<UUID, List<ItemStack>> items, final Queue source) {
        final Party senderParty = partyManager.get(sender);
        final Party targetParty = partyManager.get(target);

        if (senderParty != null && targetParty != null) {
            if (!settings.getSenderParty().equals(senderParty) || !settings.getTargetParty().equals(targetParty)) {
                lang.sendMessage(Arrays.asList(sender, target), "DUEL.party-start-failure.party-changed");
                return false;
            }

            return startMatch(settings.getSenderParty().getOnlineMembers(), settings.getTargetParty().getOnlineMembers(), settings, items, source);
        } else if (senderParty != null || targetParty != null) {
            lang.sendMessage(Arrays.asList(sender, target), "DUEL.party-start-failure.party-changed");
            return false;
        } else {
            return startMatch(Collections.singleton(sender), Collections.singleton(target), settings, items, source);
        }
    }

    /**
     * Normalizes a world name by removing namespace prefix (e.g., "minecraft:world" -> "world").
     * 
     * @param worldName The world name to normalize
     * @return Normalized world name without namespace
     */
    private String normalizeWorldName(final String worldName) {
        if (worldName == null) {
            return "";
        }
        // Remove namespace prefix if present (e.g., "minecraft:world" -> "world")
        if (worldName.contains(":")) {
            return worldName.substring(worldName.indexOf(":") + 1);
        }
        return worldName;
    }
    
    /**
     * Compares two world names, handling namespaced worlds (e.g., "minecraft:world" vs "world").
     * 
     * @param world1 First world to compare
     * @param world2 Second world to compare
     * @return true if worlds have the same name (ignoring namespace), false otherwise
     */
    private boolean isSameWorld(final World world1, final World world2) {
        if (world1 == null || world2 == null) {
            return false;
        }
        if (world1.equals(world2)) {
            return true; // Same object reference
        }
        
        // Compare by normalized name
        return normalizeWorldName(world1.getName()).equalsIgnoreCase(normalizeWorldName(world2.getName()));
    }

    private void startCheckForPlayersRoutine(final DuelMatch match, final ArenaImpl arena) {
        // Get arena world name from position - independent from chunks
        final Location arenaPosition = arena.getPosition(1);
        if (arenaPosition == null || arenaPosition.getWorld() == null) {
            return; // Can't check if arena has no valid position
        }
        
        // Store world name as string - independent from World objects and chunks
        final String arenaWorldName = normalizeWorldName(arenaPosition.getWorld().getName());
        
        final int startDelaySeconds = config.getCheckForPlayersRoutineStartDelaySeconds();
        final int checkTimeSeconds = config.getCheckForPlayersRoutineTimeSeconds();
        final String action = config.getCheckForPlayersRoutineAction();
        
        // CRITICAL: Synchronized cleanup and setup to prevent race conditions
        synchronized (activeCheckRoutineFlags) {
            // Cancel any existing routine for this arena
            activeCheckRoutineFlags.remove(arena);
            final TaskWrapper existingTask = activeCheckRoutines.remove(arena);
            if (existingTask != null && !existingTask.isCancelled()) {
                existingTask.cancel();
            }
            
            // Mark this arena as having an active routine
            activeCheckRoutineFlags.add(arena);
        }
        
        final int[] checkCount = {0}; // Track how many checks we've done
        
        // Schedule the start of the repeating task after the delay
        // CRITICAL: Always use global scheduler - independent from chunks
        final long delayTicks = startDelaySeconds * 20L;
        final Runnable startRoutineTask = () -> {
            // Double-check match state before starting routine
            final DuelMatch delayedMatch = arena.getMatch();
            if (match.isFinished() || !arena.isUsed() || delayedMatch != match || delayedMatch == null) {
                synchronized (activeCheckRoutineFlags) {
                    activeCheckRoutineFlags.remove(arena);
                }
                return; // Match ended before routine could start
            }
            
            // Schedule repeating task every second (20 ticks) on global scheduler
            final TaskWrapper task = DuelsPlugin.getSchedulerAdapter().runTaskTimer(() -> {
                try {
                    // CRITICAL: Synchronized check to prevent race conditions
                    synchronized (activeCheckRoutineFlags) {
                        if (!activeCheckRoutineFlags.contains(arena)) {
                            return; // Routine was cancelled, stop checking
                        }
                    }
                    
                    // CRITICAL: Check match state FIRST before any operations
                    final DuelMatch currentMatch = arena.getMatch();
                    if (match.isFinished() || !arena.isUsed() || currentMatch != match || currentMatch == null) {
                        synchronized (activeCheckRoutineFlags) {
                            activeCheckRoutineFlags.remove(arena);
                            activeCheckRoutines.remove(arena);
                        }
                        return; // Stop checking
                    }
                    
                    // Check if we've exceeded the time limit
                    if (checkCount[0] >= checkTimeSeconds) {
                        synchronized (activeCheckRoutineFlags) {
                            activeCheckRoutineFlags.remove(arena);
                            activeCheckRoutines.remove(arena);
                        }
                        return; // Time limit reached, stop routine
                    }
                    
                    checkCount[0]++;
                    
                    // Check each player
                    // Create a snapshot to avoid ConcurrentModificationException
                    final List<Player> playersToCheck = new ArrayList<>(match.getAllPlayers());
                    
                    for (final Player player : playersToCheck) {
                        // CRITICAL: Re-check match state before each player
                        final DuelMatch currentMatch2 = arena.getMatch();
                        if (match.isFinished() || !arena.isUsed() || currentMatch2 != match || currentMatch2 == null) {
                            // Match ended during iteration, stop immediately
                            synchronized (activeCheckRoutineFlags) {
                                activeCheckRoutineFlags.remove(arena);
                                activeCheckRoutines.remove(arena);
                            }
                            return;
                        }
                        
                        // Skip offline players
                        if (!player.isOnline()) {
                            continue;
                        }
                        
                        // CRITICAL: Verify player is still in match and not dead
                        if (!arena.has(player) || match.isDead(player) || !match.getAllPlayers().contains(player)) {
                            continue;
                        }
                        
                        // Skip dead players in team matches (spectators)
                        if (match instanceof TeamDuelMatch && match.isDead(player)) {
                            continue;
                        }
                        
                        // Check if player is in correct world using string comparison
                        final World playerWorld = player.getWorld();
                        if (playerWorld == null) {
                            continue; // Player world is null, skip
                        }
                        
                        final String playerWorldName = normalizeWorldName(playerWorld.getName());
                        if (!arenaWorldName.equalsIgnoreCase(playerWorldName)) {
                            // Player is outside arena world
                            if ("hardstop-arena".equalsIgnoreCase(action)) {
                                // Hard stop the arena immediately
                                synchronized (activeCheckRoutineFlags) {
                                    activeCheckRoutineFlags.remove(arena);
                                    final TaskWrapper removed = activeCheckRoutines.remove(arena);
                                    if (removed != null && !removed.isCancelled()) {
                                        removed.cancel();
                                    }
                                }
                                // Use global scheduler for hardResetArena
                                DuelsPlugin.getSchedulerAdapter().runTask(() -> {
                                    hardResetArena(arena);
                                });
                                return; // Stop checking immediately
                            } else if ("kill-outside-player".equalsIgnoreCase(action)) {
                                // Kill the player but continue checking others
                                DuelsPlugin.getSchedulerAdapter().runTask(player, () -> {
                                    // Final safety check before killing
                                    final DuelMatch currentMatch3 = arena.getMatch();
                                    if (player.isOnline() && !match.isFinished() && currentMatch3 == match && 
                                        currentMatch3 != null && arena.has(player) && !match.isDead(player) && 
                                        match.getAllPlayers().contains(player)) {
                                        
                                        // Clear inventory first, then kill
                                        player.getInventory().clear();
                                        player.getInventory().setArmorContents(null);
                                        player.updateInventory();
                                        player.setHealth(0);
                                        
                                        // Check if match ended after kill
                                        DuelsPlugin.getSchedulerAdapter().runTaskLater(() -> {
                                            if (match.isFinished() || !arena.isUsed() || arena.getMatch() != match) {
                                                synchronized (activeCheckRoutineFlags) {
                                                    if (activeCheckRoutineFlags.contains(arena)) {
                                                        activeCheckRoutineFlags.remove(arena);
                                                        final TaskWrapper removed = activeCheckRoutines.remove(arena);
                                                        if (removed != null && !removed.isCancelled()) {
                                                            removed.cancel();
                                                        }
                                                    }
                                                }
                                            }
                                        }, 5L);
                                        
                                        // Stop checking if match ended
                                        if (match.isFinished() || arena.getMatch() != match) {
                                            return;
                                        }
                                    }
                                });
                            }
                        }
                    }
                } catch (Exception ex) {
                    // CRITICAL: Catch exceptions to prevent task from stopping
                    synchronized (activeCheckRoutineFlags) {
                        activeCheckRoutineFlags.remove(arena);
                        activeCheckRoutines.remove(arena);
                    }
                }
            }, 0L, 20L); // Start immediately, repeat every 20 ticks (1 second)
            
            // Store task reference
            if (task != null) {
                activeCheckRoutines.put(arena, task);
            } else {
                // Scheduling failed, clean up
                synchronized (activeCheckRoutineFlags) {
                    activeCheckRoutineFlags.remove(arena);
                }
            }
        };
        
        // Always use global scheduler - independent from chunks
        TaskWrapper delayTask;
        if (delayTicks == 0) {
            delayTask = DuelsPlugin.getSchedulerAdapter().runTask(startRoutineTask);
        } else {
            delayTask = DuelsPlugin.getSchedulerAdapter().runTaskLater(startRoutineTask, delayTicks);
        }
        
        // Handle scheduling failure
        if (delayTask == null) {
            synchronized (activeCheckRoutineFlags) {
                activeCheckRoutineFlags.remove(arena);
            }
        }
    }

    private void addPlayers(final Collection<Player> players, final DuelMatch match, final ArenaImpl arena, final KitImpl kit, final Location location) {
        for (final Player player : players) {
            if (match.getSource() == null) {
                queueManager.remove(player);
            }
            
            // Abort any kit editing session
            com.meteordevelopments.duels.kit.edit.KitEditManager kitEditManagerInstance = com.meteordevelopments.duels.kit.edit.KitEditManager.getInstance();
            if (kitEditManagerInstance != null) {
                kitEditManagerInstance.checkAndAbortIfInQueueOrMatch(player);
            }

            // Schedule player operations on entity-specific scheduler for Folia compatibility
            DuelsPlugin.getSchedulerAdapter().runTask(player, () -> {
                if (player.getAllowFlight()) {
                    player.setFlying(false);
                    player.setAllowFlight(false);
                }
                // Note: closeInventory() is handled by teleport.tryTeleport() to prevent world mismatch
            });
            final boolean dropOwnInv = match.isOwnInventory() && config.isOwnInventoryDropInventoryItems();
            // If using own inventory (regardless of drop setting), do not restore experience to preserve XP changes
            final boolean restoreExperience = !match.isOwnInventory();
            playerManager.create(player, dropOwnInv, restoreExperience);
            
            // FIXED: Schedule kit loading on entity-specific scheduler after teleport completes for Folia compatibility
            if (kit != null) {
                teleport.tryTeleport(player, location, () -> {
                    DuelsPlugin.getSchedulerAdapter().runTask(player, () -> {
                        if (!player.isOnline()) {
                            return;
                        }
                        PlayerUtil.reset(player);
                        
                        // Check for player-specific kit first
                        com.meteordevelopments.duels.kit.edit.KitEditManager kitEditManager = com.meteordevelopments.duels.kit.edit.KitEditManager.getInstance();
                        if (kitEditManager != null && kitEditManager.hasPlayerKit(player, kit.getName())) {
                            // Load player's custom kit
                            kitEditManager.loadPlayerKit(player, kit.getName());
                        } else {
                            // Load default kit
                            kit.equip(player);
                        }
                    });
                });
            } else {
                teleport.tryTeleport(player, location);
            }

            if (config.isStartCommandsEnabled() && !(match.getSource() == null && config.isStartCommandsQueueOnly())) {
                try {
                    for (final String command : config.getStartCommands()) {
                        String processedCommand = command.replace("%player%", player.getName());
                        executeCommandWithDelay(processedCommand);
                    }
                } catch (Exception ex) {
                    Log.warn(this, "Error while running match start commands: " + ex.getMessage());
                }
            }

            if (config.isPlayerExecuteCommandsOnArenaPreStartEnabled() && !(match.getSource() == null && config.isPlayerExecuteCommandsOnArenaPreStartQueueOnly())) {
                try {
                    for (final String command : config.getPlayerExecuteCommandsOnArenaPreStart()) {
                        executePlayerCommandWithDelay(player, command);
                    }
                } catch (Exception ex) {
                    Log.warn(this, "Error while running player execute commands on arena pre-start: " + ex.getMessage());
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

            // Fix dupe bug: Clear cursor item before death to prevent duplication
            final ItemStack cursor = player.getItemOnCursor();
            if (cursor != null && cursor.getType() != Material.AIR) {
                event.getDrops().add(cursor);
                player.setItemOnCursor(null);
            }

            if (mcMMO != null) {
                mcMMO.enableSkills(player);
            }

            final DuelMatch match = arena.getMatch();

            if (match == null) {
                return;
            }

            // Handle ROUNDS3 deaths that bypass EntityDamageEvent (like /kill command)
            if (match.getKit() != null && match.getKit().hasCharacteristic(KitImpl.Characteristic.ROUNDS3)) {
                event.setDeathMessage(null);
                event.getDrops().clear();
                event.setKeepLevel(true);
                event.setDroppedExp(0);
                event.setKeepInventory(true);
                
                // Check if this is a direct death (like /kill) by checking if there was no recent damage event
                // In this case, we need to properly end the match
                final EntityDamageEvent lastDamage = player.getLastDamageCause();
                final boolean directDeath = lastDamage == null || 
                        (System.currentTimeMillis() - lastDamage.getEntity().getTicksLived() * 50L > 1000);
                
                if (directDeath) {
                    // Player died through /kill or similar - award round win to opponent
                    Player winner = match.getAlivePlayers().stream()
                            .filter(p -> !p.equals(player))
                            .findFirst()
                            .orElse(null);
                    
                    if (winner != null) {
                        // Mark as match-ending win for the opponent
                        match.addRoundWin(winner);
                        match.addRoundWin(winner); // Add twice to immediately win
                        match.markAsDead(player);
                        
                        if (config.isMessageOnDeathNoKiller()) {
                            arena.broadcast(plugin.getLang().getMessage("DUEL.on-death.no-killer", 
                                    "name", player.getName()));
                        }
                        
                        // Use delayed task to allow death to process
                        final Location deadLocation = player.getLocation().clone();
                        plugin.doSyncAfter(() -> {
                            handleMatchEnd(match, arena, player, deadLocation, winner);
                        }, 1L);
                    }
                }
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

            final Player killer = player.getKiller();
            if (killer != null) {
                if (config.isMessageOnDeathWithKiller()) {
                    final double health = Math.ceil(killer.getHealth()) * 0.5;
                    arena.broadcast(lang.getMessage("DUEL.on-death.with-killer", "name", player.getName(), "killer", killer.getName(), "health", health));
                }
            } else {
                if (config.isMessageOnDeathNoKiller()) {
                    arena.broadcast(lang.getMessage("DUEL.on-death.no-killer", "name", player.getName()));
                }
            }

            final int prevSize = match.size();
            
            // Handle team-based elimination
            if (match instanceof TeamDuelMatch teamMatch) {

                // Mark player as dead in the match (this updates team alive count)
                arena.remove(player);
                
                // Prevent death screen and respawn for team matches
                event.setKeepInventory(true);
                event.setDroppedExp(0);
                event.getDrops().clear();
                event.setDeathMessage(null);
                
                // Immediately set to spectator mode to prevent death screen - schedule on entity-specific scheduler for Folia compatibility
                DuelsPlugin.getSchedulerAdapter().runTask(player, () -> {
                    player.setGameMode(GameMode.SPECTATOR);
                });
                
                // Check if any team is completely eliminated
                TeamDuelMatch.Team winningTeam = teamMatch.getWinningTeam();
                if (winningTeam != null && teamMatch.size() == 1) {
                    final Location deadLocation = player.getEyeLocation().clone();
                    handleTeamMatchEnd(teamMatch, arena, deadLocation, winningTeam);
                    return;
                }
                
                // If not all teams eliminated, continue the match with player spectating in arena
                return;
            }
            
            arena.remove(player);

            if (prevSize < 2 || match.size() >= prevSize) {
                return;
            }

            // CRITICAL: Check if match still exists before handling match end (might have been cleared)
            final DuelMatch currentMatch = arena.getMatch();
            if (currentMatch == null || currentMatch != match) {
                // Match was already ended or cleared
                return;
            }

            // CRITICAL: Safety check - ensure there's at least one alive player (winner)
            // Edge case: Race condition where both players die simultaneously or match state changes
            final Set<Player> alivePlayers = match.getAlivePlayers();
            if (alivePlayers.isEmpty()) {
                // Edge case: No alive players (tie scenario)
                // This should be handled by the tie logic in handleMatchEnd, but add safety check
                Log.warn(DuelManager.this, "No alive players found when ending match in arena " + arena.getName() + " - treating as tie");
                final Location deadLocation = player.getEyeLocation().clone();
                handleMatchEnd(match, arena, player, deadLocation, null); // null winner = tie
                return;
            }

            final Location deadLocation = player.getEyeLocation().clone();
            final Player winner = alivePlayers.iterator().next();
            handleMatchEnd(match, arena, player, deadLocation, winner);
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
            final Player player = event.getPlayer();
            final ArenaImpl arena = arenaManager.get(player);

            if (arena == null) {
                return;
            }

            // Handle endgame movement restriction
            if (!config.isDisableMovementInEndgame()){
                return;
            }

            if (!arena.isEndGame()) {
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

            final ArenaImpl arena = arenaManager.get(player);
            final DuelMatch match = arena != null ? arena.getMatch() : null;
            
            if (match == null) {
                return;
            }

            // CRITICAL FIX: Handle player quit during countdown - treat as loss for quitting player, win for remaining player
            // Check if countdown is still active (same logic as GiveupCommand)
            boolean countdownActive = false;
            if (config.isCdEnabled()) {
                // Countdown messages are shown every second (20 ticks)
                // Countdown duration = (number of messages - 1) * 1000ms
                // The last message ("Now in a match") is shown AFTER countdown completes
                // Add 500ms buffer to account for timing differences
                final int countdownDuration = (config.getCdDuelMessages().size() - 1) * 1000 + 500;
                if (match.getDurationInMillis() < countdownDuration) {
                    countdownActive = true;
                }
            }
            
            if (countdownActive) {
                // Countdown is active - cancel countdown and end match with quitting player as loser
                final Player quittingPlayer = player;
                
                // For own-inventory duels with drop-inventory-items: false, preserve PlayerInfo for restoration
                final PlayerInfo info = playerManager.get(quittingPlayer);
                final boolean preserveInventory = match.isOwnInventory() && !config.isOwnInventoryDropInventoryItems();
                
                if (preserveInventory && info != null) {
                    // Update PlayerInfo with current inventory before clearing
                    info.updateInventory(quittingPlayer);
                }
                
                // Cancel countdown
                arena.setCountdown(null);
                
                // Apply config-based actions for player quit
                if (config.isOnQuitKillPlayer()) {
                    // Kill the player - this will trigger PlayerDeathEvent which will handle match ending automatically
                    try {
                        quittingPlayer.setHealth(0);
                        if (config.isOnQuitClearInventory()) {
                            quittingPlayer.getInventory().clear();
                            quittingPlayer.getInventory().setArmorContents(null);
                            quittingPlayer.updateInventory();
                        }
                    } catch (Exception ex) {
                        // Fallback: If direct call fails, schedule it immediately (shouldn't happen but safety net)
                        DuelsPlugin.getSchedulerAdapter().runTask(quittingPlayer, () -> {
                            if (quittingPlayer.isOnline()) {
                                quittingPlayer.setHealth(0);
                                if (config.isOnQuitClearInventory()) {
                                    quittingPlayer.getInventory().clear();
                                    quittingPlayer.getInventory().setArmorContents(null);
                                    quittingPlayer.updateInventory();
                                }
                            }
                        });
                    }
                    
                    // Mark quitting player as dead in match (PlayerDeathEvent will handle the rest)
                    match.markAsDead(quittingPlayer);
                    
                    // DON'T call handleMatchEnd here - let PlayerDeathEvent handle it automatically
                    // This prevents duplicate execution of pre-end commands and match ending
                } else {
                    // Don't kill player - end match directly
                    // Mark quitting player as dead in match (for match ending logic)
                    match.markAsDead(quittingPlayer);
                    
                    // Clear inventory if configured
                    if (config.isOnQuitClearInventory()) {
                        try {
                            quittingPlayer.getInventory().clear();
                            quittingPlayer.getInventory().setArmorContents(null);
                            quittingPlayer.updateInventory();
                        } catch (Exception ex) {
                            DuelsPlugin.getSchedulerAdapter().runTask(quittingPlayer, () -> {
                                if (quittingPlayer.isOnline()) {
                                    quittingPlayer.getInventory().clear();
                                    quittingPlayer.getInventory().setArmorContents(null);
                                    quittingPlayer.updateInventory();
                                }
                            });
                        }
                    }
                    
                    // End match directly since we're not killing the player
                    final Set<Player> alivePlayers = match.getAlivePlayers();
                    if (!alivePlayers.isEmpty()) {
                        final Player winner = alivePlayers.iterator().next();
                        final Location deadLocation = quittingPlayer.getLocation().clone();
                        handleMatchEnd(match, arena, quittingPlayer, deadLocation, winner);
                    } else {
                        // No alive players - treat as tie
                        final Location deadLocation = quittingPlayer.getLocation().clone();
                        handleMatchEnd(match, arena, quittingPlayer, deadLocation, null);
                    }
                }
                
                // CRITICAL FIX: NEVER remove PlayerInfo when player quits during match
                // PlayerInfo must be preserved so inventory can be restored when:
                // 1. Player rejoins (handled by PlayerInfoManager)
                // 2. Match ends (handled by handleMatchEnd)
                // 3. Server shuts down (handled by handleUnload)
                // PlayerInfo will be removed only after successful restoration in handleWin/handleLoss/handleTie
                // DO NOT remove PlayerInfo here - this was causing inventory loss!
                
                return; // Exit early - countdown quit handled
            }

            // Normal quit handling (countdown complete, match in progress)
            // CRITICAL: Check if player is already dead or match is already finished
            // This prevents duplicate processing if PlayerQuitEvent fires after PlayerDeathEvent
            // Edge case: Player quits after being killed - match ending already in progress
            if (match.isDead(player) || match.isFinished()) {
                // Player already dead - check if we're in end game phase and match needs to be completed
                if (!match.isFinished() && arena.isEndGame()) {
                    // Match is in end game phase (winner determined, waiting for teleport delay)
                    // The scheduled task in handleMatchEnd() was scheduled on the loser entity
                    // If loser quits, the task might not execute on Folia - ensure match ends properly
                    final Set<Player> alivePlayers = match.getAlivePlayers();
                    if (!alivePlayers.isEmpty()) {
                        final Player winner = alivePlayers.iterator().next();
                        final UUID loserUuid = player.getUniqueId();
                        final String loserName = player.getName();
                        
                        // Schedule match end using global scheduler to ensure it completes
                        // Use the same delay as configured, but on global scheduler instead of entity scheduler
                        DuelsPlugin.getSchedulerAdapter().runTaskLater(() -> {
                            // Double-check match is still active and not finished
                            if (arena.getMatch() != null && arena.getMatch() == match && !match.isFinished()) {
                                // Get winner again (in case state changed)
                                final Set<Player> currentAlivePlayers = match.getAlivePlayers();
                                if (!currentAlivePlayers.isEmpty()) {
                                    final Player currentWinner = currentAlivePlayers.iterator().next();
                                    
                                    // Execute the end match logic directly (similar to lines 198-225)
                                    for (Player alivePlayer : currentAlivePlayers) {
                                        if (alivePlayer.isOnline()) {
                                            handleWin(alivePlayer, player, arena, match);
                                        }
                                    }

                                    if (config.isEndCommandsEnabled() && !(!match.isFromQueue() && config.isEndCommandsQueueOnly())) {
                                        try {
                                            for (final String command : config.getEndCommands()) {
                                                String processedCommand = command
                                                        .replace("%winner%", currentWinner.getName()).replace("%loser%", loserName)
                                                        .replace("%kit%", match.getKit() != null ? match.getKit().getName() : "").replace("%arena%", arena.getName())
                                                        .replace("%bet_amount%", String.valueOf(match.getBet()));
                                                
                                                executeCommandWithDelay(processedCommand);
                                            }
                                        } catch (Exception ex) {
                                            Log.warn(DuelManager.this, "Error while running match end commands: " + ex.getMessage());
                                        }
                                    }

                                    // End the match
                                    if (arena.getMatch() != null && arena.getMatch() == match) {
                                        arena.endMatch(currentWinner.getUniqueId(), loserUuid, Reason.OPPONENT_DEFEAT);
                                    }
                                }
                            }
                        }, config.getTeleportDelay() * 20L);
                    } else {
                        // No alive players - treat as tie
                        DuelsPlugin.getSchedulerAdapter().runTaskLater(() -> {
                            if (arena.getMatch() != null && arena.getMatch() == match && !match.isFinished()) {
                                final Location deadLocation = player.getLocation().clone();
                                handleMatchEnd(match, arena, player, deadLocation, null);
                            }
                        }, config.getTeleportDelay() * 20L);
                    }
                }
                // Match is finished or will be finished by the scheduled task above
                return;
            }
            
            // For own-inventory duels with drop-inventory-items: false, preserve PlayerInfo for restoration
            final PlayerInfo info = playerManager.get(player);
            final boolean preserveInventory = match.isOwnInventory() && !config.isOwnInventoryDropInventoryItems();
            
            if (preserveInventory && info != null) {
                // Update PlayerInfo with current inventory before clearing
                info.updateInventory(player);
            }
            
            // CRITICAL FIX: Always preserve PlayerInfo when player quits during match
            // This ensures inventory can be restored when player rejoins or match ends
            // Only update inventory if we're preserving it, otherwise PlayerInfo already has the original inventory saved
            if (info != null && !preserveInventory) {
                // For non-own-inventory matches, PlayerInfo already has the original inventory saved
                // Just ensure we don't lose it - don't remove PlayerInfo here
            }
            
            // Apply config-based actions for player quit
            if (config.isOnQuitKillPlayer()) {
                // Kill the player - this will trigger PlayerDeathEvent which will handle match ending automatically
                try {
                    player.setHealth(0);
                    if (config.isOnQuitClearInventory()) {
                        player.getInventory().clear();
                        player.getInventory().setArmorContents(null);
                        player.updateInventory();
                    }
                } catch (Exception ex) {
                    // Fallback: If direct call fails, schedule it immediately (shouldn't happen but safety net)
                    DuelsPlugin.getSchedulerAdapter().runTask(player, () -> {
                        if (player.isOnline()) {
                            player.setHealth(0);
                            if (config.isOnQuitClearInventory()) {
                                player.getInventory().clear();
                                player.getInventory().setArmorContents(null);
                                player.updateInventory();
                            }
                        }
                    });
                }
                
                // Mark player as dead in match (so PlayerDeathEvent knows they're dead)
                match.markAsDead(player);
            } else {
                // Don't kill player - end match directly
                // Clear inventory if configured
                if (config.isOnQuitClearInventory()) {
                    try {
                        player.getInventory().clear();
                        player.getInventory().setArmorContents(null);
                        player.updateInventory();
                    } catch (Exception ex) {
                        DuelsPlugin.getSchedulerAdapter().runTask(player, () -> {
                            if (player.isOnline()) {
                                player.getInventory().clear();
                                player.getInventory().setArmorContents(null);
                                player.updateInventory();
                            }
                        });
                    }
                }
                
                // Mark player as dead in match (for match ending logic)
                match.markAsDead(player);
                
                // End match directly since we're not killing the player
                final Set<Player> alivePlayers = match.getAlivePlayers();
                if (!alivePlayers.isEmpty()) {
                    final Player winner = alivePlayers.iterator().next();
                    final Location deadLocation = player.getLocation().clone();
                    handleMatchEnd(match, arena, player, deadLocation, winner);
                } else {
                    // No alive players - treat as tie
                    final Location deadLocation = player.getLocation().clone();
                    handleMatchEnd(match, arena, player, deadLocation, null);
                }
                
                // Match is finished or will be finished by handleMatchEnd
                return;
            }
            
            // CRITICAL FIX: NEVER remove PlayerInfo when player quits during match
            // PlayerInfo must be preserved so inventory can be restored when:
            // 1. Player rejoins (handled by PlayerInfoManager)
            // 2. Match ends (handled by handleMatchEnd)
            // 3. Server shuts down (handled by handleUnload)
            // PlayerInfo will be removed only after successful restoration in handleWin/handleLoss/handleTie
            // DO NOT remove PlayerInfo here - this was causing inventory loss!
            
            // DON'T remove from arena or end match here - let PlayerDeathEvent handle it automatically
            // PlayerDeathEvent will call arena.remove() and check match.size() to end the match
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

        @EventHandler(priority = EventPriority.HIGHEST)
        public void on(final PlayerCommandPreprocessEvent event) {
            final Player player = event.getPlayer();
            
            if (!arenaManager.isInMatch(player)) {
                return;
            }
            
            final String command = event.getMessage().substring(1).split(" ")[0].toLowerCase();

            // Check if command should be blocked
            if (config.isBlockAllCommands() ? config.getWhitelistedCommands().contains(command) : !config.getBlacklistedCommands().contains(command)) {
                return;
            }

            // Cancel the command event - run at HIGHEST priority to ensure we cancel even if other plugins processed it
            event.setCancelled(true);
            lang.sendMessage(player, "DUEL.prevent.command", "command", event.getMessage());
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void on(final PlayerTeleportEvent event) {
            final Player player = event.getPlayer();
            
            // Always check if player is in match, regardless of cancellation state
            // This ensures we catch teleports even if other plugins tried to process them
            if (!arenaManager.isInMatch(player)) {
                return;
            }
            
            final Location to = event.getTo();
            if (to == null || to.getWorld() == null) {
                return;
            }

            if (!config.isLimitTeleportEnabled()
                    || event.getCause() == TeleportCause.ENDER_PEARL
                    || event.getCause() == TeleportCause.SPECTATE) {
                return;
            }

            final Location from = event.getFrom();
            if (from == null || from.getWorld() == null) {
                return;
            }

            // CRITICAL: Check if player is trying to teleport to a different world
            // This catches cross-world teleports from plugins that bypass command blocking (e.g., clicking chat messages)
            // Use world name comparison to handle namespaced worlds correctly (e.g., "minecraft:world" vs "world")
            if (!isSameWorld(from.getWorld(), to.getWorld())) {
                // Different world - always cancel cross-world teleports during duels
                event.setCancelled(true);
                lang.sendMessage(player, "DUEL.prevent.teleportation");
                return;
            }

            // Same world - allow small distance teleports within the same world
            if (from.distance(to) <= config.getDistanceAllowed()) {
                return;
            }

            // Cancel teleportation - run at HIGHEST priority to ensure we cancel even if other plugins processed it
            // This prevents plugins from bypassing command blocking by directly teleporting players
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
    
    /**
     * Executes a command with optional delay support.
     * Parses {delay:x} placeholder where x is delay in milliseconds.
     * 
     * @param command The command string to execute, potentially containing {delay:x}
     */
    private void executeCommandWithDelay(String command) {
        Matcher matcher = DELAY_PATTERN.matcher(command);
        
        if (matcher.find()) {
            // Extract delay value in milliseconds
            long delayMs = Long.parseLong(matcher.group(1));
            // Remove the {delay:x} placeholder from the command
            String cleanCommand = matcher.replaceAll("").trim();
            
            // Convert milliseconds to ticks (1 tick = 50ms)
            long delayTicks = delayMs / 50;
            
            // FIXED: Use global region scheduler for command dispatch in Folia
            DuelsPlugin.getSchedulerAdapter().runTaskLater(() -> {
                try {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cleanCommand);
                } catch (Exception ex) {
                    Log.warn(this, "Error executing delayed command: " + ex.getMessage());
                }
            }, delayTicks);
        } else {
            // FIXED: Execute immediately on global region scheduler for Folia compatibility
            DuelsPlugin.getSchedulerAdapter().runTask(() -> {
                try {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                } catch (Exception ex) {
                    Log.warn(this, "Error executing command: " + ex.getMessage());
                }
            });
        }
    }

    private void executePlayerCommandWithDelay(Player player, String command) {
        Matcher matcher = DELAY_PATTERN.matcher(command);
        
        if (matcher.find()) {
            // Extract delay value in milliseconds
            long delayMs = Long.parseLong(matcher.group(1));
            // Remove the {delay:x} placeholder from the command
            String cleanCommand = matcher.replaceAll("").trim();
            
            // Convert milliseconds to ticks (1 tick = 50ms)
            long delayTicks = delayMs / 50;
            
            // Use entity-specific scheduler for Folia compatibility
            DuelsPlugin.getSchedulerAdapter().runTaskLater(player, () -> {
                if (!player.isOnline()) {
                    return;
                }
                try {
                    player.performCommand(cleanCommand);
                } catch (Exception ex) {
                    Log.warn(this, "Error executing delayed player command: " + ex.getMessage());
                }
            }, delayTicks);
        } else {
            // Execute immediately on entity-specific scheduler for Folia compatibility
            DuelsPlugin.getSchedulerAdapter().runTask(player, () -> {
                if (!player.isOnline()) {
                    return;
                }
                try {
                    player.performCommand(command);
                } catch (Exception ex) {
                    Log.warn(this, "Error executing player command: " + ex.getMessage());
                }
            });
        }
    }
}
