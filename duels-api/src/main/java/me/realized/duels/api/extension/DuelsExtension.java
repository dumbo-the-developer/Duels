package me.realized.duels.api.extension;

import java.io.File;
import lombok.Getter;
import me.realized.duels.api.Duels;

public abstract class DuelsExtension {

    protected Duels api;

    @Getter
    private boolean enabled;
    @Getter
    private File folder;
    @Getter
    private File file;

    final void init(final Duels api, final File folder, final File file) {
        this.api = api;
        this.folder = folder;
        this.file = file;
    }

    public final String getName() {
        return getClass().getSimpleName();
    }

    public final void setEnabled(final boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }

        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }

        this.enabled = enabled;
    }

    public void onEnable() {}

    public void onDisable() {}
}
