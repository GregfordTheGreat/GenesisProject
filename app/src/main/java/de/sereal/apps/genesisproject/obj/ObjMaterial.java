package de.sereal.apps.genesisproject.obj;

import de.sereal.apps.genesisproject.util.Vector3D;

/**
 * Created by sereal on 11.07.2017.
 */
public class ObjMaterial
{
  public Vector3D AmbientColor;
  public Vector3D DiffuseColor;
  public Vector3D SpecularColor;
  public float shininess;
  public float opacity;
  public int TextureResourceId;
  public int TextureHandle;

  public ObjMaterial()
  {
    shininess = 0.0f;
    opacity = 1.0f;
    TextureResourceId = 0; // default of getIdentifier()
    TextureHandle = 0;  // default none
  }
}
