package com.meteordevelopments.duels.command.commands.duels.subcommands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.util.CC;
import org.bukkit.command.CommandSender;

public class ReloadRanksCommand extends BaseCommand {

    public ReloadRanksCommand(final DuelsPlugin plugin) {
        super(plugin, "reloadranks", "reloadranks", "Reloads the rank configuration.", 1, true, "reloadr");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        try {
            plugin.getRankManager().reload();
            sender.sendMessage(CC.translate(plugin.getLang().getMessage("RANK.reloaded")));
        } catch (Exception e) {
            sender.sendMessage(CC.translate("&cFailed to reload rank configuration: " + e.getMessage()));
            e.printStackTrace();
        }
    }
}
