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

package me.realized._duels.configuration;

import java.util.HashMap;
import java.util.Map;
import me.realized._duels.utilities.Reloadable;
import me.realized._duels.utilities.config.Config;

public class ConfigManager implements Reloadable {

    private static ConfigManager instance;

    private final Map<ConfigType, Config> configurations = new HashMap<>();

    public ConfigManager() {
        instance = this;
    }

    public static Config getConfig(ConfigType type) {
        return instance.getConfigByType(type);
    }

    public void register(ConfigType type, Config config) {
        configurations.put(type, config);
    }

    public Config getConfigByType(ConfigType type) {
        return configurations.get(type);
    }

    @Override
    public void handleReload(ReloadType type) {
        switch (type) {
            case WEAK:
                getConfigByType(ConfigType.MESSAGES).reload(true);
                break;
            case STRONG:
                for (Config config : configurations.values()) {
                    config.reload(true);
                }
        }
    }
}
