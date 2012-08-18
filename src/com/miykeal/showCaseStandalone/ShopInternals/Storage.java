package com.miykeal.showCaseStandalone.ShopInternals;

import java.io.Serializable;
import java.util.HashMap;

public class Storage implements Serializable {
	private static final long 			serialVersionUID 	= 1L;
	private HashMap<String, Double>		doubles				= new HashMap<String, Double>();
	private HashMap<String, Integer>	integers			= new HashMap<String, Integer>();
	private HashMap<String, String>		strings				= new HashMap<String, String>();
	private HashMap<String, Boolean>	booleans			= new HashMap<String, Boolean>();
	private HashMap<String, Byte[]>		bytes				= new HashMap<String, Byte[]>();
	private HashMap<String, Storage>	storages			= new HashMap<String, Storage>();
	
	private int		hashCode			= 0;
	private int		storageVersion		= 0;
	private Storage	storage				= this;
	
	public Storage (int version) {
		this.storageVersion = version;
	}
	
	/**
	 * Sets the internal HashMaps to the Maps from the
	 * given storage --> connected
	 * @param storage
	 */
	public Storage (Storage storage) {
		this.storage	= storage;
	}
	
	/**
	 * @return The version of this storage
	 */
	public int getVersion () {
		return storage.storageVersion;
	}
	
	/**
	 * Sets the version of this storage
	 * @param version
	 */
	public void setVersion (int version) {
		storage.storageVersion = version;
	}
	
	
	/**
	 *  Resets the known accesses
	 */
	public void resetHasChanged () {
		storage.hashCode = hashCode();
	}
	
	/**
	 * @return true if there was any set-operation between now and the last reset
	 */
	public boolean hasChanged () {
		return (storage.hashCode != storage.hashCode());
	}
	/*
	 * 
	 * Doubles
	 * 
	 */
	public void setDouble (String key, Double value) {
		storage.doubles.put(key, value);
	}
	
	public Double getDouble (String key) {
		return storage.doubles.get(key);
	}
	
	public Double getDouble (String key, Double alternative) {
		Double value = getDouble(key);
		if (value == null)
			return alternative;
		return value;
	}
	
	public Iterable<String> getDoubleKeys () {
		return storage.doubles.keySet();
	}
	
	/*
	 * 
	 * Integers
	 * 
	 */
	public void setInteger (String key, Integer value) {
		storage.integers.put(key, value);
	}
	
	public Integer getInteger (String key) {
		return storage.integers.get(key);
	}
	
	public Integer getInteger (String key, Integer alternative) {
		Integer value = getInteger(key);
		if (value == null)
			return alternative;
		return value;
	}
	
	public Iterable<String> getIntegerKeys () {
		return storage.integers.keySet();
	}
	
	/*
	 * 
	 * Strings
	 * 
	 */
	public void setString (String key, String value) {
		storage.strings.put(key, value);
	}
	
	public String getString (String key) {
		return storage.strings.get(key);
	}
	
	public String getString (String key, String alternative) {
		String value = getString(key);
		if (value == null)
			return alternative;
		return value;
	}
	
	public Iterable<String> getStringKeys () {
		return storage.strings.keySet();
	}
	
	/*
	 * 
	 * Booleans
	 * 
	 */
	public void setBoolean (String key, Boolean value) {
		storage.booleans.put(key, value);
	}
	
	public Boolean getBoolean (String key) {
		return storage.booleans.get(key);
	}
	
	public Boolean getBoolean (String key, Boolean alternative) {
		Boolean value = getBoolean(key);
		if (value == null)
			return alternative;
		return value;
	}
	
	public Iterable<String> getBooleanKeys () {
		return storage.booleans.keySet();
	}
	
	/*
	 * 
	 * Bytes
	 * 
	 */
	public void setBytes (String key, Byte value[]) {
		storage.bytes.put(key, value);
	}
	
	public Byte[] getByte (String key) {
		return storage.bytes.get(key);
	}
	
	public Byte[] getByte (String key, Byte alternative[]) {
		Byte[] value = getByte(key);
		if (value == null)
			return alternative;
		return value;
	}
	
	public Iterable<String> getByteKeys () {
		return storage.bytes.keySet();
	}
	
	/*
	 * 
	 * Storages 
	 * 
	 */
	public void setStorage (String key, Storage value) {
		storage.storages.put(key, value);
	}
	
	public Storage getStorage (String key) {
		return storage.storages.get(key);
	}
	
	public Storage getStorage (String key, Storage alternative) {
		Storage	storage	= getStorage(key);
		
		if (storage == null)
			return alternative;
		else
			return storage;
	}
	
	public Iterable<String> getStorageKeys () {
		return storage.storages.keySet();
	}
	
	public int hashCode () {
		return    storage.storageVersion
				+ storage.doubles	.hashCode()
				+ storage.integers	.hashCode()
				+ storage.strings	.hashCode()
				+ storage.booleans	.hashCode()
				+ storage.bytes		.hashCode()
				+ storage.storages	.hashCode();
	}
}
