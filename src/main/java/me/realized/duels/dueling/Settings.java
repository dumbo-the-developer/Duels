package me.realized.duels.dueling;

import java.util.UUID;

public class Settings {

    private final UUID target;

    private String kit;
    private String arena;

    public Settings(UUID target) {
        this.target = target;
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
}
