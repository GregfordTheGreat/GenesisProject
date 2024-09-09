package de.sereal.apps.genesisproject.util;

import java.io.Serializable;

public class ShortArrayList implements Serializable
{
	private static final long serialVersionUID = -2787658130131426265L;
	public static int initialCapacity = 10;

	private short[] array;
	private int size = 0;
	
	
	public ShortArrayList() 
	{
		this(initialCapacity);
	}
	
	public ShortArrayList(int capacity) 
	{
		if (capacity < 0) 
		{
			throw new IllegalArgumentException("Capacity can't be negative: " + capacity);
		}
		
		array = new short[initialCapacity];
	    size = 0;
	}
	
	
	public short[] ToArray() 
	{
		short[] result = new short[size];
		System.arraycopy(array, 0, result, 0, size);
		return result;
	}
	
	public int GetSize()
	{
		return size;
	}
	
	public short Get(int index)
	{
		checkRange(index);
		return array[index];
	}

	public void Set(int index, short value)
	{
		checkRange(index);
		array[index] = value;
	}
	
	public void Add(int value)
	{
		ensureCapacity(size + 1);
		array[size++] = (short)value;
	}
	
	public void Clear() 
	{
	    size = 0;
	}
		
	private void ensureCapacity(int mincap)
	{
		if(mincap > array.length)
		{
			int newCap = ((array.length * 3) >> 1) + 1;
			short[] newdata = new short[newCap];
			System.arraycopy(array, 0, newdata, 0, size);
			array = newdata;
		}
	}
	
	/**
	 * 
	 * @param index
	 */
	private void checkRange(int index)
	{
		if (index < 0 || index >= size) 
		{
			throw new IndexOutOfBoundsException("Index should be at least 0 and less than " + size + ", found " + index);
	    }
	}
	
}
