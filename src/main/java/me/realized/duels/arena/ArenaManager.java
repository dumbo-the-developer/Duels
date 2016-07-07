package me.realized.duels.arena;

import com.google.gson.reflect.TypeToken;
import me.realized.duels.Core;
import me.realized.duels.configuration.Config;
import me.realized.duels.data.ArenaData;
import me.realized.duels.data.DataManager;
import me.realized.duels.dueling.RequestManager;
import me.realized.duels.dueling.Settings;
import me.realized.duels.event.RequestSendEvent;
import me.realized.duels.gui.GUI;
import me.realized.duels.utilities.Helper;
import me.realized.duels.utilities.Metadata;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class ArenaManager implements Listener {

    private final Core instance;
    private final Config config;
    private final RequestManager requestManager;
    private final DataManager dataManager;
    private final File base;
    private final Random random = new Random();

    private List<Arena> arenas = new ArrayList<>();
    private GUI<Arena> gui;

    public ArenaManager(Core instance) {
        this.instance = instance;
        this.config = instance.getConfiguration();
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

        if (config.isAllowArenaSelecting()) {
            gui = new GUI<>("Arena Selection", arenas, config.arenaSelectorRows(), new GUI.ClickListener() {
                @Override
                public void onClick(InventoryClickEvent event) {
                    Player player = (Player) event.getWhoClicked();
                    ItemStack item = event.getCurrentItem();

                    if (item == null || item.getType() == Material.AIR) {
                        return;
                    }

                    Object value = player.getMetadata("request").get(0).value();

                    if (value == null || !(value instanceof Settings)) {
                        player.closeInventory();
                        return;
                    }

                    Settings settings = (Settings) value;
                    Player target = Bukkit.getPlayer(settings.getTarget());

                    if (target == null) {
                        player.closeInventory();
                        Helper.pm("&cThat player is no longer online.", player);
                        return;
                    }

                    if (isInMatch(target)) {
                        player.closeInventory();
                        Helper.pm("&cThat player is already in a match.", player);
                        return;
                    }

                    Arena arena = getArena(player, event.getClickedInventory(), event.getSlot());

                    if (arena == null || arena.isUsed()) {
                        return;
                    }

                    settings.setArena(arena.getName());
                    requestManager.sendRequestTo(player, target, settings);
                    player.closeInventory();
                    Helper.pm(config.getString("on-request-send").replace("{PLAYER}", target.getName()).replace("{KIT}", settings.getKit()).replace("{ARENA}", settings.getArena()), player);
                    Helper.pm(config.getString("on-request-receive").replace("{PLAYER}", player.getName()).replace("{KIT}", settings.getKit()).replace("{ARENA}", settings.getArena()), target);

                    RequestSendEvent requestSendEvent = new RequestSendEvent(requestManager.getRequestTo(player, target), player, target);
                    Bukkit.getPluginManager().callEvent(requestSendEvent);
                }

                @Override
                public void onClose(InventoryCloseEvent event) {
                    Player player = (Player) event.getPlayer();

                    if (gui.isPage(event.getInventory()) && player.hasMetadata("request")) {
                        player.removeMetadata("request", instance);
                    }
                }

                @Override
                public void onSwitch(Player player, Inventory opened) {
                    if (player.hasMetadata("request")) {
                        Object value = player.getMetadata("request").get(0).value();
                        player.openInventory(opened);

                        if (value == null || !(value instanceof Settings)) {
                            return;
                        }

                        player.setMetadata("request", new Metadata(instance, value));
                    }
                }
            });

            instance.getGUIManager().register(gui);
        }

        instance.info("Loaded " + arenas.size() + " arena(s).");
    }

    public void save() {
        if (gui != null) {
            gui.close("[Duels] All GUIs are automatically closed on plugin disable.");
        }

        List<ArenaData> saved = new ArrayList<>();
        Location toTeleport = dataManager.getLobby() != null ? dataManager.getLobby() : Bukkit.getWorlds().get(0).getSpawnLocation();

        if (!arenas.isEmpty()) {
            for (Arena arena : arenas) {
                saved.add(new ArenaData(arena));

                if (arena.isUsed()) {
                    Arena.Match match = arena.getCurrentMatch();

                    for (UUID uuid : arena.getPlayers()) {
                        Player player = Bukkit.getPlayer(uuid);

                        if (player == null || player.isDead()) {
                            continue;
                        }

                        if (config.isTeleportToLastLoc()) {
                            toTeleport = match.getLocation(uuid);
                        }

                        Helper.pm("&c&l[Duels] Plugin is disabling, matches are ended by default.", player);
                        Helper.reset(player, false);

                        Arena.InventoryData data = match.getInventories(uuid);

                        if (!Helper.canTeleportTo(player, toTeleport)) {
                            player.setHealth(0.0D);
                        } else {
                            player.teleport(toTeleport);
                            Helper.setInventory(player, data.getInventoryContents(), data.getArmorContents(), false);
                        }
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

    public Arena getArena(Player player, Inventory inventory, int slot) {
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
        for (Arena arena : arenas) {
            if (arena.getPlayers().contains(player.getUniqueId())) {
                return true;
            }
        }

        return false;
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
        if (event.isCancelled() || !(event.getEntity().getShooter() instanceof Player) || !config.isCdProjectileBlocked()) {
            return;
        }

        Player player = (Player) event.getEntity().getShooter();

        if (!isInMatch(player)) {
            return;
        }

        Arena arena = getArena(player);

        if (arena.isCounting()) {
            Helper.pm("&cYou are not allowed to do this before the match starts.", player);
            event.setCancelled(true);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void on(EntityDamageByEntityEvent event) {
        if (event.isCancelled() || !(event.getEntity() instanceof Player && event.getDamager() instanceof Player) || !config.isCdPvPBlocked()) {
            return;
        }

        Player player = (Player) event.getDamager();

        if (!isInMatch(player)) {
            return;
        }

        Arena arena = getArena(player);

        if (arena.isCounting()) {
            Helper.pm("&cYou are not allowed to do this before the match starts.", player);
            event.setCancelled(true);
        }
    }
}
