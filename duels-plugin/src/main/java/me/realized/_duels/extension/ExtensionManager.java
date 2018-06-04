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
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import me.realized._duels.Core;
import me.realized._duels.utilities.Helper;
import me.realized._duels.utilities.Reloadable;

public class ExtensionManager implements Reloadable {

    private final Map<String, DuelsExtension> extensions = new HashMap<>();

    private final Core instance;
    private final File folder;

    public ExtensionManager(Core instance) {
        this.instance = instance;

        File folder = new File(instance.getDataFolder(), "extensions");

        if (folder.mkdir()) {
            instance.info("Generated extensions folder.");
        }

        this.folder = folder;
    }

    public void load() {
        try {
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    int i = name.lastIndexOf('.');

                    if (i > 0) {
                        if (name.substring(i).equals(".jar")) {
                            return true;
                        }
                    }

                    return false;
                }
            };

            File[] jars = folder.listFiles(filter);

            for (File file : jars) {
                DuelsExtension extension = init(file);

                if (extension == null) {
                    continue;
                }

                if (getExtension(extension.getName()) != null) {
                    instance.warn("Failed to load extension '" + file.getName() + "': An extension with that name already exists.");
                    continue;
                }

                extensions.put(extension.getName(), extension);
            }
        } catch (Exception e) {
            instance.warn("Failed to load extensions: " + e.getMessage());
        }
    }

    public void handleDisable() {
        for (DuelsExtension extension : extensions.values()) {
            extension.setEnabled(false);
        }

        extensions.clear();
    }

    public DuelsExtension getExtension(String name) {
        return extensions.get(name);
    }

    private DuelsExtension init(File jar) {
        try {
            URL url = jar.toURI().toURL();
            URLClassLoader classLoader = new URLClassLoader(new URL[]{url}, DuelsExtension.class.getClassLoader());
            JarInputStream inputStream = new JarInputStream(url.openStream());

            Class<?> extensionClass = null;

            while (true) {
                JarEntry entry = inputStream.getNextJarEntry();

                if (entry == null) {
                    break;
                }

                String name = entry.getName();

                if (name.endsWith(".class")) {
                    name = name.replace("/", ".");
                    String className = name.substring(0, name.lastIndexOf(".class"));

                    Class<?> _class = classLoader.loadClass(className);

                    if (DuelsExtension.class.isAssignableFrom(_class)) {
                        if (extensionClass != null) {
                            instance.warn("Failed to load extension '" + jar.getName() + "': Cannot have multiple classes extending DuelsExtension.");
                            return null;
                        }

                        extensionClass = _class;
                    }
                }
            }

            if (extensionClass == null) {
                instance.warn("Failed to load extension '" + jar.getName() + "': Failed to find a class extending DuelsExtension.");
                return null;
            }

            DuelsExtension extension = ((DuelsExtension) extensionClass.newInstance());
            extension.init(instance, new File(folder, Files.getNameWithoutExtension(jar.getName())), jar);
            extension.setEnabled(true);
            return extension;
        } catch (Exception e) {
            Helper.handleException(this, e);
            instance.warn("Failed to load extension '" + jar.getName() + "': " + e.getMessage());
            return null;
        }
    }

    @Override
    public void handleReload(ReloadType type) {
        if (type == ReloadType.STRONG) {
            handleDisable();
            load();
        }
    }
}
