package com.meteordevelopments.duels.arena;

import com.meteordevelopments.duels.countdown.DuelCountdown;
import com.meteordevelopments.duels.countdown.party.PartyDuelCountdown;
import com.meteordevelopments.duels.match.DuelMatch;
import com.meteordevelopments.duels.match.party.PartyDuelMatch;
import com.meteordevelopments.duels.party.Party;
import com.meteordevelopments.duels.spectate.SpectatorImpl;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.api.arena.Arena;
import com.meteordevelopments.duels.api.event.arena.ArenaSetPositionEvent;
import com.meteordevelopments.duels.api.event.arena.ArenaStateChangeEvent;
import com.meteordevelopments.duels.api.event.match.MatchEndEvent;
import com.meteordevelopments.duels.api.event.match.MatchEndEvent.Reason;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.kit.KitImpl;
import com.meteordevelopments.duels.queue.Queue;
import com.meteordevelopments.duels.setting.Settings;
import com.meteordevelopments.duels.util.compat.Items;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
public class ArenaImpl extends BaseButton implements Arena {

    private final String name;
    private boolean disabled;
    private final Set<KitImpl> kits = new HashSet<>();
    private final Map<Integer, Location> positions = new HashMap<>();
    private DuelMatch match;
    @Setter(value = AccessLevel.PACKAGE)
    private boolean removed;
    @Setter
    private DuelCountdown countdown;

    public ArenaImpl(final DuelsPlugin plugin, final String name, final boolean disabled) {
        super(plugin, ItemBuilder
                .of(Items.EMPTY_MAP)
                .name(plugin.getLang().getMessage("GUI.arena-selector.buttons.arena.name", "name", name))
                .lore(plugin.getLang().getMessage("GUI.arena-selector.buttons.arena.lore-unavailable").split("\n"))
                .build()
        );
        this.name = name;
        this.disabled = disabled;
    }

    public ArenaImpl(final DuelsPlugin plugin, final String name) {
        this(plugin, name, false);
    }

    public void refreshGui(final boolean available) {
        setLore(lang.getMessage("GUI.arena-selector.buttons.arena.lore-" + (available ? "available" : "unavailable")).split("\n"));
        arenaManager.getGui().calculatePages();
    }

    @Nullable
    @Override
    public Location getPosition(final int pos) {
        return positions.get(pos);
    }

    @Override
    public boolean setPosition(@Nullable final Player source, final int pos, @NotNull final Location location) {
        Objects.requireNonNull(location, "location");

        if (pos <= 0 || pos > 2) {
            return false;
        }

        final ArenaSetPositionEvent event = new ArenaSetPositionEvent(source, this, pos, location);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        positions.put(event.getPos(), event.getLocation());
        arenaManager.saveArenas();
        refreshGui(isAvailable());
        return true;
    }

    @Override
    public boolean setPosition(final int pos, @NotNull final Location location) {
        return setPosition(null, pos, location);
    }

    @Override
    public boolean setDisabled(@Nullable final CommandSender source, final boolean disabled) {
        final ArenaStateChangeEvent event = new ArenaStateChangeEvent(source, this, disabled);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        this.disabled = event.isDisabled();
        arenaManager.saveArenas();
        refreshGui(isAvailable());
        return true;
    }

    @Override
    public boolean setDisabled(final boolean disabled) {
        return setDisabled(null, disabled);
    }

    public boolean isBoundless() {
        return kits.isEmpty();
    }

    public boolean isBound(@Nullable final KitImpl kit) {
        return kit != null && kits.contains(kit);
    }

    public void bind(final KitImpl kit) {
        if (isBound(kit)) {
            kits.remove(kit);
        } else {
            kits.add(kit);
        }
        arenaManager.saveArenas();
    }

    @Override
    public boolean isUsed() {
        return match != null;
    }

    public boolean isAvailable() {
        return !isDisabled() && !isUsed() && getPosition(1) != null && getPosition(2) != null;
    }

    public DuelMatch startMatch(final KitImpl kit, final Map<UUID, List<ItemStack>> items, final Settings settings, final Queue source) {
        this.match = settings.isPartyDuel() ? new PartyDuelMatch(plugin, this, kit, items, settings.getBet(), source) : new DuelMatch(plugin, this, kit, items, settings.getBet(), source);
        refreshGui(false);
        return match;
    }

