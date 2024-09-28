package com.meteordevelopments.duels.data;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.queue.Queue;

public class QueueData {

    private String kit;
    private int bet;

    private QueueData() {
    }

    public QueueData(final Queue queue) {
        this.kit = queue.getKit() != null ? queue.getKit().getName() : null;
        this.bet = queue.getBet();
    }

    public Queue toQueue(final DuelsPlugin plugin) {
        return new Queue(plugin, kit != null ? plugin.getKitManager().get(kit) : null, bet);
    }
}