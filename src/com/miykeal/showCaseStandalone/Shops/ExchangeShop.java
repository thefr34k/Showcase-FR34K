package com.miykeal.showCaseStandalone.Shops;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagString;

import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.miykeal.showCaseStandalone.ShowCaseStandalone;
import com.miykeal.showCaseStandalone.ShopInternals.ItemStackHandler;
import com.miykeal.showCaseStandalone.ShopInternals.NBTStorage;
import com.miykeal.showCaseStandalone.ShopInternals.Storage;
import com.miykeal.showCaseStandalone.Utilities.Messaging;
import com.miykeal.showCaseStandalone.Utilities.Properties;
import com.miykeal.showCaseStandalone.Utilities.Term;
import com.miykeal.showCaseStandalone.Utilities.Utilities;

public class ExchangeShop extends Shop {
	
	private static final String namedIntegerExchangeAmount		= "exchange-amount";
	private static final String namedStringExchangeMaterial		= "exchange-material";
	private static final String namedStringExchangeEnchantments	= "exchange-enchantments";
	
	private static final String namedStorgeExchangeNBT			= "exchange-nbt-storage";
	
	private static final String	namedStringExchangePrefix		= "exchange-";
	
	private Map<Enchantment, Integer>	enchantments;
	private Storage							storage;
	private ShowCaseStandalone				scs;
	
	// temporary variables
	private ItemStack		exItemStack;
	private int				inChest;
	private int				exInChest;
	private NBTStorage		exNbtStorage;

	public ExchangeShop (ShowCaseStandalone scs, int storageVersion) {
		this(scs, new Storage(storageVersion));
	}
	
	public ExchangeShop (ShowCaseStandalone scs, Storage storage) {
		super(Activity.EXCHANGE, scs, storage);
		this.scs		= scs;
		this.storage	= storage;
	}
	
	
	@Override
	public void info(Player player) {
		// get color
		String 	c	= isActive() ? Term.COLOR_EXCHANGE.get() : Term.COLOR_INACTIVE.get();
		
		List<String>	message	= new ArrayList<String>();
		message.add(Term.INFO_1.get() + c + getActivity().toString() 	+ Term.INFO_2.get() + c + (int)getPrice()		+ Term.INFO_9.get() + c + getOwner());
		
		String nameItem	= getItemName();
		String nameEx	= getExchangeItemName();
		
		if (getItemStack().getType() == Material.WRITTEN_BOOK)
			nameItem = getExchangeNBTStorage().getBookTitle();
		
		if (getExchangeItemStack().getType() == Material.WRITTEN_BOOK)
			nameItem = storage.getString(namedStringExchangePrefix+getExchangeNBTStorage().getBookTitle());
		
		message.add(Term.INFO_4.get() + c + nameItem				+ Term.INFO_14.get()+ c + nameEx	+ Term.INFO_3.get() + c + (!isUnlimited() ? (getActivity() == Activity.BUY ? getMaxAmount() : getAmount()) : Term.INFO_UNLIMITED.get()));

		StringBuffer	buffer 	= new StringBuffer();
		String			delim	= "";
				
		// normal item
		for (Enchantment en : getEnchantments().keySet()) {
			int lvl	= getEnchantments().get(en);
			
			buffer.append(delim + en.getName() + " lvl " + lvl);
			delim	= ", ";
		}
		
		if (buffer.toString().length() > 0) {
			message.add(Term.INFO_8.get() + c + buffer.toString());
			buffer	= new StringBuffer();
		}
		
		
		// exchange item
		for (Enchantment en : getExchangeEnchantments().keySet()) {
			int lvl	= getExchangeEnchantments().get(en);
			
			buffer.append(delim + en.getName() + " lvl " + lvl);
			delim	= ", ";
		}
		
		if (buffer.toString().length() > 0)
			message.add(Term.INFO_15.get() + c + buffer.toString());
		
		// sha1 key only for the root
		if (player.hasPermission(Properties.permAdmin))
			message.add(c + getSHA1());
		
		Messaging.mlSend(player, message);
	}