    public void endMatch(final UUID winner, final UUID loser, final Reason reason) {
        spectateManager.stopSpectating(this);

        final MatchEndEvent event = new MatchEndEvent(match, winner, loser, reason);
        Bukkit.getPluginManager().callEvent(event);

        final Queue source = match.getSource();
        match.setFinished();

        for(Block block : match.placedBlocks) {
            block.setType(Material.AIR);
        }

        for(Map.Entry<Location, BlockData> map : match.brokenBlocks.entrySet()) {
            map.getKey().getBlock().setBlockData(map.getValue());
        }

        for (Entity entity : match.placedEntities){
            entity.remove();
        }

        for (Block block : match.liquids) {
            Location loc = block.getLocation();
            int radius = 1;

            while (true) {
                boolean waterFound = false;

                for (int x = -radius; x <= radius; x++) {
                    for (int y = -radius; y <= radius; y++) {
                        for (int z = -radius; z <= radius; z++) {
                            Block findBlock = loc.clone().add(x, y, z).getBlock();
                            String type = findBlock.getType().name().toLowerCase();

                            if (type.contains("water") || type.contains("lava") || type.contains("cobblestone") || type.contains("obsidian")) {
                                waterFound = true;
                                findBlock.setType(Material.AIR);
                            }
                        }
                    }
                }

                if (!waterFound) {
                    break;
                }

                radius++;
            }
        }

        if(config.isClearItemsAfterMatch()) {
            match.droppedItems.forEach(Entity::remove);
        }

        match = null;

        if (source != null) {
            source.update();
            queueManager.getGui().calculatePages();
        }

        refreshGui(true);
    }

    public void startCountdown() {
        this.countdown = match instanceof PartyDuelMatch ? new PartyDuelCountdown(plugin, this, (PartyDuelMatch) match) : new DuelCountdown(plugin, this, match);
        countdown.startCountdown(0L, 20L);
    }

    boolean isCounting() {
        return countdown != null;
    }

    @Override
    public boolean has(@NotNull final Player player) {
        Objects.requireNonNull(player, "player");
        return isUsed() && !match.isDead(player);
    }

    public void add(final Player player) {
        if (isUsed()) {
            match.addPlayer(player);
        }
    }

    public void remove(final Player player) {
        if (isUsed()) {
            match.markAsDead(player);
        }
    }

    public boolean isEndGame() {
        return size() <= 1;
    }

    public int size() {
        return isUsed() ? match.size() : 0;
    }

    public Player first() {
        return isUsed() ? match.getAlivePlayers().iterator().next() : null;
    }

    @Override
    public Player getOpponent(final Player player) {
        return isUsed() ? match.getAllPlayers().stream().filter(other -> !player.equals(other)).findFirst().orElse(null) : null;
    }

    public Party getOpponent(final Party party) {
        return isUsed() ? ((PartyDuelMatch) match).getAllParties().stream().filter(other -> !party.equals(other)).findFirst().orElse(null) : null;
    }

    public Set<Player> getPlayers() {
        return isUsed() ? match.getAllPlayers() : Collections.emptySet();
    }

    public void broadcast(final String message) {
        getPlayers().forEach(player -> player.sendMessage(message));
        spectateManager.getSpectatorsImpl(this).stream().map(SpectatorImpl::getPlayer).filter(Objects::nonNull).forEach(player -> player.sendMessage(message));
    }

    @Override
    public void onClick(final Player player) {
        if (!isAvailable()) {
            return;
        }

        final Settings settings = settingManager.getSafely(player);
        final String kitName = settings.getKit() != null ? settings.getKit().getName() : lang.getMessage("GENERAL.none");

        if (!arenaManager.isSelectable(settings.getKit(), this)) {
            lang.sendMessage(player, "ERROR.setting.arena-not-applicable", "kit", kitName, "arena", name);
            return;
        }

        settings.setArena(this);
        settings.openGui(player);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        final ArenaImpl arena = (ArenaImpl) other;
        return Objects.equals(name, arena.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
