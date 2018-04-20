package me.realized._duels.dueling;

import java.util.UUID;
import org.bukkit.Location;

public class Request {

    private final UUID sender;
    private final UUID target;
    private String kit;
    private final String arena;
    private final Location base;
    private final long time;

    public Request(UUID sender, UUID target, Settings settings) {
        this.sender = sender;
        this.target = target;
        this.kit = settings.getKit();
        this.arena = settings.getArena();
        this.base = settings.getBase();
        this.time = System.currentTimeMillis();
    }

    public UUID getSender() {
        return sender;
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

    public Location getBase() {
        return base;
    }

    public long getTime() {
        return time;
    }
}
