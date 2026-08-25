package com.meteordevelopments.duels.replay.recording.optimization;

public enum ReplayQuality {
	
	LOW("Low"),
	MEDIUM("Medium"),
	HIGH("High");
	
	private final String qualityName;
	
	private ReplayQuality(final String qualityName) {
		this.qualityName = qualityName;
	}
	
	public String getQualityName() {
		return qualityName;
	}
}
