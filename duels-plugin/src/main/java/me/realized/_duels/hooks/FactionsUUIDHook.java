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

package me.realized._duels.hooks;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.event.PowerLossEvent;
import me.realized._duels.Core;
import me.realized._duels.configuration.MainConfig;
import me.realized._duels.utilities.Storage;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class FactionsUUIDHook extends PluginHook implements Listener {

    private final MainConfig config;

    public FactionsUUIDHook(Core instance) {
        super("Factions");
        this.config = instance.getConfiguration();

        if (isEnabled()) {
            Bukkit.getPluginManager().registerEvents(this, instance);
        }
    }

    @EventHandler
    public void onPowerLoss(PowerLossEvent event) {
        if (!config.isPatchesDisablePowerLoss()) {
            return;
        }

        FPlayer player = event.getfPlayer();
        Storage storage = Storage.get(player.getPlayer());
        Object value = storage.get("matchDeath");

        if (value != null) {
            event.setMessage("");
            event.setCancelled(true);
        }
    }
}
