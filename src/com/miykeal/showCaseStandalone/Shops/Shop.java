package com.miykeal.showCaseStandalone.Shops;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagString;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import com.miykeal.showCaseStandalone.ShowCaseStandalone;
import com.miykeal.showCaseStandalone.Exceptions.InsufficientPermissionException;
import com.miykeal.showCaseStandalone.ShopInternals.ItemStackHandler;
import com.miykeal.showCaseStandalone.ShopInternals.NBTStorage;
import com.miykeal.showCaseStandalone.ShopInternals.Storage;
import com.miykeal.showCaseStandalone.ShopInternals.Todo.Type;
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

public abstract class Shop implements InventoryHolder {
	public enum Activity {
		BUY, SELL, DISPLAY, EXCHANGE,
	}
	
	private static final String namedBooleanIsUnlimited	= "isUnlimited";
	
	private static final String namedDoublePrice		= "price";
	private static final String namedDoubleLocationX	= "locX";
	private static final String namedDoubleLocationY	= "locY";
	private static final String namedDoubleLocationZ	= "locZ";
	
	private static final String namedIntegerAmount		= "amount";
	private static final String namedIntegerMaxAmount	= "maxAmount";
	
	private static final String namedStringActivity		= "activity";
	private static final String namedStringMaterial		= "material";
	private static final String namedStringEnchantments	= "enchantments";
	private static final String namedStringOwner		= "owner";
	private static final String namedStringMembers		= "members";
	private static final String namedStringWorld		= "world";
	private static final String namedStringSha1			= "sha1";
	
	private static final String namedStorgeNBT			= "nbt-storage";
	
	private static final String splitMember				= ",";
		
	private static final int	maxInventoryTitleLength	= 32;
	
	
	
	public static final int		DoubleChestFields		= 6*9;	// 6 rows with with 9 fields each
	
	private ShowCaseStandalone 	scs;
	private Storage				storage;
	
	// temporary variables - IMPORTANT: access via get/set !!!!
	private Activity					activity		= null;
	private World						world			= null;
	private Location					location		= null;
	private ItemStack					itemStack		= null;
	private Item						item			= null;
	private Block						block			= null;
	private Inventory					inventory		= null;
	private boolean						isVisible		= false;
	private int							inChest			= 0;
	private Map<Enchantment, Integer>	enchantments	= null;
	private BenchMark					bench			= null;
	private NBTStorage					nbtStorage		= null;
	private List<String>				members			= null;
	
	
	/**
	 * Currently spawned item
	 * @return
	 */
	public Item getItem () {
		return item;
	}
	
	/**
	 * @param value the new block for this shop
	 */
	public void setBlock (Block value) {
		block	= value;

		setLocation	(value.getLocation());		
	}
	
	/**
	 * @return the block for this shop
	 */
	public Block getBlock () {
		if (block == null) {
//			block = getWorld().getBlockAt(getLocation());
			
			// fix for #246 ? 
			if (getLocation().getWorld() != null)
				block = getLocation().getBlock();
		}
		
		return block;
	}
	
	/**
	 * @param value the sha1-key for this shop
	 */
	public void setSHA1 (String value) {
		storage.setString(namedStringSha1, value);
	}
	
	/**
	 * @return the sha1-key for this shop
	 */
	public String getSHA1 () {
		return storage.getString(namedStringSha1);
	}
	
	/**
	 * @param value the location of this shop
	 */
	public void setLocation (Location value) {
		location	= value.add(.5, 0, .5);

		storage.setDouble(namedDoubleLocationX, location.getX());
		storage.setDouble(namedDoubleLocationY, location.getY());
		storage.setDouble(namedDoubleLocationZ, location.getZ());
		setWorld(location.getWorld());
		
	}
	
	public Location getSpawnLocation () {
		return getLocation().clone().add(0, 1, 0);
	}
	
	/**
	 * @return the location where the shop is placed
	 */
	public Location getLocation () {
		if (location == null) {
			double x	= storage.getDouble(namedDoubleLocationX);
			double y	= storage.getDouble(namedDoubleLocationY);
			double z	= storage.getDouble(namedDoubleLocationZ);
			
			location	= new Location(getWorld(), x, y, z);
		}
		return location;
	}
	
	/**
	 * @return the chunk where the shop is placed
	 */
	public Chunk getChunk () {
		int x	= (int)(double)storage.getDouble(namedDoubleLocationX);
		int z	= (int)(double)storage.getDouble(namedDoubleLocationZ);
		
		return getWorld().getChunkAt(x, z);
	}
	
	/**
	 * @param value the world where the plugin is in
	 */
	public void setWorld (World value) {
		this.world	= value;
		storage.setString(namedStringWorld, value.getName());
	}
	
