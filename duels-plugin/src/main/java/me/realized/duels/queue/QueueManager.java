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
import java.util.Objects;
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
import me.realized.duels.hooks.CombatTagPlusHook;
import me.realized.duels.hooks.PvPManagerHook;
import me.realized.duels.hooks.VaultHook;
import me.realized.duels.hooks.WorldGuardHook;
import me.realized.duels.kit.Kit;
import me.realized.duels.setting.CachedInfo;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import me.realized.duels.util.RatingUtil;
import me.realized.duels.util.inventory.InventoryUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
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
    private final Map<QueueSign, LinkedList<QueueEntry>> queues = new HashMap<>();

    private CombatTagPlusHook combatTagPlus;
    private PvPManagerHook pvpManager;
    private WorldGuardHook worldGuard;
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
        this.combatTagPlus = plugin.getHookManager().getHook(CombatTagPlusHook.class);
        this.pvpManager = plugin.getHookManager().getHook(PvPManagerHook.class);
        this.worldGuard = plugin.getHookManager().getHook(WorldGuardHook.class);
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
            final Set<QueueEntry> remove = new HashSet<>();

            for (final QueueEntry current : queue) {
                // player is already in a match
                if (remove.contains(current)) {
                    continue;
                }

                final Player player = current.player;

                for (final QueueEntry opponent : queue) {
                    final Player other = opponent.player;

                    // opponent is already in a match or the rating difference is too high
                    if (current.equals(opponent) || remove.contains(opponent) || !canFight(sign.getKit(), userManager.get(player), userManager.get(other))) {
                        continue;
                    }

                    remove.add(current);
                    remove.add(opponent);

                    final Settings setting = new Settings(plugin);
                    setting.setKit(sign.getKit());
                    setting.setBet(sign.getBet());
                    setting.getCache().put(player.getUniqueId(), current.info);
                    setting.getCache().put(other.getUniqueId(), opponent.info);

                    final String kit = sign.getKit() != null ? sign.getKit().getName() : "none";
                    lang.sendMessage(player, "SIGN.found-opponent", "name", other.getName(), "kit", kit, "bet_amount", sign.getBet());
                    lang.sendMessage(other, "SIGN.found-opponent", "name", player.getName(), "kit", kit, "bet_amount", sign.getBet());
                    duelManager.startMatch(player, other, setting, null, true);
                    break;
                }
            }

            queue.removeAll(remove);
            signs.values().stream().filter(queueSign -> queueSign.equals(sign)).forEach(queueSign -> queueSign.setCount(queue.size()));
        }), 20L, 40L).getTaskId();
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
            final Queue<QueueEntry> queue = queues.remove(queueSign);

            if (queue != null) {
                queue.forEach(entry -> lang.sendMessage(entry.player, "SIGN.remove"));
            }
        }

        return queueSign;
    }

    public void remove(final Player player) {
        for (final Map.Entry<QueueSign, LinkedList<QueueEntry>> entry : queues.entrySet()) {
            final LinkedList<QueueEntry> queue = entry.getValue();

            if (queue.removeIf(queueEntry -> queueEntry.player.equals(player))) {
                lang.sendMessage(player, "SIGN.remove");
                signs.values().stream().filter(queueSign -> queueSign.equals(entry.getKey())).forEach(queueSign -> queueSign.setCount(queue.size()));
                break;
            }
        }
    }

    public Queue<QueueEntry> get(final Player player) {
        return queues.entrySet()
            .stream().filter(entry -> entry.getValue().stream().anyMatch(queueEntry -> queueEntry.player.equals(player)))
            .findFirst().map(Entry::getValue).orElse(null);
    }

    public Queue<QueueEntry> get(final QueueSign sign) {
        return queues.computeIfAbsent(sign, result -> new LinkedList<>());
    }

    public boolean create(final Sign sign, final Kit kit, final int bet) {
        if (get(sign) != null) {
            return false;
        }

        final QueueSign created;
        final String kitName = kit != null ? kit.getName() : "none";
        signs.put(sign, created = new QueueSign(sign, lang.getMessage("SIGN.format", "kit", kitName, "bet_amount", bet), kit, bet));

        final Queue<QueueEntry> queue = get(created);
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

        final Queue<QueueEntry> queue = get(sign);
        final Queue<QueueEntry> found = get(player);

        if (found != null) {
            if (found.equals(queue)) {
                queue.removeIf(entry -> entry.player.equals(player));
                lang.sendMessage(player, "SIGN.remove");
                signs.values().stream().filter(queueSign -> queueSign.equals(sign)).forEach(queueSign -> queueSign.setCount(queue.size()));
                return;
            }

            lang.sendMessage(player, "ERROR.sign.already-in");
            return;
        }

        if (config.isRequiresClearedInventory() && InventoryUtil.hasItem(player)) {
            lang.sendMessage(player, "ERROR.duel.inventory-not-empty");
            return;
        }

        GameMode gameMode = null;

        if (config.isPreventCreativeMode() && (gameMode = player.getGameMode()) == GameMode.CREATIVE) {
            lang.sendMessage(player, "ERROR.duel.in-creative-mode");
            return;
        }

        if ((combatTagPlus != null && combatTagPlus.isTagged(player)) || (pvpManager != null && pvpManager.isTagged(player))) {
            lang.sendMessage(player, "ERROR.duel.is-tagged");
            return;
        }

        String duelzone = null;

        if (worldGuard != null && config.isDuelzoneEnabled() && (duelzone = worldGuard.findDuelZone(player)) == null) {
            lang.sendMessage(player, "ERROR.duel.not-in-duelzone", "regions", config.getDuelzones());
            return;
        }

        if (sign.getBet() > 0 && vault != null && !vault.has(sign.getBet(), player)) {
            lang.sendMessage(player, "ERROR.sign.not-enough-money", "bet_amount", sign.getBet());
            return;
        }

        queue.add(new QueueEntry(player, player.getLocation().clone(), duelzone, gameMode));

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
        final Player player = event.getPlayer();
        final Queue<QueueEntry> queue = get(player);

        if (queue == null) {
            return;
        }

        queue.removeIf(entry -> entry.player.equals(player));
    }

    private class QueueEntry {

        private final Player player;
        private final CachedInfo info;

        QueueEntry(final Player player, final Location location, final String duelzone, final GameMode gameMode) {
            this.player = player;
            this.info = new CachedInfo(location, duelzone, gameMode);
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) { return true; }
            if (other == null || getClass() != other.getClass()) { return false; }
            final QueueEntry that = (QueueEntry) other;
            return Objects.equals(player, that.player);
        }

        @Override
        public int hashCode() {
            return Objects.hash(player);
        }
    }
}
