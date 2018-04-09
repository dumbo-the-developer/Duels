package me.realized._duels.extension;

import com.google.common.io.Files;
import me.realized._duels.Core;

import java.io.File;

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

    public File getDataFolder() {
        return folder;
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

    final void init(Core instance, File folder, File base) {
        this.instance = instance;
        this.folder = folder;
        this.base = base;
    }

    public abstract void onEnable();

    public abstract void onDisable();
}
