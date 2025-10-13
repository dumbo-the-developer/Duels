package com.meteordevelopments.duels.player;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import com.meteordevelopments.duels.util.PlayerUtil;
import com.meteordevelopments.duels.util.inventory.InventoryUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.*;

@Getter
public class PlayerInfo {

    private final Map<String, Map<Integer, ItemStack>> items = new HashMap<>();
    private final List<PotionEffect> effects;
    private final double health;
    private final float experience;
    private final int level;
    private final int hunger;
    private final boolean restoreExperience;
    private final List<ItemStack> extra = new ArrayList<>();
    @Setter
    private Location location;

    public PlayerInfo(final List<PotionEffect> effects, final double health, final float experience, final int level, final int hunger, final Location location, final boolean restoreExperience) {
        this.effects = effects;
        this.health = health;
        this.experience = experience;
        this.level = level;
        this.hunger = hunger;
        this.location = location;
        this.restoreExperience = restoreExperience;
    }

    public PlayerInfo(final Player player, final boolean excludeInventory) {
        this(Lists.newArrayList(player.getActivePotionEffects()), player.getHealth(), player.getExp(), player.getLevel(), player.getFoodLevel(), player.getLocation().clone(), true);
        if (excludeInventory) {
            return;
        }

        InventoryUtil.addToMap(player.getInventory(), items);
    }

    public PlayerInfo(final Player player, final boolean excludeInventory, final boolean restoreExperience) {
        this(Lists.newArrayList(player.getActivePotionEffects()), player.getHealth(), player.getExp(), player.getLevel(), player.getFoodLevel(), player.getLocation().clone(), restoreExperience);
        if (excludeInventory) {
            return;
        }

        InventoryUtil.addToMap(player.getInventory(), items);
    }

    public void restore(final Player player) {
        final double maxHealth = PlayerUtil.getMaxHealth(player);
        player.addPotionEffects(effects);
        player.setHealth(Math.min(health, maxHealth));
        if (restoreExperience) {
            player.setExp(experience);
            player.setLevel(level);
        }
        player.setFoodLevel(hunger);
        InventoryUtil.fillFromMap(player.getInventory(), items);
        InventoryUtil.addOrDrop(player, extra);
        player.updateInventory();
    }
}
