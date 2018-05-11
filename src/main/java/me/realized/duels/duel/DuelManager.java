package me.realized.duels.duel;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.Arena;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.cache.Setting;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.util.Loadable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

public class DuelManager implements Loadable, Listener {

    private final Config config;
    private final Lang lang;
    private final ArenaManager arenaManager;

    public DuelManager(final DuelsPlugin plugin) {
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.arenaManager = plugin.getArenaManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() throws Exception {

    }

    @Override
    public void handleUnload() throws Exception {

    }

    // Make sure to check for everything again before actually starting the match!
    public void startMatch(final Player first, final Player second, final Setting setting, final Map<UUID, List<ItemStack>> items) {
        final Arena arena = setting.getArena() != null ? setting.getArena() : arenaManager.randomArena();

        if (arena == null || !arena.isAvailable()) {
            tempMsg("The arena is currently unavailable, please try again later.", first, second);
            return;
        }

        arena.setUsed(true);
        arena.setMatch(setting.getKit(), items, setting.getBet());
        first.teleport(arena.getPositions().get(1));
        arena.addPlayer(first);
        second.teleport(arena.getPositions().get(2));
        arena.addPlayer(second);

        if (setting.getKit() != null) {
            setting.getKit().equip(first, second);
        }

        tempMsg("Duel started!", first, second);
    }


    private void tempMsg(final String msg, final Player... players) {
        for (final Player player : players) {
            player.sendMessage(msg);
        }
    }

    @EventHandler
    public void on(final PlayerQuitEvent event) {

    }


    @EventHandler (ignoreCancelled = true)
    public void on(final PlayerDropItemEvent event) {
        if (!config.isPreventItemDrop() || !arenaManager.isInMatch(event.getPlayer())) {
            return;
        }

        event.setCancelled(true);
        lang.sendMessage(event.getPlayer(), "DUEL.prevent-item-drop");
    }


    @EventHandler (ignoreCancelled = true)
    public void on(final PlayerPickupItemEvent event) {
        if (!config.isPreventItemPickup() || !arenaManager.isInMatch(event.getPlayer())) {
            return;
        }

        event.setCancelled(true);
        lang.sendMessage(event.getPlayer(), "DUEL.prevent-item-pickup");
    }


    @EventHandler (ignoreCancelled = true)
    public void on(final PlayerCommandPreprocessEvent event) {
        final String command = event.getMessage().substring(1).split(" ")[0].toLowerCase();

        if (!arenaManager.isInMatch(event.getPlayer()) || (config.isBlockAllCommands() ? config.getWhitelistedCommands().contains(command) : !config.getBlacklistedCommands().contains(command))) {
            return;
        }

        event.setCancelled(true);
        lang.sendMessage(event.getPlayer(), "DUEL.prevent-command", "command", event.getMessage());
    }


    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(PlayerTeleportEvent event) {
        final Player player = event.getPlayer();
        final Location to = event.getTo();

        // TODO: 11/05/2018 Add spectators to getAllPlayers collection
        for (final UUID uuid : arenaManager.getAllPlayers()) {
            final Player target = Bukkit.getPlayer(uuid);

            if (target == null || !isSimilar(target.getLocation(), to)) {
                continue;
            }

            event.setCancelled(true);
            lang.sendMessage(player, "ERROR.patch-prevent-teleportation");
            return;
        }

        if (!config.isLimitTeleportEnabled() || event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL || !arenaManager.isInMatch(player)) {
            return;
        }

        final Location from = event.getFrom();

        if (from.getWorld().equals(to.getWorld()) && from.distance(to) <= config.getDistanceAllowed()) {
            return;
        }

        event.setCancelled(true);
        lang.sendMessage(player, "DUEL.prevent-teleportation");
    }

    private boolean isSimilar(final Location first, final Location second) {
        return Math.abs(first.getX() - second.getX()) + Math.abs(first.getY() - second.getY()) + Math.abs(first.getZ() - second.getZ()) < 5;
    }
}
