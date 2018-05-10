package me.realized.duels.arena;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import me.realized.duels.kit.Kit;
import org.bukkit.inventory.ItemStack;

public class Match {

    @Getter
    private final long creation;
    @Getter
    private final Kit kit;
    @Getter
    private final Map<UUID, List<ItemStack>> items;
    @Getter
    private final int bet;

    Match(final Kit kit, final Map<UUID, List<ItemStack>> items, final int bet) {
        this.creation = System.currentTimeMillis();
        this.kit = kit;
        this.items = items;
        this.bet = bet;
    }

    public enum EndReason {

        OPPONENT_QUIT, OPPONENT_DEFEAT, PLUGIN_DISABLE, MAX_TIME_REACHED, OTHER
    }
}
