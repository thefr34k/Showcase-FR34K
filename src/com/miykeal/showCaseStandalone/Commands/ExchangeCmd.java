package com.miykeal.showCaseStandalone.Commands;

import java.io.IOException;

import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.miykeal.showCaseStandalone.Exceptions.InsufficientPermissionException;
import com.miykeal.showCaseStandalone.Exceptions.MissingOrIncorrectArgumentException;
import com.miykeal.showCaseStandalone.ShopInternals.ItemStackHandler;
import com.miykeal.showCaseStandalone.ShopInternals.Todo;
import com.miykeal.showCaseStandalone.ShopInternals.Todo.Type;
import com.miykeal.showCaseStandalone.Shops.ExchangeShop;
import com.miykeal.showCaseStandalone.Utilities.Messaging;
import com.miykeal.showCaseStandalone.Utilities.Properties;
import com.miykeal.showCaseStandalone.Utilities.Term;
import com.miykeal.showCaseStandalone.Utilities.Utilities;

public class ExchangeCmd extends GenericCmd {

	public ExchangeCmd (CommandSender sender, String args[]){
        super(sender, args);
        this.permission = Properties.permCreateExchange; 
	}
	
	@Override
	public boolean execute() throws MissingOrIncorrectArgumentException, InsufficientPermissionException {
		if (errorCheck())
			return true;
		
		if(Properties.blacklistedWorlds.contains(player.getWorld().getName()))
            throw new InsufficientPermissionException("`rYou are not allowed to create a showcase in this world."); //msg: blacklistError
		
		//Default values:
        int 		amount 		= 0;
        boolean		unlimited	= false;
        int 		price 		= 1;
        ItemStack 	is 			= null;
        ItemStack	ex			= null;
        
        /* Using keyword "this" will attempt to sell whats in your hand.  
         *      ie. default values for material and amount are what's in your hand.
         * 
         * Otherwise default value for amount is 1.
         * 
         * Valid forms of command:
         * 1: scs exchange [item/"this"] [item/"this"]
         * 2: scs exchange [item/"this"] [item/"this"] [amount]
         * 3: scs exchange [item/"this"] [item/"this"] [amount] [price]
         */

        //We have optional number of arguments.  Lets parse through them.
        try {
            switch (args.length){
                case 0:
                case 1:
                case 2:
                	throw new MissingOrIncorrectArgumentException();
                	
                case 3:
                	is = Utilities.getItemStack(player, args[1]);
                    ex = Utilities.getItemStack(player, args[2]);
                    break;
                    
                case 4:
                    is = Utilities.getItemStack(player, args[1]);
                    ex = Utilities.getItemStack(player, args[2]);
                    if (args[3].equalsIgnoreCase("unlimited"))
                        unlimited = true;
                    else 
                        amount = Integer.parseInt(args[3]);
                    break;
                    
                case 5:
                	is = Utilities.getItemStack(player, args[1]);
                    ex = Utilities.getItemStack(player, args[2]);
                    if (args[3].equalsIgnoreCase("unlimited"))
                        unlimited = true;
                    else 
                        amount = Integer.parseInt(args[3]);
                    price	= Integer.parseInt(args[4]);
                    
                    if (price <= 0)
                    	throw new MissingOrIncorrectArgumentException();
                    break;
            }
            
        } catch (Exception e) {
            throw new MissingOrIncorrectArgumentException ();
        }
		
        if (is == null || is.getTypeId() == 0 || ex == null || ex.getTypeId() == 0)
        	throw new MissingOrIncorrectArgumentException (Term.ITEM_MISSING.get()+",is="+is+",ex="+ex);
        
        //Blacklist or whitelisted items
        if(!Properties.sellList.isEmpty() && !scs.hasPermission(player, Properties.permAdmin)) {
            MaterialData md = is.getData();
            if (Properties.sellBlackList && Properties.sellList.contains(md)
                        ||
               (!Properties.sellBlackList && !Properties.sellList.contains(md)))
                    throw new InsufficientPermissionException(Term.BLACKLIST_ITEM.get());
        }
        
        //Blacklist or whitelisted items
        if(!Properties.buyList.isEmpty() && !scs.hasPermission(player, Properties.permAdmin)) {
            MaterialData md = ex.getData();
            if (Properties.buyBlackList && Properties.buyList.contains(md)
                        ||
               (!Properties.buyBlackList && !Properties.buyList.contains(md)))
            		throw new InsufficientPermissionException(Term.BLACKLIST_ITEM.get());
        }
        
        if (price < 0)
            throw new MissingOrIncorrectArgumentException (Term.ERROR_PRICE_NEGATIVE.get());

        if (unlimited && !scs.hasPermission(player, Properties.permCreateUnlimited))
            throw new InsufficientPermissionException();
        
        if (Properties.exchangeCreatePrice > 0.0)
        	Messaging.send(player, Term.SHOP_PRICE_CREATE.get() + scs.formatCurrency(Properties.buyShopCreatePrice));
        
        Messaging.send(player, next);

        //Since these are going to be our unique keys for shops, they cannot be random.
        //We can do a random one, until they hit the block, and then it will be replaced by a 
        //hash of the full location string.
        try {
            String 	sha1 	= Utilities.getRandomSha1(player.getName());
            
            ExchangeShop	p		= new ExchangeShop(scs, Properties.storageVersion	);
            				p.setSHA1				(sha1								);
            				p.setAmount				(amount								);
            				p.setUnlimited			(unlimited							);
            				p.setItemStack			(ItemStackHandler.cloneItemStack(is));
            				p.setExchangeItemStack	(ItemStackHandler.cloneItemStack(ex));
            				p.setPrice				(price								);
            				p.setOwner				(player.getName()					);

            scs.addTodo(player, new Todo (player, Type.CREATE, p, 0, null));
        } catch (IOException ioe) {
        	Messaging.send(player, Term.ERROR.get() +ioe);
        }

        return true;
	}
	
}
