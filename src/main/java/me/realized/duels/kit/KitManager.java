package me.realized.duels.kit;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.Loadable;

public class KitManager implements Loadable {

    private final DuelsPlugin plugin;

    public KitManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handleLoad() throws Exception {

    }

    @Override
    public void handleUnload() throws Exception {

    }
}
