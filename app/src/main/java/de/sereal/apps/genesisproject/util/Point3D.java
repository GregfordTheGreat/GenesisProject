package de.sereal.apps.genesisproject.util;

public class Point3D 
{
	public float X;
	public float Y;
	public float Z;
	
	public Point3D(float x, float y, float z)
	{
		X = x;
		Y = y;
		Z = z;
	}
	
	public Point3D Normalize()
	{
		float len = (float)Math.sqrt(X * X + Y * Y + Z * Z);
		X /= len;
		Y /= len;
		Z /= len;
		return this;
	}
	
	public static Point3D GetNormal(Point3D P0, Point3D P1, Point3D P2)
	{
		Point3D vect1 = new Point3D(P1.X - P0.X, P1.Y - P0.Y, P1.Z - P0.Z);
		Point3D vect2 = new Point3D(P2.X - P0.X, P2.Y - P0.Y, P2.Z - P0.Z);
		Point3D norm = new Point3D(
				vect1.Y * vect2.Z - vect1.Z * vect2.Y,
				vect1.Z * vect2.X - vect1.X * vect2.Z,
				vect1.X * vect2.Y - vect1.Y * vect2.X);

		float len = 1.0f / (float)Math.sqrt(norm.X * norm.X + norm.Y * norm.Y + norm.Z * norm.Z);

		norm.X *= len;
		norm.Y *= len;
		norm.Z *= len;

		return norm;
	}
}
