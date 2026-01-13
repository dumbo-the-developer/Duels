package com.meteordevelopments.duels.validator.validators.request.self;

import java.util.Collection;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.hook.hooks.CombatLogXHook;
import com.meteordevelopments.duels.hook.hooks.CombatTagPlusHook;
import com.meteordevelopments.duels.hook.hooks.DeluxeCombatHook;
import com.meteordevelopments.duels.hook.hooks.PvPManagerHook;
import com.meteordevelopments.duels.party.Party;
import com.meteordevelopments.duels.validator.BaseTriValidator;
import org.bukkit.entity.Player;

public class SelfCombatTagValidator extends BaseTriValidator<Player, Party, Collection<Player>> {
   
    private static final String MESSAGE_KEY = "ERROR.duel.is-tagged";
    private static final String PARTY_MESSAGE_KEY = "ERROR.party-duel.is-tagged";

    private final CombatTagPlusHook combatTagPlus;
    private final PvPManagerHook pvpManager;
    private final CombatLogXHook combatLogX;
    private final DeluxeCombatHook deluxeCombat;

    public SelfCombatTagValidator(final DuelsPlugin plugin) {
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
    public boolean validate(final Player sender, final Party party, final Collection<Player> players) {
        if (players.stream().anyMatch(this::isTagged)) {
            lang.sendMessage(sender, party != null ? PARTY_MESSAGE_KEY : MESSAGE_KEY);
            return false;
        }

        return true;
    }
}
