/*
 * Copyright (C) 2012 Sorklin <sorklin at gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.miykeal.showCaseStandalone.Storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import com.miykeal.showCaseStandalone.ShowCaseStandalone;
import com.miykeal.showCaseStandalone.ShopInternals.Storage;
import com.miykeal.showCaseStandalone.Shops.Shop;
import com.miykeal.showCaseStandalone.Utilities.Properties;
import com.miykeal.showCaseStandalone.Utilities.Utilities;
import com.miykeal.showCaseStandalone.interfaces.ShopStorageHandler;

public class ShowCaseImport implements ShopStorageHandler{
    private ShowCaseStandalone 		scs;
    private	File					datafile;
    private HashMap<String, Shop>	shops		= new HashMap<String, Shop>();
    
    public ShowCaseImport(ShowCaseStandalone instance) {
        this.scs = instance;
        initFile();
    }
    
    private void initFile() {
        datafile = new File(ShowCaseStandalone.get().getDataFolder(), "showcases.csv");
        if(!datafile.exists())
            ShowCaseStandalone.slog(Level.WARNING, "Could not find or attach to showcases.csv");
    }
    
    public boolean fileExists(){
        return datafile.exists();
    }
    
    /**
     * Taken from ShowCase by Narrowtux.
     */
     public Shop[] loadShops(){
    	 this.shops.clear();
         List<Shop> shops = new ArrayList<Shop>();

        if (datafile.exists()) {
            FileInputStream input;
            try {
                input = new FileInputStream(datafile.getAbsoluteFile());
                InputStreamReader ir = new InputStreamReader(input);
                BufferedReader r = new BufferedReader(ir);
                String locline;
                
                int x, y, z;
                Material type;
                short matdata;
                String player;
                World world;
                String showtype;
                String data;
                
                while (true) {
                    locline = r.readLine();
                    if (locline == null) {
                        break;
                    }
                    String line[] = locline.split(",");
                    if (line.length == 9 || line.length == 10) {
                            x = Integer.valueOf(line[0]);
                            y = Integer.valueOf(line[1]);
                            z = Integer.valueOf(line[2]);
                            type = Material.getMaterial(Integer.valueOf(line[3]));
                            matdata = Short.valueOf(line[4]);
                            player = line[5];
                            world = scs.getServer().getWorld(line[6]);
                            showtype = line[7].toLowerCase();
                            data = line[line.length - 1];
                    } else {
                            continue;
                    }
                    Location loc = new Location(world, x, y, z);
                    Shop p = argumentsToShop(loc, type, matdata, player, showtype, data);
                    if(p != null)
                        shops.add(p);
                }
                
                r.close();
                
            } catch (Exception e) {
                    e.printStackTrace();
            }
        }
        
        
        for (Shop shop : shops)
        	this.shops.put(shop.getSHA1(), shop);
        
        Shop[] ret = new Shop[shops.size()];
        ret = shops.toArray(ret);
        return (shops.size() > 0) ? ret : null;
    }
    
    private Shop argumentsToShop (Location loc, Material mat, short matdata, String owner, String type, String data) {
            
        String sha1 = "";
        Shop.Activity a;
        int amount = 1;
        int maxAmount = 1;
        ItemStack is;
        double price = 1.0;

        if(loc == null) {
            ShowCaseStandalone.slog(Level.INFO, "Showcase import: location is null.");
            return null;
        }
        
        try {sha1 = Utilities.sha1(loc.toString());} catch (IOException ioe) {}
        String[] args = data.split(";");
        
        //Based on shop type, I'm going to set basic parameters, then try to
        //parse the data into those parameters.  If anything fails, I'll go
        //with the default parameters.
        
        price = 1.0;
        if (type.equalsIgnoreCase("infinite")) {
            a = Shop.Activity.SELL;
            amount = -1;
            //data = String.valueOf(price)
            //Try to parse, but ignore if I can't.
            if(data != "")
                try {
                    price = Double.parseDouble(data);
                } catch (NumberFormatException nfe) {}
            
        } else if (type.equalsIgnoreCase("finite")) {
            a = Shop.Activity.SELL;
            //data = itemAmount + ";" + pricePerItem; FINITE
            if(args.length > 1)
                try {
                    amount = Integer.parseInt(args[0]);
                    price = Double.parseDouble(args[1]);
                } catch (Exception e) {}
            
        } else if (type.equalsIgnoreCase("exchange")) {
            a = Shop.Activity.BUY;
            amount = 0;
            //data =  Exchange-type;Exchange-data;buy-amount;exchange-amount;exchange-rate-left;exchange-rate-right
            if(args.length >= 3)
                try {
                    maxAmount = Integer.parseInt(args[2]);
                } catch (Exception e) {}
            
        } else if (type.equalsIgnoreCase("basic") || type.equalsIgnoreCase("tutorial")) {
            a = Shop.Activity.DISPLAY;
            amount = 0;
            price = 0.0;
            //No need to parse the data.
            
        } else {
            ShowCaseStandalone.slog(Level.INFO, "Could not get activity for shop " + sha1);
            return null;
        }


        try {
            is 	= Utilities.getItemStackFromString(mat + ":" + matdata);
        } catch (IOException ioe) {
            ShowCaseStandalone.slog(Level.INFO, "Could not load materials for shop " + sha1);
            return null;
        }

        if(owner.equals("")){
            ShowCaseStandalone.slog(Level.INFO, "Could not owner for shop " + sha1);
            return null;
        }
        
        
//        Shop p = new Shop(sha1, scs, a, amount, maxAmount, is, price, owner);
//        p.setBlock (loc.getBlock());
        
        Shop p = Shop.getShop(a, scs, Properties.storageVersion	);
        	 p.setSHA1		(sha1			);
//        	 p.setActivity	(a				);
        	 p.setAmount	(amount			);
        	 p.setMaxAmount	(maxAmount		);
        	 p.setItemStack	(is				);
        	 p.setPrice		(price			);
        	 p.setOwner		(owner			);
        	 p.setBlock		(loc.getBlock()	);
        return p;
    }

	@Override
	public Iterator<Shop> iterator() {
		return shops.values().iterator();
	}

	@Override
	public void saveShop(String sha1, Shop p) throws IOException { }

	@Override
	public Shop loadShop(String sha1) throws IOException {
		return shops.get(sha1);
	}

	@Override
	public void saveShops(Shop[] p) throws IOException { }

	@Override
	public void removeShop(String sha1) throws IOException { }

	@Override
	public void removeAllShops() throws IOException { }

	@Override
	public Storage convert(Storage storage, int oldVersion, int version) throws IOException {
		return null;
	}

	@Override
	public int importStroage(ShopStorageHandler s) throws IOException {
		return 0;
	}

	@Override
	public void update() { }
}
