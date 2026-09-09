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
        final String limitStr = maxKits == Integer.MAX_VALUE
                ? plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GENERAL.unlimited", "Unlimited"))
                : String.valueOf(maxKits);
        final boolean reached = plugin.getCustomKitManager().hasReachedLimit(player);

        final String content = plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.main-menu.content",
                "&7Manage your custom duel kits.\n&eKits: &a%count% &7/ &e%limit%\n")
                .replace("%count%", String.valueOf(playerKits.size()))
                .replace("%limit%", limitStr));

        final SimpleForm.Builder builder = SimpleForm.builder()
                .title(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.main-menu.title", "My Custom Kits")))
                .content(content);

        if (!reached) {
            builder.button(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.main-menu.create", "&a+ Create New Kit")));
        }

        for (final CustomKit kit : playerKits) {
            final int itemCount = kit.getItems().size() + kit.getArmor().size() + (kit.getOffHand() != null ? 1 : 0);
            final String itemBtn = plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.main-menu.item-format",
                    "&b%name%\n&7(%items% items)")
                    .replace("%name%", kit.getName())
                    .replace("%items%", String.valueOf(itemCount)));
            builder.button(itemBtn);
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
                .title(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.create-prompt.title", "Create Custom Kit")))
                .input(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.create-prompt.name-label", "Kit Name")),
                        plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.create-prompt.name-placeholder", "Enter name (e.g. Sword PvP)")))
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
                .title(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.actions.title", "Kit: %kit%").replace("%kit%", kit.getName())))
                .content(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.actions.content", "&eChoose an action for this kit:")))
                .button(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.actions.edit-layout", "&e✏ Edit Inventory Layout")))
                .button(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.actions.edit-settings", "&b⚙ Edit Kit Name / Description")))
                .button(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.actions.preview", "&a👁 Preview Kit")))
                .button(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.actions.duplicate", "&6❐ Duplicate Kit")))
                .button(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.actions.delete", "&c✘ Delete Kit")))
                .button(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.actions.back", "&7« Back to Kits")))
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
                .title(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.layout.title", "Layout: %kit%").replace("%kit%", draft.getName())))
                .content(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.layout.content", "&7Tap any slot to configure its item:\n")));

        builder.button(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.layout.save", "&a✔ Save Kit")));
        builder.button(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.layout.discard", "&c✘ Discard / Cancel")));

        // Armor
        builder.button(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.layout.helmet", "&e[Helmet] &f%item%")
                .replace("%item%", formatSlotItem(plugin, draft.getArmor().get(0)))));
        builder.button(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.layout.chestplate", "&e[Chestplate] &f%item%")
                .replace("%item%", formatSlotItem(plugin, draft.getArmor().get(1)))));
        builder.button(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.layout.leggings", "&e[Leggings] &f%item%")
                .replace("%item%", formatSlotItem(plugin, draft.getArmor().get(2)))));
        builder.button(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.layout.boots", "&e[Boots] &f%item%")
                .replace("%item%", formatSlotItem(plugin, draft.getArmor().get(3)))));

        // Offhand
        builder.button(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.layout.offhand", "&6[Offhand] &f%item%")
                .replace("%item%", formatSlotItem(plugin, draft.getOffHand()))));

        // Hotbar 1-9
        for (int h = 0; h < 9; h++) {
            builder.button(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.layout.hotbar", "&b[Hotbar %slot%] &f%item%")
                    .replace("%slot%", String.valueOf(h + 1))
                    .replace("%item%", formatSlotItem(plugin, draft.getItems().get(h)))));
        }

        // Main Inventory 1-27
        for (int m = 9; m < 36; m++) {
            builder.button(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.layout.inventory", "&7[Inv %slot%] &f%item%")
                    .replace("%slot%", String.valueOf(m - 8))
                    .replace("%item%", formatSlotItem(plugin, draft.getItems().get(m)))));
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

        final String title = hasItem
                ? plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.slot-options.title-item", "Item: %material%").replace("%material%", currentItem.getType().name()))
                : plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.slot-options.title-empty", "Empty Slot"));

        final SimpleForm.Builder builder = SimpleForm.builder().title(title);

        if (hasItem) {
            builder.button(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.slot-options.edit-properties", "&e✏ Edit Item Properties")));
            builder.button(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.slot-options.change-material", "&b🔄 Change Material")));
            builder.button(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.slot-options.clear-slot", "&c✘ Clear Slot")));
            builder.button(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.slot-options.back", "&7« Back")));
        } else {
            builder.button(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.slot-options.choose-material", "&a+ Choose Material")));
            builder.button(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.slot-options.back", "&7« Back")));
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

        final SimpleForm.Builder builder = SimpleForm.builder()
                .title(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.categories.title", "Select Category")));

        for (final MaterialBrowserGui.Category cat : MaterialBrowserGui.Category.values()) {
            builder.button("§b" + cat.getDisplayName(plugin.getLang()));
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

        final String title = isArmor
                ? plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.material-picker.title-armor", "Pick Armor (%slot%)")
                        .replace("%slot%", com.meteordevelopments.duels.core.customkit.validation.CustomKitValidator.getArmorSlotName(slot)))
                : plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.material-picker.title-category", "Pick Material: %category%")
                        .replace("%category%", category.getDisplayName(plugin.getLang())));
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
                .title(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.properties.title", "Edit: %material%").replace("%material%", item.getType().name())))
                .input(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.properties.display-name-label", "Display Name")), "Display Name", displayName)
                .slider(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.properties.amount-label", "Amount")), 1, 64, 1, amount)
                .toggle(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.properties.unbreakable-label", "Unbreakable")), unbreakable)
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
                .title(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.settings.title", "Settings: %kit%").replace("%kit%", kit.getName())))
                .input(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.settings.name-label", "Kit Name")), "Kit Name", kit.getName())
                .input(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.settings.desc-line-1", "Description Line 1")), "Description line 1", kit.getDescription().size() > 0 ? kit.getDescription().get(0) : "")
                .input(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.settings.desc-line-2", "Description Line 2")), "Description line 2", kit.getDescription().size() > 1 ? kit.getDescription().get(1) : "")
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
        final String copySuffix = plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.duplicate-prompt.copy-suffix", " Copy"));
        final CustomForm form = CustomForm.builder()
                .title(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.duplicate-prompt.title", "Duplicate Kit")))
                .input(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.duplicate-prompt.name-label", "New Kit Name")), "New Kit Name", kit.getName() + copySuffix)
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
                .title(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.customkits-menu.confirm-delete.title", "Delete Kit?")))
                .content(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.customkits-menu.confirm-delete.message", "Are you sure you want to delete kit '%kit%'?").replace("%kit%", kit.getName())))
                .button1(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.actions.delete", "§c✘ Delete")))
                .button2(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.customkit-confirm.buttons.cancel.name", "§a« Cancel")))
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
        content.append(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.preview.header", "§e=== Kit Preview: §f%kit% §e===\n\n").replace("%kit%", snapshot.getName())));

        content.append(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.preview.armor-header", "§6Armor:\n")));
        final String[] armorSlots = {"Helmet", "Chestplate", "Leggings", "Boots"};
        for (int i = 0; i < 4; i++) {
            content.append(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.preview.armor-slot", "§7- %slot%: §f%item%\n")
                    .replace("%slot%", armorSlots[i])
                    .replace("%item%", formatSlotItem(plugin, snapshot.getArmor().get(i)))));
        }
        content.append(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.preview.offhand-slot", "§7- Offhand: §f%item%\n\n")
                .replace("%item%", formatSlotItem(plugin, snapshot.getOffHand()))));

        content.append(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.preview.hotbar-header", "§6Hotbar Items:\n")));
        for (int i = 0; i < 9; i++) {
            final ItemStack item = snapshot.getItems().get(i);
            if (item != null && item.getType() != Material.AIR) {
                content.append(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.preview.hotbar-item", "§7%slot%. §f%item%\n")
                        .replace("%slot%", String.valueOf(i + 1))
                        .replace("%item%", formatSlotItem(plugin, item))));
            }
        }

        content.append(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.preview.inventory-header", "\n§6Main Inventory:\n")));
        for (int i = 9; i < 36; i++) {
            final ItemStack item = snapshot.getItems().get(i);
            if (item != null && item.getType() != Material.AIR) {
                content.append(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.preview.inventory-item", "§7%slot%. §f%item%\n")
                        .replace("%slot%", String.valueOf(i - 8))
                        .replace("%item%", formatSlotItem(plugin, item))));
            }
        }

        final SimpleForm form = SimpleForm.builder()
                .title(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.preview.title", "Preview: %kit%").replace("%kit%", snapshot.getName())))
                .content(content.toString())
                .button(plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.preview.back", "§a« Back")))
                .validResultHandler(response -> {
                    if (onBack != null) onBack.run();
                })
                .closedOrInvalidResultHandler(() -> {
                    if (onBack != null) onBack.run();
                })
                .build();

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), form);
    }

    private static String formatSlotItem(final DuelsPlugin plugin, final ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return plugin.getLang().toLegacyString(plugin.getLang().getMessageOrDefault("GUI.bedrock-customkits.preview.empty", "Empty"));
        }
        final String name = (item.getItemMeta() != null && item.getItemMeta().hasDisplayName())
                ? item.getItemMeta().getDisplayName() : item.getType().name();
        return name + " x" + item.getAmount();
    }
}
