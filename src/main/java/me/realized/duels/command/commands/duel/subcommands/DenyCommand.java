package me.realized.duels.command.commands.duel.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DenyCommand extends BaseCommand {

    public DenyCommand(final DuelsPlugin plugin) {
        super(plugin, "deny", "deny [player]", "Declines a duel request.", null, 2, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;
        final Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null) {
            sender.sendMessage("Player not found");
            return;
        }

        if (requestManager.remove(target, player) == null) {
            sender.sendMessage("You do not have a request from " + target.getName());
            return;
        }

        sender.sendMessage("Denied request from " + target.getName());
        target.sendMessage(ChatColor.RED + sender.getName() + " denied your request");
    }
}
