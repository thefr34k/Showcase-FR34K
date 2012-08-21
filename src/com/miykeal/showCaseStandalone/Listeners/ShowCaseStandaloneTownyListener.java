package com.miykeal.showCaseStandalone.Listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.miykeal.showCaseStandalone.Events.ShowCaseCreateEvent;
import com.miykeal.showCaseStandalone.Utilities.Properties;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.TownBlockOwner;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.object.TownyUniverse;

public class ShowCaseStandaloneTownyListener implements Listener {

	@EventHandler
	public void onShowCaseCreateEvent (ShowCaseCreateEvent event) {
		
		Location	location	= event.getLocation();
		Player		player		= event.getPlayer();

		if (!Properties.towny_allowInWilderness)
			if (isWilderness(location))
				event.setCancelled(true);
		
//		if (Properties.towny_allowInWilderness)
//			if (!isInsideShopPlot(location))
//				event.setCancelled(true);
		
		try {
			if (Properties.towny_needsToBeOwner)
				if (!isPlotOwner(player, location))
					event.setCancelled(true);
			
		} catch (NotRegisteredException nre) {}
		
		try {
			if (Properties.towny_needsResident)
				if (!hasResident(player, location))
					event.setCancelled(true);
			
		} catch (NotRegisteredException nre) {}
		
	}
	
	
	/**
	 * @param location
	 * @return true if the given location is wilderness
	 */
	public boolean isWilderness (Location location) {
		return TownyUniverse.isWilderness(location.getBlock());
	}
	
	/**
	 * @param player
	 * @param location
	 * @return true if the given player is owner in the given plot
	 * @throws NotRegisteredException 
	 */
	public boolean isPlotOwner (Player player, Location location) throws NotRegisteredException {
		TownBlockOwner	owner	= TownyUniverse.getDataSource().getResident(player.getName());
		return TownyUniverse.getTownBlock(location).isOwner(owner);
	}
	
	/**
	 * @param player
	 * @param location
	 * @return	true if the given player has a resident in the town 
	 * @throws NotRegisteredException 
	 */
	public boolean hasResident (Player player, Location location) throws NotRegisteredException {
		return TownyUniverse.getTownBlock(location).getTown().hasResident(player.getName());
	}
	
	/**
	 * @param location
	 * @return	true if the given block is in a shop plot
	 */
	public boolean isInsideShopPlot (Location location) {
		return TownyUniverse.getTownBlock(location).getType() == TownBlockType.COMMERCIAL;
	}
}
