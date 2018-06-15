package me.realized.duels.data;

import java.util.HashMap;
import java.util.Map;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.kit.Kit;
import org.bukkit.inventory.ItemStack;

public class KitData {

    private final String name;
    private final ItemData displayed;
    private final boolean usePermission;
    private final boolean arenaSpecific;
    private final Map<String, Map<Integer, ItemData>> items = new HashMap<>();

    public KitData(final Kit kit) {
        this.name = kit.getName();
        this.displayed = new ItemData(kit.getDisplayed());
        this.usePermission = kit.isUsePermission();
        this.arenaSpecific = kit.isArenaSpecific();

        for (final Map.Entry<String, Map<Integer, ItemStack>> entry : kit.getItems().entrySet()) {
            final Map<Integer, ItemData> data = new HashMap<>();
            entry.getValue().forEach(((slot, item) -> data.put(slot, new ItemData(item))));
            items.put(entry.getKey(), data);
        }
    }

    public Kit toKit(final DuelsPlugin plugin) {
        final Kit kit = new Kit(plugin, name, displayed.toItemStack(), usePermission, arenaSpecific);

        for (final Map.Entry<String, Map<Integer, ItemData>> entry : items.entrySet()) {
            final Map<Integer, ItemStack> data = new HashMap<>();
            entry.getValue().forEach(((slot, itemData) -> data.put(slot, itemData.toItemStack())));
            kit.getItems().put(entry.getKey(), data);
        }

        return kit;
    }
}
