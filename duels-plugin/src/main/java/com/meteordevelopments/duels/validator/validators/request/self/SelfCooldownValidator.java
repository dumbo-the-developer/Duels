package com.meteordevelopments.duels.validator.validators.request.self;

import java.util.Collection;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.party.Party;
import com.meteordevelopments.duels.util.DateUtil;
import com.meteordevelopments.duels.validator.BaseTriValidator;
import org.bukkit.entity.Player;

public class SelfCooldownValidator extends BaseTriValidator<Player, Party, Collection<Player>> {

    public SelfCooldownValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean shouldValidate() {
        return config.getDuelCooldown() > 0L;
    }

    @Override
    public boolean validate(final Player sender, final Party party, final Collection<Player> players) {
        final Player cooldownPlayer = userManager.getCooldownPlayer(players);

        if (cooldownPlayer != null) {
            lang.sendMessage(sender, "ERROR.duel.in-cooldown",
                    "name", cooldownPlayer.getName(),
                    "time", DateUtil.formatMilliseconds(userManager.getDuelCooldownRemaining(cooldownPlayer)));
            return false;
        }

        return true;
    }
}