package me.realized.duels.cache;

import java.util.Collection;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class PlayerData {

    private final ItemStack[] inventory;
    private final ItemStack[] armor;
    private final Collection<PotionEffect> effects;
    private final double health;
    private final int hunger;

    @Getter
    private final Location location;

    PlayerData(final Player player) {
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
        player.updateInventory();
    }
}
