package me.realized.duels.api.kit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.realized.duels.api.event.kit.KitCreateEvent;
import me.realized.duels.api.event.kit.KitRemoveEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface KitManager {

    /**
     * @param name Name to search through the kits
     * @return Kit with the given name if exists, otherwise null
     */
    @Nullable
    Kit get(@Nonnull final String name);


    /**
     * Calls {@link KitCreateEvent} on successful creation.
     *
     * @param creator Player who is the creator of this kit
     * @param name Name of the kit
     * @return The newly created kit or null if a kit with given name already exists
     */
    @Nullable
    Kit create(@Nonnull final Player creator, @Nonnull final String name);


    /**
     * Calls {@link KitRemoveEvent} on successful removal.
     *
     * @param source CommandSender who is the source of this call
     * @param name Name of the kit to remove
     * @return The removed kit if removal was successful, otherwise null
     */
    @Nullable
    Kit remove(@Nullable CommandSender source, @Nonnull final String name);


    /**
     * Calls {@link #remove(CommandSender, String)} with source being null.
     *
     * @see #remove(CommandSender, String)
     */
    @Nullable
    Kit remove(@Nonnull final String name);
}
