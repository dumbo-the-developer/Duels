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
        renderSlot(0, draft.getArmor().get(0), 0, true, false, Material.CHAINMAIL_HELMET, "&7(Click to set Helmet)");
        renderSlot(9, draft.getArmor().get(1), 1, true, false, Material.CHAINMAIL_CHESTPLATE, "&7(Click to set Chestplate)");
        renderSlot(18, draft.getArmor().get(2), 2, true, false, Material.CHAINMAIL_LEGGINGS, "&7(Click to set Leggings)");
        renderSlot(27, draft.getArmor().get(3), 3, true, false, Material.CHAINMAIL_BOOTS, "&7(Click to set Boots)");

        // Offhand slot (slot 36)
        renderSlot(36, draft.getOffHand(), 0, false, true, Material.SHIELD, "&7(Click to set Offhand)");

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
                    renderSlot(guiSlot, item, itemSlot, false, false, Material.LIGHT_GRAY_STAINED_GLASS_PANE, "&8(Empty Slot " + itemSlot + ")");
                    mainIdx++;
                }
            }
        }

        // Hotbar 9 slots (slots 0 to 8) -> GUI slots: (row 5: 45 to 53)
        for (int h = 0; h < 9; h++) {
            final int itemSlot = h;
            final int guiSlot = 45 + h;
            final ItemStack item = draft.getItems().get(itemSlot);
            renderSlot(guiSlot, item, itemSlot, false, false, Material.LIGHT_GRAY_STAINED_GLASS_PANE, "&8(Empty Hotbar " + (h + 1) + ")");
        }

        // Control Bar:
        // Slot 38: Cancel / Discard
        set(38, new BaseButton(plugin, ItemBuilder.of(Material.RED_CONCRETE)
                .name("&c&lCancel / Discard", plugin.getLang())
                .lore(plugin.getLang(), "&7Click to discard unsaved changes.")
                .build()) {
            @Override
            public void onClick(final Player player) {
                CustomKitConfirmGui.open(
                        plugin,
                        player,
                        "Discard Changes?",
                        "Are you sure you want to discard unsaved changes?",
                        () -> {
                            plugin.getCustomKitManager().discardSession(player);
                            CustomKitMenuGui.open(plugin, player);
                        },
                        () -> CustomKitEditorGui.open(plugin, player, session)
                );
            }
        });

        // Slot 39: Clear All Items
        set(39, new BaseButton(plugin, ItemBuilder.of(Material.LAVA_BUCKET)
                .name("&c&lClear All Items", plugin.getLang())
                .lore(plugin.getLang(), "&7Click to empty all kit inventory slots.")
                .build()) {
            @Override
            public void onClick(final Player player) {
                CustomKitConfirmGui.open(
                        plugin,
                        player,
                        "Clear All Items?",
                        "Are you sure you want to clear all items in this kit?",
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
        set(40, new BaseButton(plugin, ItemBuilder.of(draft.getIcon() != null ? draft.getIcon().clone() : new ItemStack(Material.NAME_TAG))
                .name("&e&lKit Settings", plugin.getLang())
                .lore(plugin.getLang(),
                        "&7Name: &f" + draft.getName(),
                        "&7Description: &f" + (draft.getDescription().isEmpty() ? "None" : String.join(", ", draft.getDescription())),
                        "",
                        "&a[Left-Click] &7Rename Kit",
                        "&e[Right-Click] &7Edit Description",
                        "&b[Shift-Click] &7Change Icon"
                ).build()) {
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
        set(41, new BaseButton(plugin, ItemBuilder.of(Material.ENDER_EYE)
                .name("&b&lPreview Kit", plugin.getLang())
                .lore(plugin.getLang(), "&7Click to view read-only kit preview.")
                .build()) {
            @Override
            public void onClick(final Player player) {
                CustomKitPreviewGui.open(plugin, player, draft, () -> CustomKitEditorGui.open(plugin, player, session));
            }
        });

        // Slot 44: Save Kit
        set(44, new BaseButton(plugin, ItemBuilder.of(Material.EMERALD_BLOCK)
                .name("&a&lSave Kit", plugin.getLang())
                .lore(plugin.getLang(), "&7Click to validate and save this custom kit.")
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
            displayStack = ItemBuilder.of(placeholderMat)
                    .name(placeholderName, plugin.getLang())
                    .lore(plugin.getLang(), "&aClick to choose item")
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
