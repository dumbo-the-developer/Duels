package me.realized.duels.kit;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.cache.Setting;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.util.inventory.ItemBuilder;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Kit extends BaseButton {

    @Getter
    private final String name;
    @Getter
    private final Map<String, Map<Integer, ItemStack>> items = new HashMap<>();

    public Kit(final DuelsPlugin plugin, final String name, final ItemStack displayed) {
        super(plugin, displayed);
        this.name = name;
    }

    public Kit(final DuelsPlugin plugin, final String name, final PlayerInventory inventory) {
        this(plugin, name, ItemBuilder
            .of(Material.DIAMOND_SWORD)
            .name("&7&l" + name)
            .lore("&aClick to send", "&aa duel request", "&awith this kit!")
            .build());

        final Map<Integer, ItemStack> contents = new HashMap<>();

        for (int i = 0; i < inventory.getSize(); i++) {
            final ItemStack item = inventory.getItem(i);

            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            contents.put(i, item.clone());
        }

        items.put("INVENTORY", contents);

        final Map<Integer, ItemStack> armorContents = new HashMap<>();

        for (int i = inventory.getArmorContents().length - 1; i >= 0; i--) {
            final ItemStack item = inventory.getArmorContents()[i];

            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            armorContents.put(4 - i, inventory.getArmorContents()[i].clone());
        }

        items.put("ARMOR", armorContents);
    }

    public void equip(final Player player) {
        for (final Map.Entry<Integer, ItemStack> entry : items.get("INVENTORY").entrySet()) {
            player.getInventory().setItem(entry.getKey(), entry.getValue().clone());
        }

        final ItemStack[] armor = items.get("ARMOR").values().toArray(new ItemStack[4]);
        ArrayUtils.reverse(armor);
        player.getInventory().setArmorContents(armor);
    }

    @Override
    public void onClick(final Player player) {
        final Setting setting = settingCache.get(player);
        setting.setKit(this);
        setting.openGui(player);
        player.sendMessage(ChatColor.GREEN + "Selected Kit: " + name);
    }
}
