package me.realized._duels.hooks;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.event.PowerLossEvent;
import me.realized._duels.Core;
import me.realized._duels.configuration.MainConfig;
import me.realized._duels.utilities.Storage;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class FactionsUUIDHook extends PluginHook implements Listener {

    private final MainConfig config;

    public FactionsUUIDHook(Core instance) {
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

        FPlayer player = event.getfPlayer();
        Storage storage = Storage.get(player.getPlayer());
        Object value = storage.get("matchDeath");

        if (value != null) {
            event.setMessage("");
            event.setCancelled(true);
        }
    }
}
