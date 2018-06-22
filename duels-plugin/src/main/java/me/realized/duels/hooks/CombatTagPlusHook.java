package me.realized.duels.hooks;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.hook.PluginHook;
import net.minelink.ctplus.CombatTagPlus;
import org.bukkit.entity.Player;

public class CombatTagPlusHook extends PluginHook<DuelsPlugin> {

    public CombatTagPlusHook(final DuelsPlugin plugin) {
        super(plugin, "CombatTagPlus");
    }

    public void removeTag(final Player player) {
        ((CombatTagPlus) getPlugin()).getTagManager().untag(player.getUniqueId());
    }
}
