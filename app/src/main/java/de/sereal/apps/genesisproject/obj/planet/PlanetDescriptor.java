package de.sereal.apps.genesisproject.obj.planet;

import java.util.concurrent.CopyOnWriteArrayList;

import de.sereal.apps.genesisproject.obj.building.BuildingDescriptor;
import de.sereal.apps.genesisproject.util.MyConstants;
import de.sereal.apps.genesisproject.util.RoadMap;
import de.sereal.apps.genesisproject.util.Vector3D;

/**
 * Created by sereal on 10.01.2018.
 */
public class PlanetDescriptor
{
  private String name;
  private int[][] heightmap;
  private int widthSegments;
  private int heightSegments;
  private RoadMap roadMap;
  private byte[][] Resource_RareEarth;
  private byte[][] Resource_CoreIce;
  private CopyOnWriteArrayList<BuildingDescriptor> Buildings = new CopyOnWriteArrayList<>();
  private boolean isActive = false;

  public PlanetDescriptor(final String name, final int widthSegments, final int heightSegments) {
    this.name = name;
    this.widthSegments = widthSegments;
    this.heightSegments = heightSegments;
  }

  public Vector3D getPosition(int w, int h)
  {
    float halfHeight = MyConstants.TILE_SIZE * (float) (heightSegments) / 2.0f;
    float halfWidth = MyConstants.TILE_SIZE * (float) (widthSegments) / 2.0f;

    Vector3D result = new Vector3D();
    result.x = (((float) w) + 0.5f) * MyConstants.TILE_SIZE - halfWidth ;
    result.y = heightmap[h][w] * MyConstants.HEIGHT_STEP;
    result.z = (((float) h) + 0.5f) * MyConstants.TILE_SIZE - halfHeight;

    return result;
  }

  public String getName() {
    return name;
  }

  public void setHeightmap(int[][] heightmap) {
    this.heightmap = heightmap;
  }

  public int[][] getHeightmap(){
    return heightmap;
  }

  public int getWidthSegments() {
    return widthSegments;
  }

  public int getHeightSegments() {
    return heightSegments;
  }

  public RoadMap getRoadMap() { return roadMap; }

  public void setRoadMap(final RoadMap roadMap) { this.roadMap = roadMap; }

  public void setActive(boolean isActive){ this.isActive = isActive; }
  public boolean isActive() { return isActive; }

  public byte[][] getResource_CoreIce() { return Resource_CoreIce; }
  public void setResource_CoreIce(byte[][] resource_CoreIce) { Resource_CoreIce = resource_CoreIce; }

  public byte[][] getResource_RareEarth() { return Resource_RareEarth; }
  public void setResource_RareEarth(byte[][] resource_RareEarth){ Resource_RareEarth = resource_RareEarth; }

  public void addBuilding(final BuildingDescriptor bd) { Buildings.add(bd); }
  public CopyOnWriteArrayList<BuildingDescriptor> getBuildings() { return Buildings; }

  public float GetMaterialAtLocation(String materialKey, int w, int h)
  {
    switch(materialKey)
    {
      case "MATERIAL_RARE_EARTH": return ((float)(Resource_RareEarth[h][w] & 0xFF) / 255.0f);
      case "MATERIAL_CORE_ICE": return ((float)(Resource_CoreIce[h][w] & 0xFF) / 255.0f);
    }
    return 0.0f;
  }

}
