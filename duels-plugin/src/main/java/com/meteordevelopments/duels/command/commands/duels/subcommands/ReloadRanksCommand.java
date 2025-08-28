package com.meteordevelopments.duels.command.commands.duels.subcommands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.command.BaseCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ReloadRanksCommand extends BaseCommand {

    public ReloadRanksCommand(final DuelsPlugin plugin) {
        super(plugin, "reloadranks", "reloadranks", "Reloads the rank configuration.", 1, true, "reloadr");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        try {
            plugin.getRankManager().reload();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getLang().getMessage("RANK.reloaded")));
        } catch (Exception e) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cFailed to reload rank configuration: " + e.getMessage()));
            e.printStackTrace();
        }
    }
}
