package me.realized.duels.commands;

import me.realized.duels.Core;
import me.realized.duels.utilities.StringUtil;
import org.bukkit.command.CommandSender;

public abstract class SubCommand {

    private final String name;
    private final String usage;
    private final String description;
    private final int length;
    private transient final Core instance = Core.getInstance();

    protected SubCommand(String name, String usage, String description, int length) {
        this.name = name;
        this.usage = usage;
        this.description = description;
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public String getUsage() {
        return usage;
    }

    public String getDescription() {
        return description;
    }

    public int length() {
        return length;
    }

    protected Core getInstance() {
        return instance;
    }

    protected void pm(CommandSender sender, String msg) {
        sender.sendMessage(StringUtil.color(msg));
    }

    public abstract void execute(CommandSender sender, String[] args);
}
