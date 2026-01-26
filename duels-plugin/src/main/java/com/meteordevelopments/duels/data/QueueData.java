package com.meteordevelopments.duels.data;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.queue.Queue;

public class QueueData {

    private String name;
    private String kit;
    private int bet;
    private int teamSize;

    private QueueData() {
    }

    public QueueData(final Queue queue) {
        this.name = queue.getName();
        this.kit = queue.getKit() != null ? queue.getKit().getName() : null;
        this.bet = queue.getBet();
        this.teamSize = queue.getTeamSize();
    }

    public Queue toQueue(final DuelsPlugin plugin) {
        return new Queue(plugin, name != null ? name : "Unnamed", kit != null ? plugin.getKitManager().get(kit) : null, bet, teamSize <= 0 ? 1 : teamSize);
    }
}