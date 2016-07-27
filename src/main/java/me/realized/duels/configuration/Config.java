package me.realized.duels.configuration;

import me.realized.duels.Core;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {

    private static Config instance = null;

    private final boolean fixVanish;
    private final boolean fixBack;
    private final boolean fixTeleport;
    private final boolean fixInventoryOpen;

    private final boolean cdEnabled;
    private final List<String> cdMessages;
    private final boolean cdProjectile;
    private final boolean cdPvp;

    private final int kitSelector;
    private final int arenaSelector;
    private final String availableArenaDisplayName;
    private final String inUseArenaDisplayName;

    private final boolean mecEnabled;
    private final List<String> mecCommands;
    private final boolean allowArenaSelecting;
    private final boolean useOwnInventory;
    private final boolean onlyEmptyInventory;
    private final boolean teleportToLastLoc;
    private final int teleportationDelay;
    private final boolean dropItem;
    private final boolean pickUpItem;
    private final boolean blockAllCommands;
    private final List<String> whitelistedCommands;
    private final List<String> disabledCommands;

    private final boolean dzEnabled;
    private final String dzRegion;

    private final boolean displayMatches;

    private final Map<String, Object> messages = new HashMap<>();

    public Config(Core instance) {
        Config.instance = this;
        File base = new File(instance.getDataFolder(), "config.yml");

        if (!base.exists()) {
            instance.saveResource("config.yml", true);
            instance.info("Generated configuration file.");
        }

        FileConfiguration config = instance.getConfig();

        this.fixVanish = config.getBoolean("patcher.fix-vanish", false);
        this.fixBack = config.getBoolean("patcher.fix-back", true);
        this.fixTeleport = config.getBoolean("patcher.fix-teleport", true);
        this.fixInventoryOpen = config.getBoolean("patcher.fix-inventory-open", true);

        this.cdEnabled = config.getBoolean("countdown.enabled", true);
        this.cdMessages = config.isList("countdown.messages") ? config.getStringList("countdown.messages") : new ArrayList<String>();

        this.cdProjectile = config.getBoolean("countdown.block-projectile", true);
        this.cdPvp = config.getBoolean("countdown.block-pvp", true);

        this.kitSelector = config.getInt("GUI.kit-selector", 3);
        this.arenaSelector = config.getInt("GUI.arena-selector", 2);
        this.availableArenaDisplayName = config.getString("GUI.available-arena-displayname", "&9{NAME}: &aAvailable");
        this.inUseArenaDisplayName = config.getString("GUI.in-use-arena-displayname", "&9{NAME}: &cIn Use");

        this.mecEnabled = config.getBoolean("Dueling.match-end-commands.enabled", false);
        this.mecCommands = config.isList("Dueling.match-end-commands.commands") ? config.getStringList("Dueling.match-end-commands.commands") : new ArrayList<String>();
        this.allowArenaSelecting = config.getBoolean("Dueling.allow-arena-selecting", true);
        this.useOwnInventory = config.getBoolean("Dueling.use-own-inventory", false);
        this.onlyEmptyInventory = config.getBoolean("Dueling.requires-cleared-inventory", true);
        this.teleportToLastLoc = config.getBoolean("Dueling.teleport-to-lastest-location", false);
        this.teleportationDelay = config.getInt("Dueling.delay-until-teleport-on-win", 5);
        this.dropItem = config.getBoolean("Dueling.drop-item", true);
        this.pickUpItem = config.getBoolean("Dueling.pickup-item", false);
        this.blockAllCommands = config.getBoolean("Dueling.block-all-commands", false);
        this.whitelistedCommands = config.isList("Dueling.whitelisted-commands") ? config.getStringList("Dueling.whitelisted-commands") : new ArrayList<String>();
        this.disabledCommands = config.isList("Dueling.disabled-commands") ? config.getStringList("Dueling.disabled-commands") : new ArrayList<String>();

        this.dzEnabled = config.getBoolean("DuelZone.enabled", false);
        this.dzRegion = config.getString("DuelZone.region", "spawn");

        this.displayMatches = config.getBoolean("Stats.display-matches", true);

        for (String key : config.getConfigurationSection("Messages").getKeys(false)) {
            Object result = config.get("Messages." + key);

            if (result instanceof String) {
                result = ((String) result).replace("{NEWLINE}", "\n");
            }

            messages.put(key, result);
        }
    }

    public boolean fixVanish() {
        return fixVanish;
    }

    public boolean fixBack() {
        return fixBack;
    }

    public boolean fixTeleport() {
        return fixTeleport;
    }

    public boolean fixInventoryOpen() {
        return fixInventoryOpen;
    }

    public boolean isCdEnabled() {
        return cdEnabled;
    }

    public List<String> getCdMessages() {
        return cdMessages;
    }

    public boolean isCdProjectileBlocked() {
        return cdProjectile;
    }

    public boolean isCdPvPBlocked() {
        return cdPvp;
    }

    public int kitSelectorRows() {
        return kitSelector;
    }

    public int arenaSelectorRows() {
        return arenaSelector;
    }

    public String getAvailableArenaDisplayName() {
        return availableArenaDisplayName;
    }

    public String getInUseArenaDisplayName() {
        return inUseArenaDisplayName;
    }

    public boolean isMECEnabled() {
        return mecEnabled;
    }

    public List<String> getMECommands() {
        return mecCommands;
    }

    public boolean isAllowArenaSelecting() {
        return allowArenaSelecting;
    }

    public boolean isUseOwnInventory() {
        return useOwnInventory;
    }

    public boolean isOnlyEmptyInventory() {
        return onlyEmptyInventory;
    }

    public boolean isTeleportToLastLoc() {
        return teleportToLastLoc;
    }

    public int getTeleportationDelay() {
        return teleportationDelay;
    }

    public boolean isDropItem() {
        return dropItem;
    }

    public boolean isPickUpItem() {
        return pickUpItem;
    }

    public boolean isBlockAllCommands() {
        return blockAllCommands;
    }

    public List<String> getWhitelistedCommands() {
        return whitelistedCommands;
    }

    public List<String> getDisabledCommands() {
        return disabledCommands;
    }

    public boolean isDZEnabled() {
        return dzEnabled;
    }

    public String getDZRegion() {
        return dzRegion;
    }

    public boolean isDisplayMatches() {
        return displayMatches;
    }

    public String getString(String key) {
        return (String) messages.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String key, Class<T> type) {
        return (List<T>) messages.get(key);
    }

    public static Config getInstance() {
        return instance;
    }
}
