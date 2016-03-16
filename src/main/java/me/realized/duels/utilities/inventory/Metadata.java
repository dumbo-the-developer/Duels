package me.realized.duels.utilities.inventory;

import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Metadata implements MetadataValue {

    private final JavaPlugin owner;
    private final Object value;

    public Metadata(JavaPlugin owner, Object value) {
        this.owner = owner;
        this.value = value;
    }

    @Override
    public Object value() {
        return value;
    }

    @Override
    public int asInt() {
        return 0;
    }

    @Override
    public float asFloat() {
        return 0;
    }

    @Override
    public double asDouble() {
        return 0;
    }

    @Override
    public long asLong() {
        return 0;
    }

    @Override
    public short asShort() {
        return 0;
    }

    @Override
    public byte asByte() {
        return 0;
    }

    @Override
    public boolean asBoolean() {
        return false;
    }

    @Override
    public String asString() {
        return (String) value;
    }

    @Override
    public Plugin getOwningPlugin() {
        return owner;
    }

    @Override
    public void invalidate() {

    }
}
