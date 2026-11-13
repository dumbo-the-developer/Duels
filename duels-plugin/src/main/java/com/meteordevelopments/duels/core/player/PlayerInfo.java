package com.meteordevelopments.duels.core.player;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import com.meteordevelopments.duels.util.PlayerUtil;
import com.meteordevelopments.duels.util.inventory.InventoryUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

@Getter
@SuppressWarnings("unused")
public class PlayerInfo {

    private final Map<String, Map<Integer, ItemStack>> items = new HashMap<>();
    private final List<PotionEffect> effects;
    private final double health;
    private final float experience;
    private final int level;
    private final int hunger;
    private final boolean restoreExperience;
    @Setter
    private Boolean pvpState; // Null when PvPManager was unavailable or loading an older cache.
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
        restore(player, true);
    }

    public void restore(final Player player, final boolean restorePotionEffects) {
        PlayerUtil.reset(player);

        final double maxHealth = PlayerUtil.getMaxHealth(player);

        // Build the set of allowed effect types from the pre-duel snapshot.
        final Set<PotionEffectType> allowedTypes = new HashSet<>();
        if (restorePotionEffects && effects != null) {
            for (final PotionEffect effect : effects) {
                if (effect != null) {
                    player.addPotionEffect(effect);
                    allowedTypes.add(effect.getType());
                }
            }
        }

        // Final cleanup: remove any effect not in the pre-duel saved set.
        // This catches duel-gained effects that survived the reset, or effects
        // re-applied by another plugin between the reset and this restore call.
        for (final PotionEffect active : new ArrayList<>(player.getActivePotionEffects())) {
            if (active != null && !allowedTypes.contains(active.getType())) {
                try {
                    player.removePotionEffect(active.getType());
                } catch (Throwable ignored) {}
            }
        }

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

    /**
     * Restore player state but DO NOT restore experience/level. This preserves any XP
     * changes that occurred during the match (for example, if the player picked up XP orbs).
     */
    public void restoreWithoutExperience(final Player player) {
        restoreWithoutExperience(player, true);
    }

    public void restoreWithoutExperience(final Player player, final boolean restorePotionEffects) {
        PlayerUtil.reset(player);

        final double maxHealth = PlayerUtil.getMaxHealth(player);

        // Build the set of allowed effect types from the pre-duel snapshot.
        final Set<PotionEffectType> allowedTypes = new HashSet<>();
        if (restorePotionEffects && effects != null) {
            for (final PotionEffect effect : effects) {
                if (effect != null) {
                    player.addPotionEffect(effect);
                    allowedTypes.add(effect.getType());
                }
            }
        }

        // Final cleanup: remove any effect not in the pre-duel saved set.
        for (final PotionEffect active : new ArrayList<>(player.getActivePotionEffects())) {
            if (active != null && !allowedTypes.contains(active.getType())) {
                try {
                    player.removePotionEffect(active.getType());
                } catch (Throwable ignored) {}
            }
        }

        player.setHealth(Math.min(health, maxHealth));
        player.setFoodLevel(hunger);
        InventoryUtil.fillFromMap(player.getInventory(), items);
        InventoryUtil.addOrDrop(player, extra);
        player.updateInventory();
    }
}
