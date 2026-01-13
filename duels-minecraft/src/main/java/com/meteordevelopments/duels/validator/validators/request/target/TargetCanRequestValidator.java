package com.meteordevelopments.duels.validator.validators.request.target;

import java.util.Collection;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.party.Party;
import com.meteordevelopments.duels.util.function.Pair;
import com.meteordevelopments.duels.validator.BaseTriValidator;
import org.bukkit.entity.Player;
import com.meteordevelopments.duels.data.UserData;
import com.meteordevelopments.duels.Permissions;

public class TargetCanRequestValidator extends BaseTriValidator<Pair<Player, Player>, Party, Collection<Player>> {
    
    public TargetCanRequestValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Pair<Player, Player> pair, final Party party, final Collection<Player> players) {
        final UserData user = userManager.get(pair.getValue());

        if (user == null) {
            lang.sendMessage(pair.getKey(), "ERROR.data.not-found", "name", pair.getValue().getName());
            return false;
        }

        if (!pair.getKey().hasPermission(Permissions.ADMIN) && !user.canRequest()) {
            lang.sendMessage(pair.getKey(), "ERROR.duel.requests-disabled", "name",  pair.getValue().getName());
            return false;
        }

        return true;
    }

}
