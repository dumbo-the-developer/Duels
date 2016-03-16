package me.realized.duels.kits;

import com.google.gson.reflect.TypeToken;
import me.realized.duels.Core;
import me.realized.duels.event.KitCreateEvent;
import me.realized.duels.event.KitRemoveEvent;
import me.realized.duels.utilities.inventory.ItemBuilder;
import me.realized.duels.utilities.inventory.ItemFromConfig;
import me.realized.duels.utilities.inventory.ItemToConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.*;
import java.util.*;

public class KitManager implements Listener {

    private final Core instance;
    private final File base;

    private Map<String, KitContents> kits = new HashMap<>();
    private Inventory gui;

    public KitManager(Core instance) {
        this.instance = instance;
        this.base = new File(instance.getDataFolder(), "kits.json");

        try {
            boolean generated = base.createNewFile();

            if (generated) {
                instance.info("Generated arena file.");
            }

        } catch (IOException e) {
            instance.warn("Failed to generate kits file! (" + e.getMessage() + ")");
        }

        Bukkit.getPluginManager().registerEvents(this, instance);
    }

    public void load() {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(base))) {
            Map<String, Map<Type, Map<Integer, String>>> loaded = instance.getGson().fromJson(reader, new TypeToken<Map<String, Map<Type, Map<Integer, String>>>>() {}.getType());

            if (loaded != null) {
                ItemFromConfig from = new ItemFromConfig();

                for (String key : loaded.keySet()) {
                    Map<Type, Map<Integer, String>> value = loaded.get(key);
                    Map<Integer, ItemStack> inventory = new HashMap<>();
                    Map<Integer, ItemStack> armor = new HashMap<>();
                    Map<Integer, String> contents = value.get(Type.INVENTORY);

                    for (Map.Entry<Integer, String> entry : contents.entrySet()) {
                        from.setLine(entry.getValue());
                        inventory.put(entry.getKey(), from.buildWithMeta());
                    }

                    contents = value.get(Type.ARMOR);

                    for (Map.Entry<Integer, String> entry : contents.entrySet()) {
                        from.setLine(entry.getValue());
                        armor.put(entry.getKey(), from.buildWithMeta());
                    }

                    kits.put(key, new KitContents(inventory, armor));
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

        ItemToConfig to = new ItemToConfig();
        Map<String, Map<Type, Map<Integer, String>>> saved = new HashMap<>();

        for (String key : kits.keySet()) {
            KitContents kit = kits.get(key);
            Map<Type, Map<Integer, String>> contents = new HashMap<>();
            Map<Integer, String> armor = new HashMap<>();
            Map<Integer, String> inventory = new HashMap<>();

            for (Map.Entry<Integer, ItemStack> entry : kit.getArmor().entrySet()) {
                to.setItem(entry.getValue());
                armor.put(entry.getKey(), to.toString());
            }

            for (Map.Entry<Integer, ItemStack> entry : kit.getInventory().entrySet()) {
                to.setItem(entry.getValue());
                inventory.put(entry.getKey(), to.toString());
            }

            contents.put(Type.ARMOR, armor);
            contents.put(Type.INVENTORY, inventory);
            saved.put(key, contents);
        }

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(base))) {
            writer.write(instance.getGson().toJson(saved));
            writer.flush();
        } catch (IOException e) {
            instance.warn("Failed to save kits! (" + e.getMessage() + ")");
        }
    }

    public void addKit(String name, PlayerInventory inventory) {
        kits.put(name, new KitContents(inventory));
    }

    public void removeKit(String name) {
        kits.remove(name);
    }

    public KitContents getKit(String name) {
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

        List<String> kits = new ArrayList<>();

        for (String name : this.kits.keySet()) {
            kits.add(name);
        }

        for (int i = 0; i < gui.getSize(); i++) {
            if (kits.size() - 1 < i) {
                break;
            }

            gui.setItem(i, ItemBuilder.builder().type(Material.DIAMOND_SWORD).name("&7&l" + kits.get(i)).lore(Arrays.asList("&aClick to send", "&aa duel request", "&awith this kit!")).build());
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

        for (String name : kits.keySet()) {
            result.add(name);
        }

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

    private enum Type {

        INVENTORY, ARMOR
    }
}
