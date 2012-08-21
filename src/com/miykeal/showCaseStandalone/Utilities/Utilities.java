package com.miykeal.showCaseStandalone.Utilities;


import java.io.IOException;
import java.security.MessageDigest;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.miykeal.showCaseStandalone.Exceptions.MissingOrIncorrectArgumentException;
import com.miykeal.showCaseStandalone.Shops.Shop.Activity;

/**
 * Copyright (C) 2011 Kellerkindt <kellerkindt@miykeal.com>
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */


public class Utilities {
	
	
	private Utilities () {}
	
	
	/*
	 * Quelle: http://www.anyexample.com/programming/java/java_simple_class_to_compute_sha_1_hash.xml
	 */
	public static String convertToHex(byte[] data) { 
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) { 
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do { 
                if ((0 <= halfbyte) && (halfbyte <= 9)) 
                    buf.append((char) ('0' + halfbyte));
                else 
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        } 
        return buf.toString();
    } 
 
    public static String sha1(String text)  throws IOException  { 
    	try {
		    MessageDigest md;
		    md = MessageDigest.getInstance("SHA-1");
		    byte[] sha1hash = new byte[40];
		    md.update(text.getBytes("iso-8859-1"), 0, text.length());
		    sha1hash = md.digest();
		    return convertToHex(sha1hash);
    	} catch (Exception e) {
    		throw new IOException (e.toString());
    	}
    }
    
    public static String getRandomSha1 (String s) throws IOException {
    	return sha1 (new Random().nextDouble() + s + System.currentTimeMillis());
    }
    
    
    /**
     * Returns MaterialData for log:2,Wool:4 etc
     */
    public static ItemStack getItemStackFromString (String material) throws IOException {
    	String args[] = new String[2];
    	if (material.contains(":"))
    		args = material.split(":");
    	else {
    		args[0] = material;
    		args[1] = "0";
    	}
    	
    	try {
    		Material m    	= Material.getMaterial(args[0].toUpperCase());
    		if (m == null)
    			m 		  	= Material.getMaterial(Integer.parseInt(args[0]));
    		int     data 	= Integer.parseInt(args[1]);
    		int		amount	= 1;
    		
    		if (Properties.stackToMaxAmount)
    			amount = m.getMaxStackSize();
    		
    		// for the books (getHandle().tag...)
    		return new CraftItemStack(m, amount, (short)data);
//    		return new ItemStack(m, 1, (short)data);
    		//return new MaterialData (m, (byte)data);
    	} catch (Exception e) {
    		throw new IOException (e);
    	}
    }
    
    public static MaterialData getMaterialsFromString (String material) throws IOException {
    	String args[] = new String[2];
    	if (material.contains(":"))
    		args = material.split(":");
    	else {
    		args[0] = material;
    		args[1] = "0";
    	}
    	
    	try {
    		Material m    = Material.getMaterial(args[0].toUpperCase());
    		if (m == null)
                    m = Material.getMaterial(Integer.parseInt(args[0]));
    		int data = Integer.parseInt(args[1]);
    		return new MaterialData (m, (byte)data);
    	} catch (Exception e) {
    		throw new IOException (e);
    	}
    }
    
    public static String getStringFromMaterial (MaterialData material) {
    	byte	data	= material.getData();
    	String 	name	= material.toString();
    	return name+":"+((int)data);
    }
    
    public static ItemStack getItemStack(Player player, String arg) throws MissingOrIncorrectArgumentException {
        try {
            if(arg.equalsIgnoreCase("this")){
//                ShowCaseStandalone.get().log(Level.INFO, "Material in hand: " + player.getItemInHand().toString());
                return player.getItemInHand();
            } else {
                return Utilities.getItemStackFromString(arg.toUpperCase());
            }
        } catch (Exception ex) {
            throw new MissingOrIncorrectArgumentException();
        }
    }
    
    public static Activity getActivity (String string) {
    	for (Activity a : Activity.values())
    		if (a.toString().equalsIgnoreCase(string))
    			return a;
    	return null;
    }
    
    
    public static Enchantment getEnchantmentFromString(String e){
        
        Enchantment ench = null;
        
        String args[] = new String[2];
        if (e.contains(":"))
                args = e.split(":");
        else {
		args[0] = e;
		args[1] = "1";
        }
        
        try{
            ench = Enchantment.getById(Integer.parseInt(args[0]));
        } catch (NumberFormatException nfe) {
        } catch (Exception ex){
            ex.printStackTrace();
        }
        
        return ench;
    }
    
    public static int getEnchantmentLevelFromString(String e){
        
        int strength = 1;
        String args[] = new String[2];
        if (e.contains(":"))
                args = e.split(":");
        else {
		args[0] = e;
		args[1] = "1";
        }
        
        try {
            strength = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            //Can't parse to a number.  Assume a strength of 1.
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return strength;
    }
}
