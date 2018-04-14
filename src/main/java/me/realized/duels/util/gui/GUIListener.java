package me.realized.duels.util.gui;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public class GUIListener implements Listener {

    private final Multimap<UUID, AbstractGUI> privateGuis = HashMultimap.create();
    private final List<AbstractGUI> publicGuis = new ArrayList<>();

    public GUIListener(final JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void addGUI(final AbstractGUI gui) {
        publicGuis.add(gui);
    }

    public void addGUI(final Player player, final AbstractGUI gui) {
        privateGuis.put(player.getUniqueId(), gui);
    }

    private List<AbstractGUI> get(final Player player) {
        final List<AbstractGUI> guis = new ArrayList<>(publicGuis);

        if (privateGuis.containsKey(player.getUniqueId())) {
            guis.addAll(privateGuis.get(player.getUniqueId()));
        }

        return guis;
    }

    @EventHandler
    public void on(final InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        final Inventory top = player.getOpenInventory().getTopInventory();

        if (top == null) {
            return;
        }

        for (final AbstractGUI gui : get(player)) {
            if (gui.isPart(top)) {
                gui.on(player, top, event);
                break;
            }
        }
    }

    @EventHandler
    public void on(final InventoryDragEvent event) {
        final Player player = (Player) event.getWhoClicked();
        final Inventory inventory = event.getInventory();

        for (final AbstractGUI gui : get(player)) {
            if (gui.isPart(inventory)) {
                event.setCancelled(true);
                break;
            }
        }
    }
}
