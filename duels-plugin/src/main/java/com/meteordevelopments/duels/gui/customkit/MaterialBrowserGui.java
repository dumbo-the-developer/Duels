package com.meteordevelopments.duels.gui.customkit;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.core.customkit.config.CustomKitsConfig;
import com.meteordevelopments.duels.core.customkit.session.CustomKitEditSession;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.gui.SinglePageGui;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MaterialBrowserGui extends SinglePageGui<DuelsPlugin> {

    public enum Category {
        WEAPONS(Material.DIAMOND_SWORD, "Weapons"),
        ARMOR(Material.DIAMOND_CHESTPLATE, "Armor"),
        TOOLS(Material.DIAMOND_PICKAXE, "Tools"),
        FOOD(Material.GOLDEN_APPLE, "Food"),
        BLOCKS(Material.OBSIDIAN, "Blocks"),
        COMBAT(Material.ENDER_PEARL, "Combat"),
        POTIONS(Material.POTION, "Potions"),
        UTILITY(Material.WATER_BUCKET, "Utility"),
        MISC(Material.CHEST, "Miscellaneous");

        private final Material icon;
        private final String displayName;

        Category(final Material icon, final String displayName) {
            this.icon = icon;
            this.displayName = displayName;
        }
    }

    private static final Map<Category, List<Material>> CATEGORY_MATERIALS = new EnumMap<>(Category.class);

    static {
        for (final Category cat : Category.values()) {
            CATEGORY_MATERIALS.put(cat, new ArrayList<>());
        }

        for (final Material mat : Material.values()) {
            if (mat.isAir() || !mat.isItem() || mat.isLegacy()) {
                continue;
            }

            final String name = mat.name();

            if (name.endsWith("_SWORD") || name.endsWith("_AXE") && !name.endsWith("_PICKAXE") || name.equals("BOW") || name.equals("CROSSBOW") || name.equals("TRIDENT") || name.equals("MACE")) {
                CATEGORY_MATERIALS.get(Category.WEAPONS).add(mat);
            } else if (name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS") || name.equals("SHIELD") || name.equals("ELYTRA") || name.equals("TURTLE_HELMET") || name.equals("CARVED_PUMPKIN")) {
                CATEGORY_MATERIALS.get(Category.ARMOR).add(mat);
            } else if (name.endsWith("_PICKAXE") || name.endsWith("_SHOVEL") || name.endsWith("_HOE") || name.equals("SHEARS") || name.equals("FISHING_ROD") || name.equals("FLINT_AND_STEEL")) {
                CATEGORY_MATERIALS.get(Category.TOOLS).add(mat);
            } else if (mat.isEdible() || name.contains("APPLE") || name.contains("CARROT") || name.contains("BREAD") || name.contains("STEAK") || name.contains("PORKCHOP") || name.contains("BEEF") || name.contains("CHICKEN") || name.contains("MUTTON") || name.contains("POTATO") || name.contains("MELON") || name.contains("COOKIE") || name.contains("PIE") || name.contains("STEW") || name.contains("SOUP")) {
                CATEGORY_MATERIALS.get(Category.FOOD).add(mat);
            } else if (name.equals("ENDER_PEARL") || name.equals("TOTEM_OF_UNDYING") || name.equals("CHORUS_FRUIT") || name.equals("ARROW") || name.equals("SPECTRAL_ARROW") || name.equals("TIPPED_ARROW") || name.equals("WIND_CHARGE") || name.equals("FIREWORK_ROCKET") || name.equals("FIRE_CHARGE") || name.equals("SNOWBALL") || name.equals("EGG")) {
                CATEGORY_MATERIALS.get(Category.COMBAT).add(mat);
            } else if (name.contains("POTION")) {
                CATEGORY_MATERIALS.get(Category.POTIONS).add(mat);
            } else if (name.contains("BUCKET") || name.contains("BOAT") || name.contains("MINECART") || name.equals("ANVIL") || name.equals("CHIPPED_ANVIL") || name.equals("DAMAGED_ANVIL") || name.equals("CRAFTING_TABLE") || name.equals("FURNACE") || name.equals("CHEST") || name.equals("ENDER_CHEST") || name.equals("RESPAWN_ANCHOR") || name.equals("GLOWSTONE") || name.equals("END_CRYSTAL") || name.equals("COMPASS") || name.equals("CLOCK") || name.equals("LEAD")) {
                CATEGORY_MATERIALS.get(Category.UTILITY).add(mat);
            } else if (mat.isBlock()) {
                CATEGORY_MATERIALS.get(Category.BLOCKS).add(mat);
            } else {
                CATEGORY_MATERIALS.get(Category.MISC).add(mat);
            }
        }
    }

    private final CustomKitEditSession session;
    private final int targetSlot;
    private final boolean isArmor;
    private final boolean isOffHand;
    private final boolean isIconPicker;
    private Category currentCategory = Category.WEAPONS;
    private int currentPage = 0;

    public MaterialBrowserGui(final DuelsPlugin plugin,
                              final CustomKitEditSession session,
                              final int targetSlot,
                              final boolean isArmor,
                              final boolean isOffHand,
                              final boolean isIconPicker) {
        super(plugin, plugin.getLang().getMessage("GUI.material-browser.title"), 6);
        this.session = session;
        this.targetSlot = targetSlot;
        this.isArmor = isArmor;
        this.isOffHand = isOffHand;
        this.isIconPicker = isIconPicker;

        if (isArmor && !isIconPicker) {
            this.currentCategory = Category.ARMOR;
        } else if (isOffHand && !isIconPicker) {
            this.currentCategory = Category.COMBAT;
        }

        render();
    }

    public MaterialBrowserGui(final DuelsPlugin plugin,
                              final CustomKitEditSession session,
                              final int targetSlot,
                              final boolean isArmor,
                              final boolean isOffHand) {
        this(plugin, session, targetSlot, isArmor, isOffHand, false);
    }

    private void render() {
        inventory.clear();

        // Row 0 (slots 0-8): Category Tabs
        if (isArmor && !isIconPicker) {
            final String slotName = com.meteordevelopments.duels.core.customkit.validation.CustomKitValidator.getArmorSlotName(targetSlot);
            final ItemStack filler = Items.GRAY_PANE.clone();
            for (int s = 0; s < 9; s++) {
                inventory.setItem(s, filler);
            }
            set(4, new BaseButton(plugin, ItemBuilder.of(Material.ARMOR_STAND)
                    .name("&e&lArmor Slot: &b" + slotName, plugin.getLang())
                    .lore(plugin.getLang(), "&7Select valid armor for this slot below.")
                    .build()) {
                @Override
                public void onClick(final Player player) {
                }
            });
        } else {
            final Category[] categories = Category.values();
            for (int i = 0; i < categories.length && i < 9; i++) {
                final Category cat = categories[i];
                final boolean selected = (cat == currentCategory);

                final ItemBuilder builder = ItemBuilder.of(cat.icon)
                        .name((selected ? "&a&l" : "&7") + cat.displayName, plugin.getLang())
                        .lore(plugin.getLang(), selected ? "&a▶ Currently viewing" : "&eClick to view category");

                set(i, new BaseButton(plugin, builder.build()) {
                    @Override
                    public void onClick(final Player player) {
                        currentCategory = cat;
                        currentPage = 0;
                        render();
                    }
                });
            }
        }

        // Middle Rows (slots 9-44): Material Items
        final CustomKitsConfig config = plugin.getCustomKitManager().getCustomKitsConfig();
        final List<Material> list = CATEGORY_MATERIALS.getOrDefault(currentCategory, Collections.emptyList());
        final List<Material> filtered = new ArrayList<>();

        for (final Material m : list) {
            if (isArmor && !isIconPicker && !com.meteordevelopments.duels.core.customkit.validation.CustomKitValidator.isValidArmorForSlot(m, targetSlot)) {
                continue;
            }

            final String name = m.name();
            if (config.getMaterialMode() == CustomKitsConfig.MaterialMode.BLOCKLIST) {
                if (!config.getBlockedMaterials().contains(name)) {
                    filtered.add(m);
                }
            } else {
                if (config.getAllowedMaterials().contains(name)) {
                    filtered.add(m);
                }
            }
        }

        final int pageSize = 36; // slots 9 to 44
        final int totalPages = Math.max(1, (int) Math.ceil((double) filtered.size() / pageSize));
        currentPage = Math.max(0, Math.min(currentPage, totalPages - 1));

        final int startIndex = currentPage * pageSize;
        final int endIndex = Math.min(startIndex + pageSize, filtered.size());

        for (int i = startIndex; i < endIndex; i++) {
            final Material mat = filtered.get(i);
            final int guiSlot = 9 + (i - startIndex);

            final ItemStack stack = new ItemStack(mat);
            final String matTitle = "&f" + formatMaterialName(mat.name());

            final BaseButton itemBtn = new BaseButton(plugin, ItemBuilder.of(stack)
                    .name(matTitle, plugin.getLang())
                    .lore(plugin.getLang(), isIconPicker ? "&aClick to select as kit icon" : "&aClick to select this item")
                    .build()) {
                @Override
                public void onClick(final Player player) {
                    if (isIconPicker) {
                        session.getDraftKit().setIcon(new ItemStack(mat, 1));
                        session.touch();
                        plugin.getLang().sendMessage(player, "COMMAND.customkits.icon-changed", "material", mat.name());
                        CustomKitEditorGui.open(plugin, player, session);
                        return;
                    }

                    final ItemStack selectedItem = new ItemStack(mat, 1);

                    if (isArmor) {
                        session.getDraftKit().getArmor().put(targetSlot, selectedItem);
                    } else if (isOffHand) {
                        session.getDraftKit().setOffHand(selectedItem);
                    } else {
                        session.getDraftKit().getItems().put(targetSlot, selectedItem);
                    }

                    session.touch();
                    session.setActiveSlot(targetSlot);
                    session.setArmorSlot(isArmor);
                    session.setOffHandSlot(isOffHand);

                    // Open Item Editor GUI for the selected item
                    ItemEditorGui.open(plugin, player, session, targetSlot, isArmor, isOffHand);
                }
            };

            set(guiSlot, itemBtn);
        }

        // Bottom Row (slots 45-53): Navigation
        final ItemStack filler = Items.GRAY_PANE.clone();
        for (int s = 45; s < 54; s++) {
            inventory.setItem(s, filler);
        }

        // Previous Page
        if (currentPage > 0) {
            set(45, new BaseButton(plugin, ItemBuilder.of(Material.ARROW)
                    .name("&ePrevious Page (" + currentPage + "/" + totalPages + ")", plugin.getLang()).build()) {
                @Override
                public void onClick(final Player player) {
                    if (currentPage > 0) {
                        currentPage--;
                        render();
                    }
                }
            });
        }

        // Back to Layout Editor
        set(49, new BaseButton(plugin, ItemBuilder.of(Material.BARRIER)
                .name("&c&lBack to Layout Editor", plugin.getLang())
                .lore(plugin.getLang(), "&7Click to return to inventory editor.")
                .build()) {
            @Override
            public void onClick(final Player player) {
                CustomKitEditorGui.open(plugin, player, session);
            }
        });

        // Next Page
        if (currentPage < totalPages - 1) {
            set(53, new BaseButton(plugin, ItemBuilder.of(Material.ARROW)
                    .name("&eNext Page (" + (currentPage + 2) + "/" + totalPages + ")", plugin.getLang()).build()) {
                @Override
                public void onClick(final Player player) {
                    if (currentPage < totalPages - 1) {
                        currentPage++;
                        render();
                    }
                }
            });
        }
    }

    private String formatMaterialName(final String name) {
        final StringBuilder sb = new StringBuilder();
        for (final String word : name.toLowerCase().split("_")) {
            if (word.isEmpty()) continue;
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    public static void open(@NotNull final DuelsPlugin plugin,
                            @NotNull final Player player,
                            @NotNull final CustomKitEditSession session,
                            final int targetSlot,
                            final boolean isArmor,
                            final boolean isOffHand) {
        final MaterialBrowserGui gui = plugin.getGuiListener().addGui(player, new MaterialBrowserGui(
                plugin, session, targetSlot, isArmor, isOffHand, false
        ), true);
        gui.open(player);
    }

    public static void openIconPicker(@NotNull final DuelsPlugin plugin,
                                      @NotNull final Player player,
                                      @NotNull final CustomKitEditSession session) {
        final MaterialBrowserGui gui = plugin.getGuiListener().addGui(player, new MaterialBrowserGui(
                plugin, session, -1, false, false, true
        ), true);
        gui.open(player);
    }
}
