package com.meteordevelopments.duels.replay.playback;

import com.meteordevelopments.duels.replay.packet.WrapperPlayClientUseEntity;
import com.meteordevelopments.duels.replay.packet.WrapperPlayServerCamera;
import com.meteordevelopments.duels.replay.packet.WrapperPlayServerEntityDestroy;
import com.meteordevelopments.duels.replay.packet.WrapperPlayServerGameStateChange;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.replay.listener.AbstractListener;
import com.meteordevelopments.duels.replay.util.VersionUtil;
import com.meteordevelopments.duels.replay.util.VersionUtil.VersionEnum;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReplayPacketListener extends AbstractListener {

    private PacketAdapter packetAdapter;

    private Replayer replayer;

    private int previous;

    private HashMap<Player, Integer> spectating;

    public ReplayPacketListener(Replayer replayer) {
        this.replayer = replayer;
        this.spectating = new HashMap<>();
        this.previous = -1;

    }

    @Override
    public void register() {
        if (isRegistered()) return;

        this.packetAdapter = new PacketAdapter(DuelsPlugin.getInstance(), ListenerPriority.NORMAL, 
                PacketType.Play.Client.USE_ENTITY, 
                PacketType.Play.Server.ENTITY_DESTROY,
                PacketType.Play.Server.BLOCK_CHANGE,
                PacketType.Play.Server.MULTI_BLOCK_CHANGE,
                PacketType.Play.Server.BLOCK_ACTION,
                PacketType.Play.Server.EXPLOSION) {

            @SuppressWarnings("deprecation")
            @Override
            public void onPacketReceiving(PacketEvent event) {
                WrapperPlayClientUseEntity packet = new WrapperPlayClientUseEntity(event.getPacket());
                Player p = event.getPlayer();

                if (packet.getType() == EntityUseAction.ATTACK && ReplayHelper.replaySessions.containsKey(p.getName()) && replayer.getNPCList().values().stream().anyMatch(ent -> packet.getTargetID() == ent.getId())) {
                    if (p.getGameMode() != GameMode.SPECTATOR) previous = p.getGameMode().getValue();
                    setCamera(p, packet.getTargetID(), 3F);
                }
            }

            @Override
            public void onPacketSending(PacketEvent event) {
                PacketType type = event.getPacketType();
                Player p = event.getPlayer();

                if (type == PacketType.Play.Server.BLOCK_CHANGE || type == PacketType.Play.Server.MULTI_BLOCK_CHANGE || type == PacketType.Play.Server.BLOCK_ACTION || type == PacketType.Play.Server.EXPLOSION) {
                    if (ReplayHelper.replaySessions.containsKey(p.getName())) {
                        if (!Replayer.IS_REPLAY_SENDING.get()) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }

                if (type == PacketType.Play.Server.ENTITY_DESTROY) {
                    WrapperPlayServerEntityDestroy packet = new WrapperPlayServerEntityDestroy(event.getPacket());

                    if (ReplayHelper.replaySessions.containsKey(p.getName()) && isSpectating(p)) {
                        List<Integer> entityIds;
                        if (VersionUtil.isAbove(VersionEnum.V1_17)) {
                            entityIds = packet.getHandle().getIntLists().read(0);
                        } else {
                            entityIds = IntStream.of(packet.getEntityIDs()).boxed().collect(Collectors.toList());
                        }

                        for (int id : entityIds) {
                            if (id == spectating.get(p)) {
                                setCamera(p, p.getEntityId(), previous);
                            }
                        }
                    }
                }
            }

        };

        ProtocolLibrary.getProtocolManager().addPacketListener(this.packetAdapter);
    }

    @Override
    public void unregister() {
        ProtocolLibrary.getProtocolManager().removePacketListener(this.packetAdapter);
    }

    public boolean isRegistered() {
        return this.packetAdapter != null;
    }

    public int getPrevious() {
        return previous;
    }

    public boolean isSpectating(Player p) {
        return this.spectating.containsKey(p);
    }

    public void resetCamera(Player p) {
        if (this.spectating.containsKey(p)) {
            this.spectating.remove(p);

            setCamera(p, p.getEntityId(), previous);
        }
    }

    public void setCamera(Player p, int entityID, float gamemode) {
        WrapperPlayServerCamera camera = new WrapperPlayServerCamera();
        camera.setCameraId(entityID);

        WrapperPlayServerGameStateChange state = new WrapperPlayServerGameStateChange();

        if (VersionUtil.isAbove(VersionEnum.V1_16)) {
            state.getHandle().getGameStateIDs().write(0, 3);
        } else {
            state.setReason(3);
        }

        state.setValue(gamemode < 0 ? 0 : gamemode);

        state.sendPacket(p);
        camera.sendPacket(p);

        if (gamemode == 3F) {
            this.spectating.put(p, entityID);
        } else if (this.spectating.containsKey(p)) {
            this.spectating.remove(p);
        }
    }
}
