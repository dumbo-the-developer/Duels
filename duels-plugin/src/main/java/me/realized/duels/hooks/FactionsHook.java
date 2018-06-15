package me.realized.duels.hooks;

import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.event.EventFactionsPowerChange;
import com.massivecraft.factions.event.PowerLossEvent;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.hook.PluginHook;
import me.realized.duels.util.metadata.MetadataUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class FactionsHook extends PluginHook<DuelsPlugin> {

    public static final String METADATA_KEY = "DuelDeath";

    public FactionsHook(final DuelsPlugin plugin) {
        super(plugin, "Factions");

        Listener listener;

        try {
            Class.forName("com.massivecraft.factions.event.PowerLossEvent");
            listener = new FactionsUUIDListener();
        } catch (ClassNotFoundException ex) {
            listener = new Factions2Listener();
        }

        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    public class Factions2Listener implements Listener {

        @EventHandler
        public void on(final EventFactionsPowerChange event) {
            final MPlayer mPlayer = event.getMPlayer();
            final Player player = mPlayer.getPlayer();
            final Object value = MetadataUtil.removeAndGet(plugin, player, METADATA_KEY);

            if (value != null) {
                event.setCancelled(true);
            }
        }
    }

    public class FactionsUUIDListener implements Listener {

        @EventHandler
        public void on(final PowerLossEvent event) {
            final Player player = event.getfPlayer().getPlayer();
            final Object value = MetadataUtil.removeAndGet(plugin, player, METADATA_KEY);

            if (value != null) {
                event.setCancelled(true);
            }
        }
    }
}
