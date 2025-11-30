package com.meteordevelopments.duels.listeners;

import com.meteordevelopments.duels.DuelsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks ownership of EnderCrystals by tagging spawned crystals with the placing player's UUID.
 */
public class ExplosionOwnershipListener implements Listener {

    private static final String OWNER_META = "Duels-Owner";
    private final DuelsPlugin plugin;
    private final Map<UUID, Long> recentCrystalUse = new ConcurrentHashMap<>();
    private final Map<UUID, Long> recentTntMinecartUse = new ConcurrentHashMap<>();
    private final Map<World, UUID> recentBlockExplosionTrigger = new ConcurrentHashMap<>();
    private final Map<World, Long> recentBlockExplosionTime = new ConcurrentHashMap<>();
    private final Map<World, UUID> recentMinecartExplosionOwner = new ConcurrentHashMap<>();
    private final Map<World, Long> recentMinecartExplosionTime = new ConcurrentHashMap<>();

    public ExplosionOwnershipListener(final DuelsPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        // Only consider main hand interactions
        if (event.getHand() != EquipmentSlot.HAND) return;

        final Player player = event.getPlayer();
        final ItemStack item = event.getItem();

        // Record placing of crystal / TNT minecart via item in hand
        if (item != null) {
            if (item.getType() == Material.END_CRYSTAL) {
                recentCrystalUse.put(player.getUniqueId(), System.currentTimeMillis());
            } else if (item.getType() == Material.TNT_MINECART) {
                recentTntMinecartUse.put(player.getUniqueId(), System.currentTimeMillis());
            }
        }

        // Bed / respawn anchor explosions are triggered by interacting with the placed block
        if (event.getClickedBlock() != null) {
            Material type = event.getClickedBlock().getType();
            if (type == Material.RESPAWN_ANCHOR || type.name().endsWith("_BED")) {
                recentBlockExplosionTrigger.put(player.getWorld(), player.getUniqueId());
                recentBlockExplosionTime.put(player.getWorld(), System.currentTimeMillis());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntitySpawn(final EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof EnderCrystal crystal)) {
            // Not a crystal; check TNT minecart
            if (event.getEntity() instanceof ExplosiveMinecart minecart) {
                Player owner = findRecentUser(minecart.getWorld(), recentTntMinecartUse);
                if (owner != null) {
                    minecart.setMetadata(OWNER_META, new FixedMetadataValue(plugin, owner.getUniqueId().toString()));
                }
            }
            return;
        }
        // Find a player who very recently used an end crystal
        Player owner = findRecentUser(crystal.getWorld(), recentCrystalUse);
        if (owner != null) {
            crystal.setMetadata(OWNER_META, new FixedMetadataValue(plugin, owner.getUniqueId().toString()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(final EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof ExplosiveMinecart minecart)) {
            return;
        }
        // Try to get owner from metadata first
        UUID ownerUuid = getTntMinecartOwner(minecart);
        
        // If no metadata, try to find recent user as fallback
        if (ownerUuid == null) {
            Player recentUser = findRecentUser(minecart.getWorld(), recentTntMinecartUse);
            if (recentUser != null) {
                ownerUuid = recentUser.getUniqueId();
            }
        }
        
        if (ownerUuid != null) {
            World world = minecart.getWorld();
            recentMinecartExplosionOwner.put(world, ownerUuid);
            recentMinecartExplosionTime.put(world, System.currentTimeMillis());
        }
    }

    private Player findRecentUser(final World world, final Map<UUID, Long> activityMap) {
        long now = System.currentTimeMillis();
        for (Player p : world.getPlayers()) {
            Long lastUse = activityMap.get(p.getUniqueId());
            if (lastUse != null && (now - lastUse) <= 1500) {
                return p;
            }
        }
        return null;
    }

    public static UUID getCrystalOwner(final EnderCrystal crystal) {
        if (crystal.hasMetadata(OWNER_META)) {
            try {
                String uuidStr = crystal.getMetadata(OWNER_META).get(0).asString();
                return UUID.fromString(uuidStr);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public static UUID getTntMinecartOwner(final ExplosiveMinecart minecart) {
        if (minecart.hasMetadata(OWNER_META)) {
            try {
                String uuidStr = minecart.getMetadata(OWNER_META).get(0).asString();
                return UUID.fromString(uuidStr);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public static UUID getRecentBlockExplosionTrigger(final World world) {
        return world == null ? null : INSTANCE.recentBlockExplosionTrigger.get(world);
    }

    public static boolean isRecentBlockExplosionInWorld(final World world, long windowMs) {
        if (world == null) return false;
        Long time = INSTANCE.recentBlockExplosionTime.get(world);
        return time != null && (System.currentTimeMillis() - time) <= windowMs;
    }

    public static UUID getRecentMinecartExplosionOwner(final World world) {
        return world == null ? null : INSTANCE.recentMinecartExplosionOwner.get(world);
    }

    public static boolean isRecentMinecartExplosionInWorld(final World world, long windowMs) {
        if (world == null) return false;
        Long time = INSTANCE.recentMinecartExplosionTime.get(world);
        return time != null && (System.currentTimeMillis() - time) <= windowMs;
    }

    // Simple instance holder for static access to maps
    private static ExplosionOwnershipListener INSTANCE;

    {
        INSTANCE = this;
    }
}
