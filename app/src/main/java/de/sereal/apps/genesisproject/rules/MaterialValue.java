package de.sereal.apps.genesisproject.rules;

/**
 * Created by sereal on 22.08.2016.
 */
public class MaterialValue
{
  public String Material; // like [MATERIAL_RARE_EARTH]
  public float Value;

  public MaterialValue(String material, float value)
  {
    Material = material;
    Value = value;
  }
}
