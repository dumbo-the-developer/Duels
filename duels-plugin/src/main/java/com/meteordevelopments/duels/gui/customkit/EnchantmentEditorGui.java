package com.meteordevelopments.duels.gui.customkit;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.core.customkit.config.CustomKitsConfig;
import com.meteordevelopments.duels.core.customkit.session.CustomKitEditSession;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.gui.SinglePageGui;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class EnchantmentEditorGui extends SinglePageGui<DuelsPlugin> {

    private final CustomKitEditSession session;
    private final int targetSlot;
    private final boolean isArmor;
    private final boolean isOffHand;
    private boolean browsingAll = false;
    private int page = 0;

    public EnchantmentEditorGui(final DuelsPlugin plugin,
                                final CustomKitEditSession session,
                                final int targetSlot,
                                final boolean isArmor,
                                final boolean isOffHand) {
        super(plugin, plugin.getLang().getMessage("GUI.enchantment-editor.title"), 6);
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

        if (item == null || item.getType() == Material.AIR) {
            if (!inventory.getViewers().isEmpty()) {
                ItemEditorGui.open(plugin, (Player) inventory.getViewers().iterator().next(), session, targetSlot, isArmor, isOffHand);
            }
            return;
        }

        final CustomKitsConfig config = plugin.getCustomKitManager().getCustomKitsConfig();

        // Top Info Item (slot 4)
        set(4, new BaseButton(plugin, item.clone()) {
            @Override
            public void onClick(final Player player) {
            }
        });

        // Initialize Bottom navigation bar with fillers (slots 45-53) FIRST
        final ItemStack filler = Items.GRAY_PANE.clone();
        for (int s = 45; s < 54; s++) {
            inventory.setItem(s, filler);
        }

        // Back Button (slot 49)
        final String backName = browsingAll
                ? plugin.getLang().getMessageOrDefault("GUI.enchantment-editor.buttons.back-enchantments.name", "&c&lBack to Enchantments")
                : plugin.getLang().getMessageOrDefault("GUI.enchantment-editor.buttons.back-item-editor.name", "&c&lBack to Item Editor");
        final List<String> backLore = browsingAll
                ? plugin.getLang().getMessageListOrDefault("GUI.enchantment-editor.buttons.back-enchantments.lore", List.of("&7Click to go back."))
                : plugin.getLang().getMessageListOrDefault("GUI.enchantment-editor.buttons.back-item-editor.lore", List.of("&7Click to go back."));
        set(49, new BaseButton(plugin, ItemBuilder.of(Material.BARRIER)
                .name(backName, plugin.getLang())
                .lore(backLore, plugin.getLang())
                .build()) {
            @Override
            public void onClick(final Player player) {
                if (browsingAll) {
                    browsingAll = false;
                    page = 0;
                    render();
                } else {
                    ItemEditorGui.open(plugin, player, session, targetSlot, isArmor, isOffHand);
                }
            }
        });

        if (browsingAll) {
            renderAddEnchantmentList(config);
        } else {
            renderCurrentEnchantments(item, config);
        }
    }

    private void renderCurrentEnchantments(final ItemStack item, final CustomKitsConfig config) {
        final Map<Enchantment, Integer> enchants = item.getEnchantments();

        // Add Enchantment Button at slot 0
        set(0, new BaseButton(plugin, ItemBuilder.of(Material.ENCHANTED_BOOK)
                .name(plugin.getLang().getMessageOrDefault("GUI.enchantment-editor.buttons.add.name", "&a&l+ Add Enchantment"), plugin.getLang())
                .lore(plugin.getLang().getMessageListOrDefault("GUI.enchantment-editor.buttons.add.lore",
                        List.of("&7Click to browse and add an enchantment.")), plugin.getLang())
                .build()) {
            @Override
            public void onClick(final Player player) {
                browsingAll = true;
                page = 0;
                render();
            }
        });

        // Clear All Enchantments at slot 8
        if (!enchants.isEmpty()) {
            set(8, new BaseButton(plugin, ItemBuilder.of(Material.LAVA_BUCKET)
                    .name(plugin.getLang().getMessageOrDefault("GUI.enchantment-editor.buttons.clear.name", "&c&lClear All Enchantments"), plugin.getLang())
                    .lore(plugin.getLang().getMessageListOrDefault("GUI.enchantment-editor.buttons.clear.lore",
                            List.of("&7Click to remove all enchantments.")), plugin.getLang())
                    .build()) {
                @Override
                public void onClick(final Player player) {
                    final List<Enchantment> toRemove = new ArrayList<>(item.getEnchantments().keySet());
                    toRemove.forEach(item::removeEnchantment);
                    session.touch();
                    render();
                }
            });
        }

        int slot = 10;
        for (final Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            if (slot > 43) break;
            if (slot % 9 == 0) slot++;
            if (slot % 9 == 8) slot += 2;

            final Enchantment ench = entry.getKey();
            final int level = entry.getValue();
            final String enchName = formatEnchantName(ench.getKey().getKey());
            final int maxAllowed = config.getEnchantOverrides().getOrDefault(ench.getKey().getKey().toUpperCase(), config.getDefaultMaxEnchantLevel());

            final List<String> enchLore = new ArrayList<>();
            for (final String line : plugin.getLang().getMessageListOrDefault("GUI.enchantment-editor.buttons.enchant-entry.lore",
                    List.of(
                            "&7Current Level: &e%level% &7/ &a%max%",
                            "",
                            "&a[Left-Click] &7+1 Level",
                            "&e[Right-Click] &7-1 Level",
                            "&c[Shift-Click / Middle] &7Remove Enchantment"
                    ))) {
                enchLore.add(line
                        .replace("%level%", String.valueOf(level))
                        .replace("%max%", String.valueOf(maxAllowed)));
            }

            final BaseButton enchBtn = new BaseButton(plugin, ItemBuilder.of(Material.ENCHANTED_BOOK)
                    .name(plugin.getLang().getMessageOrDefault("GUI.enchantment-editor.buttons.enchant-entry.name", "&b%enchantment% %level%")
                            .replace("%enchantment%", enchName)
                            .replace("%level%", toRoman(level)), plugin.getLang())
                    .lore(enchLore, plugin.getLang())
                    .build()) {
                @Override
                public void onClick(final Player player, final InventoryClickEvent event) {
                    if (event.isShiftClick() || event.getClick() == ClickType.MIDDLE) {
                        item.removeEnchantment(ench);
                    } else if (event.isRightClick()) {
                        if (level > 1) {
                            item.addUnsafeEnchantment(ench, level - 1);
                        } else {
                            item.removeEnchantment(ench);
                        }
                    } else {
                        // Left-Click: increment up to maxAllowed
                        if (level < maxAllowed) {
                            item.addUnsafeEnchantment(ench, level + 1);
                        }
                    }
                    session.touch();
                    render();
                }
            };

            set(slot, enchBtn);
            slot++;
        }
    }

    private void renderAddEnchantmentList(final CustomKitsConfig config) {
        final ItemStack item = getItem();
        final List<Enchantment> all = new ArrayList<>(Arrays.asList(Enchantment.values()));
        all.removeIf(e -> config.getBlockedEnchantments().contains(e.getKey().getKey().toUpperCase()));

        // Filter incompatible enchantments unless allow-incompatible is true
        if (!config.isAllowIncompatibleEnchants() && item != null && item.getType() != Material.AIR) {
            all.removeIf(e -> !e.canEnchantItem(item));
        }

        final int pageSize = 28;
        final int totalPages = Math.max(1, (int) Math.ceil((double) all.size() / pageSize));
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;

        final int startIndex = page * pageSize;
        final int endIndex = Math.min(startIndex + pageSize, all.size());

        int slot = 10;
        for (int i = startIndex; i < endIndex; i++) {
            if (slot > 43) break;
            if (slot % 9 == 0) slot++;
            if (slot % 9 == 8) slot += 2;

            final Enchantment ench = all.get(i);
            final String enchName = formatEnchantName(ench.getKey().getKey());
            final int maxAllowed = config.getEnchantOverrides().getOrDefault(ench.getKey().getKey().toUpperCase(), config.getDefaultMaxEnchantLevel());

            final List<String> browseLore = new ArrayList<>();
            for (final String line : plugin.getLang().getMessageListOrDefault("GUI.enchantment-editor.buttons.browse-entry.lore",
                    List.of(
                            "&7Max Allowed Level: &a%max%",
                            "",
                            "&aClick to add level 1"
                    ))) {
                browseLore.add(line.replace("%max%", String.valueOf(maxAllowed)));
            }

            final BaseButton addBtn = new BaseButton(plugin, ItemBuilder.of(Material.BOOK)
                    .name(plugin.getLang().getMessageOrDefault("GUI.enchantment-editor.buttons.browse-entry.name", "&b%enchantment%")
                            .replace("%enchantment%", enchName), plugin.getLang())
                    .lore(browseLore, plugin.getLang())
                    .build()) {
                @Override
                public void onClick(final Player player) {
                    final ItemStack targetItem = getItem();
                    if (targetItem != null) {
                        targetItem.addUnsafeEnchantment(ench, 1);
                        session.touch();
                        browsingAll = false;
                        page = 0;
                        render();
                    }
                }
            };

            set(slot, addBtn);
            slot++;
        }

        if (page > 0) {
            final String prevName = plugin.getLang().getMessageOrDefault("GUI.enchantment-editor.buttons.previous-page.name", "&ePrevious Page (%page%/%total%)")
                    .replace("%page%", String.valueOf(page))
                    .replace("%total%", String.valueOf(totalPages));
            set(45, new BaseButton(plugin, ItemBuilder.of(Material.ARROW)
                    .name(prevName, plugin.getLang()).build()) {
                @Override
                public void onClick(final Player player) {
                    if (page > 0) {
                        page--;
                        render();
                    }
                }
            });
        }

        if (page < totalPages - 1) {
            final String nextName = plugin.getLang().getMessageOrDefault("GUI.enchantment-editor.buttons.next-page.name", "&eNext Page (%page%/%total%)")
                    .replace("%page%", String.valueOf(page + 2))
                    .replace("%total%", String.valueOf(totalPages));
            set(53, new BaseButton(plugin, ItemBuilder.of(Material.ARROW)
                    .name(nextName, plugin.getLang()).build()) {
                @Override
                public void onClick(final Player player) {
                    if (page < totalPages - 1) {
                        page++;
                        render();
                    }
                }
            });
        }
    }

    private String formatEnchantName(final String key) {
        final StringBuilder sb = new StringBuilder();
        for (final String s : key.toLowerCase().split("_")) {
            if (s.isEmpty()) continue;
            sb.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    private String toRoman(final int level) {
        return switch (level) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> String.valueOf(level);
        };
    }

    public static void open(@NotNull final DuelsPlugin plugin,
                            @NotNull final Player player,
                            @NotNull final CustomKitEditSession session,
                            final int targetSlot,
                            final boolean isArmor,
                            final boolean isOffHand) {
        final EnchantmentEditorGui gui = plugin.getGuiListener().addGui(player, new EnchantmentEditorGui(
                plugin, session, targetSlot, isArmor, isOffHand
        ), true);
        gui.open(player);
    }
}
