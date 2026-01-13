package com.meteordevelopments.duels.gui.settings;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.gui.settings.buttons.*;
import com.meteordevelopments.duels.util.compat.CompatUtil;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.gui.SinglePageGui;
import com.meteordevelopments.duels.util.inventory.Slots;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SettingsGui extends SinglePageGui<DuelsPlugin> {

    private static final int[][] PATTERNS = {
            {13},
            {12, 14},
            {12, 13, 14},
            {12, 13, 14, 22}
    };

    public SettingsGui(final DuelsPlugin plugin) {
        super(plugin, plugin.getLang().getMessage("GUI.settings.title"), 3);
        final Config config = plugin.getConfiguration();
        final ItemStack spacing = Items.from(config.getSettingsFillerType(), config.getSettingsFillerData());
        Slots.run(2, 7, slot -> inventory.setItem(slot, spacing));
        Slots.run(11, 16, slot -> inventory.setItem(slot, spacing));
        Slots.run(20, 25, slot -> inventory.setItem(slot, spacing));
        set(4, new RequestDetailsButton(plugin));

        final List<BaseButton> buttons = new ArrayList<>();

        if (config.isKitSelectingEnabled()) {
            buttons.add(new KitSelectButton(plugin));
        }

        if (config.isOwnInventoryEnabled()) {
            buttons.add(new OwnInventoryButton(plugin));
        }

        if (config.isArenaSelectingEnabled()) {
            buttons.add(new ArenaSelectButton(plugin));
        }

        if (config.isItemBettingEnabled()) {
            buttons.add(new ItemBettingButton(plugin));
        }

        if (!buttons.isEmpty()) {
            final int[] pattern = PATTERNS[buttons.size() - 1];

            for (int i = 0; i < buttons.size(); i++) {
                set(pattern[i], buttons.get(i));
            }
        }

        // Use configurable button slots from config
        final RequestSendButton sendButton = new RequestSendButton(plugin);
        final RequestSendButton sendButtonGlowing = new RequestSendButton(plugin);
        addGlowEffect(sendButtonGlowing);
        
        for (final int slot : config.getSendButtonSlots()) {
            if (config.getSendButtonGlowingSlots().contains(slot)) {
                set(slot, sendButtonGlowing);
            } else {
                set(slot, sendButton);
            }
        }

        final CancelButton cancelButton = new CancelButton(plugin);
        final CancelButton cancelButtonGlowing = new CancelButton(plugin);
        addGlowEffect(cancelButtonGlowing);
        
        for (final int slot : config.getCancelButtonSlots()) {
            if (config.getCancelButtonGlowingSlots().contains(slot)) {
                set(slot, cancelButtonGlowing);
            } else {
                set(slot, cancelButton);
            }
        }
    }
    
    /**
     * Adds enchantment glow effect to a button without showing enchantment text
     */
    private void addGlowEffect(final BaseButton button) {
        final ItemStack displayed = button.getDisplayed();
        final ItemMeta meta = displayed.getItemMeta();
        
        if (meta != null) {
            meta.addEnchant(Enchantment.DURABILITY, 1, false);
            
            if (CompatUtil.hasItemFlag()) {
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            
            displayed.setItemMeta(meta);
        }
    }
}
