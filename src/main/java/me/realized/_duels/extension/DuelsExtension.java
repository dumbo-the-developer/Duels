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

package me.realized._duels.extension;

import com.google.common.io.Files;
import java.io.File;
import me.realized._duels.Core;

public abstract class DuelsExtension {

    private boolean enabled;

    private Core instance;
    private File folder;
    private File base;

    public final Core getInstance() {
        return instance;
    }

    public final String getName() {
        return Files.getNameWithoutExtension(base.getName());
    }

    public boolean isEnabled() {
        return enabled;
    }

    final void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;

        if (enabled) {
            onEnable();
            instance.info("Extension '" + getName() + "' is now enabled.");
        } else {
            onDisable();
            instance.info("Extension '" + getName() + "' is now disabled.");
        }
    }

    public File getDataFolder() {
        return folder;
    }

    final void init(Core instance, File folder, File base) {
        this.instance = instance;
        this.folder = folder;
        this.base = base;
    }

    public abstract void onEnable();

    public abstract void onDisable();
}
