package me.realized.duels.dueling;

import java.util.UUID;

public class Request {

    private final UUID sender;
    private final UUID target;
    private final String kit;
    private final String arena;
    private final long time;

    public Request(UUID sender, UUID target, Settings settings) {
        this.sender = sender;
        this.target = target;
        this.kit = settings.getKit();
        this.arena = settings.getArena();
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

    public String getArena() {
        return arena;
    }

    public long getTime() {
        return time;
    }
}
