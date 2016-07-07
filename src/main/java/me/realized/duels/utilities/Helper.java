package me.realized.duels.utilities;

import me.realized.duels.Core;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class Helper {

    private static final Map<String, Profile> uuidCache = new HashMap<>();

    @SuppressWarnings("deprecation")
    public static UUID getUUID(String username) {
        Profile profile = get(username);

        if (profile != null) {
            return profile.getUUID();
        }

        if (Bukkit.getOnlineMode() || Bukkit.spigot().getConfig().getBoolean("settings.bungeecord")) {
            if (Bukkit.getPlayerExact(username) != null) {
                return Bukkit.getPlayerExact(username).getUniqueId();
            }

            if (Bukkit.getOfflinePlayer(username).hasPlayedBefore()) {
                UUID uuid = Bukkit.getOfflinePlayer(username).getUniqueId();
                uuidCache.put(username, new Profile(uuid));
                return uuid;
            }
        }

        return null;
    }

    private static Profile get(String name) {
        Profile profile = uuidCache.get(name.toLowerCase());

        if (profile == null) {
            return null;
        }

        if (profile.getTime() + 1000 * 300 - System.currentTimeMillis() <= 0) {
            return null;
        }

        return profile;
    }

    private static class Profile {

        private final long time;
        private final UUID uuid;

        public Profile(UUID uuid) {
            this.time = System.currentTimeMillis();
            this.uuid = uuid;
        }

        public long getTime() {
            return time;
        }

        public UUID getUUID() {
            return uuid;
        }
    }

    private static final Pattern ALPHANUMERIC = Pattern.compile("^[a-zA-Z0-9]*$");

    public static boolean isAlphanumeric(String input) {
        return ALPHANUMERIC.matcher(input).matches();
    }

    public static String toHumanReadableTime(long ms) {
        if (ms < 1000) {
            return "0 second";
        }

        long seconds = ms / 1000 + (ms % 1000 > 0 ? 1 : 0);
        long years = seconds / 31556952;
        seconds -= years * 31556952;
        long months = seconds / 2592000;
        seconds -= months * 2592000;
        long weeks = seconds / 604800;
        seconds -= weeks * 604800;
        long days = seconds / 86400;
        seconds -= days * 86400;
        long hours = seconds / 3600;
        seconds -= hours * 3600;
        long minutes = seconds / 60;
        seconds -= minutes * 60;

        StringBuilder builder = new StringBuilder();

        if (years > 0) {
            builder.append(years).append(years > 1 ? " years" : " year");
        }

        if (months > 0) {
            if (years > 0) {
                builder.append(" ");
            }

            builder.append(months).append(months > 1 ? " months" : " month");
        }

        if (weeks > 0) {
            if (years + months > 0) {
                builder.append(" ");
            }

            builder.append(weeks).append(weeks > 1 ? " weeks" : " week");
        }

        if (days > 0) {
            if (years + months + weeks > 0) {
                builder.append(" ");
            }

            builder.append(days).append(days > 1 ? " days" : " day");
        }

        if (hours > 0) {
            if (years + months + weeks + days > 0) {
                builder.append(" ");
            }

            builder.append(hours).append(hours > 1 ? " hours" : " hour");
        }

        if (minutes > 0) {
            if (years + months + weeks + days + hours > 0) {
                builder.append(" ");
            }

            builder.append(minutes).append(minutes > 1 ? " minutes" : " minute");
        }

        if (seconds > 0) {
            if (years + months + weeks + days + hours + minutes > 0) {
                builder.append(" ");
            }

            builder.append(seconds).append(seconds > 1 ? " seconds" : " second");
        }

        return builder.toString();
    }

    public static String replaceWithArgs(String txt, Object... parameters) {
        for (int i = 0; i < parameters.length; i++) {
            if (parameters.length > i + 1) {
                String current = String.valueOf(parameters[i]);
                String next = String.valueOf(parameters[i + 1]);
                txt = txt.replace(current, next);
                i++;
            }
        }

        return txt;
    }

    public static String join(List<String> list, String joiner) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < list.size() - 1; i++) {
            builder.append(list.get(i)).append(joiner);
        }

        return builder.append(list.get(list.size() - 1)).toString();
    }

    public static String color(String txt) {
        return ChatColor.translateAlternateColorCodes('&', txt);
    }

    public static String format(Location location) {
        if (location == null || location.getWorld() == null) {
            return "Invalid world or location! (Please re-set spawnpoints)";
        }

        return "(" + location.getWorld().getName() + ", " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ")";
    }

    public static boolean isInt(String input, boolean negative) {
        try {
            int result = Integer.parseInt(input);
            return !(!negative && result < 0);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean canTeleportTo(Player player, Location location) {
        PlayerTeleportEvent event = new PlayerTeleportEvent(player, player.getLocation(), location);
        Bukkit.getPluginManager().callEvent(event);
        return !(!player.getLocation().equals(location) && event.getFrom().equals(event.getTo())) && !event.isCancelled();

    }

    public static void pm(String msg, Player... players) {
        for (Player player : players) {
            player.sendMessage(color(msg));
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

        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setItemOnCursor(null);

        Inventory top = player.getOpenInventory().getTopInventory();

        if (top != null && top.getType() == InventoryType.CRAFTING) {
            top.clear();
        }

        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().clear();
        player.updateInventory();

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

    public static void refreshChunk(Location... locations) {
        for (Location location : locations) {
            if (location == null || location.getWorld() == null) {
                continue;
            }

            Chunk chunk = location.getChunk();

            if (!chunk.isLoaded()) {
                chunk.load();
            }
        }
    }

    @SuppressWarnings("deprecation")
    public static void updatePosition(Player player, Location location) {
        location = location.clone().subtract(0, 1, 0);
        Block block = location.getBlock();

        if (!block.getType().isSolid()) {
            return;
        }

        player.sendBlockChange(location, block.getType(), (byte) 0);
    }
}