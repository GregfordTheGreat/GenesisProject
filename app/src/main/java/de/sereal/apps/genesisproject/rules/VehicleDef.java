package de.sereal.apps.genesisproject.rules;

/**
 * Created by sereal on 13.08.2016.
 */
public class VehicleDef
{

  private String Key;
  private String Mesh;
  private String Name;

  public VehicleDef(String key, String mesh, String name)
  {
    Key = key;
    Mesh = mesh;
    Name = name;
  }

  public String getKey()
  {
    return Key;
  }
}
