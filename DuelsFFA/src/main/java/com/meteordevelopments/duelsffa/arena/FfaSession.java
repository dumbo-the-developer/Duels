package com.meteordevelopments.duelsffa.arena;

import com.meteordevelopments.duelsffa.util.InventorySnapshot;
import org.bukkit.Location;

import java.util.UUID;

public class FfaSession {

    private final UUID playerId;
    private final String arenaName;
    private final String kitName;
    private final boolean noKit;
    private final InventorySnapshot snapshot;
    private final Location returnLocation;

    public FfaSession(final UUID playerId, final String arenaName, final String kitName, final boolean noKit,
                      final InventorySnapshot snapshot, final Location returnLocation) {
        this.playerId = playerId;
        this.arenaName = arenaName;
        this.kitName = kitName;
        this.noKit = noKit;
        this.snapshot = snapshot;
        this.returnLocation = returnLocation;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getArenaName() {
        return arenaName;
    }

    public String getKitName() {
        return kitName;
    }

    public boolean isNoKit() {
        return noKit;
    }

    public InventorySnapshot getSnapshot() {
        return snapshot;
    }

    public Location getReturnLocation() {
        return returnLocation;
    }
}
