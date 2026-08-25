package com.meteordevelopments.duels.gui.bedrock;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.api.customkit.CustomKit;
import com.meteordevelopments.duels.api.customkit.CustomKitSnapshot;
import com.meteordevelopments.duels.core.customkit.CustomKitImpl;
import com.meteordevelopments.duels.core.customkit.config.CustomKitsConfig;
import com.meteordevelopments.duels.core.customkit.session.CustomKitEditSession;
import com.meteordevelopments.duels.core.customkit.validation.CustomKitValidator;
import com.meteordevelopments.duels.gui.customkit.MaterialBrowserGui;
import com.meteordevelopments.duels.setting.Settings;
import com.meteordevelopments.duels.util.FloodgateUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.ModalForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.*;

public final class BedrockCustomKitForm {

    private BedrockCustomKitForm() {
    }

    public static void openMainMenu(final DuelsPlugin plugin, final Player player) {
        if (!FloodgateUtil.isBedrockPlayer(player)) {
            return;
        }

        final List<CustomKit> playerKits = plugin.getCustomKitManager().getKits(player.getUniqueId());
        final int maxKits = plugin.getCustomKitManager().getMaxKits(player);
        final String limitStr = maxKits == Integer.MAX_VALUE ? "Unlimited" : String.valueOf(maxKits);
        final boolean reached = plugin.getCustomKitManager().hasReachedLimit(player);

        final SimpleForm.Builder builder = SimpleForm.builder()
                .title("My Custom Kits")
                .content("§7Manage your custom duel kits.\n§eKits: §a" + playerKits.size() + " §7/ §e" + limitStr + "\n");

        if (!reached) {
            builder.button("§a+ Create New Kit");
        }

        for (final CustomKit kit : playerKits) {
            builder.button("§b" + kit.getName() + "\n§7(" + (kit.getItems().size() + kit.getArmor().size() + (kit.getOffHand() != null ? 1 : 0)) + " items)");
        }

        builder.validResultHandler(response -> {
            final int id = response.clickedButtonId();
            if (!reached && id == 0) {
                // Create New Kit
                openCreateKitPrompt(plugin, player);
            } else {
                final int kitIndex = (!reached) ? id - 1 : id;
                if (kitIndex >= 0 && kitIndex < playerKits.size()) {
                    final CustomKit selected = playerKits.get(kitIndex);
                    openKitActions(plugin, player, (CustomKitImpl) selected);
                }
            }
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }

    private static void openCreateKitPrompt(final DuelsPlugin plugin, final Player player) {
        final CustomForm form = CustomForm.builder()
                .title("Create Custom Kit")
                .input("Kit Name", "Enter name (e.g. Sword PvP)")
                .validResultHandler(response -> {
                    final String name = response.asInput(0);
                    DuelsPlugin.getFoliaLib().getScheduler().runAtEntity(player, task -> {
                        final CustomKitImpl newKit = plugin.getCustomKitManager().createKit(player, name != null ? name.trim() : "");
                        if (newKit != null) {
                            plugin.getLang().sendMessage(player, "COMMAND.customkits.created", "kit", newKit.getName());
                            final CustomKitEditSession session = plugin.getCustomKitManager().startSession(player, newKit, true);
                            openLayoutEditor(plugin, player, session);
                        } else {
                            openMainMenu(plugin, player);
                        }
                    });
                })
                .closedOrInvalidResultHandler(() -> openMainMenu(plugin, player))
                .build();

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), form);
    }

