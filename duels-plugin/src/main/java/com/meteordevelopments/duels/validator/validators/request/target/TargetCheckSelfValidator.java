package com.meteordevelopments.duels.validator.validators.request.target;

import java.util.Collection;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.party.Party;
import com.meteordevelopments.duels.util.function.Pair;
import com.meteordevelopments.duels.validator.BaseTriValidator;
import org.bukkit.entity.Player;

public class TargetCheckSelfValidator extends BaseTriValidator<Pair<Player, Player>, Party, Collection<Player>> {

    public TargetCheckSelfValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Pair<Player, Player> pair, final Party party, final Collection<Player> players) {
        if (pair.getKey().equals(pair.getValue())) {
            lang.sendMessage(pair.getKey(), "ERROR.duel.is-self");
            return false;
        }

        return true;
    }

    
}