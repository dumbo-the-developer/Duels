package com.meteordevelopments.duels.replay.storage;

import java.util.List;

import com.meteordevelopments.duels.replay.Replay;
import com.meteordevelopments.duels.replay.util.fetcher.Consumer;

public interface IReplaySaver {

	void saveReplay(Replay replay);
	
	void loadReplay(String replayName, Consumer<Replay> consumer);
	
	boolean replayExists(String replayName);
	
	void deleteReplay(String replayName);
	
	List<String> getReplays();
}
