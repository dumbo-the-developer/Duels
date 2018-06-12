package me.realized.duels.api.event.match;

import javax.annotation.Nonnull;
import lombok.Getter;
import me.realized.duels.api.match.Match;
import org.bukkit.event.Event;

abstract class MatchEvent extends Event {

    @Getter
    private final Match match;

    MatchEvent(@Nonnull final Match match) {
        this.match = match;
    }
}
