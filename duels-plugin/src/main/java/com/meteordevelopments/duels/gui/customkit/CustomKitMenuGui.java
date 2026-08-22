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
        final String limitStr = maxKits == Integer.MAX_VALUE ? "Unlimited" : String.valueOf(maxKits);

        // Display player custom kits (slots 0-44)
        int slot = 0;
        for (final CustomKit kit : playerKits) {
            if (slot >= 45) break;
            final CustomKitImpl impl = (CustomKitImpl) kit;

            final List<String> lore = new ArrayList<>();
            if (!impl.getDescription().isEmpty()) {
                for (final String line : impl.getDescription()) {
                    lore.add("&7" + line);
                }
                lore.add("");
            }
            lore.add("&7Items: &f" + (impl.getItems().size() + impl.getArmor().size() + (impl.getOffHand() != null ? 1 : 0)));
            lore.add("");
            lore.add("&a[Left-Click] &7Edit Kit");
            lore.add("&b[Right-Click] &7Preview Kit");
            lore.add("&e[Shift-Left] &7Duplicate Kit");
            lore.add("&c[Shift-Right] &7Delete Kit");

            final ItemStack iconItem = impl.getIcon() != null ? impl.getIcon().clone() : new ItemStack(Material.NETHERITE_SWORD);

            final BaseButton kitBtn = new BaseButton(plugin, ItemBuilder.of(iconItem)
                    .name("&e&l" + impl.getName(), plugin.getLang())
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
                        CustomKitConfirmGui.open(
                                plugin,
                                player,
                                "Delete Kit?",
                                "Are you sure you want to delete kit '" + impl.getName() + "'?",
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
        set(45, new BaseButton(plugin, ItemBuilder.of(Material.BOOK)
                .name("&e&lKit Limit", plugin.getLang())
                .lore(plugin.getLang(),
                        "&7Kits: &a" + playerKits.size() + " &7/ &e" + limitStr,
                        "&7Remaining: &b" + (maxKits == Integer.MAX_VALUE ? "Unlimited" : Math.max(0, maxKits - playerKits.size()))
                ).build()) {
            @Override
            public void onClick(final Player player) {
            }
        });

        // Slot 49: Create New Kit Button
        final boolean reached = plugin.getCustomKitManager().hasReachedLimit(player);
        final Material createMat = reached ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK;
        final String createName = reached ? "&c&lKit Limit Reached" : "&a&l+ Create New Kit";

        set(49, new BaseButton(plugin, ItemBuilder.of(createMat)
                .name(createName, plugin.getLang())
                .lore(plugin.getLang(),
                        reached ? "&cYou cannot create more custom kits." : "&7Click to name and create a new kit."
                ).build()) {
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
        set(53, new BaseButton(plugin, ItemBuilder.of(Material.BARRIER)
                .name("&c&lClose Menu", plugin.getLang())
                .lore(plugin.getLang(), "&7Click to exit.")
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
