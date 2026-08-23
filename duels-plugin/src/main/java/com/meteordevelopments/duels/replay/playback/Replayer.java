package com.meteordevelopments.duels.replay.playback;

import java.util.ArrayList;



import java.util.Collection;
import java.util.HashMap;





import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.meteordevelopments.duels.replay.playback.session.ReplaySession;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;



import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.replay.api.IReplayHook;
import com.meteordevelopments.duels.replay.api.ReplayAPI;
import com.meteordevelopments.duels.replay.config.ConfigManager;
import com.meteordevelopments.duels.replay.Replay;
import com.meteordevelopments.duels.replay.data.ActionData;
import com.meteordevelopments.duels.replay.data.ActionType;
import com.meteordevelopments.duels.replay.data.ReplayData;
import com.meteordevelopments.duels.replay.data.types.ItemData;
import com.meteordevelopments.duels.replay.data.types.LocationData;
import com.meteordevelopments.duels.replay.data.types.SpawnData;
import com.meteordevelopments.duels.replay.util.entities.IEntity;
import com.meteordevelopments.duels.replay.util.entities.INPC;



import com.meteordevelopments.duels.api.folialib.task.WrappedTask;

public class Replayer {

	private HashMap<String, INPC> npcs;
	
	private HashMap<Integer, IEntity> entities;
	
	private Map<Location, ItemData> blockChanges;
	
	private Player watcher;
	
	private Replay replay;
	
	private WrappedTask runTask;
	
	private int currentTicks;
	private double speed, tmpTicks;
	
	private boolean paused, started;
	
	private ReplayingUtils utils;
	private ReplaySession session;
		
	public Replayer(Replay replay, Player watcher) {
		this.replay = replay;
		this.watcher = watcher;
		this.npcs = new HashMap<String, INPC>();
		this.entities = new HashMap<Integer, IEntity>();
		this.blockChanges = new HashMap<>();
		
		this.utils = new ReplayingUtils(this);
		this.session = new ReplaySession(this);
		this.paused = false;
	}
	
	
	public boolean start() {
		ReplayData data = this.replay.getData();
		int duration = data.getDuration();
		this.session.setStart(watcher.getLocation());
		SpawnData spawnData = null;
		if (data.getActions().containsKey(0)) {
			for (ActionData startData : data.getActions().get(0)) {
				if (startData.getPacketData() instanceof SpawnData) {
					spawnData = (SpawnData) startData.getPacketData();
					break;
				}
			}
		} else {
			spawnData = findFirstSpawn(data).orElse(null);
		}

		if (spawnData != null && !spawnData.getLocation().isValidWorld()) {
			DuelsPlugin.getInstance().getLang().sendMessage(watcher, "REPLAY.world-not-found", "world", spawnData.getLocation().getWorld());
			return false;
		}

		ReplayHelper.replaySessions.put(watcher.getName(), this);

		if (spawnData != null) {
			watcher.teleport(LocationData.toLocation(spawnData.getLocation()));
		}

		this.session.startSession();

		this.speed = 1;
		
		executeTick(0, ReplayingMode.PLAYING);
		
		this.runTask = DuelsPlugin.getFoliaLib().getScheduler().runTimerAsync(() -> {
			updateProgress();

			if (Replayer.this.paused) return;
			
			Replayer.this.tmpTicks += speed;
			if (Replayer.this.tmpTicks % 1 != 0) return;
			
			if (currentTicks < duration) {

				executeTick(currentTicks++, ReplayingMode.PLAYING);

				if ((currentTicks + 2) < duration && speed == 2)  {
					executeTick(currentTicks++, ReplayingMode.PLAYING);

				}
				
			} else {
				
				stop();
			}
		}, 1L, 1L);

		return true;
	}

	public void executeTick(int tick, ReplayingMode mode) {
		ReplayData data = this.replay.getData();
		if (!data.getActions().isEmpty() && data.getActions().containsKey(tick)) {

			if (tick == 0 && started) return;
			this.started = true;

			List<ActionData> list = data.getActions().get(tick);
			for (ActionData action : list) {

				utils.handleAction(action, data, mode);

				if (action.getType() == ActionType.CUSTOM) {
					if (ReplayAPI.getInstance().getHookManager().isRegistered()) {
						for (IReplayHook hook : ReplayAPI.getInstance().getHookManager().getHooks()) {
							hook.onPlay(action, Replayer.this);
						}
					}
				}
			
			}
			
			if (tick == 0) data.getActions().remove(tick);
		}
	}

	private void updateProgress() {
		ConfigManager.PROGRESS_TYPE.update(this);
	}
	
	private Optional<SpawnData> findFirstSpawn(ReplayData data) {
		return data.getActions().values().stream()
				.flatMap(Collection::stream)
				.filter(action -> action.getPacketData() instanceof SpawnData)
				.map(action -> (SpawnData) action.getPacketData())
				.findFirst();
	}
	
	public void stop() {
		DuelsPlugin.getInstance().getLang().sendMessage(watcher, "REPLAY.finished-watching");
		
		if (this.runTask != null) {
			this.runTask.cancel();
		}
		this.getReplay().getData().getActions().clear();
		
		for (INPC npc : this.npcs.values()) {
			npc.remove();
		}
		
		for (IEntity entity : this.entities.values()) {
			entity.remove();
		}
		
		this.utils.despawn(new ArrayList<>(this.utils.getEntities().values()), null);
				
		this.npcs.clear();
		
		this.replay.setPlaying(false);
		
		if (ConfigManager.WORLD_RESET) this.utils.resetChanges(this.blockChanges);

		this.session.stopSession();

        if (isPaused()) {
            ReplayHelper.sendClientPause(watcher, false);
        }
	}
	
	public HashMap<String, INPC> getNPCList() {
		return npcs;
	}
	
	public HashMap<Integer, IEntity> getEntityList() {
		return entities;
	}
	
	public Map<Location, ItemData> getBlockChanges() {
		return blockChanges;
	}
	
	public Player getWatchingPlayer() {
		return watcher;
	}
	
	public Replay getReplay() {
		return replay;
	}
	
	public ReplayingUtils getUtils() {
		return utils;
	}
	
	public ReplaySession getSession() {
		return session;
	}

	public boolean isPaused() {
		return paused;
	}

    public void setPaused(boolean paused, boolean updateClient) {
        if (updateClient) {
            ReplayHelper.sendClientPause(watcher, paused);
        }

        this.paused = paused;
    }

	public void setPaused(boolean paused) {
        this.setPaused(paused, false);
	}


	
	public void setSpeed(double speed) {
		this.tmpTicks = 1;
		this.speed = speed;
		
		
		ReplayHelper.sendTitle(watcher, " ", speed >= 1 ? "§ax" + speed : "§cx" + speed, 10);
	}
	
	public double getSpeed() {
		return speed;
	}
	
	public int getCurrentTicks() {
		return currentTicks;
	}
	
	public void setCurrentTicks(int currentTicks) {
		this.currentTicks = currentTicks;
	}
	
	public void sendMessage(String message) {
		if (message != null) {
			final String prefix = DuelsPlugin.getInstance().getLang().getMessage("STRINGS.PREFIX");
			this.watcher.sendMessage((prefix != null ? prefix + " " : "§9[Duels] ") + message);
		}
	}
}
