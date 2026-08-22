package com.meteordevelopments.duels.gui.customkit;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.core.customkit.session.CustomKitEditSession;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.gui.SinglePageGui;
import com.meteordevelopments.duels.util.input.ChatInputManager;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PotionEditorGui extends SinglePageGui<DuelsPlugin> {

    private final CustomKitEditSession session;
    private final int targetSlot;
    private final boolean isArmor;
    private final boolean isOffHand;
    private boolean browsingEffects = false;
    private int effectsPage = 0;
    private PotionEffectType selectedEffectType = null;

    public PotionEditorGui(final DuelsPlugin plugin,
                           final CustomKitEditSession session,
                           final int targetSlot,
                           final boolean isArmor,
                           final boolean isOffHand) {
        super(plugin, plugin.getLang().getMessage("GUI.potion-editor.title"), 6);
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

        if (item == null || !(item.getItemMeta() instanceof PotionMeta)) {
            if (!inventory.getViewers().isEmpty()) {
                ItemEditorGui.open(plugin, (Player) inventory.getViewers().iterator().next(), session, targetSlot, isArmor, isOffHand);
            }
            return;
        }

        final PotionMeta meta = (PotionMeta) item.getItemMeta();

        // Top Info Item (slot 4)
        set(4, new BaseButton(plugin, item.clone()) {
            @Override
            public void onClick(final Player player) {
            }
        });

        // Initialize bottom navigation bar with fillers (slots 45-53) FIRST
        final ItemStack filler = Items.GRAY_PANE.clone();
        for (int s = 45; s < 54; s++) {
            inventory.setItem(s, filler);
        }

        // Back Button (slot 49)
        set(49, new BaseButton(plugin, ItemBuilder.of(Material.BARRIER)
                .name(browsingEffects ? "&c&lBack to Potion Settings" : "&c&lBack to Item Editor", plugin.getLang())
                .lore(plugin.getLang(), "&7Click to go back.")
                .build()) {
            @Override
            public void onClick(final Player player) {
                if (browsingEffects) {
                    browsingEffects = false;
                    effectsPage = 0;
                    render();
                } else {
                    ItemEditorGui.open(plugin, player, session, targetSlot, isArmor, isOffHand);
                }
            }
        });

        if (browsingEffects) {
            renderBrowseEffects();
        } else {
            renderPotionSettings(meta);
        }
    }

    private void renderPotionSettings(final PotionMeta meta) {
        final PotionData data = (meta != null && meta.getBasePotionData() != null)
                ? meta.getBasePotionData()
                : new PotionData(PotionType.WATER, false, false);

        // Extended Toggle (slot 1)
        final boolean extended = data.isExtended();
        set(1, new BaseButton(plugin, ItemBuilder.of(extended ? Material.LIME_DYE : Material.GRAY_DYE)
                .name("&eExtended Duration: " + (extended ? "&aEnabled" : "&7Disabled"), plugin.getLang())
                .lore(plugin.getLang(), "&aClick to toggle")
                .build()) {
            @Override
            public void onClick(final Player player) {
                final ItemStack item = getItem();
                if (item != null && item.getItemMeta() instanceof PotionMeta m) {
                    try {
                        if (!extended) {
                            if (data.getType().isExtendable()) {
                                m.setBasePotionData(new PotionData(data.getType(), true, false));
                            } else {
                                plugin.getLang().sendMessage(player, "ERROR.customkits.potion-not-extendable");
                                return;
                            }
                        } else {
                            m.setBasePotionData(new PotionData(data.getType(), false, false));
                        }
                        item.setItemMeta(m);
                        session.touch();
                        render();
                    } catch (Exception ignored) {
                    }
                }
            }
        });

        // Upgraded (Strong / Tier II) Toggle (slot 7)
        final boolean upgraded = data.isUpgraded();
        set(7, new BaseButton(plugin, ItemBuilder.of(upgraded ? Material.LIME_DYE : Material.GRAY_DYE)
                .name("&eUpgraded (Tier II): " + (upgraded ? "&aEnabled" : "&7Disabled"), plugin.getLang())
                .lore(plugin.getLang(), "&aClick to toggle")
                .build()) {
            @Override
            public void onClick(final Player player) {
                final ItemStack item = getItem();
                if (item != null && item.getItemMeta() instanceof PotionMeta m) {
                    try {
                        if (!upgraded) {
                            if (data.getType().isUpgradeable()) {
                                m.setBasePotionData(new PotionData(data.getType(), false, true));
                            } else {
                                plugin.getLang().sendMessage(player, "ERROR.customkits.potion-not-upgradeable");
                                return;
                            }
                        } else {
                            m.setBasePotionData(new PotionData(data.getType(), false, false));
                        }
                        item.setItemMeta(m);
                        session.touch();
                        render();
                    } catch (Exception ignored) {
                    }
                }
            }
        });

        // Base Potion Type selector buttons (slots 10-25)
        final PotionType[] types = {
                PotionType.REGEN, PotionType.SPEED, PotionType.STRENGTH, PotionType.INSTANT_HEAL,
                PotionType.INSTANT_DAMAGE, PotionType.FIRE_RESISTANCE, PotionType.WATER_BREATHING,
                PotionType.INVISIBILITY, PotionType.NIGHT_VISION, PotionType.WEAKNESS,
                PotionType.SLOWNESS, PotionType.POISON, PotionType.JUMP, PotionType.SLOW_FALLING,
                PotionType.TURTLE_MASTER, PotionType.WATER
        };

        int slot = 10;
        for (final PotionType type : types) {
            if (slot % 9 == 0) slot++;
            if (slot % 9 == 8) slot += 2;
            if (slot > 25) break;

            final boolean selected = (data.getType() == type);
            final BaseButton btn = new BaseButton(plugin, ItemBuilder.of(Material.POTION)
                    .name((selected ? "&a&l" : "&b") + formatName(type.name()), plugin.getLang())
                    .lore(plugin.getLang(), selected ? "&a▶ Currently selected" : "&eClick to select")
                    .build()) {
                @Override
                public void onClick(final Player player) {
                    final ItemStack item = getItem();
                    if (item != null && item.getItemMeta() instanceof PotionMeta m) {
                        try {
                            m.setBasePotionData(new PotionData(type, false, false));
                            item.setItemMeta(m);
                            session.touch();
                            render();
                        } catch (Exception ignored) {
                        }
                    }
                }
            };

            set(slot, btn);
            slot++;
        }

        // Custom Effects Section (slots 28-43)
        set(28, new BaseButton(plugin, ItemBuilder.of(Material.BREWING_STAND)
                .name("&a&l+ Add Custom Potion Effect", plugin.getLang())
                .lore(plugin.getLang(), "&7Click to add a custom potion effect.")
                .build()) {
            @Override
            public void onClick(final Player player) {
                browsingEffects = true;
                effectsPage = 0;
                render();
            }
        });

        if (meta.hasCustomEffects()) {
            set(35, new BaseButton(plugin, ItemBuilder.of(Material.LAVA_BUCKET)
                    .name("&c&lClear Custom Effects", plugin.getLang())
                    .lore(plugin.getLang(), "&7Click to remove all custom potion effects.")
                    .build()) {
                @Override
                public void onClick(final Player player) {
                    meta.clearCustomEffects();
                    final ItemStack item = getItem();
                    if (item != null) {
                        item.setItemMeta(meta);
                        session.touch();
                        render();
                    }
                }
            });
        }

        int effSlot = 29;
        for (final PotionEffect effect : meta.getCustomEffects()) {
            if (effSlot >= 35) break;

            final BaseButton effBtn = new BaseButton(plugin, ItemBuilder.of(Material.POTION)
                    .name("&e" + formatName(effect.getType().getName()), plugin.getLang())
                    .lore(plugin.getLang(),
                            "&7Duration: &f" + (effect.getDuration() / 20) + "s",
                            "&7Amplifier: &f" + (effect.getAmplifier() + 1),
                            "",
                            "&c[Click to remove]"
                    ).build()) {
                @Override
                public void onClick(final Player player) {
                    meta.removeCustomEffect(effect.getType());
                    final ItemStack item = getItem();
                    if (item != null) {
                        item.setItemMeta(meta);
                        session.touch();
                        render();
                    }
                }
            };

            set(effSlot, effBtn);
            effSlot++;
        }
    }

    private void renderBrowseEffects() {
        final List<PotionEffectType> effectTypes = new ArrayList<>();
        for (final PotionEffectType t : PotionEffectType.values()) {
            if (t != null) effectTypes.add(t);
        }

        final int pageSize = 28;
        final int totalPages = Math.max(1, (int) Math.ceil((double) effectTypes.size() / pageSize));
        effectsPage = Math.max(0, Math.min(effectsPage, totalPages - 1));

        final int startIndex = effectsPage * pageSize;
        final int endIndex = Math.min(startIndex + pageSize, effectTypes.size());

        int slot = 10;
        for (int i = startIndex; i < endIndex; i++) {
            final PotionEffectType type = effectTypes.get(i);
            if (slot > 43) break;
            if (slot % 9 == 0) slot++;
            if (slot % 9 == 8) slot += 2;

            final BaseButton btn = new BaseButton(plugin, ItemBuilder.of(Material.POTION)
                    .name("&b" + formatName(type.getName()), plugin.getLang())
                    .lore(plugin.getLang(), "&aClick to set duration and amplifier")
                    .build()) {
                @Override
                public void onClick(final Player player) {
                    selectedEffectType = type;
                    promptEffectDetails(player);
                }
            };

            set(slot, btn);
            slot++;
        }

        if (effectsPage > 0) {
            set(45, new BaseButton(plugin, ItemBuilder.of(Material.ARROW)
                    .name("&ePrevious Page (" + effectsPage + "/" + totalPages + ")", plugin.getLang()).build()) {
                @Override
                public void onClick(final Player player) {
                    if (effectsPage > 0) {
                        effectsPage--;
                        render();
                    }
                }
            });
        }

        if (effectsPage < totalPages - 1) {
            set(53, new BaseButton(plugin, ItemBuilder.of(Material.ARROW)
                    .name("&eNext Page (" + (effectsPage + 2) + "/" + totalPages + ")", plugin.getLang()).build()) {
                @Override
                public void onClick(final Player player) {
                    if (effectsPage < totalPages - 1) {
                        effectsPage++;
                        render();
                    }
                }
            });
        }
    }

    private void promptEffectDetails(final Player player) {
        final ChatInputManager inputManager = new ChatInputManager(plugin);
        inputManager.prompt(
                player,
                plugin.getLang().getMessage("GUI.potion-editor.enter-duration-amplifier"),
                input -> {
                    try {
                        final String[] split = input.trim().split("\\s+");
                        final int durationSec = Integer.parseInt(split[0]);
                        final int amp = split.length > 1 ? Integer.parseInt(split[1]) : 1;

                        final ItemStack item = getItem();
                        if (item != null && item.getItemMeta() instanceof PotionMeta meta) {
                            meta.addCustomEffect(new PotionEffect(selectedEffectType, durationSec * 20, Math.max(0, amp - 1)), true);
                            item.setItemMeta(meta);
                            session.touch();
                        }
                    } catch (Exception e) {
                        plugin.getLang().sendMessage(player, "ERROR.command.invalid-argument", "arg", input);
                    }

                    browsingEffects = false;
                    PotionEditorGui.open(plugin, player, session, targetSlot, isArmor, isOffHand);
                },
                () -> {
                    browsingEffects = false;
                    PotionEditorGui.open(plugin, player, session, targetSlot, isArmor, isOffHand);
                }
        );
    }

    private String formatName(final String name) {
        final StringBuilder sb = new StringBuilder();
        for (final String s : name.toLowerCase().split("_")) {
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
        final PotionEditorGui gui = plugin.getGuiListener().addGui(player, new PotionEditorGui(
                plugin, session, targetSlot, isArmor, isOffHand
        ), true);
        gui.open(player);
    }
}
