package com.meteordevelopments.duels.request;

import com.meteordevelopments.duels.party.Party;
import lombok.Getter;
import com.meteordevelopments.duels.api.arena.Arena;
import com.meteordevelopments.duels.api.kit.Kit;
import com.meteordevelopments.duels.api.request.Request;
import com.meteordevelopments.duels.setting.Settings;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class RequestImpl implements Request {

    @Getter
    private final long creation;
    @Getter
    private final UUID sender;
    @Getter
    private final UUID target;
    @Getter
    private final Settings settings;

    RequestImpl(final Player sender, final Player target, final Settings setting) {
        this.creation = System.currentTimeMillis();
        this.sender = sender.getUniqueId();
        this.target = target.getUniqueId();
        this.settings = setting.lightCopy();
    }

    @Nullable
    @Override
    public Kit getKit() {
        return settings.getKit();
    }

    @Nullable
    @Override
    public Arena getArena() {
        return settings.getArena();
    }

    @Override
    public boolean canBetItems() {
        return settings.isItemBetting();
    }

    @Override
    public int getBet() {
        return settings.getBet();
    }

    public Party getSenderParty() {
        return settings.getSenderParty();
    }

    public Party getTargetParty() {
        return settings.getTargetParty();
    }

    public boolean isPartyDuel() {
        return settings.isPartyDuel();
    }
}
