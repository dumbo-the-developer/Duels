package me.realized._duels.data;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class PlayerData {

    private final ItemStack[] inventory;
    private final ItemStack[] armor;
    private final List<PotionEffect> effects;
    private final double health;
    private final int hunger;
    private Location location;

    PlayerData(Player player) {
        this.inventory = player.getInventory().getContents().clone();
        this.armor = player.getInventory().getArmorContents().clone();
        this.effects = new ArrayList<>(player.getActivePotionEffects());
        this.location = player.getLocation().clone();
        this.health = player.getHealth();
        this.hunger = player.getFoodLevel();
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void restore(Player player, boolean withInventory) {
        player.addPotionEffects(effects);
        player.setHealth(health);
        player.setFoodLevel(hunger);

        if (withInventory) {
            player.getInventory().setArmorContents(armor);
            player.getInventory().setContents(inventory);
            player.updateInventory();
        }
    }
}
