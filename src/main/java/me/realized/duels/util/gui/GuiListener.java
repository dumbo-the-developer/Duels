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

public class GuiListener implements Listener {

    private final Multimap<UUID, AbstractGui> privateGuis = HashMultimap.create();
    private final List<AbstractGui> publicGuis = new ArrayList<>();

    public GuiListener(final JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void addGui(final AbstractGui gui) {
        publicGuis.add(gui);
    }

    public <T extends AbstractGui> T addGui(final Player player, final T gui) {
        privateGuis.put(player.getUniqueId(), gui);
        return gui;
    }

    private List<AbstractGui> get(final Player player) {
        final List<AbstractGui> guis = new ArrayList<>(publicGuis);

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

        for (final AbstractGui gui : get(player)) {
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

        for (final AbstractGui gui : get(player)) {
            if (gui.isPart(inventory)) {
                gui.on(player, event.getRawSlots(), event);
                break;
            }
        }
    }
}
