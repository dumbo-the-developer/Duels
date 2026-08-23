package com.meteordevelopments.duels.replay.util.entities;

import java.util.UUID;

import com.meteordevelopments.duels.replay.util.version.EntityBridge;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import com.meteordevelopments.duels.replay.packet.WrapperPlayServerSpawnEntity;

import com.meteordevelopments.duels.replay.data.types.FishingData;
import com.meteordevelopments.duels.replay.data.types.LocationData;
import com.meteordevelopments.duels.replay.util.VersionUtil;
import com.meteordevelopments.duels.replay.util.VersionUtil.VersionEnum;
import org.bukkit.util.Vector;

public class FishingUtils {

	public static WrapperPlayServerSpawnEntity createHookPacket(FishingData fishing, int throwerID, int entID) {
		Location loc = LocationData.toLocation(fishing.getLocation());
		
		WrapperPlayServerSpawnEntity packet = new WrapperPlayServerSpawnEntity();
		
		packet.setEntityID(entID);
		if (VersionUtil.isBelow(VersionEnum.V1_13)) {
			packet.setObjectData(throwerID);
			packet.setType(90);
		}
		packet.setUniqueId(UUID.randomUUID());

        Vector velocity = new Vector(fishing.getX(), fishing.getY(), fishing.getZ());
        packet.setVelocity(velocity);
		
		if (VersionUtil.isAbove(VersionEnum.V1_14)) {
			packet.setObjectData(throwerID); // Object data index changed
			packet.getHandle().getEntityTypeModifier().write(0, EntityBridge.FISHING_BOBBER.toEntityType());
		}

		packet.setX(loc.getX());
		packet.setY(loc.getY());
		packet.setZ(loc.getZ());
		
		return packet;
	}
	
	public static com.meteordevelopments.duels.replay.packet.old.WrapperPlayServerSpawnEntity createHookPacketOld(FishingData fishing, int throwerID, int entID) {
		Location loc = LocationData.toLocation(fishing.getLocation());
		
		com.meteordevelopments.duels.replay.packet.old.WrapperPlayServerSpawnEntity packet = new com.meteordevelopments.duels.replay.packet.old.WrapperPlayServerSpawnEntity();
		
		packet.setEntityID(entID);
		packet.setObjectData(throwerID);
		packet.setType(90);
		
		packet.setOptionalSpeedX(fishing.getX());
		packet.setOptionalSpeedY(fishing.getY());
		packet.setOptionalSpeedZ(fishing.getZ());
		
		
		packet.setX(loc.getX());
		packet.setY(loc.getY());
		packet.setZ(loc.getZ());
		packet.setPitch(loc.getPitch());
		packet.setYaw(loc.getYaw());
		
		return packet;
	}
	
	
}
