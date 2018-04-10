package me.realized.duels.kit;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Kit {

    @Getter
    private final String name;
    @Getter
    private final Map<String, Map<Integer, ItemStack>> items = new HashMap<>();
    @Getter
    @Setter
    private ItemStack displayed;

    public Kit(final String name) {
        this.name = name;
        this.displayed = ItemBuilder
            .of(Material.DIAMOND_SWORD)
            .name("&7&l" + name)
            .lore("&aClick to send", "&aa duel request", "&awith this kit!")
            .build();
    }

    public Kit(final String name, final PlayerInventory inventory) {
        this(name);

        final Map<Integer, ItemStack> contents = new HashMap<>();

        for (int i = 0; i < inventory.getSize(); i++) {
            final ItemStack item = inventory.getItem(i);

            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            contents.put(i, item.clone());
        }

        items.put("INVENTORY", contents);

        final Map<Integer, ItemStack> armorContents = new HashMap<>();

        if (inventory.getHelmet() != null) {
            armorContents.put(1, inventory.getHelmet().clone());
        }

        if (inventory.getChestplate() != null) {
            armorContents.put(2, inventory.getChestplate().clone());
        }

        if (inventory.getLeggings() != null) {
            armorContents.put(3, inventory.getLeggings().clone());
        }

        if (inventory.getBoots() != null) {
            armorContents.put(4, inventory.getBoots().clone());
        }

        items.put("ARMOR", armorContents);
    }

    public void equip(final Player... players) {
        for (final Player player : players) {
            for (final Map.Entry<Integer, ItemStack> entry : items.get("INVENTORY").entrySet()) {
                player.getInventory().setItem(entry.getKey(), entry.getValue().clone());
            }

            for (final Map.Entry<Integer, ItemStack> entry : items.get("ARMOR").entrySet()) {
                switch (entry.getKey()) {
                    case 1:
                        player.getInventory().setHelmet(entry.getValue().clone());
                        break;
                    case 2:
                        player.getInventory().setChestplate(entry.getValue().clone());
                        break;
                    case 3:
                        player.getInventory().setLeggings(entry.getValue().clone());
                        break;
                    case 4:
                        player.getInventory().setBoots(entry.getValue().clone());
                        break;
                }
            }
        }
    }
}
