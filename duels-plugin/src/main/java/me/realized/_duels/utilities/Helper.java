/*
 * This file is part of Duels, licensed under the MIT License.
 *
 * Copyright (c) Realized
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.realized._duels.utilities;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Pattern;
import me.realized._duels.Core;
import me.realized._duels.arena.Arena;
import me.realized._duels.configuration.ConfigManager;
import me.realized._duels.configuration.ConfigType;
import me.realized._duels.configuration.MainConfig;
import me.realized._duels.configuration.MessagesConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

public class Helper {

    private static final Pattern ALPHANUMERIC = Pattern.compile("^[a-zA-Z0-9_-]*$");
    private static Map<String, Profile> uuidCache = new HashMap<>();

    @SuppressWarnings("deprecation")
    public static UUID getUUID(String username) {
        Profile profile = get(username);

        if (profile != null) {
            return profile.getUUID();
        }

        Player online = Bukkit.getPlayerExact(username);

        if (online != null) {
            return online.getUniqueId();
        }

        OfflinePlayer offline = Bukkit.getOfflinePlayer(username);

        if (offline != null && offline.hasPlayedBefore()) {
            UUID uuid = offline.getUniqueId();
            uuidCache.put(username, new Profile(uuid));
            return uuid;
        }

        return null;
    }

    private static Profile get(String name) {
        Profile profile = uuidCache.get(name);

        if (profile == null) {
            return null;
        }

        if (profile.getTime() + 60000L - System.currentTimeMillis() <= 0) {
            return null;
        }

        return profile;
    }

    public static boolean isAlphanumeric(String input) {
        return ALPHANUMERIC.matcher(input.replace(" ", "")).matches();
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

    public static String join(String[] args, int start, int end, String joiner) {
        return join(Arrays.asList(Arrays.copyOfRange(args, start, end)), joiner);
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

    public static void pm(CommandSender sender, String in, boolean isConfigMessage, Object... replacers) {
        if (isConfigMessage) {
            MessagesConfig messages = (MessagesConfig) ConfigManager.getConfig(ConfigType.MESSAGES);
            String string = messages.getString(in);

            if (string != null) {
                handleSound(sender, string);
                sender.sendMessage(color(replaceWithArgs(string, replacers)));
                return;
            }

            List<String> list = messages.getList(in);

            if (list != null) {
                for (String txt : list) {
                    handleSound(sender, txt);
                    sender.sendMessage(color(replaceWithArgs(txt, replacers)));
                }
            }
        } else {
            handleSound(sender, in);
            sender.sendMessage(color(replaceWithArgs(in, replacers)));
        }
    }

    public static void broadcast(Arena arena, String in, boolean isConfigMessage, Object... replacers) {
        if (isConfigMessage) {
            MessagesConfig messages = (MessagesConfig) ConfigManager.getConfig(ConfigType.MESSAGES);
            String string = messages.getString(in);
            List<String> list = messages.getList(in);

            if (string != null) {
                handleArenaBroadcast(arena, string, color(replaceWithArgs(string, replacers)));
            } else if (list != null) {
                for (String txt : list) {
                    handleArenaBroadcast(arena, txt, color(replaceWithArgs(txt, replacers)));
                }
            }
        } else {
            handleArenaBroadcast(arena, in, color(replaceWithArgs(in, replacers)));
        }
    }

    public static void broadcast(String in, Object... replacers) {
        MessagesConfig messages = (MessagesConfig) ConfigManager.getConfig(ConfigType.MESSAGES);
        String string = messages.getString(in);
        List<String> list = messages.getList(in);

        if (string != null) {
            Bukkit.broadcastMessage(color(replaceWithArgs(string, replacers)));
        } else if (list != null) {
            for (String txt : list) {
                Bukkit.broadcastMessage(color(replaceWithArgs(txt, replacers)));
            }
        }
    }

    private static void handleArenaBroadcast(Arena arena, String ogMessage, String message) {
        for (UUID uuid : arena.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);

            if (player != null) {
                handleSound(player, ogMessage);
                player.sendMessage(message);
            }
        }
    }

    private static void handleSound(CommandSender sender, String msg) {
        if (!(sender instanceof Player)) {
            return;
        }

        MainConfig config = Core.getInstance().getConfiguration();

        for (MainConfig.CustomSound sound : config.getSounds()) {
            sound.handleMessage((Player) sender, msg);
        }
    }

    public static void handleException(Object self, Exception ex) {
        Core.getInstance().logToFile(self, "ERROR: " + ex.getMessage(), Level.WARNING);

        StackTraceElement element = ex.getStackTrace()[0];
        Core.getInstance().logToFile(self, "occured in " + element.getClassName() + "#" + element.getMethodName() + " at line " + element.getLineNumber(), Level.WARNING);
    }

    public static boolean isPre1_9() {
        return (Bukkit.getVersion().contains("1.8") || Bukkit.getVersion().contains("1.7"));
    }

    public static boolean isPre1_8() {
        return Bukkit.getVersion().contains("1.7");
    }

    static class Profile {

        private final long time;
        private final UUID uuid;

        Profile(UUID uuid) {
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
}