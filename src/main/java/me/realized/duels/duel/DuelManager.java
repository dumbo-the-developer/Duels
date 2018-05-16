package me.realized.duels.duel;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.Arena;
import me.realized.duels.arena.ArenaManager;
import me.realized.duels.arena.Match;
import me.realized.duels.cache.PlayerDataCache;
import me.realized.duels.cache.Setting;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.data.MatchData;
import me.realized.duels.data.UserData;
import me.realized.duels.data.UserDataManager;
import me.realized.duels.kit.Kit;
import me.realized.duels.kit.KitManager;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

public class DuelManager implements Loadable, Listener {

    private final Config config;
    private final Lang lang;
    private final UserDataManager userDataManager;
    private final ArenaManager arenaManager;
    private final KitManager kitManager;
    private final PlayerDataCache playerDataCache;

    public DuelManager(final DuelsPlugin plugin) {
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.userDataManager = plugin.getUserManager();
        this.arenaManager = plugin.getArenaManager();
        this.kitManager = plugin.getKitManager();
        this.playerDataCache = plugin.getPlayerDataCache();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() throws Exception {

    }

    @Override
    public void handleUnload() throws Exception {

    }

    // todo: Make sure to check for everything again before actually starting the match
    public void startMatch(final Player first, final Player second, final Setting setting, final Map<UUID, List<ItemStack>> items) {
        final Arena arena = setting.getArena() != null ? setting.getArena() : arenaManager.randomArena();

        if (arena == null || !arena.isAvailable()) {
            first.sendMessage("The arena is currently unavailable, please try again later.");
            second.sendMessage("The arena is currently unavailable, please try again later.");
            return;
        }

        final Kit kit = setting.getKit() != null ? setting.getKit() : kitManager.randomKit();
        arena.setUsed(true);
        arena.setMatch(kit, items, setting.getBet());
        handlePlayer(first, arena, kit, arena.getPositions().get(1));
        handlePlayer(second, arena, kit, arena.getPositions().get(2));
    }

    private void handlePlayer(final Player player, final Arena arena, final Kit kit, final Location location) {
        if (player.getAllowFlight()) {
            player.setFlying(false);
            player.setAllowFlight(false);
        }

        player.closeInventory();
        player.teleport(location);
        arena.addPlayer(player);

        if (!config.isUseOwnInventoryEnabled()) {
            PlayerUtil.reset(player);
            playerDataCache.get(player).init(player);

            if (kit != null) {
                kit.equip(player);
            }
        }
    }

//    @EventHandler
//    public void on(final PlayerDeathEvent event) {
//        final Player player = event.getEntity();
//        System.out.println(player.getName() + " ded");
//        final Optional<Arena> result = arenaManager.get(player);
//
//        if (!result.isPresent()) {
//            return;
//        }
//
//        event.setDeathMessage(null);
//        event.setKeepInventory(config.isUseOwnInventoryEnabled() && config.isUseOwnInventoryKeepItems());
//
//        if (!config.isUseOwnInventoryEnabled()) {
//            event.setKeepLevel(true);
//            event.setDroppedExp(0);
//            event.getDrops().clear();
//        }
//
//        final Arena arena = result.get();
//        arena.removePlayer(player);
//
//        if (arena.getPlayers().size() > 0) {
//            final Player target = Bukkit.getPlayer(arena.getFirst());
//
//        }
//
//        final Match match = arena.getCurrent();
//        arena.setUsed(false);
//    }

    @EventHandler (ignoreCancelled = true)
    public void on(final EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        final Player player = (Player) event.getEntity();
        final Optional<Arena> result = arenaManager.get(player);

        if (!result.isPresent()) {
            return;
        }

        final Arena arena = result.get();

        if (arena.getPlayers().size() < 2) {
            event.setCancelled(true);
            return;
        }

        if (player.getHealth() - event.getFinalDamage() > 0) {
            return;
        }

        event.setDamage(0);
        arena.removePlayer(player);

        final Match match = arena.getCurrent();
        final Player winner = Bukkit.getPlayer(arena.getFirst());
        final long duration = System.currentTimeMillis() - match.getCreation();
        final long time = new GregorianCalendar().getTimeInMillis();
        final double health = Math.ceil(winner.getHealth()) * 0.5;
        final MatchData matchData = new MatchData(winner.getName(), player.getName(), time, duration, health);
        Optional<UserData> cached = userDataManager.get(player);
        cached.ifPresent(user -> {
            user.addLoss();
            user.addMatch(matchData);
        });
        cached = userDataManager.get(winner);
        cached.ifPresent(user -> {
            user.addWin();
            user.addMatch(matchData);
        });

        // Set loser as spectator
        arena.setUsed(false);
    }

    
    // TODO: 16/05/2018 Handle case of arena player damaging non-arena player
    @EventHandler (ignoreCancelled = true)
    public void on(final EntityDamageByEntityEvent event) {

    }


    // TODO: 16/05/2018 Handle case of player logging out while in match
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
