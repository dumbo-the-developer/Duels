package com.meteordevelopments.duels.gui.customkit;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.api.customkit.CustomKit;
import com.meteordevelopments.duels.api.customkit.CustomKitSnapshot;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.gui.SinglePageGui;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import com.meteordevelopments.duels.util.inventory.Slots;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomKitPreviewGui extends SinglePageGui<DuelsPlugin> {

    public CustomKitPreviewGui(final DuelsPlugin plugin,
                              final String kitName,
                              final String ownerName,
                              final List<String> description,
                              final ItemStack icon,
                              final Map<Integer, ItemStack> items,
                              final Map<Integer, ItemStack> armor,
                              final ItemStack offHand,
                              final Runnable onBack) {
        super(plugin, plugin.getLang().getMessage("GUI.customkit-preview.title", "kit", kitName), 6);

        final ItemStack spacer = Items.GRAY_PANE.clone();

        // Spacer column 1
        int[] spacerSlots = {1, 10, 19, 28, 37, 38, 39, 41, 42, 43};
        for (final int s : spacerSlots) {
            inventory.setItem(s, spacer);
        }

        // Armor slots
        final ItemStack helmet = armor != null ? armor.get(0) : null;
        final ItemStack chestplate = armor != null ? armor.get(1) : null;
        final ItemStack leggings = armor != null ? armor.get(2) : null;
        final ItemStack boots = armor != null ? armor.get(3) : null;

        setReadOnlySlot(0, helmet, Material.CHAINMAIL_HELMET, "&7(Empty Helmet)");
        setReadOnlySlot(9, chestplate, Material.CHAINMAIL_CHESTPLATE, "&7(Empty Chestplate)");
        setReadOnlySlot(18, leggings, Material.CHAINMAIL_LEGGINGS, "&7(Empty Leggings)");
        setReadOnlySlot(27, boots, Material.CHAINMAIL_BOOTS, "&7(Empty Boots)");

        // Offhand slot
        setReadOnlySlot(36, offHand, Material.SHIELD, "&7(Empty Offhand)");

        // Main inventory slots (27 slots: 2-8, 11-17, 20-26, 29-35)
        int mainIdx = 9;
        for (int r = 0; r < 4; r++) {
            for (int c = 2; c <= 8; c++) {
                if (r == 3 && c >= 6) {
                    continue; // Leave room if needed
                }
                if (mainIdx < 36) {
                    final int itemSlot = mainIdx;
                    final ItemStack item = items != null ? items.get(itemSlot) : null;
                    final int guiSlot = r * 9 + c;
                    setReadOnlySlot(guiSlot, item, Material.LIGHT_GRAY_STAINED_GLASS_PANE, "&8(Empty Slot)");
                    mainIdx++;
                }
            }
        }

        // Hotbar (9 slots: 45-53)
        for (int h = 0; h < 9; h++) {
            final ItemStack item = items != null ? items.get(h) : null;
            final int guiSlot = 45 + h;
            setReadOnlySlot(guiSlot, item, Material.LIGHT_GRAY_STAINED_GLASS_PANE, "&8(Empty Hotbar)");
        }

        // Info Button at slot 40
        final List<String> infoLore = new ArrayList<>();
        infoLore.add("&7Owner: &f" + (ownerName != null ? ownerName : "Unknown"));
        if (description != null && !description.isEmpty()) {
            infoLore.add("&7Description:");
            for (final String line : description) {
                infoLore.add("&f" + line);
            }
        }
        infoLore.add("&8Read-only preview");

        final BaseButton infoBtn = new BaseButton(plugin, ItemBuilder.of(icon != null ? icon.clone() : new ItemStack(Material.NETHERITE_SWORD))
                .name("&e&lKit: " + kitName, plugin.getLang())
                .lore(infoLore, plugin.getLang())
                .build()) {
            @Override
            public void onClick(final Player player) {
            }
        };
        set(40, infoBtn);

        // Back / Close Button at slot 44
        final BaseButton backBtn = new BaseButton(plugin, ItemBuilder.of(Material.BARRIER)
                .name("&c&lClose Preview", plugin.getLang())
                .lore(plugin.getLang(), "&7Click to close preview.")
                .build()) {
            @Override
            public void onClick(final Player player) {
                plugin.getGuiListener().removeGui(player, CustomKitPreviewGui.this);
                player.closeInventory();
                if (onBack != null) {
                    onBack.run();
                }
            }
        };
        set(44, backBtn);
    }

    private void setReadOnlySlot(final int slot, final ItemStack item, final Material placeholderMat, final String placeholderName) {
        final ItemStack display = item != null && item.getType() != Material.AIR
                ? item.clone()
                : ItemBuilder.of(placeholderMat).name(placeholderName, plugin.getLang()).build();

        set(slot, new BaseButton(plugin, display) {
            @Override
            public void onClick(final Player player) {
                // Read-only, no action
            }
        });
    }

    public static void open(@NotNull final DuelsPlugin plugin,
                            @NotNull final Player player,
                            @NotNull final CustomKit kit,
                            @Nullable final Runnable onBack) {
        final String ownerName = Bukkit.getOfflinePlayer(kit.getOwner()).getName();
        final CustomKitPreviewGui gui = plugin.getGuiListener().addGui(player, new CustomKitPreviewGui(
                plugin,
                kit.getName(),
                ownerName != null ? ownerName : "Unknown",
                kit.getDescription(),
                kit.getIcon(),
                kit.getItems(),
                kit.getArmor(),
                kit.getOffHand(),
                onBack
        ), true);
        gui.open(player);
    }

    public static void open(@NotNull final DuelsPlugin plugin,
                            @NotNull final Player player,
                            @NotNull final CustomKitSnapshot snapshot,
                            @Nullable final Runnable onBack) {
        final String ownerName = Bukkit.getOfflinePlayer(snapshot.getOwner()).getName();
        final CustomKitPreviewGui gui = plugin.getGuiListener().addGui(player, new CustomKitPreviewGui(
                plugin,
                snapshot.getName(),
                ownerName != null ? ownerName : "Unknown",
                snapshot.getDescription(),
                snapshot.getIcon(),
                snapshot.getItems(),
                snapshot.getArmor(),
                snapshot.getOffHand(),
                onBack
        ), true);
        gui.open(player);
    }
}