	/**
	 * @return the world where the shop is placed
	 */
	public World getWorld () {
		if (world == null) {
			if (location != null)
				world = location.getWorld();
			else
				world = scs.getServer().getWorld(storage.getString(namedStringWorld));
		}
		
		return world;
	}
	
	/**
	 * @param value the owner for this shop
	 */
	public void setOwner (String value) {
		storage.setString(namedStringOwner, value);
	}
	
	/**
	 * @return the owner as player object
	 */
	public Player getPOwner () {
		return scs.getServer().getPlayer(getOwner());
	}
	
	/**
	 * @return the owner of this shop
	 */
	public String getOwner () {
		return storage.getString(namedStringOwner);
	}
	
	/**
	 * @param value the MaterialData for this ShowCase
	 */
	public void setMaterial (MaterialData value) {
		storage.setString(namedStringMaterial, value.getItemType().toString() + ":"+((int)value.getData() & 0xFF));// Utilities.getStringFromMaterial(value));
		getItemStack().setData(value);

		for (Enchantment ench : getEnchantments().keySet()) {
			int lvl	= getEnchantments().get(ench);
			
			try {
				if (Properties.allowUnsafeEnchantments)
					itemStack.addUnsafeEnchantment(ench, lvl);
				else
					itemStack.addEnchantment		(ench, lvl);
			} catch (Exception e) {
				scs.log(Level.WARNING, "Couldn't add enchantment="+ench.getName());
			}
		}
	}
	
	/**
	 * @return the proper name of the item
	 */
	public String getItemName () {
		String itemName	= getItemStack().getType().toString();
		
		if (getItemStack().getData().getData() != (byte)0x00)
			itemName	+= ":"+(int)getItemStack().getData().getData();
		
		return itemName;
	}
	
	
	
	/**
	 * @param value the new item stack
	 */
	public void setItemStack (ItemStack value) {
		itemStack	= value;
		
		setEnchantments	(value.getEnchantments()	);
		setMaterial		(value.getData()			);
		
		// NBT
		if (itemStack instanceof CraftItemStack)
			setNBTTagCompound(((CraftItemStack)itemStack).getHandle().tag);
		
	}
	
	/**
	 * @return the item stack for this shop
	 */
	public ItemStack getItemStack () {
		if (itemStack == null) {
			
			try  {
				// debug
				if (Properties.saveDebug) {
					String 	material	= storage.getString(namedStringMaterial); 
					ShowCaseStandalone.slog(Level.INFO, "materialData="+material);
					ShowCaseStandalone.slog(Level.INFO, "material    ="+Utilities.getMaterialsFromString(material).getItemType());
				}
				
				String 	material		= storage.getString(namedStringMaterial);
				if (material != null)
					itemStack			= Utilities.getItemStackFromString(material);
				else
					return null;
				
				// NBT
				if (itemStack instanceof CraftItemStack) {
					CraftItemStack	cItemStack	= (CraftItemStack)itemStack;
					cItemStack.getHandle().setTag(getNBTTagCompound());
				}
				
				if (getEnchantments() != null) {
					for (Enchantment enchantment : getEnchantments().keySet()) {
						int level	= getEnchantments().get(enchantment);
						
						if (Properties.allowUnsafeEnchantments)
							itemStack.addUnsafeEnchantment	(enchantment, level);
						else
							itemStack.addEnchantment		(enchantment, level);
						
						
					}
				}
			} catch (Exception e) {
				ShowCaseStandalone.slog(Level.SEVERE, "Couldn't load the ItemStack for this shop: "+e.getMessage());
				e.printStackTrace();
			}
		}
		return itemStack;
	}
	
	/**
	 * @param value the new activity for this shop
	 */
	protected void setActivity (Activity value) {
		this.activity	= value;
		storage.setString(namedStringActivity, value.toString());
	}
	
	/**
	 * @return the activity of this shop - by default or any error {@link Activity.DISPLAY}
	 */
	public Activity getActivity () {
		if (activity == null)
			this.activity = Utilities.getActivity(storage.getString(namedStringActivity, Activity.DISPLAY.toString()));
		return activity;
	}
	
	/**
	 * @param value the max amount to buy
	 */
	public void setMaxAmount (int value) {
		storage.setInteger(namedIntegerMaxAmount, value);
	}
	
	/**
	 * @return the max-amount to buy for this shop
	 */
	public int getMaxAmount () {
		return storage.getInteger(namedIntegerMaxAmount, 0);
	}
	
	/**
	 * @param value the amount of items
	 */
	public void setAmount (int value) {
		storage.setInteger(namedIntegerAmount, value);
		
		// updateInventory -> getItemName which could be not set on create
		if (getItemStack() != null)
			updateInventory();
	}
	
