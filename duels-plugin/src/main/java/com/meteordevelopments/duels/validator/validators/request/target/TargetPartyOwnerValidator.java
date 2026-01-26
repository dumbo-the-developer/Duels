package com.meteordevelopments.duels.validator.validators.request.target;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.party.Party;
import com.meteordevelopments.duels.util.function.Pair;
import com.meteordevelopments.duels.validator.BaseTriValidator;
import org.bukkit.entity.Player;

import java.util.Collection;

public class TargetPartyOwnerValidator extends BaseTriValidator<Pair<Player, Player>, Party, Collection<Player>> {

    public TargetPartyOwnerValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Pair<Player, Player> pair, final Party party, final Collection<Player> players) {
        final Party senderParty = partyManager.get(pair.getKey());

        // Skip validation for 1v1s
        if (party == null && senderParty == null) {
            return true;
        }

        // If sender is in a party, they must be the owner to send a duel request
        if (senderParty != null && !senderParty.isOwner(pair.getKey())) {
            lang.sendMessage(pair.getKey(), "ERROR.party.is-not-owner");
            return false;
        }

        return true;
    }
}
