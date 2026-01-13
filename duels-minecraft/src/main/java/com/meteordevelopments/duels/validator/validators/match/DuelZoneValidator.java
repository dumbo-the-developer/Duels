package com.meteordevelopments.duels.validator.validators.match;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.hook.hooks.worldguard.WorldGuardHook;
import com.meteordevelopments.duels.setting.Settings;
import com.meteordevelopments.duels.validator.BaseBiValidator;
import org.bukkit.entity.Player;

import java.util.Collection;

public class DuelZoneValidator extends BaseBiValidator<Collection<Player>, Settings> {

    private static final String MESSAGE_KEY = "DUEL.start-failure.not-in-duelzone";
    private static final String PARTY_MESSAGE_KEY = "DUEL.party-start-failure.not-in-duelzone";

    private final WorldGuardHook worldGuard;

    public DuelZoneValidator(final DuelsPlugin plugin) {
        super(plugin);
        this.worldGuard = plugin.getHookManager().getHook(WorldGuardHook.class);
    }

    @Override
    public boolean shouldValidate() {
        return config.isDuelzoneEnabled() && worldGuard != null;
    }

    private boolean notInDuelZone(final Player player, final String duelzone) {
        return duelzone != null && !duelzone.equals(worldGuard.findDuelZone(player));
    }

    @Override
    public boolean validate(final Collection<Player> players, final Settings settings) {
        if (players.stream().anyMatch(player -> notInDuelZone(player, settings.getDuelzone(player)))) {
            lang.sendMessage(players, settings.isPartyDuel() ? PARTY_MESSAGE_KEY : MESSAGE_KEY);
            return false;
        }

        return true;
    }
}
