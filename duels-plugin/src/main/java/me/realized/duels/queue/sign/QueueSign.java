package me.realized.duels.queue.sign;

import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.realized.duels.queue.Queue;
import me.realized.duels.util.StringUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class QueueSign implements me.realized.duels.api.queue.sign.QueueSign {

    @Getter
    private final Location location;
    @Getter
    private final String[] lines;
    @Getter
    private final Queue queue;
    private int lastCount;
    @Getter
    @Setter(value = AccessLevel.PACKAGE)
    private boolean removed;

    public QueueSign(final Location location, final String format, final Queue queue) {
        this.location = location;
        this.queue = queue;

        final String[] data = {"", "", "", ""};

        if (format != null) {
            final String[] lines = format.split("\n");
            System.arraycopy(lines, 0, data, 0, lines.length);
        }

        this.lines = data;

        final Block block = location.getBlock();

        if (!(block.getState() instanceof Sign)) {
            return;
        }

        final Sign sign = (Sign) block.getState();

        sign.setLine(0, replace(lines[0], 0));
        sign.setLine(1, replace(lines[1], 0));
        sign.setLine(2, replace(lines[2], 0));
        sign.setLine(3, replace(lines[3], 0));
        sign.update(true);
    }

    private String replace(final String line, final int count) {
        return StringUtil.color(line.replace("%count%", String.valueOf(count)));
    }

    public void updateCount() {
        final Block block = location.getBlock();

        if (!(block.getState() instanceof Sign)) {
            return;
        }

        final Sign sign = (Sign) block.getState();

        if (queue.isRemoved()) {
            sign.setType(Material.AIR);
            sign.update(true);
            return;
        }

        final int count = queue.getPlayers().size();

        if (lastCount == count) {
            return;
        }

        this.lastCount = count;
        sign.setLine(0, replace(lines[0], count));
        sign.setLine(1, replace(lines[1], count));
        sign.setLine(2, replace(lines[2], count));
        sign.setLine(3, replace(lines[3], count));
        sign.update(true);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final QueueSign queueSign = (QueueSign) o;
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
