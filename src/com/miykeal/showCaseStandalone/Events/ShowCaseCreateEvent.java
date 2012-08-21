package com.miykeal.showCaseStandalone.Events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ShowCaseCreateEvent extends Event implements Cancellable{
	
	private HandlerList	handler	= new HandlerList();

	private Location			location;
	private Player				player;
	private boolean				isCancelled;
	
	public ShowCaseCreateEvent (Location location, Player player) {
		this.location	= location;
		this.player		= player;
		
	}

	/**
	 * @return The location for the new shop
	 */
	public Location getLocation () {
		return location;
	}
	
	/**
	 * @return The player who wants to create a new ShowCase
	 */
	public Player getPlayer () {
		return player;
	}

	@Override
	public HandlerList getHandlers() {
		return handler;
	}
	
	@Override
	public String getEventName() {
		return getClass().getSimpleName();
	}

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		isCancelled = cancelled;
	}
}
