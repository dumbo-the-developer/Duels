package com.meteordevelopments.duels.core.arena;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegionSelectionManager {

    private final Map<UUID, Selection> selections = new HashMap<>();

    public void setPos1(Player player, Location location) {
        getSelection(player).pos1 = location;
    }

    public void setPos2(Player player, Location location) {
        getSelection(player).pos2 = location;
    }

    public Selection getSelection(Player player) {
        return selections.computeIfAbsent(player.getUniqueId(), k -> new Selection());
    }

    public static class Selection {
        public Location pos1;
        public Location pos2;

        public boolean isComplete() {
            return pos1 != null && pos2 != null && pos1.getWorld() != null && pos1.getWorld().equals(pos2.getWorld());
        }
    }
}
