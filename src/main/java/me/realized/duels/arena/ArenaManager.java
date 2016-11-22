package me.realized.duels.arena;

import com.google.gson.reflect.TypeToken;
import me.realized.duels.Core;
import me.realized.duels.configuration.MainConfig;
import me.realized.duels.data.ArenaData;
import me.realized.duels.data.DataManager;
import me.realized.duels.data.PlayerData;
import me.realized.duels.data.PlayerManager;
import me.realized.duels.dueling.RequestManager;
import me.realized.duels.dueling.Settings;
import me.realized.duels.event.MatchEndEvent;
import me.realized.duels.event.RequestSendEvent;
import me.realized.duels.utilities.Helper;
import me.realized.duels.utilities.Reloadable;
import me.realized.duels.utilities.Storage;
import me.realized.duels.utilities.gui.GUI;
import me.realized.duels.utilities.gui.GUIListener;
import me.realized.duels.utilities.location.Teleport;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

public class ArenaManager implements Listener, Reloadable {

    private final Core instance;
    private final MainConfig config;
    private final Teleport teleport;
    private final PlayerManager playerManager;
    private final RequestManager requestManager;
    private final DataManager dataManager;
    private final File base;
    private final Random random = new Random();

    private List<Arena> arenas = new ArrayList<>();
    private GUI<Arena> gui;

    private BukkitTask task;

    public ArenaManager(Core instance) {
        this.instance = instance;
        this.config = instance.getConfiguration();
        this.teleport = instance.getTeleport();
        this.playerManager = instance.getPlayerManager();
        this.requestManager = instance.getRequestManager();
        this.dataManager = instance.getDataManager();

        Bukkit.getPluginManager().registerEvents(this, instance);

        base = new File(instance.getDataFolder(), "arenas.json");

        try {
            boolean generated = base.createNewFile();

            if (generated) {
                instance.info("Generated arena file.");
            }

        } catch (IOException e) {
            instance.warn("Failed to generate arena file! (" + e.getMessage() + ")");
        }
    }

