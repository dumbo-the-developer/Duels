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
import org.bukkit.scheduler.BukkitRunnable;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.replay.api.ReplaySessionFinishEvent;
import com.meteordevelopments.duels.replay.config.ConfigManager;
import com.meteordevelopments.duels.replay.config.ItemConfig;
import com.meteordevelopments.duels.replay.config.ItemConfigOption;
import com.meteordevelopments.duels.replay.config.ItemConfigType;

public class ReplaySession {

	private Replayer replayer;
	
	private Player player;
	
	private ItemStack content[];
	
	private int level;
	
	private float xp;
	
	private Location start;
	
	private ReplayPacketListener packetListener;
	
	public ReplaySession(Replayer replayer) {
		this.replayer = replayer;
		
		this.player = this.replayer.getWatchingPlayer();
		
		this.packetListener = new ReplayPacketListener(replayer);
	}
	
	public void startSession() {
		this.packetListener.register();

		this.content = this.player.getInventory().getContents();
		if (this.start == null) {
			this.start = this.player.getLocation();
		}
		this.level = this.player.getLevel();
		this.xp = this.player.getExp();

		this.player.setHealth(this.player.getMaxHealth());
		this.player.setFoodLevel(20);
		this.player.getInventory().clear();
		
		ItemConfigOption teleport = ItemConfig.getItem(ItemConfigType.TELEPORT);
		ItemConfigOption time = ItemConfig.getItem(ItemConfigType.SPEED);
		ItemConfigOption leave = ItemConfig.getItem(ItemConfigType.LEAVE);
		ItemConfigOption backward = ItemConfig.getItem(ItemConfigType.BACKWARD);
		ItemConfigOption forward = ItemConfig.getItem(ItemConfigType.FORWARD);
		ItemConfigOption pauseResume = ItemConfig.getItem(ItemConfigType.PAUSE);

		List<ItemConfigOption> configItems = Arrays.asList(teleport, time, leave, backward, forward, pauseResume);

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
		
		if (ConfigManager.HIDE_PLAYERS) {
			for (Player all : Bukkit.getOnlinePlayers()) {
				if (all == this.player) continue;
				
				this.player.hidePlayer(all);
			}
		}


	}
	
	public void stopSession() {
		if (ReplayHelper.replaySessions.containsKey(this.player.getName())) {
			ReplayHelper.replaySessions.remove(this.player.getName());
		}
		
		this.packetListener.unregister();

		
		DuelsPlugin.getFoliaLib().getScheduler().runAtEntity(player, task -> {
			resetPlayer();
			player.teleport(start);

			if (ConfigManager.HIDE_PLAYERS) {
				for (Player all : Bukkit.getOnlinePlayers()) {
					if (all == player) continue;
					player.showPlayer(all);
				}
			}

			ReplaySessionFinishEvent finishEvent = new ReplaySessionFinishEvent(replayer.getReplay(), player);
			Bukkit.getPluginManager().callEvent(finishEvent);
		});
		

	}
	
	public void resetPlayer() {
		packetListener.resetCamera(player);

		player.getInventory().clear();
		player.getInventory().setContents(content);
		
		if (player.getGameMode() != GameMode.CREATIVE) {
			player.setFlying(false);
			player.setAllowFlight(false);
		}

		if (ConfigManager.PROGRESS_TYPE == ReplayProgressType.XP_BAR) {
			player.setLevel(level);
			player.setExp(xp);
		}
	}

	public void setStart(Location start) {
		this.start = start;
	}

	public ReplayPacketListener getPacketListener() {
		return packetListener;
	}
}
