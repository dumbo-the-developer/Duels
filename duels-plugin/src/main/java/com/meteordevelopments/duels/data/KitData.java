package com.meteordevelopments.duels.data;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.kit.KitImpl;
import com.meteordevelopments.duels.kit.KitImpl.Characteristic;
import com.meteordevelopments.duels.util.Log;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class KitData {

    private static final String SLOT_LOAD_FAILURE = "Could not load slot %s for kit %s!";
    private static final String ITEM_LOAD_FAILURE = "Could not load item %s for kit %s!";
    @Getter
    private String name;
    private ItemData displayed;
    private boolean usePermission;
    private boolean arenaSpecific;
    private final Set<Characteristic> characteristics = new HashSet<>();
    private final Map<String, Map<Integer, ItemData>> items = new HashMap<>();
    // for Gson deserializer
    private KitData() {
    }

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

    public static KitData fromKit(final KitImpl kit) {
        return new KitData(kit);
    }

    public KitImpl toKit(final DuelsPlugin plugin) {
        ItemStack displayed;

        if (this.displayed == null || (displayed = this.displayed.toItemStack()) == null) {
            displayed = ItemBuilder.of(Material.BARRIER).name("&cCould not load displayed item for " + name + "!").build();
        }

        final KitImpl kit = new KitImpl(plugin, name, displayed, usePermission, arenaSpecific, characteristics);

        for (final Map.Entry<String, Map<Integer, ItemData>> entry : items.entrySet()) {
            final Map<Integer, ItemStack> data = new HashMap<>();
            entry.getValue().forEach(((slot, itemData) -> {
                if (itemData == null) {
                    Log.warn(String.format(SLOT_LOAD_FAILURE, slot, kit.getName()));
                    return;
                }

                final ItemStack item = itemData.toItemStack();

                if (item == null) {
                    Log.warn(String.format(ITEM_LOAD_FAILURE, itemData, kit.getName()));
                    return;
                }

                data.put(slot, item);
            }));
            kit.getItems().put(entry.getKey(), data);
        }

        return kit;
    }
}
