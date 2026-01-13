package com.meteordevelopments.duels.command.commands.duels.subcommands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.arena.ArenaImpl;
import com.meteordevelopments.duels.command.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class HardstopCommand extends BaseCommand {

    public HardstopCommand(final DuelsPlugin plugin) {
        super(plugin, "hardstop", "hardstop <arena>", "Emergency stop for stuck arenas.", 2, false);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final String arenaName = String.join(" ", args).substring(args[0].length() + 1).trim();
        
        final ArenaImpl arena = arenaManager.get(arenaName);
        
        if (arena == null) {
            lang.sendMessage(sender, "ERROR.arena.not-found", "name", arenaName);
            return;
        }
        
        // Use DuelManager's hard reset method
        if (duelManager.hardResetArena(arena)) {
            lang.sendMessage(sender, "COMMAND.duels.hardstop.success", "arena", arena.getName());
        } else {
            lang.sendMessage(sender, "COMMAND.duels.hardstop.no-match", "arena", arena.getName());
        }
    }
    
    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 2) {
            return handleTabCompletion(args[1], arenaManager.getNames());
        }
        return null;
    }
}

