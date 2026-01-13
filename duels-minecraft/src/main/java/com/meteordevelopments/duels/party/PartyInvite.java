package com.meteordevelopments.duels.party;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PartyInvite {

    @Getter
    private final long creation;

    @Getter
    private final UUID sender;
    @Getter
    private final UUID receiver;
    @Getter
    private final Party party;

    public PartyInvite(final Player sender, final Player receiver, final Party party) {
        this.creation = System.currentTimeMillis();
        this.sender = sender.getUniqueId();
        this.receiver = receiver.getUniqueId();
        this.party = party;
    }
}
