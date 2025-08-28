package com.meteordevelopments.duels.arena;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Charsets;
import lombok.Getter;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.api.arena.Arena;
import com.meteordevelopments.duels.api.arena.ArenaManager;
import com.meteordevelopments.duels.api.event.arena.ArenaCreateEvent;
import com.meteordevelopments.duels.api.event.arena.ArenaRemoveEvent;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.config.Lang;
import com.meteordevelopments.duels.data.ArenaData;
import com.meteordevelopments.duels.kit.KitImpl;
import com.meteordevelopments.duels.queue.Queue;
import com.meteordevelopments.duels.util.Loadable;
import com.meteordevelopments.duels.util.Log;
import com.meteordevelopments.duels.util.StringUtil;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.gui.MultiPageGui;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import com.meteordevelopments.duels.util.io.FileUtil;
import com.meteordevelopments.duels.util.json.JsonUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class ArenaManagerImpl implements Loadable, ArenaManager {

    private static final String FILE_NAME = "arenas.json";

    private static final String ERROR_NOT_ALPHANUMERIC = "&c&lCould not load arena %s: Name is not alphanumeric.";
    private static final String ARENAS_LOADED = "&2Loaded %s arena(s).";

    private final DuelsPlugin plugin;
    private final Config config;
    private final Lang lang;
    private final File file;

    private final List<ArenaImpl> arenas = new ArrayList<>();

    @Getter
    private MultiPageGui<DuelsPlugin> gui;

    public ArenaManagerImpl(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.file = new File(plugin.getDataFolder(), FILE_NAME);

        Bukkit.getPluginManager().registerEvents(new ArenaListener(), plugin);
    }

    @Override
    public void handleLoad() throws IOException {
        gui = new MultiPageGui<>(plugin, lang.getMessage("GUI.arena-selector.title"), config.getArenaSelectorRows(), arenas);
        gui.setSpaceFiller(Items.from(config.getArenaSelectorFillerType(), config.getArenaSelectorFillerData()));
        gui.setPrevButton(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.kit-selector.buttons.previous-page.name")).build());
        gui.setNextButton(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.kit-selector.buttons.next-page.name")).build());
        gui.setEmptyIndicator(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.kit-selector.buttons.empty.name")).build());
        plugin.getGuiListener().addGui(gui);

        // Load arenas from MongoDB instead of file
        try {
            final var mongo = plugin.getMongoService();
            if (mongo != null) {
                final var collection = mongo.collection("arenas");
                for (final org.bson.Document doc : collection.find()) {
                    final String json = doc.toJson();
                    final ArenaData arenaData = JsonUtil.getObjectMapper().readValue(json, com.meteordevelopments.duels.data.ArenaData.class);
                    if (arenaData != null) {
                        if (!StringUtil.isAlphanumeric(arenaData.getName())) {
                            DuelsPlugin.sendMessage(String.format(ERROR_NOT_ALPHANUMERIC, arenaData.getName()));
                            continue;
                        }
                        arenas.add(arenaData.toArena(plugin));
                    }
                }
            }
        } catch (Exception ex) {
            Log.error(this, ex.getMessage(), ex);
        }

        DuelsPlugin.sendMessage(String.format(ARENAS_LOADED, arenas.size()));
        gui.calculatePages();
    }

    @Override
    public void handleUnload() {
        if (gui != null) {
            plugin.getGuiListener().removeGui(gui);
        }

        arenas.clear();
    }

    void saveArenas() {
        try {
            final var mongo = plugin.getMongoService();
            if (mongo != null) {
                final var collection = mongo.collection("arenas");
                for (final ArenaImpl arena : arenas) {
                    final ArenaData data = new ArenaData(arena);
                    final String json = JsonUtil.getObjectWriter().writeValueAsString(data);
                    final org.bson.Document doc = org.bson.Document.parse(json);
                    doc.put("_id", data.getName());
                    collection.replaceOne(
                        new org.bson.Document("_id", data.getName()),
                        doc,
                        new com.mongodb.client.model.ReplaceOptions().upsert(true)
                    );
                }
                // Prune documents that no longer exist in memory
                final java.util.Set<String> names =
                    arenas.stream()
                          .map(ArenaImpl::getName)
                          .collect(java.util.stream.Collectors.toSet());
                collection.deleteMany(
                    new org.bson.Document("_id", new org.bson.Document("$nin", names))
                );
                if (plugin.getRedisService() != null) {
                    arenas.forEach(a ->
                        plugin.getRedisService().publish(
                            com.meteordevelopments.duels.redis.RedisService.CHANNEL_INVALIDATE_ARENA,
                            a.getName()
                        )
                    );
                }
            }
        } catch (Exception ex) {
            Log.error(this, ex.getMessage(), ex);
        }
    }

    @Nullable
    @Override
    public ArenaImpl get(@NotNull final String name) {
        Objects.requireNonNull(name, "name");
        return arenas.stream().filter(arena -> arena.getName().equals(name)).findFirst().orElse(null);
    }

    // Called by Redis subscriber
    public void reloadArena(@NotNull final String name) {
        final var mongo = plugin.getMongoService();
        if (mongo == null) { return; }
        try {
            final var doc = mongo.collection("arenas").find(new org.bson.Document("_id", name)).first();
            if (doc == null) {
                arenas.removeIf(a -> a.getName().equals(name));
                if (gui != null) { gui.calculatePages(); }
                return;
            }
            final String json = doc.toJson();
            final com.meteordevelopments.duels.data.ArenaData data = com.meteordevelopments.duels.util.json.JsonUtil.getObjectMapper().readValue(json, com.meteordevelopments.duels.data.ArenaData.class);
            if (data == null) { return; }
            final ArenaImpl arena = data.toArena(plugin);
            // Replace existing
            arenas.removeIf(a -> a.getName().equals(name));
            arenas.add(arena);
            if (gui != null) { gui.calculatePages(); }
        } catch (Exception ignored) {}
    }

    @Nullable
    @Override
    public ArenaImpl get(@NotNull final Player player) {
        Objects.requireNonNull(player, "player");
        return arenas.stream().filter(arena -> arena.has(player)).findFirst().orElse(null);
    }

    @Override
    public boolean isInMatch(@NotNull final Player player) {
        Objects.requireNonNull(player, "player");
        return get(player) != null;
    }

    @NotNull
    @Override
    public List<Arena> getArenas() {
        return Collections.unmodifiableList(arenas);
    }

    public boolean create(final CommandSender source, final String name) {
        if (get(name) != null) {
            return false;
        }

        final ArenaImpl arena = new ArenaImpl(plugin, name);
        arenas.add(arena);
        saveArenas();

        final ArenaCreateEvent event = new ArenaCreateEvent(source, arena);
        Bukkit.getPluginManager().callEvent(event);
        gui.calculatePages();
        return true;
    }

    public boolean remove(final CommandSender source, final ArenaImpl arena) {
        if (arenas.remove(arena)) {
            arena.setRemoved(true);
            saveArenas();
            try {
                if (plugin.getRedisService() != null) {
                    plugin.getRedisService().publish(com.meteordevelopments.duels.redis.RedisService.CHANNEL_INVALIDATE_ARENA, arena.getName());
                }
            } catch (Exception ignored) {}

            final ArenaRemoveEvent event = new ArenaRemoveEvent(source, arena);
            Bukkit.getPluginManager().callEvent(event);
            gui.calculatePages();
            return true;
        }

        return false;
    }

    public List<ArenaImpl> getArenasImpl() {
        return arenas;
    }

    public Set<Player> getPlayers() {
        return arenas.stream().flatMap(arena -> arena.getPlayers().stream()).collect(Collectors.toSet());
    }

    public long getPlayersInMatch(final Queue queue) {
        return arenas.stream().filter(arena -> arena.isUsed() && arena.getMatch().isFromQueue() && arena.getMatch().getSource().equals(queue)).count() * 2;
    }

    public boolean isSelectable(@Nullable final KitImpl kit, @NotNull final ArenaImpl arena) {
        if (!arena.isAvailable()) {
            return false;
        }

        if (arena.isBoundless()) {
            if (kit == null) {
                return true;
            } else {
                return !kit.isArenaSpecific();
            }
        }

        return arena.isBound(kit);
    }

    public ArenaImpl randomArena(final KitImpl kit) {
        final List<ArenaImpl> available = arenas.stream().filter(arena -> isSelectable(kit, arena)).collect(Collectors.toList());
        return !available.isEmpty() ? available.get(ThreadLocalRandom.current().nextInt(available.size())) : null;
    }

    public List<String> getNames() {
        return arenas.stream().map(ArenaImpl::getName).collect(Collectors.toList());
    }

    // Called on kit removal
    public void clearBinds(final KitImpl kit) {
        arenas.stream().filter(arena -> arena.isBound(kit)).forEach(arena -> arena.bind(kit));
    }

    private class ArenaListener implements Listener {

        @EventHandler(ignoreCancelled = true)
        public void on(final PlayerInteractEvent event) {
            if (!event.hasBlock() || !config.isPreventInteract()) {
                return;
            }

            final ArenaImpl arena = get(event.getPlayer());

            if (arena == null || !arena.isCounting()) {
                return;
            }

            event.setCancelled(true);
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final EntityDamageEvent event) {
            if (!config.isPreventPvp() || !(event.getEntity() instanceof Player)) {
                return;
            }

            final ArenaImpl arena = get((Player) event.getEntity());

            if (arena == null || !arena.isCounting()) {
                return;
            }

            event.setCancelled(true);
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final ProjectileLaunchEvent event) {
            if (!config.isPreventLaunchProjectile()) {
                return;
            }

            final ProjectileSource shooter = event.getEntity().getShooter();

            if (!(shooter instanceof Player)) {
                return;
            }

            final ArenaImpl arena = get((Player) shooter);

            if (arena == null || !arena.isCounting()) {
                return;
            }

            event.setCancelled(true);
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final PlayerMoveEvent event) {
            if (!config.isPreventMovement()) {
                return;
            }

            final Location from = event.getFrom();
            final Location to = event.getTo();

            if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
                return;
            }

            final ArenaImpl arena = get(event.getPlayer());

            if (arena == null || !arena.isCounting()) {
                return;
            }

            event.setTo(event.getFrom());
        }
    }
}
