package me.realized.duels.hooks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.config.Config;
import me.realized.duels.util.hook.PluginHook;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

public class McMMOHook extends PluginHook<DuelsPlugin> {

    private final Config config;
    private final Map<UUID, PermissionAttachment> attachments = new HashMap<>();

    public McMMOHook(final DuelsPlugin plugin) {
        super(plugin, "mcMMO");
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
