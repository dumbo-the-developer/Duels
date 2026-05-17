package com.meteordevelopments.duels.data;

import lombok.Getter;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.core.arena.ArenaImpl;

import java.util.*;

public class ArenaData {

    @Getter
    private String name;
    private boolean disabled;
    private ItemData displayed;
    private Set<String> kits = new HashSet<>();
    private Map<Integer, LocationData> positions = new HashMap<>();
    private LocationData regionPos1, regionPos2;

    private ArenaData() {
    }

    public ArenaData(final ArenaImpl arena) {
        this.name = arena.getName();
        this.disabled = arena.isDisabled();
        // Always serialize the original display item with {status} placeholder
        this.displayed = ItemData.fromItemStack(arena.getDisplayedOriginal());
        arena.getKits().forEach(kit -> this.kits.add(kit.getName()));
        arena.getPositions().entrySet()
                .stream().filter(entry -> entry.getValue().getWorld() != null).forEach(entry -> positions.put(entry.getKey(), LocationData.fromLocation(entry.getValue())));
        if (arena.getRegionPos1() != null) {
            this.regionPos1 = LocationData.fromLocation(arena.getRegionPos1());
        }
        if (arena.getRegionPos2() != null) {
            this.regionPos2 = LocationData.fromLocation(arena.getRegionPos2());
        }
    }

    public ArenaImpl toArena(final DuelsPlugin plugin) {
        final ArenaImpl arena = new ArenaImpl(plugin, name, disabled);

        // Restore display item if it was saved (use setDisplayedWithoutSave to avoid triggering save during loading)
        if (this.displayed != null) {
            org.bukkit.inventory.ItemStack displayedItem = this.displayed.toItemStack();
            if (displayedItem != null) {
                arena.setDisplayedWithoutSave(displayedItem);
            }
        }

        // Manually bind kits and add locations to prevent saveArenas being called
        kits.stream().map(name -> plugin.getKitManager().get(name)).filter(Objects::nonNull).forEach(kit -> arena.getKits().add(kit));
        positions.forEach((key, value) -> arena.getPositions().put(key, value.toLocation()));
        if (regionPos1 != null) {
            arena.setRegionPos1(regionPos1.toLocation());
        }
        if (regionPos2 != null) {
            arena.setRegionPos2(regionPos2.toLocation());
        }

        arena.refreshGui(arena.isAvailable());
        
        return arena;
    }
}
