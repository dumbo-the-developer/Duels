package me.realized.duels.cache;

import java.util.Collection;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class PlayerData {

    private ItemStack[] inventory;
    private ItemStack[] armor;
    private Collection<PotionEffect> effects;
    private double health;
    private int hunger;
    @Getter
    @Setter
    private Location location;

    PlayerData() {}

    public void init(final Player player) {
        this.inventory = player.getInventory().getContents().clone();
        this.armor = player.getInventory().getArmorContents().clone();
        this.effects = player.getActivePotionEffects();
        this.health = player.getHealth();
        this.hunger = player.getFoodLevel();
        this.location = player.getLocation().clone();
    }

    public void restore(final Player player) {
        player.addPotionEffects(effects);
        player.setHealth(health);
        player.setFoodLevel(hunger);
        player.getInventory().setArmorContents(armor);
        player.getInventory().setContents(inventory);
    }
}
