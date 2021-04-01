package me.realized.duels.hook.hooks;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaManagerImpl;
import me.realized.duels.config.Config;
import me.realized.duels.util.hook.PluginHook;
import net.minelink.ctplus.CombatTagPlus;
import net.minelink.ctplus.event.PlayerCombatTagEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CombatTagPlusHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "CombatTagPlus";

    private final Config config;
    private final ArenaManagerImpl arenaManager;

    public CombatTagPlusHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
        this.config = plugin.getConfiguration();
        this.arenaManager = plugin.getArenaManager();
        Bukkit.getPluginManager().registerEvents(new CombatTagPlusListener(), plugin);
    }

    public boolean isTagged(final Player player) {
        return config.isCtpPreventDuel() && ((CombatTagPlus) getPlugin()).getTagManager().isTagged(player.getUniqueId());
    }

    public class CombatTagPlusListener implements Listener {

        @EventHandler(ignoreCancelled = true)
        public void on(final PlayerCombatTagEvent event) {
            if (!config.isCtpPreventTag()) {
                return;
            }

            final Player player = event.getPlayer();

            if (!arenaManager.isInMatch(player)) {
                return;
            }

            event.setCancelled(true);
        }
    }
}
