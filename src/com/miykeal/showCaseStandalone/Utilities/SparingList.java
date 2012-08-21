package com.miykeal.showCaseStandalone.Utilities;

import java.util.ArrayList;
import java.util.Collection;


public class SparingList<T> extends ArrayList<T>{
	private static final long serialVersionUID = 1L;

	public SparingList () {
	}

	@Override
	public boolean add(T e) {
		if (this.contains(e))
			return true;
		else
			return super.add(e);
	}
	
	@Override
	public boolean addAll (Collection<? extends T> collection) {
		for (T e : collection)
			this.add(e);
		return true;
	}
}
