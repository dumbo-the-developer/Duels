package com.meteordevelopments.duels.gui.customkit;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.api.customkit.CustomKit;
import com.meteordevelopments.duels.core.customkit.CustomKitImpl;
import com.meteordevelopments.duels.core.customkit.validation.CustomKitValidator;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.gui.configuration.CustomKitSelectGuiConfig;
import com.meteordevelopments.duels.gui.configuration.GuiDecoration;
import com.meteordevelopments.duels.gui.configuration.GuiItemConfig;
import com.meteordevelopments.duels.setting.Settings;
import com.meteordevelopments.duels.util.gui.SinglePageGui;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomKitSelectGui extends SinglePageGui<DuelsPlugin> {

    public CustomKitSelectGui(final DuelsPlugin plugin, final Player player) {
        super(plugin,
                plugin.getGuiConfigManager().getCustomKitSelectGuiConfig() != null
                        ? plugin.getGuiConfigManager().getCustomKitSelectGuiConfig().getTitle()
                        : plugin.getLang().getMessage("GUI.customkit-selector.title"),
                plugin.getGuiConfigManager().getCustomKitSelectGuiConfig() != null
                        ? plugin.getGuiConfigManager().getCustomKitSelectGuiConfig().getRows()
                        : 4);

        render(player);
    }

    private void render(final Player player) {
        inventory.clear();

        final CustomKitSelectGuiConfig guiConfig = plugin.getGuiConfigManager().getCustomKitSelectGuiConfig();
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

        final List<CustomKit> playerKits = plugin.getCustomKitManager().getKits(player.getUniqueId());

        if (playerKits.isEmpty()) {
            final GuiItemConfig emptyCfg = guiConfig.getEmptyButton();
            if (emptyCfg != null && !emptyCfg.getSlots().isEmpty()) {
                for (final int slot : emptyCfg.getSlots()) {
                    if (slot >= 0 && slot < inventory.getSize()) {
                        final boolean glow = emptyCfg.isGlowingAt(slot);
                        final ItemStack item = emptyCfg.buildItem(plugin.getLang(), glow);
                        set(slot, new BaseButton(plugin, item) {
                            @Override
                            public void onClick(final Player player) {
                                CustomKitTypeSelectGui.open(plugin, player);
                            }
                        });
                    }
                }
            } else if (inventory.getSize() > 22) {
                set(22, new BaseButton(plugin, ItemBuilder.of(Material.STICK)
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
            }
        } else {
            final List<Integer> itemSlots = !guiConfig.getItemSlots().isEmpty()
                    ? guiConfig.getItemSlots()
                    : defaultSlots();

            final GuiItemConfig template = guiConfig.getCustomKitButton();

            int index = 0;
            for (final CustomKit kit : playerKits) {
                if (index >= itemSlots.size()) {
                    break;
                }

                final int slot = itemSlots.get(index);
                if (slot < 0 || slot >= inventory.getSize()) {
                    index++;
                    continue;
                }

                final CustomKitImpl impl = (CustomKitImpl) kit;
                final int itemCount = impl.getItems().size() + impl.getArmor().size() + (impl.getOffHand() != null ? 1 : 0);

                final ItemStack iconItem;
                if (impl.getIcon() != null) {
                    iconItem = impl.getIcon().clone();
                } else if (template != null) {
                    final ItemStack base = template.buildItem(plugin.getLang(), false);
                    iconItem = base.clone();
                } else {
                    iconItem = new ItemStack(Material.NETHERITE_SWORD);
                }

                final Map<String, String> placeholders = new HashMap<>();
                placeholders.put("name", impl.getName());
                placeholders.put("items", String.valueOf(itemCount));

                final List<String> lore = new ArrayList<>();
                if (template != null && template.getLore() != null && !template.getLore().isEmpty()) {
                    for (final String line : template.getLore()) {
                        if (line.contains("%description%")) {
                            if (!impl.getDescription().isEmpty()) {
                                for (final String desc : impl.getDescription()) {
                                    lore.add("&7" + desc);
                                }
                            }
                        } else {
                            lore.add(formatPlaceholders(line, placeholders));
                        }
                    }
                } else {
                    if (!impl.getDescription().isEmpty()) {
                        for (final String desc : impl.getDescription()) {
                            lore.add("&7" + desc);
                        }
                    }
                    lore.add("&7Items: &e" + itemCount);
                    lore.add("");
                    lore.add("&aClick to select this kit for duel request");
                    lore.add("&7Note: Custom kit duels only support Money Betting");
                }

                final String displayName = template != null && template.getName() != null
                        ? formatPlaceholders(template.getName(), placeholders)
                        : "&d&l" + impl.getName();

                final BaseButton kitBtn = new BaseButton(plugin, ItemBuilder.of(iconItem)
                        .name(displayName, plugin.getLang())
                        .lore(lore, plugin.getLang())
                        .build()) {
                    @Override
                    public void onClick(final Player player) {
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
                index++;
            }
        }

        // Place Back button
        final GuiItemConfig backCfg = guiConfig.getBackButton();
        if (backCfg != null && !backCfg.getSlots().isEmpty()) {
            for (final int slot : backCfg.getSlots()) {
                if (slot >= 0 && slot < inventory.getSize()) {
                    final boolean glow = backCfg.isGlowingAt(slot);
                    final ItemStack item = backCfg.buildItem(plugin.getLang(), glow);
                    set(slot, new BaseButton(plugin, item) {
                        @Override
                        public void onClick(final Player player) {
                            CustomKitTypeSelectGui.open(plugin, player);
                        }
                    });
                }
            }
        }
    }

    private List<Integer> defaultSlots() {
        final List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < inventory.getSize() - 9; i++) {
            slots.add(i);
        }
        return slots;
    }

    private String formatPlaceholders(String text, final Map<String, String> placeholders) {
        if (text == null || placeholders == null || placeholders.isEmpty()) {
            return text;
        }

        for (final Map.Entry<String, String> entry : placeholders.entrySet()) {
            text = text.replace("%" + entry.getKey() + "%", entry.getValue() != null ? entry.getValue() : "");
        }

        return text;
    }

    public static void open(@NotNull final DuelsPlugin plugin, @NotNull final Player player) {
        final CustomKitSelectGui gui = plugin.getGuiListener().addGui(player, new CustomKitSelectGui(plugin, player), true);
        gui.open(player);
    }
}
