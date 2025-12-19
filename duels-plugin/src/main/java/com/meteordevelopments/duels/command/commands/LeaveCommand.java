package com.meteordevelopments.duels.command.commands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.arena.ArenaImpl;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.config.CommandsConfig.CommandSettings;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

        // Notify player before forfeiting
        lang.sendMessage(sender, "COMMAND.leave.success");
        
        // Make the player lose the match by setting their health to 0
        // The match end logic will handle all cleanup and opponent notifications
        player.setHealth(0);
    }
}
