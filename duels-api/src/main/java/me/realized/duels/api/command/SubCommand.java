package me.realized.duels.api.command;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.Getter;
import org.bukkit.command.CommandSender;

public abstract class SubCommand {

    @Getter
    private final String name;
    @Getter
    private final String usage;
    @Getter
    private final String description;
    @Getter
    private final String permission;
    @Getter
    private final boolean playerOnly;
    @Getter
    private final int length;
    @Getter
    private final String[] aliases;

    public SubCommand(@Nonnull final String name, @Nullable final String usage, @Nullable final String description, @Nullable final String permission, final boolean playerOnly, final int length,
        final String... aliases) {
        Objects.requireNonNull(name, "name");
        this.name = name;
        this.usage = usage;
        this.description = description;
        this.permission = permission;
        this.playerOnly = playerOnly;
        this.length = Math.max(length, 1);
        this.aliases = aliases;
    }

    public abstract void execute(final CommandSender sender, final String label, final String[] args);
}
