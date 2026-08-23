package com.meteordevelopments.duels.replay.config;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.config.Config;
import com.meteordevelopments.duels.replay.playback.session.ReplayProgressType;
import com.meteordevelopments.duels.replay.playback.session.ReplayProgression;
import com.meteordevelopments.duels.replay.recording.optimization.ReplayQuality;

public class ConfigManager {

	public static boolean ENABLED = true;
	public static int MAX_LENGTH = 3600;
	public static int CLEANUP_REPLAYS = 30;
	
	public static boolean RECORD_BLOCKS = true;
	public static boolean REAL_CHANGES = true;
	public static boolean RECORD_ITEMS = true;
	public static boolean RECORD_ENTITIES = false;
	public static boolean RECORD_CHAT = false;
	public static boolean SAVE_STOP = true;
	public static boolean USE_OFFLINE_SKINS = false;
	public static boolean HIDE_PLAYERS = true;
	public static boolean ADD_PLAYERS = false;
	public static boolean WORLD_RESET = false;
    public static boolean USE_MODERN_PAUSE = true;

	public static ReplayProgression PROGRESS_TYPE = ReplayProgressType.ACTION_BAR;
	public static ReplayQuality QUALITY = ReplayQuality.HIGH;
	public static String CHAT_FORMAT = "&r<{name}> {message}";
	
	public static void loadConfigs() {
		ItemConfig.loadConfig();
		loadData(true);
	}
	
	public static void loadData(boolean initial) {
		final Config config = DuelsPlugin.getInstance().getConfiguration();
		if (config == null) return;

		ENABLED = config.isReplayEnabled();
		MAX_LENGTH = config.getReplayMaxLength();
		SAVE_STOP = config.isReplaySaveOnStop();
		CLEANUP_REPLAYS = config.getReplayCleanupDays();
		HIDE_PLAYERS = config.isReplayHidePlayers();
		USE_MODERN_PAUSE = config.isReplayUseModernPause();
		WORLD_RESET = config.isReplayResetWorldChanges();

		try {
			QUALITY = ReplayQuality.valueOf(config.getReplayQuality().toUpperCase());
		} catch (Exception e) {
			QUALITY = ReplayQuality.HIGH;
		}

		try {
			PROGRESS_TYPE = ReplayProgressType.valueOf(config.getReplayProgressDisplay().toUpperCase());
		} catch (Exception e) {
			PROGRESS_TYPE = ReplayProgressType.getDefault();
		}

		RECORD_BLOCKS = config.isReplayRecordBlocks();
		REAL_CHANGES = config.isReplayRecordBlocksRealChanges();
		RECORD_ENTITIES = config.isReplayRecordEntities();
		RECORD_ITEMS = config.isReplayRecordItems();
		RECORD_CHAT = config.isReplayRecordChat();
		CHAT_FORMAT = config.getReplayChatFormat();
	}
	
	public static void reloadConfig() {
		ItemConfig.loadConfig();
		loadData(false);
	}
}
