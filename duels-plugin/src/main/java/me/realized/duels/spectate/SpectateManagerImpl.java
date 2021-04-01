package me.realized.duels.spectate;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.api.arena.Arena;
import me.realized.duels.api.event.spectate.SpectateEndEvent;
import me.realized.duels.api.event.spectate.SpectateStartEvent;
import me.realized.duels.api.spectate.SpectateManager;
import me.realized.duels.api.spectate.Spectator;
import me.realized.duels.arena.ArenaImpl;
import me.realized.duels.arena.ArenaManagerImpl;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.hook.hooks.MyPetHook;
import me.realized.duels.player.PlayerInfo;
import me.realized.duels.player.PlayerInfoManager;
import me.realized.duels.teleport.Teleport;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class SpectateManagerImpl implements Loadable, SpectateManager {

    // The location of the stop spec item in player's hotbar.
    private static final int STOP_SPEC_ITEM_SLOT = 8;

    private final DuelsPlugin plugin;
    private final Config config;
    private final Lang lang;
    private final ArenaManagerImpl arenaManager;
    private final PlayerInfoManager playerManager;

    // Map UUID to a spectator
    private final Map<UUID, SpectatorImpl> spectators = new HashMap<>();
    // Map arena to a collection of spectators
    private final Multimap<Arena, SpectatorImpl> arenas = HashMultimap.create();

    private Teleport teleport;
    private MyPetHook myPet;

    public SpectateManagerImpl(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.arenaManager = plugin.getArenaManager();
        this.playerManager = plugin.getPlayerManager();

        Bukkit.getPluginManager().registerEvents(new SpectateListener(), plugin);
    }

    @Override
    public void handleLoad() {
        // Late-init since SpectateManager is loaded before below variables are loaded
        this.teleport = plugin.getTeleport();
        this.myPet = plugin.getHookManager().getHook(MyPetHook.class);
    }

    @Override
    public void handleUnload() {
        spectators.clear();
    }

    @Nullable
    @Override
    public SpectatorImpl get(@Nonnull final Player player) {
        Objects.requireNonNull(player, "player");
        return spectators.get(player.getUniqueId());
    }

    @Override
    public boolean isSpectating(@Nonnull final Player player) {
        Objects.requireNonNull(player, "player");
        return get(player) != null;
    }

    @Nonnull
    @Override
    public Result startSpectating(@Nonnull final Player player, @Nonnull final Player target) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(target, "target");

        if (isSpectating(player)) {
            return Result.ALREADY_SPECTATING;
        }

        if (plugin.getQueueManager().isInQueue(player)) {
            return Result.IN_QUEUE;
        }

        if (arenaManager.isInMatch(player)) {
            return Result.IN_MATCH;
        }

        final ArenaImpl arena = arenaManager.get(target);

        if (arena == null) {
            return Result.TARGET_NOT_IN_MATCH;
        }

        final SpectatorImpl spectator = new SpectatorImpl(player, target, arena);
        final SpectateStartEvent event = new SpectateStartEvent(player, spectator);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return Result.EVENT_CANCELLED;
        }

        // Remove pet before teleport
        if (myPet != null) {
            myPet.removePet(player);
        }

        // Cache current player state to return to after spectating is over
        playerManager.create(player);
        PlayerUtil.reset(player);

        // Teleport before putting in map to prevent teleport being cancelled
        teleport.tryTeleport(player, target.getLocation().clone().add(0, 2, 0));
        spectators.put(player.getUniqueId(), spectator);
        arenas.put(arena, spectator);
        player.setGameMode(GameMode.SPECTATOR);

        // Broadcast to the arena that player has begun spectating if player does not have the SPEC_ANON permission.
        if (!player.hasPermission(Permissions.SPEC_ANON)) {
            arena.getMatch().getAllPlayers().forEach(matchPlayer -> lang.sendMessage(matchPlayer, "SPECTATE.arena-broadcast", "name", player.getName()));
        }

        return Result.SUCCESS;
    }

    /**
     * Puts player out of spectator mode.
     *
     * @param player Player to put out of spectator mode
     * @param spectator {@link SpectatorImpl} instance associated to this player
     */
    public void stopSpectating(final Player player, final SpectatorImpl spectator) {
        spectators.remove(player.getUniqueId());
        arenas.remove(spectator.getArena(), spectator);
        player.setGameMode(GameMode.SURVIVAL);
        PlayerUtil.reset(player);

        final PlayerInfo info = playerManager.remove(player);

        if (info != null) {
            teleport.tryTeleport(player, info.getLocation());
            info.restore(player);
        } else {
            teleport.tryTeleport(player, playerManager.getLobby());
        }

        final SpectateEndEvent event = new SpectateEndEvent(player, spectator);
        Bukkit.getPluginManager().callEvent(event);
    }

    /**
     * Puts player out of spectator mode.
     *
     * @see #stopSpectating(Player, SpectatorImpl)
     */
    @Override
    public void stopSpectating(@Nonnull final Player player) {
        Objects.requireNonNull(player, "player");
        stopSpectating(player, get(player));
    }

    /**
     * Puts all spectators of the given {@link ArenaImpl} out of spectator mode.
     *
     * @param arena {@link ArenaImpl} to end spectating
     */
    public void stopSpectating(final ArenaImpl arena) {
        final Collection<SpectatorImpl> spectators = arenas.asMap().remove(arena);

        if (spectators == null || spectators.isEmpty()) {
            return;
        }

        spectators.forEach(spectator -> {
            final Player player = Bukkit.getPlayer(spectator.getUuid());

            if (player == null) {
                return;
            }

            stopSpectating(player, spectator);
            lang.sendMessage(player, "SPECTATE.match-end");
        });
    }

    @Nonnull
    @Override
    public List<Spectator> getSpectators(@Nonnull final Arena arena) {
        Objects.requireNonNull(arena, "arena");
        return Collections.unmodifiableList(Lists.newArrayList(getSpectatorsImpl(arena)));
    }

    public Collection<SpectatorImpl> getSpectatorsImpl(final Arena arena) {
        return arenas.asMap().getOrDefault(arena, Collections.emptyList());
    }

    public Collection<Player> getAllSpectators() {
        return spectators.values()
            .stream()
            .map(spectator -> Bukkit.getPlayer(spectator.getUuid()))
            .collect(Collectors.toList());
    }

    private class SpectateListener implements Listener {

        @EventHandler
        public void on(final PlayerQuitEvent event) {
            final Player player = event.getPlayer();
            final SpectatorImpl spectator = get(player);

            if (spectator == null) {
                return;
            }

            stopSpectating(player, spectator);
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final PlayerCommandPreprocessEvent event) {
            final Player player = event.getPlayer();

            if (!isSpectating(player)) {
                return;
            }

            final String command = event.getMessage().substring(1).split(" ")[0].toLowerCase();

            if (command.equalsIgnoreCase("spectate") || command.equalsIgnoreCase("spec") || config.getSpecWhitelistedCommands().contains(command)) {
                return;
            }

            event.setCancelled(true);
            lang.sendMessage(player, "SPECTATE.prevent.command", "command", event.getMessage());
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final PlayerTeleportEvent event) {
            final Player player = event.getPlayer();
            final SpectatorImpl spectator = get(player);

            if (spectator == null) {
                return;
            }

            event.setCancelled(true);
            lang.sendMessage(player, "SPECTATE.prevent.teleportation");
        }
    }
}
