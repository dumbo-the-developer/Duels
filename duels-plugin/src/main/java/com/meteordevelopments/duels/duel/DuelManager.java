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
import space.arim.morepaperlib.scheduling.ScheduledTask;

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
            
            // FIXED: Check if loser is online to use appropriate scheduler for Folia compatibility
            final boolean loserIsOnline = loser.isOnline();
            if (loserIsOnline) {
                // Normal flow: loser is still online (normal death)
                DuelsPlugin.getMorePaperLib().scheduling().entitySpecificScheduler(loser).runDelayed(() -> {
                    // Handle the loser (remove from arena and restore state)
                    // This is especially important for ROUNDS3 where the loser never actually dies
                    if (match.getKit() != null && match.getKit().hasCharacteristic(KitImpl.Characteristic.ROUNDS3)) {
                        handleLoss(loser, arena, match);
                    }
                    
                    for (Player alivePlayer : winners) {
                        handleWin(alivePlayer, loser, arena, match);
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

                    // FIXED: Use global scheduler for arena.endMatch() to avoid Folia region conflicts
                    // This prevents errors when entities are in different regions or already removed by other plugins
                    DuelsPlugin.getMorePaperLib().scheduling().globalRegionalScheduler().run(() -> {
                        arena.endMatch(winner.getUniqueId(), loser.getUniqueId(), Reason.OPPONENT_DEFEAT);
                    });
                }, null, config.getTeleportDelay() * 20L);
            } else {
                // FIXED: Loser has quit - use global scheduler instead to prevent winners from getting stuck
                DuelsPlugin.getMorePaperLib().scheduling().globalRegionalScheduler().runDelayed(() -> {
                    // Don't need to handle the loser since they're offline
                    
                    for (Player alivePlayer : winners) {
                        if (alivePlayer.isOnline()) {  // Safety check
                            handleWin(alivePlayer, loser, arena, match);
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

                    arena.endMatch(winner.getUniqueId(), loser.getUniqueId(), Reason.OPPONENT_DEFEAT);
                }, config.getTeleportDelay() * 20L);
            }
        }, 1L);
    }

    public void handleTeamMatchEnd(TeamDuelMatch match, ArenaImpl arena, Location deadLocation, TeamDuelMatch.Team winningTeam) {
        DuelsPlugin.getMorePaperLib().scheduling().regionSpecificScheduler(deadLocation).runDelayed(() -> {
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

            final Set<Player> winners = match.getWinningTeamPlayers();
            final Set<Player> losers = match.getLosingTeamPlayers();
            
            // Restore gamemode for all players immediately using individual schedulers
            final Set<Player> allPlayers = match.getAllPlayers();
            for (Player player : allPlayers) {
                DuelsPlugin.getMorePaperLib().scheduling().entitySpecificScheduler(player).run(() -> {
                    player.setGameMode(GameMode.SURVIVAL);
                    player.setAllowFlight(false);
                    player.setFlying(false);
                }, null);
            }
            
            // Create inventory snapshots for display
            allPlayers.forEach(p -> inventoryManager.create(p, false));
            userDataManager.handleTeamMatchEnd(match, winners, losers);
            plugin.doSyncAfter(() -> inventoryManager.handleMatchEnd(match), 1L);
            
            // Schedule teleportation and restoration for all players after delay
            DuelsPlugin.getMorePaperLib().scheduling().regionSpecificScheduler(deadLocation).runDelayed(() -> {
                // Handle losers (including dead spectators)
                for (Player loser : losers) {
                    if (mcMMO != null) {
                        mcMMO.enableSkills(loser);
                    }
                    
                    final PlayerInfo loserInfo = playerManager.get(loser);
                    if (loserInfo != null && !loser.isDead()) {
                        playerManager.remove(loser);
                        
                        // Schedule player operations on entity-specific scheduler for Folia compatibility
                        DuelsPlugin.getMorePaperLib().scheduling().entitySpecificScheduler(loser).run(() -> {
                            if (!(match.isOwnInventory() && config.isOwnInventoryDropInventoryItems())) {
                                PlayerUtil.reset(loser);
                            }
                            
                            // FIXED: Wait for teleport to complete before restoring to prevent dimension change errors
                            teleport.tryTeleport(loser, loserInfo.getLocation(), () -> {
                                DuelsPlugin.getMorePaperLib().scheduling().entitySpecificScheduler(loser).run(() -> {
                                    if (loser.isOnline()) {
                                        loserInfo.restore(loser);
                                    }
                                }, null);
                            });
                        }, null);
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
                DuelsPlugin.getMorePaperLib().scheduling().globalRegionalScheduler().run(() -> {
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
                    DuelsPlugin.getMorePaperLib().scheduling().entitySpecificScheduler(winner).run(() -> {
                        if (winner.isOnline()) {
                            if (match.isOwnInventory()) {
                                // Preserve any XP gained during the duel when using own inventory
                                info.restoreWithoutExperience(winner);
                            } else {
                                // Restore saved experience for non-own-inventory matches
                                info.restore(winner);
                            }
                        }
                    }, null);
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
            this.durationCheckTask = plugin.doSyncRepeat(() -> {
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
                            DuelsPlugin.getMorePaperLib().scheduling().entitySpecificScheduler(player).run(() -> {
                                try {
                                    // Double-check player is still online inside scheduler
                                    if (!player.isOnline()) {
                                        return;
                                    }
                                    
                                    handleTie(player, arena, match, alive);
                                    lang.sendMessage(player, "DUEL.on-end.tie");
                                } catch (Exception ex) {
                                    Log.warn(this, "Error handling tie for player " + player.getName() + ": " + ex.getMessage());
                                }
                            }, null);
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
                    DuelsPlugin.getMorePaperLib().scheduling().globalRegionalScheduler().runDelayed(() -> {
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
                    if (match.isDead(player)) continue;

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
                // FIXED: Wait for teleport to complete before restoring to prevent dimension change errors
                teleport.tryTeleport(player, info.getLocation(), () -> {
                    DuelsPlugin.getMorePaperLib().scheduling().entitySpecificScheduler(player).run(() -> {
                        if (player.isOnline()) {
                            info.restore(player);
                        }
                    }, null);
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
                DuelsPlugin.getMorePaperLib().scheduling().entitySpecificScheduler(player).run(() -> {
                    try {
                        // Double-check player is still online inside scheduler
                        if (!player.isOnline()) {
                            return;
                        }
                        
                        handleTie(player, arena, match, alive);
                        lang.sendMessage(player, "DUEL.on-end.tie");
                    } catch (Exception ex) {
                        Log.warn(this, "Error handling tie for player " + player.getName() + " in hardreset: " + ex.getMessage());
                    }
                }, null);
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
        
        // Always end match on global scheduler after processing all players
        DuelsPlugin.getMorePaperLib().scheduling().globalRegionalScheduler().runDelayed(() -> {
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
                    DuelsPlugin.getMorePaperLib().scheduling().entitySpecificScheduler(winner).run(() -> {
                        if (winner.isOnline()) {
                            if (match.isOwnInventory()) {
                                // Preserve any XP gained during the duel when using own inventory
                                info.restoreWithoutExperience(winner);
                            } else {
                                // Restore saved experience for non-own-inventory matches
                                info.restore(winner);
                            }
                        }
                    }, null);
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
                    DuelsPlugin.getMorePaperLib().scheduling().entitySpecificScheduler(loser).run(() -> {
                        if (loser.isOnline()) {
                            if (match.isOwnInventory()) {
                                // Preserve any XP gained during the duel when using own inventory
                                info.restoreWithoutExperience(loser);
                            } else {
                                // Restore saved experience for non-own-inventory matches
                                info.restore(loser);
                            }
                        }
                    }, null);
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
            DuelsPlugin.getMorePaperLib().scheduling().entitySpecificScheduler(player).run(() -> {
                if (player.getAllowFlight()) {
                    player.setFlying(false);
                    player.setAllowFlight(false);
                }
                // Note: closeInventory() is handled by teleport.tryTeleport() to prevent world mismatch
            }, null);
            final boolean dropOwnInv = match.isOwnInventory() && config.isOwnInventoryDropInventoryItems();
            // If using own inventory (regardless of drop setting), do not restore experience to preserve XP changes
            final boolean restoreExperience = !match.isOwnInventory();
            playerManager.create(player, dropOwnInv, restoreExperience);
            
            // FIXED: Schedule kit loading on entity-specific scheduler after teleport completes for Folia compatibility
            if (kit != null) {
                teleport.tryTeleport(player, location, () -> {
                    DuelsPlugin.getMorePaperLib().scheduling().entitySpecificScheduler(player).run(() -> {
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
                    }, null);
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
                        
                        arena.broadcast(plugin.getLang().getMessage("DUEL.on-death.no-killer", 
                                "name", player.getName()));
                        
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
                DuelsPlugin.getMorePaperLib().scheduling().entitySpecificScheduler(player).run(() -> {
                    player.setGameMode(GameMode.SPECTATOR);
                }, null);
                
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
                
                // Cancel countdown
                arena.setCountdown(null);
                
                // Mark quitting player as dead in match
                match.markAsDead(quittingPlayer);
                
                // Remove quitting player from arena
                arena.remove(quittingPlayer);
                
                // Remove PlayerInfo for quitting player and store items (prevents restore on rejoin)
                final PlayerInfo quitPlayerInfo = playerManager.remove(quittingPlayer);
                final List<ItemStack> quitPlayerItems = match.getItems(quittingPlayer);
                if (quitPlayerInfo != null && quitPlayerItems != null) {
                    quitPlayerInfo.getExtra().addAll(quitPlayerItems);
                }
                
                // Get the remaining player (winner)
                final Set<Player> alivePlayers = match.getAlivePlayers();
                if (alivePlayers.isEmpty()) {
                    // No alive players - shouldn't happen but handle it
                    DuelsPlugin.getMorePaperLib().scheduling().globalRegionalScheduler().runDelayed(() -> {
                        try {
                            arena.endMatch(null, null, Reason.TIE);
                        } catch (Exception ex) {
                            Log.warn(DuelManager.this, "Error ending match in arena " + arena.getName() + " after countdown quit: " + ex.getMessage());
                        }
                    }, 3L);
                    return;
                }
                
                final Player winner = alivePlayers.iterator().next();
                final Location deadLocation = quittingPlayer.getLocation() != null ? quittingPlayer.getLocation() : (winner.getLocation() != null ? winner.getLocation() : arena.getPosition(1));
                
                // End match with winner and loser (quitting player is offline, so handleMatchEnd will handle it correctly)
                DuelsPlugin.getMorePaperLib().scheduling().globalRegionalScheduler().runDelayed(() -> {
                    try {
                        handleMatchEnd(match, arena, quittingPlayer, deadLocation, winner);
                    } catch (Exception ex) {
                        Log.warn(DuelManager.this, "Error handling match end after countdown quit: " + ex.getMessage());
                    }
                }, 1L);
                
                return; // Exit early - countdown quit handled
            }

            // Normal quit handling (countdown complete, match in progress)
            // Schedule player operations on entity-specific scheduler for Folia compatibility
            DuelsPlugin.getMorePaperLib().scheduling().entitySpecificScheduler(player).run(() -> {
                player.setHealth(0);
                player.getInventory().clear();
                player.getInventory().setArmorContents(null);
                player.updateInventory();
            }, null);

            final PlayerInfo info = playerManager.get(player);
            // DON'T restore here - let them stay dead and respawn normally
            // The PlayerInfo is kept in the manager for when they respawn
            // Removed: info.restore(player);
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
            DuelsPlugin.getMorePaperLib().scheduling().globalRegionalScheduler().runDelayed(() -> {
                try {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cleanCommand);
                } catch (Exception ex) {
                    Log.warn(this, "Error executing delayed command: " + ex.getMessage());
                }
            }, delayTicks);
        } else {
            // FIXED: Execute immediately on global region scheduler for Folia compatibility
            DuelsPlugin.getMorePaperLib().scheduling().globalRegionalScheduler().run(() -> {
                try {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                } catch (Exception ex) {
                    Log.warn(this, "Error executing command: " + ex.getMessage());
                }
            });
        }
    }
}
