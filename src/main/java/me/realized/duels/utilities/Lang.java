package me.realized.duels.utilities;

import java.util.Arrays;
import java.util.List;

public enum Lang {

    ARENA_CREATE("&7Arena &f{NAME} &7was successfully created.", false),
    ARENA_DELETE("&7Arena &f{NAME} &7was successfully deleted.", false),
    ARENA_INFO(Arrays.asList(
            "&b&m------------------------------------",
            "&7Displaying info of arena &f{NAME} &7-",
            "&7Disabled: &f{DISABLED}", "&7In use: &f{IN_USE}",
            "&7Players: &f{PLAYERS}",
            "&7Locations: &f{LOCATIONS}",
            "&b&m------------------------------------"), false),
    ARENA_TOGGLE_ON("&a&lArena {NAME} is now enabled!", false),
    ARENA_TOGGLE_OFF("&c&lArena {NAME} is now disabled!", false),
    ARENA_SET_POSITION("&7Set spawn position for player &f{POSITION} &7for arena &f{NAME}&7.", false),

    KIT_CREATE("&7Saved kit &f{NAME}&7!", false),
    KIT_DELETE("&7Deleted kit &f{NAME}&7!", false),
    KIT_LOAD("&7Loaded kit &f{NAME} &7to your inventory.", false),

    LIST(Arrays.asList(
            "&b&m------------------------------------",
            "&7Arena name color states:",
            "&4DARK RED&7: Arena is disabled.",
            "&9BLUE&7: Arena without valid spawn positions set.",
            "&aGREEN&7: Arena available for a match.",
            "&cRED&7: Arena in use.",
            "&7Arenas: &r{ARENAS}",
            "&7Kits: &r{KITS}",
            "&7Lobby Location: &r{LOBBY}",
            "&b&m------------------------------------"), false),

    EDIT("&7Action executed (&f{PLAYER}&7): &e{ACTION}", false),
    SET_LOBBY("&7Lobby set on your current location.", false),
    ADMIN_COMMAND_USAGE("&e{USAGE} &7- {DESC}", true);

    private String message = null;
    private List<String> messages = null;
    private final boolean excludePrefix;

    Lang(String message, boolean excludePrefix) {
        this.message = message;
        this.excludePrefix = excludePrefix;
    }

    Lang(List<String> messages, boolean excludePrefix) {
        this.messages = messages;
        this.excludePrefix = excludePrefix;
    }

    public String getMessage() {
        return (excludePrefix ? "" : "&9[Duels] &r") + message;
    }

    public List<String> getMessages() {
        return messages;
    }
}
