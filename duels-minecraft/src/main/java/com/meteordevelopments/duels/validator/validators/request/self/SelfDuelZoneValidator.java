package com.meteordevelopments.duels.validator.validators.request.self;

import java.util.Collection;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.hook.hooks.worldguard.WorldGuardHook;
import com.meteordevelopments.duels.party.Party;
import com.meteordevelopments.duels.validator.BaseTriValidator;
import org.bukkit.entity.Player;

public class SelfDuelZoneValidator extends BaseTriValidator<Player, Party, Collection<Player>> {
    
    private static final String MESSAGE_KEY = "ERROR.duel.not-in-duelzone";
    private static final String PARTY_MESSAGE_KEY = "ERROR.party-duel.not-in-duelzone";

    private final WorldGuardHook worldGuard;

    public SelfDuelZoneValidator(final DuelsPlugin plugin) {
        super(plugin);
        this.worldGuard = plugin.getHookManager().getHook(WorldGuardHook.class);
    }

    @Override
    public boolean shouldValidate() {
        return config.isDuelzoneEnabled() && worldGuard != null;
    }

    @Override
    public boolean validate(final Player sender, final Party party, final Collection<Player> players) {
        if (players.stream().anyMatch(player -> worldGuard.findDuelZone(player) == null)) {
            lang.sendMessage(sender, party != null ? PARTY_MESSAGE_KEY : MESSAGE_KEY, "regions", config.getDuelzones());
        }

        return true;
    }
}
