package com.meteordevelopments.duels.replay.listener;







import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.meteordevelopments.duels.DuelsPlugin;

public abstract class AbstractListener implements Listener{
	
	protected DuelsPlugin plugin;
	
	public AbstractListener(){
		this.plugin = DuelsPlugin.getInstance();
	}
	
	public void register(){
		Bukkit.getPluginManager().registerEvents(this, this.plugin);
	}
	
	public void unregister(){
		HandlerList.unregisterAll(this);
	}
	

}