    public static void openKitActions(final DuelsPlugin plugin, final Player player, final CustomKitImpl kit) {
        final SimpleForm form = SimpleForm.builder()
                .title("Kit: " + kit.getName())
                .content("§eChoose an action for this kit:")
                .button("§e✏ Edit Inventory Layout")
                .button("§b⚙ Edit Kit Name / Description")
                .button("§a👁 Preview Kit")
                .button("§6❐ Duplicate Kit")
                .button("§c✘ Delete Kit")
                .button("§7« Back to Kits")
                .validResultHandler(response -> {
                    final int id = response.clickedButtonId();
                    switch (id) {
                        case 0 -> {
                            final CustomKitEditSession session = plugin.getCustomKitManager().startSession(player, kit, false);
                            openLayoutEditor(plugin, player, session);
                        }
                        case 1 -> openSettingsEditor(plugin, player, kit);
                        case 2 -> openPreview(plugin, player, kit.toSnapshot(), () -> openKitActions(plugin, player, kit));
                        case 3 -> openDuplicatePrompt(plugin, player, kit);
                        case 4 -> openDeleteConfirm(plugin, player, kit);
                        default -> openMainMenu(plugin, player);
                    }
                })
                .closedOrInvalidResultHandler(() -> openMainMenu(plugin, player))
                .build();

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), form);
    }

    public static void openLayoutEditor(final DuelsPlugin plugin, final Player player, final CustomKitEditSession session) {
        final CustomKitImpl draft = session.getDraftKit();
        final SimpleForm.Builder builder = SimpleForm.builder()
                .title("Layout: " + draft.getName())
                .content("§7Tap any slot to configure its item:\n");

        builder.button("§a✔ Save Kit");
        builder.button("§c✘ Discard / Cancel");

        // Armor
        builder.button("§e[Helmet] §f" + formatSlotItem(draft.getArmor().get(0)));
        builder.button("§e[Chestplate] §f" + formatSlotItem(draft.getArmor().get(1)));
        builder.button("§e[Leggings] §f" + formatSlotItem(draft.getArmor().get(2)));
        builder.button("§e[Boots] §f" + formatSlotItem(draft.getArmor().get(3)));

        // Offhand
        builder.button("§6[Offhand] §f" + formatSlotItem(draft.getOffHand()));

        // Hotbar 1-9
        for (int h = 0; h < 9; h++) {
            builder.button("§b[Hotbar " + (h + 1) + "] §f" + formatSlotItem(draft.getItems().get(h)));
        }

        // Main Inventory 1-27
        for (int m = 9; m < 36; m++) {
            builder.button("§7[Inv " + (m - 8) + "] §f" + formatSlotItem(draft.getItems().get(m)));
        }

        builder.validResultHandler(response -> {
            final int id = response.clickedButtonId();
            if (id == 0) {
                // Save Kit
                DuelsPlugin.getFoliaLib().getScheduler().runAtEntity(player, task -> {
                    final boolean success = plugin.getCustomKitManager().saveSession(player);
                    if (success) {
                        plugin.getLang().sendMessage(player, "COMMAND.customkits.saved", "kit", draft.getName());
                    }
                    openMainMenu(plugin, player);
                });
            } else if (id == 1) {
                // Discard
                plugin.getCustomKitManager().discardSession(player);
                openMainMenu(plugin, player);
            } else if (id >= 2 && id <= 5) {
                // Armor 0 to 3
                final int armorIndex = id - 2;
                openSlotOptions(plugin, player, session, armorIndex, true, false);
            } else if (id == 6) {
                // Offhand
                openSlotOptions(plugin, player, session, 0, false, true);
            } else if (id >= 7 && id <= 15) {
                // Hotbar 0-8
                final int hotbarIndex = id - 7;
                openSlotOptions(plugin, player, session, hotbarIndex, false, false);
            } else if (id >= 16 && id <= 42) {
                // Main Inv 9-35
                final int invIndex = (id - 16) + 9;
                openSlotOptions(plugin, player, session, invIndex, false, false);
            }
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }

    private static void openSlotOptions(final DuelsPlugin plugin,
                                       final Player player,
                                       final CustomKitEditSession session,
                                       final int slot,
                                       final boolean isArmor,
                                       final boolean isOffHand) {
        final ItemStack currentItem = isArmor ? session.getDraftKit().getArmor().get(slot)
                : isOffHand ? session.getDraftKit().getOffHand()
                : session.getDraftKit().getItems().get(slot);

        final boolean hasItem = (currentItem != null && currentItem.getType() != Material.AIR);

        final SimpleForm.Builder builder = SimpleForm.builder()
                .title(hasItem ? "Item: " + currentItem.getType().name() : "Empty Slot");

        if (hasItem) {
            builder.button("§e✏ Edit Item Properties");
            builder.button("§b🔄 Change Material");
            builder.button("§c✘ Clear Slot");
            builder.button("§7« Back");
        } else {
            builder.button("§a+ Choose Material");
            builder.button("§7« Back");
        }

        builder.validResultHandler(response -> {
            final int id = response.clickedButtonId();
            if (hasItem) {
                if (id == 0) {
                    openItemProperties(plugin, player, session, slot, isArmor, isOffHand);
                } else if (id == 1) {
                    openCategoryPicker(plugin, player, session, slot, isArmor, isOffHand);
                } else if (id == 2) {
                    if (isArmor) session.getDraftKit().getArmor().remove(slot);
                    else if (isOffHand) session.getDraftKit().setOffHand(null);
                    else session.getDraftKit().getItems().remove(slot);
                    session.touch();
                    openLayoutEditor(plugin, player, session);
                } else {
                    openLayoutEditor(plugin, player, session);
                }
            } else {
                if (id == 0) {
                    openCategoryPicker(plugin, player, session, slot, isArmor, isOffHand);
                } else {
                    openLayoutEditor(plugin, player, session);
                }
            }
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }

    private static void openCategoryPicker(final DuelsPlugin plugin,
                                          final Player player,
                                          final CustomKitEditSession session,
                                          final int slot,
                                          final boolean isArmor,
                                          final boolean isOffHand) {
        if (isArmor) {
            openMaterialPicker(plugin, player, session, slot, true, isOffHand, MaterialBrowserGui.Category.ARMOR);
            return;
        }

        final SimpleForm.Builder builder = SimpleForm.builder().title("Select Category");

        for (final MaterialBrowserGui.Category cat : MaterialBrowserGui.Category.values()) {
            builder.button("§b" + cat.name());
        }

        builder.validResultHandler(response -> {
            final int catIdx = response.clickedButtonId();
            if (catIdx >= 0 && catIdx < MaterialBrowserGui.Category.values().length) {
                final MaterialBrowserGui.Category chosen = MaterialBrowserGui.Category.values()[catIdx];
                openMaterialPicker(plugin, player, session, slot, isArmor, isOffHand, chosen);
            } else {
                openLayoutEditor(plugin, player, session);
            }
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }

    private static void openMaterialPicker(final DuelsPlugin plugin,
                                          final Player player,
                                          final CustomKitEditSession session,
                                          final int slot,
                                          final boolean isArmor,
                                          final boolean isOffHand,
                                          final MaterialBrowserGui.Category category) {
        final CustomKitsConfig config = plugin.getCustomKitManager().getCustomKitsConfig();
        final List<Material> materials = new ArrayList<>();

        for (final Material m : Material.values()) {
            if (m.isAir() || !m.isItem() || m.isLegacy()) continue;
            if (isArmor && !com.meteordevelopments.duels.core.customkit.validation.CustomKitValidator.isValidArmorForSlot(m, slot)) {
                continue;
            }
            final String name = m.name();
            if (config.getMaterialMode() == CustomKitsConfig.MaterialMode.BLOCKLIST && config.getBlockedMaterials().contains(name)) {
                continue;
            }
            materials.add(m);
        }

        final String title = isArmor ? "Pick Armor (" + com.meteordevelopments.duels.core.customkit.validation.CustomKitValidator.getArmorSlotName(slot) + ")" : "Pick Material: " + category.name();
        final SimpleForm.Builder builder = SimpleForm.builder().title(title);

        final List<Material> displayList = materials.subList(0, Math.min(100, materials.size()));
        for (final Material m : displayList) {
            builder.button("§f" + m.name());
        }

        builder.validResultHandler(response -> {
            final int id = response.clickedButtonId();
            if (id >= 0 && id < displayList.size()) {
                final Material selectedMat = displayList.get(id);
                final ItemStack stack = new ItemStack(selectedMat, 1);

                if (isArmor) session.getDraftKit().getArmor().put(slot, stack);
                else if (isOffHand) session.getDraftKit().setOffHand(stack);
                else session.getDraftKit().getItems().put(slot, stack);

                session.touch();
                openItemProperties(plugin, player, session, slot, isArmor, isOffHand);
            } else {
                openLayoutEditor(plugin, player, session);
            }
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), builder.build());
    }

    private static void openItemProperties(final DuelsPlugin plugin,
                                          final Player player,
                                          final CustomKitEditSession session,
                                          final int slot,
                                          final boolean isArmor,
                                          final boolean isOffHand) {
        final ItemStack item = isArmor ? session.getDraftKit().getArmor().get(slot)
                : isOffHand ? session.getDraftKit().getOffHand()
                : session.getDraftKit().getItems().get(slot);

        if (item == null) {
            openLayoutEditor(plugin, player, session);
            return;
        }

        final ItemMeta meta = item.getItemMeta();
        final String displayName = meta != null && meta.hasDisplayName() ? meta.getDisplayName() : "";
        final int amount = item.getAmount();
        final boolean unbreakable = meta != null && meta.isUnbreakable();

        final CustomForm form = CustomForm.builder()
                .title("Edit: " + item.getType().name())
                .input("Display Name", "Display Name", displayName)
                .slider("Amount", 1, 64, 1, amount)
                .toggle("Unbreakable", unbreakable)
                .validResultHandler(response -> {
                    final String newName = response.asInput(0);
                    final int newAmount = (int) response.asSlider(1);
                    final boolean newUnbreakable = response.asToggle(2);

                    item.setAmount(Math.max(1, Math.min(64, newAmount)));
                    if (meta != null) {
                        if (newName != null && !newName.trim().isEmpty()) {
                            meta.setDisplayName(plugin.getLang().toLegacyString(newName.trim()));
                        } else {
                            meta.setDisplayName(null);
                        }
                        meta.setUnbreakable(newUnbreakable);
                        item.setItemMeta(meta);
                    }

                    session.touch();
                    openLayoutEditor(plugin, player, session);
                })
                .closedOrInvalidResultHandler(() -> openLayoutEditor(plugin, player, session))
                .build();

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), form);
    }

    private static void openSettingsEditor(final DuelsPlugin plugin, final Player player, final CustomKitImpl kit) {
        final CustomForm form = CustomForm.builder()
                .title("Settings: " + kit.getName())
                .input("Kit Name", "Kit Name", kit.getName())
                .input("Description Line 1", "Description line 1", kit.getDescription().size() > 0 ? kit.getDescription().get(0) : "")
                .input("Description Line 2", "Description line 2", kit.getDescription().size() > 1 ? kit.getDescription().get(1) : "")
                .validResultHandler(response -> {
                    final String newName = response.asInput(0);
                    final String desc1 = response.asInput(1);
                    final String desc2 = response.asInput(2);

                    if (newName != null && !newName.trim().isEmpty()) {
                        kit.setName(newName.trim());
                    }

                    final List<String> desc = new ArrayList<>();
                    if (desc1 != null && !desc1.trim().isEmpty()) desc.add(desc1.trim());
                    if (desc2 != null && !desc2.trim().isEmpty()) desc.add(desc2.trim());
                    kit.setDescription(desc);

                    plugin.getCustomKitManager().saveKit(kit);
                    openKitActions(plugin, player, kit);
                })
                .closedOrInvalidResultHandler(() -> openKitActions(plugin, player, kit))
                .build();

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), form);
    }

    private static void openDuplicatePrompt(final DuelsPlugin plugin, final Player player, final CustomKitImpl kit) {
        final CustomForm form = CustomForm.builder()
                .title("Duplicate Kit")
                .input("New Kit Name", "New Kit Name", kit.getName() + " Copy")
                .validResultHandler(response -> {
                    final String newName = response.asInput(0);
                    DuelsPlugin.getFoliaLib().getScheduler().runAtEntity(player, task -> {
                        final CustomKitImpl dup = plugin.getCustomKitManager().duplicateKit(player, kit.getUniqueId(), newName != null ? newName.trim() : "");
                        if (dup != null) {
                            plugin.getLang().sendMessage(player, "COMMAND.customkits.duplicated", "kit", dup.getName());
                        }
                        openMainMenu(plugin, player);
                    });
                })
                .closedOrInvalidResultHandler(() -> openKitActions(plugin, player, kit))
                .build();

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), form);
    }

    private static void openDeleteConfirm(final DuelsPlugin plugin, final Player player, final CustomKitImpl kit) {
        final ModalForm form = ModalForm.builder()
                .title("Delete Kit?")
                .content("Are you sure you want to delete '" + kit.getName() + "'?")
                .button1("§c✘ Delete")
                .button2("§a« Cancel")
                .validResultHandler(response -> {
                    if (response.clickedFirst()) {
                        DuelsPlugin.getFoliaLib().getScheduler().runAtEntity(player, task -> {
                            plugin.getCustomKitManager().deleteKit(player.getUniqueId(), kit.getUniqueId());
                            plugin.getLang().sendMessage(player, "COMMAND.customkits.deleted", "kit", kit.getName());
                            openMainMenu(plugin, player);
                        });
                    } else {
                        openKitActions(plugin, player, kit);
                    }
                })
                .closedOrInvalidResultHandler(() -> openKitActions(plugin, player, kit))
                .build();

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), form);
    }

    public static void openPreview(final DuelsPlugin plugin, final Player player, final CustomKitSnapshot snapshot, final Runnable onBack) {
        final StringBuilder content = new StringBuilder();
        content.append("§e=== Kit Preview: §f").append(snapshot.getName()).append(" §e===\n\n");

        content.append("§6Armor:\n");
        content.append("§7- Helmet: §f").append(formatSlotItem(snapshot.getArmor().get(0))).append("\n");
        content.append("§7- Chestplate: §f").append(formatSlotItem(snapshot.getArmor().get(1))).append("\n");
        content.append("§7- Leggings: §f").append(formatSlotItem(snapshot.getArmor().get(2))).append("\n");
        content.append("§7- Boots: §f").append(formatSlotItem(snapshot.getArmor().get(3))).append("\n");
        content.append("§7- Offhand: §f").append(formatSlotItem(snapshot.getOffHand())).append("\n\n");

        content.append("§6Hotbar Items:\n");
        for (int i = 0; i < 9; i++) {
            final ItemStack item = snapshot.getItems().get(i);
            if (item != null && item.getType() != Material.AIR) {
                content.append("§7").append(i + 1).append(". §f").append(formatSlotItem(item)).append("\n");
            }
        }

        content.append("\n§6Main Inventory:\n");
        for (int i = 9; i < 36; i++) {
            final ItemStack item = snapshot.getItems().get(i);
            if (item != null && item.getType() != Material.AIR) {
                content.append("§7").append(i - 8).append(". §f").append(formatSlotItem(item)).append("\n");
            }
        }

        final SimpleForm form = SimpleForm.builder()
                .title("Preview: " + snapshot.getName())
                .content(content.toString())
                .button("§a« Back")
                .validResultHandler(response -> {
                    if (onBack != null) onBack.run();
                })
                .closedOrInvalidResultHandler(() -> {
                    if (onBack != null) onBack.run();
                })
                .build();

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), form);
    }

    private static String formatSlotItem(final ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return "Empty";
        }
        final String name = (item.getItemMeta() != null && item.getItemMeta().hasDisplayName())
                ? item.getItemMeta().getDisplayName() : item.getType().name();
        return name + " x" + item.getAmount();
    }
}
