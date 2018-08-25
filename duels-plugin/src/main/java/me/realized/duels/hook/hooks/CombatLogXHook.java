package me.realized.duels.hook.hooks;

import com.SirBlobman.combatlogx.Combat;
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
            final Class<?> clazz = Class.forName("com.SirBlobman.combatlogx.Combat");
            clazz.getMethod("isInCombat", Player.class);
            clazz.getMethod("remove", Player.class);
        } catch (NoSuchMethodException | ClassNotFoundException ex) {
            throw new RuntimeException("This version of " + getName() + " is not supported. Please try upgrading to the latest version.");
        }
    }

    public boolean isTagged(final Player player) {
        return config.isClxPreventDuel() && Combat.isInCombat(player);
    }

    public void removeTag(final Player player) {
        if (!config.isClxUntag()) {
            return;
        }

        Combat.remove(player);
    }
}
