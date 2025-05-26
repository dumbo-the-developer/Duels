package com.meteordevelopments.duels.command.commands.duel.subcommands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.api.event.request.RequestDenyEvent;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.request.RequestImpl;
import com.meteordevelopments.duels.util.function.Pair;
import com.meteordevelopments.duels.util.validator.ValidatorUtil;
import com.meteordevelopments.duels.party.Party;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;

public class DenyCommand extends BaseCommand {

    public DenyCommand(final DuelsPlugin plugin) {
        super(plugin, "deny", "deny [player]", "Denies a duel request.", 2, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            lang.sendMessage(sender, "ERROR.player.console");
            return;
        }

        final Player player = (Player) sender;
        final Player target = Bukkit.getPlayer(args[1]);

        if (target == null || !player.canSee(target)) {
            lang.sendMessage(sender, "ERROR.player.not-found", "name", args[1]);
            return;
        }

        RequestImpl request = requestManager.get(target, player);
        if (request != null && request.isPartyDuel()) {
            Party playerParty = partyManager.get(player);
            if (playerParty == null || !playerParty.isOwner(player)) {
                lang.sendMessage(sender, "ERROR.party.is-not-owner");
                return;
            }
        }

        if (!ValidatorUtil.validate(validatorManager.getDuelDenyTargetValidators(), new Pair<>(player, target), partyManager.get(target), Collections.emptyList())) {
            return;
        }

        request = requestManager.remove(target, player);
        if (request == null) {
            lang.sendMessage(sender, "ERROR.request.not-found", "name", target.getName());
            return;
        }

        if (request.isPartyDuel()) {
            final Collection<Player> senderPartyMembers = request.getSenderParty().getOnlineMembers();
            final Player targetPartyLeader = request.getTargetParty().getOwner().getPlayer();
            lang.sendMessage(senderPartyMembers, "COMMAND.duel.party-request.deny.receiver-party", "owner", player.getName(), "name", target.getName());
            lang.sendMessage(targetPartyLeader, "COMMAND.duel.party-request.deny.sender-party", "owner", target.getName(), "name", player.getName());
        } else {
            lang.sendMessage(player, "COMMAND.duel.request.deny.receiver", "name", target.getName());
            lang.sendMessage(target, "COMMAND.duel.request.deny.sender", "name", player.getName());
        }

        Bukkit.getPluginManager().callEvent(new RequestDenyEvent(player, target, request));
    }
}
