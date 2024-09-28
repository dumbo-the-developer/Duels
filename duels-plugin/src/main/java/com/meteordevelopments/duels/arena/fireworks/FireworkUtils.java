package com.meteordevelopments.duels.arena.fireworks;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;

import java.util.HashMap;
import java.util.Map;

public class FireworkUtils {
    private static final Map<String, Color> colorMap = new HashMap<>();
    private static final Map<String, FireworkEffect.Type> typeMap = new HashMap<>();

    static {
        colorMap.put("AQUA", Color.AQUA);
        colorMap.put("BLACK", Color.BLACK);
        colorMap.put("BLUE", Color.BLUE);
        colorMap.put("FUCHSIA", Color.FUCHSIA);
        colorMap.put("GRAY", Color.GRAY);
        colorMap.put("GREEN", Color.GREEN);
        colorMap.put("LIME", Color.LIME);
        colorMap.put("MAROON", Color.MAROON);
        colorMap.put("NAVY", Color.NAVY);
        colorMap.put("OLIVE", Color.OLIVE);
        colorMap.put("ORANGE", Color.ORANGE);
        colorMap.put("PURPLE", Color.PURPLE);
        colorMap.put("RED", Color.RED);
        colorMap.put("SILVER", Color.SILVER);
        colorMap.put("TEAL", Color.TEAL);
        colorMap.put("WHITE", Color.WHITE);
        colorMap.put("YELLOW", Color.YELLOW);

        typeMap.put("BALL", FireworkEffect.Type.BALL);
        typeMap.put("BALL_LARGE", FireworkEffect.Type.BALL_LARGE);
        typeMap.put("BURST", FireworkEffect.Type.BURST);
        typeMap.put("CREEPER", FireworkEffect.Type.CREEPER);
        typeMap.put("STAR", FireworkEffect.Type.STAR);
    }

    public static Color getColor(String colorName) {
        return colorMap.getOrDefault(colorName.toUpperCase(), Color.WHITE); // default to WHITE if not found
    }

    public static FireworkEffect.Type getType(String typeName) {
        return typeMap.getOrDefault(typeName.toUpperCase(), FireworkEffect.Type.BALL); // default to BALL if not found
    }
}
