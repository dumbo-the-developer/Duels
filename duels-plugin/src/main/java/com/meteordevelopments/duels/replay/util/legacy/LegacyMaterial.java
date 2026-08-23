package com.meteordevelopments.duels.replay.util.legacy;

import org.bukkit.Material;
import java.lang.reflect.Method;

public class LegacyMaterial {

    private static Method getMaterialIntMethod;

    static {
        try {
            getMaterialIntMethod = Material.class.getMethod("getMaterial", int.class);
        } catch (Throwable ignored) {
        }
    }

    public static Material getMaterialById(int id) {
        if (getMaterialIntMethod != null) {
            try {
                return (Material) getMaterialIntMethod.invoke(null, id);
            } catch (Throwable ignored) {
            }
        }
        return Material.AIR;
    }
}
