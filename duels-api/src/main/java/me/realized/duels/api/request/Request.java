package me.realized.duels.api.request;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.realized.duels.api.arena.Arena;
import me.realized.duels.api.kit.Kit;

public interface Request {

    /**
     * @return The UUID of the sender of this request
     */
    @Nonnull
    UUID getSender();


    /**
     * @return The UUID of the receiver of this request
     */
    @Nonnull
    UUID getTarget();


    /**
     * @return The kit for this request if selected, otherwise null
     */
    @Nullable
    Kit getKit();


    /**
     * @return The arena for this request if selected, otherwise null
     */
    @Nullable
    Arena getArena();

    /**
     * @return true if item betting is enabled for this request, otherwise false
     */
    boolean canBetItems();

    /**
     * @return The bet for this request or 0 if not specified
     */
    int getBet();
}
