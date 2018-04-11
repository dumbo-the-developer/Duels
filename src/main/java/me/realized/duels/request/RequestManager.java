package me.realized.duels.request;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.UUID;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.Loadable;

public class RequestManager implements Loadable {

    private final DuelsPlugin plugin;
    private final Multimap<UUID, Request> requests = HashMultimap.create();

    public RequestManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handleLoad() throws Exception {

    }

    @Override
    public void handleUnload() throws Exception {

    }
}
