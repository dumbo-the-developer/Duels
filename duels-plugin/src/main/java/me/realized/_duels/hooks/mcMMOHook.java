/*
 * This file is part of Duels, licensed under the MIT License.
 *
 * Copyright (c) Realized
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.realized._duels.hooks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import me.realized._duels.Core;
import me.realized._duels.configuration.MainConfig;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

public class mcMMOHook extends PluginHook {

    private final Core instance;
    private final MainConfig config;
    private final List<String> skills = Arrays.asList("swords", "archery", "axes", "taming", "unarmed");

    private Map<UUID, PermissionAttachment> attachments = new HashMap<>();

    public mcMMOHook(Core instance) {
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
