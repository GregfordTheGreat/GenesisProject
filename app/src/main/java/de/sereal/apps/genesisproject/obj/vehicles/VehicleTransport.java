package de.sereal.apps.genesisproject.obj.vehicles;

import android.content.Context;
import android.util.Log;

import de.sereal.apps.genesisproject.R;
import de.sereal.apps.genesisproject.util.PositionAnimation;

/**
 * Created by sereal on 13.08.2016.
 */
public class VehicleTransport extends Vehicle
{

  public static String KEY = "VEHICLE_TRANSPORT";
  public VehicleTransport(Context context, float unitSize)
  {
    super(context, unitSize);
  }

  @Override
  public void Build()
  {
    Log.d("Build","Transport vehicle");
    BuildFromFile(context.getResources().openRawResource(R.raw.vehicle_transport));
  }
}
