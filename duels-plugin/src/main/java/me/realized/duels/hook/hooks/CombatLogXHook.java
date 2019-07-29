package me.realized.duels.hook.hooks;

import com.SirBlobman.combatlogx.event.PlayerUntagEvent.UntagReason;
import com.SirBlobman.combatlogx.utility.CombatUtil;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.config.Config;
import me.realized.duels.util.hook.PluginHook;
import org.bukkit.entity.Player;

public class CombatLogXHook extends PluginHook<DuelsPlugin> {

    private final Config config;

    public CombatLogXHook(final DuelsPlugin plugin) {
        super(plugin, "CombatLogX");
        this.config = plugin.getConfiguration();

        try {
            final Class<?> clazz = Class.forName("com.SirBlobman.combatlogx.utility.CombatUtil");
            clazz.getMethod("isInCombat", Player.class);
            final Class<?> reasonClazz = Class.forName("com.SirBlobman.combatlogx.event.PlayerUntagEvent$UntagReason");
            clazz.getMethod("untag", Player.class, reasonClazz);
        } catch (NoSuchMethodException | ClassNotFoundException ex) {
            throw new RuntimeException("This version of " + getName() + " is not supported. Please try upgrading to the latest version.");
        }
    }

    public boolean isTagged(final Player player) {
        return config.isClxPreventDuel() && CombatUtil.isInCombat(player);
    }

    public void removeTag(final Player player) {
        if (!config.isClxUntag()) {
            return;
        }

        CombatUtil.untag(player, UntagReason.EXPIRE_ENEMY_DEATH);
    }
}
