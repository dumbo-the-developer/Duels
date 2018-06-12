package me.realized.duels.api.event;

import javax.annotation.Nullable;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;

public abstract class SourcedEvent extends Event {

    @Getter
    private final CommandSender source;

    protected SourcedEvent(@Nullable final CommandSender source) {
        this.source = source;
    }

    public boolean hasSource() {
        return getSource() != null;
    }
}
