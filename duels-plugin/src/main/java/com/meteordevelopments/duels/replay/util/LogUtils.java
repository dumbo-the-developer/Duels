package com.meteordevelopments.duels.replay.util;

import com.meteordevelopments.duels.DuelsPlugin;

public class LogUtils {

	public static void log(String message){
		DuelsPlugin.getInstance().getLogger().info(message);
	}

}
