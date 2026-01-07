package com.meteordevelopments.duels.command.commands.duel.subcommands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.arena.ArenaImpl;
import com.meteordevelopments.duels.command.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveupCommand extends BaseCommand {

    public GiveupCommand(final DuelsPlugin plugin) {
        super(plugin, "giveup", "giveup", "Surrender and forfeit the current duel.", Permissions.GIVEUP, 1, true, "surrender", "forfeit");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;
        
        // Check if player is in a match
        if (!arenaManager.isInMatch(player)) {
            lang.sendMessage(player, "ERROR.duel.not-in-match", "name", player.getName());
            return;
        }
        
        final ArenaImpl arena = arenaManager.get(player);
        final var match = arena.getMatch();
        
        // Check if countdown is complete (match has started)
        // Countdown runs for the length of countdown messages (typically 5-6 seconds)
        // We check if countdown is enabled and if match duration is less than countdown duration
        if (match != null && config.isCdEnabled()) {
            // Countdown messages are shown every second (20 ticks)
            // Countdown duration = (number of messages - 1) * 1000ms
            // The last message ("Now in a match") is shown AFTER countdown completes
            // Add 500ms buffer to account for timing differences
            final int countdownDuration = (config.getCdDuelMessages().size() - 1) * 1000 + 500;
            if (match.getDurationInMillis() < countdownDuration) {
                lang.sendMessage(player, "DUEL.giveup.countdown-active");
                return;
            }
        }
        
        // Check if match is not in endgame phase
        if (arena.isEndGame()) {
            lang.sendMessage(player, "DUEL.giveup.endgame-phase");
            return;
        }
        
        // Kill the player - this will trigger PlayerDeathEvent which handles everything
        // Use entity scheduler to ensure thread safety
        DuelsPlugin.getMorePaperLib().scheduling().entitySpecificScheduler(player).run(() -> {
            if (player.isOnline() && arenaManager.isInMatch(player)) {
                player.setHealth(0);
            }
        }, null);
    }
}

