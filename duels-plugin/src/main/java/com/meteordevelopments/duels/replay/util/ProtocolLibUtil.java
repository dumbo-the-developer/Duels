package com.meteordevelopments.duels.replay.util;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.meteordevelopments.duels.replay.util.entities.PacketNPC;

public class ProtocolLibUtil {

    /**
     * Initialize some ProtocolLib wrappers to avoid class loading issues
     * in the first replay after a server start.
     */
    public static void prepare() {
        PacketNPC npc = new PacketNPC();
        npc.setData(new WrappedDataWatcher());
        npc.getInfoAddPacket();
        npc.look(0, 0);
    }
}
