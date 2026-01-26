package com.meteordevelopments.duels.validator.validators.match;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.setting.Settings;
import com.meteordevelopments.duels.validator.BaseBiValidator;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;

public class CheckMoveValidator extends BaseBiValidator<Collection<Player>, Settings> {

    private static final String MESSAGE_KEY = "DUEL.start-failure.player-moved";
    private static final String PARTY_MESSAGE_KEY = "DUEL.party-start-failure.player-moved";

    public CheckMoveValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean shouldValidate() {
        return config.isCancelIfMoved();
    }

    /**
     * Normalizes world names by removing namespace prefixes (e.g., "minecraft:world" -> "world")
     */
    private String normalizeWorldName(String worldName) {
        if (worldName == null) {
            return "";
        }
        // Remove namespace prefix if present (e.g., "minecraft:world" -> "world")
        if (worldName.contains(":")) {
            return worldName.substring(worldName.indexOf(":") + 1);
        }
        return worldName;
    }

    /**
     * Compares two worlds by normalized name to handle namespaced worlds correctly
     */
    private boolean isSameWorld(World world1, World world2) {
        if (world1 == null || world2 == null) {
            return false;
        }
        if (world1.equals(world2)) {
            return true; // Same object reference
        }
        // Compare by normalized name
        return normalizeWorldName(world1.getName()).equalsIgnoreCase(normalizeWorldName(world2.getName()));
    }

    private boolean notInLoc(final Player player, final Location location) {
        if (location == null) {
            return false;
        }

        final Location source = player.getLocation();
        if (source.getWorld() == null || location.getWorld() == null) {
            return true; // Different if either world is null
        }
        
        // Use normalized world name comparison to handle namespaced worlds
        return !isSameWorld(source.getWorld(), location.getWorld())
                || source.getBlockX() != location.getBlockX()
                || source.getBlockY() != location.getBlockY()
                || source.getBlockZ() != location.getBlockZ();
    }

    @Override
    public boolean validate(final Collection<Player> players, final Settings settings) {
        if (players.stream().anyMatch(player -> notInLoc(player, settings.getBaseLoc(player)))) {
            lang.sendMessage(players, settings.isPartyDuel() ? PARTY_MESSAGE_KEY : MESSAGE_KEY);
            return false;
        }

        return true;
    }
}