	/**
	 * @return the amount of items in this shop
	 */
	public int getAmount () {
		int amount = storage.getInteger(namedIntegerAmount, 0);
		if (amount < 0)				// there was an issue in b112 that it counts negative...
			return amount * -1;
		else
			return amount;
	}
	
	/**
	 * @param value the price of this shop
	 */
	public void setPrice (double value) {
		storage.setDouble(namedDoublePrice, value);
	}
	
	/**
	 * @return the price for one item in this shop
	 */
	public double getPrice () {
		return storage.getDouble(namedDoublePrice, Double.MAX_VALUE);
	}
	
	/**
	 * @param value true if the shop is unlimited, false if not
	 */
	public void setUnlimited (boolean value) {
		storage.setBoolean(namedBooleanIsUnlimited, value);
	}
	
	/**
	 * @return true if the shop is a unlimited shop, false if not
	 */
	public boolean isUnlimited () {
		return storage.getBoolean(namedBooleanIsUnlimited, false);
	}
	
	/**
	 * @param value if the shop is visible, false if not
	 */
	public void setVisible (boolean value) {
		if (value)
			this.show();
		else
			this.hide();
	}
	
	/**
	 * @return true if the shop is visible, false if not
	 */
	public boolean isVisible () {
		return isVisible;
	}
	
	/**
	 * @param value the new storage
	 */
	public void setStorage (Storage value) {
		storage = value;
	}
	
	/**
	 * @return the internal storage of this shop
	 */
	public Storage getStorage () {
		return storage;
	}

	/**
	 * @param p
	 * @return
	 */
	public boolean hasPermissions (Player p) {
		return (p.getName().equalsIgnoreCase(getOwner()) || p.hasPermission(Properties.permAdmin));
	}
	
	/**
	 * @return the inventory for this shop
	 */
	public Inventory getInventory () {
		if (inventory == null) {
			String name	= getActivity().toString()+" "+getItemName();
			
			if (getItemStack().getType() == Material.WRITTEN_BOOK)
				name = getActivity().toString()+" "+getNBTStorage().getBookTitle();			
			
			if (name.length() > maxInventoryTitleLength)
				name = name.substring(0, maxInventoryTitleLength-1);
			
			inventory = scs.getServer().createInventory(this, DoubleChestFields, name);
			updateInventory();
		}
		return inventory;
	}
	
	/**
	 * Updates the inventor
	 */
	protected void updateInventory () {
		getInventory().clear();
		
		if (getAmount() > 0)
			inChest = ItemStackHandler.addToInventory(getInventory(), getItemStack(), getAmount());
		else
			inChest = 0;
//		
//		
//		
//		{
//			safeAddItems(getInventory(), getAmount(), getItemStack());
//		}
//		this.inChest	= countItems(getItemStack());
	}
	
//	/**
//	 * Counts the items in this shop-inventory
//	 * -- ignoring 
//	 * @param is
//	 * @return
//	 */
//	public int countItems (ItemStack is) {
//		int			amount		= 0;
//		Inventory	inventory	= getInventory();
//		
//		for (ItemStack iis : inventory) {
//			if (iis != null) {
//				if (itemsEqual(iis, is))
//					amount += iis.getAmount();
//			}	
//		}
//		return amount;
//	}
	
//	/**
//	 * Returns every ItemStack which is not allowed throw
//	 * the given stacks back to the player
//	 * @param player
//	 * @param stacks
//	 */
//	public void sortChest (Player player, ItemStack ...stacks) {
//		Inventory	inventory	= getInventory();
////		player.updateInventory();	// update that the item was removed
//		
//		for (ItemStack iis : inventory) {
//			if (iis != null) {
//				boolean cont = false;
//				
//				for (ItemStack sis : stacks)		// ItemStack allowed?
//					if (itemsEqual(iis, sis)) {
//						cont = true;
//						break;
//					}
//				
//				if (cont)
//					continue;
//				
//				player.getInventory().addItem(cloneItemStack(iis));	// Send ItemStack back
//			}	
//		}
//		
//		player.updateInventory();	// update that the item has been added again
//	}
	
	/**
	 * When the inventory was closed
	 */
	public void onInventoryClosed (Player player) {
		int removed	= ItemStackHandler.removeFromInventory(getInventory(), getCompatibleItems(), -1, needsEqualNBTTag());
		
		// gives the player all items back which are not compatible
		for (ItemStack is : getInventory()) {
			if (is != null)
				player.getInventory().addItem(is);
		}
		
		// set the new amount - after item giving back, because updateInvenotry() is called
		setAmount(getAmount()+(removed	-inChest));
	}
	
	
	/**
	 * Starts a new benchmark if it is allowed via Properties.interactDebug
	 * @param name
	 */
	public void startBench (String name, String startInfo) {
		if (Properties.interactDebug) {
			bench = new  BenchMark (name);
			bench.start(startInfo);
		}
	}
	
