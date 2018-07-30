package me.realized.duels.data;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.queue.QueueSign;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class QueueSignData {

    private final LocationData location;
    private final String kit;
    private final int bet;

    public QueueSignData(final QueueSign queue) {
        this.location = new LocationData(queue.getLocation());
        this.kit = queue.getKit() != null ? queue.getKit().getName() : null;
        this.bet = queue.getBet();
    }

    public QueueSign toQueueSign(final DuelsPlugin plugin) {
        final Location location = this.location.toLocation();

        if (location.getWorld() == null) {
            return null;
        }

        final Block block = location.getBlock();

        if (!(block.getState() instanceof Sign)) {
            return null;
        }

        return new QueueSign(location, plugin.getLang().getMessage("SIGN.format",
            "kit", kit != null ? kit : "none", "bet_amount", bet), kit != null ? plugin.getKitManager().get(kit) : null, bet);
    }
}