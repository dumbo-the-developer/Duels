package com.meteordevelopments.duelsffa.command;

import com.meteordevelopments.duels.api.Duels;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class CommandRegistrar {

    private final Duels api;
    private CommandMap commandMap;

    public CommandRegistrar(final Duels api) {
        this.api = api;
    }

    public boolean register(final Command command) {
        CommandMap map = getCommandMap();
        if (map == null) {
            api.warn("[DuelsFFA Extension] Unable to access CommandMap.");
            return false;
        }
        if (map instanceof SimpleCommandMap) {
            Command existing = ((SimpleCommandMap) map).getCommand(command.getName());
            if (existing != null && existing != command) {
                api.warn("[DuelsFFA Extension] Command '/" + command.getName() + "' is already registered by another plugin.");
                return true;
            }
        }

        boolean registered = map.register("duels", command);
        if (!registered) {
            api.warn("[DuelsFFA Extension] Failed to register '/" + command.getName() + "'.");
        }
        return registered;
    }

    public void unregister(final Command command) {
        CommandMap map = getCommandMap();
        if (map == null) {
            return;
        }
        command.unregister(map);
        removeKnownCommands(command, map);
    }

    private CommandMap getCommandMap() {
        if (commandMap != null) {
            return commandMap;
        }
        try {
            Method getCommandMap = api.getServer().getClass().getMethod("getCommandMap");
            Object result = getCommandMap.invoke(api.getServer());
            if (result instanceof CommandMap) {
                commandMap = (CommandMap) result;
                return commandMap;
            }
        } catch (Exception ignored) {
        }
        try {
            Field commandMapField = api.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            Object result = commandMapField.get(api.getServer());
            if (result instanceof CommandMap) {
                commandMap = (CommandMap) result;
                return commandMap;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void removeKnownCommands(final Command command, final CommandMap map) {
        if (!(map instanceof SimpleCommandMap)) {
            return;
        }
        try {
            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            Object value = knownCommandsField.get(map);
            if (!(value instanceof Map)) {
                return;
            }
            @SuppressWarnings("unchecked")
            Map<String, Command> known = (Map<String, Command>) value;
            known.entrySet().removeIf(entry -> entry.getValue() == command);
        } catch (Exception ignored) {
        }
    }
}
