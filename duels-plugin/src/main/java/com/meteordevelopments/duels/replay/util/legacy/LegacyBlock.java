package com.meteordevelopments.duels.replay.util.legacy;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class LegacyBlock {

    private static Method setTypeIdAndDataMethod;
    private static Method sendBlockChangeMethod;

    static {
        try {
            setTypeIdAndDataMethod = Block.class.getMethod("setTypeIdAndData", int.class, byte.class, boolean.class);
        } catch (Throwable ignored) {
        }
        try {
            sendBlockChangeMethod = Player.class.getMethod("sendBlockChange", Location.class, int.class, byte.class);
        } catch (Throwable ignored) {
        }
    }

    public static void setTypeIdAndData(Block block, int typeId, byte data, boolean applyPhysics) {
        if (setTypeIdAndDataMethod != null && block != null) {
            try {
                setTypeIdAndDataMethod.invoke(block, typeId, data, applyPhysics);
            } catch (Throwable ignored) {
            }
        }
    }

    public static void sendBlockChange(Player player, Location loc, int typeId, byte data) {
        if (sendBlockChangeMethod != null && player != null && loc != null) {
            try {
                sendBlockChangeMethod.invoke(player, loc, typeId, data);
            } catch (Throwable ignored) {
            }
        }
    }
}
