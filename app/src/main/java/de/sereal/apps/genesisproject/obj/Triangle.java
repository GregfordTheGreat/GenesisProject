package de.sereal.apps.genesisproject.obj;

import de.sereal.apps.genesisproject.util.Vector3D;

public class Triangle
{
	public Vector3D P0 = new Vector3D();
	public Vector3D P1 = new Vector3D();
	public Vector3D P2 = new Vector3D();
	
	public Vector3D GetVectorA()
	{
		return P1.Substract(P0);
	}

	public Vector3D GetVectorB()
	{
		return P2.Substract(P0);
	}
}
