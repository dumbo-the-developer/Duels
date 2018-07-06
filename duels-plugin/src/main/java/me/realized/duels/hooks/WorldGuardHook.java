package me.realized.duels.hooks;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.Collection;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.config.Config;
import me.realized.duels.util.hook.PluginHook;
import org.bukkit.entity.Player;

public class WorldGuardHook extends PluginHook<DuelsPlugin> {

    private final Config config;

    public WorldGuardHook(final DuelsPlugin plugin) {
        super(plugin, "WorldGuard");
        this.config = plugin.getConfiguration();
    }

    public String findDuelZone(final Player player) {
        if (!config.isDuelzoneEnabled()) {
            return null;
        }

        final WorldGuardPlugin plugin = (WorldGuardPlugin) getPlugin();
        final Collection<String> allowedRegions = config.getDuelzones();

        if (allowedRegions.isEmpty()) {
            return null;
        }

        for (final ProtectedRegion region : plugin.getRegionManager(player.getWorld()).getApplicableRegions(player.getLocation())) {
            if (allowedRegions.contains(region.getId())) {
                return region.getId();
            }
        }

        return null;
    }
}
