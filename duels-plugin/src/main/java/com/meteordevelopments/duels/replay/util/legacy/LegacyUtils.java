package com.meteordevelopments.duels.replay.util.legacy;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

public class LegacyUtils {

    public static String getInventoryTitle(InventoryClickEvent event) {
        return event.getView().getTitle();
    }
}