    public void load() {
        arenas.clear();

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(base))) {
            List<ArenaData> loaded = instance.getGson().fromJson(reader, new TypeToken<List<ArenaData>>() {}.getType());

            if (loaded != null && !loaded.isEmpty()) {
                for (ArenaData data : loaded) {
                    arenas.add(data.toArena());
                }
            }
        } catch (IOException ex) {
            instance.warn("Failed to load arenas from the file! (" + ex.getMessage() + ")");
        }

        if (config.isDuelingAllowArenaSelecting()) {
            gui = new GUI<>("Arena Selection", arenas, config.getGuiArenaSelectorRows(), new GUIListener() {
                @Override
                public void onClick(InventoryClickEvent event) {
                    Player player = (Player) event.getWhoClicked();
                    ItemStack item = event.getCurrentItem();

                    if (item == null || item.getType() == Material.AIR) {
                        return;
                    }

                    Object value = Storage.get(player).get("request");

                    if (value == null || !(value instanceof Settings)) {
                        player.closeInventory();
                        return;
                    }

                    Settings settings = (Settings) value;
                    Player target = Bukkit.getPlayer(settings.getTarget());

                    if (target == null) {
                        player.closeInventory();
                        Helper.pm(player, "Errors.player-left", true);
                        return;
                    }

                    if (isInMatch(target)) {
                        player.closeInventory();
                        Helper.pm(player, "Errors.already-in-match.target", true);
                        return;
                    }

                    Arena arena = getArena(player, event.getClickedInventory(), event.getSlot());

                    if (arena == null || arena.isUsed()) {
                        return;
                    }

                    settings.setArena(arena.getName());
                    requestManager.sendRequestTo(player, target, settings);
                    player.closeInventory();
                    Helper.pm(player, "Dueling.on-request-send.sender", true, "{PLAYER}", target.getName(), "{KIT}", settings.getKit(), "{ARENA}", settings.getArena());
                    Helper.pm(target, "Dueling.on-request-send.receiver", true, "{PLAYER}", player.getName(), "{KIT}", settings.getKit(), "{ARENA}", settings.getArena());

                    RequestSendEvent requestSendEvent = new RequestSendEvent(requestManager.getRequestTo(player, target), player, target);
                    Bukkit.getPluginManager().callEvent(requestSendEvent);
                }

                @Override
                public void onClose(InventoryCloseEvent event) {
                    Player player = (Player) event.getPlayer();

                    if (gui.isPage(event.getInventory())) {
                        Storage.get(player).remove("request");
                    }
                }

                @Override
                public void onSwitch(Player player, Inventory opened) {}
            });

            instance.getGUIManager().register(gui);
        }

        if (config.isDuelingMatchMaxDurationEnabled()) {
            task = Bukkit.getScheduler().runTaskTimer(instance, new Runnable() {

                @Override
                public void run() {
                    for (Arena arena : arenas) {
                        if (arena.isUsed() && arena.getPlayers().size() >= 2) {
                            Arena.Match match = arena.getCurrentMatch();

                            if ((System.currentTimeMillis() - match.getStart()) / 1000L <= config.getDuelingMatchMaxDurationDuration()) {
                                return;
                            }

                            instance.getSpectatorManager().handleMatchEnd(arena);
                            match.setEndReason(MatchEndEvent.EndReason.MAX_TIME_REACHED);

                            Iterator<UUID> iterator = arena.getPlayers().iterator();

                            instance.logToFile(this, "Match between " + arena.getFormattedPlayers() + " reached max time limit, ending automatically.", Level.INFO);

                            while (iterator.hasNext()) {
                                UUID uuid = iterator.next();
                                iterator.remove();

                                Player player = Bukkit.getPlayer(uuid);

                                if (player == null || player.isDead()) {
                                    continue;
                                }

                                PlayerData data = playerManager.getData(player);
                                playerManager.removeData(player);

                                if (!config.isDuelingTeleportToLatestLocation()) {
                                    data.setLocation(dataManager.getLobby());
                                }

                                Helper.pm(player, "Dueling.max-match-time-reached", true);

                                if (!config.isDuelingUseOwnInventory()) {
                                    Helper.reset(player, false);
                                }

                                if (!teleport.isAuthorizedFor(player, data.getLocation())) {
                                    player.setHealth(0.0D);
                                    Helper.pm(player, "&4Teleportation failed! You were killed to prevent staying in the arena.", false);
                                    continue;
                                }

                                teleport.teleportPlayer(player, data.getLocation());
                                data.restore(player, !config.isDuelingRequiresClearedInventory());
                            }

                            arena.setUsed(false);

                            if (gui != null) {
                                gui.update(arenas);
                            }
                        }
                    }
                }
            }, 0L, 20L);
        }

        instance.info("Loaded " + arenas.size() + " arena(s).");
    }

    public void save() {
        if (gui != null) {
            gui.close("[Duels] All GUIs are automatically closed on plugin shutdown.");
        }

        if (task != null) {
            task.cancel();
            task = null;
        }

        List<ArenaData> saved = new ArrayList<>();

        if (!arenas.isEmpty()) {
            for (Arena arena : arenas) {
                saved.add(new ArenaData(arena));

                if (arena.isUsed()) {
                    Arena.Match match = arena.getCurrentMatch();

                    instance.getSpectatorManager().handleMatchEnd(arena);
                    match.setEndReason(MatchEndEvent.EndReason.PLUGIN_DISABLE);

                    instance.logToFile(this, "Plugin is disabling! Match between " + arena.getFormattedPlayers() + " was ended automatically.", Level.INFO);

                    for (UUID uuid : arena.getPlayers()) {
                        Player player = Bukkit.getPlayer(uuid);

                        if (player == null || player.isDead()) {
                            continue;
                        }

                        PlayerData data = playerManager.getData(player);
                        playerManager.removeData(player);

                        if (!config.isDuelingTeleportToLatestLocation()) {
                            data.setLocation(dataManager.getLobby());
                        }

                        Helper.pm(player, "&c&l[Duels] Plugin is disabling, matches are ended by default.", false);

                        if (!config.isDuelingUseOwnInventory()) {
                            Helper.reset(player, false);
                        }

                        if (!teleport.isAuthorizedFor(player, data.getLocation())) {
                            player.setHealth(0.0D);
                            Helper.pm(player, "&4Teleportation failed! You were killed to prevent staying in the arena.", false);
                            continue;
                        }

                        teleport.teleportPlayer(player, data.getLocation());
                        data.restore(player, !config.isDuelingRequiresClearedInventory());
                    }
                }
            }
        }

        try {
            boolean generated = base.createNewFile();

            if (generated) {
                instance.info("Generated arena file!");
            }

            Writer writer = new OutputStreamWriter(new FileOutputStream(base));
            instance.getGson().toJson(saved, writer);
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            instance.warn("Failed to save arenas! (" + ex.getMessage() + ")");
        }
    }

    public Arena getArena(String name) {
        for (Arena arena : arenas) {
            if (arena.getName().equals(name)) {
                return arena;
            }
        }

        return null;
    }

    public Arena getArena(Player player) {
        for (Arena arena : arenas) {
            if (arena.getPlayers().contains(player.getUniqueId())) {
                return arena;
            }
        }

        return null;
    }

    private Arena getArena(Player player, Inventory inventory, int slot) {
        return gui.getData(player, inventory, slot);
    }

    public Arena getAvailableArena() {
        List<Arena> available = new ArrayList<>();

        for (Arena arena : arenas) {
            if (!arena.isDisabled()  && arena.isValid() && !arena.isUsed()) {
                available.add(arena);
            }
        }

        if (available.isEmpty()) {
            return null;
        }

        return available.get(random.nextInt(available.size()));
    }

    public boolean isInMatch(Player player) {
        return getArena(player) != null;
    }

    public void createArena(String name) {
        arenas.add(new Arena(name, false));

        if (gui != null) {
            gui.update(arenas);
        }
    }

    public void removeArena(Arena arena) {
        arenas.remove(arena);

        if (gui != null) {
            gui.update(arenas);
        }
    }

    public List<Arena> getArenas() {
        return arenas;
    }

    public List<String> getArenaNames() {
        List<String> result = new ArrayList<>();

        if (arenas.isEmpty()) {
            result.add("No arenas are currently loaded.");
            return result;
        }

        for (Arena arena : arenas) {
            if (arena.isDisabled()) {
                result.add(ChatColor.DARK_RED + arena.getName());
                continue;
            }

            if (!arena.isValid()) {
                result.add(ChatColor.BLUE + arena.getName());
                continue;
            }

            result.add((arena.isUsed() ? ChatColor.RED : ChatColor.GREEN) + arena.getName());
        }

        return result;
    }

    public GUI<Arena> getGUI() {
        return gui;
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void on(ProjectileLaunchEvent event) {
        if (event.isCancelled() || !(event.getEntity().getShooter() instanceof Player) || !config.isCountdownBlockProjectile()) {
            return;
        }

        Player player = (Player) event.getEntity().getShooter();
        Arena arena = getArena(player);

        if (arena == null) {
            return;
        }

        if (arena.isCounting()) {
            Helper.pm(player, "Errors.blocked-countdown-action", true);
            event.setCancelled(true);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void on(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player && event.getDamager() instanceof Player) || !config.isCountdownBlockPvp()) {
            return;
        }

        Player player = (Player) event.getDamager();
        Arena arena = getArena(player);

        if (arena == null) {
            return;
        }

        Player damaged = (Player) event.getEntity();

        if (arena.isCounting()) {
            Helper.pm(player, "Errors.blocked-countdown-action", true);
            event.setCancelled(true);
        } else if (event.isCancelled() && config.isPatchesForceAllowPvp() && arena.hasPlayer(damaged)) {
            event.setCancelled(false);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void on(PlayerInteractEvent event) {
        if (!config.isSoupEnabled() || !event.getAction().name().contains("RIGHT")) {
            return;
        }

        Player player = event.getPlayer();

        if (player.getHealth() == player.getMaxHealth()) {
            return;
        }

        Arena arena = getArena(player);

        if (arena == null || !arena.getName().startsWith(config.getSoupArenasStartingWith())) {
            return;
        }

        boolean regen = false;

        if (Helper.isPre1_9()) {
            if (player.getItemInHand().getType() == Material.MUSHROOM_SOUP) {
                player.setItemInHand(new ItemStack(Material.BOWL));
                regen = true;
            }
        } else {
            if (player.getInventory().getItemInMainHand().getType() == Material.MUSHROOM_SOUP) {
                player.getInventory().setItemInMainHand(new ItemStack(Material.BOWL));
                regen = true;
            } else if (player.getInventory().getItemInOffHand().getType() == Material.MUSHROOM_SOUP) {
                player.getInventory().setItemInOffHand(new ItemStack(Material.BOWL));
                regen = true;
            }
        }

        if (!regen) {
            return;
        }

        if (player.getHealth() + config.getSoupHeartsToRegen() * 2.0 <= player.getMaxHealth()) {
            player.setHealth(player.getHealth() + config.getSoupHeartsToRegen() * 2.0);
        } else {
            player.setHealth(player.getHealth() + (player.getMaxHealth() - player.getHealth()));
        }
    }

    @Override
    public void handleReload(ReloadType type) {
        if (type == ReloadType.STRONG) {
            save();
            load();
        }
    }
}