	@Override
	public void interact(Player p, int interactAmount) {
		if (!checkInteractConditions(p, true))
			return;
		
		// bench
		startBench("interact, player="+p.getName() +",interactAmount="+interactAmount, "init");
		
		
		// Collect information
		int			purchPrice		= (int)getPrice() * interactAmount;
		int			workingAmount	= getAmount() < interactAmount ? getAmount() : interactAmount;
		
		// bench
		markBench("exchange");

		// check for early exit conditions
		if (!isUnlimited() && getAmount() <= 0) {
			Messaging.send	(p, 			Term.SHOP_EMPTY_COSTUMER.get());
			msgOwner		(getPOwner(), 	Term.SHOP_EMPTY_OWNER.get(getItemName()));
			markBench("!unlimited && amt <= 0");
			return;
		}
		
		// bench
		markBench("math conditions");
		
		
		//Does the player have enough exchange-items to buy the item?
		if (ItemStackHandler.countCompatibleItemStacks(p.getInventory(), getExchangeItemStack(), exNeedsEqualNBTTag()) < purchPrice) {
//		if (countSaleableItems(p, getExchangeItemStack()) < purchPrice) {
			Messaging.send(p, Term.ERROR_INSUFFICIENT_ITEMS_EXCHANGE.get());
			markBench("item check - insufficient items");
			return;
		}
		
		// check how many items could be added
		workingAmount	= ItemStackHandler.addToInventory (p.getInventory(), getItemStack(), workingAmount);
		purchPrice		= workingAmount * (int)getPrice();
		ItemStackHandler.removeFromInventory(p.getInventory(), getExchangeItemStack(), purchPrice, exNeedsEqualNBTTag());
		
		// bench
		markBench ("safeAddItems");
		
		if(workingAmount == 0){
			Messaging.send(p, Term.ERROR_INSUFFICIENT_ROOM_BUY.get());
			return;
		}
		
		if (!isUnlimited()) {
			setAmount			(getAmount()			-workingAmount);
			setExchangeAmount	(getExchangeAmount()	+purchPrice);
		}
		
		// bench
        markBench("items change");
        
//        TODO:
//        // setup undo
//        ShowCaseStandalone.pv.setLastTransaction(p, new Transaction(p, this, workingAmount));
//        
//        // bench
//        markBench("setup undo");
        
        Messaging.send			(p, 			Term.MESSAGE_SELL_COSTUMER.get(getItemName(), 	String.valueOf(workingAmount), 	scs.formatCurrency(purchPrice))			);
		ShowCaseStandalone.tlog	(p.getName(), 	getOwner(), getActivity().toString(), 			workingAmount, purchPrice, 		getMaterial(), getSHA1(), getAmount()	);
		
		// bench
		markBench("log transaction");
		
		if (!isUnlimited()){
			msgOwner(getPOwner(), Term.MESSAGE_SELL_OWNER_1.get(getItemName(),	String.valueOf(getAmount())));
			msgOwner(getPOwner(), Term.MESSAGE_SELL_OWNER_2.get(p.getName(), 	String.valueOf(workingAmount), scs.formatCurrency(purchPrice)));
//			msgOwner(getPOwner(), "`Y" + Term.INFO_13.get() + getSHA1());
        }
		
//		// update it!!! :D
//		updateInventory();
		
		// stop bench
		stopBench();
	}
	
	/**
	 * @return
	 */
	public boolean exNeedsEqualNBTTag () {
		return needsEqualNBTTag(getExchangeItemStack().getType());
	}
	
	/**
	 * @return The amount of exchange-items
	 */
	public int getExchangeAmount () {
		return storage.getInteger(namedIntegerExchangeAmount, 0);
	}
	
	/**
	 * Sets the amount of the exchange-items
	 * @param value
	 */
	public void setExchangeAmount (int value) {
		storage.setInteger(namedIntegerExchangeAmount, value);
		
		// updateInventory -> getItemName which could be not set on create
		if (getItemStack() != null)
			updateInventory();
	}

