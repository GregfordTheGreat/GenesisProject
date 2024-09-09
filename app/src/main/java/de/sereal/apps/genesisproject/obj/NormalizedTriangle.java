package de.sereal.apps.genesisproject.obj;


import de.sereal.apps.genesisproject.util.Point3D;

public class NormalizedTriangle
{
	public Point3D P0;
	public Point3D P1;
	public Point3D P2;

	public NormalizedTriangle(Point3D p0, Point3D p1, Point3D p2)
	{
		P0 = p0;
		P1 = p1;
		P2 = p2;
	}
	
	public NormalizedTriangle(Point3D p0, Point3D p1, Point3D p2, boolean normalized)
	{
		P0 = p0;
		P1 = p1;
		P2 = p2;
		
		if(normalized)
		{
			P0.Normalize();
			P1.Normalize();
			P2.Normalize();
		}
	}
	
	public Point3D GetNormal()
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
