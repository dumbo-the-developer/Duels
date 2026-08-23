package com.meteordevelopments.duels.replay.data.types;

public class AnimationData extends PacketData {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5227638148471461255L;

	private int id;
	
	public AnimationData(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
}
