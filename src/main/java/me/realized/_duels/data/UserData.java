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

package me.realized._duels.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import me.realized._duels.configuration.ConfigManager;
import me.realized._duels.configuration.ConfigType;
import me.realized._duels.configuration.MainConfig;

public class UserData {

    private final UUID uuid;
    private String name;
    private int wins = 0;
    private int losses = 0;
    private boolean requests = true;
    private List<MatchData> matches = new ArrayList<>();
    private Map<String, Object> data;

    public UserData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean canRequest() {
        return requests;
    }

    public void setRequestEnabled(boolean requests) {
        this.requests = requests;
    }

    public List<MatchData> getMatches() {
        return matches;
    }

    public void addMatch(MatchData match) {
        matches.add(match);
        refreshMatches();
    }

    public void refreshMatches() {
        int maxMatches = ((MainConfig) ConfigManager.getConfig(ConfigType.MAIN)).getStatsAmountOfMatches();

        if (maxMatches < 0) {
            maxMatches = 0;
        }

        if (matches.size() >= maxMatches) {
            matches = new ArrayList<>(matches.subList(matches.size() - maxMatches, matches.size()));
        }
    }

    final Object addData(String key, Object value) {
        if (data == null) {
            data = new HashMap<>();
        }

        return data.put(key, value);
    }

    final boolean removeData(String key) {
        return data != null && data.remove(key) != null;

    }

    public int get(StatsType type) {
        switch (type) {
            case WINS:
                return wins;
            case LOSSES:
                return losses;
            default:
                return 0;
        }
    }

    public void edit(EditType editType, StatsType statsType, int amount) {
        switch (editType) {
            case ADD:
                switch (statsType) {
                    case WINS:
                        wins += amount;
                        break;
                    case LOSSES:
                        losses += amount;
                        break;
                }

                break;
            case REMOVE:
                switch (statsType) {
                    case WINS:
                        wins -= amount;
                        break;
                    case LOSSES:
                        losses -= amount;
                        break;
                }

                break;
            case SET:
                switch (statsType) {
                    case WINS:
                        wins = amount;
                        break;
                    case LOSSES:
                        losses = amount;
                        break;
                }

                break;
        }
    }

    public enum StatsType {

        WINS,
        LOSSES;

        public static boolean isValue(String input) {
            for (StatsType type : values()) {
                if (type.name().equals(input)) {
                    return true;
                }
            }

            return false;
        }
    }

    public enum EditType {

        ADD,
        REMOVE,
        SET;

        public static boolean isValue(String input) {
            for (EditType type : values()) {
                if (type.name().equals(input)) {
                    return true;
                }
            }

            return false;
        }
    }
}
