package me.realized.duels.player;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class PlayerInfo {

    @Getter
    private final List<ItemStack> inventory;
    private final ItemStack[] armor;
    private final Collection<PotionEffect> effects;
    private final double health;
    private final int hunger;

    @Getter
    @Setter
    private Location location;

    public PlayerInfo(final Player player, final boolean inventory) {
        this.inventory = inventory ? Lists.newArrayList(player.getInventory().getContents().clone()) : Collections.emptyList();
        this.armor = inventory ? player.getInventory().getArmorContents().clone() : new ItemStack[0];
        this.effects = player.getActivePotionEffects();
        this.health = player.getHealth();
        this.hunger = player.getFoodLevel();
        this.location = player.getLocation().clone();
    }

    public void restore(final Player player, boolean respawn) {
        player.addPotionEffects(effects);

        if (!respawn) {
            player.setHealth(health);
            player.setFoodLevel(hunger);
        }

        if (armor.length > 0) {
            player.getInventory().setArmorContents(armor);
        }

        inventory.stream().filter(Objects::nonNull).forEach(item -> player.getInventory().addItem(item));
        player.updateInventory();
    }
}
