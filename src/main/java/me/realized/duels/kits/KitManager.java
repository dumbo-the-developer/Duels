package me.realized.duels.kits;

import com.google.gson.reflect.TypeToken;
import me.realized.duels.Core;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.configuration.Config;
import me.realized.duels.data.KitData;
import me.realized.duels.dueling.RequestManager;
import me.realized.duels.dueling.Settings;
import me.realized.duels.event.RequestSendEvent;
import me.realized.duels.gui.GUI;
import me.realized.duels.utilities.Helper;
import me.realized.duels.utilities.Metadata;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KitManager implements Listener {

    private final Core instance;
    private final Config config;
    private final ArenaManager arenaManager;
    private final RequestManager requestManager;
    private final File base;

    private Map<String, Kit> kits = new HashMap<>();
    private GUI<Kit> gui;

    public KitManager(Core instance) {
        this.instance = instance;
        this.config = instance.getConfiguration();
        this.arenaManager = instance.getArenaManager();
        this.requestManager = instance.getRequestManager();
        this.base = new File(instance.getDataFolder(), "kits.json");

        try {
            boolean generated = base.createNewFile();

            if (generated) {
                instance.info("Generated kits file.");
            }

        } catch (IOException e) {
            instance.warn("Failed to generate kits file! (" + e.getMessage() + ")");
        }

        Bukkit.getPluginManager().registerEvents(this, instance);
    }

    public void load() {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(base))) {
            Map<String, KitData> loaded = instance.getGson().fromJson(reader, new TypeToken<Map<String, KitData>>() {}.getType());

            if (loaded != null) {
                for (Map.Entry<String, KitData> entry : loaded.entrySet()) {
                    kits.put(entry.getKey(), entry.getValue().toKit());
                }
            }
        } catch (IOException e) {
            instance.warn("Failed to load kits! (" + e.getMessage() + ")");
        }

        instance.info("Loaded " + kits.size() + " kit(s).");
        gui = new GUI<>("Kit Selection", new ArrayList<>(kits.values()), config.kitSelectorRows(), new GUI.ClickListener() {

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

                if (arenaManager.isInMatch(target)) {
                    player.closeInventory();
                    Helper.pm("&cThat player is already in a match.", player);
                    return;
                }

                Kit kit = getKit(player, event.getClickedInventory(), event.getSlot());

                if (kit == null) {
                    return;
                }

                settings.setKit(kit.getName());

                if (!config.isAllowArenaSelecting()) {
                    requestManager.sendRequestTo(player, target, settings);
                    player.closeInventory();
                    Helper.pm(config.getString("on-request-send").replace("{PLAYER}", target.getName()).replace("{KIT}", kit.getName()).replace("{ARENA}", "random"), player);
                    Helper.pm(config.getString("on-request-receive").replace("{PLAYER}", player.getName()).replace("{KIT}", kit.getName()).replace("{ARENA}", "random"), target);

                    RequestSendEvent requestSendEvent = new RequestSendEvent(requestManager.getRequestTo(player, target), player, target);
                    Bukkit.getPluginManager().callEvent(requestSendEvent);
                } else {
                    onSwitch(player, arenaManager.getGUI().getFirst());
                }
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

    public void save() {
        gui.close("[Duels] All GUIs are automatically closed on plugin disable.");
        Map<String, KitData> saved = new HashMap<>();

        for (Map.Entry<String, Kit> entry : kits.entrySet()) {
            saved.put(entry.getKey(), new KitData(entry.getValue()));
        }

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(base))) {
            writer.write(instance.getGson().toJson(saved));
            writer.flush();
        } catch (IOException e) {
            instance.warn("Failed to save kits! (" + e.getMessage() + ")");
        }
    }

    public void addKit(String name, Kit kit) {
        kits.put(name, kit);
        gui.update(new ArrayList<>(kits.values()));
    }

    public void removeKit(String name) {
        kits.remove(name);
        gui.update(new ArrayList<>(kits.values()));
    }

    public Kit getKit(Player player, Inventory inventory, int slot) {
        return gui.getData(player, inventory, slot);
    }

    public Kit getKit(String name) {
        return kits.get(name);
    }

    public GUI<Kit> getGUI() {
        return gui;
    }

    public List<Kit> getKits() {
        return new ArrayList<>(kits.values());
    }

    public List<String> getKitNames() {
        List<String> result = new ArrayList<>();

        if (kits.isEmpty()) {
            result.add("No kits are currently loaded.");
            return result;
        }

        result.addAll(kits.keySet());
        return result;
    }

    public enum Type {

        INVENTORY, ARMOR
    }
}
