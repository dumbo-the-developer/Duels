package com.meteordevelopments.duels.gui.settings;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.gui.configuration.GuiDecoration;
import com.meteordevelopments.duels.gui.configuration.GuiItemConfig;
import com.meteordevelopments.duels.gui.configuration.SettingsGuiConfig;
import com.meteordevelopments.duels.gui.settings.buttons.*;
import com.meteordevelopments.duels.util.gui.SinglePageGui;
import org.bukkit.inventory.ItemStack;

public class SettingsGui extends SinglePageGui<DuelsPlugin> {

    public SettingsGui(final DuelsPlugin plugin) {
        super(plugin,
                plugin.getGuiConfigManager().getSettingsGuiConfig() != null
                        ? plugin.getGuiConfigManager().getSettingsGuiConfig().getTitle()
                        : plugin.getLang().getMessage("GUI.settings.title"),
                plugin.getGuiConfigManager().getSettingsGuiConfig() != null
                        ? plugin.getGuiConfigManager().getSettingsGuiConfig().getRows()
                        : 6);

        final SettingsGuiConfig guiConfig = plugin.getGuiConfigManager().getSettingsGuiConfig();
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

        // Place details button
        final GuiItemConfig detailsCfg = guiConfig.getDetailsButton();
        if (detailsCfg != null) {
            for (final int slot : detailsCfg.getSlots()) {
                if (slot >= 0 && slot < inventory.getSize()) {
                    final boolean glow = detailsCfg.isGlowingAt(slot);
                    set(slot, new RequestDetailsButton(plugin, detailsCfg, glow));
                }
            }
        }

        // Place kit selector button
        final GuiItemConfig kitCfg = guiConfig.getKitSelectButton();
        if (kitCfg != null && config.isKitSelectingEnabled()) {
            for (final int slot : kitCfg.getSlots()) {
                if (slot >= 0 && slot < inventory.getSize()) {
                    final boolean glow = kitCfg.isGlowingAt(slot);
                    set(slot, new KitSelectButton(plugin, kitCfg, glow));
                }
            }
        }

        // Place own inventory button
        final GuiItemConfig ownInvCfg = guiConfig.getOwnInventoryButton();
        if (ownInvCfg != null && config.isOwnInventoryEnabled()) {
            for (final int slot : ownInvCfg.getSlots()) {
                if (slot >= 0 && slot < inventory.getSize()) {
                    final boolean glow = ownInvCfg.isGlowingAt(slot);
                    set(slot, new OwnInventoryButton(plugin, ownInvCfg, glow));
                }
            }
        }

        // Place arena selector button
        final GuiItemConfig arenaCfg = guiConfig.getArenaSelectButton();
        if (arenaCfg != null && config.isArenaSelectingEnabled()) {
            for (final int slot : arenaCfg.getSlots()) {
                if (slot >= 0 && slot < inventory.getSize()) {
                    final boolean glow = arenaCfg.isGlowingAt(slot);
                    set(slot, new ArenaSelectButton(plugin, arenaCfg, glow));
                }
            }
        }

        // Place item betting button
        final GuiItemConfig betCfg = guiConfig.getItemBettingButton();
        if (betCfg != null && config.isItemBettingEnabled()) {
            for (final int slot : betCfg.getSlots()) {
                if (slot >= 0 && slot < inventory.getSize()) {
                    final boolean glow = betCfg.isGlowingAt(slot);
                    set(slot, new ItemBettingButton(plugin, betCfg, glow));
                }
            }
        }

        // Place send buttons
        final GuiItemConfig sendCfg = guiConfig.getSendButton();
        if (sendCfg != null) {
            for (final int slot : sendCfg.getSlots()) {
                if (slot >= 0 && slot < inventory.getSize()) {
                    final boolean glow = sendCfg.isGlowingAt(slot);
                    set(slot, new RequestSendButton(plugin, sendCfg, glow));
                }
            }
        }

        // Place cancel buttons
        final GuiItemConfig cancelCfg = guiConfig.getCancelButton();
        if (cancelCfg != null) {
            for (final int slot : cancelCfg.getSlots()) {
                if (slot >= 0 && slot < inventory.getSize()) {
                    final boolean glow = cancelCfg.isGlowingAt(slot);
                    set(slot, new CancelButton(plugin, cancelCfg, glow));
                }
            }
        }
    }
}
