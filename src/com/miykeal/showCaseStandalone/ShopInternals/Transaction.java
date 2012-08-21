package com.miykeal.showCaseStandalone.ShopInternals;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import com.miykeal.showCaseStandalone.ShowCaseStandalone;
import com.miykeal.showCaseStandalone.Shops.Shop;
import com.miykeal.showCaseStandalone.Shops.Shop.Activity;
import com.miykeal.showCaseStandalone.Utilities.Messaging;
import com.miykeal.showCaseStandalone.Utilities.Properties;
import com.miykeal.showCaseStandalone.Utilities.Term;
import com.miykeal.showCaseStandalone.interfaces.Balance;

/**
* Copyright (C) 2011 Kellerkindt <kellerkindt@miykeal.com>, Sorklin <sorklin@gmail.com>
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

public class Transaction {
    
    Activity activity;
    Player player;
    Shop shop;
    int quantity;
    double price;
    long time;
    public String returnMessage; 
    
    public Transaction(Player player, Shop shop, int quantity){
        this.player = player;
        this.shop = shop;
        this.time = System.currentTimeMillis();
        this.quantity = quantity;
        returnMessage = "";
        
        price = this.shop.getPrice();
        this.activity = this.shop.getActivity();
    }
    
    /**
     * Undoes a transaction, if the transaction falls within the allowed time,
     * both players have enough money, and items to undo the transaction.
     * @return True the transaction was rolled back.  False it was not rolled back.
     */
    public boolean undo(){
        //Lets check for problems before we undo the transaction.
        if((System.currentTimeMillis() - time) > Properties.maxUndoTime){
            returnMessage = Term.ERROR_UNDO_EXPIRED.get();
            return false;
        }
        
        if(this.quantity == 0){
            returnMessage = Term.ERROR_UNDO_TWICE.get();
            return false;
        }
            
        
        Balance 		balance = ShowCaseStandalone.get().getBalanceHandler();
        List<String>	msg;
        
        switch(activity){
            case BUY:
                //Player sold something.  So check:
                //  Player has enough money to undo.
                //  Showcase has enough items to undo.
                
                if(!balance.hasEnough(player.getName(), price * quantity)){
                    returnMessage = Term.ERROR_UNDO_BUY_MONEY.get();
                    return false;
                }
                
                if(shop.getAmount() < quantity && !shop.isUnlimited()){
                    returnMessage = Term.ERROR_UNDO_BUY_ITEM.get();
                    return false;
                }
                
                //If the player doesn't have the room, tough luck.  Put as many in as possible.
                ItemStackHandler.addToInventory(player.getInventory(), shop.getItemStack(), quantity);
                shop.setAmount(shop.getAmount() - quantity);
                //Don't need to credit unlimited shop's account.
                if(!shop.isUnlimited())
                    balance.add(shop.getOwner(), price * quantity);
                
                balance.sub(player, price * quantity);
                
                msg = this.info();
                msg.set(0, Term.MESSAGE_SUCCESSFULL_UNDID.get());
                Messaging.mlSend(player, msg);
                
                this.quantity = 0; //So that we can't do this again.
                this.price = 0;
                break;
                
            case SELL:
                //We need to check a few things before undoing the transaction:
                //  Since the player bought something, does the shop owner still have that money?
                //  Does the player still have the items?
                if(!shop.isUnlimited() && !balance.hasEnough(shop.getOwner(), price * quantity)){
                    returnMessage = Term.ERROR_UNDO_SELL_MONEY.get();
                    return false;
                }
                
                if (ItemStackHandler.countCompatibleItemStacks(player.getInventory(), shop.getItemStack(), shop.needsEqualNBTTag()) < quantity) {
                    returnMessage = Term.ERROR_UNDO_SELL_ITEM.get();
                    return false;
                }
                
                int removed = ItemStackHandler.removeFromInventory(player.getInventory(), shop.getItemStack(), quantity, shop.needsEqualNBTTag());//shop.safeRemoveItems(player, quantity);
                if(removed != quantity){
                    //We should never be here, because we already counted saleable items.
                    //But i'm including this just in case my logic has problems.
                    //ShowCaseStandalone.spam("Removed: " + removed + " != quantity: " + quantity);
                    //Put em back.
                    ItemStackHandler.addToInventory(player.getInventory(), shop.getItemStack(), removed);
                    Messaging.send(player, Term.ERROR_UNDO_UNKNOWN.get());
                    return false;
                }
                
                //Remember, if the shop is unlimited, not to do any amount or balance changes.
                if(!shop.isUnlimited())
                    shop.setAmount(shop.getAmount() + quantity);
                balance.add(player, price * quantity);
                
                if(!shop.isUnlimited())
                    balance.sub(shop.getOwner(), price * quantity);
                
                msg = this.info();
                msg.set(1, Term.MESSAGE_SUCCESSFULL_UNDID.get());
                Messaging.mlSend(player, msg);
                
                this.quantity = 0; //So that we can't do this again.
                this.price = 0;
                break;
            
            
            case EXCHANGE:
            	// TODO:
            	break;
            case DISPLAY:
            	break;
        }
        return true;
    }
    
    /**
     * Displays info about the last transaction.
     */
    public List<String> info(){
        List<String> msg = new ArrayList<String>();
        Balance balance = ShowCaseStandalone.get().getBalanceHandler();
        if(quantity == 0){
            msg.add(Term.INFO_UNDO_1.get());
        } else {
            long curTime = System.currentTimeMillis();
            long timeLeft = Properties.maxUndoTime - (curTime - time);
            
            msg.add(Term.INFO_UNDO_2.get());
            msg.add(Term.INFO_UNDO_3.get());
            msg.add(Term.INFO_UNDO_4.get(player.getName(), 			shop.getOwner()));
            msg.add(Term.INFO_UNDO_5.get(activity.toString(), 		shop.getItemName(), String.valueOf(quantity)));
            msg.add(Term.INFO_UNDO_6.get(balance.format(price),  	balance.format(price * quantity)));
            
            if(timeLeft > 0) {
            	msg.add(Term.INFO_UNDO_7.get(String.valueOf((Properties.maxUndoTime - (curTime - time)) / 1000)));
            } else {
            	msg.add(Term.INFO_UNDO_8.get());
            }
            
            msg.add(Term.INFO_UNDO_2.get());
        }
        return msg;
    }
            
}
