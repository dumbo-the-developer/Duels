package com.meteordevelopments.duels.replay.playback.session;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.meteordevelopments.duels.replay.playback.ReplayHelper;
import com.meteordevelopments.duels.replay.playback.ReplayPacketListener;
import com.meteordevelopments.duels.replay.playback.Replayer;
import org.bukkit.Bukkit;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.replay.api.ReplaySessionFinishEvent;
import com.meteordevelopments.duels.replay.config.ConfigManager;
import com.meteordevelopments.duels.replay.config.ItemConfig;
import com.meteordevelopments.duels.replay.config.ItemConfigOption;
import com.meteordevelopments.duels.replay.config.ItemConfigType;

public class ReplaySession {

	private final Replayer replayer;
	private final Player player;
	private final ReplayPacketListener packetListener;

	private ItemStack[] content;
	private ItemStack[] armor;
	private ItemStack offhand;
	
	private int level;
	private float xp;
	private double health;
	private int food;
	private GameMode previousGameMode;
	private boolean wasAllowFlight;
	private boolean wasFlying;
	private Location start;

	public ReplaySession(Replayer replayer) {
		this.replayer = replayer;
		this.player = this.replayer.getWatchingPlayer();
		this.packetListener = new ReplayPacketListener(replayer);
	}
	
	public void startSession() {
		this.packetListener.register();

		this.content = this.player.getInventory().getContents();
		this.armor = this.player.getInventory().getArmorContents();
		try {
			this.offhand = this.player.getInventory().getItemInOffHand();
		} catch (Throwable ignored) {}

		if (this.start == null) {
			this.start = this.player.getLocation();
		}
		this.level = this.player.getLevel();
		this.xp = this.player.getExp();
		this.health = this.player.getHealth();
		this.food = this.player.getFoodLevel();
		this.previousGameMode = this.player.getGameMode();
		this.wasAllowFlight = this.player.getAllowFlight();
		this.wasFlying = this.player.isFlying();

		// Save snapshot to disk immediately for crash/restart safety
		ReplaySpectatorStorage.saveSnapshot(this.player, this.start, this.level, this.xp);

		this.player.setHealth(this.player.getMaxHealth());
		this.player.setFoodLevel(20);
		this.player.getInventory().clear();
		
		ItemConfigOption inspect = ItemConfig.getItem(ItemConfigType.INSPECT);
		if (inspect == null) inspect = ItemConfig.getItem(ItemConfigType.TELEPORT);
		ItemConfigOption time = ItemConfig.getItem(ItemConfigType.SPEED);
		ItemConfigOption leave = ItemConfig.getItem(ItemConfigType.LEAVE);
		ItemConfigOption backward = ItemConfig.getItem(ItemConfigType.BACKWARD);
		ItemConfigOption forward = ItemConfig.getItem(ItemConfigType.FORWARD);
		ItemConfigOption pauseResume = ItemConfig.getItem(ItemConfigType.PAUSE);

		List<ItemConfigOption> configItems = Arrays.asList(inspect, time, leave, backward, forward, pauseResume);

		configItems.stream()
			.filter(Objects::nonNull)
			.filter(ItemConfigOption::isEnabled)
			.forEach(item -> {
				ItemStack is = ReplayHelper.createItem(item);
				if (is != null) {
					this.player.getInventory().setItem(item.getSlot(), is);
				}
			});
		
		this.player.setAllowFlight(true);
		this.player.setFlying(true);
		this.player.setCollidable(false);
		this.player.setInvisible(true);
		
		for (Player all : Bukkit.getOnlinePlayers()) {
			if (all == this.player) continue;
			
			this.player.hidePlayer(DuelsPlugin.getInstance(), all);
			all.hidePlayer(DuelsPlugin.getInstance(), this.player);
		}
	}
	
	public void stopSession() {
		if (ReplayHelper.replaySessions.containsKey(this.player.getName())) {
			ReplayHelper.replaySessions.remove(this.player.getName());
		}
		
		this.packetListener.unregister();

		// Perform immediate synchronous reset so inventory is never lost
		resetPlayer();
		if (start != null && start.getWorld() != null) {
			player.teleport(start);
		}

		for (Player all : Bukkit.getOnlinePlayers()) {
			if (all == player) continue;
			player.showPlayer(DuelsPlugin.getInstance(), all);
			all.showPlayer(DuelsPlugin.getInstance(), player);
		}

		try {
			ReplaySessionFinishEvent finishEvent = new ReplaySessionFinishEvent(replayer.getReplay(), player);
			Bukkit.getPluginManager().callEvent(finishEvent);
		} catch (Throwable ignored) {}
	}
	
	public void resetPlayer() {
		try {
			packetListener.resetCamera(player);
		} catch (Throwable ignored) {}

		player.getInventory().clear();
		if (content != null) {
			player.getInventory().setContents(content);
		}
		if (armor != null) {
			player.getInventory().setArmorContents(armor);
		}
		if (offhand != null) {
			try {
				player.getInventory().setItemInOffHand(offhand);
			} catch (Throwable ignored) {}
		}
		
		player.setCollidable(true);
		player.setInvisible(false);

		if (previousGameMode != null) {
			player.setGameMode(previousGameMode);
		}

		player.setAllowFlight(wasAllowFlight);
		player.setFlying(wasFlying && wasAllowFlight);

		if (health > 0) {
			player.setHealth(Math.min(health, player.getMaxHealth()));
		}
		if (food > 0) {
			player.setFoodLevel(food);
		}

		if (ConfigManager.PROGRESS_TYPE == ReplayProgressType.XP_BAR) {
			player.setLevel(level);
			player.setExp(xp);
		}

		// Delete persistent snapshot file
		ReplaySpectatorStorage.deleteSnapshot(player.getUniqueId());
	}

	public void setStart(Location start) {
		this.start = start;
	}

	public ReplayPacketListener getPacketListener() {
		return packetListener;
	}
}
