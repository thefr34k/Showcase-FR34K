package com.miykeal.showCaseStandalone.ShopInternals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.server.NBTTagCompound;

import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class ItemStackHandler {
	
	/**
	 * Counts the Items that are compatible with the given ItemStacks
	 * @param inventory
	 * @param allowedItems
	 * @param checkNBT
	 * @return
	 */
	public static int countCompatibleItemStacks (Inventory inventory, List<ItemStack> allowedItems, boolean checkNBT) {
		int	found	= 0;
		
		// Thru all ItemStacks in the inventory
		for (ItemStack is1 : inventory) {
			
			// Check if its allowed
			for (ItemStack is2 : allowedItems)
				if (itemsEqual(is1, is2, checkNBT))
					found += is1.getAmount();
		}
		
		return found;
	}
	
	/**
	 * Counts the Items that are compatible with the given ItemStack
	 * @param inventory
	 * @param itemStack
	 * @param checkNBT
	 * @return
	 */
	public static int countCompatibleItemStacks (Inventory inventory, ItemStack itemStack, boolean checkNBT) {
		List<ItemStack>	list	= new ArrayList<ItemStack>();
						list.add(itemStack);
		return countCompatibleItemStacks(inventory, list, checkNBT);
	}
	
	/**
	 * Tries to add ItemStacks of the given amount to the Inventory
	 * @param inventory
	 * @param type
	 * @param amount
	 * @return The amount of items that were actually added
	 */
	public static int addToInventory (Inventory inventory, ItemStack type, int amount) {
		ItemStack	is	= cloneItemStack(type);
					is.setAmount(amount);
					
		HashMap<Integer, ItemStack> map = inventory.addItem(is);
		
		if (map.values().isEmpty())
			return amount;
		else	// while there is only 1 ItemStack removed, there is only 1 Integer entry
			return amount - map.values().iterator().next().getAmount();
	}

	/**
	 * Tries to remove the given amount of ItemStacks
	 * from the given Inventory that are listed in allowedItems
	 * @param inventory
	 * @param allowedItems
	 * @param amount the amount or -1 for all
	 * @param checkNBT
	 * @return The amount of items that were actually removed 
	 */
	public static int removeFromInventory (Inventory inventory, List<ItemStack> allowedItems, int amount, boolean checkNBT) {
		List<ItemStack>	remove		= new ArrayList<ItemStack>();
		int				removed		= 0;
		boolean			canRemove	= false;
		boolean			unlimited	= amount < 0;
		
		
		for (ItemStack is1 : inventory) {
			if (is1 == null)
				continue;
			
			if (amount == 0 && !unlimited)
				break;
			
			
			// checks if is1 can be removed
			canRemove	= false;
			for (ItemStack is2 : allowedItems)
				if (itemsEqual(is1, is2, checkNBT)) {
					canRemove = true;
					break;
				}
			
			
			if (!canRemove)
				continue;
			
			
			if (is1.getAmount() <= amount) {
				remove.add(is1);
				amount -= is1.getAmount();
				removed+= is1.getAmount();
			} else if (!unlimited) {
				is1.setAmount(is1.getAmount()-amount);
				removed += amount;
				amount 	 = 0;
				
			} else if (unlimited) {
				removed += is1.getAmount();
				remove.add(is1);
			}
		}
		
		
		// remove items
		for (ItemStack is : remove)
			inventory.removeItem(is);
		
		
		return removed;
	}
	
	/**
	 * Tries to remove the given amount of ItemStacks
	 * from the given Inventory of the given ItemStack
	 * @param inventory
	 * @param itemStack
	 * @param amount the amount or -1 for all
	 * @param checkNBT
	 * @return The amount of items that were actually removed 
	 */
	public static int removeFromInventory (Inventory inventory, ItemStack itemStack, int amount, boolean checkNBT) {
		List<ItemStack>	list	= new ArrayList<ItemStack>();
						list.add(itemStack);
		return removeFromInventory(inventory, list, amount, checkNBT);
	}
	
	/**
     * Returns whether the underlying item (TypeID, Durability and enchantments) are equal.
     * This is almost identical to .equals() for ItemStack, but ignores the amount in the stack.
     * @param is1
     * @param is2
     */
    public static boolean itemsEqual(ItemStack is1, ItemStack is2, boolean checkNBT) {
    	if (is1 == null || is2 == null)
    		return is1 == is2;
    	
        boolean eqId	= is1.getTypeId() == is2.getTypeId();
        boolean eqData	= is1.getData().equals(is2.getData());
        boolean eqDurab	= is1.getDurability() == is2.getDurability();
        boolean eqEnch	= enchantmentsEqual(is1.getEnchantments(), is2.getEnchantments());
        boolean eqNBT	= true;
        boolean is1CIS	= is1 instanceof CraftItemStack;
        boolean is2CIS	= is2 instanceof CraftItemStack;
        
        if (is1CIS && is2CIS) {
        	NBTTagCompound 	nbt1		= ((CraftItemStack)is1).getHandle().tag;
        	NBTTagCompound 	nbt2		= ((CraftItemStack)is2).getHandle().tag;
        	boolean			nullNBT1	= nbt1 == null;
        	boolean			nullNBT2	= nbt2 == null;
        	
        	if (nullNBT1 == nullNBT2) {
        		if (!nullNBT1) {
        			eqNBT = nbt1.equals(nbt2);
        		}
        	} else
        		eqNBT = false;
        }
        
        return eqId && eqData && eqDurab && eqEnch && ((eqNBT && (is1CIS == is2CIS)) || !checkNBT);
    }
    
    /**
     * @param en1
     * @param en2
     * @return Whether the given maps of enchantments are equal
     */
    public static boolean enchantmentsEqual (Map<Enchantment, Integer> en1, Map<Enchantment, Integer> en2) {
    	if (en1.size() != en2.size())
    		return false;
    	
    	for (Enchantment een1 : en1.keySet()) {
    		boolean found	= false;
    		
    		for (Enchantment een2 : en2.keySet())
    			if (een1.getId() 			== een2.getId()
    			 && een1.getMaxLevel()		== een2.getMaxLevel()
    			 && een1.getStartLevel()	== een2.getStartLevel()
    			 && en1.get(een1)			== en2.get(een2))
    				found = true;
    		
    		if (!found)
    			return false;
    	}
    	return true;
    }
    
    /**
     * Since there are some issues
     * @param is
     * @return
     */
    public static ItemStack cloneItemStack (ItemStack is) {
    	ItemStack cloned = is.clone();

    	if (cloned instanceof CraftItemStack) {
    		CraftItemStack	cCloned	= (CraftItemStack)cloned;
    		CraftItemStack	cIs		= (CraftItemStack)is;
    		
    		if (cIs.getHandle().tag != null) {
    			NBTTagCompound	com	= (NBTTagCompound)cIs.getHandle().tag.clone();
    			cCloned.getHandle().setTag(com);
    		}
    	}
    	
    	return cloned;
    }
}
