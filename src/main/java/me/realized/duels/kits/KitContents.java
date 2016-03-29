package me.realized.duels.kits;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

public class KitContents {

    private Map<Integer, ItemStack> inventory = new HashMap<>();
    private Map<Integer, ItemStack> armor = new HashMap<>();

    public KitContents(PlayerInventory playerInventory) {
        for (int i = 0; i < playerInventory.getSize(); i++) {
            ItemStack item = playerInventory.getContents()[i];

            if (item == null) {
                continue;
            }

            inventory.put(i, item.clone());
        }

        if (playerInventory.getHelmet() != null) {
            armor.put(1, playerInventory.getHelmet().clone());
        }

        if (playerInventory.getChestplate() != null) {
            armor.put(2, playerInventory.getChestplate().clone());
        }

        if (playerInventory.getLeggings() != null) {
            armor.put(3, playerInventory.getLeggings().clone());
        }

        if (playerInventory.getBoots() != null) {
            armor.put(4, playerInventory.getBoots().clone());
        }
    }

    public KitContents(Map<Integer, ItemStack> inventory, Map<Integer, ItemStack> armor) {
        this.inventory = inventory;
        this.armor = armor;
    }

    public Map<Integer, ItemStack> getInventory() {
        return inventory;
    }

    public Map<Integer, ItemStack> getArmor() {
        return armor;
    }

    public void equip(Player... players) {
        for (Player player : players) {
            for (Map.Entry<Integer, ItemStack> entry : inventory.entrySet()) {
                player.getInventory().setItem(entry.getKey(), entry.getValue());
            }

            for (Map.Entry<Integer, ItemStack> entry : armor.entrySet()) {
                switch (entry.getKey()) {
                    case 1:
                        player.getInventory().setHelmet(entry.getValue());
                        break;
                    case 2:
                        player.getInventory().setChestplate(entry.getValue());
                        break;
                    case 3:
                        player.getInventory().setLeggings(entry.getValue());
                        break;
                    case 4:
                        player.getInventory().setBoots(entry.getValue());
                        break;
                }
            }
        }
    }
}
