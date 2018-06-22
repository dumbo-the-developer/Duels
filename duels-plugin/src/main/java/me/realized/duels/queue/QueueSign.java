package me.realized.duels.queue;

import java.util.Objects;
import lombok.Getter;
import me.realized.duels.kit.Kit;
import org.bukkit.block.Sign;

public class QueueSign {

    @Getter
    private final Sign sign;
    @Getter
    private final Kit kit;
    @Getter
    private final int bet;

    public QueueSign(final Sign sign, final Kit kit, final int bet) {
        this.sign = sign;
        this.kit = kit;
        this.bet = bet;
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
}
