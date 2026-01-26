package com.meteordevelopments.duels.command.commands.party.subcommands;

import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.party.PartyMember;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.party.Party;

public class KickCommand extends BaseCommand {
    
    public KickCommand(final DuelsPlugin plugin) {
        super(plugin, "kick", "kick [player]", "Kicks a player from your party.", Permissions.PARTY, 2, true, "remove");
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
        
        final PartyMember member = party.get(args[1]);

        if (member == null) {
            lang.sendMessage(sender, "ERROR.party.not-a-member", "name", args[1]);
            return;
        }
        
        if (member.getUuid().equals(player.getUniqueId())) {
            lang.sendMessage(sender, "ERROR.party.kick-self");
            return;
        }

        partyManager.remove(member, party);

        final Player target = member.getPlayer();

        if (target != null) {
            lang.sendMessage(target, "COMMAND.party.kick.receiver", "owner", player.getName());
        }

        lang.sendMessage(party.getOnlineMembers(), "COMMAND.party.kick.members", "owner", player.getName(), "name", member.getName());
    }
}
