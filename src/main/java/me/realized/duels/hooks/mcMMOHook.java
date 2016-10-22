package me.realized.duels.hooks;

import me.realized.duels.Core;
import me.realized.duels.configuration.MainConfig;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.*;

public class McMMOHook extends PluginHook {

    private final Core instance;
    private final MainConfig config;
    private final List<String> skills = Arrays.asList("swords", "archery", "axes", "taming", "unarmed");

    private Map<UUID, PermissionAttachment> attachments = new HashMap<>();

    public McMMOHook(Core instance) {
        super("mcMMO");
        this.instance = instance;
        this.config = instance.getConfiguration();
    }

    public void disableSkills(Player player) {
        if (!isEnabled() || !config.isPatchesDisableMcMMOInMatch()) {
            return;
        }

        if (!attachments.containsKey(player.getUniqueId())) {
            attachments.put(player.getUniqueId(), player.addAttachment(instance));
        }

        PermissionAttachment attachment = attachments.get(player.getUniqueId());

        for (String skill : skills) {
            String permission = "mcmmo.skills." + skill;

            if (player.hasPermission(permission)) {
                attachment.setPermission(permission, false);
            }
        }

        attachment.getPermissible().recalculatePermissions();
    }

    public void enableSkills(Player player) {
        if (!isEnabled() || !config.isPatchesDisableMcMMOInMatch()) {
            return;
        }

        PermissionAttachment attachment = attachments.get(player.getUniqueId());

        if (attachment == null || !attachment.getPermissible().equals(player)) {
            return;
        }

        attachments.remove(player.getUniqueId());
        attachment.remove();
    }
}
