package me.realized.duels.util;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

public final class BlockUtil {

    private BlockUtil() {}

    public static <T extends BlockState> T getTargetBlock(final Player player, final Class<T> type, final int range) {
        final BlockIterator iterator = new BlockIterator(player, range);

        while (iterator.hasNext()) {
            final Block block = iterator.next();

            if (type.isInstance(block.getState())) {
                return type.cast(block.getState());
            }
        }

        return null;
    }

    public static boolean near(final Player player, final Block block, final int hDiff, final int vDiff) {
        int pX = player.getLocation().getBlockX();
        int pY = player.getLocation().getBlockY();
        int pZ = player.getLocation().getBlockZ();
        int bX = block.getLocation().getBlockX();
        int bY = block.getLocation().getBlockY();
        int bZ = block.getLocation().getBlockZ();
        return Math.abs(pX - bX) <= hDiff && Math.abs(pY - bY) <= vDiff && Math.abs(pZ - bZ) <= hDiff;
    }
}
