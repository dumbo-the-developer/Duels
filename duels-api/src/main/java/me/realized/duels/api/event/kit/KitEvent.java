package me.realized.duels.api.event.kit;

import javax.annotation.Nonnull;
import lombok.Getter;
import me.realized.duels.api.event.SourcedEvent;
import me.realized.duels.api.kit.Kit;
import org.bukkit.command.CommandSender;

abstract class KitEvent extends SourcedEvent {

    @Getter
    private final Kit kit;

    KitEvent(final CommandSender source, @Nonnull final Kit kit) {
        super(source);
        this.kit = kit;
    }
}
