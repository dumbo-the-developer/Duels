package com.meteordevelopments.duels.replay.api;

import java.util.List;

import com.meteordevelopments.duels.replay.data.ActionData;
import com.meteordevelopments.duels.replay.data.types.PacketData;
import com.meteordevelopments.duels.replay.playback.Replayer;

public interface IReplayHook {

	List<PacketData> onRecord(String playerName);
	
	void onPlay(ActionData data, Replayer replayer);
}
