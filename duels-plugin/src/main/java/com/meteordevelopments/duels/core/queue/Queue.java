package com.meteordevelopments.duels.core.queue;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.api.kit.Kit;
import com.meteordevelopments.duels.api.queue.DQueue;
import com.meteordevelopments.duels.gui.BaseButton;
import com.meteordevelopments.duels.gui.configuration.GuiItemConfig;
import com.meteordevelopments.duels.util.inventory.ItemBuilder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Queue extends BaseButton implements DQueue {

    @Getter
    private final String name;
    @Getter
    private final Kit kit;
    @Getter
    private final int bet;
    @Getter
    private final int teamSize;
    @Getter
    private final List<QueueEntry> players = new LinkedList<>();
    @Getter
    @Setter(value = AccessLevel.PACKAGE)
    private boolean removed;

    public Queue(final DuelsPlugin plugin, final Kit kit, final int bet) {
        this(plugin, "Unnamed", kit, bet, 1);
    }

    public Queue(final DuelsPlugin plugin, final String name, final Kit kit, final int bet) {
        this(plugin, name, kit, bet, 1);
    }

    public Queue(final DuelsPlugin plugin, final Kit kit, final int bet, final int teamSize) {
        this(plugin, "Unnamed", kit, bet, teamSize);
    }

    public Queue(final DuelsPlugin plugin, final String name, final Kit kit, final int bet, final int teamSize) {
        super(plugin, buildDefaultItem(plugin, name, kit, bet, Math.max(1, teamSize), 0, 0));
        this.name = name;
        this.kit = kit;
        this.bet = bet;
        this.teamSize = Math.max(1, teamSize);
    }

    private static ItemStack buildDefaultItem(final DuelsPlugin plugin, final String name, final Kit kit, final int bet, final int teamSize, final int inQueue, final long inMatch) {
        final Map<String, String> placeholders = new HashMap<>();
        placeholders.put("name", name);
        placeholders.put("kit", kit != null ? kit.getName() : plugin.getLang().getMessage("GENERAL.none"));
        placeholders.put("bet_amount", String.valueOf(bet));
        placeholders.put("team_size", String.valueOf(teamSize));
        placeholders.put("in_queue", String.valueOf(inQueue));
        placeholders.put("in_match", String.valueOf(inMatch));

        if (plugin.getGuiConfigManager() != null && plugin.getGuiConfigManager().getQueueSelectGuiConfig() != null) {
            final GuiItemConfig queueBtn = plugin.getGuiConfigManager().getQueueSelectGuiConfig().getQueueButton();
            if (queueBtn != null && queueBtn.getName() != null) {
                final ItemStack item = queueBtn.buildItem(plugin.getLang(), queueBtn.isGlowing(), placeholders);
                if (plugin.getConfiguration().isInheritKitItemType() && kit != null) {
                    final ItemStack kitDisplay = kit.getDisplayed().clone();
                    kitDisplay.setItemMeta(item.getItemMeta());
                    return kitDisplay;
                }
                return item;
            }
        }

        final ItemStack base = (plugin.getConfiguration().isInheritKitItemType() && kit != null)
                ? kit.getDisplayed().clone()
                : ItemBuilder.of(Material.DIAMOND_SWORD).build();

        return ItemBuilder.of(base)
                .name(plugin.getLang().getMessage("GUI.queues.buttons.queue.name",
                        "name", name, "kit", kit != null ? kit.getName() : plugin.getLang().getMessage("GENERAL.none"), "bet_amount", bet, "in_queue", inQueue, "in_match", inMatch), plugin.getLang())
                .lore(plugin.getLang(), plugin.getLang().getMessage("GUI.queues.buttons.queue.lore",
                        "name", name, "kit", kit != null ? kit.getName() : plugin.getLang().getMessage("GENERAL.none"), "bet_amount", bet, "in_queue", inQueue, "in_match", inMatch).split("\n"))
                .build();
    }

    @Override
    public boolean isInQueue(@NotNull final Player player) {
        return players.stream().anyMatch(entry -> entry.getPlayer().equals(player));
    }

    @NotNull
    @Override
    public List<Player> getQueuedPlayers() {
        return players.stream().map(QueueEntry::getPlayer).toList();
    }

    void addPlayer(final QueueEntry entry) {
        players.add(entry);
        update();
        queueManager.getGui().calculatePages();
    }

    boolean removePlayer(final Player player) {
        if (players.removeIf(entry -> entry.getPlayer().equals(player))) {
            update();
            queueManager.getGui().calculatePages();
            return true;
        }

        return false;
    }

    boolean removeAll(final Set<QueueEntry> players) {
        if (this.players.removeAll(players)) {
            update();
            return true;
        }

        return false;
    }

    @Override
    public long getPlayersInMatch() {
        return arenaManager.getPlayersInMatch(this);
    }

    public void update() {
        int inQueue = players.size();
        long inMatch = getPlayersInMatch();

        if (plugin.getGuiConfigManager() != null && plugin.getGuiConfigManager().getQueueSelectGuiConfig() != null) {
            final GuiItemConfig queueBtn = plugin.getGuiConfigManager().getQueueSelectGuiConfig().getQueueButton();
            if (queueBtn != null && queueBtn.getName() != null) {
                final Map<String, String> placeholders = new HashMap<>();
                placeholders.put("name", name);
                placeholders.put("kit", kit != null ? kit.getName() : lang.getMessage("GENERAL.none"));
                placeholders.put("bet_amount", String.valueOf(bet));
                placeholders.put("team_size", String.valueOf(teamSize));
                placeholders.put("in_queue", String.valueOf(inQueue));
                placeholders.put("in_match", String.valueOf(inMatch));

                final ItemStack updated = queueBtn.buildItem(lang, queueBtn.isGlowing(), placeholders);
                if (plugin.getConfiguration().isInheritKitItemType() && kit != null) {
                    final ItemMeta meta = updated.getItemMeta();
                    final ItemStack kitDisplay = kit.getDisplayed().clone();
                    kitDisplay.setItemMeta(meta);
                    setDisplayed(kitDisplay);
                } else {
                    setDisplayed(updated);
                }
                return;
            }
        }

        setDisplayName(lang.getMessage("GUI.queues.buttons.queue.name",
                "name", name, "kit", kit != null ? kit.getName() : lang.getMessage("GENERAL.none"), "bet_amount", bet, "in_queue", inQueue, "in_match", inMatch), lang);
        setLore(lang, lang.getMessage("GUI.queues.buttons.queue.lore",
                "name", name, "kit", kit != null ? kit.getName() : lang.getMessage("GENERAL.none"), "bet_amount", bet, "in_queue", inQueue, "in_match", inMatch).split("\n"));
    }

    @Override
    public void onClick(final Player player) {
        queueManager.addToQueue(player, this);
    }

    @Override
    public String toString() {
        return name + " (" + (kit != null ? kit.getName() : "none") + " $" + bet + ")";
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        final Queue queue = (Queue) other;
        return bet == queue.bet && teamSize == queue.teamSize && Objects.equals(name, queue.name) && Objects.equals(kit, queue.kit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, kit, bet, teamSize);
    }
}
