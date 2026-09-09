package com.meteordevelopments.duels.gui.customkit;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.api.customkit.CustomKit;
import com.meteordevelopments.duels.core.customkit.CustomKitImpl;
import com.meteordevelopments.duels.core.customkit.session.CustomKitEditSession;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.gui.SinglePageGui;
import com.meteordevelopments.duels.util.input.ChatInputManager;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CustomKitMenuGui extends SinglePageGui<DuelsPlugin> {

    public CustomKitMenuGui(final DuelsPlugin plugin, final Player player) {
        super(plugin, plugin.getLang().getMessage("GUI.customkits-menu.title"), 6);

        final List<CustomKit> playerKits = plugin.getCustomKitManager().getKits(player.getUniqueId());
        final int maxKits = plugin.getCustomKitManager().getMaxKits(player);
        final String unlimitedStr = plugin.getLang().getMessageOrDefault("GENERAL.unlimited", "Unlimited");
        final String limitStr = maxKits == Integer.MAX_VALUE ? unlimitedStr : String.valueOf(maxKits);

        // Display player custom kits (slots 0-44)
        int slot = 0;
        for (final CustomKit kit : playerKits) {
            if (slot >= 45) break;
            final CustomKitImpl impl = (CustomKitImpl) kit;
            final int itemCount = impl.getItems().size() + impl.getArmor().size() + (impl.getOffHand() != null ? 1 : 0);

            final List<String> templateLore = plugin.getLang().getMessageList("GUI.customkits-menu.buttons.kit.lore",
                    "name", impl.getName(),
                    "items", itemCount);

            final List<String> lore = new ArrayList<>();
            if (!templateLore.isEmpty()) {
                for (final String line : templateLore) {
                    if (line.contains("%description%")) {
                        if (!impl.getDescription().isEmpty()) {
                            for (final String desc : impl.getDescription()) {
                                lore.add("&7" + desc);
                            }
                        }
                    } else {
                        lore.add(line);
                    }
                }
            } else {
                if (!impl.getDescription().isEmpty()) {
                    for (final String line : impl.getDescription()) {
                        lore.add("&7" + line);
                    }
                    lore.add("");
                }
                lore.add("&7Items: &f" + itemCount);
                lore.add("");
                lore.add("&a[Left-Click] &7Edit Kit");
                lore.add("&b[Right-Click] &7Preview Kit");
                lore.add("&e[Shift-Left] &7Duplicate Kit");
                lore.add("&c[Shift-Right] &7Delete Kit");
            }

            final ItemStack iconItem = impl.getIcon() != null ? impl.getIcon().clone() : new ItemStack(Material.NETHERITE_SWORD);
            final String displayName = plugin.getLang().getMessageOrDefault("GUI.customkits-menu.buttons.kit.name",
                    "&e&l" + impl.getName(), "name", impl.getName());

            final BaseButton kitBtn = new BaseButton(plugin, ItemBuilder.of(iconItem)
                    .name(displayName, plugin.getLang())
                    .lore(lore, plugin.getLang())
                    .build()) {
                @Override
                public void onClick(final Player player, final InventoryClickEvent event) {
                    if (event.isShiftClick() && event.isRightClick()) {
                        // Delete Kit
                        if (!player.hasPermission(Permissions.CUSTOMKITS_DELETE) && !player.hasPermission(Permissions.CUSTOMKITS_USE)) {
                            plugin.getLang().sendMessage(player, "ERROR.no-permission", "permission", Permissions.CUSTOMKITS_DELETE);
                            return;
                        }
                        final String confirmTitle = plugin.getLang().getMessageOrDefault("GUI.customkits-menu.confirm-delete.title", "Delete Kit?");
                        final String confirmMsg = plugin.getLang().getMessageOrDefault("GUI.customkits-menu.confirm-delete.message",
                                "Are you sure you want to delete kit '" + impl.getName() + "'?", "kit", impl.getName());

                        CustomKitConfirmGui.open(
                                plugin,
                                player,
                                confirmTitle,
                                confirmMsg,
                                () -> {
                                    plugin.getCustomKitManager().deleteKit(player.getUniqueId(), impl.getUniqueId());
                                    plugin.getLang().sendMessage(player, "COMMAND.customkits.deleted", "kit", impl.getName());
                                    CustomKitMenuGui.open(plugin, player);
                                },
                                () -> CustomKitMenuGui.open(plugin, player)
                        );
                    } else if (event.isShiftClick() && event.isLeftClick()) {
                        // Duplicate Kit
                        if (!player.hasPermission(Permissions.CUSTOMKITS_DUPLICATE) && !player.hasPermission(Permissions.CUSTOMKITS_USE)) {
                            plugin.getLang().sendMessage(player, "ERROR.no-permission", "permission", Permissions.CUSTOMKITS_DUPLICATE);
                            return;
                        }
                        promptDuplicateKit(player, impl);
                    } else if (event.isRightClick() || event.getClick() == ClickType.MIDDLE) {
                        // Preview Kit
                        CustomKitPreviewGui.open(plugin, player, impl.toSnapshot(), () -> CustomKitMenuGui.open(plugin, player));
                    } else {
                        // Edit Kit
                        if (!player.hasPermission(Permissions.CUSTOMKITS_EDIT) && !player.hasPermission(Permissions.CUSTOMKITS_USE)) {
                            plugin.getLang().sendMessage(player, "ERROR.no-permission", "permission", Permissions.CUSTOMKITS_EDIT);
                            return;
                        }

                        final CustomKitEditSession session = plugin.getCustomKitManager().startSession(player, impl, false);
                        CustomKitEditorGui.open(plugin, player, session);
                    }
                }
            };

            set(slot, kitBtn);
            slot++;
        }

        // Bottom navigation & control bar (slots 45-53)
        final ItemStack filler = Items.GRAY_PANE.clone();
        for (int s = 45; s < 54; s++) {
            inventory.setItem(s, filler);
        }

        // Slot 45: Limit Info Button
        final Object remainingObj = maxKits == Integer.MAX_VALUE ? unlimitedStr : Math.max(0, maxKits - playerKits.size());
        final String limitName = plugin.getLang().getMessageOrDefault("GUI.customkits-menu.buttons.limit.name", "&e&lKit Limit");
        final List<String> limitLore = plugin.getLang().getMessageListOrDefault("GUI.customkits-menu.buttons.limit.lore",
                java.util.Arrays.asList(
                        "&7Kits: &a" + playerKits.size() + " &7/ &e" + limitStr,
                        "&7Remaining: &b" + remainingObj
                ),
                "count", playerKits.size(),
                "limit", limitStr,
                "remaining", remainingObj
        );

        set(45, new BaseButton(plugin, ItemBuilder.of(Material.BOOK)
                .name(limitName, plugin.getLang())
                .lore(limitLore, plugin.getLang())
                .build()) {
            @Override
            public void onClick(final Player player) {
            }
        });

        // Slot 49: Create New Kit Button
        final boolean reached = plugin.getCustomKitManager().hasReachedLimit(player);
        final Material createMat = reached ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK;
        final String createName = reached
                ? plugin.getLang().getMessageOrDefault("GUI.customkits-menu.buttons.create.name-reached", "&c&lKit Limit Reached")
                : plugin.getLang().getMessageOrDefault("GUI.customkits-menu.buttons.create.name", "&a&l+ Create New Kit");
        final List<String> createLore = reached
                ? plugin.getLang().getMessageListOrDefault("GUI.customkits-menu.buttons.create.lore-reached",
                        java.util.Collections.singletonList("&cYou cannot create more custom kits."))
                : plugin.getLang().getMessageListOrDefault("GUI.customkits-menu.buttons.create.lore",
                        java.util.Collections.singletonList("&7Click to name and create a new kit."));

        set(49, new BaseButton(plugin, ItemBuilder.of(createMat)
                .name(createName, plugin.getLang())
                .lore(createLore, plugin.getLang())
                .build()) {
            @Override
            public void onClick(final Player player) {
                if (reached) {
                    plugin.getLang().sendMessage(player, "ERROR.customkits.limit-reached", "limit", maxKits);
                    return;
                }

                if (!player.hasPermission(Permissions.CUSTOMKITS_CREATE) && !player.hasPermission(Permissions.CUSTOMKITS_USE)) {
                    plugin.getLang().sendMessage(player, "ERROR.no-permission", "permission", Permissions.CUSTOMKITS_CREATE);
                    return;
                }

                promptCreateKit(player);
            }
        });

        // Slot 53: Close Button
        final String closeName = plugin.getLang().getMessageOrDefault("GUI.customkits-menu.buttons.close.name", "&c&lClose Menu");
        final List<String> closeLore = plugin.getLang().getMessageListOrDefault("GUI.customkits-menu.buttons.close.lore",
                java.util.Collections.singletonList("&7Click to exit."));

        set(53, new BaseButton(plugin, ItemBuilder.of(Material.BARRIER)
                .name(closeName, plugin.getLang())
                .lore(closeLore, plugin.getLang())
                .build()) {
            @Override
            public void onClick(final Player player) {
                plugin.getGuiListener().removeGui(player, CustomKitMenuGui.this);
                player.closeInventory();
            }
        });
    }

    private void promptCreateKit(final Player player) {
        final ChatInputManager inputManager = new ChatInputManager(plugin);
        inputManager.prompt(
                player,
                plugin.getLang().getMessage("COMMAND.customkits.enter-name"),
                input -> {
                    final CustomKitImpl newKit = plugin.getCustomKitManager().createKit(player, input.trim());
                    if (newKit != null) {
                        plugin.getLang().sendMessage(player, "COMMAND.customkits.created", "kit", newKit.getName());
                        final CustomKitEditSession session = plugin.getCustomKitManager().startSession(player, newKit, true);
                        CustomKitEditorGui.open(plugin, player, session);
                    } else {
                        CustomKitMenuGui.open(plugin, player);
                    }
                },
                () -> CustomKitMenuGui.open(plugin, player)
        );
    }

    private void promptDuplicateKit(final Player player, final CustomKitImpl original) {
        final ChatInputManager inputManager = new ChatInputManager(plugin);
        inputManager.prompt(
                player,
                plugin.getLang().getMessage("COMMAND.customkits.enter-new-name"),
                input -> {
                    final CustomKitImpl dup = plugin.getCustomKitManager().duplicateKit(player, original.getUniqueId(), input.trim());
                    if (dup != null) {
                        plugin.getLang().sendMessage(player, "COMMAND.customkits.duplicated", "kit", dup.getName());
                    }
                    CustomKitMenuGui.open(plugin, player);
                },
                () -> CustomKitMenuGui.open(plugin, player)
        );
    }

    public static void open(@NotNull final DuelsPlugin plugin, @NotNull final Player player) {
        final CustomKitMenuGui gui = plugin.getGuiListener().addGui(player, new CustomKitMenuGui(plugin, player), true);
        gui.open(player);
    }
}
