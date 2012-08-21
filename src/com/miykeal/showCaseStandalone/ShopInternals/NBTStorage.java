package com.miykeal.showCaseStandalone.ShopInternals;

import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagString;

public class NBTStorage extends Storage{
	private static final long serialVersionUID = 1L;
	
	private static final String tagStringAuthor		= "author";
	private static final String tagStringTitle		= "title";
	private static final String tagStringPages		= "pages";
	private static final String tagStringPagePref	= "page-";
	
	private static final String tagIntegerPagesSize = "pages";

	private NBTTagCompound	tag				= null;
	private Storage			storage			= this;
	
	public NBTStorage (NBTTagCompound tag, int version) {
		super(version);
		
		this.tag	= tag;
		
		saveNBT(storage, tag);
	}
	
	public NBTStorage (Storage storage) {
		super(storage);
		
		this.tag	= loadNBT(storage);
	}
	
	/**
	 * @return	The saved tag
	 */
	public NBTTagCompound getNBTTagCompound () {
		return this.tag;
	}
	

	/**
	 * Saves every known NBT-tag to the storage
	 * @param nbt
	 */
	private void saveNBT (Storage storage, NBTTagCompound nbt) {
		if (nbt == null)
			return;
		
		String		author	= nbt.getString	(tagStringAuthor);
		String		title	= nbt.getString	(tagStringTitle);
		NBTTagList	pages	= nbt.getList	(tagStringPages);
		
		if (pages != null) {
			for (int i = 0; i < pages.size(); i++)
				setString(tagStringPagePref+i, ((NBTTagString)pages.get(i)).data);
			setInteger(tagIntegerPagesSize, pages.size());
		}
		
		if (title != null)
			setString(tagStringTitle, title);
		
		if (author != null)
			setString(tagStringAuthor, author);
	}
	
	/**
	 * Loads the NBT with every known NBT-tag
	 * @return
	 */
	private NBTTagCompound loadNBT (Storage storage) {
		String 	author	= storage.getString	(tagStringAuthor);
		String 	title	= storage.getString	(tagStringTitle);
		Integer pSize	= storage.getInteger(tagIntegerPagesSize);
		
		NBTTagCompound compound	= new NBTTagCompound();
		
		if (pSize != null) {
			NBTTagList	list	= new NBTTagList();
			for (int i = 0; i < pSize; i++) {
				String 			page	= storage.getString(tagStringPagePref+i);
				
				if (page != null) {
					NBTTagString tag	= new NBTTagString(page);
					tag.data			= page;
					list.add(tag);
				}
			}
			
			compound.set(tagStringPages, list);
		}
		
		if (title != null)
			compound.setString(tagStringTitle, title);
		
		if (author != null)
			compound.setString(tagStringAuthor, author);
		
		
		return compound;
	}
	
	/**
	 * @return	The Book-Title or null
	 */
	public String getBookTitle () {
		return getString(tagStringTitle);
	}
}
