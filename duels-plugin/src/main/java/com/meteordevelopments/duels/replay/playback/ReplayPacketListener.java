package com.meteordevelopments.duels.replay.playback;

import com.meteordevelopments.duels.replay.packet.WrapperPlayClientEntityAction;
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
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerAction;
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

    private final Replayer replayer;

    private int previous;

    private final HashMap<Player, Integer> spectating;
    private final HashMap<Player, Long> spectateStartTime;

    public ReplayPacketListener(Replayer replayer) {
        this.replayer = replayer;
        this.spectating = new HashMap<>();
        this.spectateStartTime = new HashMap<>();
        this.previous = -1;
    }

    @Override
    public void register() {
        if (isRegistered()) return;

        this.packetAdapter = new PacketAdapter(DuelsPlugin.getInstance(), ListenerPriority.NORMAL, 
                PacketType.Play.Client.USE_ENTITY, 
                PacketType.Play.Client.ENTITY_ACTION,
                PacketType.Play.Client.STEER_VEHICLE,
                PacketType.Play.Server.ENTITY_DESTROY,
                PacketType.Play.Server.BLOCK_CHANGE,
                PacketType.Play.Server.MULTI_BLOCK_CHANGE,
                PacketType.Play.Server.BLOCK_ACTION,
                PacketType.Play.Server.EXPLOSION) {

            @SuppressWarnings("deprecation")
            @Override
            public void onPacketReceiving(PacketEvent event) {
                final PacketType type = event.getPacketType();
                final Player p = event.getPlayer();

                if (type == PacketType.Play.Client.USE_ENTITY) {
                    WrapperPlayClientUseEntity packet = new WrapperPlayClientUseEntity(event.getPacket());
                    if (packet.getType() == EntityUseAction.ATTACK && ReplayHelper.replaySessions.containsKey(p.getName()) && replayer.getNPCList().values().stream().anyMatch(ent -> packet.getTargetID() == ent.getId())) {
                        setCamera(p, packet.getTargetID(), 3F);
                    }
                } else if (type == PacketType.Play.Client.ENTITY_ACTION) {
                    if (ReplayHelper.replaySessions.containsKey(p.getName()) && isSpectating(p)) {
                        WrapperPlayClientEntityAction packet = new WrapperPlayClientEntityAction(event.getPacket());
                        PlayerAction action = packet.getAction();
                        long elapsed = System.currentTimeMillis() - spectateStartTime.getOrDefault(p, 0L);
                        if (action == PlayerAction.START_SNEAKING && elapsed > 500) {
                            DuelsPlugin.getFoliaLib().getScheduler().runAtEntity(p, task -> {
                                resetCamera(p);
                                ReplayHelper.sendTitle(p, " ", "§7Free Camera Mode", 20);
                            });
                        }
                    }
                } else if (type == PacketType.Play.Client.STEER_VEHICLE) {
                    if (ReplayHelper.replaySessions.containsKey(p.getName()) && isSpectating(p)) {
                        boolean unmount = false;
                        try {
                            if (event.getPacket().getBooleans().size() > 1) {
                                unmount = Boolean.TRUE.equals(event.getPacket().getBooleans().read(1));
                            } else if (event.getPacket().getBytes().size() > 0) {
                                byte flags = event.getPacket().getBytes().read(0);
                                unmount = (flags & 0x02) != 0;
                            }
                        } catch (Exception ignored) {
                            unmount = false;
                        }
                        long elapsed = System.currentTimeMillis() - spectateStartTime.getOrDefault(p, 0L);
                        if (unmount && elapsed > 500) {
                            DuelsPlugin.getFoliaLib().getScheduler().runAtEntity(p, task -> {
                                resetCamera(p);
                                ReplayHelper.sendTitle(p, " ", "§7Free Camera Mode", 20);
                            });
                        }
                    }
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
                                resetCamera(p);
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
        this.spectating.remove(p);
        this.spectateStartTime.remove(p);
        int targetGm = (previous >= 0) ? previous : ((p.getGameMode() != GameMode.SPECTATOR) ? p.getGameMode().getValue() : 0);
        setCamera(p, p.getEntityId(), (float) targetGm);
        p.setAllowFlight(true);
        p.setFlying(true);
    }

    public void setCamera(Player p, int entityID, float gamemode) {
        if (gamemode == 3F) {
            if (previous < 0) {
                previous = (p.getGameMode() != GameMode.SPECTATOR) ? p.getGameMode().getValue() : 0;
            }
            this.spectating.put(p, entityID);
            this.spectateStartTime.put(p, System.currentTimeMillis());
        } else {
            this.spectating.remove(p);
            this.spectateStartTime.remove(p);
        }

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
    }
}
