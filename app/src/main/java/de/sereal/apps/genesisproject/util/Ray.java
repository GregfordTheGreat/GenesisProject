package de.sereal.apps.genesisproject.util;

public class Ray 
{
	public Vector3D P0;
	public Vector3D P1;
	
	public Vector3D GetVector()
	{
		return P1.Substract(P0);
	}
}
