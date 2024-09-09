package de.sereal.apps.genesisproject.obj.building;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import de.sereal.apps.genesisproject.GameActivity;
import de.sereal.apps.genesisproject.obj.SimpleVBO;
import de.sereal.apps.genesisproject.obj.VertexBufferObject;
import de.sereal.apps.genesisproject.obj.WavefrontVBO;
import de.sereal.apps.genesisproject.obj.particle.ParticleEffect;
import de.sereal.apps.genesisproject.obj.vehicles.Vehicle;
import de.sereal.apps.genesisproject.obj.vehicles.VehicleDescriptor;
import de.sereal.apps.genesisproject.rules.MaterialValue;
import de.sereal.apps.genesisproject.util.ArrayHelpers;
import de.sereal.apps.genesisproject.util.Helpers;
import de.sereal.apps.genesisproject.util.MaterialConversion;
import de.sereal.apps.genesisproject.util.MyConstants.Direction;
import de.sereal.apps.genesisproject.util.PathNode;
import de.sereal.apps.genesisproject.util.Position2D;
import de.sereal.apps.genesisproject.util.PositionAnimation;
import de.sereal.apps.genesisproject.util.Vector3D;
import de.sereal.apps.genesisproject.util.VehicleJob;


public abstract class Building
{
  protected VertexBufferObject buildingVBO = null;
  protected VertexBufferObject buildingVBOLight = null;
  protected Context context;
	protected float unitSize;
  public Vector<Vector3D> BoundingBox = new Vector<>();

	public abstract void Build();
  public abstract void DrawEffects();
  public String buildingDefinitionKey;
  public Direction BuildingDirection;
  public Vector<ParticleEffect> particleEffects = new Vector<>();
  private BuildingDescriptor buildingDescriptor;
	
  public Building(Context context, float unitSize, final String buildingDefKey)
  {
		this.context = context;
		this.unitSize = unitSize;
		this.buildingDefinitionKey = buildingDefKey;
		Build();
        BoundingBox = buildingVBO.getBoundingBox();
  }

  public void SetProduction()
  {

  }

  public void setBuildingDescriptor(final BuildingDescriptor buildingDescriptor) { this.buildingDescriptor = buildingDescriptor; }
  public BuildingDescriptor getBuildingDescriptor() { return buildingDescriptor; }

	public void SetPosition(float x, float y, float z)
	{
    // move the building
    buildingVBO.SetPosition(x,y,z);

    // recalculate BoundingBox from scratch
    BoundingBox = buildingVBO.getBoundingBox();

    if(buildingVBOLight != null && buildingVBOLight.valid)
    {
      buildingVBOLight.SetPosition(x,y,z);
    }

    for (ParticleEffect pEff : particleEffects)
    {
      pEff.MoveOrigin(x, y, z);
    }
  }

	public void SetDirection(Direction direction, int[][] buildingGrid)
	{
		float x;
    this.BuildingDirection = direction;

		if(direction == Direction.SOUTH)
			return;

    int[][] grid = ArrayHelpers.deepCopyIntMatrix(buildingGrid);
    int h = (grid != null) ? grid.length : 0;
    int w = (h>0) ? grid[0].length : 0;
    int tmp;

    if(h == 0 || w == 0)
      return;

    float centerX = (float)w / 2.0f * unitSize;
    float centerZ = (float)h / 2.0f * unitSize;
    SetPosition(-centerX, 0.0f, -centerZ);

    switch(direction)
    {
      case EAST:
      case WEST:
        tmp = h;
        h = w;
        w = tmp;
        break;

      default:
        break;
    }


    buildingVBO.SetDirection(direction);

    switch(direction)
    {
      case EAST:
        for (ParticleEffect pEff : particleEffects)
        {
          x = pEff.Origin.x;
          pEff.Origin.x =  pEff.Origin.z;
          pEff.Origin.z =  -x;
        }
        break;

      case NORTH:
        for (ParticleEffect pEff : particleEffects)
        {
          pEff.Origin.x =  -pEff.Origin.x;
          pEff.Origin.z =  -pEff.Origin.z;
        }
        break;

      case WEST:
        for (ParticleEffect pEff : particleEffects)
        {
          x = pEff.Origin.x;
          pEff.Origin.x =  -pEff.Origin.z;
          pEff.Origin.z =  x;
        }
        break;
    }

    if(direction == Direction.EAST || direction == Direction.WEST)
    {
      centerX += (float)(w - h) / 2.0f * unitSize;
      centerZ -= (float)(w - h) / 2.0f * unitSize;
    }

    SetPosition(centerX, 0.0f, centerZ);
	}

	public void Animate(int ms)
	{
    for(ParticleEffect pEff : particleEffects)
    {
      pEff.Animate(ms);
    }
  }



  public void BuildFromFile(int idMesh, int idLightMesh)
  {
    buildingVBO = SimpleVBO.BuildFromFile(context, idMesh, unitSize);

    if(idLightMesh != 0)
    buildingVBOLight = SimpleVBO.BuildFromFile(context, idLightMesh, unitSize);
  }

  public void BuildFromWavefrontObj(int idObj)
  {
    buildingVBO = WavefrontVBO.BuildFromWavefrontObj(context, idObj);
  }

  public void DrawPositionOnly(int positionHandle)
  {
    buildingVBO.DrawPositionOnly(positionHandle);
  }


  public void Draw(int positionHandle, int colorHandle, int normalHandle, int texIDHandle, int textureHandle, int texCoordHandle)
	{
    buildingVBO.Draw(positionHandle, colorHandle, normalHandle, texIDHandle, textureHandle, texCoordHandle);
	}

}
