package me.realized._duels.utilities.gui;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public class GUIManager implements Listener {

    private List<GUI> registered = new ArrayList<>();

    public GUIManager(JavaPlugin instance) {
        Bukkit.getPluginManager().registerEvents(this, instance);
    }

    public void register(GUI gui) {
        registered.add(gui);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void on(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory top = player.getOpenInventory().getTopInventory();
        Inventory clicked = event.getClickedInventory();

        if (top == null || clicked == null) {
            return;
        }

        for (GUI gui : registered) {
            if (!gui.isPage(top)) {
                continue;
            }

            event.setCancelled(true);

            if (!clicked.equals(top)) {
                continue;
            }

            gui.getListener().onClick(event);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void on(InventoryCloseEvent event) {
        for (GUI gui : registered) {
            if (gui.isPage(event.getInventory())) {
                gui.getListener().onClose(event);
            }
        }
    }
}
