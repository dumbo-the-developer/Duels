package me.realized.duels.utilities;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.realized.duels.Core;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerUtil {

    public static boolean canTeleportTo(Player player, Location location) {
        PlayerTeleportEvent teleportEvent = new PlayerTeleportEvent(player, player.getLocation(), location);
        Bukkit.getPluginManager().callEvent(teleportEvent);

        return !teleportEvent.isCancelled();
    }

    public static void pm(String msg, Player... players) {
        for (Player player : players) {
            player.sendMessage(StringUtil.color(msg));
        }
    }

    private static void extinguish(final Player player) {
        new BukkitRunnable() {

            @Override
            public void run() {
                player.setFireTicks(0);
            }
        }.runTaskLater(Core.getInstance(), 1L);
    }

    public static void reset(Player player, boolean extinguish) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        player.setHealth(20.0D);
        player.setFoodLevel(20);
        clearInventory(player);

        if (extinguish) {
            extinguish(player);
        }
    }

    public static void reset(boolean extinguish, Player... players) {
        for (Player player : players) {
            reset(player, extinguish);
        }
    }

    public static void setInventory(final Player player, final ItemStack[] inventory, final ItemStack[] armor, boolean delayed) {
        if (!delayed) {
            player.getInventory().setContents(inventory);
            player.getInventory().setArmorContents(armor);
            player.updateInventory();
        } else {
            new BukkitRunnable() {

                @Override
                public void run() {
                    player.getInventory().setContents(inventory);
                    player.getInventory().setArmorContents(armor);
                    player.updateInventory();
                }
            }.runTaskLater(Core.getInstance(), 1L);
        }
    }

    private static void clearInventory(Player player) {
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().clear();
        player.updateInventory();
    }

    public static boolean hasEmptyInventory(Player player) {
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && item.getType() != Material.AIR) {
                return false;
            }
        }

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);

            if (item != null && item.getType() != Material.AIR) {
                return false;
            }
        }

        return true;
    }

    private static ApplicableRegionSet getRegions(Location location) {
        Core instance = Core.getInstance();

        if (instance.getWorldGuard() == null) {
            return null;
        }

        ApplicableRegionSet regions = instance.getWorldGuard().getRegionManager(location.getWorld()).getApplicableRegions(location);

        if (regions.getRegions().isEmpty()) {
            return null;
        }

        return regions;
    }

    public static boolean isInRegion(Player player, String name) {
        ApplicableRegionSet regions = getRegions(player.getLocation());

        if (regions == null) {
            return false;
        }

        for (ProtectedRegion region : regions) {
            if (region.getId().equals(name)) {
                return true;
            }
        }

        return false;
    }

    public static void refreshChunk(Location... locations) {
        for (Location location : locations) {
            if (location == null || location.getWorld() == null) {
                continue;
            }

            location.getChunk().load();
        }
    }
}
