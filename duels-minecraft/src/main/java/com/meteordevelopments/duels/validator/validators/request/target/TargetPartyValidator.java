package com.meteordevelopments.duels.validator.validators.request.target;

import java.util.Collection;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.party.Party;
import com.meteordevelopments.duels.util.function.Pair;
import com.meteordevelopments.duels.validator.BaseTriValidator;
import org.bukkit.entity.Player;

public class TargetPartyValidator extends BaseTriValidator<Pair<Player, Player>, Party, Collection<Player>> {

    public TargetPartyValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Pair<Player, Player> pair, final Party party, final Collection<Player> players)  {
        final Party senderParty = partyManager.get(pair.getKey());
        // Skip for 1v1s
        if (party == null) {
            if (senderParty != null) {
                lang.sendMessage(pair.getKey(), "ERROR.party.not-in-party.target", "name", pair.getValue().getName());
                return false;
            }

            return true;
        }

        if (senderParty == null) {
            lang.sendMessage(pair.getKey(), "ERROR.party.not-in-party.sender", "name", pair.getKey().getName());
            return false;
        }

        // If sender is in the same party as target
        if (senderParty.equals(party)) {
            lang.sendMessage(pair.getKey(), "ERROR.party.in-same-party", "name", pair.getValue().getName());
            return false;
        }
        
        if (config.isPartySameSizeOnly() && senderParty.size() != party.size()) {
            lang.sendMessage(pair.getKey(), "ERROR.party.is-not-same-size");
            return false;
        }

        if (players.size() != party.size()) {
            lang.sendMessage(pair.getKey(), "ERROR.party.is-not-online.target", "name", pair.getValue().getName());
            return false;
        }
        
        return true;
    }

}
