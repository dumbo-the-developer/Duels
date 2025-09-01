package com.meteordevelopments.duels.validator.validators.request.target;

import java.util.Collection;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.party.Party;
import com.meteordevelopments.duels.util.function.Pair;
import com.meteordevelopments.duels.validator.BaseTriValidator;
import org.bukkit.entity.Player;


public class TargetCheckSelfMatchValidator extends BaseTriValidator<Pair<Player, Player>, Party, Collection<Player>> {

    private static final String MESSAGE_KEY = "ERROR.duel.already-in-match.sender";
    private static final String PARTY_MESSAGE_KEY = "ERROR.party-duel.already-in-match.sender";

    public TargetCheckSelfMatchValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Pair<Player, Player> pair, final Party party, final Collection<Player> players) {
        if (arenaManager.isInMatch(pair.getKey())) {
            lang.sendMessage(pair.getKey(), party != null ? PARTY_MESSAGE_KEY : MESSAGE_KEY, "name", pair.getValue().getName());
            return false;
        }

        return true;
    }
}