	/**
	 * Marks the BenchMark if it is allowed via Properties.interactDebug
	 * @param info
	 */
	public void markBench (String info) {
		if (Properties.interactDebug)
			if (bench != null)
				bench.mark(info);
	}
	
	/**
	 * Marks the BenchMark if it is allowed via Properties.interactDebug
	 */
	public void markBench () {
		if (Properties.interactDebug)
			if (bench != null)
				bench.mark();
	}
	
	/**
	 * Stops the BenchMark
	 */
	public void stopBench () {
		if (Properties.interactDebug)
			if (bench != null)
				bench.end();
	}
	
	
	
        

	/**
	 * @param scs
	 * @param storage
	 */
	public Shop (Activity activity, ShowCaseStandalone scs, Storage storage) {
		this.activity	= activity;
		this.scs		= scs;
		this.storage	= storage;
		
		this.setActivity(activity);
		
//		getInventory();		// Force
	}
	
	/**
	 * @param scs
	 */
	public Shop (Activity activity, ShowCaseStandalone scs, int storageVersion) {
		this (activity, scs, new Storage(storageVersion));
	}
	
	/**
	 * Sends the given player information about the shop
	 * The Sha1-key will only sent if it is not null
	 * @param player
	 * @param activity
	 * @param price
	 * @param inventory
	 * @param sha1
	 */
	public void showDetails (Player player) {
		String 	c	= Term.COLOR_INACTIVE.get();
		
		// Get the color
		if (getActivity() == Activity.SELL		&& isActive())
			c	= Term.COLOR_SELL.get();
		
		else if (getActivity() == Activity.BUY		&& isActive())
			c	= Term.COLOR_BUY.get();
		
		else if (getActivity() == Activity.EXCHANGE	&& isActive())
			c	= Term.COLOR_EXCHANGE.get();
		
		
		
		List<String>	message	= new ArrayList<String>();
						message.add(Term.INFO_1.get() + c + getActivity().toString() 	+ Term.INFO_2.get() + c +scs.formatCurrency(getPrice())		+ Term.INFO_9.get() + c + getOwner());
		
		String 	text	= null;
		String	name	= getItemName();
		
		if (!isUnlimited()) {
			if (getActivity() == Activity.BUY)
				text = getAmount()+ "/" + getMaxAmount();
			else
				text = "" + getAmount();
		} else {
			text = Term.INFO_UNLIMITED.get();
		}
		
		if (getItemStack().getType() == Material.WRITTEN_BOOK)
			name = getNBTStorage().getBookTitle();
		
		message.add(Term.INFO_4.get() + c + name				+ Term.INFO_3.get() + c + text);
//						message.add(Term.INFO_4.get() + c + getItemName()				+ Term.INFO_3.get() + c + (!isUnlimited() ? (getActivity() == Activity.BUY ? getMaxAmount() : getAmount()) : Term.INFO_UNLIMITED.get()));
		
		StringBuffer	buffer 	= new StringBuffer();
		String			delim	= "";
	
		for (Enchantment en : getEnchantments().keySet()) {
			int lvl	= getEnchantments().get(en);
			
			buffer.append(delim + en.getName() + " lvl " + lvl);
			delim	= ", ";
		}
		
		if (buffer.toString().length() > 0)
			message.add(Term.INFO_8.get() + c + buffer.toString());
		
		if (player.hasPermission(Properties.permAdmin))
			message.add(c + getSHA1());
		
		Messaging.mlSend(player, message);
	}
	
	/**
	 * Creates a shop for the given activity
	 * @param act
	 * @param scs
	 * @return
	 */
	public static Shop getShop(Activity act, ShowCaseStandalone scs, int storageVersion) {
		return getShop (act, scs, new Storage(storageVersion));
	}
	
	public static Shop getShop(ShowCaseStandalone scs, Storage storage) {
		Activity	act	= Utilities.getActivity(storage.getString(namedStringActivity));
		return getShop(act, scs, storage);
	}
	
	/**
	 * Creates a shop for the given activity and the given storage
	 * @param act
	 * @param scs
	 * @param storage
	 * @return
	 */
	public static Shop getShop(Activity act, ShowCaseStandalone scs, Storage storage) {
		switch (act) {
			case DISPLAY:
				return new DisplayShop 		(scs, storage);
			case EXCHANGE:
				return new ExchangeShop 	(scs, storage);
			case SELL:
				return new SellShop			(scs, storage);
			case BUY:
				return new BuyShop			(scs, storage);
			default:	// Should never happen !!
				ShowCaseStandalone.get().log(Level.SEVERE, "Couldn't encode activity="+act.toString());
				return null;
		}
	}

