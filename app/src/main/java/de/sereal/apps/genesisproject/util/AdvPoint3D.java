package de.sereal.apps.genesisproject.util;

/**
 * Created by sereal on 26.02.2017.
 */
public class AdvPoint3D
{
  public float x,y,z;
  public float u,v;
  public float r, g, b, a;

  public AdvPoint3D()
  {
    this.x = 0.0f;
    this.y = 0.0f;
    this.z = 0.0f;
    this.u = 0.0f;
    this.v = 0.0f;
    this.r = 0.0f;
    this.g = 0.0f;
    this.b = 0.0f;
    this.a = 0.0f;
  }

  public AdvPoint3D(float x, float y, float z, float u, float v, float r, float g, float b, float a)
  {
    this.x = x;
    this.y = y;
    this.z = z;
    this.u = u;
    this.v = v;
    this.r = r;
    this.g = g;
    this.b = b;
    this.a = a;
  }

  public AdvPoint3D(float x, float y, float z, float u, float v, float[] rgba)
  {
    this.x = x;
    this.y = y;
    this.z = z;
    this.u = u;
    this.v = v;
    this.r = rgba[0];
    this.g = rgba[1];
    this.b = rgba[2];
    this.a = rgba[3];
  }
}
