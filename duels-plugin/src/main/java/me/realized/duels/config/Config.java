package me.realized.duels.config;

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
    private boolean ctpPreventDuel;
    @Getter
    private boolean ctpUntag;
    @Getter
    private boolean pmPreventDuel;
    @Getter
    private boolean pmUntag;
    @Getter
    private boolean autoUnvanish;
    @Getter
    private boolean setBackLocation;
    @Getter
    private boolean disableSkills;
    @Getter
    private boolean fuNoPowerLoss;
    @Getter
    private boolean fNoPowerLoss;
    @Getter
    private boolean duelzoneEnabled;
    @Getter
    private List<String> duelzones;
    @Getter
    private boolean myPetDespawn;
    @Getter
    private boolean preventBountyLoss;
    @Getter
    private boolean preventAddDeath;

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
    private boolean startCommandsEnabled;
    @Getter
    private List<String> startCommands;
    @Getter
    private boolean endCommandsEnabled;
    @Getter
    private List<String> endCommands;
    @Getter
    private boolean projectileHitMessageEnabled;
    @Getter
    private List<String> projectileHitMessageTypes;
    @Getter
    private boolean preventInventoryOpen;
    @Getter
    private boolean removeEmptyBottle;
    @Getter
    private boolean preventTpToMatchPlayers;
    @Getter
    private boolean forceAllowCombat;
    @Getter
    private boolean cancelIfMoved;
    @Getter
    private boolean teleportToLastLocation;
    @Getter
    private int teleportDelay;
    @Getter
    private boolean spawnFirework;
    @Getter
    private boolean arenaOnlyEndMessage;
    @Getter
    private boolean displayInventories;
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
    private String topWinsType;
    @Getter
    private String topWinsIdentifier;
    @Getter
    private String topLossesType;
    @Getter
    private String topLossesIdentifier;
    @Getter
    private String topKitType;
    @Getter
    private String topKitIdentifier;

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

    private final Map<String, MessageSound> sounds = new HashMap<>();

    public Config(final DuelsPlugin plugin) {
        super(plugin, "config");
    }

    @Override
    protected void loadValues(FileConfiguration configuration) throws Exception {
        if (configuration.getInt("config-version", 0) < getLatestVersion()) {
            configuration = convert(null);
        }

        version = configuration.getInt("config-version");
        checkForUpdates = configuration.getBoolean("check-for-updates", true);

        ctpPreventDuel = configuration.getBoolean("supported-plugins.CombatTagPlus.prevent-duel-if-tagged", true);
        ctpUntag = configuration.getBoolean("supported-plugins.CombatTagPlus.untag-on-duel-teleport", true);
        pmPreventDuel = configuration.getBoolean("supported-plugins.PvPManager.prevent-duel-if-tagged", true);
        pmUntag = configuration.getBoolean("supported-plugins.PvPManager.untag-on-duel-teleport", true);
        autoUnvanish = configuration.getBoolean("supported-plugins.Essentials.auto-unvanish", true);
        setBackLocation = configuration.getBoolean("supported-plugins.Essentials.set-back-location", true);
        disableSkills = configuration.getBoolean("supported-plugins.mcMMO.disable-skills-in-duel", true);
        fuNoPowerLoss = configuration.getBoolean("supported-plugins.FactionsUUID.no-power-loss-in-duel", true);
        fNoPowerLoss = configuration.getBoolean("supported-plugins.Factions.no-power-loss-in-duel", true);
        duelzoneEnabled = configuration.getBoolean("supported-plugins.WorldGuard.duelzone.enabled", false);
        duelzones = configuration.getStringList("supported-plugins.WorldGuard.duelzone.regions");
        myPetDespawn = configuration.getBoolean("supported-plugins.MyPet.despawn-pet-in-duel", false);
        preventBountyLoss = configuration.getBoolean("supported-plugins.BountyHunters.prevent-bounty-loss-in-duel", true);
        preventAddDeath = configuration.getBoolean("supported-plugins.SimpleClans.prevent-add-death-in-duel", true);
        requiresClearedInventory = configuration.getBoolean("request.requires-cleared-inventory", true);
        preventCreativeMode = configuration.getBoolean("request.prevent-creative-mode", false);
        arenaSelectingEnabled = configuration.getBoolean("request.arena-selecting.enabled", true);
        arenaSelectingUsePermission = configuration.getBoolean("request.arena-selecting.use-permission", false);
        itemBettingEnabled = configuration.getBoolean("request.item-betting.enabled", true);
        itemBettingUsePermission = configuration.getBoolean("request.item-betting.use-permission", false);
        moneyBettingEnabled = configuration.getBoolean("request.money-betting.enabled", true);
        moneyBettingUsePermission = configuration.getBoolean("request.money-betting.use-permission", false);
        expiration = Math.max(configuration.getInt("request.expiration", 30), 0);

        useOwnInventoryEnabled = configuration.getBoolean("duel.use-own-inventory.enabled", false);
        useOwnInventoryKeepItems = configuration.getBoolean("duel.use-own-inventory.keep-items", false);
        maxDuration = configuration.getInt("duel.match.max-duration", -1);
        startCommandsEnabled = configuration.getBoolean("duel.match.start-commands.enabled", false);
        startCommands = configuration.getStringList("duel.match.start-commands.commands");
        endCommandsEnabled = configuration.getBoolean("duel.match.end-commands.enabled", false);
        endCommands = configuration.getStringList("duel.match.end-commands.commands");
        projectileHitMessageEnabled = configuration.getBoolean("duel.projectile-hit-message.enabled", true);
        projectileHitMessageTypes = configuration.getStringList("duel.projectile-hit-message.types");
        preventInventoryOpen = configuration.getBoolean("duel.prevent-inventory-open", true);
        removeEmptyBottle = configuration.getBoolean("duel.remove-empty-bottle", true);
        preventTpToMatchPlayers = configuration.getBoolean("duel.prevent-teleport-to-match-players", true);
        forceAllowCombat = configuration.getBoolean("duel.force-allow-combat", true);
        cancelIfMoved = configuration.getBoolean("duel.cancel-if-moved", false);
        teleportToLastLocation = configuration.getBoolean("duel.teleport-to-last-location", false);
        teleportDelay = configuration.getInt("duel.teleport-delay", 5);
        spawnFirework = configuration.getBoolean("duel.spawn-firework", true);
        arenaOnlyEndMessage = configuration.getBoolean("duel.arena-only-end-message", false);
        displayInventories = configuration.getBoolean("duel.display-inventories", true);
        preventItemDrop = configuration.getBoolean("duel.prevent-item-drop", false);
        preventItemPickup = configuration.getBoolean("duel.prevent-item-pickup", true);
        limitTeleportEnabled = configuration.getBoolean("duel.limit-teleportation.enabled", true);
        distanceAllowed = configuration.getDouble("duel.limit-teleportation.distance-allowed", 5.0);
        blockAllCommands = configuration.getBoolean("duel.block-all-commands", false);
        whitelistedCommands = configuration.getStringList("duel.whitelisted-commands");
        blacklistedCommands = configuration.getStringList("duel.blacklisted-commands");

        ratingEnabled = configuration.getBoolean("rating.enabled", true);
        kFactor = Math.max(configuration.getInt("rating.k-factor", 32), 1);
        defaultRating = Math.max(configuration.getInt("rating.default-rating", 1400), 0);
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
        matchesToDisplay = Math.max(configuration.getInt("stats.matches-to-display", 10), 0);

        topUpdateInterval = Math.max(configuration.getInt("top.update-interval", 5), 1) * 60L * 1000L;
        topWinsType = configuration.getString("top.displayed-replacers.wins.type", "Wins");
        topWinsIdentifier = configuration.getString("top.displayed-replacers.wins.identifier", "wins");
        topLossesType = configuration.getString("top.displayed-replacers.losses.type", "Losses");
        topLossesIdentifier = configuration.getString("top.displayed-replacers.losses.identifier", "losses");
        topKitType = configuration.getString("top.displayed-replacers.kit.type", "%kit%");
        topKitIdentifier = configuration.getString("top.displayed-replacers.kit.identifier", "rating");

        kitSelectorRows = Math.min(Math.max(configuration.getInt("guis.kit-selector.rows", 2), 1), 5);
        arenaSelectorRows = Math.min(Math.max(configuration.getInt("guis.arena-selector.rows", 3), 1), 5);

        soupEnabled = configuration.getBoolean("soup.enabled", true);
        nameStartingWith = configuration.getString("soup.arena-name-starting-with", "soup arena");
        heartsToRegen = Math.max(configuration.getDouble("soup.hearts-to-regen", 3.5), 0);

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
