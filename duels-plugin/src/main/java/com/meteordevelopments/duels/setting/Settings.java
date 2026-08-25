package com.meteordevelopments.duels.setting;

import com.meteordevelopments.duels.party.Party;
import lombok.Getter;
import lombok.Setter;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.core.arena.ArenaImpl;
import com.meteordevelopments.duels.gui.settings.SettingsGui;
import com.meteordevelopments.duels.core.kit.KitImpl;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Settings {

    private final DuelsPlugin plugin;
    private final SettingsGui gui;

    @Getter
    private UUID target;
    @Getter
    private KitImpl kit;
    @Getter
    private com.meteordevelopments.duels.api.customkit.CustomKit customKit;
    @Getter
    @Setter
    private com.meteordevelopments.duels.api.customkit.CustomKitSnapshot customKitSnapshot;
    @Getter
    @Setter
    private ArenaImpl arena;
    @Getter
    @Setter
    private int bet;
    @Getter
    private boolean itemBetting;
    @Getter
    private boolean ownInventory;
    @Getter
    private Map<UUID, CachedInfo> cache = new HashMap<>();
    @Getter
    @Setter
    private Party senderParty;
    @Getter
    @Setter
    private Party targetParty;

    public Settings(final DuelsPlugin plugin, final Player player) {
        this.plugin = plugin;
        this.gui = player != null ? plugin.getGuiListener().addGui(player, new SettingsGui(plugin)) : null;
        // If kits are disabled, then ownInventory is enabled by default.
        this.ownInventory = !plugin.getConfiguration().isKitSelectingEnabled();
    }

    public Settings(final DuelsPlugin plugin) {
        this(plugin, null);
    }

    public void reset() {
        target = null;
        senderParty = null;
        targetParty = null;
        kit = null;
        customKit = null;
        customKitSnapshot = null;
        arena = null;
        bet = 0;
        itemBetting = false;
        ownInventory = !plugin.getConfiguration().isKitSelectingEnabled();
        clearCache();
    }

    public void setTarget(final Player target) {
        if (this.target != null && !this.target.equals(target.getUniqueId())) {
            reset();
        }

        this.target = target.getUniqueId();
    }

    public void updateGui(final Player player) {
        if (gui != null) {
            gui.update(player);
        }
    }

    public void clearCache() {
        cache.clear();
    }

    public void openGui(final Player player) {
        gui.open(player);
    }

    public void setBaseLoc(final Player player) {
        cache.computeIfAbsent(player.getUniqueId(), result -> new CachedInfo()).setLocation(player.getLocation().clone());
    }

    public Location getBaseLoc(final Player player) {
        final CachedInfo info = cache.get(player.getUniqueId());

        if (info == null) {
            return null;
        }

        return info.getLocation();
    }

    public void setDuelzone(final Player player, final String duelzone) {
        cache.computeIfAbsent(player.getUniqueId(), result -> new CachedInfo()).setDuelzone(duelzone);
    }

    public String getDuelzone(final Player player) {
        final CachedInfo info = cache.get(player.getUniqueId());

        if (info == null) {
            return null;
        }

        return info.getDuelzone();
    }

    public boolean isPartyDuel() {
        return senderParty != null && targetParty != null;
    }

    public void setItemBetting(final boolean itemBetting) {
        // Player-created custom kit duels ONLY support money betting.
        if (this.customKit != null) {
            this.itemBetting = false;
            return;
        }
        this.itemBetting = itemBetting;
    }

    public void setKit(final KitImpl kit) {
        this.kit = kit;
        this.customKit = null;
        this.customKitSnapshot = null;
        this.ownInventory = false;
    }

    public void setCustomKit(final com.meteordevelopments.duels.api.customkit.CustomKit customKit) {
        this.customKit = customKit;
        this.kit = null;
        this.customKitSnapshot = customKit != null ? customKit.toSnapshot() : null;
        this.ownInventory = false;
        // Enforce money betting only for custom kit duels
        this.itemBetting = false;
    }

    public void setOwnInventory(final boolean ownInventory) {
        this.ownInventory = ownInventory;

        if (ownInventory) {
            this.kit = null;
            this.customKit = null;
            this.customKitSnapshot = null;
        }
    }

    // Don't copy the gui since it won't be required to start a match
    public Settings lightCopy() {
        final Settings copy = new Settings(plugin);
        copy.target = target;
        copy.senderParty = senderParty;
        copy.targetParty = targetParty;
        copy.kit = kit;
        copy.customKit = customKit;
        copy.customKitSnapshot = customKitSnapshot;
        copy.arena = arena;
        copy.bet = bet;
        copy.itemBetting = itemBetting;
        copy.ownInventory = ownInventory;
        copy.cache = new HashMap<>(cache);
        return copy;
    }
}
