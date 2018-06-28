package me.realized.duels.extra;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.Arena;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.config.Config;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SoupListener implements Listener {

    private final Config config;
    private final ArenaManager arenaManager;

    public SoupListener(final DuelsPlugin plugin) {
        this.config = plugin.getConfiguration();
        this.arenaManager = plugin.getArenaManager();

        if (config.isSoupEnabled()) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    @EventHandler
    public void on(final PlayerInteractEvent event) {
        if (!event.hasItem() || !event.getAction().name().contains("RIGHT")) {
            return;
        }

        final ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.MUSHROOM_SOUP) {
            return;
        }

        event.setUseItemInHand(Result.DENY);

        final Player player = event.getPlayer();

        if (player.getHealth() == player.getMaxHealth()) {
            return;
        }

        final Arena arena = arenaManager.get(player);

        if (arena == null || !arena.getName().startsWith(config.getNameStartingWith())) {
            return;
        }

        player.getInventory().setItem(player.getInventory().getHeldItemSlot(), new ItemStack(Material.BOWL));

        final double regen = config.getHeartsToRegen() * 2.0;
        final double oldHealth = player.getHealth();
        player.setHealth(oldHealth + regen > player.getMaxHealth() ? player.getMaxHealth() : oldHealth + regen);
    }
}
