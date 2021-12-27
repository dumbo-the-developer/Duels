package me.realized.duels.api.queue.sign;

import java.util.List;
import org.bukkit.block.Sign;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the QueueSignManager singleton used by Duels.
 *
 * @since 3.2.0
 */
public interface QueueSignManager {

    /**
     * Gets a {@link QueueSign} that is associated with the given {@link Sign}.
     *
     * @param sign {@link Sign} to match in the loaded queue signs.
     * @return {@link QueueSign} associated with the {@link Sign} or null if not found.
     */
    @Nullable
    QueueSign get(@NotNull final Sign sign);


    /**
     * An UnmodifiableList of {@link QueueSign}s that are currently loaded.
     *
     * @return Never-null UnmodifiableList of {@link QueueSign}s that are currently loaded.
     */
    @NotNull
    List<QueueSign> getQueueSigns();
}
