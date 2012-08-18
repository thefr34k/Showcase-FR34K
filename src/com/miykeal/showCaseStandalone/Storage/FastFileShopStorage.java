package com.miykeal.showCaseStandalone.Storage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.miykeal.showCaseStandalone.ShowCaseStandalone;
import com.miykeal.showCaseStandalone.ShopInternals.Storage;
import com.miykeal.showCaseStandalone.Shops.Shop;
import com.miykeal.showCaseStandalone.Utilities.Properties;
import com.miykeal.showCaseStandalone.Utilities.SparingList;
import com.miykeal.showCaseStandalone.interfaces.ShopStorageHandler;

public class FastFileShopStorage implements ShopStorageHandler{
	
	
	private static final String	directory			= "ffs-storage";
	private static final String	fileBeginning		= "ffss_";
	private static final String nodeNameRoot		= "scs-shop";
//	private static final long	updateAfter			= 1000 * 60 * 15;	// every 15min
	
	private HashMap<String, Shop>			shops	= new HashMap<String, 	Shop>();			// All shops	<sha1, toString>>
	private List<String>					changed	= new SparingList<String>();
	
	private ShowCaseStandalone				scs;
//	private long							lastUpdate	= 0;
	
	
	public FastFileShopStorage (ShowCaseStandalone scs) {
		this.scs	= scs;
	}
	

	@Override
	public void saveShop(String sha1, Shop p) throws IOException {
		shops.put(sha1, p);
		
		if (!shops.containsKey(sha1) || p.getStorage().hasChanged())
			changed.add(sha1);
		
	}
	
	/**
	 * @return
	 */
	private File getFileFor (String sha1) {
		return new File (getDirectory(), fileBeginning+sha1);
	}
	
	/**
	 * @return
	 */
	private File getDirectory () {
		File f	= new File (ShowCaseStandalone.get().getDataFolder(), directory);
		
		f.mkdirs();
		
		return f;
	}
	
	
	@Override
	public Shop loadShop(String sha1) throws IOException {
		return shops.get(sha1);
	}

	@Override
	public void saveShops(Shop[] p) throws IOException {
		for (Shop s : p)
			saveShop(s.getSHA1(), s);
	}

	@Override
	public Shop[] loadShops() throws IOException {
		File 		dir		= getDirectory();
		File 		files[]	= dir.listFiles();
		List<Shop>	lShops	= new ArrayList<Shop>();
		
		for (File f : files) {
			
			try {			
				if (f.getName().startsWith(fileBeginning)) {
					Shop	shop	= loadXMLShopFromFile(f);
					String	sha1	= shop.getSHA1();
					
					shops.put(sha1, shop);
					lShops.add(shop);
				}
			} catch (Exception e) {
				scs.log(Level.WARNING, "Couldn't load shop: "+f.getAbsolutePath());
				e.printStackTrace();
			}
		}
		
		for (Shop shop : lShops) {
			if (shop.getStorage().hasChanged())// Cause of import from old format
				changed.add(shop.getSHA1());
		}
		update();								// save
		
		scs.log(Level.INFO, "Loaded Shops: "+lShops.size());
		
		Shop	shops[]	= new Shop [lShops.size()];
		int		i		= 0;
		for (Shop p : lShops) {
			shops[i]	= p;
			i++;
		}
			

		return shops;
	}
	
	/**
	 * @param file
	 * @param shop
	 * @throws IOException
	 * @throws TransformerException
	 * @throws ParserConfigurationException
	 */
	private void saveShopToXMLFile (File file, Shop shop) throws IOException, TransformerException, ParserConfigurationException {		
		Document 	document = XMLStorageParser.createDocument();
		Node		shopNode = XMLStorageParser.storageToNode(document, shop.getStorage(), nodeNameRoot);
		
					document.appendChild(shopNode);
		String 		toString = XMLStorageParser.transform(document);
		
		FileOutputStream	fos	= new FileOutputStream	(file);
		PrintStream			ps	= new PrintStream		(fos);
		
		ps.print(toString);
		

		ps.close();
		fos.close();
		
	}
	
	/**
	 * @param file
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	private Shop loadXMLShopFromFile (File file) throws SAXException, IOException, ParserConfigurationException {
		Document		document	= XMLStorageParser.parseDocument(file);
		Node			shopNode	= document.getDocumentElement();//.getFirstChild();
		Storage			storage		= XMLStorageParser.nodeToStorage(shopNode);
		
		if (storage.getVersion() < Properties.storageVersion)
			convert(storage, storage.getVersion(), Properties.storageVersion);
		
		Shop			shop		= Shop.getShop(scs, storage);
		return shop;
	}

	@Override
	public void removeShop(String sha1) throws IOException {
		shops.remove(sha1);
		changed.add	(sha1);
	}

	@Override
	public void removeAllShops() throws IOException {
		for (String s : shops.keySet())
			changed.add(s);
		
		shops.clear();
	}
	
	

	@Override
	public void update() {
		List<String>	successfull	= new SparingList<String>();
		
		for (Shop p : shops.values())
			if (p.getStorage() != null)
				if (p.getStorage().hasChanged())
					changed.add(p.getSHA1());
		
		for (String sha1 : changed) {
			
			File	file	= getFileFor(sha1);
			Shop	shop	= shops.get	(sha1);
			
			if (shop == null && file.exists())	// shop was deleted
				file.delete();
			
			if (shop != null) {
				try {
					saveShopToXMLFile(file, shop);
					shop.getStorage().resetHasChanged();
					successfull.add(sha1);
				} catch (Exception e) {
					scs.log(Level.WARNING, "Couldn't save shop with sha1-key="+sha1);
					e.printStackTrace();
				}
			}
			
		}
		
		for (String s : successfull)
			changed.remove(s);
	}


	@Override
	public int importStroage(ShopStorageHandler storage) throws IOException {
		int 		imported = 0;
		
		for (Shop s : storage) {
			try {
				this.saveShop(s.getSHA1(), s);
				imported++;
			} catch (IOException ioe) { }	// ignoring it
		}
		
		return imported;
	}


	@Override
	public Iterator<Shop> iterator() {
		return shops.values().iterator();
	}


	@Override
	public Storage convert(Storage storage, int oldVersion, int version) throws IOException {
		if (storage == null)
			return null;
		
		switch (oldVersion) {
			case 0:	// no version
				for (Shop shop : shops.values())
					if (storage.equals(shop.getStorage())) {
						shop.updateToNBTTagStorage();
						break;
					}
				
				
						
				
			case 1:
				
		}
		
		storage.setVersion(version);
		return storage;
	}
	
	
}
