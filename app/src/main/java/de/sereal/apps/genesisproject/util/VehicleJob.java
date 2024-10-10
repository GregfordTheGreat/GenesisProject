package de.sereal.apps.genesisproject.util;

import de.sereal.apps.genesisproject.obj.building.BuildingDescriptor;

/**
 * Created by sereal on 23.08.2016.
 */
public class VehicleJob extends VehicleTask
{
  public enum VehicleJobTypes
  {
    UNKNOWN(0),
    LOAD_CARGO(1),
    UNLOAD_CARGO(2),
    LOAD_DELIVERY(3),
    UNLOAD_DELIVERY(4),
    REMOVE_TRANSPORT(5),
    REMOVE_DELIVERY(6);

    private int value;
    VehicleJobTypes(int value){ this.value = value; }
    public int getValue(){ return value; }

    public static VehicleJobTypes parse(int value) {
      for (VehicleJobTypes d : values()) {
        if(d.value == value)
          return d;
      }
      return null;
    }
  }

  public VehicleJobTypes JobType = VehicleJobTypes.UNKNOWN;
  public BuildingDescriptor buildingDescriptor;

  public VehicleJob(VehicleJobTypes jobType, BuildingDescriptor buildingDescriptor)
  {
    this.JobType = jobType;
    this.buildingDescriptor = buildingDescriptor;
  }
}
