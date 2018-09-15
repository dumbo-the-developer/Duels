package me.realized.duels.arena;

import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.Getter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.event.arena.ArenaCreateEvent;
import me.realized.duels.api.event.arena.ArenaRemoveEvent;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.data.ArenaData;
import me.realized.duels.kit.Kit;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.compat.Items;
import me.realized.duels.util.gui.MultiPageGui;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.projectiles.ProjectileSource;

public class ArenaManager implements Loadable, me.realized.duels.api.arena.ArenaManager, Listener {

    private static final long AUTO_SAVE_INTERVAL = 20L * 60 * 5;

    private final DuelsPlugin plugin;
    private final Config config;
    private final Lang lang;
    private final File file;
    @Getter
    private final Set<Arena> arenas = new HashSet<>();

    @Getter
    private MultiPageGui<DuelsPlugin> gui;
    private int autoSaveTask;

    public ArenaManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.file = new File(plugin.getDataFolder(), "arenas.json");
    }

    @Override
    public void handleLoad() throws IOException {
        gui = new MultiPageGui<>(plugin, lang.getMessage("GUI.arena-selector.title"), config.getArenaSelectorRows(), arenas);
        gui.setSpaceFiller(Items.from(config.getArenaSelectorFillerType(), config.getArenaSelectorFillerData()));
        plugin.getGuiListener().addGui(gui);

        if (config.isCdEnabled()) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }

        if (!file.exists()) {
            file.createNewFile();
        } else {
            try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
                final List<ArenaData> data = plugin.getGson().fromJson(reader, new TypeToken<List<ArenaData>>() {}.getType());

                if (data != null) {
                    for (final ArenaData arenaData : data) {
                        if (!StringUtil.isAlphanumeric(arenaData.getName())) {
                            Log.warn(this, "Excluding arena '" + arenaData.getName() + "' from load: Name is not alphanumeric.");
                            continue;
                        }

                        arenas.add(arenaData.toArena(plugin));
                    }
                }
            }
        }

        Log.info(this, "Loaded " + arenas.size() + " arena(s).");
        gui.calculatePages();

        this.autoSaveTask = plugin.doSyncRepeat(() -> {
            try {
                saveArenas();
            } catch (IOException ex) {
                Log.error(this, ex.getMessage(), ex);
            }
        }, AUTO_SAVE_INTERVAL, AUTO_SAVE_INTERVAL).getTaskId();
    }

    @Override
    public void handleUnload() throws IOException {
        plugin.cancelTask(autoSaveTask);

        if (gui != null) {
            plugin.getGuiListener().removeGui(gui);
        }

        saveArenas();
        arenas.clear();
    }

    private void saveArenas() throws IOException {
        final List<ArenaData> data = new ArrayList<>();

        for (final Arena arena : arenas) {
            data.add(new ArenaData(arena));
        }

        if (!file.exists()) {
            file.createNewFile();
        }

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file))) {
            plugin.getGson().toJson(data, writer);
            writer.flush();
        }
    }

    @Nullable
    @Override
    public Arena get(@Nonnull final String name) {
        Objects.requireNonNull(name, "name");
        return arenas.stream().filter(arena -> arena.getName().equals(name)).findFirst().orElse(null);
    }

    @Nullable
    @Override
    public Arena get(@Nonnull final Player player) {
        Objects.requireNonNull(player, "player");
        return arenas.stream().filter(arena -> arena.has(player)).findFirst().orElse(null);
    }

    public boolean create(final CommandSender source, final String name) {
        if (get(name) != null) {
            return false;
        }

        final Arena arena = new Arena(plugin, name);
        arenas.add(arena);
        final ArenaCreateEvent event = new ArenaCreateEvent(source, arena);
        plugin.getServer().getPluginManager().callEvent(event);
        gui.calculatePages();
        return true;
    }

    public boolean remove(final CommandSender source, final Arena arena) {
        if (arenas.remove(arena)) {
            arena.setRemoved(true);

            final ArenaRemoveEvent event = new ArenaRemoveEvent(source, arena);
            plugin.getServer().getPluginManager().callEvent(event);
            gui.calculatePages();
            return true;
        }

        return false;
    }

    @Override
    public boolean isInMatch(@Nonnull final Player player) {
        Objects.requireNonNull(player, "player");
        return get(player) != null;
    }

    public Set<Player> getPlayers() {
        return arenas.stream().flatMap(arena -> arena.getPlayers().stream()).collect(Collectors.toSet());
    }

    public Arena randomArena(final Kit kit) {
        final List<Arena> available = arenas.stream()
            .filter(arena -> (kit == null || !kit.isArenaSpecific() || kit.canUse(arena)) && arena.isAvailable())
            .collect(Collectors.toList());

        if (available.isEmpty()) {
            return null;
        }

        return available.get(ThreadLocalRandom.current().nextInt(available.size()));
    }

    @EventHandler(ignoreCancelled = true)
    public void on(final EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        final Arena arena = get((Player) event.getEntity());

        if (arena == null || !arena.isCounting()) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void on(final ProjectileLaunchEvent event) {
        final ProjectileSource shooter = event.getEntity().getShooter();

        if (!(shooter instanceof Player)) {
            return;
        }

        final Arena arena = get((Player) shooter);

        if (arena == null || !arena.isCounting()) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void on(final PlayerMoveEvent event) {
        final Location from = event.getFrom();
        final Location to = event.getTo();

        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        final Arena arena = get(event.getPlayer());

        if (arena == null || !arena.isCounting()) {
            return;
        }

        event.setTo(event.getFrom());
    }
}
