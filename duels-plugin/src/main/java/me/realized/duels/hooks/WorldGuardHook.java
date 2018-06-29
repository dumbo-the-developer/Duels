package me.realized.duels.hooks;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.util.List;
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

    public boolean inDuelZone(Player player) {
        if (!config.isDuelzoneEnabled()) {
            return true;
        }

        final WorldGuardPlugin plugin = (WorldGuardPlugin) getPlugin();
        final List<String> allowedRegions = config.getDuelzoneRegions();
        return plugin.getRegionManager(player.getWorld())
            .getApplicableRegions(player.getLocation()).getRegions()
            .stream().anyMatch(region -> allowedRegions.contains(region.getId()));
    }
}
