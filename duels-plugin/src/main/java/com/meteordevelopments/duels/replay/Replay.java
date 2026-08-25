package com.meteordevelopments.duels.replay;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.replay.data.DuelReplayMetadata;
import com.meteordevelopments.duels.replay.data.ReplayData;
import com.meteordevelopments.duels.replay.data.ReplayInfo;
import com.meteordevelopments.duels.replay.recording.Recorder;
import com.meteordevelopments.duels.replay.playback.Replayer;
import com.meteordevelopments.duels.replay.util.StringUtils;

public class Replay {

	private String id;
	
	private ReplayData data;
	
	private ReplayInfo replayInfo;

	private DuelReplayMetadata metadata;
	
	private Recorder recorder;
	private Replayer replayer;
	
	private boolean isRecording, isPlaying;
	
	public Replay() {
		this.id = StringUtils.getRandomString(6);
		this.data = new ReplayData();
		this.isRecording = false;
		this.isPlaying = false;
	}
	
	public Replay(String id, ReplayData data) {
		this.id = id;
		this.data = data;
	}
	
	public void record(CommandSender sender, Player... players) {
		recordAll(Arrays.asList(players), sender);
	}
	
	public void recordAll(List<Player> players, CommandSender sender) {
		this.recorder = new Recorder(this, players, sender);
		this.recorder.start();
		this.isRecording = true;
		
		ReplayManagerImpl.activeReplays.put(this.id, this);
	}
	
	public void play(Player watcher) {
		DuelsPlugin.getFoliaLib().getScheduler().runAtEntity(watcher, task -> startReplay(watcher));
	}
		
	private void startReplay(Player watcher) {
		this.replayer = new Replayer(this, watcher);
		this.isPlaying = this.replayer.start();
	}
	
	public String getId() {
		return id;
	}
	
	public ReplayData getData() {
		return data;
	}
	
	public void setData(ReplayData data) {
		this.data = data;
	}
	
	public Recorder getRecorder() {
		return recorder;
	}
	
	public Replayer getReplayer() {
		return replayer;
	}
	
	public boolean isRecording() {
		return isRecording;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public void setRecording(boolean recording) {
		this.isRecording = recording;
	}
	
	public boolean isPlaying() {
		return isPlaying;
	}
	
	public void setPlaying(boolean isPlaying) {
		this.isPlaying = isPlaying;
	}
	
	public void setReplayInfo(ReplayInfo replayInfo) {
		this.replayInfo = replayInfo;
	}
	
	public ReplayInfo getReplayInfo() {
		return replayInfo;
	}

	public DuelReplayMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(DuelReplayMetadata metadata) {
		this.metadata = metadata;
	}
}
