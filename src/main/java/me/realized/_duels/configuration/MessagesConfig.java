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
import java.util.List;
import java.util.Map;
import me.realized._duels.Core;
import me.realized._duels.utilities.config.Config;
import org.bukkit.configuration.MemorySection;

public class MessagesConfig extends Config {

    private final Map<String, String> strings = new HashMap<>();
    private final Map<String, List<String>> lists = new HashMap<>();

    public MessagesConfig(Core instance) {
        super("messages.yml", instance);
        handleLoad();
    }

    @Override
    public void handleLoad() {
        // Clearing in case of configuration reload
        strings.clear();
        lists.clear();

        for (String path : base.getKeys(true)) {
            Object value = base.get(path);

            if (value == null || value instanceof MemorySection) {
                continue;
            }

            if (value instanceof String && !((String) value).isEmpty()) {
                strings.put(path, (String) value);
            } else if (value instanceof List && !((List) value).isEmpty()) {
                lists.put(path, base.getStringList(path));
            }
        }
    }

    public String getString(String path) {
        return strings.get(path);
    }

    public List<String> getList(String path) {
        return lists.get(path);
    }
}
