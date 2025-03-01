package com.meteordevelopments.duels.validator.validators.match;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.hook.hooks.CombatLogXHook;
import com.meteordevelopments.duels.hook.hooks.CombatTagPlusHook;
import com.meteordevelopments.duels.hook.hooks.DeluxeCombatHook;
import com.meteordevelopments.duels.hook.hooks.PvPManagerHook;
import com.meteordevelopments.duels.setting.Settings;
import com.meteordevelopments.duels.validator.BaseBiValidator;
import org.bukkit.entity.Player;

import java.util.Collection;

public class CombatTagValidator extends BaseBiValidator<Collection<Player>, Settings> {

    private static final String MESSAGE_KEY = "DUEL.start-failure.is-tagged";
    private static final String PARTY_MESSAGE_KEY = "DUEL.party-start-failure.is-tagged";

    private final CombatTagPlusHook combatTagPlus;
    private final PvPManagerHook pvpManager;
    private final CombatLogXHook combatLogX;
    private final DeluxeCombatHook deluxeCombat;

    public CombatTagValidator(final DuelsPlugin plugin) {
        super(plugin);
        this.combatTagPlus = plugin.getHookManager().getHook(CombatTagPlusHook.class);
        this.pvpManager = plugin.getHookManager().getHook(PvPManagerHook.class);
        this.combatLogX = plugin.getHookManager().getHook(CombatLogXHook.class);
        this.deluxeCombat = plugin.getHookManager().getHook(DeluxeCombatHook.class);
    }

    @Override
    public boolean shouldValidate() {
        return (combatTagPlus != null && config.isCtpPreventDuel())
                || (pvpManager != null && config.isPmPreventDuel())
                || (combatLogX != null && config.isClxPreventDuel())
                || (deluxeCombat != null && config.isDcPreventDuel());
    }

    private boolean isTagged(final Player player) {
        return (combatTagPlus != null && combatTagPlus.isTagged(player))
                || (pvpManager != null && pvpManager.isTagged(player))
                || (combatLogX != null && combatLogX.isTagged(player))
                || (deluxeCombat != null && deluxeCombat.isTagged(player));
    }

    @Override
    public boolean validate(final Collection<Player> players, final Settings settings) {
        if (players.stream().anyMatch(this::isTagged)) {
            lang.sendMessage(players, settings.isPartyDuel() ? PARTY_MESSAGE_KEY : MESSAGE_KEY);
            return false;
        }

        return true;
    }
}
