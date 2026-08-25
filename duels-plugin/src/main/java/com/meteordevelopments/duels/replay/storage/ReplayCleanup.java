package com.meteordevelopments.duels.replay.storage;

import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.replay.config.ConfigManager;
import com.meteordevelopments.duels.replay.util.LogUtils;

public class ReplayCleanup {

	public static void cleanupReplays() {
		if (ConfigManager.CLEANUP_REPLAYS <= 0) return;
		
		List<String> replays = ReplaySaver.getReplays();
		if (replays == null || replays.isEmpty()) return;

		DuelsPlugin.getFoliaLib().getScheduler().runAsync(task -> {
			replays.forEach(ReplayCleanup::checkAndDelete);
		});
	}
	
	private static void checkAndDelete(String replay) {
		LocalDate creationDate = getCreationDate(replay);
		LocalDate threshold = LocalDate.now().minusDays(ConfigManager.CLEANUP_REPLAYS);

		if (creationDate.isBefore(threshold)) {
			LogUtils.log("Replay " + replay + " has expired. Removing it...");
			ReplaySaver.delete(replay);
		}
	}
	
	private static LocalDate getCreationDate(String replay) {
		File file = new File(DefaultReplaySaver.DIR, replay + ".replay");
		if (file.exists()) {
			return fromMillis(file.lastModified());
		}
		
		return LocalDate.now();
	}
	
	private static LocalDate fromMillis(long millis) {
		return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate();
	}
}
