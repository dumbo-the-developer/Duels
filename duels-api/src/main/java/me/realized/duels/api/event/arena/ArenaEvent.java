package me.realized.duels.api.event.arena;

import javax.annotation.Nonnull;
import lombok.Getter;
import me.realized.duels.api.arena.Arena;
import me.realized.duels.api.event.SourcedEvent;
import org.bukkit.command.CommandSender;

abstract class ArenaEvent extends SourcedEvent {

    @Getter
    private final Arena arena;

    ArenaEvent(final CommandSender source, @Nonnull final Arena arena) {
        super(source);
        this.arena = arena;
    }
}
