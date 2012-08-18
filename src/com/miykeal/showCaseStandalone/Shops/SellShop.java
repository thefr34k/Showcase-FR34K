package com.miykeal.showCaseStandalone.Shops;

import org.bukkit.entity.Player;

import com.miykeal.showCaseStandalone.ShowCaseStandalone;
import com.miykeal.showCaseStandalone.ShopInternals.ItemStackHandler;
import com.miykeal.showCaseStandalone.ShopInternals.Storage;
import com.miykeal.showCaseStandalone.ShopInternals.Transaction;
import com.miykeal.showCaseStandalone.Utilities.Messaging;
import com.miykeal.showCaseStandalone.Utilities.Term;
import com.miykeal.showCaseStandalone.interfaces.Balance;

public class SellShop extends Shop {
	
	private ShowCaseStandalone	scs;
	
	public SellShop (ShowCaseStandalone scs, int storageVersion) {
		this (scs, new Storage(storageVersion));
	}
	
	public SellShop (ShowCaseStandalone scs, Storage storage) {
		super(Activity.SELL, scs, storage);
		this.scs	= scs;
	}

	@Override
	public void info(Player p) {
		super.showDetails(p);
	}

	@Override
	public void interact(Player p, int interactAmount) {
		if (!checkInteractConditions(p, true))
			return;
		
		// bench
		startBench("interact, player="+p.getName() +",interactAmount="+interactAmount, "init");
		
		
		// Collect information
		Balance		balance			= scs.getBalanceHandler();
		double		purchPrice;
		int			workingAmount	= (!isUnlimited() && getAmount() <= interactAmount) ? getAmount() : interactAmount;
		
		// bench
		markBench("sell");
		
		// check for early exit conditions
		if (!isUnlimited() && getAmount() <= 0) {
			Messaging.send	(p, 			Term.SHOP_EMPTY_COSTUMER.get());
			msgOwner		(getPOwner(), 	Term.SHOP_EMPTY_OWNER.get(getItemName()));
			markBench("!unlimited && amt <= 0");
			return;
		}
		
		// bench
		markBench("math conditions");
		
		//Does the player have enough money to buy the item?
		if (!balance.hasEnough(p.getName(), getPrice() * workingAmount)) {
			Messaging.send(p, Term.ERROR_INSUFFICIENT_MONEY_COSTUMER.get());
			markBench("economy check no money");
			return;
		}
		
		//place as many of the items as i can, and get that amount.
		workingAmount = ItemStackHandler.addToInventory(p.getInventory(), getItemStack(), workingAmount);
//		workingAmount = safeAddItems(p, workingAmount);
		
		// bench
		markBench ("safeAddItems");
		
		if(workingAmount == 0){
			Messaging.send(p, Term.ERROR_INSUFFICIENT_ROOM_BUY.get());
			return;
		}
		
		purchPrice = getPrice() * workingAmount;
        balance.sub(p, purchPrice);
        
        if (!isUnlimited()) {
        	balance.add	(getOwner(), 	purchPrice		);
        	setAmount	(getAmount() - 	workingAmount	);
        }
        
        // bench
        markBench("economy transaction");
        
        
        // setup undo
        ShowCaseStandalone.pv.setLastTransaction(p, new Transaction(p, this, workingAmount));
        
        // bench
        markBench("setup undo");
        
        Messaging.send			(p, 			Term.MESSAGE_SELL_COSTUMER.get(getItemName(), 	String.valueOf(workingAmount), 	scs.formatCurrency(purchPrice))			);
		ShowCaseStandalone.tlog	(p.getName(), 	getOwner(), getActivity().toString(), 			workingAmount, purchPrice, 		getMaterial(), getSHA1(), getAmount()	);
		
		// bench
		markBench("log transaction");
		
		if (!isUnlimited()){
			msgOwner(getPOwner(), Term.MESSAGE_SELL_OWNER_1.get(getItemName(),	String.valueOf(getAmount())));
			msgOwner(getPOwner(), Term.MESSAGE_SELL_OWNER_2.get(p.getName(), 	String.valueOf(workingAmount), scs.formatCurrency(purchPrice)));
			msgOwner(getPOwner(), "`Y" + Term.INFO_13.get() + getSHA1());
        }
		
		// stop bench
		stopBench();
	}

}
