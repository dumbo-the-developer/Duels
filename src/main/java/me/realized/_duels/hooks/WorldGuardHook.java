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

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.List;
import me.realized._duels.Core;
import me.realized._duels.configuration.MainConfig;
import org.bukkit.entity.Player;

public class WorldGuardHook extends PluginHook {

    private final MainConfig config;

    public WorldGuardHook(Core instance) {
        super("WorldGuard");
        this.config = instance.getConfiguration();
    }

    public boolean canUseDuelCommands(Player player) {
        if (!isEnabled() || !config.isDuelZoneEnabled()) {
            return true;
        }

        List<String> allowedRegions = config.getDuelZoneRegions();
        WorldGuardPlugin worldguard = (WorldGuardPlugin) getPlugin();
        ApplicableRegionSet regions = worldguard.getRegionManager(player.getWorld()).getApplicableRegions(player.getLocation());

        if (regions.getRegions().isEmpty()) {
            return false;
        }

        for (ProtectedRegion region : regions.getRegions()) {
            if (allowedRegions.contains(region.getId())) {
                return true;
            }
        }

        return false;
    }
}
