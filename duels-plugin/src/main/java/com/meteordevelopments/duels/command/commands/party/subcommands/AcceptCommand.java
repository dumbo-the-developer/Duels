package com.meteordevelopments.duels.command.commands.party.subcommands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.party.Party;
import com.meteordevelopments.duels.party.PartyInvite;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AcceptCommand extends BaseCommand {
    
    public AcceptCommand(final DuelsPlugin plugin) {
        super(plugin, "accept", "accept [player]", "Accepts a party invitation.", Permissions.PARTY, 2, true, "a");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;

        if (partyManager.isInParty(player)) {
            lang.sendMessage(sender, "ERROR.party.already-in-party.sender");
            return;
        }

        final Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null || !player.canSee(target)) {
            lang.sendMessage(sender, "ERROR.player.not-found", "name", args[1]);
            return;
        }

        final PartyInvite invite = partyManager.removeInvite(target, player);

        if (invite == null) {
            lang.sendMessage(sender, "ERROR.party.no-invite", "name", target.getName());
            return;
        }

        final Party party = invite.getParty();

        if (party.isRemoved()) {
            lang.sendMessage(sender, "ERROR.party.not-found");
            return;
        }

        if (party.size() >= config.getPartyMaxSize()) {
            lang.sendMessage(sender, "ERROR.party.max-size-reached.target", "name", target.getName());
            return;
        }
        
        lang.sendMessage(player, "COMMAND.party.invite.accept.receiver", "name", target.getName());
        lang.sendMessage(party.getOnlineMembers(), "COMMAND.party.invite.accept.members", "name", player.getName());
        partyManager.join(player, party);
    }
}
