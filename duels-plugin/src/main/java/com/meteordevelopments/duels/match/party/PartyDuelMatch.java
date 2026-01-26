package com.meteordevelopments.duels.match.party;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.arena.ArenaImpl;
import com.meteordevelopments.duels.kit.KitImpl;
import com.meteordevelopments.duels.match.DuelMatch;
import com.meteordevelopments.duels.party.Party;
import com.meteordevelopments.duels.queue.Queue;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;
public class PartyDuelMatch extends DuelMatch {

    @Getter
    private final Map<Player, Party> playerToParty = new HashMap<>();
    @Getter
    private final Multimap<Party, Player> partyToPlayers = HashMultimap.create();
    private final Map<Party, Integer> alivePlayers = new HashMap<>();

    public PartyDuelMatch(final DuelsPlugin plugin, final ArenaImpl arena, final KitImpl kit, final Map<UUID, List<ItemStack>> items, final int bet, final Queue source) {
        super(plugin,arena, kit, items, bet, source);
    }

    public Set<Party> getAllParties() {
        return partyToPlayers.keySet();
    }

    public List<String> getNames(final Party party) {
        final Collection<Player> members = partyToPlayers.asMap().get(party);

        if (members == null) {
            return Collections.emptyList();
        }

        return members.stream().map(Player::getName).collect(Collectors.toList());
    }
    
    @Override
    public void addPlayer(final Player player) {
        super.addPlayer(player);

        final Party party = partyManager.get(player);
        playerToParty.put(player, party);
        partyToPlayers.put(party, player);

        final Integer count = alivePlayers.get(party);

        if (count == null) {
            alivePlayers.put(party, 1);
            return;
        }

        alivePlayers.put(party, count + 1);
    }

    @Override
    public void markAsDead(Player player) {
        super.markAsDead(player);

        final Party party = playerToParty.get(player);

        if (party == null) {
            return;
        }

        final Integer count = alivePlayers.get(party);

        if (count == null) {
            return;
        }

        alivePlayers.put(party, count - 1);
    }

    @Override
    public int size() {
        return (int) alivePlayers.entrySet().stream().filter(entry -> entry.getValue() > 0).count();
    }
}