	/**
	 * Sets the Material for the exchange item
	 * @param value
	 */
	public void setExchangeMaterial (MaterialData value) {
		storage.setString(namedStringExchangeMaterial, value.getItemType().toString() + ":"+((int)value.getData() & 0xFF));// Utilities.getStringFromMaterial(value));
		exItemStack	= value.toItemStack();
		
		for (Enchantment ench : getExchangeEnchantments().keySet()) {
			int lvl	= getExchangeEnchantments().get(ench);
			
			try {
				if (Properties.allowUnsafeEnchantments)
					exItemStack.addUnsafeEnchantment(ench, lvl);
				else
					exItemStack.addEnchantment		(ench, lvl);
			} catch (Exception e) {
				scs.log(Level.WARNING, "Couldn't add enchantment="+ench.getName());
			}
		}
	}
	
	/**
	 * @return The ItemStack of the exchange item
	 */
	public ItemStack getExchangeItemStack () {
		if (exItemStack == null) {
			try  {
				// debug
				if (Properties.saveDebug) {
					String 	material	= storage.getString(namedStringExchangeMaterial); 
					ShowCaseStandalone.slog(Level.INFO, "materialData="+material);
					ShowCaseStandalone.slog(Level.INFO, "material    ="+Utilities.getMaterialsFromString(material).getItemType());
				}
				
				String 	material		= storage.getString(namedStringExchangeMaterial);
						exItemStack		= Utilities.getItemStackFromString(material);
					
				// NBT
				if (exItemStack instanceof CraftItemStack) {
					CraftItemStack cExItemStack	= (CraftItemStack)exItemStack;
					cExItemStack.getHandle().setTag(getNBTTagCompound());
				}
				
				if (getExchangeEnchantments() != null) {
					for (Enchantment enchantment : getExchangeEnchantments().keySet()) {
						int level	= getExchangeEnchantments().get(enchantment);
						
						if (Properties.allowUnsafeEnchantments)
							exItemStack.addUnsafeEnchantment	(enchantment, level);
						else
							exItemStack.addEnchantment			(enchantment, level);
					}
				}
			} catch (Exception e) {
				ShowCaseStandalone.slog(Level.SEVERE, "Couldn't load the ItemStack for this shop: "+e.getMessage());
				e.printStackTrace();
			}
		}
		return exItemStack;
	}
	
	/**
	 * Sets the exchange-ItemStack
	 * @param ex
	 */
	public void setExchangeItemStack (ItemStack ex) {
		this.exItemStack	= ex;
		setExchangeEnchantments	(ex.getEnchantments()	);
		setExchangeMaterial		(ex.getData()			);
		
		if (ex instanceof CraftItemStack)
			setExchangeNBTTagCompound(((CraftItemStack)ex).getHandle().tag);
	}
	
	/**
	 * @return The name of the exchange-item
	 */
	public String getExchangeItemName () {
		String itemName	= getExchangeItemStack().getType().toString();
		
		if (getExchangeItemStack().getData().getData() != (byte)0x00)
			itemName	+= ":"+(int)getExchangeItemStack().getData().getData();
		
		return itemName;
	}
	
	/**
	 * Sets the exchange-enchantments
	 * @param enchantments
	 */
	public void setExchangeEnchantments (Map<Enchantment, Integer> enchantments) {
		this.enchantments	= enchantments;
		this.storage.setString(namedStringExchangeEnchantments, getExchangeEnchantmentsAsString());
	}
	
