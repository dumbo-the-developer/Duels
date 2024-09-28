package com.meteordevelopments.duels.util;

import org.bukkit.Bukkit;

public class Version {

    private static String getMinecraftVersion() {
        // Example to get the Minecraft server version
        return Bukkit.getServer().getBukkitVersion(); // Returns something like "1.20.6"
    }

    public static boolean isVersionEqualOrAbove(String version, String targetVersion) {
        String[] versionParts = version.split("\\.");
        String[] targetParts = targetVersion.split("\\.");

        for (int i = 0; i < Math.max(versionParts.length, targetParts.length); i++) {
            int vPart = i < versionParts.length ? Integer.parseInt(versionParts[i]) : 0;
            int tPart = i < targetParts.length ? Integer.parseInt(targetParts[i]) : 0;

            if (vPart > tPart) {
                return true;
            } else if (vPart < tPart) {
                return false;
            }
        }
        return true; // versions are equal
    }

    // Overloaded method to check against the server's current version
    public static boolean isCurrentVersionEqualOrAbove(String targetVersion) {
        return isVersionEqualOrAbove(getMinecraftVersion(), targetVersion);
    }

}
