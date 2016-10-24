package me.realized.duels.utilities.location;

import me.realized.duels.Core;
import me.realized.duels.configuration.MainConfig;
import me.realized.duels.utilities.Helper;
import me.realized.duels.utilities.Storage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class Teleport implements Listener {

    private final MainConfig config;

    public Teleport(Core instance) {
        this.config = instance.getConfiguration();
        Bukkit.getPluginManager().registerEvents(this, instance);
    }

    public boolean isAuthorizedFor(Player base, Location to) {
        if (to == null || to.getWorld() == null) {
            return false;
        }

        Helper.refreshChunk(to);
        Helper.updatePosition(base, to);

        if (config.isPatchesForceAllowTeleportation()) {
            return true;
        }

        PlayerTeleportEvent event = new PlayerTeleportEvent(base, base.getLocation(), to, PlayerTeleportEvent.TeleportCause.PLUGIN);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public void teleportPlayer(Player base, Location to) {
        if (config.isPatchesForceAllowTeleportation()) {
            Storage.get(base).set("teleportedTo", to);
        }

        base.teleport(to);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void on(PlayerTeleportEvent event) {
        if (!config.isPatchesForceAllowTeleportation()) {
            return;
        }

        Storage storage = Storage.get(event.getPlayer());
        Object value = storage.get("teleportedTo");

        if (value == null) {
            return;
        }

        storage.remove("teleportedTo");
        event.setTo((Location) value);
        event.setCancelled(false);
    }
}
