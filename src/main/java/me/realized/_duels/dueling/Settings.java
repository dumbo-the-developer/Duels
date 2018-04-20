package me.realized._duels.dueling;

import java.util.UUID;
import org.bukkit.Location;

public class Settings {

    private final UUID target;
    private final Location base;

    private String kit;
    private String arena;

    public Settings(UUID target, Location base) {
        this.target = target;
        this.base = base;
    }

    public UUID getTarget() {
        return target;
    }

    public String getKit() {
        return kit;
    }

    public void setKit(String kit) {
        this.kit = kit;
    }

    public String getArena() {
        return arena;
    }

    public void setArena(String arena) {
        this.arena = arena;
    }

    public Location getBase() {
        return base;
    }
}
