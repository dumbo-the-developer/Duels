package me.realized.duels.util.gui;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import me.realized.duels.util.Loadable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public class GuiListener<P extends JavaPlugin> implements Loadable, Listener {

    private final Multimap<UUID, AbstractGui<P>> privateGuis = HashMultimap.create();
    private final List<AbstractGui<P>> publicGuis = new ArrayList<>();

    public GuiListener(final P plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() {}

    @Override
    public void handleUnload() {
        privateGuis.clear();
        publicGuis.clear();
    }

    public void addGui(final AbstractGui<P> gui) {
        publicGuis.add(gui);
    }

    public <T extends AbstractGui<P>> T addGui(final Player player, final T gui) {
        privateGuis.put(player.getUniqueId(), gui);
        return gui;
    }

    public void removeGui(final AbstractGui<P> gui) {
        publicGuis.remove(gui);
    }

    public void removeGui(final Player player, final AbstractGui<P> gui) {
        final Collection<AbstractGui<P>> guis = privateGuis.asMap().get(player.getUniqueId());

        if (guis != null) {
            guis.remove(gui);
        }
    }

    private List<AbstractGui<P>> get(final Player player) {
        final List<AbstractGui<P>> guis = new ArrayList<>(publicGuis);

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

        for (final AbstractGui<P> gui : get(player)) {
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

        for (final AbstractGui<P> gui : get(player)) {
            if (gui.isPart(inventory)) {
                gui.on(player, event.getRawSlots(), event);
                break;
            }
        }
    }

    @EventHandler
    public void on(final InventoryCloseEvent event) {
        final Player player = (Player) event.getPlayer();
        final Inventory inventory = event.getInventory();

        for (final AbstractGui<P> gui : get(player)) {
            if (gui.isPart(inventory)) {
                gui.on(player, event.getInventory(), event);
                break;
            }
        }
    }

    @EventHandler
    public void on(final InventoryOpenEvent event) {
        final Player player = (Player) event.getPlayer();
        final Inventory inventory = event.getInventory();

        for (final AbstractGui<P> gui : get(player)) {
            if (gui.isPart(inventory)) {
                final Collection<AbstractGui<P>> guis = privateGuis.asMap().get(event.getPlayer().getUniqueId());

                if (guis == null) {
                    return;
                }

                guis.removeIf(privateGui -> privateGui.removeIfEmpty() && !privateGui.equals(gui) && !privateGui.hasViewers());
                break;
            }
        }
    }

    @EventHandler
    public void on(final PlayerQuitEvent event) {
        privateGuis.removeAll(event.getPlayer().getUniqueId());
    }
}
