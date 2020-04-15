package me.realized.duels.listeners;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.Arena;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.arena.Match;
import me.realized.duels.config.Config;
import me.realized.duels.kit.Kit.Characteristic;
import me.realized.duels.util.compat.Items;
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
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void on(final PlayerInteractEvent event) {
        if (!event.hasItem() || !event.getAction().name().contains("RIGHT")) {
            return;
        }

        final Player player = event.getPlayer();
        final Arena arena = arenaManager.get(player);

        if (arena == null) {
            return;
        }

        final Match match = arena.getMatch();

        if (match == null || match.getKit() == null || !match.getKit().hasCharacteristic(Characteristic.SOUP)) {
            return;
        }

        final ItemStack item = event.getItem();

        if (item == null || item.getType() != Items.MUSHROOM_SOUP) {
            return;
        }

        event.setUseItemInHand(Result.DENY);

        if (config.isSoupCancelIfAlreadyFull() && player.getHealth() == player.getMaxHealth()) {
            return;
        }

        player.getInventory().setItem(player.getInventory().getHeldItemSlot(), config.isSoupRemoveEmptyBowl() ? null : new ItemStack(Material.BOWL));

        final double regen = config.getSoupHeartsToRegen() * 2.0;
        final double oldHealth = player.getHealth();
        player.setHealth(oldHealth + regen > player.getMaxHealth() ? player.getMaxHealth() : oldHealth + regen);
    }
}
