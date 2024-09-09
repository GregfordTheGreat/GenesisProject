package de.sereal.apps.genesisproject.util;

/**
 * Created by sereal on 13.08.2016.
 */
public class PositionAnimation extends VehicleTask
{
  public int TimeSpanMs;
  public Vector3D PositionChange;
  public float RotationX;
  public float RotationY;
  public float RotationZ;

  public PositionAnimation(int ms, float dx, float dy, float dz, float xRot, float yRot, float zRot)
  {
    PositionChange = new Vector3D(dx, dy, dz);
    RotationX = xRot;
    RotationY = yRot;
    RotationZ = zRot;
    TimeSpanMs = ms;
  }
}
