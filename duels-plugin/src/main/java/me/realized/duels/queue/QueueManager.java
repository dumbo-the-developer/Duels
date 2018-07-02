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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.data.QueueSignData;
import me.realized.duels.data.UserData;
import me.realized.duels.data.UserManager;
import me.realized.duels.duel.DuelManager;
import me.realized.duels.extra.Permissions;
import me.realized.duels.hooks.VaultHook;
import me.realized.duels.kit.Kit;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import me.realized.duels.util.RatingUtil;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class QueueManager implements Loadable, Listener {

    private final DuelsPlugin plugin;
    private final Config config;
    private final Lang lang;
    private final UserManager userManager;
    private final DuelManager duelManager;
    private final File file;
    private final Map<Sign, QueueSign> signs = new HashMap<>();
    private final Map<QueueSign, LinkedList<Player>> queues = new HashMap<>();

    private VaultHook vault;
    private int queueTask;

    public QueueManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.userManager = plugin.getUserManager();
        this.duelManager = plugin.getDuelManager();
        this.file = new File(plugin.getDataFolder(), "signs.json");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() throws Exception {
        this.vault = plugin.getHookManager().getHook(VaultHook.class);

        if (!file.exists()) {
            file.createNewFile();
        } else {
            try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
                final List<QueueSignData> data = plugin.getGson().fromJson(reader, new TypeToken<List<QueueSignData>>() {}.getType());

                if (data != null) {
                    data.forEach(queueSignData -> {
                        final QueueSign queueSign = queueSignData.toQueueSign(plugin);

                        if (queueSign != null) {
                            final Sign sign = queueSign.getSign();
                            signs.put(sign, queueSign);
                        }
                    });
                }
            }
        }

        Log.info(this, "Loaded " + signs.size() + " queue sign(s).");

        this.queueTask = plugin.doSyncRepeat(() -> queues.forEach((sign, queue) -> {
            final Set<Player> remove = new HashSet<>();

            for (final Player current : queue) {
                // player is already in a match
                if (remove.contains(current)) {
                    continue;
                }

                for (final Player opponent : queue) {
                    // opponent is already in a match
                    if (current.equals(opponent) || remove.contains(opponent)) {
                        continue;
                    }

                    if (!canFight(sign.getKit(), userManager.get(current), userManager.get(opponent))) {
                        continue;
                    }

                    remove.add(current);
                    remove.add(opponent);

                    final Settings setting = new Settings(plugin);
                    setting.setKit(sign.getKit());
                    setting.setBet(sign.getBet());

                    final String kit = sign.getKit() != null ? sign.getKit().getName() : "none";
                    lang.sendMessage(current, "SIGN.found-opponent", "name", opponent.getName(), "kit", kit, "bet_amount", sign.getBet());
                    lang.sendMessage(opponent, "SIGN.found-opponent", "name", current.getName(), "kit", kit, "bet_amount", sign.getBet());
                    duelManager.startMatch(current, opponent, setting, null, true);
                }
            }

            queue.removeAll(remove);
            signs.values().stream().filter(queueSign -> queueSign.equals(sign)).forEach(queueSign -> queueSign.setCount(queue.size()));
        }), 20L, 40L);
    }

    private boolean canFight(final Kit kit, final UserData first, final UserData second) {
        if (kit == null || !config.isRatingEnabled()) {
            return true;
        }

        if (first != null && second != null) {
            final int firstRating = first.getRating(kit);
            final int secondRating = second.getRating(kit);
            final int kFactor = config.getKFactor();
            return RatingUtil.getChange(kFactor, firstRating, secondRating) != 0 && RatingUtil.getChange(kFactor, secondRating, firstRating) != 0;
        }

        return false;
    }

    @Override
    public void handleUnload() throws Exception {
        plugin.cancelTask(queueTask);

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

    public QueueSign get(final Sign sign) {
        return signs.get(sign);
    }

    public QueueSign remove(final Sign sign) {
        final QueueSign queueSign = signs.remove(sign);

        if (queueSign != null && signs.values().stream().noneMatch(s -> s.equals(queueSign))) {
            final Queue<Player> queue = queues.remove(queueSign);

            if (queue != null) {
                queue.forEach(player -> lang.sendMessage(player, "SIGN.remove"));
            }
        }

        return queueSign;
    }

    public void remove(final Player player) {
        queues.forEach((sign, queue) -> {
            if (queue.remove(player)) {
                lang.sendMessage(player, "SIGN.remove");
                signs.values().stream().filter(queueSign -> queueSign.equals(sign)).forEach(queueSign -> queueSign.setCount(queue.size()));
            }
        });
    }

    public Queue<Player> get(final Player player) {
        return queues.entrySet().stream().filter(entry -> entry.getValue().contains(player)).findFirst().map(Entry::getValue).orElse(null);
    }

    public Queue<Player> get(final QueueSign sign) {
        return queues.computeIfAbsent(sign, result -> new LinkedList<>());
    }

    public boolean create(final Sign sign, final Kit kit, final int bet) {
        if (get(sign) != null) {
            return false;
        }

        final QueueSign created;
        final String kitName = kit != null ? kit.getName() : "none";
        signs.put(sign, created = new QueueSign(sign, lang.getMessage("SIGN.format", "kit", kitName, "bet_amount", bet), kit, bet));

        final Queue<Player> queue = get(created);
        signs.values().stream().filter(queueSign -> queueSign.equals(created)).forEach(queueSign -> queueSign.setCount(queue.size()));
        return true;
    }

    public Collection<QueueSign> getSigns() {
        return signs.values();
    }

    @EventHandler
    public void on(final PlayerInteractEvent event) {
        final BlockState state;

        if (!event.hasBlock() || !((state = event.getClickedBlock().getState()) instanceof Sign)) {
            return;
        }

        final Player player = event.getPlayer();
        final QueueSign sign = get((Sign) state);

        if (sign == null) {
            return;
        }

        final Queue<Player> queue = get(sign);
        final Queue<Player> found = get(player);

        if (found != null) {
            if (found.equals(queue)) {
                queue.remove(player);
                lang.sendMessage(player, "SIGN.remove");
                signs.values().stream().filter(queueSign -> queueSign.equals(sign)).forEach(queueSign -> queueSign.setCount(queue.size()));
                return;
            }

            lang.sendMessage(player, "ERROR.sign.already-in");
            return;
        }

        if (sign.getBet() > 0 && vault != null && !vault.has(sign.getBet(), player)) {
            lang.sendMessage(player, "ERROR.sign.not-enough-money", "bet_amount", sign.getBet());
            return;
        }

        queue.add(player);

        final String kit = sign.getKit() != null ? sign.getKit().getName() : "none";
        lang.sendMessage(player, "SIGN.add", "kit", kit, "bet_amount", sign.getBet());
        signs.values().stream().filter(queueSign -> queueSign.equals(sign)).forEach(queueSign -> queueSign.setCount(queue.size()));
    }

    @EventHandler(ignoreCancelled = true)
    public void on(final BlockBreakEvent event) {
        final BlockState state = event.getBlock().getState();

        if (!(state instanceof Sign)) {
            return;
        }

        final Sign sign = (Sign) state;

        if (get(sign) == null) {
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

    @EventHandler
    public void on(final PlayerQuitEvent event) {
        final Queue<Player> queue = get(event.getPlayer());

        if (queue == null) {
            return;
        }

        queue.remove(event.getPlayer());
    }
}
