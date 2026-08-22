package com.meteordevelopments.duels.gui.customkit;

import com.google.common.collect.Multimap;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.core.customkit.config.CustomKitsConfig;
import com.meteordevelopments.duels.core.customkit.session.CustomKitEditSession;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.gui.SinglePageGui;
import com.meteordevelopments.duels.util.input.ChatInputManager;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AttributeEditorGui extends SinglePageGui<DuelsPlugin> {

    private final CustomKitEditSession session;
    private final int targetSlot;
    private final boolean isArmor;
    private final boolean isOffHand;

    // Steps for adding modifier: 0 = viewing list, 1 = selecting attribute, 2 = selecting slot, 3 = selecting operation
    private int step = 0;
    private Attribute selectedAttribute = null;
    private EquipmentSlot selectedSlot = EquipmentSlot.HAND;

    public AttributeEditorGui(final DuelsPlugin plugin,
                              final CustomKitEditSession session,
                              final int targetSlot,
                              final boolean isArmor,
                              final boolean isOffHand) {
        super(plugin, plugin.getLang().getMessage("GUI.attribute-editor.title"), 6);
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
            ItemEditorGui.open(plugin, (Player) inventory.getViewers().iterator().next(), session, targetSlot, isArmor, isOffHand);
            return;
        }

        final CustomKitsConfig config = plugin.getCustomKitManager().getCustomKitsConfig();

        // Top Info Item (slot 4)
        set(4, new BaseButton(plugin, item.clone()) {
            @Override
            public void onClick(final Player player) {
            }
        });

        if (step == 0) {
            renderCurrentModifiers(item, config);
        } else if (step == 1) {
            renderSelectAttribute();
        } else if (step == 2) {
            renderSelectSlot();
        } else if (step == 3) {
            renderSelectOperation();
        }

        // Bottom navigation bar
        final ItemStack filler = Items.GRAY_PANE.clone();
        for (int s = 45; s < 54; s++) {
            inventory.setItem(s, filler);
        }

        // Back Button (slot 49)
        set(49, new BaseButton(plugin, ItemBuilder.of(Material.BARRIER)
                .name(step > 0 ? "&c&lBack" : "&c&lBack to Item Editor", plugin.getLang())
                .lore(plugin.getLang(), "&7Click to go back.")
                .build()) {
            @Override
            public void onClick(final Player player) {
                if (step > 0) {
                    step--;
                    render();
                } else {
                    ItemEditorGui.open(plugin, player, session, targetSlot, isArmor, isOffHand);
                }
            }
        });
    }

    private void renderCurrentModifiers(final ItemStack item, final CustomKitsConfig config) {
        final ItemMeta meta = item.getItemMeta();
        final Multimap<Attribute, AttributeModifier> modifiers = (meta != null && meta.hasAttributeModifiers())
                ? meta.getAttributeModifiers() : null;

        // Add Modifier Button (slot 0)
        set(0, new BaseButton(plugin, ItemBuilder.of(Material.NETHER_STAR)
                .name("&a&l+ Add Attribute Modifier", plugin.getLang())
                .lore(plugin.getLang(), "&7Click to configure and add a modifier.")
                .build()) {
            @Override
            public void onClick(final Player player) {
                final CustomKitsConfig customKitsConfig = plugin.getCustomKitManager().getCustomKitsConfig();
                if (modifiers != null && modifiers.size() >= customKitsConfig.getMaxModifiersPerItem()) {
                    plugin.getLang().sendMessage(player, "ERROR.customkits.too-many-attributes", "max", customKitsConfig.getMaxModifiersPerItem());
                    return;
                }
                step = 1;
                render();
            }
        });

        // Clear All Modifiers (slot 8)
        if (modifiers != null && !modifiers.isEmpty()) {
            set(8, new BaseButton(plugin, ItemBuilder.of(Material.LAVA_BUCKET)
                    .name("&c&lClear All Modifiers", plugin.getLang())
                    .lore(plugin.getLang(), "&7Click to remove all attribute modifiers.")
                    .build()) {
                @Override
                public void onClick(final Player player) {
                    if (meta != null) {
                        for (final Attribute attr : Attribute.values()) {
                            meta.removeAttributeModifier(attr);
                        }
                        item.setItemMeta(meta);
                        session.touch();
                        render();
                    }
                }
            });
        }

        if (modifiers != null) {
            int slot = 10;
            for (final Map.Entry<Attribute, AttributeModifier> entry : modifiers.entries()) {
                if (slot > 43) break;
                if (slot % 9 == 0) slot++;
                if (slot % 9 == 8) slot += 2;

                final Attribute attr = entry.getKey();
                final AttributeModifier mod = entry.getValue();

                final BaseButton modBtn = new BaseButton(plugin, ItemBuilder.of(Material.IRON_SWORD)
                        .name("&e" + formatName(attr.name()), plugin.getLang())
                        .lore(plugin.getLang(),
                                "&7Amount: &f" + mod.getAmount(),
                                "&7Operation: &f" + mod.getOperation().name(),
                                "&7Slot: &f" + (mod.getSlot() != null ? mod.getSlot().name() : "ANY"),
                                "",
                                "&c[Click to remove]"
                        ).build()) {
                    @Override
                    public void onClick(final Player player) {
                        meta.removeAttributeModifier(attr, mod);
                        item.setItemMeta(meta);
                        session.touch();
                        render();
                    }
                };

                set(slot, modBtn);
                slot++;
            }
        }
    }

    private void renderSelectAttribute() {
        final Attribute[] attributes = {
                Attribute.GENERIC_ATTACK_DAMAGE,
                Attribute.GENERIC_ATTACK_SPEED,
                Attribute.GENERIC_ARMOR,
                Attribute.GENERIC_ARMOR_TOUGHNESS,
                Attribute.GENERIC_KNOCKBACK_RESISTANCE,
                Attribute.GENERIC_MOVEMENT_SPEED,
                Attribute.GENERIC_MAX_HEALTH,
                Attribute.GENERIC_LUCK
        };

        int slot = 10;
        for (final Attribute attr : attributes) {
            final BaseButton btn = new BaseButton(plugin, ItemBuilder.of(Material.PAPER)
                    .name("&b" + formatName(attr.name()), plugin.getLang())
                    .lore(plugin.getLang(), "&aClick to select attribute")
                    .build()) {
                @Override
                public void onClick(final Player player) {
                    selectedAttribute = attr;
                    step = 2;
                    render();
                }
            };
            set(slot, btn);
            slot++;
            if (slot == 17) slot = 19;
        }
    }

    private void renderSelectSlot() {
        final EquipmentSlot[] slots = EquipmentSlot.values();

        int slot = 11;
        for (final EquipmentSlot s : slots) {
            final BaseButton btn = new BaseButton(plugin, ItemBuilder.of(Material.ARMOR_STAND)
                    .name("&eSlot: " + s.name(), plugin.getLang())
                    .lore(plugin.getLang(), "&aClick to select equipment slot")
                    .build()) {
                @Override
                public void onClick(final Player player) {
                    selectedSlot = s;
                    step = 3;
                    render();
                }
            };
            set(slot, btn);
            slot += 2;
            if (slot >= 35) break;
        }
    }

    private void renderSelectOperation() {
        final AttributeModifier.Operation[] ops = AttributeModifier.Operation.values();

        int slot = 11;
        for (final AttributeModifier.Operation op : ops) {
            final BaseButton btn = new BaseButton(plugin, ItemBuilder.of(Material.REPEATER)
                    .name("&aOperation: " + op.name(), plugin.getLang())
                    .lore(plugin.getLang(), "&7Click to proceed to enter modifier amount.")
                    .build()) {
                @Override
                public void onClick(final Player player) {
                    promptAmount(player, op);
                }
            };
            set(slot, btn);
            slot += 3;
        }
    }

    private void promptAmount(final Player player, final AttributeModifier.Operation op) {
        final CustomKitsConfig config = plugin.getCustomKitManager().getCustomKitsConfig();
        final CustomKitsConfig.AttributeLimit limit = config.getAttributeLimits().get(selectedAttribute.name());
        final String rangeInfo = (limit != null) ? " (Range: " + limit.getMin() + " to " + limit.getMax() + ")" : "";

        final ChatInputManager inputManager = new ChatInputManager(plugin);
        inputManager.prompt(
                player,
                plugin.getLang().getMessage("GUI.attribute-editor.enter-amount") + rangeInfo,
                input -> {
                    try {
                        final double amount = Double.parseDouble(input.trim());

                        if (limit != null && (amount < limit.getMin() || amount > limit.getMax())) {
                            plugin.getLang().sendMessage(player, "ERROR.customkits.attribute-amount-invalid",
                                    "attribute", selectedAttribute.name(), "min", limit.getMin(), "max", limit.getMax());
                            AttributeEditorGui.open(plugin, player, session, targetSlot, isArmor, isOffHand);
                            return;
                        }

                        final ItemStack item = getItem();
                        if (item != null) {
                            final ItemMeta meta = item.getItemMeta();
                            if (meta != null) {
                                final AttributeModifier modifier = new AttributeModifier(
                                        UUID.randomUUID(),
                                        "duels." + selectedAttribute.name().toLowerCase(),
                                        amount,
                                        op,
                                        selectedSlot
                                );
                                meta.addAttributeModifier(selectedAttribute, modifier);
                                item.setItemMeta(meta);
                                session.touch();
                            }
                        }
                    } catch (NumberFormatException e) {
                        plugin.getLang().sendMessage(player, "ERROR.command.invalid-number");
                    }

                    step = 0;
                    AttributeEditorGui.open(plugin, player, session, targetSlot, isArmor, isOffHand);
                },
                () -> AttributeEditorGui.open(plugin, player, session, targetSlot, isArmor, isOffHand)
        );
    }

    private String formatName(final String name) {
        final StringBuilder sb = new StringBuilder();
        for (final String s : name.replace("GENERIC_", "").toLowerCase().split("_")) {
            if (s.isEmpty()) continue;
            sb.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    public static void open(@NotNull final DuelsPlugin plugin,
                            @NotNull final Player player,
                            @NotNull final CustomKitEditSession session,
                            final int targetSlot,
                            final boolean isArmor,
                            final boolean isOffHand) {
        final AttributeEditorGui gui = plugin.getGuiListener().addGui(player, new AttributeEditorGui(
                plugin, session, targetSlot, isArmor, isOffHand
        ), true);
        gui.open(player);
    }
}
