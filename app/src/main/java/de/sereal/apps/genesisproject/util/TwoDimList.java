package de.sereal.apps.genesisproject.util;

import android.util.SparseArray;

public class TwoDimList<E> 
{
	private SparseArray<SparseArray<E>> outer = new SparseArray<SparseArray<E>>();
	
	public boolean HasKey(int i, int j)
	{
		try{
			return (outer.get(i).get(j) != null);
		}
		catch(NullPointerException nex)
		{
			return false;
		}
	}
	
	public void Put(int i, int j, E obj)
	{
		SparseArray<E> inner = outer.get(i);
		if(inner == null)
		{
			inner = new SparseArray<E>(); 
			outer.append(i, inner);
		}
		inner.append(j, obj);
	}
	
	public E Get(int i, int j)
	{
		try{
			return outer.get(i).get(j);
		}
		catch(NullPointerException nex)
		{
			return null;
		}
	}
}
