package com.meteordevelopments.duels.command.commands;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.Permissions;
import com.meteordevelopments.duels.api.customkit.CustomKit;
import com.meteordevelopments.duels.command.BaseCommand;
import com.meteordevelopments.duels.config.CommandsConfig.CommandSettings;
import com.meteordevelopments.duels.core.customkit.CustomKitImpl;
import com.meteordevelopments.duels.core.customkit.session.CustomKitEditSession;
import com.meteordevelopments.duels.gui.bedrock.BedrockCustomKitForm;
import com.meteordevelopments.duels.gui.customkit.CustomKitEditorGui;
import com.meteordevelopments.duels.gui.customkit.CustomKitMenuGui;
import com.meteordevelopments.duels.gui.customkit.CustomKitPreviewGui;
import com.meteordevelopments.duels.util.FloodgateUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CustomKitsCommand extends BaseCommand {

    public CustomKitsCommand(final DuelsPlugin plugin, final CommandSettings settings) {
        super(plugin, settings.getName(), "customkits [create|edit|delete|duplicate|preview|list] [kitname]", "Manage your custom duel kits.", Permissions.CUSTOMKITS_USE, 0, true, settings.getAliasArray());
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;

        if (args.length == 0) {
            if (FloodgateUtil.isBedrockPlayer(player)) {
                BedrockCustomKitForm.openMainMenu(plugin, player);
            } else {
                CustomKitMenuGui.open(plugin, player);
            }
            return;
        }

        final String sub = args[0].toLowerCase();

        switch (sub) {
            case "create": {
                if (!player.hasPermission(Permissions.CUSTOMKITS_CREATE) && !player.hasPermission(Permissions.CUSTOMKITS_USE)) {
                    lang.sendMessage(player, "ERROR.no-permission", "permission", Permissions.CUSTOMKITS_CREATE);
                    return;
                }

                if (args.length < 2) {
                    lang.sendMessage(player, "COMMAND.customkits.usage-create");
                    return;
                }

                final String kitName = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
                final CustomKitImpl newKit = plugin.getCustomKitManager().createKit(player, kitName);
                if (newKit != null) {
                    lang.sendMessage(player, "COMMAND.customkits.created", "kit", newKit.getName());
                    final CustomKitEditSession session = plugin.getCustomKitManager().startSession(player, newKit, true);
                    if (FloodgateUtil.isBedrockPlayer(player)) {
                        BedrockCustomKitForm.openLayoutEditor(plugin, player, session);
                    } else {
                        CustomKitEditorGui.open(plugin, player, session);
                    }
                }
                break;
            }

            case "edit": {
                if (!player.hasPermission(Permissions.CUSTOMKITS_EDIT) && !player.hasPermission(Permissions.CUSTOMKITS_USE)) {
                    lang.sendMessage(player, "ERROR.no-permission", "permission", Permissions.CUSTOMKITS_EDIT);
                    return;
                }

                if (args.length < 2) {
                    lang.sendMessage(player, "COMMAND.customkits.usage-edit");
                    return;
                }

                final String kitName = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
                final CustomKitImpl kit = plugin.getCustomKitManager().getKit(player.getUniqueId(), kitName);

                if (kit == null) {
                    lang.sendMessage(player, "ERROR.customkits.not-found", "name", kitName);
                    return;
                }

                final CustomKitEditSession session = plugin.getCustomKitManager().startSession(player, kit, false);
                if (FloodgateUtil.isBedrockPlayer(player)) {
                    BedrockCustomKitForm.openLayoutEditor(plugin, player, session);
                } else {
                    CustomKitEditorGui.open(plugin, player, session);
                }
                break;
            }

            case "delete": {
                if (!player.hasPermission(Permissions.CUSTOMKITS_DELETE) && !player.hasPermission(Permissions.CUSTOMKITS_USE)) {
                    lang.sendMessage(player, "ERROR.no-permission", "permission", Permissions.CUSTOMKITS_DELETE);
                    return;
                }

                if (args.length < 2) {
                    lang.sendMessage(player, "COMMAND.customkits.usage-delete");
                    return;
                }

                final String kitName = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
                final CustomKitImpl kit = plugin.getCustomKitManager().getKit(player.getUniqueId(), kitName);

                if (kit == null) {
                    lang.sendMessage(player, "ERROR.customkits.not-found", "name", kitName);
                    return;
                }

                plugin.getCustomKitManager().deleteKit(player.getUniqueId(), kit.getUniqueId());
                lang.sendMessage(player, "COMMAND.customkits.deleted", "kit", kit.getName());
                break;
            }

            case "duplicate": {
                if (!player.hasPermission(Permissions.CUSTOMKITS_DUPLICATE) && !player.hasPermission(Permissions.CUSTOMKITS_USE)) {
                    lang.sendMessage(player, "ERROR.no-permission", "permission", Permissions.CUSTOMKITS_DUPLICATE);
                    return;
                }

                if (args.length < 3) {
                    lang.sendMessage(player, "COMMAND.customkits.usage-duplicate");
                    return;
                }

                final String oldName = args[1];
                final String newName = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                final CustomKitImpl original = plugin.getCustomKitManager().getKit(player.getUniqueId(), oldName);

                if (original == null) {
                    lang.sendMessage(player, "ERROR.customkits.not-found", "name", oldName);
                    return;
                }

                final CustomKitImpl dup = plugin.getCustomKitManager().duplicateKit(player, original.getUniqueId(), newName);
                if (dup != null) {
                    lang.sendMessage(player, "COMMAND.customkits.duplicated", "kit", dup.getName());
                }
                break;
            }

            case "preview": {
                if (args.length < 2) {
                    lang.sendMessage(player, "COMMAND.customkits.usage-preview");
                    return;
                }

                final String kitName = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
                final CustomKitImpl kit = plugin.getCustomKitManager().getKit(player.getUniqueId(), kitName);

                if (kit == null) {
                    lang.sendMessage(player, "ERROR.customkits.not-found", "name", kitName);
                    return;
                }

                if (FloodgateUtil.isBedrockPlayer(player)) {
                    BedrockCustomKitForm.openPreview(plugin, player, kit.toSnapshot(), () -> BedrockCustomKitForm.openMainMenu(plugin, player));
                } else {
                    CustomKitPreviewGui.open(plugin, player, kit, () -> CustomKitMenuGui.open(plugin, player));
                }
                break;
            }

            case "list": {
                final List<CustomKit> playerKits = plugin.getCustomKitManager().getKits(player.getUniqueId());
                if (playerKits.isEmpty()) {
                    lang.sendMessage(player, "COMMAND.customkits.list.empty");
                    return;
                }

                final List<String> names = new ArrayList<>();
                for (final CustomKit k : playerKits) {
                    names.add(k.getName());
                }
                lang.sendMessage(player, "COMMAND.customkits.list.header", "count", playerKits.size());
                lang.sendMessage(player, "COMMAND.customkits.list.entry", "kits", String.join("&7, &f", names));
                break;
            }

            default:
                if (FloodgateUtil.isBedrockPlayer(player)) {
                    BedrockCustomKitForm.openMainMenu(plugin, player);
                } else {
                    CustomKitMenuGui.open(plugin, player);
                }
                break;
        }
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (!(sender instanceof Player player)) {
            return null;
        }

        if (args.length == 1) {
            return handleTabCompletion(args[0], List.of("create", "edit", "delete", "duplicate", "preview", "list"));
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("edit") || args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("preview") || args[0].equalsIgnoreCase("duplicate"))) {
            final List<String> names = new ArrayList<>();
            for (final CustomKit k : plugin.getCustomKitManager().getKits(player.getUniqueId())) {
                names.add(k.getName());
            }
            return handleTabCompletion(args[1], names);
        }

        return null;
    }
}
