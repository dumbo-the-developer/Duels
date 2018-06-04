/*
 * This file is part of Duels, licensed under the MIT License.
 *
 * Copyright (c) Realized
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.realized._duels.kits;

import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.realized._duels.Core;
import me.realized._duels.arena.ArenaManager;
import me.realized._duels.configuration.MainConfig;
import me.realized._duels.data.KitData;
import me.realized._duels.dueling.RequestManager;
import me.realized._duels.dueling.Settings;
import me.realized._duels.event.RequestSendEvent;
import me.realized._duels.utilities.Helper;
import me.realized._duels.utilities.Reloadable;
import me.realized._duels.utilities.Storage;
import me.realized._duels.utilities.gui.GUI;
import me.realized._duels.utilities.gui.GUIListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class KitManager implements Listener, Reloadable {

    private final Core instance;
    private final MainConfig config;
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
        kits.clear();

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

        gui = new GUI<>("Kit Selection", getKits(), config.getGuiKitSelectorRows(), new GUIListener() {

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

                if (arenaManager.isInMatch(target)) {
                    player.closeInventory();
                    Helper.pm(player, "Errors.already-in-match.target", true);
                    return;
                }

                Kit kit = getKit(player, event.getClickedInventory(), event.getSlot());

                if (kit == null) {
                    return;
                }

                settings.setKit(kit.getName());

                if (!config.isDuelingAllowArenaSelecting()) {
                    requestManager.sendRequestTo(player, target, settings);
                    player.closeInventory();

                    Helper.pm(player, "Dueling.on-request-send.sender", true, "{PLAYER}", target.getName(), "{KIT}", settings.getKit(), "{ARENA}", "random");
                    Helper.pm(target, "Dueling.on-request-send.receiver", true, "{PLAYER}", player.getName(), "{KIT}", settings.getKit(), "{ARENA}", "random");

                    RequestSendEvent requestSendEvent = new RequestSendEvent(requestManager.getRequestTo(player, target), player, target);
                    Bukkit.getPluginManager().callEvent(requestSendEvent);
                } else {
                    onSwitch(player, arenaManager.getGUI().getFirst());
                }
            }

            @Override
            public void onClose(InventoryCloseEvent event) {
                Player player = (Player) event.getPlayer();

                if (gui.isPage(event.getInventory())) {
                    Storage.get(player).remove("request");
                }
            }

            @Override
            public void onSwitch(Player player, Inventory opened) {
                Object value = Storage.get(player).get("request");

                if (value != null && value instanceof Settings) {
                    player.openInventory(opened);
                    Storage.get(player).set("request", value);
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
        gui.update(getKits());
    }

    public void removeKit(String name) {
        kits.remove(name);
        gui.update(getKits());
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
        List<Kit> kits = new ArrayList<>(this.kits.values());
        Collections.sort(kits, new Comparator<Kit>() {
            @Override
            public int compare(Kit kit1, Kit kit2) {
                return kit1.getName().compareTo(kit2.getName());
            }
        });

        return kits;
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

    @Override
    public void handleReload(ReloadType type) {
        if (type == ReloadType.STRONG) {
            save();
            load();
        }
    }

    public enum Type {

        INVENTORY,
        ARMOR
    }
}
