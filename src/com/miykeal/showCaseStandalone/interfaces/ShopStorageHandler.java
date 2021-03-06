package com.miykeal.showCaseStandalone.interfaces;

import java.io.IOException;

import com.miykeal.showCaseStandalone.ShopInternals.Storage;
import com.miykeal.showCaseStandalone.Shops.Shop;

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


public interface ShopStorageHandler extends Iterable<Shop> {
	
	public void 	saveShop 		(String sha1, Shop p)	throws IOException;
	public Shop 	loadShop 		(String sha1)			throws IOException;
	
	public void 	saveShops 		(Shop p[])				throws IOException;
	public Shop[]	loadShops 		()						throws IOException;
	
	public void     removeShop		(String sha1)			throws IOException;
    public void     removeAllShops	()						throws IOException;
    
    /**
     * Converts the given Storage to the new version
     * @param storage
     * @param oldVersion
     * @param version
     * @throws IOException
     * @return 	The converted Storage
     */
    public Storage	convert			(Storage storage, int oldVersion, int version)	throws IOException;
    
    /**
     * @param s		The ShopStorage to import
     * @return		The amount of imported shops
     * @throws IOException
     */
    public int		importStroage	(ShopStorageHandler s)			throws IOException;
        
    /**
     * Commits changes to the database or file
     */
    public void update();
}
