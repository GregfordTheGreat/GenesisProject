package de.sereal.apps.genesisproject.util;

public class Vector3D {

	public float x,y,z;
	
	public Vector3D() 
	{
		x = 0;
		y = 0;
		z = 0;
	}
	
	public Vector3D(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vector3D (float x1, float y1, float z1, float x2, float y2, float z2)
	{
		this.x = x2 - x1;
		this.y = y2 - y1;
		this.z = z2 - z1;
	}
	
	public boolean IsZero()
	{
		return (x==0 && y==0 && z == 0);
	}
	
	public float Dot(Vector3D vec)
	{
		return (x*vec.x + y*vec.y + z*vec.z);
	}
	
	public static Vector3D Cross(Vector3D v1, Vector3D v2)
	{
		return new Vector3D( 
				(v1.y*v2.z) - (v1.z*v2.y),
				(v1.z*v2.x) - (v1.x*v2.z),
				(v1.x*v2.y) - (v1.y*v2.x));
	}
	
	public Vector3D Substract(Vector3D vec)
	{
		return new Vector3D(x - vec.x, y - vec.y, z - vec.z);
	}
	public Vector3D Add(Vector3D vec)
	{
		return new Vector3D(x + vec.x, y + vec.y, z + vec.z);
	}
	public void AddToThis(Vector3D vec)
	{
		x += vec.x;
		y += vec.y;
		z += vec.z;
	}
	public Vector3D Mult(float m)
	{
		return new Vector3D(x * m, y * m, z * m);
	}
	
	public float Length()
	{
		return (float)Math.sqrt(x * x + y * y + z * z);
	}

	public void Normalize()
	{
		float len = Length();
		x /= len;
		y /= len;
		z /= len;
	}

	public static Vector3D GetNormal(Vector3D P0, Vector3D P1, Vector3D P2)
	{
		Vector3D V1 = P1.Substract(P0);
		Vector3D V2 = P2.Substract(P0);
		
		V1 = Vector3D.Cross(V1, V2);
		V1.Normalize();
		return V1;
	}
}
