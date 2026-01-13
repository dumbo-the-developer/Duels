package com.meteordevelopments.duels.command.commands.party.subcommands;

import com.meteordevelopments.duels.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.party.Party;

public class TransferCommand extends BaseCommand {
    
    public TransferCommand(final DuelsPlugin plugin) {
        super(plugin, "transfer", "transfer [player]", "Transfers the party ownership to another member of your party.", Permissions.PARTY, 2, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;
        final Party party = partyManager.get(player);

        if (party == null) {
            lang.sendMessage(sender, "ERROR.party.not-in-party.sender");
            return;
        }

        if (!party.isOwner(player)) {
            lang.sendMessage(sender, "ERROR.party.is-not-owner");
            return;
        }

        final Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null || !player.canSee(target)) {
            lang.sendMessage(sender, "ERROR.player.not-found", "name", args[1]);
            return;
        }

        if (!party.isMember(target)) {
            lang.sendMessage(sender, "ERROR.party.not-a-member", "name", target.getName());
            return;
        }
        
        party.setOwner(target);
        lang.sendMessage(party.getOnlineMembers(), "COMMAND.party.transfer", "owner", player.getName(), "name", target.getName());
    }
}
