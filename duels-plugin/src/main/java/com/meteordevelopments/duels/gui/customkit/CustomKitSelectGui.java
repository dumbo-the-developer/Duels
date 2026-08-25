package com.meteordevelopments.duels.gui.customkit;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.api.customkit.CustomKit;
import com.meteordevelopments.duels.core.customkit.CustomKitImpl;
import com.meteordevelopments.duels.core.customkit.validation.CustomKitValidator;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.setting.Settings;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.gui.SinglePageGui;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CustomKitSelectGui extends SinglePageGui<DuelsPlugin> {

    public CustomKitSelectGui(final DuelsPlugin plugin, final Player player) {
        super(plugin, plugin.getLang().getMessage("GUI.customkit-selector.title"), 6);

        render(player);
    }

    private void render(final Player player) {
        inventory.clear();

        final List<CustomKit> playerKits = plugin.getCustomKitManager().getKits(player.getUniqueId());

        if (playerKits.isEmpty()) {
            set(22, new BaseButton(plugin, ItemBuilder.of(Material.BARRIER)
                    .name("&c&lNo Custom Kits Found", plugin.getLang())
                    .lore(plugin.getLang(),
                            "&7You have not created any custom kits yet.",
                            "&7Use &e/customkits &7to create your first kit!"
                    ).build()) {
                @Override
                public void onClick(final Player player) {
                    CustomKitTypeSelectGui.open(plugin, player);
                }
            });
        } else {
            int slot = 0;
            for (final CustomKit kit : playerKits) {
                if (slot > 44) break;

                final CustomKitImpl impl = (CustomKitImpl) kit;
                final int itemCount = impl.getItems().size() + impl.getArmor().size() + (impl.getOffHand() != null ? 1 : 0);

                final List<String> lore = new ArrayList<>();
                if (!impl.getDescription().isEmpty()) {
                    for (final String desc : impl.getDescription()) {
                        lore.add("&7" + desc);
                    }
                }
                lore.add("&7Items: &e" + itemCount);
                lore.add("");
                lore.add("&aClick to select this kit for duel request");
                lore.add("&7Note: Custom kit duels only support Money Betting");

                final ItemStack iconItem = impl.getIcon() != null ? impl.getIcon().clone() : new ItemStack(Material.NETHERITE_SWORD);

                final BaseButton kitBtn = new BaseButton(plugin, ItemBuilder.of(iconItem)
                        .name("&b&l" + impl.getName(), plugin.getLang())
                        .lore(lore, plugin.getLang())
                        .build()) {
                    @Override
                    public void onClick(final Player player) {
                        // Validate kit before allowing selection
                        final CustomKitValidator.ValidationResult val = plugin.getCustomKitManager().getValidator().validateKit(
                                impl, player, plugin.getCustomKitManager().getCustomKitsConfig()
                        );

                        if (!val.isValid()) {
                            plugin.getLang().sendMessage(player, val.getMessageKey(), val.getReplacers());
                            return;
                        }

                        final Settings settings = settingManager.getSafely(player);
                        settings.setCustomKit(impl);
                        settings.openGui(player);
                    }
                };

                set(slot, kitBtn);
                slot++;
            }
        }

        // Bottom Navigation Bar (slots 45-53)
        final ItemStack filler = Items.GRAY_PANE.clone();
        for (int s = 45; s < 54; s++) {
            inventory.setItem(s, filler);
        }

        // Back Button at slot 49
        set(49, new BaseButton(plugin, ItemBuilder.of(Material.BARRIER)
                .name("&c&lBack", plugin.getLang())
                .lore(plugin.getLang(), "&7Click to return.")
                .build()) {
            @Override
            public void onClick(final Player player) {
                CustomKitTypeSelectGui.open(plugin, player);
            }
        });
    }

    public static void open(@NotNull final DuelsPlugin plugin, @NotNull final Player player) {
        final CustomKitSelectGui gui = plugin.getGuiListener().addGui(player, new CustomKitSelectGui(plugin, player), true);
        gui.open(player);
    }
}