	/**
	 * Tells the given player important information about the shop
	 * @param p
	 */
	public abstract void info(Player p);
	
	/**
	 * Let the given player interact with the shop that can be buy or sale
	 * @param p
	 * @param amount
	 */
	public abstract void interact (Player p, int interactAmount);
	
	/**
	 * Automatically sends the player error messages if set
	 * @return true if the player can interact with this shop, false otherwise
	 */
	public boolean checkInteractConditions (Player p, boolean sendPlayer) {
		if (!isVisible())
			return false;
		
		if (getInventory().getViewers().size() > 0) {
			if (sendPlayer)
				Messaging.send(p, Term.ERROR_CURRENTLY_INVENTORY_OPENED.get());
			return false;
		}
		
		updateInventory();
		
		return true;
	}

	/**
	 * Checks if there is already an item - server-crash?
	 * @return
	 */
	public boolean checkItem () {
		for (Entity e : getLocation().getWorld().getEntities())
		{
			double x = e.getLocation().getX();
			double z = e.getLocation().getZ();
			double yDiff = getSpawnLocation().getY() - e.getLocation().getY();
                        
			if (yDiff < 0)
				yDiff *= -1;

			if (x == getSpawnLocation().getX() && yDiff <= 1.5 && z == getSpawnLocation().getZ()) {
                            
                            ShowCaseStandalone.slog(Level.FINEST, "Potential hit on checkItem()");
				try {
					Item itemE = (Item)e;
					if (ItemStackHandler.itemsEqual(itemE.getItemStack(), getItemStack(), true)) {
                        ShowCaseStandalone.slog(Level.FINEST, "Existing stack: " + itemE.getItemStack().toString());
                        itemE.getItemStack().setAmount(1); //Removes duped items, which can occur.
						this.item = itemE;
						scs.log(Level.FINER, "Attaching to existing item.");
						return true;
					}
				} catch (Exception ex) {}
			}
		}
		return false;
	}

	
	/**
	 * Spawns the Item for this shop 
	 */
	public void show() {
		if (getSpawnLocation() == null)
			return;

		if (!checkItem()) {
			item 	= getWorld().dropItem(getSpawnLocation(), getItemStack());
			item.setVelocity(new Vector(0, 0.1, 0));
			item.setPickupDelay(1000);
			
		}
		isVisible 	= true;
	}

	
	/**
	 * Removes the Item from this shop 
	 */
	public void hide() {
		if (item != null) {       
	        ShowCaseStandalone.slog(Level.FINEST, "Hiding showcase: " + getSHA1());
	        item.remove();
	        
	        int		x	= getSpawnLocation().getBlockX();
	        int 	y 	= 0;
	        int 	z	= getSpawnLocation().getBlockZ();
	        World	w	= getSpawnLocation().getWorld();
	        
	        item.teleport(new Location(w, x, y, z));
			item 	= null;
		}
		isVisible	= false;
	}
	
	/**
	 * @return true if the shop is active i.e. does the sell shop have stuff to sell, does the buy shop have stuff to buy ... false if not
	 */
	public boolean isActive () {
		if (isUnlimited())
			return true;
		switch (getActivity()) {
			case BUY:
				return (getAmount() < getMaxAmount());
			case SELL:
			case EXCHANGE:
				return (getAmount() > 0);
			default:
				return true;
		}
	}
	
	
	private void setEnchantments (Map<Enchantment, Integer> enchantments) {
		this.enchantments	= enchantments;
		this.storage.setString(namedStringEnchantments, getEnchantmentsAsString());
	}

	public Map<Enchantment, Integer> getEnchantments () {
		if (enchantments == null) {
			String  enchantments	= storage.getString(namedStringEnchantments);
			this.enchantments		= new HashMap<Enchantment, Integer>();
			
			if (enchantments != null) {
				try {
						String ench[]		= enchantments.split(",");
						for (String string : ench) {
							Enchantment	enchantment	= Utilities.getEnchantmentFromString		(string);
							int			level		= Utilities.getEnchantmentLevelFromString	(string);
							
							if (enchantment != null)
								this.enchantments.put(enchantment, level);

						}
					
				} catch (Exception e) {
					ShowCaseStandalone.slog(Level.WARNING, "Couldn't load the enchantments");
				}
			}
		}
		return enchantments;
	}


	
	/**
	 * @param forceReset	re-set the enchantment in the storage
	 * @return the enchantments which is used by this shop
	 */
	public String getEnchantmentsAsString() {
		if (storage.getString(namedStringEnchantments) == null) { 
            StringBuilder sb = new StringBuilder();
            String delim = "";

            for(Map.Entry<Enchantment, Integer> entry : getEnchantments().entrySet()) {
                sb.append(delim);
                sb.append(entry.getKey().getId());
                sb.append(":");
                sb.append(entry.getValue());
                delim=",";
            }
            return sb.toString();
//            storage.setString(namedStringEnchantments, sb.toString());
		}
		return storage.getString(namedStringEnchantments);
	}
	
