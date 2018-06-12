package me.realized._duels.hooks;

import java.util.HashMap;
import java.util.Map;

public class HookManager {

    private final Map<String, PluginHook> hooks = new HashMap<>();

    public void register(String name, PluginHook hook) {
        hooks.put(name, hook);
    }

    public PluginHook get(String name) {
        return hooks.get(name);
    }
}
