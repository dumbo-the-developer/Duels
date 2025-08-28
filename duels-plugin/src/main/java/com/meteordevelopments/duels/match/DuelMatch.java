package com.meteordevelopments.duels.match;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.api.match.Match;
import com.meteordevelopments.duels.arena.ArenaImpl;
import com.meteordevelopments.duels.kit.KitImpl;
import com.meteordevelopments.duels.party.PartyManagerImpl;
import com.meteordevelopments.duels.queue.Queue;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import lombok.Getter;

public class DuelMatch implements Match {
    
    protected final PartyManagerImpl partyManager;

    @Getter
    private final long creation;

    @Getter
    private final ArenaImpl arena;
    @Getter
    private final KitImpl kit;
    private final Map<UUID, List<ItemStack>> items;
    @Getter
    private final int bet;
    @Getter
    private final Queue source;

    @Getter
    private boolean finished;

    // Round tracking for ROUNDS3 characteristic
    @Getter
    private int currentRound = 0;
    private final Map<Player, Integer> roundWins = new HashMap<>();

    public HashMap<Location, BlockData> brokenBlocks = new HashMap<>();
    public List<Block> placedBlocks = new ArrayList<>();
    public List<Block> liquids = new ArrayList<>();
    public List<Entity> placedEntities = new ArrayList<>();
    public List<Item> droppedItems = new ArrayList<>();

    // Default value for players is false, which is set to true if player is killed in the match.
    private final Map<Player, Boolean> players = new HashMap<>();

    public DuelMatch(final DuelsPlugin plugin, final ArenaImpl arena, final KitImpl kit, final Map<UUID, List<ItemStack>> items, final int bet, final Queue source) {
        this.partyManager = plugin.getPartyManager();
        this.creation = System.currentTimeMillis();
        this.arena = arena;
        this.kit = kit;
        this.items = items;
        this.bet = bet;
        this.source = source;
    }
    
    public long getDurationInMillis() {
        return System.currentTimeMillis() - creation;
    }

    public boolean isFromQueue() {
        return source != null;
    }

    public boolean isOwnInventory() {
        return kit == null;
    }
    
    public void setFinished() {
        finished = true;
    }

    public void addPlayer(final Player player) {
        players.put(player, false);
    }

    public void markAsDead(final Player player) {
        if (players.containsKey(player)) {
            players.put(player, true);
        }
    }

    public boolean isDead(final Player player) {
        return players.getOrDefault(player, true);
    }

    public Set<Player> getAlivePlayers() {
        return players.entrySet().stream().filter(entry -> !entry.getValue()).map(Entry::getKey).collect(Collectors.toSet());
    }

    public Set<Player> getAllPlayers() {
        return players.keySet();
    }

    public int size() {
        return getAlivePlayers().size();
    }

    public List<ItemStack> getItems() {
        return items != null ? items.values().stream().flatMap(Collection::stream).collect(Collectors.toList()) : Collections.emptyList();
    }

    @NotNull
    @Override
    public List<ItemStack> getItems(@NotNull final Player player) {
        Objects.requireNonNull(player, "player");

        if (this.items == null) {
            return Collections.emptyList();
        }

        final List<ItemStack> items = this.items.get(player.getUniqueId());
        return items != null ? items : Collections.emptyList();
    }

    @NotNull
    @Override
    public Set<Player> getPlayers() {
        return Collections.unmodifiableSet(getAlivePlayers());
    }

    @NotNull
    @Override
    public Set<Player> getStartingPlayers() {
        return Collections.unmodifiableSet(getAllPlayers());
    }

    @Override
    public long getStart() {
        return creation;
    }

    public int getRoundWins(Player player) {
        return roundWins.getOrDefault(player, 0);
    }

    public void addRoundWin(Player player) {
        roundWins.put(player, getRoundWins(player) + 1);
    }

    public boolean hasWonMatch(Player player) {
        return getRoundWins(player) >= 2;
    }

    public void nextRound() {
        currentRound++;
        // Reset player death states for next round
        players.replaceAll((p, v) -> false);
    }

    public void handleMatchEnd(Player winner, Player loser) {
        // Mark loser as dead
        markAsDead(loser);

        // Set winner's health to full
        winner.setHealth(winner.getMaxHealth());

        // Clear any effects or states that might interfere
        winner.setFireTicks(0);
        winner.setFallDistance(0);
        winner.setVelocity(new org.bukkit.util.Vector(0, 0, 0));

        // Set match as finished
        setFinished();
    }
}