	// TODO: Dafuq does this do? Or what is it for? getDurability???
	public String getMaterial () {
        return getItemStack().getTypeId()+ ":" + getItemStack().getDurability();    
	}
	
	/**
	 * Saves the NBTTagCompound
	 * @param prefix
	 * @param comp
	 */
	public void setNBTTagCompound (NBTTagCompound comp) {
		setNBTStorage(new NBTStorage(comp, getStorage().getVersion()));
	}
	
	/**
	 * Gets the TagCompound
	 * @param prefix
	 * @return
	 */
	public NBTTagCompound getNBTTagCompound () {
		return getNBTStorage().getNBTTagCompound();
	}
	
	/**
	 * Sets the NBTStorage
	 * @param storage
	 */
	public void setNBTStorage (NBTStorage storage) {
		nbtStorage	= storage;
		this.storage.setStorage(namedStorgeNBT, storage);
	}
	
	/**
	 * @return The NBTStorage
	 */
	public NBTStorage getNBTStorage () {
		if (nbtStorage == null) {
			Storage	storage	= getStorage().getStorage(namedStorgeNBT);
			
			if (storage == null)
				nbtStorage = new NBTStorage(new NBTTagCompound(), getStorage().getVersion());
			else
				nbtStorage = new NBTStorage(storage);
		}
		return nbtStorage;
	}
	
	/**
	 * There was a version (b121) where the author, title and pages where save
	 * directly into the storage of the shop - this function removes the tags
	 * and saves the NBTTag to an extra storage in the storage of the shop
	 */
	public void updateToNBTTagStorage () {
		// no book saved?
		if (storage.getBoolean("isBook") == null || storage.getBoolean("isBook") == false)
			return;
		
		NBTTagCompound	tag		= new NBTTagCompound();
		String 			author	= storage.getString	("book-author"	);
		String			title	= storage.getString	("book-title"	);
		int				pages	= storage.getInteger("book-pages"	);
		NBTTagList		list	= new NBTTagList();
		
		for (int i = 0; i < pages; i++) {
			String			siteContent	= storage.getString("book-page-"+i);
			NBTTagString	tString		= new NBTTagString(siteContent);
			
			tString.data	= siteContent;
			
			list.add(tString);
		}
		
		tag.setString	("author", 	author);
		tag.setString	("title",	title);
		tag.set			("pages", 	list);
		setNBTTagCompound(tag);
	}
	
	/**
	 * @return The list of all members
	 */
	private List<String> getMemberList() {
		if (members == null) {
			members = new ArrayList<String>();
			
			String sMembers		= storage.getString	(namedStringMembers, "");
			String aMembers[]	= sMembers.split	(splitMember);
			
			for (String s : aMembers)
				members.add(s);
		}
		return members;
	}


	/**
	 * Saves the current MemberList to the storage
	 */
	private void saveMemberList () {
		StringBuffer	members		= new StringBuffer();
		boolean			addSplit	= false;
		
		for (String member : this.members) {
			if (addSplit)
				members.append(splitMember);
			members.append(member);
			addSplit = true;
		}
		
		storage.setString(namedStringMembers, members.toString());
	}
	
	/**
	 * @return The Members for this shop like %member1%,%member2%,....
	 */
	public String getMembers () {
		return storage.getString(namedStringMembers);
	}
	
	/**
	 * Adds a member to this shop
	 * @param name
	 */
	public void addMember (String name) {
		getMemberList().add(name);
		saveMemberList();
	}
	
	/**
	 * Removes a member from this shop
	 * @param name
	 */
	public void removeMember (String name) {
		getMemberList().remove(name);
		saveMemberList();
	}
	
	/**
	 * @return Whether the given player is a member of this shop  
	 */
	public boolean isMember (String name) {
		for (String s : getMemberList())
			if (s != null && s.equals(name))
				return true;
		return false;
	}
	
	/**
	 * @return Whether the given player is the owner of this shop
	 */
	public boolean isOwner (String name) {
		return getOwner().equals(name);
	}


	/**
	 * Checks if the given Player can Interact with the shop
	 * in the given way as
	 * @param player
	 * @param type
	 * @param isAdmin
	 * @return
	 */
	public boolean canDo (Player player, Type type, boolean isAdmin){
		if (isAdmin || isOwner(player.getName()))
			return true;
		
		if (!isMember(player.getName()))
			return false; 
		
		switch (type) {
			case ADD:
			case GET:
				return true;
			default:
				return false;
		}
	}
	
