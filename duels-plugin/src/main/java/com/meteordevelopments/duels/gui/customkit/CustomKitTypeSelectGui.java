package com.meteordevelopments.duels.gui.customkit;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.gui.configuration.GuiDecoration;
import com.meteordevelopments.duels.gui.configuration.GuiItemConfig;
import com.meteordevelopments.duels.gui.configuration.KitTypeSelectGuiConfig;
import com.meteordevelopments.duels.setting.Settings;
import com.meteordevelopments.duels.util.gui.SinglePageGui;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class CustomKitTypeSelectGui extends SinglePageGui<DuelsPlugin> {

    public CustomKitTypeSelectGui(final DuelsPlugin plugin, final Player player) {
        super(plugin,
                plugin.getGuiConfigManager().getKitTypeSelectGuiConfig() != null
                        ? plugin.getGuiConfigManager().getKitTypeSelectGuiConfig().getTitle()
                        : plugin.getLang().getMessage("GUI.kit-type-selector.title"),
                plugin.getGuiConfigManager().getKitTypeSelectGuiConfig() != null
                        ? plugin.getGuiConfigManager().getKitTypeSelectGuiConfig().getRows()
                        : 4);

        final KitTypeSelectGuiConfig guiConfig = plugin.getGuiConfigManager().getKitTypeSelectGuiConfig();
        final Config config = plugin.getConfiguration();

        if (guiConfig == null) {
            return;
        }

        // Place decorations
        for (final GuiDecoration decoration : guiConfig.getDecorations().values()) {
            final GuiItemConfig itemConfig = decoration.getItemConfig();
            for (final int slot : decoration.getSlots()) {
                if (slot >= 0 && slot < inventory.getSize()) {
                    final boolean glow = itemConfig.isGlowingAt(slot);
                    final ItemStack item = itemConfig.buildItem(plugin.getLang(), glow);
                    inventory.setItem(slot, item);
                }
            }
        }

        // Place Server Kits button
        final GuiItemConfig serverKitsCfg = guiConfig.getServerKitsButton();
        if (serverKitsCfg != null) {
            for (final int slot : serverKitsCfg.getSlots()) {
                if (slot >= 0 && slot < inventory.getSize()) {
                    final boolean glow = serverKitsCfg.isGlowingAt(slot);
                    final ItemStack item = serverKitsCfg.buildItem(plugin.getLang(), glow);
                    set(slot, new BaseButton(plugin, item) {
                        @Override
                        public void onClick(final Player player) {
                            kitManager.getGui().open(player);
                        }
                    });
                }
            }
        }

        // Place Custom Kits button
        final GuiItemConfig customKitsCfg = guiConfig.getCustomKitsButton();
        if (customKitsCfg != null) {
            final boolean hasPermission = player.hasPermission(Permissions.CUSTOMKITS_USE);
            for (final int slot : customKitsCfg.getSlots()) {
                if (slot >= 0 && slot < inventory.getSize()) {
                    final boolean glow = customKitsCfg.isGlowingAt(slot);
                    final ItemStack item;
                    if (!hasPermission && customKitsCfg.getLoreNoPermission() != null && !customKitsCfg.getLoreNoPermission().isEmpty()) {
                        final GuiItemConfig noPermItem = new GuiItemConfig(customKitsCfg.getMaterial(), customKitsCfg.getData(), customKitsCfg.getName());
                        noPermItem.setLore(customKitsCfg.getLoreNoPermission());
                        noPermItem.setCustomModelData(customKitsCfg.getCustomModelData());
                        item = noPermItem.buildItem(plugin.getLang(), glow);
                    } else {
                        item = customKitsCfg.buildItem(plugin.getLang(), glow);
                    }

                    set(slot, new BaseButton(plugin, item) {
                        @Override
                        public void onClick(final Player player) {
                            if (!player.hasPermission(Permissions.CUSTOMKITS_USE)) {
                                plugin.getLang().sendMessage(player, "ERROR.no-permission", "permission", Permissions.CUSTOMKITS_USE);
                                return;
                            }
                            CustomKitSelectGui.open(plugin, player);
                        }
                    });
                }
            }
        }

        // Place Own Inventory button
        final GuiItemConfig ownInvCfg = guiConfig.getOwnInventoryButton();
        if (config.isOwnInventoryEnabled() && ownInvCfg != null) {
            final boolean hasPermission = !config.isOwnInventoryUsePermission()
                    || player.hasPermission(Permissions.OWN_INVENTORY)
                    || player.hasPermission(Permissions.SETTING_ALL);

            for (final int slot : ownInvCfg.getSlots()) {
                if (slot >= 0 && slot < inventory.getSize()) {
                    final boolean glow = ownInvCfg.isGlowingAt(slot);
                    final ItemStack item;
                    if (!hasPermission && ownInvCfg.getLoreNoPermission() != null && !ownInvCfg.getLoreNoPermission().isEmpty()) {
                        final GuiItemConfig noPermItem = new GuiItemConfig(ownInvCfg.getMaterial(), ownInvCfg.getData(), ownInvCfg.getName());
                        noPermItem.setLore(ownInvCfg.getLoreNoPermission());
                        noPermItem.setCustomModelData(ownInvCfg.getCustomModelData());
                        item = noPermItem.buildItem(plugin.getLang(), glow);
                    } else {
                        item = ownInvCfg.buildItem(plugin.getLang(), glow);
                    }

                    set(slot, new BaseButton(plugin, item) {
                        @Override
                        public void onClick(final Player player) {
                            if (config.isOwnInventoryUsePermission() && !player.hasPermission(Permissions.OWN_INVENTORY) && !player.hasPermission(Permissions.SETTING_ALL)) {
                                lang.sendMessage(player, "ERROR.no-permission", "permission", Permissions.OWN_INVENTORY);
                                return;
                            }
                            final Settings settings = settingManager.getSafely(player);
                            settings.setOwnInventory(true);
                            settings.openGui(player);
                        }
                    });
                }
            }
        }

        // Place Back button
        final GuiItemConfig backCfg = guiConfig.getBackButton();
        if (backCfg != null) {
            for (final int slot : backCfg.getSlots()) {
                if (slot >= 0 && slot < inventory.getSize()) {
                    final boolean glow = backCfg.isGlowingAt(slot);
                    final ItemStack item = backCfg.buildItem(plugin.getLang(), glow);
                    set(slot, new BaseButton(plugin, item) {
                        @Override
                        public void onClick(final Player player) {
                            final Settings settings = settingManager.getSafely(player);
                            settings.openGui(player);
                        }
                    });
                }
            }
        }
    }

    public static void open(@NotNull final DuelsPlugin plugin, @NotNull final Player player) {
        final CustomKitTypeSelectGui gui = plugin.getGuiListener().addGui(player, new CustomKitTypeSelectGui(plugin, player), true);
        gui.open(player);
    }
}
