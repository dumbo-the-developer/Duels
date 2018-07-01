package me.realized.duels.arena;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.Getter;
import me.realized.duels.kit.Kit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Match implements me.realized.duels.api.match.Match {

    @Getter
    private final Arena arena;
    @Getter
    private final long start;
    @Getter
    private final Kit kit;
    private final Map<UUID, List<ItemStack>> items;
    @Getter
    private final int bet;
    @Getter
    private final boolean fromQueue;
    private final Map<Player, Boolean> players = new HashMap<>();

    Match(final Arena arena, final Kit kit, final Map<UUID, List<ItemStack>> items, final int bet, final boolean fromQueue) {
        this.arena = arena;
        this.start = System.currentTimeMillis();
        this.kit = kit;
        this.items = items;
        this.bet = bet;
        this.fromQueue = fromQueue;
    }

    Map<Player, Boolean> getPlayerMap() {
        return players;
    }

    public Set<Player> getPlayers() {
        return players.keySet();
    }

    public boolean isDead(final Player player) {
        return players.getOrDefault(player, true);
    }

    @Override
    public List<ItemStack> getItems(@Nonnull final Player player) {
        Objects.requireNonNull(player, "player");

        if (this.items == null) {
            return Collections.emptyList();
        }

        final List<ItemStack> items = this.items.get(player.getUniqueId());
        return items != null ? items : Collections.emptyList();
    }

    public List<ItemStack> getItems() {
        return items != null ? items.values().stream().flatMap(Collection::stream).collect(Collectors.toList()) : Collections.emptyList();
    }
}
