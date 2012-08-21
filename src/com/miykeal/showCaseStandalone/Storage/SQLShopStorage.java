/*
 * Copyright (C) 2012 Sorklin, KellerKindt <sorklin at gmail.com>
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

import java.io.IOException;
import java.util.Iterator;

import com.miykeal.showCaseStandalone.ShopInternals.Storage;
import com.miykeal.showCaseStandalone.Shops.Shop;
import com.miykeal.showCaseStandalone.interfaces.ShopStorageHandler;

/**
 *
 * @author Sorklin <sorklin at gmail.com>
 */
public class SQLShopStorage implements ShopStorageHandler {

    @Override
    public void saveShop(String sha1, Shop p) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Shop loadShop(String sha1) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void saveShops(Shop[] p) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Shop[] loadShops() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeShop(String sha1) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeAllShops() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void update() {
    	throw new UnsupportedOperationException("Not supported yet.");
    }

	@Override
	public Iterator<Shop> iterator() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Storage convert(Storage storage, int oldVersion, int version) throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int importStroage(ShopStorageHandler s) throws IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
    
}
