package me.realized._duels.data;

import java.util.HashMap;
import java.util.Map;
import me.realized._duels.kits.Kit;
import me.realized._duels.kits.KitManager;
import me.realized._duels.utilities.inventory.JSONItem;
import org.bukkit.inventory.ItemStack;

public class KitData {

    private final String name;
    private final JSONItem displayed;
    private final Map<KitManager.Type, Map<Integer, JSONItem>> items = new HashMap<>();

    public KitData(Kit kit) {
        this.name = kit.getName();
        this.displayed = JSONItem.fromItemStack(kit.getDisplayed());

        for (Map.Entry<KitManager.Type, Map<Integer, ItemStack>> entry : kit.getItems().entrySet()) {
            Map<Integer, JSONItem> saved = new HashMap<>();

            for (Map.Entry<Integer, ItemStack> nextEntry : entry.getValue().entrySet()) {
                saved.put(nextEntry.getKey(), JSONItem.fromItemStack(nextEntry.getValue()));
            }

            items.put(entry.getKey(), saved);
        }
    }

    public Kit toKit() {
        Map<Integer, ItemStack> inventory = new HashMap<>();

        for (Map.Entry<Integer, JSONItem> entry : items.get(KitManager.Type.INVENTORY).entrySet()) {
            inventory.put(entry.getKey(), entry.getValue().construct());
        }

        Map<Integer, ItemStack> armor = new HashMap<>();

        for (Map.Entry<Integer, JSONItem> entry : items.get(KitManager.Type.ARMOR).entrySet()) {
            armor.put(entry.getKey(), entry.getValue().construct());
        }

        Map<KitManager.Type, Map<Integer, ItemStack>> items = new HashMap<>();
        items.put(KitManager.Type.INVENTORY, inventory);
        items.put(KitManager.Type.ARMOR, armor);
        return new Kit(name, displayed.construct(), items);
    }
}
