package com.meteordevelopments.duels.util;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Scheduler adapter that works with both Paper and Folia
 * Automatically detects the platform and uses the appropriate scheduler
 */
@SuppressWarnings({"all"})
public class SchedulerAdapter {

    private final Plugin plugin;
    @Getter
    private final boolean isFolia;
    private Object globalRegionScheduler;
    private Object asyncScheduler;
    private Method scheduleMethod;
    private Method runDelayedMethod;
    private Method runAtFixedRateMethod;
    private Method cancelMethod;

    public SchedulerAdapter(Plugin plugin) {
        this.plugin = plugin;
        this.isFolia = checkFolia();

        if (isFolia) {
            try {
                // Get Folia schedulers via reflection
                Class<?> serverClass = Bukkit.getServer().getClass();
                Method getGlobalRegionScheduler = serverClass.getMethod("getGlobalRegionScheduler");
                Method getAsyncScheduler = serverClass.getMethod("getAsyncScheduler");

                this.globalRegionScheduler = getGlobalRegionScheduler.invoke(Bukkit.getServer());
                this.asyncScheduler = getAsyncScheduler.invoke(Bukkit.getServer());

                // Cache reflection methods for better performance
                Class<?> globalSchedulerClass = globalRegionScheduler.getClass();
                this.scheduleMethod = globalSchedulerClass.getMethod("run", Plugin.class, Consumer.class);
                this.runDelayedMethod = globalSchedulerClass.getMethod("runDelayed", Plugin.class, Consumer.class, long.class);

                Class<?> asyncSchedulerClass = asyncScheduler.getClass();
                this.runAtFixedRateMethod = asyncSchedulerClass.getMethod("runAtFixedRate",
                        Plugin.class, Consumer.class, long.class, long.class, TimeUnit.class);

            } catch (Exception e) {
                plugin.getLogger().severe("Failed to initialize Folia schedulers: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Check if the server is running Folia
     */
    private boolean checkFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Run a task on the main/global region thread
     */
    public TaskWrapper runTask(Runnable task) {
        if (isFolia) {
            try {
                Object scheduledTask = scheduleMethod.invoke(globalRegionScheduler, plugin,
                        (Consumer<Object>) t -> task.run());
                return new FoliaTaskWrapper(scheduledTask);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to schedule Folia task: " + e.getMessage());
                return null;
            }
        } else {
            return new BukkitTaskWrapper(Bukkit.getScheduler().runTask(plugin, task));
        }
    }

    /**
     * Run a task asynchronously
     */
    public TaskWrapper runTaskAsynchronously(Runnable task) {
        if (isFolia) {
            try {
                Object scheduledTask = asyncScheduler.getClass()
                        .getMethod("runNow", Plugin.class, Consumer.class)
                        .invoke(asyncScheduler, plugin, (Consumer<Object>) t -> task.run());
                return new FoliaTaskWrapper(scheduledTask);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to schedule Folia async task: " + e.getMessage());
                return null;
            }
        } else {
            return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
        }
    }

    /**
     * Run a task on the entity's region (Folia) or main thread (Paper)
     */
    public TaskWrapper runTask(Entity entity, Runnable task) {
        if (isFolia) {
            try {
                Object entityScheduler = entity.getClass().getMethod("getScheduler").invoke(entity);
                Object scheduledTask = entityScheduler.getClass()
                        .getMethod("run", Plugin.class, Consumer.class, Runnable.class)
                        .invoke(entityScheduler, plugin, (Consumer<Object>) t -> task.run(), null);
                return new FoliaTaskWrapper(scheduledTask);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to schedule Folia entity task: " + e.getMessage());
                return null;
            }
        } else {
            return new BukkitTaskWrapper(Bukkit.getScheduler().runTask(plugin, task));
        }
    }

    /**
     * Run a task at a location's region (Folia) or main thread (Paper)
     */
    public TaskWrapper runTask(Location location, Runnable task) {
        if (isFolia) {
            try {
                Object regionScheduler = Bukkit.getServer().getClass()
                        .getMethod("getRegionScheduler")
                        .invoke(Bukkit.getServer());
                Object scheduledTask = regionScheduler.getClass()
                        .getMethod("run", Plugin.class, Location.class, Consumer.class)
                        .invoke(regionScheduler, plugin, location, (Consumer<Object>) t -> task.run());
                return new FoliaTaskWrapper(scheduledTask);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to schedule Folia region task: " + e.getMessage());
                return null;
            }
        } else {
            return new BukkitTaskWrapper(Bukkit.getScheduler().runTask(plugin, task));
        }
    }

    /**
     * Run a delayed task
     */
    public TaskWrapper runTaskLater(Runnable task, long delay) {
        if (isFolia) {
            try {
                Object scheduledTask = runDelayedMethod.invoke(globalRegionScheduler, plugin,
                        (Consumer<Object>) t -> task.run(), delay);
                return new FoliaTaskWrapper(scheduledTask);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to schedule Folia delayed task: " + e.getMessage());
                return null;
            }
        } else {
            return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskLater(plugin, task, delay));
        }
    }

    /**
     * Run a delayed task at a location's region (Folia) or main thread (Paper)
     */
    public TaskWrapper runTaskLater(Location location, Runnable task, long delay) {
        if (isFolia) {
            try {
                Object regionScheduler = Bukkit.getServer().getClass()
                        .getMethod("getRegionScheduler")
                        .invoke(Bukkit.getServer());
                Object scheduledTask = regionScheduler.getClass()
                        .getMethod("runDelayed", Plugin.class, Consumer.class, Location.class, long.class)
                        .invoke(regionScheduler, plugin, (Consumer<Object>) t -> task.run(), location, delay);
                return new FoliaTaskWrapper(scheduledTask);
            } catch (Exception e) {
                // Silently fail - we have fallback to global scheduler
                // This is expected when chunk is not loaded, no need to log
                return null;
            }
        } else {
            return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskLater(plugin, task, delay));
        }
    }

    /**
     * Run a delayed task on the entity's region (Folia) or main thread (Paper)
     */
    public TaskWrapper runTaskLater(Entity entity, Runnable task, long delay) {
        if (isFolia) {
            try {
                Object entityScheduler = entity.getClass().getMethod("getScheduler").invoke(entity);
                Object scheduledTask = entityScheduler.getClass()
                        .getMethod("runDelayed", Plugin.class, Consumer.class, Runnable.class, long.class)
                        .invoke(entityScheduler, plugin, (Consumer<Object>) t -> task.run(), null, delay);
                return new FoliaTaskWrapper(scheduledTask);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to schedule Folia delayed entity task: " + e.getMessage());
                return null;
            }
        } else {
            return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskLater(plugin, task, delay));
        }
    }

    /**
     * Run a repeating task asynchronously
     */
    public TaskWrapper runTaskTimerAsynchronously(Runnable task, long delay, long period) {
        if (isFolia) {
            try {
                // Convert ticks to milliseconds (1 tick = 50ms)
                long delayMs = delay * 50;
                long periodMs = period * 50;

                // Create a cancellable wrapper for Folia tasks before scheduling
                // The wrapper is used in the task lambda below to check cancellation state
                FoliaTaskWrapper wrapper = new FoliaTaskWrapper(null);

                // Schedule the task with cancellation check
                // Note: The wrapper is captured in this lambda and won't be visible to other
                // threads until this method returns, so there's no race condition risk
                Object scheduledTask = runAtFixedRateMethod.invoke(asyncScheduler, plugin,
                        (Consumer<Object>) t -> {
                            if (!wrapper.isCancelled()) {
                                task.run();
                            }
                        }, delayMs, periodMs, TimeUnit.MILLISECONDS);

                // Set the scheduled task reference for potential external cancellation attempts
                wrapper.setTask(scheduledTask);
                return wrapper;
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to schedule Folia repeating async task: " + e.getMessage());
                return null;
            }
        } else {
            return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, period));
        }
    }

    public TaskWrapper runTaskTimer(Runnable task, long delay, long period) {
        if (isFolia) {
            try {
                // Convert ticks to milliseconds (1 tick = 50ms)
                long delayMs = delay * 50;
                long periodMs = period * 50;

                // Create a cancellable wrapper for Folia tasks before scheduling
                // The wrapper is used in the task lambda below to check cancellation state
                FoliaTaskWrapper wrapper = new FoliaTaskWrapper(null);

                // Schedule the task with cancellation check
                // Note: The wrapper is captured in this lambda and won't be visible to other
                // threads until this method returns, so there's no race condition risk
                Object scheduledTask = runAtFixedRateMethod.invoke(asyncScheduler, plugin,
                        (Consumer<Object>) t -> {
                            if (!wrapper.isCancelled()) {
                                task.run();
                            }
                        }, delayMs, periodMs, TimeUnit.MILLISECONDS);

                // Set the scheduled task reference for potential external cancellation attempts
                wrapper.setTask(scheduledTask);
                return wrapper;
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to schedule Folia repeating async task: " + e.getMessage());
                return null;
            }
        } else {
            return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period));
        }
    }

    /**
     * Wrapper interface for scheduled tasks
     */
    public interface TaskWrapper {
        void cancel();
        boolean isCancelled();
    }

    /**
     * Wrapper for Bukkit tasks
     */
    private static class BukkitTaskWrapper implements TaskWrapper {
        private final org.bukkit.scheduler.BukkitTask task;

        public BukkitTaskWrapper(org.bukkit.scheduler.BukkitTask task) {
            this.task = task;
        }

        @Override
        public void cancel() {
            if (task != null) {
                task.cancel();
            }
        }

        @Override
        public boolean isCancelled() {
            return task == null || task.isCancelled();
        }
    }

    /**
     * Wrapper for Folia tasks
     * Folia's AsyncScheduler.runAtFixedRate() returns a ScheduledTask that cannot be cancelled
     * externally, so we track cancellation state internally and check it in the task execution
     */
    private static class FoliaTaskWrapper implements TaskWrapper {
        @Setter
        private volatile Object task;
        private volatile boolean cancelled = false;

        public FoliaTaskWrapper(Object task) {
            this.task = task;
        }

        @Override
        public void cancel() {
            this.cancelled = true;
            // Also attempt to cancel the underlying Folia task if possible
            if (task != null) {
                try {
                    task.getClass().getMethod("cancel").invoke(task);
                } catch (Exception e) {
                    // Ignore - some Folia tasks may not support external cancellation
                }
            }
        }

        @Override
        public boolean isCancelled() {
            if (cancelled) return true;
            if (task == null) return true;
            try {
                return (boolean) task.getClass().getMethod("isCancelled").invoke(task);
            } catch (Exception e) {
                return cancelled;
            }
        }
    }
}