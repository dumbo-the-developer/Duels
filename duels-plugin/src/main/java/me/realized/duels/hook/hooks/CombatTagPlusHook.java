package me.realized.duels.hook.hooks;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.config.Config;
import me.realized.duels.util.hook.PluginHook;
import net.minelink.ctplus.CombatTagPlus;
import org.bukkit.entity.Player;

public class CombatTagPlusHook extends PluginHook<DuelsPlugin> {

    private final Config config;

    public CombatTagPlusHook(final DuelsPlugin plugin) {
        super(plugin, "CombatTagPlus");
        this.config = plugin.getConfiguration();
    }

    public boolean isTagged(final Player player) {
        return config.isCtpPreventDuel() && ((CombatTagPlus) getPlugin()).getTagManager().isTagged(player.getUniqueId());
    }

    public void removeTag(final Player player) {
        if (!config.isCtpUntag()) {
            return;
        }

        ((CombatTagPlus) getPlugin()).getTagManager().untag(player.getUniqueId());
    }
}
