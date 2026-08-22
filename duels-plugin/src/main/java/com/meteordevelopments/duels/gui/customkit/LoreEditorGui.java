package com.meteordevelopments.duels.gui.customkit;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.core.customkit.session.CustomKitEditSession;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.util.StringUtil;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.gui.SinglePageGui;
import com.meteordevelopments.duels.util.input.ChatInputManager;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class LoreEditorGui extends SinglePageGui<DuelsPlugin> {

    private final CustomKitEditSession session;
    private final int targetSlot;
    private final boolean isArmor;
    private final boolean isOffHand;

    public LoreEditorGui(final DuelsPlugin plugin,
                         final CustomKitEditSession session,
                         final int targetSlot,
                         final boolean isArmor,
                         final boolean isOffHand) {
        super(plugin, plugin.getLang().getMessage("GUI.lore-editor.title"), 6);
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
            ItemEditorGui.open(plugin, (Player) inventory.getViewers().iterator().next(), session, targetSlot, isArmor, isOffHand);
            return;
        }

        final ItemMeta meta = item.getItemMeta();

        // Top Info Item (slot 4)
        set(4, new BaseButton(plugin, item.clone()) {
            @Override
            public void onClick(final Player player) {
            }
        });

        // Set Display Name Button (slot 1)
        final String currentDisplayName = (meta != null && meta.hasDisplayName()) ? meta.getDisplayName() : "&7(Default)";
        set(1, new BaseButton(plugin, ItemBuilder.of(Material.NAME_TAG)
                .name("&e&lSet Display Name", plugin.getLang())
                .lore(plugin.getLang(),
                        "&7Current: " + currentDisplayName,
                        "",
                        "&aClick to change display name."
                ).build()) {
            @Override
            public void onClick(final Player player) {
                promptDisplayName(player);
            }
        });

        // Reset Display Name Button (slot 2)
        if (meta != null && meta.hasDisplayName()) {
            set(2, new BaseButton(plugin, ItemBuilder.of(Material.REDSTONE)
                    .name("&c&lReset Display Name", plugin.getLang())
                    .lore(plugin.getLang(), "&7Click to restore default item name.")
                    .build()) {
                @Override
                public void onClick(final Player player) {
                    meta.setDisplayName(null);
                    item.setItemMeta(meta);
                    session.touch();
                    render();
                }
            });
        }

        // Add Lore Line Button (slot 7)
        set(7, new BaseButton(plugin, ItemBuilder.of(Material.WRITABLE_BOOK)
                .name("&a&l+ Add Lore Line", plugin.getLang())
                .lore(plugin.getLang(), "&7Click to append a new lore line.")
                .build()) {
            @Override
            public void onClick(final Player player) {
                promptAddLoreLine(player);
            }
        });

        // Clear All Lore (slot 8)
        if (meta != null && meta.hasLore()) {
            set(8, new BaseButton(plugin, ItemBuilder.of(Material.LAVA_BUCKET)
                    .name("&c&lClear All Lore", plugin.getLang())
                    .lore(plugin.getLang(), "&7Click to remove all lore lines.")
                    .build()) {
                @Override
                public void onClick(final Player player) {
                    meta.setLore(null);
                    item.setItemMeta(meta);
                    session.touch();
                    render();
                }
            });
        }

        // Render Lore lines (slots 10-43)
        final List<String> lore = (meta != null && meta.hasLore()) ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        int slot = 10;
        for (int i = 0; i < lore.size(); i++) {
            if (slot > 43) break;
            if (slot % 9 == 0) slot++;
            if (slot % 9 == 8) slot += 2;

            final int lineIndex = i;
            final String lineText = lore.get(i);

            final BaseButton lineBtn = new BaseButton(plugin, ItemBuilder.of(Material.PAPER)
                    .name("&eLine " + (lineIndex + 1) + ": " + lineText, plugin.getLang())
                    .lore(plugin.getLang(),
                            "&a[Left-Click] &7Edit line",
                            "&e[Shift-Left] &7Move UP",
                            "&b[Shift-Right] &7Move DOWN",
                            "&c[Right-Click] &7Delete line"
                    ).build()) {
                @Override
                public void onClick(final Player player, final org.bukkit.event.inventory.InventoryClickEvent event) {
                    if (event.isShiftClick() && event.isLeftClick()) {
                        // Move Up
                        if (lineIndex > 0) {
                            Collections.swap(lore, lineIndex, lineIndex - 1);
                            meta.setLore(lore);
                            item.setItemMeta(meta);
                            session.touch();
                            render();
                        }
                    } else if (event.isShiftClick() && event.isRightClick()) {
                        // Move Down
                        if (lineIndex < lore.size() - 1) {
                            Collections.swap(lore, lineIndex, lineIndex + 1);
                            meta.setLore(lore);
                            item.setItemMeta(meta);
                            session.touch();
                            render();
                        }
                    } else if (event.isRightClick()) {
                        // Delete line
                        lore.remove(lineIndex);
                        meta.setLore(lore.isEmpty() ? null : lore);
                        item.setItemMeta(meta);
                        session.touch();
                        render();
                    } else {
                        // Left-Click: Edit line
                        promptEditLoreLine(player, lineIndex);
                    }
                }
            };

            set(slot, lineBtn);
            slot++;
        }

        // Bottom navigation bar
        final ItemStack filler = Items.GRAY_PANE.clone();
        for (int s = 45; s < 54; s++) {
            inventory.setItem(s, filler);
        }

        // Back Button (slot 49)
        set(49, new BaseButton(plugin, ItemBuilder.of(Material.BARRIER)
                .name("&c&lBack to Item Editor", plugin.getLang())
                .lore(plugin.getLang(), "&7Click to go back.")
                .build()) {
            @Override
            public void onClick(final Player player) {
                ItemEditorGui.open(plugin, player, session, targetSlot, isArmor, isOffHand);
            }
        });
    }

    private void promptDisplayName(final Player player) {
        final ChatInputManager inputManager = new ChatInputManager(plugin);
        inputManager.prompt(
                player,
                plugin.getLang().getMessage("GUI.lore-editor.enter-display-name"),
                input -> {
                    final ItemStack item = getItem();
                    if (item != null) {
                        final ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName(plugin.getLang().toLegacyString(input));
                            item.setItemMeta(meta);
                            session.touch();
                        }
                    }
                    LoreEditorGui.open(plugin, player, session, targetSlot, isArmor, isOffHand);
                },
                () -> LoreEditorGui.open(plugin, player, session, targetSlot, isArmor, isOffHand)
        );
    }

    private void promptAddLoreLine(final Player player) {
        final ChatInputManager inputManager = new ChatInputManager(plugin);
        inputManager.prompt(
                player,
                plugin.getLang().getMessage("GUI.lore-editor.enter-lore-line"),
                input -> {
                    final ItemStack item = getItem();
                    if (item != null) {
                        final ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            final List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                            lore.add(plugin.getLang().toLegacyString(input));
                            meta.setLore(lore);
                            item.setItemMeta(meta);
                            session.touch();
                        }
                    }
                    LoreEditorGui.open(plugin, player, session, targetSlot, isArmor, isOffHand);
                },
                () -> LoreEditorGui.open(plugin, player, session, targetSlot, isArmor, isOffHand)
        );
    }

    private void promptEditLoreLine(final Player player, final int lineIndex) {
        final ChatInputManager inputManager = new ChatInputManager(plugin);
        inputManager.prompt(
                player,
                plugin.getLang().getMessage("GUI.lore-editor.enter-new-lore-line"),
                input -> {
                    final ItemStack item = getItem();
                    if (item != null) {
                        final ItemMeta meta = item.getItemMeta();
                        if (meta != null && meta.hasLore()) {
                            final List<String> lore = new ArrayList<>(meta.getLore());
                            if (lineIndex < lore.size()) {
                                lore.set(lineIndex, plugin.getLang().toLegacyString(input));
                                meta.setLore(lore);
                                item.setItemMeta(meta);
                                session.touch();
                            }
                        }
                    }
                    LoreEditorGui.open(plugin, player, session, targetSlot, isArmor, isOffHand);
                },
                () -> LoreEditorGui.open(plugin, player, session, targetSlot, isArmor, isOffHand)
        );
    }

    public static void open(@NotNull final DuelsPlugin plugin,
                            @NotNull final Player player,
                            @NotNull final CustomKitEditSession session,
                            final int targetSlot,
                            final boolean isArmor,
                            final boolean isOffHand) {
        final LoreEditorGui gui = plugin.getGuiListener().addGui(player, new LoreEditorGui(
                plugin, session, targetSlot, isArmor, isOffHand
        ), true);
        gui.open(player);
    }
}
