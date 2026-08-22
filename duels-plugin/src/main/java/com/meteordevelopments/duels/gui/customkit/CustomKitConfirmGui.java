package com.meteordevelopments.duels.gui.customkit;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.util.gui.SinglePageGui;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CustomKitConfirmGui extends SinglePageGui<DuelsPlugin> {

    public CustomKitConfirmGui(final DuelsPlugin plugin,
                              final String title,
                              final String questionText,
                              final Runnable onConfirm,
                              final Runnable onCancel) {
        super(plugin, title, 3);

        final BaseButton confirmButton = new BaseButton(plugin, ItemBuilder.of(Material.LIME_CONCRETE)
                .name("&a&lConfirm", plugin.getLang())
                .lore(plugin.getLang(), "&7Click to proceed with this action.")
                .build()) {
            @Override
            public void onClick(final Player player) {
                plugin.getGuiListener().removeGui(player, CustomKitConfirmGui.this);
                player.closeInventory();
                if (onConfirm != null) {
                    onConfirm.run();
                }
            }
        };

        final BaseButton cancelButton = new BaseButton(plugin, ItemBuilder.of(Material.RED_CONCRETE)
                .name("&c&lCancel", plugin.getLang())
                .lore(plugin.getLang(), "&7Click to cancel and go back.")
                .build()) {
            @Override
            public void onClick(final Player player) {
                plugin.getGuiListener().removeGui(player, CustomKitConfirmGui.this);
                player.closeInventory();
                if (onCancel != null) {
                    onCancel.run();
                }
            }
        };

        final BaseButton infoButton = new BaseButton(plugin, ItemBuilder.of(Material.PAPER)
                .name("&e&lInformation", plugin.getLang())
                .lore(plugin.getLang(), "&7" + questionText)
                .build()) {
            @Override
            public void onClick(final Player player) {
            }
        };

        set(11, confirmButton);
        set(13, infoButton);
        set(15, cancelButton);
    }

    public static void open(@NotNull final DuelsPlugin plugin,
                            @NotNull final Player player,
                            @NotNull final String title,
                            @NotNull final String questionText,
                            @NotNull final Runnable onConfirm,
                            @NotNull final Runnable onCancel) {
        final CustomKitConfirmGui gui = plugin.getGuiListener().addGui(player, new CustomKitConfirmGui(plugin, title, questionText, onConfirm, onCancel), true);
        gui.open(player);
    }
}
