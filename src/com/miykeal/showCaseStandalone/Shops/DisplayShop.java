package com.miykeal.showCaseStandalone.Shops;

import org.bukkit.entity.Player;

import com.miykeal.showCaseStandalone.ShowCaseStandalone;
import com.miykeal.showCaseStandalone.ShopInternals.Storage;
import com.miykeal.showCaseStandalone.Utilities.Messaging;
import com.miykeal.showCaseStandalone.Utilities.Term;

/**
 * @author Kellerkindt
 * This class represents the display showcase
 */
public class DisplayShop extends Shop {
	
	public DisplayShop (ShowCaseStandalone scs, int storageVersion) {
		this (scs, new Storage(storageVersion));
	}
	
	public DisplayShop (ShowCaseStandalone scs, Storage storage) {
		super (Activity.DISPLAY, scs, storage);
	}

	@Override
	public void info(Player p) {
		Messaging.send(p, Term.ITEM_ON_DISPLAY.get(getItemName()) + " @ " + getOwner());
	}

	@Override
	public void interact(Player p, int interactAmount) {
		info (p);
	}

}
