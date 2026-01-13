package com.meteordevelopments.duels.party;

import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import lombok.Getter;

@Getter
public class PartyMember {

    private final long creation;

    private final UUID uuid;
    private final String name;
    private final Party party;

    private long lastLogout;

    public PartyMember(final Player player, final Party party) {
        this.creation = System.currentTimeMillis();
        this.uuid = player.getUniqueId();
        this.name = player.getName();
        this.party = party;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public boolean isOnline() {
        return getPlayer() != null;
    }

    public void setLastLogout() {
        lastLogout = System.currentTimeMillis();
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        return uuid.equals(((PartyMember) other).uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
