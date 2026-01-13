package com.meteordevelopments.duels.validator.validators.request.self;

import java.util.Collection;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.party.Party;
import com.meteordevelopments.duels.validator.BaseTriValidator;
import org.bukkit.entity.Player;

public class SelfBlacklistedWorldValidator extends BaseTriValidator<Player, Party, Collection<Player>> {

    private static final String MESSAGE_KEY = "ERROR.duel.in-blacklisted-world";
    private static final String PARTY_MESSAGE_KEY = "ERROR.party-duel.in-blacklisted-world";
    
    public SelfBlacklistedWorldValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean shouldValidate() {
        return !config.getBlacklistedWorlds().isEmpty();
    }

    private boolean isBlacklistedWorld(final Player player) {
        return config.getBlacklistedWorlds().contains(player.getWorld().getName());
    }

    @Override
    public boolean validate(final Player sender, final Party party, final Collection<Player> players) {
        if (players.stream().anyMatch(this::isBlacklistedWorld)) {
            lang.sendMessage(sender, party != null ? PARTY_MESSAGE_KEY : MESSAGE_KEY);
            return false;
        }

        return true;
    }
}
