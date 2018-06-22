package me.realized.duels.queue;

import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.data.QueueSignData;
import me.realized.duels.duel.DuelManager;
import me.realized.duels.hooks.VaultHook;
import me.realized.duels.kit.Kit;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import me.realized.duels.util.StringUtil;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class QueueManager implements Loadable, Listener {

    private final DuelsPlugin plugin;
    private final Config config;
    private final Lang lang;
    private final DuelManager duelManager;
    private final File file;
    private final Map<Sign, QueueSign> signs = new HashMap<>();
    private final Map<QueueSign, Queue<Player>> queues = new HashMap<>();

    private VaultHook vault;

    public QueueManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.duelManager = plugin.getDuelManager();
        this.file = new File(plugin.getDataFolder(), "signs.json");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() throws Exception {
        this.vault = plugin.getHookManager().getHook(VaultHook.class);

        if (!file.exists()) {
            file.createNewFile();
            return;
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
            final List<QueueSignData> data = plugin.getGson().fromJson(reader, new TypeToken<List<QueueSignData>>() {}.getType());

            if (data != null) {
                data.forEach(queueSignData -> {
                    final QueueSign queueSign = queueSignData.toQueueSign(plugin);

                    if (queueSign != null) {
                        final Sign sign = queueSign.getSign();
                        signs.put(sign, queueSign);
                        sign.setLine(0, StringUtil.color("&a[Click to Join]"));
                        sign.setLine(1, StringUtil.color("&c" + (queueSign.getKit() != null ? queueSign.getKit().getName() : "none")));
                        sign.setLine(2, StringUtil.color("&6$" + queueSign.getBet()));
                        sign.setLine(3, StringUtil.color("&f&l0 IN QUEUE"));
                        sign.update();
                    }
                });
            }
        }

        Log.info(this, "Loaded " + signs.size() + " queue sign(s).");

        plugin.doSyncRepeat(() -> queues.forEach((sign, queue) -> {
            if (queue.size() >= 2) {
                final Player first = queue.poll();
                final Player second = queue.poll();
                final Settings setting = new Settings(plugin);
                setting.setKit(sign.getKit());
                setting.setBet(sign.getBet());
                duelManager.startMatch(first, second, setting, null, true);
                signs.values().stream().filter(queueSign -> queueSign.equals(sign)).forEach(queueSign -> {
                    queueSign.getSign().setLine(3, StringUtil.color("&f&l" + queue.size() + " IN QUEUE"));
                    queueSign.getSign().update();
                });
            }
        }), 20L, 40L);
    }

    @Override
    public void handleUnload() throws Exception {
        if (signs.isEmpty()) {
            return;
        }

        final List<QueueSignData> data = new ArrayList<>();

        for (final QueueSign sign : signs.values()) {
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
        queues.clear();
    }

    public Queue<Player> get(final Player player) {
        return queues.entrySet().stream().filter(entry -> entry.getValue().contains(player)).findFirst().map(Entry::getValue).orElse(null);
    }

    public QueueSign get(final Sign sign) {
        return signs.get(sign);
    }

    public Queue<Player> get(final QueueSign sign) {
        return queues.computeIfAbsent(sign, result -> new LinkedList<>());
    }

    public void add(final QueueSign sign, final Player player) {
        // Update all associated signs as well. Remember that one queue will have many QueueSigns linked to it!
    }

    public boolean create(final Sign sign, final Kit kit, final int bet) {
        if (get(sign) != null) {
            return false;
        }

        signs.put(sign, new QueueSign(sign, kit, bet));
        return true;
    }

    @EventHandler
    public void on(final PlayerInteractEvent event) {
        if (!event.hasBlock() || !(event.getClickedBlock().getState() instanceof Sign)) {
            return;
        }

        final Player player = event.getPlayer();
        final QueueSign sign = get((Sign) event.getClickedBlock().getState());

        if (sign == null) {
            player.sendMessage("not a queue sign");
            return;
        }

        final Queue<Player> queue = get(sign);
        final Queue<Player> found = get(player);

        if (found != null) {
            if (found.equals(queue)) {
                queue.remove(player);
                player.sendMessage("Removed from queue");
                signs.values().stream().filter(queueSign -> queueSign.equals(sign)).forEach(queueSign -> {
                    queueSign.getSign().setLine(3, StringUtil.color("&f&l" + queue.size() + " IN QUEUE"));
                    queueSign.getSign().update();
                });
                return;
            }

            player.sendMessage("Already in queue");
            return;
        }

        if (sign.getBet() > 0 && vault != null && !vault.has(sign.getBet(), player)) {
            player.sendMessage("You do not have enough money to join this queue");
            return;
        }

        queue.add(player);
        player.sendMessage("Added to queue");
        signs.values().stream().filter(queueSign -> queueSign.equals(sign)).forEach(queueSign -> {
            queueSign.getSign().setLine(3, StringUtil.color("&f&l" + queue.size() + " IN QUEUE"));
            queueSign.getSign().update();
        });
    }
}
