package com.meteordevelopments.duels.party;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

public class Party {

    @Getter
    private final long creation;
    @Getter
    @Setter
    private boolean friendlyFire;
    @Getter
    @Setter(value = AccessLevel.PACKAGE)
    private boolean removed;

    @Getter
    private PartyMember owner;

    private final Map<UUID, PartyMember> members = new HashMap<>();

    public Party(final Player owner) {
        this.creation = System.currentTimeMillis();
        add(owner);
        this.owner = get(owner);
    }

    public PartyMember get(final String name) {
        return members.values().stream().filter(member -> member.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public PartyMember get(final Player player) {
        return members.get(player.getUniqueId());
    }

    public boolean isMember(final Player player) {
        return get(player) != null;
    }

    public boolean add(final Player player) {
        PartyMember member = get(player);

        if (member != null) {
            return false;
        }

        members.put(player.getUniqueId(), new PartyMember(player, this));
        return true;
    }

    public boolean remove(final PartyMember member) {
        return members.remove(member.getUuid()) != null;
    }

    public boolean remove(final Player player) {
        return members.remove(player.getUniqueId()) != null;
    }

    public boolean isOwner(final Player player) {
        return owner != null && owner.equals(get(player));
    }

    public void setOwner(final Player other) {
        PartyMember member = get(other);

        if (member == null) {
            return;
        }

        this.owner = member;
    }

    public int size() {
        return members.size();
    }

    public Collection<PartyMember> getMembers() {
        return members.values();
    }

    public List<Player> getOnlineMembers() {
        return members.values().stream().map(PartyMember::getPlayer).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