	/**
	 * @return The HashMap of all exchange-enchantments
	 */
	public Map<Enchantment, Integer> getExchangeEnchantments () {
		if (enchantments == null) {
			if (storage.getString(namedStringExchangeEnchantments) != null) {
				try {
					String  enchantments	= storage.getString(namedStringExchangeEnchantments);
					this.enchantments		= new HashMap<Enchantment, Integer>();
					
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
	 * @return The exchange-enchantments as String
	 */
	public String getExchangeEnchantmentsAsString () {
		if (storage.getString(namedStringExchangeEnchantments) == null) { 
            StringBuilder sb = new StringBuilder();
            String delim = "";

            for(Map.Entry<Enchantment, Integer> entry : getExchangeEnchantments().entrySet()){
                sb.append(delim);
                sb.append(entry.getKey().getId());
                sb.append(":");
                sb.append(entry.getValue());
                delim=",";
            }
		}
		return storage.getString(namedStringExchangeEnchantments);
	}
	
	@Override
	public void updateInventory () {
		getInventory().clear();
		
		if (getAmount() > 0)
			inChest	= ItemStackHandler.addToInventory(getInventory(), getItemStack(), getAmount());	
		else
			inChest = 0;
		
		
		if (getExchangeAmount() > 0)
			exInChest = ItemStackHandler.addToInventory(getInventory(), getExchangeItemStack(), getExchangeAmount());
		else
			exInChest = 0;
	}
	
	@Override
	public void onInventoryClosed (Player player) {
		int removed		= ItemStackHandler.removeFromInventory(getInventory(), getCompatibleItems(), 	-1, needsEqualNBTTag());
		int removedEx	= ItemStackHandler.removeFromInventory(getInventory(), getExchangeItemStack(), 	-1, exNeedsEqualNBTTag());
		
		// gives the player all items back which are not compatible
		for (ItemStack is : getInventory())
			if (is != null)
				player.getInventory().addItem(is);
		

		// set the new amount
		setAmount			(getAmount()		+(removed	-inChest));
		setExchangeAmount	(getExchangeAmount()+(removedEx	-exInChest));
	}
	/**
	 * Saves the NBTTagCompound
	 * @param prefix
	 * @param comp
	 */
	public void setExchangeNBTTagCompound (NBTTagCompound comp) {
		setExchangeNBTStorage(new NBTStorage(comp, getStorage().getVersion()));
	}
	
	/**
	 * Gets the TagCompound
	 * @param prefix
	 * @return
	 */
	public NBTTagCompound getExchangeNBTTagCompound () {
		return getExchangeNBTStorage().getNBTTagCompound();
	}
	
	/**
	 * @return The NBTExchangeStorage
	 */
	public NBTStorage getExchangeNBTStorage () {
		if (exNbtStorage == null) {
			Storage	storage	= getStorage().getStorage(namedStorgeExchangeNBT);
			
			if (storage == null)
				exNbtStorage = new NBTStorage(new NBTTagCompound(), getStorage().getVersion());
			else
				exNbtStorage = new NBTStorage(storage);
		}
		return exNbtStorage;
	}
	
	/**
	 * Sets the ExchangeNBTStorage
	 * @param storage
	 */
	public void setExchangeNBTStorage (NBTStorage storage) {
		exNbtStorage	= storage;
		storage.setStorage(namedStorgeExchangeNBT, storage);
	}
	
	/**
	 * There was a version (b121) where the author, title and pages where save
	 * directly into the storage of the shop - this function removes the tags
	 * and saves the NBTTag to an extra storage in the storage of the shop
	 */
	public void updateToNBTTagStorage () {
		super.updateToNBTTagStorage();
		
		// no book saved?
		if (storage.getBoolean("exchange-isBook") == null || storage.getBoolean("exchange-isBook") == false)
			return;
		
		NBTTagCompound	tag		= new NBTTagCompound();
		String 			author	= storage.getString	("exchange-book-author"	);
		String			title	= storage.getString	("exchange-book-title"	);
		int				pages	= storage.getInteger("exchange-book-pages"	);
		NBTTagList		list	= new NBTTagList();
		
		for (int i = 0; i < pages; i++) {
			String			siteContent	= storage.getString("exchange-book-page-"+i);
			NBTTagString	tString		= new NBTTagString(siteContent);
			
			tString.data	= siteContent;
			
			list.add(tString);
		}
		
		tag.setString	("author", 	author);
		tag.setString	("title",	title);
		tag.set			("pages", 	list);
		setExchangeNBTTagCompound(tag);
	}
}
