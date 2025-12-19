package com.meteordevelopments.duels.command.commands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.arena.ArenaImpl;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.config.CommandsConfig.CommandSettings;
import com.meteordevelopments.duels.inventories.InventoryManager;
import com.meteordevelopments.duels.match.DuelMatch;
import com.meteordevelopments.duels.match.team.TeamDuelMatch;
import com.meteordevelopments.duels.player.PlayerInfo;
import com.meteordevelopments.duels.util.PlayerUtil;
import com.meteordevelopments.duels.util.inventory.InventoryUtil;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

public class LeaveCommand extends BaseCommand {

    public LeaveCommand(final DuelsPlugin plugin, final CommandSettings settings) {
        super(plugin, Objects.requireNonNull(settings, "settings").getName(), Permissions.LEAVE, true, settings.getAliasArray());
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;

        // Check if player is in a match
        final ArenaImpl arena = arenaManager.get(player);
        
        if (arena == null || !arena.isUsed()) {
            lang.sendMessage(sender, "ERROR.leave.not-in-match");
            return;
        }

        final DuelMatch match = arena.getMatch();
        
        if (match == null) {
            lang.sendMessage(sender, "ERROR.leave.not-in-match");
            return;
        }

        // Notify player before forfeiting
        lang.sendMessage(sender, "COMMAND.leave.success");
        
        // Handle inventory drops if own inventory mode with dropping enabled
        if (match.isOwnInventory() && config.isOwnInventoryDropInventoryItems()) {
            // Drop inventory items at player's location
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                }
            }
            for (ItemStack item : player.getInventory().getArmorContents()) {
                if (item != null) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                }
            }
        }
        
        // Create inventory snapshot before changes
        inventoryManager.create(player, false);
        
        // Handle team/party matches
        if (match instanceof TeamDuelMatch teamMatch) {
            // Check if removing this player will eliminate their team BEFORE marking as dead
            final int prevSize = teamMatch.size();
            
            // Mark player as dead in the team match
            arena.remove(player);
            
            // Check if this caused a team to be completely eliminated
            final int newSize = teamMatch.size();
            TeamDuelMatch.Team winningTeam = teamMatch.getWinningTeam();
            
            if (winningTeam != null && newSize < prevSize && teamMatch.size() == 1) {
                // Match is over, trigger team match end
                plugin.doSyncAfter(() -> {
                    duelManager.handleTeamMatchEnd(teamMatch, arena, player.getLocation(), winningTeam);
                }, 1L);
            } else {
                // Match continues, player becomes spectator
                player.setGameMode(GameMode.SPECTATOR);
                player.setAllowFlight(true);
                player.setFlying(true);
                
                // Clear inventory since they're spectating (unless it was dropped)
                if (!match.isOwnInventory() || !config.isOwnInventoryDropInventoryItems()) {
                    PlayerUtil.reset(player);
                }
            }
        } else {
            // Regular 1v1 or party match
            final Player opponent = match.getAlivePlayers().stream()
                    .filter(p -> !p.equals(player))
                    .findFirst()
                    .orElse(null);
            
            // Mark player as dead
            arena.remove(player);
            
            if (opponent != null) {
                // Broadcast forfeit message
                if (config.isSendDeathMessages()) {
                    arena.broadcast(lang.getMessage("DUEL.on-death.no-killer", "name", player.getName()));
                }
                
                // Trigger match end with opponent as winner
                plugin.doSyncAfter(() -> {
                    duelManager.handleMatchEnd(match, arena, player, player.getLocation(), opponent);
                }, 1L);
            } else {
                // No opponent found (shouldn't happen), just teleport player back
                final PlayerInfo info = playerManager.get(player);
                if (info != null) {
                    playerManager.remove(player);
                    PlayerUtil.reset(player);
                    teleport.tryTeleport(player, info.getLocation());
                    info.restore(player);
                } else {
                    teleport.tryTeleport(player, playerManager.getLobby());
                }
            }
        }
    }
}
