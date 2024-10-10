package de.sereal.apps.genesisproject.obj.vehicles;

import android.util.Log;

import java.util.HashMap;
import java.util.Vector;

import de.sereal.apps.genesisproject.util.PositionAnimation;
import de.sereal.apps.genesisproject.util.Vector3D;
import de.sereal.apps.genesisproject.util.VehicleJob;
import de.sereal.apps.genesisproject.util.VehicleTask;

/**
 * Created by sereal on 19.01.2018.
 */
public class VehicleDescriptor
{
  private String cargoType;
  private float cargoAmount;
  protected Vector<VehicleTask> TaskQueue = new Vector<>();
  public Vehicle vehicle = null;
  public Vector3D position;
  public Vector3D rotation = new Vector3D(0.0f, 90.0f, 0.0f);

  public VehicleDescriptor(final Vector3D position, final String cargoType) {
    this.position = position;
    this.cargoType = cargoType;
    this.cargoAmount = 0f;
  }
  
  public void setCargoAmount(final Float cargoAmount) { 
      this.cargoAmount = cargoAmount; 
  }
  
  public String getCargoType(){ return cargoType; }
  public Float getCargoAmount(){ return cargoAmount; }
  
  public void AddToTaskQueue(final VehicleTask queueItem)
  {
    TaskQueue.addElement(queueItem);
  }
  public Vector<VehicleTask> getTaskQueue() { return TaskQueue; }

  public void Process(float deltaMs) {
//    Log.d("Wee wee animation!!!",ms+"ms");

    // if there's a actual vehicle visualized
    boolean visualVehicle = (vehicle != null);

      VehicleTask task;
    for(; TaskQueue.size()!=0 && deltaMs != 0;)
    {
      task = TaskQueue.get(0);
      if(task instanceof VehicleJob)
      {
        switch( ((VehicleJob)task).JobType )
        {
          case LOAD_CARGO:
            ((VehicleJob)task).buildingDescriptor.transferToCargo();
            break;

          case UNLOAD_CARGO:
            ((VehicleJob)task).buildingDescriptor.transferToStash();
            break;

          case LOAD_DELIVERY:
            ((VehicleJob)task).buildingDescriptor.transferFromStash();
            break;

          case UNLOAD_DELIVERY:
            ((VehicleJob)task).buildingDescriptor.transferFromCargo();
            break;

          case REMOVE_TRANSPORT:
            ((VehicleJob)task).buildingDescriptor.RemoveTransportVehicle();
            break;

          case REMOVE_DELIVERY:
            ((VehicleJob)task).buildingDescriptor.RemoveDeliveryVehicle();
            break;
        }
        TaskQueue.remove(0);
      }else
      if(task instanceof PositionAnimation)
      {
        PositionAnimation posAnim = (PositionAnimation)task;
        if( posAnim.TimeSpanMs <= deltaMs)
        {
          deltaMs -= posAnim.TimeSpanMs;

          position.AddToThis(posAnim.PositionChange);
          rotation.x += posAnim.RotationX;
          rotation.y += posAnim.RotationY;
          rotation.z += posAnim.RotationZ;

//          if(visualVehicle)
//          {
//            vehicle.position.AddToThis(posAnim.PositionChange);
//            vehicle.RotationX += posAnim.RotationX;
//            vehicle.RotationY += posAnim.RotationY;
//            vehicle.RotationZ += posAnim.RotationZ;
//          }

          TaskQueue.remove(0);
        }
        else
        {
          // there's less time than this part of the animation lasts
//        Log.d("Trying to change",posAnim.TimeSpanMs+"ms left, "+posAnim.PositionChange.x+"/"+posAnim.PositionChange.y+"/"+posAnim.PositionChange.z + "    -> "+position.x+"/"+position.y+"/"+position.z);

          float ratio = deltaMs / (float)posAnim.TimeSpanMs;

          position.AddToThis(posAnim.PositionChange.Mult(ratio));
          rotation.x += posAnim.RotationX * ratio;
          rotation.y += posAnim.RotationY * ratio;
          rotation.z += posAnim.RotationZ * ratio;


          ratio = 1.0f - ratio;
          posAnim.TimeSpanMs -= deltaMs;
          posAnim.PositionChange.x *= ratio;
          posAnim.PositionChange.y *= ratio;
          posAnim.PositionChange.z *= ratio;
          posAnim.RotationX *= ratio;
          posAnim.RotationY *= ratio;
          posAnim.RotationZ *= ratio;

          break;
        }
      }


    }

    while(rotation.x >= 360.0f) rotation.x -=360.0f;
    while(rotation.y >= 360.0f) rotation.y -=360.0f;
    while(rotation.z >= 360.0f) rotation.z -=360.0f;
    while(rotation.x < 0.0f) rotation.x +=360.0f;
    while(rotation.y < 0.0f) rotation.y +=360.0f;
    while(rotation.z < 0.0f) rotation.z +=360.0f;

    if(visualVehicle)
    {
      vehicle.RotationX = rotation.x;
      vehicle.RotationY = rotation.y;
      vehicle.RotationZ = rotation.z;
      vehicle.position.x = position.x;
      vehicle.position.y = position.y;
      vehicle.position.z = position.z;
    }
  }

}
