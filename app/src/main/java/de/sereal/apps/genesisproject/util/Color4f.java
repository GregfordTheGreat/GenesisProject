package de.sereal.apps.genesisproject.util;

public class Color4f 
{
	public float r;
	public float g;
	public float b;
	public float a;
	
	public Color4f(float r, float g, float b) 
	{
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = 1.0f;
	}

	public Color4f(float r, float g, float b, float a) 
	{
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
}
