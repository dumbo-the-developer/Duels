package me.realized.duels.inventories;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.gui.inventory.InventoryGui;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.gui.GuiListener;
import org.bukkit.entity.Player;

public class InventoryManager implements Loadable {

    private final DuelsPlugin plugin;
    private final GuiListener<DuelsPlugin> guiListener;
    private final Map<UUID, InventoryGui> inventories = new HashMap<>();

    public InventoryManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.guiListener = plugin.getGuiListener();
    }

    @Override
    public void handleLoad() {
        // Start auto expire task
    }

    @Override
    public void handleUnload() {
        inventories.clear();
    }

    public void create(final Player player) {
        final InventoryGui gui = new InventoryGui(plugin, player);
        guiListener.addGui(gui);
        inventories.put(player.getUniqueId(), gui);
    }

    public InventoryGui get(final UUID uuid) {
        return inventories.get(uuid);
    }

    public void remove(final Player player) {
        final InventoryGui gui = inventories.remove(player.getUniqueId());

        if (gui != null) {
            guiListener.removeGui(gui);
        }
    }
}
