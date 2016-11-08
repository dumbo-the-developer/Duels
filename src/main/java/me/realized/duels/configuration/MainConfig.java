package me.realized.duels.configuration;

import me.realized.duels.Core;
import me.realized.duels.utilities.config.Config;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class MainConfig extends Config {

    private boolean patchesToggleVanishOnStart;
    private boolean patchesSetBackLocation;
    private boolean patchesCancelTeleportToPlayersInMatch;
    private boolean patchesFixInventoryOpen;
    private boolean patchesDisableMcMMOInMatch;
    private boolean patchesDisablePowerLoss;
    private boolean patchesStrictTeleportation;
    private boolean patchesForceAllowPvp;
    private boolean patchesForceAllowTeleportation;
    private boolean patchesCancelMatchIfMoved;

    private boolean countdownEnabled;
    private List<String> countdownMessages;
    private boolean countdownBlockProjectile;
    private boolean countdownBlockPvp;

    private int guiKitSelectorRows;
    private int guiArenaSelectorRows;
    private String guiAvailableArenaDisplayname;
    private String guiInUseArenaDisplayname;

    private List<String> spectatingWhitelistedCommands;

    private boolean duelingMatchMaxDurationEnabled;
    private int duelingMatchMaxDurationDuration;
    private boolean duelingMatchEndCommandsEnabled;
    private List<String> duelingMatchEndCommandsCommands;
    private boolean duelingAllowArenaSelecting;
    private boolean duelingUseOwnInventory;
    private boolean duelingRequiresClearedInventory;
    private boolean duelingTeleportToLatestLocation;
    private int duelingDelayUntilTeleport;
    private boolean duelingDropItem;
    private boolean duelingPickUpItem;
    private boolean duelingBlockAllCommands;
    private List<String> duelingWhitelistedCommands;
    private List<String> duelingDisabledCommands;

    private boolean duelZoneEnabled;
    private String duelZoneRegion;

    private boolean statsDisplayMatches;
    private int statsAmountOfMatches;

    private boolean soupEnabled;
    private String soupArenasStartingWith;
    private double soupHeartsToRegen;

    private final Map<String, CustomSound> sounds = new HashMap<>();

    public MainConfig(Core instance) {
        super("config.yml", instance);
        handleLoad();
    }

    @Override
    public void handleLoad() {
        sounds.clear();

        this.patchesToggleVanishOnStart = base.getBoolean("Patches.toggle-vanish-on-start", false);
        this.patchesSetBackLocation = base.getBoolean("Patches.set-back-location", true);
        this.patchesCancelTeleportToPlayersInMatch = base.getBoolean("Patches.cancel-teleport-to-players-in-match", true);
        this.patchesFixInventoryOpen = base.getBoolean("Patches.fix-inventory-open", true);
        this.patchesDisableMcMMOInMatch = base.getBoolean("Patches.disable-mcMMO-in-match", true);
        this.patchesDisablePowerLoss = base.getBoolean("Patches.disable-power-loss", true);
        this.patchesStrictTeleportation = base.getBoolean("Patches.strict-teleportation", false);
        this.patchesForceAllowPvp = base.getBoolean("Patches.force-allow-pvp", true);
        this.patchesForceAllowTeleportation = base.getBoolean("Patches.force-allow-teleportation", true);
        this.patchesCancelMatchIfMoved = base.getBoolean("Patches.cancel-match-if-moved", false);

        this.countdownEnabled = base.getBoolean("Countdown.enabled", true);
        this.countdownMessages = base.isList("Countdown.messages") ? base.getStringList("Countdown.messages") : new ArrayList<String>();
        this.countdownBlockProjectile = base.getBoolean("Countdown.block-projectile", true);
        this.countdownBlockPvp = base.getBoolean("Countdown.block-pvp", true);

        this.guiKitSelectorRows = base.getInt("GUI.kit-selector-rows", 3);
        this.guiArenaSelectorRows = base.getInt("GUI.arena-selector-rows", 2);
        this.guiAvailableArenaDisplayname = base.getString("GUI.available-arena-displayname", "&9{NAME}: &aAvailable");
        this.guiInUseArenaDisplayname = base.getString("GUI.in-use-arena-displayname", "&9{NAME}: &cIn Use");

        this.spectatingWhitelistedCommands = base.isList("Spectating.whitelisted-commands") ? base.getStringList("Spectating.whitelisted-commands") : new ArrayList<String>();

        this.duelingMatchMaxDurationEnabled = base.getBoolean("Dueling.match-max-duration.enabled", true);
        this.duelingMatchMaxDurationDuration = base.getInt("Dueling.match-max-duration.duration", 600);
        this.duelingMatchEndCommandsEnabled = base.getBoolean("Dueling.match-end-commands.enabled", false);
        this.duelingMatchEndCommandsCommands = base.isList("Dueling.match-end-commands.commands") ? base.getStringList("Dueling.match-end-commands.commands") : new ArrayList<String>();
        this.duelingAllowArenaSelecting = base.getBoolean("Dueling.allow-arena-selecting", true);
        this.duelingUseOwnInventory = base.getBoolean("Dueling.use-own-inventory", false);
        this.duelingRequiresClearedInventory = base.getBoolean("Dueling.requires-cleared-inventory", true);
        this.duelingTeleportToLatestLocation = base.getBoolean("Dueling.teleport-to-latest-location", false);
        this.duelingDelayUntilTeleport = base.getInt("Dueling.delay-until-teleport", 5);
        this.duelingDropItem = base.getBoolean("Dueling.drop-item", true);
        this.duelingPickUpItem = base.getBoolean("Dueling.pick-up-item", true);
        this.duelingBlockAllCommands = base.getBoolean("Dueling.block-all-commands", false);
        this.duelingWhitelistedCommands = base.isList("Dueling.whitelisted-commands") ? base.getStringList("Dueling.whitelisted-commands") : new ArrayList<String>();
        this.duelingDisabledCommands = base.isList("Dueling.disabled-commands") ? base.getStringList("Dueling.disabled-commands") : new ArrayList<String>();

        this.duelZoneEnabled = base.getBoolean("DuelZone.enabled", false);
        this.duelZoneRegion = base.getString("DuelZone.region", "spawn");

        this.statsDisplayMatches = base.getBoolean("Stats.display-matches", true);
        this.statsAmountOfMatches = base.getInt("Stats.amount-of-matches", 10);

        this.soupEnabled = base.getBoolean("Soup.enabled", false);
        this.soupArenasStartingWith = base.getString("Soup.arenas-starting-with", "soup arena");
        this.soupHeartsToRegen = base.getDouble("Soup.hearts-to-regen", 3.5);

        if (base.isConfigurationSection("Sounds")) {
            for (String name : base.getConfigurationSection("Sounds").getKeys(false)) {

                if (!base.isConfigurationSection("Sounds." + name + ".sound")) {
                    continue;
                }

                ConfigurationSection soundSection = base.getConfigurationSection("Sounds." + name + ".sound");
                String type = soundSection.getString("type");
                Sound sound = null;

                for (Sound soundInstance : Sound.values()) {
                    if (soundInstance.name().equalsIgnoreCase(type)) {
                        sound = soundInstance;
                        break;
                    }
                }

                if (sound == null) {
                    continue;
                }

                double pitch = soundSection.getDouble("pitch");
                double volume = soundSection.getDouble("volume");
                sounds.put(name, new CustomSound(sound, pitch, volume, base.getStringList("Sounds." + name + ".on-messages")));
            }
        }
    }

    public boolean isPatchesToggleVanishOnStart() {
        return patchesToggleVanishOnStart;
    }

    public boolean isPatchesSetBackLocation() {
        return patchesSetBackLocation;
    }

    public boolean isPatchesCancelTeleportToPlayersInMatch() {
        return patchesCancelTeleportToPlayersInMatch;
    }

    public boolean isPatchesFixInventoryOpen() {
        return patchesFixInventoryOpen;
    }

    public boolean isPatchesDisableMcMMOInMatch() {
        return patchesDisableMcMMOInMatch;
    }

    public boolean isPatchesDisablePowerLoss() {
        return patchesDisablePowerLoss;
    }

    public boolean isPatchesStrictTeleportation() {
        return patchesStrictTeleportation;
    }

    public boolean isPatchesForceAllowPvp() {
        return patchesForceAllowPvp;
    }

    public boolean isPatchesForceAllowTeleportation() {
        return patchesForceAllowTeleportation;
    }

    public boolean isPatchesCancelMatchIfMoved() {
        return patchesCancelMatchIfMoved;
    }

    public boolean isCountdownEnabled() {
        return countdownEnabled;
    }

    public List<String> getCountdownMessages() {
        return countdownMessages;
    }

    public boolean isCountdownBlockProjectile() {
        return countdownBlockProjectile;
    }

    public boolean isCountdownBlockPvp() {
        return countdownBlockPvp;
    }

    public int getGuiKitSelectorRows() {
        return guiKitSelectorRows;
    }

    public int getGuiArenaSelectorRows() {
        return guiArenaSelectorRows;
    }

    public String getGuiAvailableArenaDisplayname() {
        return guiAvailableArenaDisplayname;
    }

    public String getGuiInUseArenaDisplayname() {
        return guiInUseArenaDisplayname;
    }

    public List<String> getSpectatingWhitelistedCommands() {
        return spectatingWhitelistedCommands;
    }

    public boolean isDuelingMatchMaxDurationEnabled() {
        return duelingMatchMaxDurationEnabled;
    }

    public int getDuelingMatchMaxDurationDuration() {
        return duelingMatchMaxDurationDuration;
    }

    public boolean isDuelingMatchEndCommandsEnabled() {
        return duelingMatchEndCommandsEnabled;
    }

    public List<String> getDuelingMatchEndCommandsCommands() {
        return duelingMatchEndCommandsCommands;
    }

    public boolean isDuelingAllowArenaSelecting() {
        return duelingAllowArenaSelecting;
    }

    public boolean isDuelingUseOwnInventory() {
        return duelingUseOwnInventory;
    }

    public boolean isDuelingRequiresClearedInventory() {
        return duelingRequiresClearedInventory;
    }

    public boolean isDuelingTeleportToLatestLocation() {
        return duelingTeleportToLatestLocation;
    }

    public int getDuelingDelayUntilTeleport() {
        return duelingDelayUntilTeleport;
    }

    public boolean isDuelingDropItem() {
        return duelingDropItem;
    }

    public boolean isDuelingPickUpItem() {
        return duelingPickUpItem;
    }

    public boolean isDuelingBlockAllCommands() {
        return duelingBlockAllCommands;
    }

    public List<String> getDuelingWhitelistedCommands() {
        return duelingWhitelistedCommands;
    }

    public List<String> getDuelingDisabledCommands() {
        return duelingDisabledCommands;
    }

    public boolean isDuelZoneEnabled() {
        return duelZoneEnabled;
    }

    public String getDuelZoneRegion() {
        return duelZoneRegion;
    }

    public boolean isStatsDisplayMatches() {
        return statsDisplayMatches;
    }

    public int getStatsAmountOfMatches() {
        return statsAmountOfMatches;
    }

    public boolean isSoupEnabled() {
        return soupEnabled;
    }

    public String getSoupArenasStartingWith() {
        return soupArenasStartingWith;
    }

    public double getSoupHeartsToRegen() {
        return soupHeartsToRegen;
    }

    public CustomSound getSound(String name) {
        return sounds.get(name);
    }

    public Collection<CustomSound> getSounds() {
        return sounds.values();
    }

    public class CustomSound {

        private final Sound sound;
        private final double pitch;
        private final double volume;
        private final List<String> messages;

        CustomSound(Sound sound, double pitch, double volume, List<String> messages) {
            this.sound = sound;
            this.pitch = pitch;
            this.volume = volume;
            this.messages = messages;
        }

        public void handleMessage(Player player, String msg) {
            if (messages.contains(msg)) {
                play(player);
            }
        }

        public void play(Player player) {
            player.playSound(player.getLocation(), sound, (float) volume, (float) pitch);
        }
    }
}
