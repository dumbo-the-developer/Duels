package com.meteordevelopments.duels.validator.validators.match;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.setting.Settings;
import com.meteordevelopments.duels.validator.BaseBiValidator;
import org.bukkit.entity.Player;

import java.util.Collection;

public class PartyValidator extends BaseBiValidator<Collection<Player>, Settings> {

    public PartyValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Collection<Player> players, final Settings settings) {
        if (!settings.isPartyDuel()) {
            return true;
        }

        int senderPartySize = settings.getSenderParty().size();
        int targetPartySize = settings.getTargetParty().size();

        if (config.isPartySameSizeOnly() && senderPartySize != targetPartySize) {
            lang.sendMessage(players, "DUEL.party-start-failure.is-not-same-size");
            return false;
        }

        if (players.size() != senderPartySize + targetPartySize) {
            lang.sendMessage(players, "DUEL.party-start-failure.is-not-all-online");
            return false;
        }

        return true;
    }
}
