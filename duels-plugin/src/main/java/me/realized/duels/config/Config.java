package me.realized.duels.config;

import java.util.List;
import lombok.Getter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.config.AbstractConfiguration;
import org.bukkit.configuration.file.FileConfiguration;

public class Config extends AbstractConfiguration<DuelsPlugin> {

    @Getter
    private int version;
    @Getter
    private boolean checkForUpdates;

    @Getter
    private boolean arenaSelectingEnabled;
    @Getter
    private boolean arenaSelectingUsePermission;
    @Getter
    private boolean itemBettingEnabled;
    @Getter
    private boolean itemBettingUsePermission;
    @Getter
    private boolean moneyBettingEnabled;
    @Getter
    private boolean moneyBettingUsePermission;
    @Getter
    private boolean useOwnInventoryEnabled;
    @Getter
    private boolean useOwnInventoryKeepItems;
    @Getter
    private boolean requiresClearedInventory;
    @Getter
    private boolean preventCreativeMode;
    @Getter
    private int maxDuration;
    @Getter
    private boolean endCommandsEnabled;
    @Getter
    private List<String> endCommands;
    @Getter
    private boolean preventInventoryOpen;
    @Getter
    private boolean removeEmptyBottle;
    @Getter
    private boolean preventTpToMatchPlayers;
    @Getter
    private boolean preventMcMMO;
    @Getter
    private boolean forceAllowCombat;
    @Getter
    private boolean forceUnvanish;
    @Getter
    private boolean teleportToLastLocation;
    @Getter
    private int teleportDelay;
    @Getter
    private boolean preventItemDrop;
    @Getter
    private boolean preventItemPickup;
    @Getter
    private boolean limitTeleportEnabled;
    @Getter
    private double distanceAllowed;
    @Getter
    private boolean blockAllCommands;
    @Getter
    private List<String> whitelistedCommands;
    @Getter
    private List<String> blacklistedCommands;

    @Getter
    private int kFactor;
    @Getter
    private int defaultRating;
    @Getter
    private boolean queueMatchesOnly;

    @Getter
    private boolean specRequiresClearedInventory;
    @Getter
    private List<String> specWhitelistedCommands;

    @Getter
    private boolean cdEnabled;
    @Getter
    private List<String> cdMessages;
    @Getter
    private List<String> titles;
    @Getter
    private boolean preventMovement;
    @Getter
    private boolean preventLaunchProjectile;
    @Getter
    private boolean preventPvp;

    public Config(final DuelsPlugin plugin) {
        super(plugin, "_config");
    }

    @Override
    protected void loadValues(final FileConfiguration configuration) {
        version = configuration.getInt("config-version");
        checkForUpdates = configuration.getBoolean("check-for-updates", true);
        arenaSelectingEnabled = configuration.getBoolean("duel.arena-selecting.enabled", true);
        arenaSelectingUsePermission = configuration.getBoolean("duel.arena-selecting.use-permission", false);
        itemBettingEnabled = configuration.getBoolean("duel.item-betting.enabled", true);
        itemBettingUsePermission = configuration.getBoolean("duel.item-betting.use-permission", false);
        moneyBettingEnabled = configuration.getBoolean("duel.money-betting.enabled", true);
        moneyBettingUsePermission = configuration.getBoolean("duel.money-betting.use-permission", false);

        useOwnInventoryEnabled = configuration.getBoolean("duel.use-own-inventory.enabled", false);
        useOwnInventoryKeepItems = configuration.getBoolean("duel.use-own-inventory.keep-items", false);
        requiresClearedInventory = configuration.getBoolean("duel.requires-cleared-inventory", true);
        preventCreativeMode = configuration.getBoolean("duel.prevent-creative-mode", true);
        maxDuration = configuration.getInt("duel.match.max-duration", -1);
        endCommandsEnabled = configuration.getBoolean("duel.match.end-commands.enabled", false);
        endCommands = configuration.getStringList("duel.match.end-commands.commands");
        preventInventoryOpen = configuration.getBoolean("duel.prevent-inventory-open", true);
        removeEmptyBottle = configuration.getBoolean("duel.remove-empty-bottle", true);
        preventTpToMatchPlayers = configuration.getBoolean("duel.prevent-teleport-to-match-players", true);
        preventMcMMO = configuration.getBoolean("duel.prevent-mcmmo-skills", true);
        forceAllowCombat = configuration.getBoolean("duel.force-allow-combat", true);
        forceUnvanish = configuration.getBoolean("duel.force-unvanish", true);
        teleportToLastLocation = configuration.getBoolean("duel.teleport-to-last-location", false);
        teleportDelay = configuration.getInt("duel.teleport-delay", 5);
        preventItemDrop = configuration.getBoolean("duel.prevent-item-drop", false);
        preventItemPickup = configuration.getBoolean("duel.prevent-item-pickup", true);
        limitTeleportEnabled = configuration.getBoolean("duel.limit-teleportation.enabled", true);
        distanceAllowed = configuration.getDouble("duel.limit-teleportation.distance-allowed", 5.0);
        blockAllCommands = configuration.getBoolean("duel.block-all-commands", false);
        whitelistedCommands = configuration.getStringList("duel.whitelisted-commands");
        blacklistedCommands = configuration.getStringList("duel.blacklisted-commands");
        kFactor = configuration.getInt("rating.k-factor", 32);
        defaultRating = configuration.getInt("rating.default-rating", 1400);
        queueMatchesOnly = configuration.getBoolean("rating.queue-matches-only", true);
        specRequiresClearedInventory = configuration.getBoolean("spectate.requires-cleared-inventory", false);
        specWhitelistedCommands = configuration.getStringList("spectate.whitelisted-commands");
        cdEnabled = configuration.getBoolean("countdown.enabled", true);
        cdMessages = configuration.getStringList("countdown.messages");
        titles = configuration.getStringList("countdown.titles");
        preventMovement = configuration.getBoolean("countdown.prevent.movement", true);
        preventLaunchProjectile = configuration.getBoolean("countdown.prevent.launch-projectile", true);
        preventPvp = configuration.getBoolean("countdown.prevent.pvp", true);
    }
}
