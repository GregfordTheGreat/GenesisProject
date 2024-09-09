package de.sereal.apps.genesisproject.util;

/**
 * Created by sereal on 23.08.2016.
 */
public class VehicleTask
{
  public enum VehicleTaskType{
    VEHICLE_JOB(0),
    VEHICLE_POS_ANIM(1);

    private int value;
    VehicleTaskType(int value){ this.value = value; }
    public int getValue(){ return value; }

    public static VehicleTaskType parse(int value) {
      for (VehicleTaskType d : values()) {
        if(d.value == value)
          return d;
      }
      return null;
    }

  }

}