	/**
	 * @return A list of all ItemStacks that are compatible with
	 * the shops ItemStack
	 */
	public List<ItemStack> getCompatibleItems () {
		List<ItemStack>	list	= new ArrayList<ItemStack>();
						list.add(getItemStack());
						
		switch (getItemStack().getType()) {
			case WRITTEN_BOOK:
				list.add(new CraftItemStack(Material.BOOK));
				list.add(new CraftItemStack(Material.BOOK_AND_QUILL));
				break;
			default:
				break;
		}
		
		return list;
	}


	/**
	 * Checks if the given Material needs an
	 * equal NBTTag if it is used  in an ItemStack
	 * @param material
	 * @return
	 */
	public boolean needsEqualNBTTag (Material material) {
		switch (material) {
			case WRITTEN_BOOK:
				return false;
				
			default:
				return true;
		}
	}
	
	/**
	 * Equal to needsEqualNBTTag(getItemStack().getType())
	 * @return
	 */
	public boolean needsEqualNBTTag () {
		return needsEqualNBTTag(getItemStack().getType());
	}
        
	
	/**
	 * Adds Items from a Player to this shop - for a non buy/sell action
	 * @param player
	 * @param amount
	 * @return The amount which was actually removed from the players inventory
	 * @throws InsufficientPermissionException	If the player isn't the owner or a member
	 */
	public int getItemsFromPlayer (Player player, int amount, boolean isAdmin) throws InsufficientPermissionException {
		if (!canDo(player, Type.ADD, isAdmin))
			throw new InsufficientPermissionException();
		
		int removed	= ItemStackHandler.removeFromInventory(player.getInventory(), getCompatibleItems(), amount, needsEqualNBTTag());
		setAmount(getAmount()+removed);
		return removed;
	}
	
	
	
