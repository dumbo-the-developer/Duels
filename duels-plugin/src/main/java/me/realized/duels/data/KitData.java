package me.realized.duels.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.kit.KitImpl;
import me.realized.duels.kit.KitImpl.Characteristic;
import me.realized.duels.util.Log;
import org.bukkit.inventory.ItemStack;

public class KitData {

    private static transient final String ITEM_LOAD_FAILURE = "Could not load item %s for kit %s!";

    public static KitData fromKit(final KitImpl kit) {
        return new KitData(kit);
    }

    private String name;
    private ItemData displayed;
    private boolean usePermission;
    private boolean arenaSpecific;
    private Set<Characteristic> characteristics = new HashSet<>();
    private Map<String, Map<Integer, ItemData>> items = new HashMap<>();

    // for Gson deserializer
    private KitData() {}

    private KitData(final KitImpl kit) {
        this.name = kit.getName();
        this.displayed = ItemData.fromItemStack(kit.getDisplayed());
        this.usePermission = kit.isUsePermission();
        this.arenaSpecific = kit.isArenaSpecific();
        this.characteristics.addAll(kit.getCharacteristics());

        for (final Map.Entry<String, Map<Integer, ItemStack>> entry : kit.getItems().entrySet()) {
            final Map<Integer, ItemData> data = new HashMap<>();
            entry.getValue().entrySet()
                .stream()
                .filter(value -> Objects.nonNull(value.getValue()))
                .forEach(value -> data.put(value.getKey(), ItemData.fromItemStack(value.getValue())));
            items.put(entry.getKey(), data);
        }
    }

    public KitImpl toKit(final DuelsPlugin plugin) {
        final KitImpl kit = new KitImpl(plugin, name, displayed.toItemStack(), usePermission, arenaSpecific, characteristics);

        for (final Map.Entry<String, Map<Integer, ItemData>> entry : items.entrySet()) {
            final Map<Integer, ItemStack> data = new HashMap<>();
            entry.getValue().forEach(((slot, itemData) -> {
                final ItemStack item = itemData.toItemStack();

                if (item == null) {
                    Log.warn(String.format(ITEM_LOAD_FAILURE, itemData.toString(), kit.getName()));
                    return;
                }

                data.put(slot, item);
            }));
            kit.getItems().put(entry.getKey(), data);
        }

        return kit;
    }
}
