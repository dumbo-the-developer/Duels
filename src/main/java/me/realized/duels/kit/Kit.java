package me.realized.duels.kit;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import me.realized.duels.cache.Setting;
import me.realized.duels.cache.SettingCache;
import me.realized.duels.util.gui.Button;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Kit extends Button {

    private final SettingCache cache;
    @Getter
    private final String name;
    @Getter
    private final Map<String, Map<Integer, ItemStack>> items = new HashMap<>();

    public Kit(final SettingCache cache, final String name, final ItemStack displayed) {
        super(displayed);
        this.cache = cache;
        this.name = name;
    }

    public Kit(final SettingCache cache, final String name, final PlayerInventory inventory) {
        this(cache, name, ItemBuilder
            .of(Material.DIAMOND_SWORD)
            .name("&7&l" + name)
            .lore("&aClick to send", "&aa duel request", "&awith this kit!")
            .build());

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

    @Override
    public void onClick(final Player player) {
        final Setting setting = cache.get(player);
        setting.setKit(this);
        setting.getGui().open(player);
    }
}
