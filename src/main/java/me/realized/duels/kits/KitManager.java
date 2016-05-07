package me.realized.duels.kits;

import com.google.gson.reflect.TypeToken;
import me.realized.duels.Core;
import me.realized.duels.data.KitData;
import me.realized.duels.event.KitCreateEvent;
import me.realized.duels.event.KitItemChangeEvent;
import me.realized.duels.event.KitRemoveEvent;
import me.realized.duels.utilities.inventory.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KitManager implements Listener {

    private final Core instance;
    private final File base;

    private Map<String, Kit> kits = new HashMap<>();
    private Map<Integer, String> getBySlot = new HashMap<>();
    private Inventory gui;

    public KitManager(Core instance) {
        this.instance = instance;
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

        int max = ((kits.size() / 9) * 9 + (kits.size() % 9 > 0 ? 9 : 0));
        int limited = (max <= 56 ? max : 56) + (max == 0 ? 9 : 0);
        gui = Bukkit.createInventory(null, limited, "Kit Selection");
        refreshGUI(false);
    }

    public void save() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Inventory top = player.getOpenInventory().getTopInventory();

            if (top != null && top.getTitle().equals("Kit Selection")) {
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "[Duels] Closing GUI due to plugin disable.");
            }
        }

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
    }

    public void removeKit(String name) {
        kits.remove(name);
    }

    public String getKit(int slot) {
        return getBySlot.get(slot);
    }

    public Kit getKit(String name) {
        return kits.get(name);
    }

    public Inventory getKitGUI() {
        return gui;
    }

    private void refreshGUI(boolean players) {
        gui.clear();

        if (kits.isEmpty()) {
            gui.setItem(4, ItemBuilder.builder().type(Material.REDSTONE_BLOCK).name("&cNo Kits found!").build());
            return;
        }

        List<Kit> kits = new ArrayList<>();
        kits.addAll(this.kits.values());

        for (int i = 0; i < gui.getSize(); i++) {
            if (kits.size() - 1 < i) {
                break;
            }

            Kit kit = kits.get(i);
            getBySlot.put(i, kit.getName());
            gui.setItem(i, kit.getDisplayed());
        }

        if (players) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Inventory top = player.getOpenInventory().getTopInventory();

                if (top != null && top.getTitle().equals("Kit Selection")) {
                    player.updateInventory();
                }
            }
        }
    }

    public List<String> getKits() {
        List<String> result = new ArrayList<>();

        if (kits.isEmpty()) {
            result.add("No kits are currently loaded.");
            return result;
        }

        result.addAll(kits.keySet());
        return result;
    }

    @EventHandler
    public void onCreate(KitCreateEvent event) {
        refreshGUI(true);
    }

    @EventHandler
    public void onRemove(KitRemoveEvent event) {
        refreshGUI(true);
    }

    @EventHandler
    public void onItemUpdate(KitItemChangeEvent event) {
        refreshGUI(true);
    }

    public enum Type {

        INVENTORY, ARMOR
    }
}
