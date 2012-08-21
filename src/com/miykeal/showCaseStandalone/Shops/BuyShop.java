package com.miykeal.showCaseStandalone.Shops;

import java.util.logging.Level;

import org.bukkit.entity.Player;

import com.miykeal.showCaseStandalone.ShowCaseStandalone;
import com.miykeal.showCaseStandalone.ShopInternals.ItemStackHandler;
import com.miykeal.showCaseStandalone.ShopInternals.Storage;
import com.miykeal.showCaseStandalone.ShopInternals.Transaction;
import com.miykeal.showCaseStandalone.Utilities.Messaging;
import com.miykeal.showCaseStandalone.Utilities.Term;
import com.miykeal.showCaseStandalone.interfaces.Balance;

/**
 * @author Kellerkindt
 * This class represents the buy-showcase
 */
public class BuyShop extends Shop {
	
	private ShowCaseStandalone	scs;
	
	public BuyShop (ShowCaseStandalone scs, int storageVersion) {
		this(scs, new Storage(storageVersion));
	}
	
	public BuyShop (ShowCaseStandalone scs, Storage storage)  {
		super(Activity.BUY, scs, storage);
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
		int			workingAmount;
		
		// bench
		markBench("buy");
		
//		scs.log(Level.INFO, "interact amount="+interactAmount);
		
		if (!isUnlimited()) {
			if (getAmount() >= getMaxAmount()) {
				Messaging.send	(p, 			Term.ERROR_FULL_SHOWCASE_COSTUMER.get()	);
				msgOwner		(getPOwner(),	Term.ERROR_FULL_SHOWCASE_OWNER.get()	);		
				return;	
			} else if ((getMaxAmount() - getAmount()) < interactAmount) {
				interactAmount	= getMaxAmount() - getAmount();
			}
			
			// bench
			markBench("buy init");
			workingAmount	= ItemStackHandler.countCompatibleItemStacks(p.getInventory(), getCompatibleItems(), needsEqualNBTTag());
			
			// bench
			markBench("count saleable items");
			workingAmount	= (workingAmount < interactAmount) ? workingAmount : interactAmount;
			
		} else
			workingAmount	= interactAmount;
		
		scs.log(Level.INFO, "interact amount="+interactAmount);
		
		if (balance.hasEnough(getOwner(), getPrice() * workingAmount) || isUnlimited()) {
			
			// bench
			markBench ("economy check");
			workingAmount	= ItemStackHandler.removeFromInventory(p.getInventory(), getCompatibleItems(), workingAmount, needsEqualNBTTag());
//			workingAmount	= safeRemoveItems(p, workingAmount);
			
			// bench
			markBench("save remove items");
			
			if (workingAmount == 0) {
				Messaging.send(p, Term.ITEM_NOT_MATCHING.get());
				return;
			}
			
			purchPrice	= getPrice() * workingAmount;
			balance.add(p, purchPrice);
			
			// on "normal" shops
			if (!isUnlimited()) {
				balance.sub(getOwner(), purchPrice);
				setAmount(getAmount()+workingAmount);
			}
			
			// bench
			markBench ("economy transaction");
			
			if (!isUnlimited()) {
				msgOwner(getPOwner(), Term.MESSAGE_BUY_OWNER_1.get(getItemName(), 	""+getAmount(), 	""+getMaxAmount())					);
				msgOwner(getPOwner(), Term.MESSAGE_BUY_OWNER_2.get(p.getName(), 	""+workingAmount,	 scs.formatCurrency(purchPrice))	);
				msgOwner(getPOwner(), "`Y" + Term.INFO_13.get() + getSHA1());
			}
			
			// log transaction
			ShowCaseStandalone.pv.setLastTransaction(p, new Transaction(p, this, workingAmount));
			
			// bench
			markBench("setup undo");
			
			Messaging.send			(p, 			Term.MESSAGE_BUY.get(getItemName(), ""+workingAmount, 			scs.formatCurrency(purchPrice) ));
			ShowCaseStandalone.tlog	(p.getName(), 	getOwner(),							getActivity().toString(), 	workingAmount, purchPrice, getMaterial(), getSHA1(), getAmount());
			
			// bench
			markBench("log transaction");
			
		} else {
			if (!isUnlimited())
				msgOwner		(getPOwner(),	Term.ERROR_INSUFFICIENT_MONEY_OWNER.get(""+workingAmount, getItemName(), p.getName()));
				Messaging.send	(p,				Term.ERROR_INSUFFICIENT_MONEY_COSTUMER.get());	
		}
		
		stopBench();
	}

}
