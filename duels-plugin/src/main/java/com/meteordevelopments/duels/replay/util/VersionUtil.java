package com.meteordevelopments.duels.replay.util;

import java.lang.reflect.Field;
import java.util.Arrays;


import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class VersionUtil {

	public static VersionEnum VERSION;

	static {
		VERSION = VersionEnum.parseVersion();
	}

	public static boolean isCompatible(VersionEnum ve){
		return VERSION.equals(ve);
	}
	
	public static boolean isAbove(VersionEnum ve) {		
		return VERSION.getOrder() >= ve.getOrder();
	}
	
	public static boolean isBelow(VersionEnum ve) {
		return VERSION.getOrder() <= ve.getOrder();
	}
	
	public static boolean isBetween(VersionEnum ve1, VersionEnum ve2) {
		return isAbove(ve1) && isBelow(ve2);
	}
	
	public static Class<?> getNmsClass(String nmsClassName) throws ClassNotFoundException {
	    return Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + "." + nmsClassName);
	}
	
	public static void sendPacket(Player p, Object packet){
		try{
			Object nmsPlayer = p.getClass().getMethod("getHandle").invoke(p);
			Field playerConnectionField = nmsPlayer.getClass().getField("playerConnection");
			Object pConnection = playerConnectionField.get(nmsPlayer);
			pConnection.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server." + VersionUtil.VERSION + ".Packet")).invoke(pConnection, packet);
		}catch(Exception e){
			e.printStackTrace();
		}

	}
	

	
	public enum VersionEnum {

		V1_8(1),
		V1_9(2),
		V1_10(3),
		V1_11(4),
		V1_12(5),
		V1_13(6),
		V1_14(7),
		V1_15(8),
		V1_16(9),
		V1_17(10),
		V1_18(11),
		V1_19(12),
		V1_20(13),
		V1_21(14),
        V1_21_10(15),
        V26_1(16),
        V26_2(17);

		
		private int order;

		VersionEnum(int order) {
			this.order = order;
		}
		
		public int getOrder() {
			return order;
		}

        public int getPatch() {
            String[] parts = this.name().split("_");
            if (parts.length == 3) {
                try {
                    return Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
            return 0;
        }

		public static VersionEnum parseVersion() {
			String bukkitVersion = Bukkit.getBukkitVersion();
			String version = (bukkitVersion != null ? bukkitVersion.split("-")[0] : "1.21");
			String[] parts = version.split("\\.");

			int majorInt = parts.length > 0 ? extractNumber(parts[0], 1) : 1;
			int minorInt = parts.length > 1 ? extractNumber(parts[1], 0) : 0;
			int patchInt = parts.length > 2 ? extractNumber(parts[2], 0) : 0;

			final int finalPatch = patchInt;
			String majorMinor = majorInt + "_" + minorInt;
			String majorMinorPatch = majorMinor + "_" + patchInt;

			return Arrays.stream(VersionEnum.values())
					.filter(v -> v.toString().equals("V" + majorMinorPatch)
							|| (v.toString().startsWith("V" + majorMinor + "_") && v.getPatch() != 0 && v.getPatch() <= finalPatch))
					.max((v1, v2) -> Integer.compare(v1.getOrder(), v2.getOrder()))
					.orElseGet(() -> {
						try {
							return VersionEnum.valueOf("V" + majorMinor);
						} catch (IllegalArgumentException e) {
							// Log warning on startup and use the latest supported version as fallback
							Bukkit.getLogger().warning("Detected untested or unsupported Minecraft version: " + bukkitVersion + ". Falling back to latest known version.");
							return VersionEnum.values()[VersionEnum.values().length - 1];
						}
					});
		}

		private static int extractNumber(String s, int defaultValue) {
			if (s == null || s.isEmpty()) return defaultValue;
			StringBuilder digits = new StringBuilder();
			for (char c : s.toCharArray()) {
				if (Character.isDigit(c)) {
					digits.append(c);
				} else if (digits.length() > 0) {
					break;
				}
			}
			if (digits.length() == 0) return defaultValue;
			try {
				return Integer.parseInt(digits.toString());
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		}

	}
}



