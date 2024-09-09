package de.sereal.apps.genesisproject.obj.particle;

import de.sereal.apps.genesisproject.util.Vector3D;

/**
 * Created by sereal on 18.10.2016.
 */
public abstract class ParticleEffect
{
  public Vector3D Origin;
  public abstract void Draw();
  public abstract void Animate(float ms);

  public void SetOrigin(float x, float y, float z)
  {
    Origin = new Vector3D(x, y, z);
  }

  public void MoveOrigin(float x, float y, float z)
  {
    Origin.x += x;
    Origin.y += y;
    Origin.z += z;
  }
}
