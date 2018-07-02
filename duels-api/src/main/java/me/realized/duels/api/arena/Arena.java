package me.realized.duels.api.arena;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.realized.duels.api.event.arena.ArenaSetPositionEvent;
import me.realized.duels.api.event.arena.ArenaStateChangeEvent;
import me.realized.duels.api.match.Match;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface Arena {

    /**
     * @return Name of the arena
     */
    @Nonnull
    String getName();


    /**
     * @return true if arena is disabled, otherwise false
     */
    boolean isDisabled();


    /**
     * Calls {@link #setDisabled(CommandSender, boolean)} with source being null.
     *
     * @see #setDisabled(CommandSender, boolean)
     */
    void setDisabled(final boolean disabled);


    /**
     * Calls {@link ArenaStateChangeEvent}.
     *
     * @param source CommandSender who is the source of this call.
     * @param disabled true to disable the arena, false to enable the arena
     */
    void setDisabled(@Nullable final CommandSender source, final boolean disabled);


    /**
     * @param pos Position number associated with the resulting location
     * @return Location with the position number or null if not set
     */
    @Nullable
    Location getPosition(final int pos);


    /**
     * Calls {@link ArenaSetPositionEvent}.
     *
     * @param source Player who is the source of this call.
     * @param pos Position number for the location
     * @param location Location to set
     */
    void setPosition(@Nullable final Player source, final int pos, @Nonnull final Location location);


    /**
     * Calls {@link #setPosition(Player, int, Location)} with source being null.
     *
     * @see #setPosition(Player, int, Location)
     */
    void setPosition(final int pos, @Nonnull final Location location);


    /**
     * @return true if arena is in use. This also guarantees that {@link #getMatch()} will not be null.
     */
    boolean isUsed();


    /**
     * @return Match instance if a duel is currently being played on this arena, otherwise null
     */
    @Nullable
    Match getMatch();


    /**
     * @param player Player to check if in arena
     * @return true if player is in this arena, otherwise false
     */
    boolean has(@Nonnull final Player player);
}
