package me.realized.duels.dueling;

import java.util.UUID;

public class Request {

    private final UUID sender;
    private final UUID target;
    private final String kit;
    private final long time;

    public Request(UUID sender, UUID target, String kit) {
        this.sender = sender;
        this.target = target;
        this.kit = kit;
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

    public long getTime() {
        return time;
    }
}
