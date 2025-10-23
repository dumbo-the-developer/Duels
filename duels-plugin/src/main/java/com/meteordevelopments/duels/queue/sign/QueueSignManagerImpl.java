package com.meteordevelopments.duels.queue.sign;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.api.event.queue.sign.QueueSignCreateEvent;
import com.meteordevelopments.duels.api.event.queue.sign.QueueSignRemoveEvent;
import com.meteordevelopments.duels.api.queue.sign.QueueSign;
import com.meteordevelopments.duels.api.queue.sign.QueueSignManager;
import com.meteordevelopments.duels.config.Lang;
import com.meteordevelopments.duels.data.QueueSignData;
import com.meteordevelopments.duels.queue.Queue;
import com.meteordevelopments.duels.queue.QueueManager;
import com.meteordevelopments.duels.util.Loadable;
import com.meteordevelopments.duels.util.Log;
import com.meteordevelopments.duels.util.io.FileUtil;
import com.meteordevelopments.duels.util.json.JsonUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.arim.morepaperlib.scheduling.ScheduledTask;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class QueueSignManagerImpl implements Loadable, QueueSignManager, Listener {

    private static final String FILE_NAME = "signs.json";

    private static final String SIGNS_LOADED = "&2Loaded %s queue sign(s).";

    private final DuelsPlugin plugin;
    private final Lang lang;
    private final QueueManager queueManager;
    private final File file;

    private final Map<Location, QueueSignImpl> signs = new HashMap<>();

    private ScheduledTask updateTask;

    public QueueSignManagerImpl(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLang();
        this.queueManager = plugin.getQueueManager();
        this.file = new File(plugin.getDataFolder(), FILE_NAME);

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() throws IOException {
        if (FileUtil.checkNonEmpty(file, true)) {
            try (final Reader reader = new InputStreamReader(Files.newInputStream(file.toPath()), Charsets.UTF_8)) {
                final List<QueueSignData> data = JsonUtil.getObjectMapper().readValue(reader, new TypeReference<>() {
                });

                if (data != null) {
                    data.forEach(queueSignData -> {
                        final QueueSignImpl queueSign = queueSignData.toQueueSign(plugin);

                        if (queueSign != null) {
                            signs.put(queueSign.getLocation(), queueSign);
                        }
                    });
                }
            }
        }

        DuelsPlugin.sendMessage(String.format(SIGNS_LOADED, signs.size()));

        this.updateTask = plugin.doSyncRepeat(() -> signs.entrySet().removeIf(entry -> {
            entry.getValue().update();
            return entry.getValue().getQueue().isRemoved();
        }), 20L, 20L);
    }

    @Override
    public void handleUnload() {
        plugin.cancelTask(updateTask);
        signs.clear();
    }

    private void saveQueueSigns() {
        final List<QueueSignData> data = new ArrayList<>();

        for (final QueueSignImpl sign : signs.values()) {
            if (sign.getQueue().isRemoved()) {
                continue;
            }

            data.add(new QueueSignData(sign));
        }

        try (final Writer writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()), Charsets.UTF_8)) {
            JsonUtil.getObjectWriter().writeValue(writer, data);
            writer.flush();
        } catch (IOException ex) {
            Log.error(this, ex.getMessage(), ex);
        }
    }

    @Nullable
    @Override
    public QueueSignImpl get(@NotNull final Sign sign) {
        Objects.requireNonNull(sign, "sign");
        return get(sign.getLocation());
    }

    public QueueSignImpl get(final Location location) {
        return signs.get(location);
    }

    public boolean create(final Player creator, final Location location, final Queue queue) {
        if (get(location) != null) {
            return false;
        }

        final QueueSignImpl created;
        final String kitName = queue.getKit() != null ? queue.getKit().getName() : lang.getMessage("GENERAL.none");
        signs.put(location, created = new QueueSignImpl(location, lang.getMessage("SIGN.format", "name", queue.getName(), "kit", kitName, "bet_amount", queue.getBet()), queue, lang));
        signs.values().stream().filter(sign -> sign.equals(created)).forEach(QueueSignImpl::update);
        saveQueueSigns();

        final QueueSignCreateEvent event = new QueueSignCreateEvent(creator, created);
        Bukkit.getPluginManager().callEvent(event);
        return true;
    }

    public QueueSignImpl remove(final Player source, final Location location) {
        final QueueSignImpl queueSign = signs.remove(location);

        if (queueSign == null) {
            return null;
        }

        queueSign.setRemoved(true);
        saveQueueSigns();

        final QueueSignRemoveEvent event = new QueueSignRemoveEvent(source, queueSign);
        Bukkit.getPluginManager().callEvent(event);
        return queueSign;
    }

    public Collection<QueueSignImpl> getSigns() {
        return signs.values();
    }

    @NotNull
    @Override
    public List<QueueSign> getQueueSigns() {
        return Lists.newArrayList(getSigns());
    }

    @EventHandler
    public void on(final PlayerInteractEvent event) {
        if (!event.hasBlock()) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null || !(block.getState() instanceof Sign)) {
            return;
        }

        Player player = event.getPlayer();
        QueueSignImpl sign = get(block.getLocation());

        if (sign != null && queueManager.queue(player, sign.getQueue())) {
            signs.values().stream()
                    .filter(queueSign -> queueSign.equals(sign))
                    .forEach(QueueSignImpl::update);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(final BlockBreakEvent event) {
        final Block block = event.getBlock();

        if (!(block.getState() instanceof Sign) || get(block.getLocation()) == null) {
            return;
        }

        final Player player = event.getPlayer();

        if (!player.hasPermission(Permissions.ADMIN)) {
            lang.sendMessage(player, "ERROR.no-permission", "permission", Permissions.ADMIN);
            return;
        }

        lang.sendMessage(player, "ERROR.sign.cancel-break");
        event.setCancelled(true);
    }
}
