package de.sereal.apps.genesisproject.util;

import java.io.Serializable;

public class FloatArrayList implements Serializable
{
	private static final long serialVersionUID = -2787658130131426265L;
	public static int initialCapacity = 10;

	private float[] array;
	private int size = 0;
	
	
	public FloatArrayList() 
	{
		this(initialCapacity);
	}
	
	public FloatArrayList(int capacity) 
	{
		if (capacity < 0) 
		{
			throw new IllegalArgumentException("Capacity can't be negative: " + capacity);
		}
		
		array = new float[initialCapacity];
	    size = 0;
	}
	
	
	public float[] ToArray() 
	{
		float[] result = new float[size];
		System.arraycopy(array, 0, result, 0, size);
		return result;
	}
	
	public int GetSize()
	{
		return size;
	}
	
	public float Get(int index)
	{
		checkRange(index);
		return array[index];
	}

	public void Set(int index, float value)
	{
		checkRange(index);
		array[index] = value;
	}
	
	public void Add(float value)
	{
		ensureCapacity(size + 1);
		array[size++] = value;
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
			float[] newdata = new float[newCap];
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