	/**
	 * Removes Items from this shop and adds them to the player - for a non buy/sell action
	 * @param player
	 * @param amount
	 * @return The amount which was actually added to the players inventory
	 * @throws InsufficientPermissionException	If the player isn't the owner or a member
	 */
	public int addItemsToPlayer (Player player, int amount, boolean isAdmin) throws InsufficientPermissionException {
		if (!canDo(player, Type.GET, isAdmin))
			throw new InsufficientPermissionException();
		
		if (amount > getAmount())
			amount = getAmount();
		
		int added	= ItemStackHandler.addToInventory(player.getInventory(), getItemStack(), amount);
		setAmount(getAmount()-added);
		return added;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
//	
//	
//	
//	/**
//     * This removes item stacks one by one.  It does this to make sure that only 
//     * items with the proper data field are removed.  This includes durability.
//     * @param p The player whose inventory you're removing.
//     * @param interactAmount The max amount to remove.
//     * @return the number of items actually removed.
//     */
//	public final int safeRemoveItems (Player p, int interactAmount) {
//		return safeRemoveItems(p, interactAmount, getItemStack());
//	}
//	
//	public final int safeRemoveItems(Player p, int interactAmount, ItemStack itemStack){
//		return safeRemoveItems(p.getInventory(), interactAmount, itemStack);
//	}
//	
//    /**
//     * This removes item stacks one by one.  It does this to make sure that only 
//     * items with the proper data field are removed.  This includes durability.
//     * @param p The player whose inventory you're removing.
//     * @param interactAmount The max amount to remove.
//     * @return the number of items actually removed.
//     */
//    public final int safeRemoveItems(Inventory inventory, int interactAmount, ItemStack itemStack){
//        //There is a chance that interactAmount == 0.  If so, just exit.
//        
//        if(interactAmount == 0)
//            return 0;
//        
//        HashMap<Integer, ? extends ItemStack> it = (HashMap<Integer, ? extends ItemStack>)inventory.all(itemStack.getTypeId());
//        
//        int needed = interactAmount;
//        
//        
//        for (Map.Entry<Integer, ? extends ItemStack> pairs : it.entrySet()) {
//            ItemStack 	invItem = pairs.getValue();
//            int			key 	= pairs.getKey();
//            
//            ShowCaseStandalone.slog(Level.FINER, "found itr: " + invItem.toString());
//            
//            if(itemsEqual(invItem, itemStack)){
//                ShowCaseStandalone.slog(Level.FINER, "Found a match for data in slot " 
//                        + key + ": " 
//                        + MaterialNames.getItemName(invItem.getTypeId(), invItem.getDurability()) 
//                        + "=" + getItemName());
//                //ShowCaseStandalone.spam("invItem.getamount: " + invItem.getAmount());
//                if(invItem.getAmount() <= needed){
//                    
//                    ShowCaseStandalone.slog(Level.FINER, "invItem is <= needed (" + needed + ")");
//                    
//                    needed -= invItem.getAmount();
//                    inventory.clear(key);
//                } else {
//                    
//                    ShowCaseStandalone.slog(Level.FINER, "invItem is > needed (" + needed + ")");
//                    ShowCaseStandalone.slog(Level.FINER, "Attempting to set inv position " + key + " to size " 
//                            + (invItem.getAmount() - needed));
//                    
//                    invItem.setAmount(invItem.getAmount() - needed);
//                    inventory.setItem(key, invItem);
//                    
//                    needed = 0;
//                }
//                
//                if(needed == 0)
//                    break;
//            }
//        }
//        //ShowCaseStandalone.spam("returning: " + (interactAmount - needed));
//        return (interactAmount - needed);
//    }
//    
//    /**
//     * Returns the number of items that exactly match the shop item.
//     * @param p The player whose inventory we're looking at.
//     */
//    public final int countSaleableItems(Player p, ItemStack itemStack){
//        HashMap<Integer, ? extends ItemStack> it = (HashMap<Integer, ? extends ItemStack>)p.getInventory().all(itemStack.getTypeId());
//        int saleable = 0;
//        
//        for (Map.Entry<Integer, ? extends ItemStack> pairs : it.entrySet()) {
//            ItemStack invItem = pairs.getValue();
//            
//            ShowCaseStandalone.slog(Level.FINER, "found itr: " + invItem.toString());
//            
//            if(itemsEqual(invItem, itemStack)){
//                ShowCaseStandalone.slog(Level.FINER, "Found a match for data in slot " 
//                            + pairs.getKey() + ": " 
//                            + MaterialNames.getItemName(invItem.getTypeId(), invItem.getDurability()) 
//                            + "=" + itemStack.getType()); // changed
//                saleable += invItem.getAmount();
//            }
//        }
//        return saleable;
//    }
//    
//    /**
//     * Returns the number of items that exactly match the shop item.
//     * @param player  The player whose inventory we're looking at.
//     */
//    public final int countSaleableItems (Player player) {
//    	return countSaleableItems(player, getItemStack());
//    }
//    
//    
//    public final int safeAddItems(Player p, int interactAmount){
//    	if (p != null)
//    		return safeAddItems(p.getInventory(), interactAmount, getItemStack());
//    	else
//    		return 0;
//    }
//    
//    /**
//     * Fills inventory with items from the shop, up to inventory 
//     * capacity, or interactAmount.  Respects stack sizes.
//     * @param p Player whose inventory we're filling.
//     * @param interactAmount Max amount of items to place.  If set to 0,
//     * the 
//     * @return the actual number of items placed.
//     */
//    public final int safeAddItems(Inventory inv, int interactAmount, ItemStack itemStack){
//        //There is a chance that interactAmount == 0.  If so, just exit.
//        //ShowCaseStandalone.slog(Level.INFO, "safeAddItems() started. interactamount: " + interactAmount);
//        if(interactAmount == 0)
//            return 0;
//
//        int stackAmount = itemStack.getType().getMaxStackSize();
//        
//        //Get the smaller of the two for stack size.  This will give us our largest legal stack size.
//        stackAmount = (stackAmount < interactAmount) ? stackAmount : interactAmount;
//        
//        // //Bukkit issue: Clone isn't working for unsafe enchantments.  Fixed in R4, but 
//        // //I'm keeping this work around for people on R3.
//        // Bukkit issue: Clone isn't working for NBTTags
//        ItemStack 	tmpIS	= cloneItemStack (itemStack);
//        			tmpIS.setAmount(1);
//        
//        
//        HashMap<Integer, ItemStack> leftOver = new HashMap<Integer, ItemStack>();
//        int remainingItems = interactAmount;
//        
//        while(remainingItems > 0){
//            leftOver = inv.addItem(tmpIS);
//            if(leftOver.isEmpty()){
//                remainingItems -= stackAmount;
//                if(remainingItems == 0)
//                    break;
//                stackAmount = (stackAmount < remainingItems) ? stackAmount : remainingItems;
//                tmpIS.setAmount(stackAmount);
//            } else {
//                //We're out of room.
//                remainingItems -= (stackAmount - leftOver.get(0).getAmount());
//                break;
//            }
//        }
//        //ShowCaseStandalone.slog(Level.INFO, "returning placed amount: " + (interactAmount - remainingItems));
//        return (interactAmount - remainingItems);
//    }
//    
    
    
    
    
    public void msgOwner(Player player, String msg){
        if(player != null)
            if(!ShowCaseStandalone.pv.ignoreMessages(player))
                Messaging.send(player, msg);
    }
    
    
        
}
