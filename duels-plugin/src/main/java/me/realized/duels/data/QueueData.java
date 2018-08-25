package me.realized.duels.data;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.queue.Queue;

public class QueueData {

    private final String kit;
    private final int bet;

    public QueueData(final Queue queue) {
        this.kit = queue.getKit() != null ? queue.getKit().getName() : null;
        this.bet = queue.getBet();
    }

    public Queue toQueue(final DuelsPlugin plugin) {
        return new Queue(plugin, kit != null ? plugin.getKitManager().get(kit) : null, bet);
    }
}