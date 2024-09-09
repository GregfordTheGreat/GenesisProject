package de.sereal.apps.genesisproject.obj.building;

/**
 * Created by sereal on 18.01.2018.
 */
public class BuildingDescriptorProduction extends BuildingDescriptor
{

  public BuildingDescriptorProduction() {
    NeedsTransportation = true;
  }

  @Override
  public void Production(float deltaMs)
  {
    float val;
    for(String materialKey : ProductionValues.keySet())
    {
      if(!ProducedGoods.containsKey(materialKey))
        ProducedGoods.put(materialKey, 0.0f);

      val = Math.min(storageCapacities.get(materialKey), ProducedGoods.get(materialKey) + (deltaMs / 1000.0f * ProductionValues.get(materialKey)));
      if(val / storageCapacities.get(materialKey) >= 0.3f)
      {
        ReadyForTransportation = true;
      }

      ProducedGoods.put(materialKey, val);
    }
  }

}
