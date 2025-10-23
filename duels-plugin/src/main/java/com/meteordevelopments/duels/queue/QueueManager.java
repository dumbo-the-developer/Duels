package com.meteordevelopments.duels.queue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Charsets;
import com.meteordevelopments.duels.hook.hooks.*;
import lombok.Getter;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.api.event.queue.QueueCreateEvent;
import com.meteordevelopments.duels.api.event.queue.QueueJoinEvent;
import com.meteordevelopments.duels.api.event.queue.QueueLeaveEvent;
import com.meteordevelopments.duels.api.event.queue.QueueRemoveEvent;
import com.meteordevelopments.duels.api.kit.Kit;
import com.meteordevelopments.duels.api.queue.DQueue;
import com.meteordevelopments.duels.api.queue.DQueueManager;
import com.meteordevelopments.duels.arena.ArenaManagerImpl;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.config.Lang;
import com.meteordevelopments.duels.data.QueueData;
import com.meteordevelopments.duels.data.UserData;
import com.meteordevelopments.duels.data.UserManagerImpl;
import com.meteordevelopments.duels.duel.DuelManager;
import com.meteordevelopments.duels.hook.hooks.worldguard.WorldGuardHook;
import com.meteordevelopments.duels.kit.KitManagerImpl;
import com.meteordevelopments.duels.setting.Settings;
import com.meteordevelopments.duels.spectate.SpectateManagerImpl;
import com.meteordevelopments.duels.util.Loadable;
import com.meteordevelopments.duels.util.Log;
import com.meteordevelopments.duels.util.NumberUtil;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.gui.MultiPageGui;
import com.meteordevelopments.duels.util.inventory.InventoryUtil;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import com.meteordevelopments.duels.util.io.FileUtil;
import com.meteordevelopments.duels.util.json.JsonUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.arim.morepaperlib.scheduling.ScheduledTask;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class QueueManager implements Loadable, DQueueManager, Listener {

    private static final String FILE_NAME = "queues.json";

    private static final String QUEUES_LOADED = "&2Loaded %s queue(s).";

    private final DuelsPlugin plugin;
    private final Config config;
    private final Lang lang;
    private final UserManagerImpl userManager;
    private final KitManagerImpl kitManager;
    private final ArenaManagerImpl arenaManager;
    private final SpectateManagerImpl spectateManager;
    private final DuelManager duelManager;
    private final File file;

    private final List<Queue> queues = new ArrayList<>();

    private CombatTagPlusHook combatTagPlus;
    private PvPManagerHook pvpManager;
    private DeluxeCombatHook deluxeCombat;
    private WorldGuardHook worldGuard;
    private CombatLogXHook combatLogX;
    private VaultHook vault;
    private ScheduledTask queueTask;

    @Getter
    private MultiPageGui<DuelsPlugin> gui;

    public QueueManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.userManager = plugin.getUserManager();
        this.kitManager = plugin.getKitManager();
        this.arenaManager = plugin.getArenaManager();
        this.spectateManager = plugin.getSpectateManager();
        this.duelManager = plugin.getDuelManager();
        this.file = new File(plugin.getDataFolder(), FILE_NAME);

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private boolean canFight(final Kit kit, final UserData first, final UserData second) {
        if (!config.isRatingEnabled()) {
            return true;
        }

        if (first != null && second != null) {
            final int firstRating = first.getRatingUnsafe(kit);
            final int secondRating = second.getRatingUnsafe(kit);
            final int kFactor = config.getKFactor();
            final int maxDifference = config.getMaxDifference();
            return firstRating - secondRating <= maxDifference && secondRating - firstRating <= maxDifference && NumberUtil.getChange(kFactor, firstRating, secondRating) != 0 && NumberUtil.getChange(kFactor, secondRating, firstRating) != 0;
        }

        return false;
    }

    @Override
    public void handleLoad() throws IOException {
        this.gui = new MultiPageGui<>(plugin, lang.getMessage("GUI.queues.title"), config.getQueuesRows(), queues);
        gui.setSpaceFiller(Items.from(config.getQueuesFillerType(), config.getQueuesFillerData()));
        gui.setPrevButton(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.queues.buttons.previous-page.name"), lang).build());
        gui.setNextButton(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.queues.buttons.next-page.name"), lang).build());
        gui.setEmptyIndicator(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.queues.buttons.empty.name"), lang).build());
        plugin.getGuiListener().addGui(gui);

        if (FileUtil.checkNonEmpty(file, true)) {
            try (final Reader reader = new InputStreamReader(Files.newInputStream(file.toPath()), Charsets.UTF_8)) {
                final List<QueueData> data = JsonUtil.getObjectMapper().readValue(reader, new TypeReference<>() {
                });

                if (data != null) {
                    data.forEach(queueData -> {
                        final Queue queue = queueData.toQueue(plugin);

                        if (queue != null && !queues.contains(queue)) {
                            queues.add(queue);
                        }
                    });
                }
            }
        }

        DuelsPlugin.sendMessage(String.format(QUEUES_LOADED, queues.size()));
        gui.calculatePages();

        this.combatTagPlus = plugin.getHookManager().getHook(CombatTagPlusHook.class);
        this.pvpManager = plugin.getHookManager().getHook(PvPManagerHook.class);
        this.deluxeCombat = plugin.getHookManager().getHook(DeluxeCombatHook.class);
        this.combatLogX = plugin.getHookManager().getHook(CombatLogXHook.class);
        this.worldGuard = plugin.getHookManager().getHook(WorldGuardHook.class);
        this.vault = plugin.getHookManager().getHook(VaultHook.class);
        this.queueTask = plugin.doSyncRepeat(() -> {
            boolean update = false;

            for (final Queue queue : queues) {
                final Set<QueueEntry> remove = new HashSet<>();

                // Group-based matching for x vs x
                final int size = Math.max(1, queue.getTeamSize());
                final List<QueueEntry> entries = new ArrayList<>(queue.getPlayers());

                // Attempt to find two disjoint groups of 'size' each that can fight
                // Simple greedy approach: iterate windows
                for (int i = 0; i <= entries.size() - size; i++) {
                    if (update) { break; }
                    final List<QueueEntry> firstGroup = new ArrayList<>();
                    boolean skipI = false;
                    for (int k = 0; k < size; k++) {
                        final QueueEntry e = entries.get(i + k);
                        if (remove.contains(e)) { skipI = true; break; }
                        firstGroup.add(e);
                    }
                    if (skipI) { continue; }

                    for (int j = i + size; j <= entries.size() - size; j++) {
                        final List<QueueEntry> secondGroup = new ArrayList<>();
                        boolean ok = true;
                        for (int k = 0; k < size; k++) {
                            final QueueEntry e = entries.get(j + k);
                            if (remove.contains(e)) { ok = false; break; }
                            secondGroup.add(e);
                        }
                        if (!ok) { continue; }

                        // Rating compatibility check: all-vs-all pairs should be allowed
                        boolean compatible = true;
                        for (final QueueEntry a : firstGroup) {
                            for (final QueueEntry b : secondGroup) {
                                if (!canFight(queue.getKit(), userManager.get(a.getPlayer()), userManager.get(b.getPlayer()))) {
                                    compatible = false;
                                    break;
                                }
                            }
                            if (!compatible) break;
                        }
                        if (!compatible) { continue; }

                        // Build Settings and start match using party or individuals
                        final Settings setting = new Settings(plugin);
                        if (queue.getKit() != null) {
                            setting.setKit(kitManager.get(queue.getKit().getName()));
                        } else {
                            setting.setOwnInventory(true);
                        }
                        setting.setBet(queue.getBet());

                        final List<Player> firstPlayers = new ArrayList<>();
                        final List<Player> secondPlayers = new ArrayList<>();
                        for (final QueueEntry e : firstGroup) {
                            final Player p = e.getPlayer();
                            firstPlayers.add(p);
                            setting.getCache().put(p.getUniqueId(), e.getInfo());
                        }
                        for (final QueueEntry e : secondGroup) {
                            final Player p = e.getPlayer();
                            secondPlayers.add(p);
                            setting.getCache().put(p.getUniqueId(), e.getInfo());
                        }

                        // Clear parties to avoid mismatch unless both sides are proper parties
                        setting.setSenderParty(null);
                        setting.setTargetParty(null);

                        final String kit = queue.getKit() != null ? queue.getKit().getName() : lang.getMessage("GENERAL.none");
                        firstPlayers.forEach(p -> lang.sendMessage(p, "QUEUE.found-opponent", "name", secondPlayers.get(0).getName(), "kit", kit, "bet_amount", queue.getBet()));
                        secondPlayers.forEach(p -> lang.sendMessage(p, "QUEUE.found-opponent", "name", firstPlayers.get(0).getName(), "kit", kit, "bet_amount", queue.getBet()));

                        duelManager.startMatch(firstPlayers, secondPlayers, setting, null, queue);

                        remove.addAll(firstGroup);
                        remove.addAll(secondGroup);
                        update = true;
                        break;
                    }
                }

                if (queue.removeAll(remove) && !update) {
                    update = true;
                }
            }

            if (update) {
                gui.calculatePages();
            }
        }, 20L, 40L);
    }

    @Override
    public void handleUnload() {
        plugin.cancelTask(queueTask);

        if (gui != null) {
            plugin.getGuiListener().removeGui(gui);
        }

        queues.clear();
    }

    private void saveQueues() {
        final List<QueueData> data = new ArrayList<>();

        for (final Queue queue : queues) {
            data.add(new QueueData(queue));
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
    public Queue get(@Nullable final Kit kit, final int bet) {
        return queues.stream().filter(queue -> Objects.equals(kit, queue.getKit()) && queue.getBet() == bet).findFirst().orElse(null);
    }

    @Nullable
    @Override
    public Queue get(@NotNull final Player player) {
        Objects.requireNonNull(player, "player");
        return queues.stream().filter(queue -> queue.isInQueue(player)).findFirst().orElse(null);
    }

    @Nullable
    public Queue randomQueue() {
        return !queues.isEmpty() ? queues.get(ThreadLocalRandom.current().nextInt(queues.size())) : null;
    }

    @Nullable
    public Queue getByName(final String name) {
        return queues.stream().filter(queue -> queue.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public List<String> getQueueNames() {
        return queues.stream().map(Queue::getName).collect(Collectors.toList());
    }

    @Nullable
    @Override
    public Queue create(@Nullable final CommandSender source, @Nullable final Kit kit, final int bet) {
        return create(source, "Unnamed", kit, bet, 1);
    }

    public Queue create(@Nullable final CommandSender source, @Nullable final Kit kit, final int bet, final int teamSize) {
        return create(source, "Unnamed", kit, bet, teamSize);
    }

    public Queue create(@Nullable final CommandSender source, final String name, @Nullable final Kit kit, final int bet, final int teamSize) {
        final Queue queue = new Queue(plugin, name, kit, bet, Math.max(1, teamSize));

        if (queues.contains(queue)) {
            return null;
        }

        queues.add(queue);
        saveQueues();

        final QueueCreateEvent event = new QueueCreateEvent(source, queue);
        Bukkit.getPluginManager().callEvent(event);
        gui.calculatePages();
        return queue;
    }

    @Nullable
    @Override
    public Queue create(@Nullable final Kit kit, final int bet) {
        return create(null, "Unnamed", kit, bet, 1);
    }

    @Nullable
    @Override
    public Queue remove(@Nullable final CommandSender source, @Nullable final Kit kit, final int bet) {
        return remove(source, get(kit, bet));
    }

    @Nullable
    @Override
    public Queue remove(@Nullable final Kit kit, final int bet) {
        return remove(null, kit, bet);
    }

    @NotNull
    @Override
    public List<DQueue> getQueues() {
        return Collections.unmodifiableList(queues);
    }

    @Override
    public boolean isInQueue(@NotNull final Player player) {
        Objects.requireNonNull(player, "player");
        return queues.stream().anyMatch(queue -> queue.isInQueue(player));
    }

    @Override
    public boolean addToQueue(@NotNull final Player player, @NotNull final DQueue queue) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(queue, "queue");
        return queue(player, (Queue) queue);
    }

    @Nullable
    @Override
    public DQueue removeFromQueue(@NotNull final Player player) {
        Objects.requireNonNull(player, "player");
        return remove(player);
    }

    public Queue remove(final CommandSender source, final Queue queue) {
        if (queue == null || !queues.remove(queue)) {
            return null;
        }

        saveQueues();
        queue.getPlayers().forEach(entry -> lang.sendMessage(entry.getPlayer(), "QUEUE.remove"));
        queue.getPlayers().clear();
        queue.setRemoved(true);

        final QueueRemoveEvent event = new QueueRemoveEvent(source, queue);
        Bukkit.getPluginManager().callEvent(event);
        gui.calculatePages();
        return queue;
    }

    public boolean queue(final Player player, final Queue queue) {
        final Queue found = get(player);

        if (found != null) {
            if (found.equals(queue)) {
                queue.removePlayer(player);
                lang.sendMessage(player, "QUEUE.remove");
                return false;
            }

            lang.sendMessage(player, "ERROR.queue.already-in");
            return false;
        }

        if (spectateManager.isSpectating(player)) {
            lang.sendMessage(player, "ERROR.duel.already-spectating.sender");
            return false;
        }

        if (arenaManager.isInMatch(player)) {
            lang.sendMessage(player, "ERROR.duel.already-in-match.sender");
            return false;
        }

        if (config.isRequiresClearedInventory() && InventoryUtil.hasItem(player)) {
            lang.sendMessage(player, "ERROR.duel.inventory-not-empty");
            return false;
        }

        if (config.isPreventCreativeMode() && player.getGameMode() == GameMode.CREATIVE) {
            lang.sendMessage(player, "ERROR.duel.in-creative-mode");
            return false;
        }

        if ((combatTagPlus != null && combatTagPlus.isTagged(player))
                || (pvpManager != null && pvpManager.isTagged(player))
                || (deluxeCombat != null && deluxeCombat.isTagged(player))
                || (combatLogX != null && combatLogX.isTagged(player))) {
            lang.sendMessage(player, "ERROR.duel.is-tagged");
            return false;
        }

        String duelzone = null;

        if (worldGuard != null && config.isDuelzoneEnabled() && (duelzone = worldGuard.findDuelZone(player)) == null) {
            lang.sendMessage(player, "ERROR.duel.not-in-duelzone", "regions", config.getDuelzones());
            return false;
        }

        if (queue.getBet() > 0 && vault != null && !vault.has(queue.getBet(), player)) {
            lang.sendMessage(player, "ERROR.queue.not-enough-money", "bet_amount", queue.getBet());
            return false;
        }

        final QueueJoinEvent event = new QueueJoinEvent(player, queue);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        queue.addPlayer(new QueueEntry(player, player.getLocation().clone(), duelzone));

        // Abort any kit editing session
        com.meteordevelopments.duels.kit.edit.KitEditManager kitEditManager = com.meteordevelopments.duels.kit.edit.KitEditManager.getInstance();
        if (kitEditManager != null) {
            kitEditManager.checkAndAbortIfInQueueOrMatch(player);
        }

        final String kit = queue.getKit() != null ? queue.getKit().getName() : lang.getMessage("GENERAL.none");
        lang.sendMessage(player, "QUEUE.add", "name", queue.getName(), "kit", kit, "bet_amount", queue.getBet());
        return true;
    }

    public Queue remove(final Player player) {
        for (final Queue queue : queues) {
            if (queue.removePlayer(player)) {
                final QueueLeaveEvent event = new QueueLeaveEvent(player, queue);
                Bukkit.getPluginManager().callEvent(event);
                lang.sendMessage(player, "QUEUE.remove");
                return queue;
            }
        }

        return null;
    }

    @EventHandler
    public void on(final PlayerQuitEvent event) {
        remove(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void on(final PlayerCommandPreprocessEvent event) {
        final String command = event.getMessage().substring(1).split(" ")[0].toLowerCase();

        if (!isInQueue(event.getPlayer()) || !config.getQueueBlacklistedCommands().contains(command)) {
            return;
        }

        event.setCancelled(true);
        lang.sendMessage(event.getPlayer(), "QUEUE.prevent.command", "command", event.getMessage());
    }
}
