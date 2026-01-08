package com.meteordevelopments.duels.queue.sign;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.api.queue.sign.QueueSign;
import com.meteordevelopments.duels.queue.Queue;
import com.meteordevelopments.duels.util.StringUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.util.Objects;

@SuppressWarnings("deprecation")
public class QueueSignImpl implements QueueSign {

    @Getter
    private final Location location;
    @Getter
    private final String[] lines;
    @Getter
    private final Queue queue;
    @Getter
    @Setter(value = AccessLevel.PACKAGE)
    private boolean removed;

    private int lastInQueue;
    private long lastInMatch;
    private com.meteordevelopments.duels.config.Lang lang;

    public QueueSignImpl(final Location location, final String format, final Queue queue) {
        this(location, format, queue, null);
    }

    public QueueSignImpl(final Location location, final String format, final Queue queue, final com.meteordevelopments.duels.config.Lang lang) {
        this.location = location;
        this.queue = queue;
        this.lang = lang;

        final String[] data = {"", "", "", ""};

        if (format != null) {
            final String[] lines = format.split("\n");
            System.arraycopy(lines, 0, data, 0, lines.length);
        }

        this.lines = data;

        final Block block = location.getBlock();

        if (!(block.getState() instanceof Sign sign)) {
            return;
        }

        // FIXED: Schedule sign operations on region-specific scheduler for Folia compatibility
        final org.bukkit.Location signLocation = sign.getLocation();
        if (signLocation != null) {
            DuelsPlugin.getMorePaperLib().scheduling()
                .regionSpecificScheduler(signLocation)
                .run(() -> {
                    final Block blockAtLocation = signLocation.getBlock();
                    if (blockAtLocation.getState() instanceof Sign signState) {
                        signState.setLine(0, replace(lines[0], 0, 0));
                        signState.setLine(1, replace(lines[1], 0, 0));
                        signState.setLine(2, replace(lines[2], 0, 0));
                        signState.setLine(3, replace(lines[3], 0, 0));
                        signState.update();
                    }
                });
        } else {
            // Fallback: use global scheduler if location is null
            DuelsPlugin.getMorePaperLib().scheduling()
                .globalRegionalScheduler()
                .run(() -> {
                    final Block blockAtLocation = location.getBlock();
                    if (blockAtLocation.getState() instanceof Sign signState) {
                        signState.setLine(0, replace(lines[0], 0, 0));
                        signState.setLine(1, replace(lines[1], 0, 0));
                        signState.setLine(2, replace(lines[2], 0, 0));
                        signState.setLine(3, replace(lines[3], 0, 0));
                        signState.update();
                    }
                });
        }
    }

    private String replace(final String line, final int inQueue, final long inMatch) {
        String replacedLine = line.replace("%in_queue%", String.valueOf(inQueue)).replace("%in_match%", String.valueOf(inMatch));
        
        if (lang != null) {
            return lang.toLegacyString(replacedLine);
        } else {
            return StringUtil.color(replacedLine);
        }
    }

    public void update() {
        final Block block = location.getBlock();

        if (!(block.getState() instanceof Sign sign)) {
            return;
        }

        if (queue.isRemoved()) {
            // Schedule block operation on region-specific scheduler for Folia compatibility
            final org.bukkit.Location signLocation = sign.getLocation();
            DuelsPlugin.getMorePaperLib().scheduling()
                .regionSpecificScheduler(signLocation)
                .run(() -> {
                    sign.setType(Material.AIR);
                    sign.update();
                });
            return;
        }

        final int inQueue = queue.getPlayers().size();
        final long inMatch = queue.getPlayersInMatch();

        if (lastInQueue == inQueue && lastInMatch == inMatch) {
            return;
        }

        this.lastInQueue = inQueue;
        this.lastInMatch = inMatch;

        // FIXED: Schedule sign operations on region-specific scheduler for Folia compatibility
        final org.bukkit.Location signLocation = sign.getLocation();
        if (signLocation != null) {
            DuelsPlugin.getMorePaperLib().scheduling()
                .regionSpecificScheduler(signLocation)
                .run(() -> {
                    final Block blockAtLocation = signLocation.getBlock();
                    if (blockAtLocation.getState() instanceof Sign signState) {
                        signState.setLine(0, replace(lines[0], inQueue, inMatch));
                        signState.setLine(1, replace(lines[1], inQueue, inMatch));
                        signState.setLine(2, replace(lines[2], inQueue, inMatch));
                        signState.setLine(3, replace(lines[3], inQueue, inMatch));
                        signState.update();
                    }
                });
        } else {
            // Fallback: use global scheduler if location is null
            DuelsPlugin.getMorePaperLib().scheduling()
                .globalRegionalScheduler()
                .run(() -> {
                    final Block blockAtLocation = location.getBlock();
                    if (blockAtLocation.getState() instanceof Sign signState) {
                        signState.setLine(0, replace(lines[0], inQueue, inMatch));
                        signState.setLine(1, replace(lines[1], inQueue, inMatch));
                        signState.setLine(2, replace(lines[2], inQueue, inMatch));
                        signState.setLine(3, replace(lines[3], inQueue, inMatch));
                        signState.update();
                    }
                });
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final QueueSignImpl queueSign = (QueueSignImpl) o;
        return Objects.equals(queue, queueSign.getQueue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(queue);
    }

    @Override
    public String toString() {
        return StringUtil.parse(location);
    }
}
