package com.meteordevelopments.duels.hook.hooks;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.util.hook.PluginHook;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class McMMOHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "mcMMO";

    private final Config config;
    private final Map<UUID, PermissionAttachment> attachments = new HashMap<>();

    public McMMOHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
        this.config = plugin.getConfiguration();
    }

    public void disableSkills(final Player player) {
        if (!config.isDisableSkills()) {
            return;
        }

        final PermissionAttachment attachment = attachments.computeIfAbsent(player.getUniqueId(), result -> player.addAttachment(plugin));
        attachment.setPermission("mcmmo.skills.*", false);
        player.recalculatePermissions();
    }

    public void enableSkills(final Player player) {
        if (!config.isDisableSkills()) {
            return;
        }

        final PermissionAttachment attachment = attachments.remove(player.getUniqueId());

        if (attachment == null) {
            return;
        }

        attachment.remove();
    }
}
