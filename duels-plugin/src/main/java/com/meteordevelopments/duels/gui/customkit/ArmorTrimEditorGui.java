package com.meteordevelopments.duels.gui.customkit;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.core.customkit.session.CustomKitEditSession;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.gui.SinglePageGui;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ArmorTrimEditorGui extends SinglePageGui<DuelsPlugin> {

    private final CustomKitEditSession session;
    private final int targetSlot;
    private final boolean isArmor;
    private final boolean isOffHand;

    private boolean selectingMaterial = false;
    private TrimPattern selectedPattern = null;

    public ArmorTrimEditorGui(final DuelsPlugin plugin,
                              final CustomKitEditSession session,
                              final int targetSlot,
                              final boolean isArmor,
                              final boolean isOffHand) {
        super(plugin, plugin.getLang().getMessage("GUI.armor-trim-editor.title"), 6);
        this.session = session;
        this.targetSlot = targetSlot;
        this.isArmor = isArmor;
        this.isOffHand = isOffHand;

        render();
    }

    private ItemStack getItem() {
        if (isArmor) {
            return session.getDraftKit().getArmor().get(targetSlot);
        } else if (isOffHand) {
            return session.getDraftKit().getOffHand();
        } else {
            return session.getDraftKit().getItems().get(targetSlot);
        }
    }

    private void render() {
        inventory.clear();
        final ItemStack item = getItem();

        if (item == null || !(item.getItemMeta() instanceof ArmorMeta)) {
            ItemEditorGui.open(plugin, (Player) inventory.getViewers().iterator().next(), session, targetSlot, isArmor, isOffHand);
            return;
        }

        final ArmorMeta meta = (ArmorMeta) item.getItemMeta();

        // Top Info Item (slot 4)
        set(4, new BaseButton(plugin, item.clone()) {
            @Override
            public void onClick(final Player player) {
            }
        });

        // Clear Trim Button (slot 8)
        if (meta.hasTrim()) {
            set(8, new BaseButton(plugin, ItemBuilder.of(Material.LAVA_BUCKET)
                    .name("&c&lRemove Armor Trim", plugin.getLang())
                    .lore(plugin.getLang(), "&7Click to clear the armor trim.")
                    .build()) {
                @Override
                public void onClick(final Player player) {
                    meta.setTrim(null);
                    item.setItemMeta(meta);
                    session.touch();
                    render();
                }
            });
        }

        if (selectingMaterial) {
            renderMaterialSelection();
        } else {
            renderPatternSelection();
        }

        // Bottom navigation bar
        final ItemStack filler = Items.GRAY_PANE.clone();
        for (int s = 45; s < 54; s++) {
            inventory.setItem(s, filler);
        }

        // Back Button (slot 49)
        set(49, new BaseButton(plugin, ItemBuilder.of(Material.BARRIER)
                .name(selectingMaterial ? "&c&lBack to Patterns" : "&c&lBack to Item Editor", plugin.getLang())
                .lore(plugin.getLang(), "&7Click to go back.")
                .build()) {
            @Override
            public void onClick(final Player player) {
                if (selectingMaterial) {
                    selectingMaterial = false;
                    render();
                } else {
                    ItemEditorGui.open(plugin, player, session, targetSlot, isArmor, isOffHand);
                }
            }
        });
    }

    private void renderPatternSelection() {
        final TrimPattern[] patterns = {
                TrimPattern.SENTRY, TrimPattern.DUNE, TrimPattern.COAST, TrimPattern.WILD,
                TrimPattern.WARD, TrimPattern.EYE, TrimPattern.VEX, TrimPattern.TIDE,
                TrimPattern.RIB, TrimPattern.SPIRE, TrimPattern.WAYFINDER,
                TrimPattern.RAISER, TrimPattern.SHAPER, TrimPattern.HOST, TrimPattern.SILENCE
        };

        int slot = 10;
        for (final TrimPattern pat : patterns) {
            if (slot > 43) break;
            if (slot % 9 == 0) slot++;
            if (slot % 9 == 8) slot += 2;

            final String patName = formatKey(pat.getKey().getKey());
            final BaseButton btn = new BaseButton(plugin, ItemBuilder.of(Material.ARMOR_STAND)
                    .name("&b" + patName + " Pattern", plugin.getLang())
                    .lore(plugin.getLang(), "&aClick to select pattern")
                    .build()) {
                @Override
                public void onClick(final Player player) {
                    selectedPattern = pat;
                    selectingMaterial = true;
                    render();
                }
            };

            set(slot, btn);
            slot++;
        }
    }

    private void renderMaterialSelection() {
        final TrimMaterial[] materials = {
                TrimMaterial.QUARTZ, TrimMaterial.IRON, TrimMaterial.NETHERITE, TrimMaterial.REDSTONE,
                TrimMaterial.COPPER, TrimMaterial.GOLD, TrimMaterial.EMERALD, TrimMaterial.DIAMOND,
                TrimMaterial.LAPIS, TrimMaterial.AMETHYST
        };

        final Material[] icons = {
                Material.QUARTZ, Material.IRON_INGOT, Material.NETHERITE_INGOT, Material.REDSTONE,
                Material.COPPER_INGOT, Material.GOLD_INGOT, Material.EMERALD, Material.DIAMOND,
                Material.LAPIS_LAZULI, Material.AMETHYST_SHARD
        };

        int slot = 10;
        for (int i = 0; i < materials.length; i++) {
            if (slot > 43) break;
            if (slot % 9 == 0) slot++;
            if (slot % 9 == 8) slot += 2;

            final TrimMaterial trimMat = materials[i];
            final Material icon = icons[i];
            final String matName = formatKey(trimMat.getKey().getKey());

            final BaseButton btn = new BaseButton(plugin, ItemBuilder.of(icon)
                    .name("&e" + matName + " Trim", plugin.getLang())
                    .lore(plugin.getLang(), "&aClick to apply trim")
                    .build()) {
                @Override
                public void onClick(final Player player) {
                    final ItemStack item = getItem();
                    if (item != null && item.getItemMeta() instanceof ArmorMeta meta) {
                        meta.setTrim(new ArmorTrim(trimMat, selectedPattern));
                        item.setItemMeta(meta);
                        session.touch();
                        selectingMaterial = false;
                        render();
                    }
                }
            };

            set(slot, btn);
            slot++;
        }
    }

    private String formatKey(final String key) {
        final StringBuilder sb = new StringBuilder();
        for (final String s : key.toLowerCase().split("_")) {
            if (s.isEmpty()) continue;
            sb.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    public static void open(@NotNull final DuelsPlugin plugin,
                            @NotNull final Player player,
                            @NotNull final CustomKitEditSession session,
                            final int targetSlot,
                            final boolean isArmor,
                            final boolean isOffHand) {
        final ArmorTrimEditorGui gui = plugin.getGuiListener().addGui(player, new ArmorTrimEditorGui(
                plugin, session, targetSlot, isArmor, isOffHand
        ), true);
        gui.open(player);
    }
}
