package com.meteordevelopments.duelsffa.arena;

import com.meteordevelopments.duels.api.Duels;
import com.meteordevelopments.duelsffa.FfaExtension;
import com.meteordevelopments.duelsffa.config.FfaConfig;
import com.meteordevelopments.duelsffa.selection.Selection;
import com.meteordevelopments.duelsffa.util.Callback;
import com.meteordevelopments.duelsffa.util.ChunkLoc;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class RegenZone {

    private final Duels api;
    private final FfaExtension extension;
    private final FfaConfig config;
    private final FfaArena arena;
    private final Set<ChunkLoc> chunks = new HashSet<>();
    private Location min;
    private Location max;
    private Region region;
    private File file;
    private final File regionMaskFile;
    private RegionMaskData regionMaskData;
    private boolean resetting;
    private final org.bukkit.World world;

    RegenZone(final FfaExtension extension, final Duels api, final FfaArena arena, final File folder, final Selection selection) {
        this.api = api;
        this.extension = extension;
        this.config = extension.getConfiguration();
        this.arena = arena;
        this.file = new File(folder, arena.getName() + ".schem");
        this.regionMaskFile = new File(folder, arena.getName() + ".mask");
        this.world = selection.getWorld();
        if (this.world == null) {
            throw new IllegalArgumentException("Selection world is null");
        }

        this.region = selection.toRegion();
        if (this.region == null) {
            throw new IllegalArgumentException("Selection region is null");
        }

        this.regionMaskData = RegionMaskData.fromRegion(this.region);
        File worldFile = new File(folder, arena.getName() + ".world");
        try {
            Files.write(worldFile.toPath(), this.world.getName().getBytes(), new OpenOption[0]);
        } catch (IOException e) {
            api.warn("[DuelsFFA Extension] Failed to save world name for zone " + arena.getName());
        }
        updateBoundsFromRegion();
        saveSchematic();
        saveRegionMask();
        loadChunks();
    }

    RegenZone(final FfaExtension extension, final Duels api, final FfaArena arena, final File file) {
        this.api = api;
        this.extension = extension;
        this.config = extension.getConfiguration();
        this.arena = arena;
        this.file = file;
        this.regionMaskFile = new File(file.getParentFile(), arena.getName() + ".mask");
        File worldFile = new File(file.getParentFile(), arena.getName() + ".world");
        org.bukkit.World loadedWorld = null;
        if (worldFile.exists()) {
            try {
                String worldName = new String(Files.readAllBytes(worldFile.toPath())).trim();
                loadedWorld = Bukkit.getWorld(worldName);
            } catch (IOException e) {
                api.warn("[DuelsFFA Extension] Failed to load world name for zone " + arena.getName());
            }
        }
        this.world = loadedWorld != null ? loadedWorld : Bukkit.getWorlds().get(0);
        loadRegionMask();
        loadSchematicBounds();
        loadChunks();
    }

    public String getName() {
        return arena.getName();
    }

    public Location getMin() {
        return min;
    }

    public Location getMax() {
        return max;
    }

    public Region getRegion() {
        return region;
    }

    public boolean isResetting() {
        return resetting;
    }

    public org.bukkit.World getWorld() {
        return min != null ? min.getWorld() : world;
    }

    public Set<ChunkLoc> getChunks() {
        return chunks;
    }

    public long getTotalBlocks() {
        if (regionMaskData != null) {
            return regionMaskData.getCount();
        }
        if (region != null) {
            return region.getVolume();
        }
        return (long) (max.getBlockX() - min.getBlockX() + 1) * (max.getBlockY() - min.getBlockY() + 1) * (max.getBlockZ() - min.getBlockZ() + 1);
    }

    private void saveRegionMask() {
        if (regionMaskData == null) {
            return;
        }

        try {
            regionMaskData.write(regionMaskFile);
        } catch (IOException e) {
            api.warn("[DuelsFFA Extension] Failed to save region mask for zone " + arena.getName());
        }
    }

    private void loadRegionMask() {
        if (!regionMaskFile.exists()) {
            regionMaskData = null;
            return;
        }

        try {
            regionMaskData = RegionMaskData.read(regionMaskFile);
        } catch (IOException e) {
            regionMaskData = null;
            api.warn("[DuelsFFA Extension] Failed to load region mask for zone " + arena.getName());
        }
    }

    private void saveSchematic() {
        try {
            World weWorld = BukkitAdapter.adapt(min.getWorld());
            Region copyRegion = region != null ? region : new CuboidRegion(
                    weWorld,
                    BlockVector3.at(min.getBlockX(), min.getBlockY(), min.getBlockZ()),
                    BlockVector3.at(max.getBlockX(), max.getBlockY(), max.getBlockZ())
            );
            BlockArrayClipboard clipboard = new BlockArrayClipboard(copyRegion);
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld)) {
                ForwardExtentCopy copy = new ForwardExtentCopy(editSession, copyRegion, clipboard, copyRegion.getMinimumPoint());
                copy.setCopyingEntities(config.isWeCopyEntities());
                copy.setCopyingBiomes(config.isWeCopyBiomes());
                Operations.complete(copy);
            }
            try (ClipboardWriter writer = BuiltInClipboardFormat.FAST.getWriter(Files.newOutputStream(file.toPath(), new OpenOption[0]))) {
                writer.write((Clipboard) clipboard);
            }
        } catch (IOException e) {
            api.warn("[DuelsFFA Extension] Failed to save schematic for zone " + arena.getName());
        }
    }

    private void loadSchematicBounds() {
        try {
            ClipboardFormat format = ClipboardFormats.findByFile(file);
            if (format == null) {
                return;
            }
            try (InputStream in = Files.newInputStream(file.toPath());
                 ClipboardReader reader = format.getReader(in)) {
                Clipboard clipboard = reader.read();
                this.region = clipboard.getRegion().clone();
                updateBoundsFromRegion();
            }
        } catch (IOException e) {
            api.warn("[DuelsFFA Extension] Failed to load schematic bounds for zone " + arena.getName());
        }
    }

    private void updateBoundsFromRegion() {
        if (region == null) {
            return;
        }

        BlockVector3 minPoint = region.getMinimumPoint();
        BlockVector3 maxPoint = region.getMaximumPoint();
        this.min = new Location(world, minPoint.x(), minPoint.y(), minPoint.z());
        this.max = new Location(world, maxPoint.x(), maxPoint.y(), maxPoint.z());
    }

    private void loadChunks() {
        if (min == null || max == null) {
            return;
        }
        for (int x = min.getBlockX() >> 4; x <= max.getBlockX() >> 4; ++x) {
            for (int z = min.getBlockZ() >> 4; z <= max.getBlockZ() >> 4; ++z) {
                this.chunks.add(new ChunkLoc(x, z));
            }
        }
    }

    public void resetInstant() {
        try {
            World weWorld = BukkitAdapter.adapt(min.getWorld());
            ClipboardFormat format = ClipboardFormats.findByFile(file);
            if (format == null) {
                api.warn("[DuelsFFA Extension] Failed to find schematic format for " + file.getName());
                return;
            }
            try (InputStream in = Files.newInputStream(file.toPath());
                 ClipboardReader reader = format.getReader(in)) {
                Clipboard clipboard = reader.read();
                try (EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld)) {
                    if (config.isWeFastMode()) {
                        editSession.setFastMode(true);
                    }
                    ForwardExtentCopy operation = new ForwardExtentCopy(
                            clipboard,
                            clipboard.getRegion(),
                            editSession,
                            BlockVector3.at(min.getBlockX(), min.getBlockY(), min.getBlockZ())
                    );
                    if (regionMaskData != null) {
                        BlockVector3 sourceMin = clipboard.getMinimumPoint();
                        int xTranslate = min.getBlockX() - sourceMin.x();
                        int yTranslate = min.getBlockY() - sourceMin.y();
                        int zTranslate = min.getBlockZ() - sourceMin.z();
                        Mask sourceMask = new Mask() {
                            @Override
                            public boolean test(BlockVector3 vector) {
                                return regionMaskData.containsWorld(
                                        vector.x() + xTranslate,
                                        vector.y() + yTranslate,
                                        vector.z() + zTranslate
                                );
                            }

                            @Override
                            public Mask copy() {
                                return this;
                            }
                        };
                        operation.setSourceMask(sourceMask);
                    }
                    operation.setCopyingBiomes(config.isWeCopyBiomes());
                    operation.setCopyingEntities(config.isWeCopyEntities());
                    Operations.complete(operation);
                    if (config.isWeFlushQueue()) {
                        editSession.flushQueue();
                    }
                }
            }
        } catch (IOException e) {
            api.warn("[DuelsFFA Extension] Failed to paste schematic for zone " + arena.getName());
        }
    }

    public void reset(final Callback onDone) {
        api.doSync(() -> {
            if (resetting) {
                if (onDone != null) {
                    onDone.call();
                }
                return;
            }

            resetting = true;
            api.doAsync(() -> {
                try {
                    resetInstant();
                } catch (Throwable ignored) {
                }
                api.doSync(() -> {
                    try {
                        for (ChunkLoc chunkLoc : chunks) {
                            Chunk chunk = world.getChunkAt(chunkLoc.getX(), chunkLoc.getZ());
                            if (chunk.isLoaded()) {
                                chunk.unload(false);
                            }
                        }
                    } catch (Throwable ignored) {
                    }
                    resetting = false;
                    if (onDone != null) {
                        onDone.call();
                    }
                });
            });
        });
    }

    public boolean contains(final Location location) {
        if (!min.getWorld().equals(location.getWorld())) {
            return false;
        }
        if (regionMaskData != null) {
            return regionMaskData.containsWorld(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        }
        if (region != null) {
            return region.contains(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        }
        return min.getBlockX() <= location.getBlockX() && location.getBlockX() <= max.getBlockX()
                && min.getBlockY() <= location.getBlockY() && location.getBlockY() <= max.getBlockY()
                && min.getBlockZ() <= location.getBlockZ() && location.getBlockZ() <= max.getBlockZ();
    }

    public boolean contains(final Chunk chunk) {
        return chunks.contains(new ChunkLoc(chunk));
    }

    private static final class RegionMaskData {
        private static final int MAGIC = 0x41524D31; // ARM1

        private final int minX;
        private final int minY;
        private final int minZ;
        private final int maxX;
        private final int maxY;
        private final int maxZ;
        private final int sizeX;
        private final int sizeZ;
        private final BitSet bits;
        private final long count;

        private RegionMaskData(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BitSet bits, long count) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
            this.sizeX = maxX - minX + 1;
            this.sizeZ = maxZ - minZ + 1;
            this.bits = bits;
            this.count = count;
        }

        static RegionMaskData fromRegion(Region region) {
            BlockVector3 min = region.getMinimumPoint();
            BlockVector3 max = region.getMaximumPoint();
            int minX = min.x();
            int minY = min.y();
            int minZ = min.z();
            int maxX = max.x();
            int maxY = max.y();
            int maxZ = max.z();

            long sizeX = (long) maxX - minX + 1L;
            long sizeY = (long) maxY - minY + 1L;
            long sizeZ = (long) maxZ - minZ + 1L;
            long volume = sizeX * sizeY * sizeZ;
            if (volume <= 0L || volume > Integer.MAX_VALUE) {
                return null;
            }

            BitSet bits = new BitSet((int) volume);
            long count = 0L;
            for (BlockVector3 point : region) {
                int index = index(point.x() - minX, point.y() - minY, point.z() - minZ, (int) sizeX, (int) sizeZ);
                if (!bits.get(index)) {
                    bits.set(index);
                    count++;
                }
            }

            return new RegionMaskData(minX, minY, minZ, maxX, maxY, maxZ, bits, count);
        }

        long getCount() {
            return count;
        }

        boolean containsWorld(int x, int y, int z) {
            if (x < minX || x > maxX || y < minY || y > maxY || z < minZ || z > maxZ) {
                return false;
            }

            int relX = x - minX;
            int relY = y - minY;
            int relZ = z - minZ;
            return bits.get(index(relX, relY, relZ, sizeX, sizeZ));
        }

        void write(File file) throws IOException {
            byte[] data = bits.toByteArray();
            try (OutputStream out = Files.newOutputStream(file.toPath(), new OpenOption[0]);
                 GZIPOutputStream gzip = new GZIPOutputStream(out);
                 DataOutputStream dos = new DataOutputStream(gzip)) {
                dos.writeInt(MAGIC);
                dos.writeInt(minX);
                dos.writeInt(minY);
                dos.writeInt(minZ);
                dos.writeInt(maxX);
                dos.writeInt(maxY);
                dos.writeInt(maxZ);
                dos.writeLong(count);
                dos.writeInt(data.length);
                dos.write(data);
            }
        }

        static RegionMaskData read(File file) throws IOException {
            try (InputStream in = Files.newInputStream(file.toPath());
                 GZIPInputStream gzip = new GZIPInputStream(in);
                 DataInputStream dis = new DataInputStream(gzip)) {
                int magic = dis.readInt();
                if (magic != MAGIC) {
                    throw new IOException("Invalid region mask file magic");
                }

                int minX = dis.readInt();
                int minY = dis.readInt();
                int minZ = dis.readInt();
                int maxX = dis.readInt();
                int maxY = dis.readInt();
                int maxZ = dis.readInt();
                long count = dis.readLong();
                int length = dis.readInt();
                if (length < 0) {
                    throw new IOException("Invalid region mask data length");
                }

                byte[] data = new byte[length];
                dis.readFully(data);
                BitSet bits = BitSet.valueOf(data);
                return new RegionMaskData(minX, minY, minZ, maxX, maxY, maxZ, bits, count);
            }
        }

        private static int index(int relX, int relY, int relZ, int sizeX, int sizeZ) {
            return (relY * sizeZ + relZ) * sizeX + relX;
        }
    }
}
