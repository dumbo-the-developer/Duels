/*
 * This file is part of Duels, licensed under the MIT License.
 *
 * Copyright (c) Realized
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
    private boolean allowArenaSelecting;
    @Getter
    private boolean allowItemBetting;
    @Getter
    private boolean allowMoneyBetting;
    @Getter
    private boolean useOwnInventoryEnabled;
    @Getter
    private boolean useOwnInventoryKeepItems;
    @Getter
    private boolean requiresClearedInventory;
    @Getter
    private int maxDuration;
    @Getter
    private boolean endCommandsEnabled;
    @Getter
    private List<String> endCommands;
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
    private boolean specRequiresClearedInventory;
    @Getter
    private List<String> specWhitelistedCommands;

    @Getter
    private boolean cdEnabled;
    @Getter
    private List<String> cdMessages;
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
        allowArenaSelecting = configuration.getBoolean("duel.allow-arena-selecting", true);
        allowItemBetting = configuration.getBoolean("duel.allow-item-betting", true);
        allowMoneyBetting = configuration.getBoolean("duel.allow-money-betting", true);
        useOwnInventoryEnabled = configuration.getBoolean("duel.use-own-inventory.enabled", false);
        useOwnInventoryKeepItems = configuration.getBoolean("duel.use-own-inventory.keep-items", false);
        requiresClearedInventory = configuration.getBoolean("duel.requires-cleared-inventory", true);
        maxDuration = configuration.getInt("duel.match.max-duration", -1);
        endCommandsEnabled = configuration.getBoolean("duel.match.end-commands.enabled", false);
        endCommands = configuration.getStringList("duel.match.end-commands.commands");
        teleportToLastLocation = configuration.getBoolean("duel.teleport-to-last-location", false);
        teleportDelay = configuration.getInt("duel.teleport-delay", 5);
        preventItemDrop = configuration.getBoolean("duel.prevent-item-drop", false);
        preventItemPickup = configuration.getBoolean("duel.prevent-item-pickup", true);
        limitTeleportEnabled = configuration.getBoolean("duel.limit-teleportation.enabled", true);
        distanceAllowed = configuration.getDouble("duel.limit-teleportation.distance-allowed", 5.0);
        blockAllCommands = configuration.getBoolean("duel.block-all-commands", false);
        whitelistedCommands = configuration.getStringList("duel.whitelisted-commands");
        blacklistedCommands = configuration.getStringList("duel.blacklisted-commands");
        specRequiresClearedInventory = configuration.getBoolean("spectate.requires-cleared-inventory", false);
        specWhitelistedCommands = configuration.getStringList("spectate.whitelisted-commands");
        cdEnabled = configuration.getBoolean("countdown.enabled", true);
        cdMessages = configuration.getStringList("countdown.messages");
        preventMovement = configuration.getBoolean("countdown.prevent.movement", true);
        preventLaunchProjectile = configuration.getBoolean("countdown.prevent.launch-projectile", true);
        preventPvp = configuration.getBoolean("countdown.prevent.pvp", true);
    }
}
