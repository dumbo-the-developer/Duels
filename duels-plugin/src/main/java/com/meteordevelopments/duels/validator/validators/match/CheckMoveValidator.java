package com.meteordevelopments.duels.validator.validators.match;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.setting.Settings;
import com.meteordevelopments.duels.validator.BaseBiValidator;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;

public class CheckMoveValidator extends BaseBiValidator<Collection<Player>, Settings> {

    private static final String MESSAGE_KEY = "DUEL.start-failure.player-moved";
    private static final String PARTY_MESSAGE_KEY = "DUEL.party-start-failure.player-moved";

    public CheckMoveValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean shouldValidate() {
        return config.isCancelIfMoved();
    }

    private boolean notInLoc(final Player player, final Location location) {
        if (location == null) {
            return false;
        }

        final Location source = player.getLocation();
        return !source.getWorld().equals(location.getWorld())
                || source.getBlockX() != location.getBlockX()
                || source.getBlockY() != location.getBlockY()
                || source.getBlockZ() != location.getBlockZ();
    }

    @Override
    public boolean validate(final Collection<Player> players, final Settings settings) {
        if (players.stream().anyMatch(player -> notInLoc(player, settings.getBaseLoc(player)))) {
            lang.sendMessage(players, settings.isPartyDuel() ? PARTY_MESSAGE_KEY : MESSAGE_KEY);
            return false;
        }

        return true;
    }
}
