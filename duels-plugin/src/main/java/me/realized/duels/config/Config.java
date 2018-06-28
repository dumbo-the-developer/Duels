package me.realized.duels.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.EnumUtil;
import me.realized.duels.util.config.AbstractConfiguration;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Config extends AbstractConfiguration<DuelsPlugin> {

    @Getter
    private int version;
    @Getter
    private boolean checkForUpdates;

    @Getter
    private boolean requiresClearedInventory;
    @Getter
    private boolean preventCreativeMode;
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
    private int expiration;

    @Getter
    private boolean useOwnInventoryEnabled;
    @Getter
    private boolean useOwnInventoryKeepItems;
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
    private boolean cancelIfMoved;
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
    private boolean ratingEnabled;
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

    @Getter
    private boolean displayRatings;
    @Getter
    private boolean displayPastMatches;
    @Getter
    private int matchesToDisplay;

    @Getter
    private long topUpdateInterval;

    @Getter
    private int kitSelectorRows;
    @Getter
    private int arenaSelectorRows;

    @Getter
    private boolean soupEnabled;
    @Getter
    private String nameStartingWith;
    @Getter
    private double heartsToRegen;

    @Getter
    private boolean duelZoneEnabled;
    @Getter
    private List<String> duelZoneRegions;

    private final Map<String, MessageSound> sounds = new HashMap<>();

    public Config(final DuelsPlugin plugin) {
        super(plugin, "config");
    }

    @Override
    protected void loadValues(FileConfiguration configuration) throws IOException {
        if (configuration.getInt("config-version", 0) < getLatestVersion()) {
            configuration = convert(null);
        }

        version = configuration.getInt("config-version");
        checkForUpdates = configuration.getBoolean("check-for-updates", true);
        requiresClearedInventory = configuration.getBoolean("request.requires-cleared-inventory", true);
        preventCreativeMode = configuration.getBoolean("request.prevent-creative-mode", false);
        arenaSelectingEnabled = configuration.getBoolean("request.arena-selecting.enabled", true);
        arenaSelectingUsePermission = configuration.getBoolean("request.arena-selecting.use-permission", false);
        itemBettingEnabled = configuration.getBoolean("request.item-betting.enabled", true);
        itemBettingUsePermission = configuration.getBoolean("request.item-betting.use-permission", false);
        moneyBettingEnabled = configuration.getBoolean("request.money-betting.enabled", true);
        moneyBettingUsePermission = configuration.getBoolean("request.money-betting.use-permission", false);
        expiration = configuration.getInt("request.expiration", 30);
        useOwnInventoryEnabled = configuration.getBoolean("duel.use-own-inventory.enabled", false);
        useOwnInventoryKeepItems = configuration.getBoolean("duel.use-own-inventory.keep-items", false);
        maxDuration = configuration.getInt("duel.match.max-duration", -1);
        endCommandsEnabled = configuration.getBoolean("duel.match.end-commands.enabled", false);
        endCommands = configuration.getStringList("duel.match.end-commands.commands");
        preventInventoryOpen = configuration.getBoolean("duel.prevent-inventory-open", true);
        removeEmptyBottle = configuration.getBoolean("duel.remove-empty-bottle", true);
        preventTpToMatchPlayers = configuration.getBoolean("duel.prevent-teleport-to-match-players", true);
        preventMcMMO = configuration.getBoolean("duel.prevent-mcmmo-skills", true);
        forceAllowCombat = configuration.getBoolean("duel.force-allow-combat", true);
        forceUnvanish = configuration.getBoolean("duel.force-unvanish", true);
        cancelIfMoved = configuration.getBoolean("duel.cancel-if-moved", false);
        teleportToLastLocation = configuration.getBoolean("duel.teleport-to-last-location", false);
        teleportDelay = configuration.getInt("duel.teleport-delay", 5);
        preventItemDrop = configuration.getBoolean("duel.prevent-item-drop", false);
        preventItemPickup = configuration.getBoolean("duel.prevent-item-pickup", true);
        limitTeleportEnabled = configuration.getBoolean("duel.limit-teleportation.enabled", true);
        distanceAllowed = configuration.getDouble("duel.limit-teleportation.distance-allowed", 5.0);
        blockAllCommands = configuration.getBoolean("duel.block-all-commands", false);
        whitelistedCommands = configuration.getStringList("duel.whitelisted-commands");
        blacklistedCommands = configuration.getStringList("duel.blacklisted-commands");
        ratingEnabled = configuration.getBoolean("rating.enabled", true);
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
        displayRatings = configuration.getBoolean("stats.display-ratings", true);
        displayPastMatches = configuration.getBoolean("stats.display-past-matches", true);
        matchesToDisplay = configuration.getInt("stats.matches-to-display", 10);
        topUpdateInterval = configuration.getInt("top.update-interval", 5) * 60L * 1000L;
        kitSelectorRows = Math.min(Math.max(configuration.getInt("guis.kit-selector.rows", 2), 1), 5);
        arenaSelectorRows = Math.min(Math.max(configuration.getInt("guis.arena-selector.rows", 3), 1), 5);
        soupEnabled = configuration.getBoolean("soup.enabled", true);
        nameStartingWith = configuration.getString("soup.arena-name-starting-with", "soup arena");
        heartsToRegen = configuration.getDouble("soup.hearts-to-regen", 3.5);
        duelZoneEnabled = configuration.getBoolean("duelzone.enabled", false);
        duelZoneRegions = configuration.getStringList("duelzone.regions");

        final ConfigurationSection sounds = configuration.getConfigurationSection("sounds");

        if (sounds != null) {
            for (final String name : sounds.getKeys(false)) {
                final ConfigurationSection sound = sounds.getConfigurationSection(name);
                final Sound type = EnumUtil.getByName(sound.getString("type"), Sound.class);

                if (type == null) {
                    continue;
                }

                this.sounds.put(name, new MessageSound(type, sound.getDouble("pitch"), sound.getDouble("volume"), sound.getStringList("trigger-messages")));
            }
        }
    }

    public void playSound(final Player player, final String message) {
        sounds.values().stream()
            .filter(sound -> sound.getMessages().contains(message))
            .forEach(sound -> player.playSound(player.getLocation(), sound.getType(), sound.getVolume(), sound.getPitch()));
    }

    public MessageSound getSound(final String name) {
        return sounds.get(name);
    }

    public Set<String> getSounds() {
        return sounds.keySet();
    }

    public class MessageSound {

        @Getter
        private final Sound type;
        @Getter
        private final float pitch, volume;
        @Getter
        private final List<String> messages;

        MessageSound(final Sound type, final double pitch, final double volume, final List<String> messages) {
            this.type = type;
            this.pitch = (float) pitch;
            this.volume = (float) volume;
            this.messages = messages;
        }
    }
}
