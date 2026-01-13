package com.meteordevelopments.duels.data;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.kit.KitImpl;
import com.meteordevelopments.duels.queue.Queue;
import com.meteordevelopments.duels.queue.sign.QueueSignImpl;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class QueueSignData {

    private LocationData location;
    private String queueName;
    private String kit;
    private int bet;
    private int teamSize;

    private QueueSignData() {
    }

    public QueueSignData(final QueueSignImpl sign) {
        this.location = LocationData.fromLocation(sign.getLocation());

        final Queue queue = sign.getQueue();
        this.queueName = queue.getName();
        this.kit = queue.getKit() != null ? queue.getKit().getName() : null;
        this.bet = queue.getBet();
        this.teamSize = queue.getTeamSize();
    }

    public QueueSignImpl toQueueSign(final DuelsPlugin plugin) {
        final Location location = this.location.toLocation();

        if (location.getWorld() == null) {
            return null;
        }

        final Block block = location.getBlock();

        if (!(block.getState() instanceof Sign)) {
            return null;
        }

        // Try to find queue by name first, then fall back to kit-based lookup for backwards compatibility
        Queue queue = plugin.getQueueManager().getByName(queueName);
        
        if (queue == null) {
            // Fallback to old kit-based system for backwards compatibility
            final KitImpl kit = this.kit != null ? plugin.getKitManager().get(this.kit) : null;
            queue = plugin.getQueueManager().get(kit, bet);
            
            if (queue == null) {
                // Create new queue with the stored name
                plugin.getQueueManager().create(null, queueName != null ? queueName : "Unnamed", kit, bet, teamSize <= 0 ? 1 : teamSize);
                queue = plugin.getQueueManager().getByName(queueName);
            }
        }

        return new QueueSignImpl(location, plugin.getLang().getMessage("SIGN.format", "name", queueName != null ? queueName : "Unnamed", "kit", this.kit != null ? this.kit : plugin.getLang().getMessage("GENERAL.none"), "bet_amount", bet), queue, plugin.getLang());
    }
}