package me.realized._duels.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

abstract class KitEvent extends Event {

    private final String name;
    private final Player player;

    KitEvent(String name, Player player) {
        this.name = name;
        this.player = player;
    }

    public String getName() {
        return name;
    }

    public Player getPlayer() {
        return player;
    }
}
