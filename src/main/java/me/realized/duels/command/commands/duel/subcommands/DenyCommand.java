package me.realized.duels.command.commands.duel.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import org.bukkit.Bukkit;
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
            lang.sendMessage(sender, "ERROR.player-not-found", "name", args[1]);
            return;
        }

        if (requestManager.remove(target, player) == null) {
            lang.sendMessage(sender, "ERROR.no-request", "player", target.getName());
            return;
        }

        lang.sendMessage(target, "COMMAND.duel.request.denied.sender", "player", player.getName());
        lang.sendMessage(player, "COMMAND.duel.request.denied.receiver", "player", target.getName());
    }
}
