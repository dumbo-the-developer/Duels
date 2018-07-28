package me.realized.duels.api;

import javax.annotation.Nonnull;
import lombok.NonNull;
import me.realized.duels.api.arena.ArenaManager;
import me.realized.duels.api.command.SubCommand;
import me.realized.duels.api.kit.KitManager;
import me.realized.duels.api.user.UserManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public interface Duels extends Plugin {

    @Nonnull
    UserManager getUserManager();


    @Nonnull
    ArenaManager getArenaManager();


    @Nonnull
    KitManager getKitManager();


    /**
     * @param command Name of the parent command to register the sub command
     * @param subCommand SubCommand to register
     * @return true if sub command was successfully registered, otherwise false
     */
    boolean registerSubCommand(@Nonnull final String command, @Nonnull final SubCommand subCommand);


    /**
     * Reloads the plugin.
     *
     * @return true if reload was successful, otherwise false
     */
    boolean reload();


    /**
     * Runs the task on server thread.
     *
     * @param task Task to run
     * @return BukkitTask executed
     * @since 3.1.0
     */
    BukkitTask doSync(@NonNull final Runnable task);


    /**
     * Runs the task after delay on server thread.
     *
     * @param task Task to run
     * @param delay time to delay. 20L = 1s
     * @return BukkitTask executed
     * @since 3.1.0
     */
    BukkitTask doSyncAfter(@NonNull final Runnable task, long delay);


    /**
     * Runs the task after delay on server thread repeatedly.
     *
     * @param task Task to run
     * @param delay time to delay the start of repeat. 20L = 1s
     * @param interval interval of this task. 20L = 1s
     * @return BukkitTask executed
     * @since 3.1.0
     */
    BukkitTask doSyncRepeat(@NonNull final Runnable task, long delay, long interval);


    /**
     * Runs the task asynchronously.
     *
     * @param task Task to run asynchronously
     * @return BukkitTask executed
     * @since 3.1.0
     */
    BukkitTask doAsync(@NonNull final Runnable task);


    /**
     * Runs the task after delay asynchronously.
     *
     * @param task Task to run asynchronously
     * @param delay time to delay. 20L = 1s
     * @return BukkitTask executed
     * @since 3.1.0
     */
    BukkitTask doAsyncAfter(@NonNull final Runnable task, long delay);


    /**
     * Runs the task after delay asynchronously repeatedly.
     *
     * @param task Task to run asynchronously
     * @param delay time to delay the start of repeat. 20L = 1s
     * @param interval interval of this task. 20L = 1s
     * @return BukkitTask executed
     * @since 3.1.0
     */
    BukkitTask doAsyncRepeat(@NonNull final Runnable task, long delay, long interval);


    /**
     * @param message message to log
     */
    void info(@NonNull final String message);


    /**
     * @param message message to log
     */
    void warn(@NonNull final String message);


    /**
     * @param message message to log
     */
    void error(@NonNull final String message);


    /**
     * @param message message to log
     */
    void error(@NonNull final String message, @NonNull Throwable thrown);


    /**
     * @return version of the plugin
     * @since 3.1.0
     */
    String getVersion();
}
