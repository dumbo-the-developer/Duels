package com.meteordevelopments.duels.gui.customkit;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.core.customkit.CustomKitImpl;
import com.meteordevelopments.duels.core.customkit.session.CustomKitEditSession;
import com.meteordevelopments.duels.core.customkit.validation.CustomKitValidator;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.gui.SinglePageGui;
import com.meteordevelopments.duels.util.input.ChatInputManager;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CustomKitEditorGui extends SinglePageGui<DuelsPlugin> {

    private final CustomKitEditSession session;

    public CustomKitEditorGui(final DuelsPlugin plugin, final CustomKitEditSession session) {
        super(plugin, plugin.getLang().getMessage("GUI.customkit-editor.title", "kit", session.getDraftKit().getName()), 6);
        this.session = session;

        render();
    }

    private void render() {
        inventory.clear();
        final CustomKitImpl draft = session.getDraftKit();

        // Spacer column 1 and control dividers
        final ItemStack spacer = Items.GRAY_PANE.clone();
        final int[] spacers = {1, 10, 19, 28, 37, 42, 43};
        for (final int s : spacers) {
            inventory.setItem(s, spacer);
        }

        // Armor slots (0: Helmet, 9: Chestplate, 18: Leggings, 27: Boots)
        final String helmetPh = plugin.getLang().getMessageOrDefault("GUI.customkit-editor.placeholders.helmet", "&7(Click to set Helmet)");
        final String chestPh = plugin.getLang().getMessageOrDefault("GUI.customkit-editor.placeholders.chestplate", "&7(Click to set Chestplate)");
        final String legsPh = plugin.getLang().getMessageOrDefault("GUI.customkit-editor.placeholders.leggings", "&7(Click to set Leggings)");
        final String bootsPh = plugin.getLang().getMessageOrDefault("GUI.customkit-editor.placeholders.boots", "&7(Click to set Boots)");
        final String offhandPh = plugin.getLang().getMessageOrDefault("GUI.customkit-editor.placeholders.offhand", "&7(Click to set Offhand)");

        renderSlot(0, draft.getArmor().get(0), 0, true, false, Material.CHAINMAIL_HELMET, helmetPh);
        renderSlot(9, draft.getArmor().get(1), 1, true, false, Material.CHAINMAIL_CHESTPLATE, chestPh);
        renderSlot(18, draft.getArmor().get(2), 2, true, false, Material.CHAINMAIL_LEGGINGS, legsPh);
        renderSlot(27, draft.getArmor().get(3), 3, true, false, Material.CHAINMAIL_BOOTS, bootsPh);

        // Offhand slot (slot 36)
        renderSlot(36, draft.getOffHand(), 0, false, true, Material.SHIELD, offhandPh);

        // Main Inventory 27 slots (slots 9 to 35) -> GUI slots: (rows 0-3, columns 2-8)
        int mainIdx = 9;
        for (int r = 0; r < 4; r++) {
            for (int c = 2; c <= 8; c++) {
                if (r == 3 && c >= 6) {
                    continue;
                }
                if (mainIdx < 36) {
                    final int itemSlot = mainIdx;
                    final int guiSlot = r * 9 + c;
                    final ItemStack item = draft.getItems().get(itemSlot);
                    final String emptySlotPh = plugin.getLang().getMessageOrDefault(
                            "GUI.customkit-editor.placeholders.empty-slot",
                            "&8(Empty Slot " + itemSlot + ")",
                            "slot", itemSlot);
                    renderSlot(guiSlot, item, itemSlot, false, false, Material.LIGHT_GRAY_STAINED_GLASS_PANE, emptySlotPh);
                    mainIdx++;
                }
            }
        }

        // Hotbar 9 slots (slots 0 to 8) -> GUI slots: (row 5: 45 to 53)
        for (int h = 0; h < 9; h++) {
            final int itemSlot = h;
            final int guiSlot = 45 + h;
            final ItemStack item = draft.getItems().get(itemSlot);
            final String emptyHotbarPh = plugin.getLang().getMessageOrDefault(
                    "GUI.customkit-editor.placeholders.empty-hotbar",
                    "&8(Empty Hotbar " + (h + 1) + ")",
                    "slot", (h + 1));
            renderSlot(guiSlot, item, itemSlot, false, false, Material.LIGHT_GRAY_STAINED_GLASS_PANE, emptyHotbarPh);
        }

        // Control Bar:
        // Slot 38: Cancel / Discard
        final String cancelName = plugin.getLang().getMessageOrDefault("GUI.customkit-editor.buttons.cancel.name", "&c&lCancel / Discard");
        final List<String> cancelLore = plugin.getLang().getMessageListOrDefault("GUI.customkit-editor.buttons.cancel.lore",
                java.util.Collections.singletonList("&7Click to discard unsaved changes."));
        final String discardTitle = plugin.getLang().getMessageOrDefault("GUI.customkit-editor.confirm-discard.title", "Discard Changes?");
        final String discardMsg = plugin.getLang().getMessageOrDefault("GUI.customkit-editor.confirm-discard.message",
                "Are you sure you want to discard unsaved changes?");

        set(38, new BaseButton(plugin, ItemBuilder.of(Material.RED_CONCRETE)
                .name(cancelName, plugin.getLang())
                .lore(cancelLore, plugin.getLang())
                .build()) {
            @Override
            public void onClick(final Player player) {
                CustomKitConfirmGui.open(
                        plugin,
                        player,
                        discardTitle,
                        discardMsg,
                        () -> {
                            plugin.getCustomKitManager().discardSession(player);
                            CustomKitMenuGui.open(plugin, player);
                        },
                        () -> CustomKitEditorGui.open(plugin, player, session)
                );
            }
        });

        // Slot 39: Clear All Items
        final String clearName = plugin.getLang().getMessageOrDefault("GUI.customkit-editor.buttons.clear.name", "&c&lClear All Items");
        final List<String> clearLore = plugin.getLang().getMessageListOrDefault("GUI.customkit-editor.buttons.clear.lore",
                java.util.Collections.singletonList("&7Click to empty all kit inventory slots."));
        final String clearTitle = plugin.getLang().getMessageOrDefault("GUI.customkit-editor.confirm-clear.title", "Clear All Items?");
        final String clearMsg = plugin.getLang().getMessageOrDefault("GUI.customkit-editor.confirm-clear.message",
                "Are you sure you want to clear all items in this kit?");

        set(39, new BaseButton(plugin, ItemBuilder.of(Material.LAVA_BUCKET)
                .name(clearName, plugin.getLang())
                .lore(clearLore, plugin.getLang())
                .build()) {
            @Override
            public void onClick(final Player player) {
                CustomKitConfirmGui.open(
                        plugin,
                        player,
                        clearTitle,
                        clearMsg,
                        () -> {
                            draft.getItems().clear();
                            draft.getArmor().clear();
                            draft.setOffHand(null);
                            session.touch();
                            CustomKitEditorGui.open(plugin, player, session);
                        },
                        () -> CustomKitEditorGui.open(plugin, player, session)
                );
            }
        });

        // Slot 40: Kit Settings (Rename, Change Icon, Description)
        final String noneDesc = plugin.getLang().getMessageOrDefault("GENERAL.none", "None");
        final String settingsName = plugin.getLang().getMessageOrDefault("GUI.customkit-editor.buttons.settings.name", "&e&lKit Settings");
        final List<String> settingsLore = plugin.getLang().getMessageListOrDefault("GUI.customkit-editor.buttons.settings.lore",
                java.util.Arrays.asList(
                        "&7Name: &f%name%",
                        "&7Description: &f%description%",
                        "",
                        "&a[Left-Click] &7Rename Kit",
                        "&e[Right-Click] &7Edit Description",
                        "&b[Shift-Click] &7Change Icon"
                ),
                "name", draft.getName(),
                "description", draft.getDescription().isEmpty() ? noneDesc : String.join(", ", draft.getDescription())
        );

        set(40, new BaseButton(plugin, ItemBuilder.of(draft.getIcon() != null ? draft.getIcon().clone() : new ItemStack(Material.NAME_TAG))
                .name(settingsName, plugin.getLang())
                .lore(settingsLore, plugin.getLang())
                .build()) {
            @Override
            public void onClick(final Player player, final org.bukkit.event.inventory.InventoryClickEvent event) {
                if (event.isShiftClick()) {
                    promptKitIcon(player);
                } else if (event.isRightClick()) {
                    promptKitDescription(player);
                } else {
                    promptKitRename(player);
                }
            }
        });

        // Slot 41: Preview Kit
        final String previewName = plugin.getLang().getMessageOrDefault("GUI.customkit-editor.buttons.preview.name", "&b&lPreview Kit");
        final List<String> previewLore = plugin.getLang().getMessageListOrDefault("GUI.customkit-editor.buttons.preview.lore",
                java.util.Collections.singletonList("&7Click to view read-only kit preview."));

        set(41, new BaseButton(plugin, ItemBuilder.of(Material.ENDER_EYE)
                .name(previewName, plugin.getLang())
                .lore(previewLore, plugin.getLang())
                .build()) {
            @Override
            public void onClick(final Player player) {
                CustomKitPreviewGui.open(plugin, player, draft, () -> CustomKitEditorGui.open(plugin, player, session));
            }
        });

        // Slot 44: Save Kit
        final String saveName = plugin.getLang().getMessageOrDefault("GUI.customkit-editor.buttons.save.name", "&a&lSave Kit");
        final List<String> saveLore = plugin.getLang().getMessageListOrDefault("GUI.customkit-editor.buttons.save.lore",
                java.util.Collections.singletonList("&7Click to validate and save this custom kit."));

        set(44, new BaseButton(plugin, ItemBuilder.of(Material.EMERALD_BLOCK)
                .name(saveName, plugin.getLang())
                .lore(saveLore, plugin.getLang())
                .build()) {
            @Override
            public void onClick(final Player player) {
                final boolean success = plugin.getCustomKitManager().saveSession(player);
                if (success) {
                    plugin.getLang().sendMessage(player, "COMMAND.customkits.saved", "kit", draft.getName());
                    CustomKitMenuGui.open(plugin, player);
                }
            }
        });
    }

    private void renderSlot(final int guiSlot,
                            final ItemStack item,
                            final int targetSlot,
                            final boolean isArmor,
                            final boolean isOffHand,
                            final Material placeholderMat,
                            final String placeholderName) {
        final boolean hasItem = (item != null && item.getType() != Material.AIR);

        final ItemStack displayStack;
        if (hasItem) {
            displayStack = item.clone();
        } else {
            final List<String> chooseLore = plugin.getLang().getMessageListOrDefault(
                    "GUI.customkit-editor.placeholders.choose-item-lore",
                    java.util.Collections.singletonList("&aClick to choose item"));
            displayStack = ItemBuilder.of(placeholderMat)
                    .name(placeholderName, plugin.getLang())
                    .lore(chooseLore, plugin.getLang())
                    .build();
        }

        set(guiSlot, new BaseButton(plugin, displayStack) {
            @Override
            public void onClick(final Player player) {
                session.setActiveSlot(targetSlot);
                session.setArmorSlot(isArmor);
                session.setOffHandSlot(isOffHand);

                if (hasItem) {
                    ItemEditorGui.open(plugin, player, session, targetSlot, isArmor, isOffHand);
                } else {
                    MaterialBrowserGui.open(plugin, player, session, targetSlot, isArmor, isOffHand);
                }
            }
        });
    }

    private void promptKitRename(final Player player) {
        final ChatInputManager inputManager = new ChatInputManager(plugin);
        inputManager.prompt(
                player,
                plugin.getLang().getMessage("COMMAND.customkits.enter-new-name"),
                input -> {
                    final CustomKitValidator.ValidationResult val = plugin.getCustomKitManager().getValidator().validateName(
                            input,
                            player.getUniqueId(),
                            session.getDraftKit().getUniqueId(),
                            plugin.getCustomKitManager().getCustomKitsConfig()
                    );

                    if (!val.isValid()) {
                        plugin.getLang().sendMessage(player, val.getMessageKey(), val.getReplacers());
                    } else {
                        session.getDraftKit().setName(input.trim());
                        session.touch();
                        plugin.getLang().sendMessage(player, "COMMAND.customkits.renamed", "kit", input.trim());
                    }

                    CustomKitEditorGui.open(plugin, player, session);
                },
                () -> CustomKitEditorGui.open(plugin, player, session)
        );
    }

    private void promptKitDescription(final Player player) {
        final ChatInputManager inputManager = new ChatInputManager(plugin);
        inputManager.prompt(
                player,
                plugin.getLang().getMessage("COMMAND.customkits.enter-description"),
                input -> {
                    if (input.trim().equalsIgnoreCase("clear")) {
                        session.getDraftKit().getDescription().clear();
                        plugin.getLang().sendMessage(player, "COMMAND.customkits.description-updated");
                    } else if (input.trim().equalsIgnoreCase("cancel")) {
                        plugin.getLang().sendMessage(player, "GENERAL.cancelled");
                    } else {
                        session.getDraftKit().getDescription().clear();
                        session.getDraftKit().getDescription().add(input.trim());
                        plugin.getLang().sendMessage(player, "COMMAND.customkits.description-updated");
                    }
                    session.touch();
                    CustomKitEditorGui.open(plugin, player, session);
                },
                () -> CustomKitEditorGui.open(plugin, player, session)
        );
    }

    private void promptKitIcon(final Player player) {
        final ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand != null && hand.getType() != Material.AIR) {
            session.getDraftKit().setIcon(new ItemStack(hand.getType(), 1));
            session.touch();
            plugin.getLang().sendMessage(player, "COMMAND.customkits.icon-changed", "material", hand.getType().name());
            CustomKitEditorGui.open(plugin, player, session);
        } else {
            MaterialBrowserGui.openIconPicker(plugin, player, session);
        }
    }

    public static void open(@NotNull final DuelsPlugin plugin,
                            @NotNull final Player player,
                            @NotNull final CustomKitEditSession session) {
        final CustomKitEditorGui gui = plugin.getGuiListener().addGui(player, new CustomKitEditorGui(plugin, session), true);
        gui.open(player);
    }
}
