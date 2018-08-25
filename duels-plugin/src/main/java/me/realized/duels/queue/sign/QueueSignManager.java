package me.realized.duels.queue.sign;

import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.event.queue.sign.QueueSignCreateEvent;
import me.realized.duels.api.event.queue.sign.QueueSignRemoveEvent;
import me.realized.duels.config.Lang;
import me.realized.duels.data.QueueSignData;
import me.realized.duels.extra.Permissions;
import me.realized.duels.queue.Queue;
import me.realized.duels.queue.QueueManager;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class QueueSignManager implements Loadable, me.realized.duels.api.queue.sign.QueueSignManager, Listener {

    private final DuelsPlugin plugin;
    private final Lang lang;
    private final QueueManager queueManager;
    private final File file;

    private final Map<Location, QueueSign> signs = new HashMap<>();

    private int updateTask;

    public QueueSignManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLang();
        this.queueManager = plugin.getQueueManager();
        this.file = new File(plugin.getDataFolder(), "signs.json");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() throws Exception {
        if (!file.exists()) {
            file.createNewFile();
        } else {
            try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
                final List<QueueSignData> data = plugin.getGson().fromJson(reader, new TypeToken<List<QueueSignData>>() {}.getType());

                if (data != null) {
                    data.forEach(queueSignData -> {
                        final QueueSign queueSign = queueSignData.toQueueSign(plugin);

                        if (queueSign != null) {
                            signs.put(queueSign.getLocation(), queueSign);
                        }
                    });
                }
            }
        }

        Log.info(this, "Loaded " + signs.size() + " queue sign(s).");

        this.updateTask = plugin.doSyncRepeat(() -> signs.entrySet().removeIf(entry -> {
            entry.getValue().updateCount();
            return entry.getValue().getQueue().isRemoved();
        }), 20L, 20L).getTaskId();
    }

    @Override
    public void handleUnload() throws Exception {
        plugin.cancelTask(updateTask);

        final List<QueueSignData> data = new ArrayList<>();

        for (final QueueSign sign : signs.values()) {
            if (sign.getQueue().isRemoved()) {
                continue;
            }

            data.add(new QueueSignData(sign));
        }

        if (!file.exists()) {
            file.createNewFile();
        }

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file))) {
            writer.write(plugin.getGson().toJson(data));
            writer.flush();
        }

        signs.clear();
    }

    @Nullable
    @Override
    public QueueSign get(@Nonnull final Sign sign) {
        Objects.requireNonNull(sign, "sign");
        return get(sign.getLocation());
    }

    public QueueSign get(final Location location) {
        return signs.get(location);
    }

    public boolean create(final Player creator, final Location location, final Queue queue) {
        if (get(location) != null) {
            return false;
        }

        final QueueSign created;
        final String kitName = queue.getKit() != null ? queue.getKit().getName() : "none";
        signs.put(location, created = new QueueSign(location, lang.getMessage("SIGN.format", "kit", kitName, "bet_amount", queue.getBet()), queue));
        signs.values().stream().filter(sign -> sign.equals(created)).forEach(QueueSign::updateCount);

        final QueueSignCreateEvent event = new QueueSignCreateEvent(creator, created);
        plugin.getServer().getPluginManager().callEvent(event);
        return true;
    }

    public QueueSign remove(final Player source, final Location location) {
        final QueueSign queueSign = signs.remove(location);

        if (queueSign == null) {
            return null;
        }

        queueSign.setRemoved(true);

        final QueueSignRemoveEvent event = new QueueSignRemoveEvent(source, queueSign);
        plugin.getServer().getPluginManager().callEvent(event);
        return queueSign;
    }

    public Collection<QueueSign> getSigns() {
        return signs.values();
    }

    @Nonnull
    @Override
    public List<QueueSign> getQueueSigns() {
        return Lists.newArrayList(getSigns());
    }

    @EventHandler
    public void on(final PlayerInteractEvent event) {
        final Block block;

        if (!event.hasBlock() || !((block = event.getClickedBlock()).getState() instanceof Sign)) {
            return;
        }

        final Player player = event.getPlayer();
        final QueueSign sign = get(block.getLocation());

        if (sign == null || !queueManager.queue(player, sign.getQueue())) {
            return;
        }

        signs.values().stream().filter(queueSign -> queueSign.equals(sign)).forEach(QueueSign::updateCount);
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
