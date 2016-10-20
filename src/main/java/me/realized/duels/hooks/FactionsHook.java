package me.realized.duels.hooks;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.event.PowerLossEvent;
import me.realized.duels.Core;
import me.realized.duels.configuration.MainConfig;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class FactionsHook extends PluginHook implements Listener {

    private final MainConfig config;

    public FactionsHook(Core instance) {
        super("Factions");
        this.config = instance.getConfiguration();

        if (isEnabled()) {
            Bukkit.getPluginManager().registerEvents(this, instance);
        }
    }

    @EventHandler
    public void onPowerLoss(PowerLossEvent event) {
        if (!config.isPatchesDisablePowerLoss()) {
            return;
        }

        long now = System.currentTimeMillis();
        FPlayer player = event.getfPlayer();

        if (player.getPlayer().hasMetadata("lastMatchDeath")) {
            long value = (long) player.getPlayer().getMetadata("lastMatchDeath").get(0).value();

            if (now - value < 1000L) {
                event.setMessage("");
                event.setCancelled(true);
            }
        }
    }
}
