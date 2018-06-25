package me.realized.duels.queue;

import java.util.Objects;
import lombok.Getter;
import me.realized.duels.kit.Kit;
import me.realized.duels.util.StringUtil;
import org.bukkit.block.Sign;

public class QueueSign {

    @Getter
    private final Sign sign;
    @Getter
    private final String[] lines;
    @Getter
    private final Kit kit;
    @Getter
    private final int bet;

    public QueueSign(final Sign sign, final String format, final Kit kit, final int bet) {
        this.sign = sign;
        this.kit = kit;
        this.bet = bet;

        final String[] data = {"", "", "", ""};

        if (format != null) {
            final String[] lines = format.split("\n");
            System.arraycopy(data, 0, lines, 0, 4);
        }

        this.lines = data;
        sign.setLine(0, replace(lines[0], 0));
        sign.setLine(1, replace(lines[1], 0));
        sign.setLine(2, replace(lines[2], 0));
        sign.setLine(3, replace(lines[3], 0));
        sign.update(true);
    }

    private String replace(final String line, final int count) {
        return StringUtil.color(line.replace("%count%", String.valueOf(count)));
    }

    public void setCount(final int count) {
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
        return bet == queueSign.bet && Objects.equals(kit, queueSign.kit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kit, bet);
    }

    @Override
    public String toString() {
        return (kit != null ? kit.getName() : "none") + " - $" + bet + " - " + StringUtil.parse(sign.getLocation());
    }
}
