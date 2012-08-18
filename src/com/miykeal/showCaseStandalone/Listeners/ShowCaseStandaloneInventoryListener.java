package com.miykeal.showCaseStandalone.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import com.miykeal.showCaseStandalone.ShowCaseStandalone;
import com.miykeal.showCaseStandalone.Shops.Shop;

public class ShowCaseStandaloneInventoryListener implements Listener {
	
	private ShowCaseStandalone	scs;
	
	public ShowCaseStandaloneInventoryListener (ShowCaseStandalone scs) {
		this.scs	= scs;
		this.scs.getServer().getPluginManager().registerEvents(this, scs);
	}

	@EventHandler(ignoreCancelled=true)
	public void onInventoryClose (InventoryCloseEvent event) {
		
		try {
			Inventory		inventory	= event.getView().getTopInventory();
			InventoryHolder	holder		= inventory.getHolder();
			Player			player		= (Player)	event.getPlayer();
			
			if (holder == null)
				return;
			
			Shop			shop		= (Shop)	holder;
							shop.onInventoryClosed(player);

		} catch (ClassCastException cce) {
			// holder isn't the shop --> ignore
		}
		
		
	}
}
