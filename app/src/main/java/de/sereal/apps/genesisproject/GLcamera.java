package de.sereal.apps.genesisproject;

import de.sereal.apps.genesisproject.util.Vector3D;

public class GLcamera
{
	private float radius			= 19.0f;

	public Vector3D position	= new Vector3D(0, 0, radius);
	public Vector3D richtung	= new Vector3D(0, 0, 0);
	public Vector3D oben		= new Vector3D(0, 1, 0);
	public Vector3D rechts		= new Vector3D(0, 1, 0);
	
	float zweiPi = 6.283185308f;
	float     Pi = 3.141592654f;
	public void rotate(float angel_y, float angel_z)
	{
		position.z = radius * ((float)Math.cos((angel_y / 360f) * zweiPi) * (float)Math.cos((angel_z / 360f) * zweiPi)); 
		position.x = radius * ((float)Math.sin((angel_y / 360f) * zweiPi) * (float)Math.cos((angel_z / 360f) * zweiPi)); 
		position.y = radius * ((float)Math.sin((angel_z / 180f) * Pi));

		// take the vector from the top as reference point
		rechts.z = 0; 
		rechts.x = 0; 
		rechts.y = radius;
		rechts = rechts.Substract(position);
		rechts = Vector3D.Cross(position, rechts);
		rechts.Normalize();

		position.x += richtung.x;
		position.y += richtung.y;
		position.z += richtung.z;
	}
	
	public void MoveXZ(float screenx, float screeny)
	{
		Vector3D delta = richtung.Substract(position); // echte richtung
		Vector3D rechtsCpy = rechts.Mult(1.0f);

		delta.y = 0;
		rechtsCpy.y = 0;
		
		screenx /= 100.0f;
		screeny /= 100.0f;
		
		delta.Normalize();
		rechtsCpy.Normalize();

		position.x += ((delta.x * screenx ) + (rechtsCpy.x * screeny)); 
		position.z += ((delta.z * screenx ) + (rechtsCpy.z * screeny)); 
		richtung.x += ((delta.x * screenx ) + (rechtsCpy.x * screeny)); 
		richtung.z += ((delta.z * screenx ) + (rechtsCpy.z * screeny)); 
	}

	public void MoveTo(Vector3D pos)
	{
		Vector3D delta = pos.Substract(richtung);
		richtung.x += delta.x;
		richtung.z += delta.z;
		position.x += delta.x;
		position.z += delta.z;
	}
	
	/**
	 * Set the position of the eye
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setPosition(float x, float y, float z)
	{ 
		position.x = x;
		position.y = y;
		position.z = z;
	}
	
	public void setZoom(float newZoom)
	{ 
		Vector3D delta = position.Substract(richtung); // echte position

		delta.x = delta.x / radius * newZoom;
		delta.y = delta.y / radius * newZoom;
		delta.z = delta.z / radius * newZoom;
//		position.x = position.x / radius * newZoom;
//		position.y = position.y / radius * newZoom;
//		position.z = position.z / radius * newZoom;

		position = delta.Add(richtung);
		
		radius=newZoom;
	}
	
	public float getZoom()
	{ 
		return radius; 
	}
}
