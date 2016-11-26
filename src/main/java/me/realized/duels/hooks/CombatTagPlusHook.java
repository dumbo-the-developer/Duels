package me.realized.duels.hooks;

import me.realized.duels.Core;
import me.realized.duels.configuration.MainConfig;
import net.minelink.ctplus.CombatTagPlus;
import org.bukkit.entity.Player;

public class CombatTagPlusHook extends PluginHook {

    private final MainConfig config;

    public CombatTagPlusHook(Core instance) {
        super("CombatTagPlus");
        this.config = instance.getConfiguration();
    }

    public boolean isTagged(Player player) {
        return !(!isEnabled() || !config.isPatchesDisallowDuelingWhileTagged()) && ((CombatTagPlus) getPlugin()).getTagManager().isTagged(player.getUniqueId());
    }
}
