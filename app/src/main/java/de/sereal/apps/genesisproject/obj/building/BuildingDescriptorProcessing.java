package de.sereal.apps.genesisproject.obj.building;

import de.sereal.apps.genesisproject.rules.MaterialValue;
import android.util.Log;

/**
 * Created by sereal on 18.01.2018.
 */
public class BuildingDescriptorProcessing extends BuildingDescriptor
{

  public BuildingDescriptorProcessing() {
    needsTransportation = true;
  }

  @Override
  public void Production(float deltaMs)
  {
    float prodValue = 1.0f;

    for(MaterialValue mv : ManufacturingTable.InputMaterials)
    {
      // dont have anything or storage has room
      if(!ProductionInputStorage.containsKey(mv.Material) || storageCapacities.get(mv.Material) - ProductionInputStorage.get(mv.Material) >= mv.Value) {
        readyForDelivery.put(mv.Material, true);
      }

      if(!ProductionInputStorage.containsKey(mv.Material))
      {
        prodValue = 0;
        //Log.d("Missing material", mv.Value + " of " + mv.Material);
      }else{
        prodValue = Math.min(Math.min((deltaMs / 60000.0f) * mv.Value, ProductionInputStorage.get(mv.Material)) / mv.Value, prodValue);
        // Log.d("Found material", ProductionInputStorage.get(mv.Material) + " of " + mv.Material+ " prodValue is now " + prodValue);
      }
    }

    if(prodValue > 0.0f)
    {
      for(MaterialValue mv : ManufacturingTable.InputMaterials)
      {
        ProductionInputStorage.put(mv.Material, ProductionInputStorage.get(mv.Material) - (prodValue * mv.Value));
      }

      if(!ProducedGoods.containsKey(ManufacturingTable.Output.Material))
        ProducedGoods.put(ManufacturingTable.Output.Material,0.0f);

      ProducedGoods.put(ManufacturingTable.Output.Material, ProducedGoods.get(ManufacturingTable.Output.Material) + (prodValue * ManufacturingTable.Output.Value));
      if(ProducedGoods.get(ManufacturingTable.Output.Material) / storageCapacities.get(ManufacturingTable.Output.Material) >= 0.3f) {
          readyForTransportation.put(ManufacturingTable.Output.Material, true);
      }

      // Log.d("Producing", (prodValue * ManufacturingTable.Output.Value) + " of " + ManufacturingTable.Output.Material);
    }

  }
}
