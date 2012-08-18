package com.miykeal.showCaseStandalone.Listeners;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.material.Attachable;
import org.bukkit.material.MaterialData;

import com.miykeal.showCaseStandalone.ShowCaseStandalone;
import com.miykeal.showCaseStandalone.Events.ShowCaseCreateEvent;
import com.miykeal.showCaseStandalone.Exceptions.InsufficientPermissionException;
import com.miykeal.showCaseStandalone.Exceptions.ShopNotFoundException;
import com.miykeal.showCaseStandalone.ShopInternals.ItemStackHandler;
import com.miykeal.showCaseStandalone.ShopInternals.Todo;
import com.miykeal.showCaseStandalone.ShopInternals.Todo.Type;
import com.miykeal.showCaseStandalone.Shops.Shop;
import com.miykeal.showCaseStandalone.Shops.Shop.Activity;
import com.miykeal.showCaseStandalone.Utilities.BenchMark;
import com.miykeal.showCaseStandalone.Utilities.Messaging;
import com.miykeal.showCaseStandalone.Utilities.Properties;
import com.miykeal.showCaseStandalone.Utilities.Term;
import com.miykeal.showCaseStandalone.Utilities.Utilities;

/**
* Copyright (C) 2011 Kellerkindt <kellerkindt@miykeal.com>
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
public class ShowCaseStandalonePlayerListener implements Listener {
	
	private HashMap<Player, Todo>		todo 	= new HashMap<Player, Todo>();
	private final ShowCaseStandalone	scs;

	public ShowCaseStandalonePlayerListener(ShowCaseStandalone instance) {
		scs = instance;
        scs.getServer().getPluginManager().registerEvents(this, scs);
	}
	
        /*
	 * Cancel pickup of a Item if the item is a shop Item
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerPickupItem (PlayerPickupItemEvent pe) {
		if ( scs.getShopHandler().isShopItem(pe.getItem()) )
			pe.setCancelled(true);				
	}
	
	/*
	 * Let the player Interact with the shop
         * Lets keep the priority low, so we don't get cancelled when we're not doing anything.
	 */
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled=true)
	public void onPlayerInteract (PlayerInteractEvent pie) {
            /*
             * This whole routine needs optimization.  Currently it takes far more time 
             * then it should.
             */
            
            BenchMark bm = null;
            if(Properties.interactDebug){
                bm = new BenchMark("onPlayerInteract");
                bm.start("init");
            }
                
                
		// Abort if action does not fit - saves power :)
		if (!pie.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !pie.getAction().equals(Action.LEFT_CLICK_BLOCK))
			return;
		
		// Collects information
		Player 		player 		= pie.getPlayer();
		Block	 	block		= pie.getClickedBlock();

                if(Properties.interactDebug)
                    bm.mark();
                
		try {
			// Interact
			if (pie.getAction().equals(Action.RIGHT_CLICK_BLOCK))
			{
                //Lets check for attachables in player's hand.  IF found, abort this. 
                //This will allow players to attach signs, paintings, torches etc. to showcases.
                //I have to find type ids for signs and paintings, since they don't respond to 
                //any instanceof's i could find.
                if(pie.hasItem() && !todo.containsKey(player))
                    if((pie.getItem().getData() instanceof Attachable) ||
                        (pie.getItem().getTypeId() == 323) ||
                        (pie.getItem().getTypeId() == 321) )
                            return;
                            
				if (todo.containsKey(player)) {

					 
					
					if (todo.get(player).type.equals(Type.CREATE)){
                        if(Properties.interactDebug)
                            bm.mark("Rightclickblock");
                                                
						if(cantInteract(player, block))
                            throw new InsufficientPermissionException(Term.ERROR_AREA_PROTECTED.get());
                        else
							this.create(player, block);
					}
	                                
					else if (todo.get(player).type.equals(Type.REMOVE))
						this.remove(player, block);
					
					else if (todo.get(player).type.equals(Type.ADD)) 
						this.add(player, block, (int)todo.get(player).amount);
					
					
					else if (todo.get(player).type.equals(Type.GET)) 
						this.get(player, block, (int)todo.get(player).amount);
					
					else if (todo.get(player).type.equals(Type.LIMIT))
						this.limit (player, block, (int)todo.get(player).amount);
					
					else if (todo.get(player).type.equals(Type.SETOWNER))
						this.setOwner (player, block);
					
					else if (todo.get(player).type.equals(Type.SETPRICE))
						this.price (player, block, todo.get(player).amount);
					
					else if (todo.get(player).type.equals(Type.DESTROY))
						this.destroy(player, block);
					
                    if(Properties.interactDebug)
                        bm.mark("end if block");
                                        

					pie.setCancelled(true);
					player.updateInventory();	// Have to :(

                    if (todo.containsKey(player))
                            todo.remove(player);

                    
                    if(Properties.interactDebug){
                        bm.mark("end rightclick");
                        bm.end();
                    }
					
					
				} else {
					Shop p = scs.getShopHandler().getShopForBlock(pie.getClickedBlock());
                                        
					
					if (p.getOwner().equals(player.getName()) && !p.isUnlimited() && p.getActivity() != Activity.DISPLAY) {
						
//						scs.getServer().getPluginManager().callEvent(new InventoryEvent(new ShopInventoryView(p, player)));
						
//						player.openInventory(new ShopInventoryView(p, player));
//						player.openInventory(new ShopInventory(p, player));
//						
//						Inventory inventory = scs.getServer().createInventory(player, 6*9);//.createInventory(new DoubleChest(null), InventoryType.CHEST);
//						
//						ItemStack	is	= p.getItemStack().clone();
//									is.setAmount(p.getAmount());
//						
//						inventory.addItem(is);
						
//						inventory.setContents(new ItemStack[27]);
						pie.setCancelled		(true);
						player.openInventory	(p.getInventory());
						
						
						
//						scs.log(Level.INFO, inventory.toString());
						
					} else {
                        if(Properties.interactDebug)
                            bm.mark("interact shopforblck");
	                                        
						if (p != null && scs.hasPermission(player, Properties.permUse)) {
		                    if(Properties.interactDebug)
		                        bm.mark("hasPermission");
	                                            
							pie.setCancelled(true);
	                        if(player.isSneaking()){
                            //	p.interact(player, scs.pv.getPlayerTransactionAmount(player));  -- This went around the shop handler, which actually is the thing that updates the save file.  Bad.
                                scs.getShopHandler().interact(block, player, ShowCaseStandalone.pv.getPlayerTransactionAmount(player));
                            } else { 
                            	scs.getShopHandler().interact(block, player, 1);
                            }
	                            
	                            if(Properties.interactDebug)
	                                bm.mark("afterinteract");
	                                                 
							player.updateInventory();	// Have to :(
	                                                
	                        if(Properties.interactDebug){
	                            bm.mark("updateInventory");
	                            bm.end();
	                        }
	                                                
						} else if (p != null && !scs.hasPermission(player, Properties.permUse))
							throw new InsufficientPermissionException();
					}
				}
			}
			
			// Show info
			else if (pie.getAction().equals(Action.LEFT_CLICK_BLOCK) ) {
                if(Properties.interactDebug)
                    bm.mark("leftclick");
                                
				Shop p = scs.getShopHandler().getShopForBlock(pie.getClickedBlock());
                                
                if(Properties.interactDebug)
                    bm.mark("interact shopforblck");
                                
				if (p != null && scs.hasPermission(player, Properties.permUse)) {             
		            if(Properties.interactDebug)
		                bm.mark("hasPermission");
	                                    
					pie.setCancelled(true);
					p.info(player);
				} else if (p != null && !scs.hasPermission(player, Properties.permUse))
					throw new InsufficientPermissionException();
                                
                if(Properties.interactDebug)
                    bm.end();
			}
		} catch (ShopNotFoundException snfe) {
			// No problem, if the selected block is no shop, nothing needs to be done.
			/// Just give a little feedback
			if (todo.containsKey(player))
				Messaging.send(player, Term.ERROR_NOT_A_SHOP);
		} catch (InsufficientPermissionException ipe) {
            Messaging.send(player, "`r" + ipe.getMessage());
            pie.setCancelled(true);
            
            
        } finally {
//        	if (todo.containsKey(player))
                todo.remove(player);
        }
	}

	/*
	 * Remove any player-set unit size.
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		ShowCaseStandalone.pv.clearPlayerTransactionAmount(p);
		ShowCaseStandalone.pv.clearLastTransaction(p);
	}
        
        
	/*
	 * Adds given Todo-Object to HashMap
	 */
	public void addTodo (Player player, Todo t) {
		todo.put(player, t);
	}
	
	/*
	 * Removes Todo-Object with given player as key
	 */
	public Todo removeTodo (Player player) {
			return todo.remove(player);
	}
	
	/**
	 * Destroys the given shop
	 * @param player
	 * @param b
	 * @throws ShopNotFoundException
	 * @throws InsufficientPermissionException
	 */
	private void destroy (Player player, Block b) throws ShopNotFoundException, InsufficientPermissionException {
		Shop	shop	= scs.getShopHandler().getShopForBlock(b);
		
		if (shop == null)
			throw new ShopNotFoundException();
		
		if (!scs.hasPermission(player, Properties.permAdmin))
			throw new InsufficientPermissionException(Term.ERROR_INSUFFICIENT_PERMISSION_DESTROY.get());
		
		
		// destroy the shop
		scs.getShopHandler().removeShop(shop);
		Messaging.send(player, Term.MESSAGE_SUCCESSFULL_DESTROYED);
	}

	/*
	 * Changes price of a shop
	 */
	private void price (Player player, Block b, double price) throws ShopNotFoundException, InsufficientPermissionException {
		Shop shop = scs.getShopHandler().getShopForBlock(b);

		if (shop == null)
                        throw new ShopNotFoundException();

		if (shop.getActivity().equals(Activity.DISPLAY)) {
			Messaging.send(player, Term.ERROR_SET_PRICE_DISPLAY.get());
			return;
		}
                
        if (!player.getName().equals(shop.getOwner()) && !scs.hasPermission(player, Properties.permAdmin))
                throw new InsufficientPermissionException(Term.ERROR_INSUFFICIENT_PERMISSION_SET_PRICE.get());
		
                
		// Set the shop price
		shop.setPrice(price); 
		Messaging.send(player, Term.MESSAGE_SET_PRICE.get() + scs.formatCurrency(shop.getPrice()));

		// Saving changes
		this.saveShop(shop, player);
	}


	/*
	 * Changes maxAmount of a BUY showcase
	 */
	private void limit (Player player, Block b, int limit) throws ShopNotFoundException, InsufficientPermissionException {
		Shop shop = scs.getShopHandler().getShopForBlock(b);

		if (shop == null) 
                        throw new ShopNotFoundException();

		if(!shop.getActivity().equals(Activity.BUY)) {
			Messaging.send(player, Term.ERROR_BUY_LIMIT.get());
			return;
		}
                
        if (!player.getName().equals(shop.getOwner()) && !scs.hasPermission(player, Properties.permAdmin)) 
                throw new InsufficientPermissionException(Term.ERROR_INSUFFICIENT_PERMISSION_SET_LIMIT.get());

		// Set the shop limit
		shop.setMaxAmount(limit);
		Messaging.send(player, Term.MESSAGE_BUY_LIMIT.get() + shop.getMaxAmount());

		// Saving changes
		this.saveShop(shop, player);
	}


	/*
	 * Add Items to a shop
	 */
	private void add (Player player, Block b, int amount) throws ShopNotFoundException, InsufficientPermissionException {
		Shop shop = scs.getShopHandler().getShopForBlock(b);

		if (shop == null)
        	throw new ShopNotFoundException();
		
		if (shop.getActivity().equals(Activity.DISPLAY)) {
			Messaging.send(player, Term.ERROR_ADD_ITEMS_DISPlAY.get());
			return;
		}
		
		if (shop.isUnlimited()) {
			Messaging.send(player, Term.ERROR_ADD_ITEMS_UNLIMITED.get());
			return;
		}
		
		int workingAmount = shop.getItemsFromPlayer(player, amount, scs.hasPermission(player, Properties.permAdmin));
		saveShop(shop, player);
		
		Messaging.send(player, Term.INVENTORY_UPDATE.get(String.valueOf(workingAmount),
				String.valueOf(shop.getAmount())));
		
		ShowCaseStandalone.tlog(player.getName(), player.getName(), "add",
				workingAmount, 0, shop.getMaterial(), shop.getSHA1(), shop.getAmount());
		
//		
//		
//
//
//		if (shop.getActivity().equals(Activity.DISPLAY)) {
//			Messaging.send(player, Term.ERROR_ADD_ITEMS_DISPlAY.get());
//			return;
//		}
//                
//        if (!player.getName().equals(shop.getOwner()) && !scs.hasPermission(player, Properties.permAdmin)) 
//                throw new InsufficientPermissionException(Term.ERROR_INSUFFICIENT_PERMISSION_ADD_ITEM.get()); 
//
//		if (shop.isUnlimited()) {
//			Messaging.send(player, Term.ERROR_ADD_ITEMS_UNLIMITED.get());
//			return;
//		}
//                
//		int workingAmount = ItemStackHandler.removeFromInventory(player.getInventory(), shop.getItemStack(), amount, shop.needsEqualNBTTag());
////        int workingAmount = shop.safeRemoveItems(player, amount);
//        shop.setAmount(shop.getAmount() + workingAmount);
//        Messaging.send(player, Term.INVENTORY_UPDATE.get(String.valueOf(workingAmount), String.valueOf(shop.getAmount())));
//
//            // Saving changes
//     	this.saveShop(shop, player);
//                    
//        ShowCaseStandalone.tlog(player.getName(), player.getName(), "add", workingAmount, 0, shop.getMaterial(), shop.getSHA1(), shop.getAmount());
	}


	/*
	 * Get Item from shop
	 */
	private void get (Player player, Block b, int amount) throws ShopNotFoundException, InsufficientPermissionException {
		Shop shop = scs.getShopHandler().getShopForBlock(b);

		if (shop == null)
			throw new ShopNotFoundException();

		if (shop.getActivity().equals(Activity.DISPLAY)) {
			Messaging.send(player, Term.ERROR_GET_DISPLAY.get());
			return;
		}
		
		int workingAmount = shop.addItemsToPlayer(player, amount, scs.hasPermission(player, Properties.permAdmin));
		saveShop(shop, player);
		
		Messaging.send(player, Term.MESSAGE_RECEIVED_ITEMS.get(String.valueOf(workingAmount)) + 
              ((workingAmount == 0) ? Term.ERROR_INSUFFICIENT_ROOM.get() : ""));

        ShowCaseStandalone.tlog(player.getName(), player.getName(), "get", 
              workingAmount, 0, shop.getMaterial(), shop.getSHA1(), shop.getAmount());
		
		
//                
//        if (!player.getName().equals(shop.getOwner()) && !scs.hasPermission(player, Properties.permAdmin)) 
//                throw new InsufficientPermissionException(Term.ERROR_INSUFFICIENT_PERMISSION_GET_ITEM.get());
//                
//		if (shop.getAmount() < 0)
//			Messaging.send(player, Term.ERROR_REM_ITEMS_UNLIMITED.get());
//		
//            if (amount == 0 || amount > shop.getAmount())
//                    amount = shop.getAmount();
//                        
//            //Remove as many of the item, up to the amount specified, and store
//            //how much was actually removed.
//            int workingAmount	= ItemStackHandler.addToInventory(player.getInventory(), shop.getItemStack(), amount);
////            int workingAmount = shop.safeAddItems(player, amount);
//            shop.setAmount(shop.getAmount() - workingAmount);
//            Messaging.send(player, Term.MESSAGE_RECEIVED_ITEMS.get(String.valueOf(workingAmount)) + 
//                ((workingAmount == 0) ? Term.ERROR_INSUFFICIENT_ROOM.get() : ""));
//
//            ShowCaseStandalone.tlog(player.getName(), player.getName(), "get", 
//                workingAmount, 0, shop.getMaterial(), shop.getSHA1(), shop.getAmount());
//		
//            // Saving changes
//            this.saveShop(shop, player);
	}


	/*
	 * Removes a shop
	 */
	private void remove (Player player, Block b) throws ShopNotFoundException, InsufficientPermissionException {
		Shop shop = scs.getShopHandler().getShopForBlock(b);

		if (shop == null)
			throw new ShopNotFoundException();

		if (!player.getName().equals(shop.getOwner()) && !scs.hasPermission(player, Properties.permAdmin))
			throw new InsufficientPermissionException(Term.ERROR_INSUFFICIENT_PERMISSION_REM_SHOWCASE.get());

		if (!shop.getActivity().equals(Activity.DISPLAY)) {
			//Remove as many items as I can.
			int workingAmount = ItemStackHandler.addToInventory(player.getInventory(), shop.getItemStack(), shop.getAmount());
			shop.setAmount(shop.getAmount() - workingAmount);
			Messaging.send(player, Term.MESSAGE_RECEIVED_ITEMS.get(String.valueOf(workingAmount)));

			ShowCaseStandalone.tlog(player.getName(), player.getName(), "remove", workingAmount, 0, shop.getMaterial(), shop.getSHA1(), shop.getAmount());

            if(shop.getAmount() > 0){
				Messaging.send(player, Term.INVENTORY_FULL.get());
				Messaging.send(player, Term.ITEM_LEFT.get() + shop.getAmount());
				this.saveShop(shop, player);
				return;
            }
		}

                // Remove the showcase
		scs.getShopHandler().removeShop(shop);
		Messaging.send(player, Term.MESSAGE_SUCCESSFULL_REMOVED.get());
	}

	/*
	 * Create a shop
	 */
	private void create (Player player, Block b) throws ShopNotFoundException, InsufficientPermissionException {
		Shop p				= this.removeTodo(player).shop;
		int removed = 0;

		double createPrice = 0;
		switch (p.getActivity()) {
		case SELL:
			createPrice = Properties.sellShopCreatePrice;
			break;
		case BUY:
			createPrice = Properties.buyShopCreatePrice;
			break;
		case DISPLAY:
			createPrice = Properties.displayCreatePrice;
			break;
		case EXCHANGE:
			createPrice	= Properties.exchangeCreatePrice;
		}

		if(!scs.getBalanceHandler().hasEnough(player.getName(), createPrice)){
			Messaging.send(player, Term.ERROR_INSUFFICIENT_MONEY_CREATE.get());
			return;
		}   

		if (scs.getShopHandler().isShopBlock(b)) {
			Messaging.send(player, Term.ERROR_ALREADY_SHOWCASE.get()); 
			return;
		}
                
        //Check for blacklisted/whitelisted shop block
        // I hate fucking workarounds.  Fucking bukkit.
        MaterialData md = new MaterialData(b.getTypeId(), b.getData());
        if  (Properties.blackList && Properties.blockList.contains(md)
                ||
            (!Properties.blackList && !Properties.blockList.contains(md)))
                throw new InsufficientPermissionException(Term.BLACKLIST_BLOCK.get());
        
                
		if ((p.getActivity() == Activity.SELL || p.getActivity() == Activity.EXCHANGE) && p.getAmount() > 0 && !p.isUnlimited()) {
			//Just try to remove the items and see how many I actually can remove (up to the specified amount).
			removed = ItemStackHandler.removeFromInventory(player.getInventory(), p.getItemStack(), p.getAmount(), p.needsEqualNBTTag());
			if(removed == 0) {
				Messaging.send(player, Term.ERROR_INSUFFICIENT_ITEMS_CREATE.get());
				return;
			}
		}
        //Try to replace with a unique hash, otherwise keep the random hash.
        try {p.setSHA1(Utilities.sha1(b.toString()));} catch (IOException ioe) {}
		p.setAmount     (removed);
		p.setBlock		(b);
		p.setVisible	(true);

		scs.getShopHandler().addShop(p);
		this.saveShop(p, player);	// Saving the shop

		scs.getBalanceHandler().sub(player, createPrice);

		Messaging.send(player, Term.MESSAGE_SUCCESSFULL_CREATED.get());
		
		if (!p.getActivity().equals(Activity.DISPLAY)) {
			Messaging.send(player, Term.INVENTORY_CURRENT.get() + p.getAmount()); 
			ShowCaseStandalone.tlog(player.getName(), player.getName(), "create", removed, createPrice, p.getMaterial(), p.getSHA1(), p.getAmount());
		}
	}


	/*
	 *  Set Owner of a shop
	 */
	private void setOwner (Player player, Block b) throws ShopNotFoundException, InsufficientPermissionException {
		Todo t = this.removeTodo(player);

		Shop p = scs.getShopHandler().getShopForBlock(b);
                
		if (p == null) 
			throw new ShopNotFoundException();
                
	    if (!player.getName().equals(p.getOwner()) && !scs.hasPermission(player, Properties.permAdmin))
            throw new InsufficientPermissionException(Term.ERROR_INSUFFICIENT_PERMISSION_SET_OWNER.get());
                
		p.setOwner(p.getOwner());     
		Messaging.send(player, Term.MESSAGE_SET_OWNER.get(t.toString()));

		// Saving changes
		this.saveShop(p, player);
	}

	private void saveShop (Shop p, Player player) {
		try {
			scs.getShopHandler().save(p);
		} catch (IOException ioe) {
			scs.log(Level.WARNING, ioe+" while saving a shop.");
			Messaging.send(player, Term.ERROR_ON_SAVE.get());
		}
	}
        
        /**
         * Checks for ability of player to do something where the showcase will be.  Default is
         * to check for building rights (BlockPlaceEvent), but can be other.
         * @param p Player
         * @param b Block
         * @return 
         */
        private boolean cantInteract(Player p, Block b) {
            //Right now, block place is the only interact I can think of supported by bukkit.
            BlockPlaceEvent 		bpe 	= new BlockPlaceEvent(b, b.getState(), b.getRelative(BlockFace.DOWN), p.getItemInHand(), p, true);
            ShowCaseCreateEvent		scce	= new ShowCaseCreateEvent(b.getLocation(), p);
            
            Bukkit.getServer().getPluginManager().callEvent(scce);
            Bukkit.getServer().getPluginManager().callEvent(bpe);
            
            return bpe.isCancelled() || scce.isCancelled();
        }
}
