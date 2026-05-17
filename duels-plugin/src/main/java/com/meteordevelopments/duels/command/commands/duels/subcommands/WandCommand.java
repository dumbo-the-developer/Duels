package com.meteordevelopments.duels.command.commands.duels.subcommands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.listeners.WandListener;
import com.meteordevelopments.duels.util.CC;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WandCommand extends BaseCommand {

    public WandCommand(final DuelsPlugin plugin) {
        super(plugin, "wand", "wand", "Gibt das Arena Wand Tool.", 1, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        Player player = (Player) sender;
        player.getInventory().addItem(WandListener.getWandItem());
        player.sendMessage(CC.translate("&aDu hast das Duels Arena Wand Tool erhalten!"));
    }
}
