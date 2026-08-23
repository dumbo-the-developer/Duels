package com.meteordevelopments.duels.replay.playback.session;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.replay.util.LogUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class ReplaySpectatorStorage {

    private static File getSessionsFolder() {
        File folder = new File(DuelsPlugin.getInstance().getDataFolder(), "replays/active_sessions");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

    private static File getFile(UUID uuid) {
        return new File(getSessionsFolder(), uuid.toString() + ".yml");
    }

    public static void saveSnapshot(Player player, Location startLocation, int level, float exp) {
        try {
            File file = getFile(player.getUniqueId());
            YamlConfiguration cfg = new YamlConfiguration();

            cfg.set("uuid", player.getUniqueId().toString());
            cfg.set("name", player.getName());
            cfg.set("location", startLocation != null ? startLocation : player.getLocation());
            cfg.set("gamemode", player.getGameMode().name());
            cfg.set("level", level);
            cfg.set("exp", exp);
            cfg.set("health", player.getHealth());
            cfg.set("food", player.getFoodLevel());
            cfg.set("allowFlight", player.getAllowFlight());
            cfg.set("flying", player.isFlying());

            cfg.set("contents", player.getInventory().getContents());
            cfg.set("armor", player.getInventory().getArmorContents());
            try {
                cfg.set("offhand", player.getInventory().getItemInOffHand());
            } catch (Throwable ignored) {}

            cfg.save(file);
        } catch (Exception e) {
            LogUtils.log("Failed to save replay spectator snapshot for " + player.getName() + ": " + e.getMessage());
        }
    }

    public static boolean hasSnapshot(UUID uuid) {
        return getFile(uuid).exists();
    }

    @SuppressWarnings("unchecked")
    public static boolean restoreSnapshot(Player player) {
        File file = getFile(player.getUniqueId());
        if (!file.exists()) {
            return false;
        }

        try {
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

            Location loc = (Location) cfg.get("location");
            if (loc != null && loc.getWorld() != null) {
                player.teleport(loc);
            }

            player.getInventory().clear();

            Object contentsObj = cfg.get("contents");
            if (contentsObj instanceof List) {
                List<?> list = (List<?>) contentsObj;
                ItemStack[] items = list.toArray(new ItemStack[0]);
                player.getInventory().setContents(items);
            } else if (contentsObj instanceof ItemStack[]) {
                player.getInventory().setContents((ItemStack[]) contentsObj);
            }

            Object armorObj = cfg.get("armor");
            if (armorObj instanceof List) {
                List<?> list = (List<?>) armorObj;
                ItemStack[] armor = list.toArray(new ItemStack[0]);
                player.getInventory().setArmorContents(armor);
            } else if (armorObj instanceof ItemStack[]) {
                player.getInventory().setArmorContents((ItemStack[]) armorObj);
            }

            ItemStack offhand = cfg.getItemStack("offhand");
            if (offhand != null) {
                try {
                    player.getInventory().setItemInOffHand(offhand);
                } catch (Throwable ignored) {}
            }

            if (cfg.contains("gamemode")) {
                try {
                    player.setGameMode(GameMode.valueOf(cfg.getString("gamemode")));
                } catch (Exception ignored) {}
            }
            if (cfg.contains("level")) player.setLevel(cfg.getInt("level"));
            if (cfg.contains("exp")) player.setExp((float) cfg.getDouble("exp"));
            if (cfg.contains("health")) {
                double health = cfg.getDouble("health", player.getMaxHealth());
                player.setHealth(Math.min(health, player.getMaxHealth()));
            }
            if (cfg.contains("food")) player.setFoodLevel(cfg.getInt("food", 20));

            boolean allowFlight = cfg.getBoolean("allowFlight", false);
            player.setAllowFlight(allowFlight);
            player.setFlying(cfg.getBoolean("flying", false) && allowFlight);

            player.setCollidable(true);
            player.setInvisible(false);

            file.delete();
            return true;
        } catch (Exception e) {
            LogUtils.log("Failed to restore replay spectator snapshot for " + player.getName() + ": " + e.getMessage());
            file.delete();
            return false;
        }
    }

    public static void deleteSnapshot(UUID uuid) {
        File file = getFile(uuid);
        if (file.exists()) {
            file.delete();
        }
    }

    public static void checkAndRestoreAllOnline() {
        File folder = getSessionsFolder();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File f : files) {
            try {
                String name = f.getName().replace(".yml", "");
                UUID uuid = UUID.fromString(name);
                Player p = Bukkit.getPlayer(uuid);
                if (p != null && p.isOnline()) {
                    restoreSnapshot(p);
                }
            } catch (Exception ignored) {}
        }
    }
}
