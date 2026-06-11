package com.meteordevelopments.duelsffa.util;

import org.bukkit.Chunk;

import java.util.Objects;

public class ChunkLoc {

    private final int x;
    private final int z;

    public ChunkLoc(final int x, final int z) {
        this.x = x;
        this.z = z;
    }

    public ChunkLoc(final Chunk chunk) {
        this(chunk.getX(), chunk.getZ());
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkLoc chunkLoc = (ChunkLoc) o;
        return x == chunkLoc.x && z == chunkLoc.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }

    @Override
    public String toString() {
        return "{" + x + "," + z + "}";
    }
}
