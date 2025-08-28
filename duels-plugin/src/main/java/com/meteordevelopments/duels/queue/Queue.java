package com.meteordevelopments.duels.queue;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.api.kit.Kit;
import com.meteordevelopments.duels.api.queue.DQueue;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Queue extends BaseButton implements DQueue {

    @Getter
    private final Kit kit;
    @Getter
    private final int bet;
    @Getter
    private final List<QueueEntry> players = new LinkedList<>();
    @Getter
    @Setter(value = AccessLevel.PACKAGE)
    private boolean removed;

    public Queue(final DuelsPlugin plugin, final Kit kit, final int bet) {
        super(plugin, ItemBuilder
                .of((plugin.getConfiguration().isInheritKitItemType() && kit != null) ? kit.getDisplayed().clone() : ItemBuilder.of(Material.DIAMOND_SWORD).build())
                .name(plugin.getLang().getMessage("GUI.queues.buttons.queue.name",
                        "kit", kit != null ? kit.getName() : plugin.getLang().getMessage("GENERAL.none"), "bet_amount", bet, "in_queue", 0, "in_match", 0))
                .lore(plugin.getLang().getMessage("GUI.queues.buttons.queue.lore",
                        "kit", kit != null ? kit.getName() : plugin.getLang().getMessage("GENERAL.none"), "bet_amount", bet, "in_queue", 0, "in_match", 0).split("\n"))
                .build());
        this.kit = kit;
        this.bet = bet;
    }

    @Override
    public boolean isInQueue(@NotNull final Player player) {
        return players.stream().anyMatch(entry -> entry.getPlayer().equals(player));
    }

    @NotNull
    @Override
    public List<Player> getQueuedPlayers() {
        return players.stream().map(QueueEntry::getPlayer).toList();
    }

    void addPlayer(final QueueEntry entry) {
        players.add(entry);
        update();
        queueManager.getGui().calculatePages();
    }

    boolean removePlayer(final Player player) {
        if (players.removeIf(entry -> entry.getPlayer().equals(player))) {
            update();
            queueManager.getGui().calculatePages();
            return true;
        }

        return false;
    }

    boolean removeAll(final Set<QueueEntry> players) {
        if (this.players.removeAll(players)) {
            update();
            return true;
        }

        return false;
    }

    @Override
    public long getPlayersInMatch() {
        return arenaManager.getPlayersInMatch(this);
    }

    public void update() {
        int inQueue = players.size();
        long inMatch = getPlayersInMatch();
        setDisplayName(lang.getMessage("GUI.queues.buttons.queue.name",
                "kit", kit != null ? kit.getName() : lang.getMessage("GENERAL.none"), "bet_amount", bet, "in_queue", inQueue, "in_match", inMatch));
        setLore(lang.getMessage("GUI.queues.buttons.queue.lore",
                "kit", kit != null ? kit.getName() : lang.getMessage("GENERAL.none"), "bet_amount", bet, "in_queue", inQueue, "in_match", inMatch).split("\n"));
    }

    @Override
    public void onClick(final Player player) {
        queueManager.addToQueue(player, this);
    }

    @Override
    public String toString() {
        return (kit != null ? kit.getName() : "none") + " ($" + bet + ")";
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        final Queue queue = (Queue) other;
        return bet == queue.bet && Objects.equals(kit, queue.kit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kit, bet);
    }
}
