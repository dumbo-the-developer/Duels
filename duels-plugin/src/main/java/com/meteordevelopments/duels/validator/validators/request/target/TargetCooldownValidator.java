package com.meteordevelopments.duels.validator.validators.request.target;

import java.util.Collection;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.party.Party;
import com.meteordevelopments.duels.util.DateUtil;
import com.meteordevelopments.duels.util.function.Pair;
import com.meteordevelopments.duels.validator.BaseTriValidator;
import org.bukkit.entity.Player;

public class TargetCooldownValidator extends BaseTriValidator<Pair<Player, Player>, Party, Collection<Player>> {

    public TargetCooldownValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean shouldValidate() {
        return config.getDuelCooldown() > 0L;
    }

    @Override
    public boolean validate(final Pair<Player, Player> pair, final Party party, final Collection<Player> players) {
        final Player cooldownPlayer = userManager.getCooldownPlayer(players);

        if (cooldownPlayer != null) {
            lang.sendMessage(pair.getKey(), "ERROR.duel.in-cooldown",
                    "name", cooldownPlayer.getName(),
                    "time", DateUtil.formatMilliseconds(userManager.getDuelCooldownRemaining(cooldownPlayer)));
            return false;
        }

        return true;
    }
}