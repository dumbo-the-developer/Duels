package com.meteordevelopments.duels.gui.customkit;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.core.customkit.session.CustomKitEditSession;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.gui.SinglePageGui;
import com.meteordevelopments.duels.util.input.ChatInputManager;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ItemEditorGui extends SinglePageGui<DuelsPlugin> {

    private final CustomKitEditSession session;
    private final int targetSlot;
    private final boolean isArmor;
    private final boolean isOffHand;

    public ItemEditorGui(final DuelsPlugin plugin,
                         final CustomKitEditSession session,
                         final int targetSlot,
                         final boolean isArmor,
                         final boolean isOffHand) {
        super(plugin, plugin.getLang().getMessage("GUI.item-editor.title"), 6);
        this.session = session;
        this.targetSlot = targetSlot;
        this.isArmor = isArmor;
        this.isOffHand = isOffHand;

        render();
    }

    private ItemStack getItem() {
        if (isArmor) {
            return session.getDraftKit().getArmor().get(targetSlot);
        } else if (isOffHand) {
            return session.getDraftKit().getOffHand();
        } else {
            return session.getDraftKit().getItems().get(targetSlot);
        }
    }

    private void render() {
        inventory.clear();
        final ItemStack item = getItem();

        if (item == null || item.getType() == Material.AIR) {
            MaterialBrowserGui.open(plugin, (Player) inventory.getViewers().iterator().next(), session, targetSlot, isArmor, isOffHand);
            return;
        }

        final ItemMeta meta = item.getItemMeta();

        // Slot 4: Item Preview
        set(4, new BaseButton(plugin, item.clone()) {
            @Override
            public void onClick(final Player player) {
            }
        });

        // Slot 10: Change Material
        final List<String> changeMatLore = new ArrayList<>();
        for (final String line : plugin.getLang().getMessageListOrDefault("GUI.item-editor.buttons.change-material.lore",
                List.of("&7Current: &f%material%", "", "&aClick to browse materials"))) {
            changeMatLore.add(line.replace("%material%", item.getType().name()));
        }
        set(10, new BaseButton(plugin, ItemBuilder.of(Material.CHEST)
                .name(plugin.getLang().getMessageOrDefault("GUI.item-editor.buttons.change-material.name", "&e&lChange Material"), plugin.getLang())
                .lore(changeMatLore, plugin.getLang())
                .build()) {
            @Override
            public void onClick(final Player player) {
                MaterialBrowserGui.open(plugin, player, session, targetSlot, isArmor, isOffHand);
            }
        });

        // Slot 11: Change Amount
        final int amount = item.getAmount();
        set(11, new BaseButton(plugin, ItemBuilder.of(Material.HOPPER)
                .name(plugin.getLang().getMessageOrDefault("GUI.item-editor.buttons.change-amount.name", "&e&lChange Amount: &a%amount%")
                        .replace("%amount%", String.valueOf(amount)), plugin.getLang())
                .lore(plugin.getLang().getMessageListOrDefault("GUI.item-editor.buttons.change-amount.lore",
                        List.of(
                                "&a[Left-Click] &7+1",
                                "&e[Right-Click] &7-1",
                                "&b[Shift-Left] &7+16",
                                "&c[Shift-Right] &7-16"
                        )), plugin.getLang())
                .build()) {
            @Override
            public void onClick(final Player player, final org.bukkit.event.inventory.InventoryClickEvent event) {
                int delta = 1;
                if (event.isShiftClick()) {
                    delta = 16;
                }
                if (event.isRightClick()) {
                    delta = -delta;
                }
                final int newAmount = Math.max(1, Math.min(64, amount + delta));
                item.setAmount(newAmount);
                session.touch();
                render();
            }
        });

        // Slot 12: Display Name & Lore
        set(12, new BaseButton(plugin, ItemBuilder.of(Material.WRITABLE_BOOK)
                .name(plugin.getLang().getMessageOrDefault("GUI.item-editor.buttons.display-name-lore.name", "&e&lDisplay Name & Lore"), plugin.getLang())
                .lore(plugin.getLang().getMessageListOrDefault("GUI.item-editor.buttons.display-name-lore.lore",
                        List.of("&7Click to edit item name and lore lines.")), plugin.getLang())
                .build()) {
            @Override
            public void onClick(final Player player) {
                LoreEditorGui.open(plugin, player, session, targetSlot, isArmor, isOffHand);
            }
        });

        // Slot 14: Enchantments
        final int enchCount = item.getEnchantments().size();
        set(14, new BaseButton(plugin, ItemBuilder.of(Material.ENCHANTED_BOOK)
                .name(plugin.getLang().getMessageOrDefault("GUI.item-editor.buttons.enchantments.name", "&b&lEnchantments (&e%count%&b)")
                        .replace("%count%", String.valueOf(enchCount)), plugin.getLang())
                .lore(plugin.getLang().getMessageListOrDefault("GUI.item-editor.buttons.enchantments.lore",
                        List.of("&7Click to configure item enchantments.")), plugin.getLang())
                .build()) {
            @Override
            public void onClick(final Player player) {
                EnchantmentEditorGui.open(plugin, player, session, targetSlot, isArmor, isOffHand);
            }
        });

        // Slot 15: Attributes
        final int attrCount = (meta != null && meta.hasAttributeModifiers()) ? meta.getAttributeModifiers().size() : 0;
        set(15, new BaseButton(plugin, ItemBuilder.of(Material.NETHER_STAR)
                .name(plugin.getLang().getMessageOrDefault("GUI.item-editor.buttons.attributes.name", "&b&lAttributes (&e%count%&b)")
                        .replace("%count%", String.valueOf(attrCount)), plugin.getLang())
                .lore(plugin.getLang().getMessageListOrDefault("GUI.item-editor.buttons.attributes.lore",
                        List.of("&7Click to configure attribute modifiers.")), plugin.getLang())
                .build()) {
            @Override
            public void onClick(final Player player) {
                AttributeEditorGui.open(plugin, player, session, targetSlot, isArmor, isOffHand);
            }
        });

        // Slot 16: Armor Trims (only for armor)
        final String matName = item.getType().name();
        final boolean isArmorItem = matName.endsWith("_HELMET") || matName.endsWith("_CHESTPLATE") || matName.endsWith("_LEGGINGS") || matName.endsWith("_BOOTS");
        if (isArmorItem) {
            set(16, new BaseButton(plugin, ItemBuilder.of(Material.ARMOR_STAND)
                    .name(plugin.getLang().getMessageOrDefault("GUI.item-editor.buttons.armor-trim.name", "&b&lArmor Trim"), plugin.getLang())
                    .lore(plugin.getLang().getMessageListOrDefault("GUI.item-editor.buttons.armor-trim.lore",
                            List.of("&7Click to configure trim pattern and material.")), plugin.getLang())
                    .build()) {
                @Override
                public void onClick(final Player player) {
                    ArmorTrimEditorGui.open(plugin, player, session, targetSlot, isArmor, isOffHand);
                }
            });
        }

        // Slot 19: Potion Settings (for potion items)
        if (meta instanceof PotionMeta || item.getType().name().contains("POTION") || item.getType() == Material.TIPPED_ARROW) {
            set(19, new BaseButton(plugin, ItemBuilder.of(Material.BREWING_STAND)
                    .name(plugin.getLang().getMessageOrDefault("GUI.item-editor.buttons.potion-settings.name", "&d&lPotion Settings"), plugin.getLang())
                    .lore(plugin.getLang().getMessageListOrDefault("GUI.item-editor.buttons.potion-settings.lore",
                            List.of("&7Click to configure potion type and custom effects.")), plugin.getLang())
                    .build()) {
                @Override
                public void onClick(final Player player) {
                    PotionEditorGui.open(plugin, player, session, targetSlot, isArmor, isOffHand);
                }
            });
        }

        // Slot 20: Durability / Damage (for damageable items)
        if (meta instanceof Damageable dmg) {
            set(20, new BaseButton(plugin, ItemBuilder.of(Material.ANVIL)
                    .name(plugin.getLang().getMessageOrDefault("GUI.item-editor.buttons.durability-damage.name", "&e&lDurability Damage: &f%damage%")
                            .replace("%damage%", String.valueOf(dmg.getDamage())), plugin.getLang())
                    .lore(plugin.getLang().getMessageListOrDefault("GUI.item-editor.buttons.durability-damage.lore",
                            List.of(
                                    "&a[Left-Click] &7Restore full durability (0 damage)",
                                    "&e[Right-Click] &7Enter custom damage value"
                            )), plugin.getLang())
                    .build()) {
                @Override
                public void onClick(final Player player, final org.bukkit.event.inventory.InventoryClickEvent event) {
                    if (event.isRightClick()) {
                        promptDurabilityDamage(player, dmg);
                    } else {
                        dmg.setDamage(0);
                        item.setItemMeta((ItemMeta) dmg);
                        session.touch();
                        render();
                    }
                }
            });
        }

        // Slot 21: Unbreakable Toggle
        final boolean isUnbreakable = meta != null && meta.isUnbreakable();
        final String statusText = isUnbreakable
                ? plugin.getLang().getMessageOrDefault("GENERAL.enabled", "&aEnabled")
                : plugin.getLang().getMessageOrDefault("GENERAL.disabled", "&7Disabled");
        set(21, new BaseButton(plugin, ItemBuilder.of(isUnbreakable ? Material.LIME_DYE : Material.GRAY_DYE)
                .name(plugin.getLang().getMessageOrDefault("GUI.item-editor.buttons.unbreakable.name", "&eUnbreakable: %status%")
                        .replace("%status%", statusText), plugin.getLang())
                .lore(plugin.getLang().getMessageListOrDefault("GUI.item-editor.buttons.unbreakable.lore",
                        List.of("&aClick to toggle")), plugin.getLang())
                .build()) {
            @Override
            public void onClick(final Player player) {
                if (meta != null) {
                    meta.setUnbreakable(!isUnbreakable);
                    item.setItemMeta(meta);
                    session.touch();
                    render();
                }
            }
        });

        // Slot 22: Item Flags
        final String hideEnchantsVal = meta != null && meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS)
                ? plugin.getLang().getMessageOrDefault("GENERAL.true", "&aYES")
                : plugin.getLang().getMessageOrDefault("GENERAL.false", "&7NO");
        final String hideAttrsVal = meta != null && meta.hasItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                ? plugin.getLang().getMessageOrDefault("GENERAL.true", "&aYES")
                : plugin.getLang().getMessageOrDefault("GENERAL.false", "&7NO");
        final String hideUnbreakVal = meta != null && meta.hasItemFlag(ItemFlag.HIDE_UNBREAKABLE)
                ? plugin.getLang().getMessageOrDefault("GENERAL.true", "&aYES")
                : plugin.getLang().getMessageOrDefault("GENERAL.false", "&7NO");

        final List<String> itemFlagsLore = new ArrayList<>();
        for (final String line : plugin.getLang().getMessageListOrDefault("GUI.item-editor.buttons.item-flags.lore",
                List.of(
                        "&7Hide Enchants: %hide_enchants%",
                        "&7Hide Attributes: %hide_attributes%",
                        "&7Hide Unbreakable: %hide_unbreakable%",
                        "",
                        "&a[Left-Click] &7Toggle Hide Enchants",
                        "&e[Right-Click] &7Toggle Hide Attributes",
                        "&b[Shift-Click] &7Toggle Hide Unbreakable"
                ))) {
            itemFlagsLore.add(line
                    .replace("%hide_enchants%", hideEnchantsVal)
                    .replace("%hide_attributes%", hideAttrsVal)
                    .replace("%hide_unbreakable%", hideUnbreakVal));
        }

        set(22, new BaseButton(plugin, ItemBuilder.of(Material.FLOWER_BANNER_PATTERN)
                .name(plugin.getLang().getMessageOrDefault("GUI.item-editor.buttons.item-flags.name", "&e&lItem Flags"), plugin.getLang())
                .lore(itemFlagsLore, plugin.getLang())
                .build()) {
            @Override
            public void onClick(final Player player, final org.bukkit.event.inventory.InventoryClickEvent event) {
                if (meta != null) {
                    if (event.isRightClick()) {
                        if (meta.hasItemFlag(ItemFlag.HIDE_ATTRIBUTES)) {
                            meta.removeItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        } else {
                            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        }
                    } else if (event.isShiftClick()) {
                        if (meta.hasItemFlag(ItemFlag.HIDE_UNBREAKABLE)) {
                            meta.removeItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                        } else {
                            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                        }
                    } else {
                        if (meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS)) {
                            meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
                        } else {
                            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        }
                    }
                    item.setItemMeta(meta);
                    session.touch();
                    render();
                }
            }
        });

        // Slot 23: Custom Model Data
        final Integer cmd = (meta != null && meta.hasCustomModelData()) ? meta.getCustomModelData() : null;
        final String cmdVal = cmd != null ? String.valueOf(cmd) : plugin.getLang().getMessageOrDefault("GENERAL.none", "None");
        set(23, new BaseButton(plugin, ItemBuilder.of(Material.COMMAND_BLOCK)
                .name(plugin.getLang().getMessageOrDefault("GUI.item-editor.buttons.custom-model-data.name", "&eCustom Model Data: &f%data%")
                        .replace("%data%", cmdVal), plugin.getLang())
                .lore(plugin.getLang().getMessageListOrDefault("GUI.item-editor.buttons.custom-model-data.lore",
                        List.of("&aClick to enter custom model data number")), plugin.getLang())
                .build()) {
            @Override
            public void onClick(final Player player) {
                promptCustomModelData(player);
            }
        });

        // Slot 24: Leather Armor Color (for leather items)
        if (meta instanceof LeatherArmorMeta leatherMeta) {
            final List<String> leatherLore = new ArrayList<>();
            for (final String line : plugin.getLang().getMessageListOrDefault("GUI.item-editor.buttons.leather-color.lore",
                    List.of(
                            "&7RGB: &f%r%, %g%, %b%",
                            "",
                            "&aClick to choose color"
                    ))) {
                leatherLore.add(line
                        .replace("%r%", String.valueOf(leatherMeta.getColor().getRed()))
                        .replace("%g%", String.valueOf(leatherMeta.getColor().getGreen()))
                        .replace("%b%", String.valueOf(leatherMeta.getColor().getBlue())));
            }

            set(24, new BaseButton(plugin, ItemBuilder.of(Material.CYAN_DYE)
                    .name(plugin.getLang().getMessageOrDefault("GUI.item-editor.buttons.leather-color.name", "&e&lLeather Armor Color"), plugin.getLang())
                    .lore(leatherLore, plugin.getLang())
                    .build()) {
                @Override
                public void onClick(final Player player) {
                    promptLeatherColor(player);
                }
            });
        }

        // Slot 25: Remove / Clear Item
        set(25, new BaseButton(plugin, ItemBuilder.of(Material.LAVA_BUCKET)
                .name(plugin.getLang().getMessageOrDefault("GUI.item-editor.buttons.remove-item.name", "&c&lRemove Item"), plugin.getLang())
                .lore(plugin.getLang().getMessageListOrDefault("GUI.item-editor.buttons.remove-item.lore",
                        List.of("&7Click to delete this item from the kit.")), plugin.getLang())
                .build()) {
            @Override
            public void onClick(final Player player) {
                if (isArmor) {
                    session.getDraftKit().getArmor().remove(targetSlot);
                } else if (isOffHand) {
                    session.getDraftKit().setOffHand(null);
                } else {
                    session.getDraftKit().getItems().remove(targetSlot);
                }
                session.touch();
                CustomKitEditorGui.open(plugin, player, session);
            }
        });

        // Bottom Navigation Bar (slots 45-53)
        final ItemStack filler = Items.GRAY_PANE.clone();
        for (int s = 45; s < 54; s++) {
            inventory.setItem(s, filler);
        }

        // Slot 49: Back to Layout Editor
        set(49, new BaseButton(plugin, ItemBuilder.of(Material.BARRIER)
                .name(plugin.getLang().getMessageOrDefault("GUI.item-editor.buttons.back.name", "&c&lBack to Layout Editor"), plugin.getLang())
                .lore(plugin.getLang().getMessageListOrDefault("GUI.item-editor.buttons.back.lore",
                        List.of("&7Click to return to the kit layout editor.")), plugin.getLang())
                .build()) {
            @Override
            public void onClick(final Player player) {
                CustomKitEditorGui.open(plugin, player, session);
            }
        });
    }

    private void promptCustomModelData(final Player player) {
        final ChatInputManager inputManager = new ChatInputManager(plugin);
        inputManager.prompt(
                player,
                plugin.getLang().getMessage("GUI.item-editor.enter-model-data"),
                input -> {
                    try {
                        final int val = Integer.parseInt(input.trim());
                        final ItemStack item = getItem();
                        if (item != null) {
                            final ItemMeta meta = item.getItemMeta();
                            if (meta != null) {
                                if (val <= 0) {
                                    meta.setCustomModelData(null);
                                } else {
                                    meta.setCustomModelData(val);
                                }
                                item.setItemMeta(meta);
                                session.touch();
                            }
                        }
                    } catch (NumberFormatException e) {
                        plugin.getLang().sendMessage(player, "ERROR.command.invalid-number");
                    }
                    ItemEditorGui.open(plugin, player, session, targetSlot, isArmor, isOffHand);
                },
                () -> ItemEditorGui.open(plugin, player, session, targetSlot, isArmor, isOffHand)
        );
    }

    private void promptLeatherColor(final Player player) {
        final ChatInputManager inputManager = new ChatInputManager(plugin);
        inputManager.prompt(
                player,
                plugin.getLang().getMessage("GUI.item-editor.enter-rgb-color"),
                input -> {
                    try {
                        final String[] split = input.trim().split(" ");
                        if (split.length >= 3) {
                            final int r = Math.max(0, Math.min(255, Integer.parseInt(split[0])));
                            final int g = Math.max(0, Math.min(255, Integer.parseInt(split[1])));
                            final int b = Math.max(0, Math.min(255, Integer.parseInt(split[2])));

                            final ItemStack item = getItem();
                            if (item != null && item.getItemMeta() instanceof LeatherArmorMeta lm) {
                                lm.setColor(Color.fromRGB(r, g, b));
                                item.setItemMeta(lm);
                                session.touch();
                            }
                        }
                    } catch (Exception e) {
                        plugin.getLang().sendMessage(player, "ERROR.command.invalid-argument", "arg", input);
                    }
                    ItemEditorGui.open(plugin, player, session, targetSlot, isArmor, isOffHand);
                },
                () -> ItemEditorGui.open(plugin, player, session, targetSlot, isArmor, isOffHand)
        );
    }

    private void promptDurabilityDamage(final Player player, final Damageable dmg) {
        final ChatInputManager inputManager = new ChatInputManager(plugin);
        inputManager.prompt(
                player,
                plugin.getLang().getMessage("GUI.item-editor.enter-damage"),
                input -> {
                    try {
                        final int damage = Integer.parseInt(input.trim());
                        dmg.setDamage(Math.max(0, damage));
                        final ItemStack item = getItem();
                        if (item != null) {
                            item.setItemMeta((ItemMeta) dmg);
                            session.touch();
                        }
                    } catch (NumberFormatException e) {
                        plugin.getLang().sendMessage(player, "ERROR.command.invalid-number");
                    }
                    ItemEditorGui.open(plugin, player, session, targetSlot, isArmor, isOffHand);
                },
                () -> ItemEditorGui.open(plugin, player, session, targetSlot, isArmor, isOffHand)
        );
    }

    public static void open(@NotNull final DuelsPlugin plugin,
                            @NotNull final Player player,
                            @NotNull final CustomKitEditSession session,
                            final int targetSlot,
                            final boolean isArmor,
                            final boolean isOffHand) {
        final ItemEditorGui gui = plugin.getGuiListener().addGui(player, new ItemEditorGui(
                plugin, session, targetSlot, isArmor, isOffHand
        ), true);
        gui.open(player);
    }
}
