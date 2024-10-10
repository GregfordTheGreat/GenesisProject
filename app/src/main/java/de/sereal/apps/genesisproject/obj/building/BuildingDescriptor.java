package de.sereal.apps.genesisproject.obj.building;

import android.util.Log;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import de.sereal.apps.genesisproject.GameActivity;
import de.sereal.apps.genesisproject.obj.planet.PlanetDescriptor;
import de.sereal.apps.genesisproject.obj.vehicles.VehicleDescriptor;
import de.sereal.apps.genesisproject.rules.BuildingDef;
import de.sereal.apps.genesisproject.rules.MaterialValue;
import de.sereal.apps.genesisproject.util.Helpers;
import de.sereal.apps.genesisproject.util.MaterialConversion;
import de.sereal.apps.genesisproject.util.MyConstants;
import de.sereal.apps.genesisproject.util.PathNode;
import de.sereal.apps.genesisproject.util.Position2D;
import de.sereal.apps.genesisproject.util.PositionAnimation;
import de.sereal.apps.genesisproject.util.Vector3D;
import de.sereal.apps.genesisproject.util.VehicleJob;
import java.util.Map;
import java.util.Optional;

/**
 * Created by sereal on 14.01.2018.
 */
public class BuildingDescriptor
{
  private String buildingType;
  private MyConstants.Direction buildingDirection;
  private Vector3D buildingPosition;
  private Position2D pickingPoint;
  private int[][] buildingGrid;
  private PlanetDescriptor planetDescriptor;
  private VehicleDescriptor transportVehicle = null;
  private VehicleDescriptor deliveryVehicle = null;
  public List<PathNode> PathToTransportCenter = null;
  public Vector<Position2D> ConnectionPoints = new Vector<>();

  protected HashMap<String, Float> ProductionValues = new HashMap<>();
  protected HashMap<String, Float> ProducedGoods = new HashMap<>();
  protected MaterialConversion ManufacturingTable = new MaterialConversion();
  protected HashMap<String, Float> ProductionInputStorage = new HashMap<>();
  protected HashMap<String, Float> resourceCosts = new HashMap<>();
  protected HashMap<String, Float> resourceProductions = new HashMap<>();
  protected HashMap<String, Float> storageCapacities = new HashMap<>();
  public boolean needsTransportation = false;
  
  public boolean transportInProgress = false;
  public boolean deliveryInProgress = false;
  public Map<String, Boolean> readyForDelivery = new HashMap<>();
  public Map<String, Boolean> readyForTransportation = new HashMap<>();
  
  public void setPlanetDescriptor(PlanetDescriptor planetDescriptor) { this.planetDescriptor = planetDescriptor; }

  public String getBuildingType() { return buildingType; }
  public void setBuildingType(final String buildingType) { this.buildingType = buildingType; }

  public MyConstants.Direction getBuildingDirection() { return buildingDirection; }
  public void setBuildingDirection(MyConstants.Direction buildingDirection) { this.buildingDirection = buildingDirection; }

  public Vector3D getBuildingPosition() { return buildingPosition; }
  public void setBuildingPosition(Vector3D buildingPosition) { this.buildingPosition = buildingPosition; }

  public Position2D getPickingPoint() { return pickingPoint; }
  public void setPickingPoint(Position2D pickingPoint) { this.pickingPoint = pickingPoint; }

  public int[][] getBuildingGrid() { return buildingGrid; }
  public void setBuildingGrid(int[][] buildingGrid) { this.buildingGrid = buildingGrid; }

  public HashMap<String, Float> getProductionInputStorage() { return ProductionInputStorage; }
  public HashMap<String, Float> getResourceCosts() { return resourceCosts; }
  public HashMap<String, Float> getResourceProductions() { return resourceProductions; }
  
  public void AddToProductionInputStorage(final String materialKey, final float value){
    if(!ProductionInputStorage.containsKey(materialKey))
      ProductionInputStorage.put(materialKey, value);
    else
      ProductionInputStorage.put(materialKey, ProductionInputStorage.get(materialKey) + value);
  }
  public HashMap<String, Float> getProducedGoods() { return ProducedGoods; }
  public void AddToProducedGoods(final String materialKey, final float value){
    if(!ProducedGoods.containsKey(materialKey))
      ProducedGoods.put(materialKey, value);
    else
      ProducedGoods.put(materialKey, ProducedGoods.get(materialKey) + value);
  }
  
  public float getStorageCapacity(final String itemName) {
      return Optional.ofNullable(storageCapacities.get(itemName)).orElse(0f);
  }

  public void UpdateConnectionPoints()
  {
    ConnectionPoints.clear();

    int h, w, tmp;
    int x, y;
    int[][] grid = buildingGrid;
    h = (grid != null) ? grid.length : 0;
    w = (h>0) ? grid[0].length : 0;

    // no grid? nothing to do
    if(h == 0 || w == 0)
      return;

    // turn grid by direction
    switch(buildingDirection)
    {
      case WEST:
        grid = Helpers.MatrixRotate90CW(grid);
        tmp = h;
        h = w;
        w = tmp;
        break;

      case NORTH:
        grid = Helpers.MatrixRotate180(grid);
        break;

      case EAST:
        grid = Helpers.MatrixRotate180(grid);
        grid = Helpers.MatrixRotate90CW(grid);
        tmp = h;
        h = w;
        w = tmp;
        break;

      default:
      case SOUTH:
        // no change
        break;
    }

    for(int hh=0; hh<h; hh++)
    {
      for (int ww = 0; ww < w; ww++)
      {
        if(grid[hh][ww] != 2)
          continue;

        y = pickingPoint.Y - (h / 2) + hh;
        x = pickingPoint.X - (w / 2) + ww;

        ConnectionPoints.addElement(new Position2D(x,y));
      }
    }
  }


  public void SetPathToTransportCenter(List<PathNode> path) {
    PathToTransportCenter = path;
  }

  public void transferFromCargo() {
      final String cargoType = deliveryVehicle.getCargoType();
      final Float cargoAmount = deliveryVehicle.getCargoAmount();
      if (cargoType != null && cargoAmount != null) {
        if(!ProductionInputStorage.containsKey(cargoType)) {
            ProductionInputStorage.put(cargoType, 0.0f);
        }

        ProductionInputStorage.put(cargoType, ProductionInputStorage.get(cargoType) + deliveryVehicle.getCargoAmount());
        readyForDelivery.put(cargoType, false);
      }
      deliveryVehicle.setCargoAmount(0f);
  }
  
  public void transferToCargo() {
      final String cargoType = transportVehicle.getCargoType();
      transportVehicle.setCargoAmount(ProducedGoods.get(cargoType));
      ProducedGoods.remove(cargoType);
      readyForTransportation.put(cargoType, false);
  }

  public void transferToStash() {
    GameActivity.MyGameLogic.AddToStash(transportVehicle.getCargoType(), transportVehicle.getCargoAmount());
    transportVehicle.setCargoAmount(0f);
  }

  public void transferFromStash() {
    final String cargoType = deliveryVehicle.getCargoType();
    
    for(MaterialValue mv : ManufacturingTable.InputMaterials) {
      if (cargoType.equals(mv.Material)) {
          GameActivity.MyGameLogic.RemoveFromStash(mv.Material, mv.Value);
          deliveryVehicle.setCargoAmount(mv.Value);
          break;
      }
    }
  }
  
  public void RemoveTransportVehicle() {
    if(planetDescriptor.isActive()) {
      GameActivity.MyGameLogic.RemoveCargoTransport(transportVehicle.vehicle);
    }
    transportVehicle = null;
    transportInProgress = false;
  }

  public void RemoveDeliveryVehicle() {
    if(planetDescriptor.isActive()) {
      GameActivity.MyGameLogic.RemoveCargoTransport(deliveryVehicle.vehicle);
    }
    deliveryVehicle = null;
    deliveryInProgress = false;
  }

  public VehicleDescriptor getTransportVehicle(){ return transportVehicle; }
  public VehicleDescriptor getDeliveryVehicle(){ return deliveryVehicle; }

  public void setTransportVehicle(final VehicleDescriptor vehicleDescriptor){ this.transportVehicle = vehicleDescriptor; }
  public void setDeliveryVehicle(final VehicleDescriptor vehicleDescriptor){ this.deliveryVehicle = vehicleDescriptor; }

  protected void CreateAnimationForTransport(List<PathNode> path, final String cargoType)
  {
    final PathNode start = path.get(0);
    
    transportVehicle = new VehicleDescriptor(planetDescriptor.getPosition(start.x, start.y), cargoType);
    if(planetDescriptor.isActive()){
      // when the planet is active, we want to show the vehicle
      transportVehicle.vehicle = GameActivity.MyGameLogic.AddCargoTransport(start.x, start.y);
    }


    MyConstants.Direction directionFirst = Helpers.GetDirection(path.get(0), path.get(1));
    float horizontalRotation = 0.0f;
    switch(directionFirst)
    {
      case EAST: horizontalRotation = 0.0f;  break;
      case WEST: horizontalRotation = 180.0f; break;
      case NORTH: horizontalRotation = 90.0f; break;
      case SOUTH: horizontalRotation = -90.0f; break;
    }
    transportVehicle.AddToTaskQueue(new PositionAnimation(0, 0.0f, 0.0f, 0.0f, 0.0f, horizontalRotation, 0.0f));

    CreateAnimationForVehicle(transportVehicle, path);
    transportVehicle.AddToTaskQueue(new VehicleJob(VehicleJob.VehicleJobTypes.LOAD_CARGO, this));

    Collections.reverse(path);
    CreateAnimationForVehicle(transportVehicle, path);
    transportVehicle.AddToTaskQueue(new VehicleJob(VehicleJob.VehicleJobTypes.UNLOAD_CARGO, this));
    Collections.reverse(path);

    transportVehicle.AddToTaskQueue(new VehicleJob(VehicleJob.VehicleJobTypes.REMOVE_TRANSPORT, this));
  }

  protected void CreateAnimationForDeliverance(List<PathNode> path, final String cargoType) {
    PathNode start = path.get(0);
    deliveryVehicle = new VehicleDescriptor(planetDescriptor.getPosition(start.x, start.y), cargoType);
    if(planetDescriptor.isActive()){
      // when the planet is active, we want to show the vehicle
      deliveryVehicle.vehicle = GameActivity.MyGameLogic.AddCargoTransport(start.x, start.y);
    }

    deliveryVehicle.AddToTaskQueue(new VehicleJob(VehicleJob.VehicleJobTypes.LOAD_DELIVERY, this));
    MyConstants.Direction directionFirst = Helpers.GetDirection(path.get(0), path.get(1));
    float horizontalRotation = 0.0f;
    switch(directionFirst)
    {
      case EAST: horizontalRotation = 0.0f;  break;
      case WEST: horizontalRotation = 180.0f; break;
      case NORTH: horizontalRotation = 90.0f; break;
      case SOUTH: horizontalRotation = -90.0f; break;
    }
    deliveryVehicle.AddToTaskQueue(new PositionAnimation(0, 0.0f, 0.0f, 0.0f, 0.0f, horizontalRotation, 0.0f));

    CreateAnimationForVehicle(deliveryVehicle, path);
    deliveryVehicle.AddToTaskQueue(new VehicleJob(VehicleJob.VehicleJobTypes.UNLOAD_DELIVERY, this));

    Collections.reverse(path);
    CreateAnimationForVehicle(deliveryVehicle, path);
    Collections.reverse(path);

    deliveryVehicle.AddToTaskQueue(new VehicleJob(VehicleJob.VehicleJobTypes.REMOVE_DELIVERY, this));
  }

  private void CreateAnimationForVehicle(VehicleDescriptor vh, List<PathNode> path)
  {
    int len = path.size();

    // ausrichtung
    MyConstants.Direction directionFirst = Helpers.GetDirection(path.get(0), path.get(1));
    MyConstants.Direction directionSecond;
    PathNode nodeFirst, nodeSecond;

    // initial
    float horizontalRotation = 0.0f;
    float verticalRotationX = 0, verticalRotationZ = 0;
    float x=0, y = 0, z=0;

    switch(directionFirst)
    {
      case EAST: x = 0.5f * MyConstants.TILE_SIZE; z = 0; break;
      case WEST:  x = -0.5f * MyConstants.TILE_SIZE; z = 0; break;
      case NORTH: x = 0; z = -0.5f * MyConstants.TILE_SIZE; break;
      case SOUTH: x = 0; z = 0.5f * MyConstants.TILE_SIZE; break;
    }
    vh.AddToTaskQueue(new PositionAnimation(500, x, 0.0f, z, 0.0f, 0.0f, 0.0f));

    boolean wasClimbing = false;
    for(int a=1; a<len-1; a++)
    {
      nodeFirst = path.get(a);
      nodeSecond = path.get(a + 1);
      directionSecond = Helpers.GetDirection(nodeFirst, nodeSecond);

      y = 0;
      if(wasClimbing)
      {
        wasClimbing = false;
        verticalRotationX *= -1.0f;
        verticalRotationZ *= -1.0f;
      }else{
        // reset
        verticalRotationX = 0;
        verticalRotationZ = 0;
      }

      if (directionFirst == directionSecond)
      {
        if (nodeFirst.EW != 0 || nodeFirst.NS != 0)
        {
          boolean up = false;
          if(nodeFirst.EW == 1 && directionFirst == MyConstants.Direction.EAST) up = true;
          if(nodeFirst.EW == -1 && directionFirst == MyConstants.Direction.WEST) up = true;
          if(nodeFirst.NS == 1 && directionFirst == MyConstants.Direction.NORTH) up = true;
          if(nodeFirst.NS == -1 && directionFirst == MyConstants.Direction.SOUTH) up = true;

          y = (up ? 0.5f : -0.5f) * MyConstants.TILE_SIZE;
          if (directionFirst == MyConstants.Direction.EAST || directionFirst == MyConstants.Direction.WEST)
            verticalRotationZ = (up ? 30.0f : -30.0f) * (directionFirst == MyConstants.Direction.WEST ? -1.0f : 1.0f);
          else
            verticalRotationX = (up ? 30.0f : -30.0f) * (directionFirst == MyConstants.Direction.SOUTH ? -1.0f : 1.0f);

          wasClimbing = true;
        }
      }

      switch(directionFirst)
      {
        case EAST:
          if(directionSecond == MyConstants.Direction.EAST)
          {
            x = MyConstants.TILE_SIZE; z = 0; horizontalRotation = 0;
          }
          else
          {
            if(directionSecond == MyConstants.Direction.NORTH)
            {
              x = 0.5f * MyConstants.TILE_SIZE; z = -0.5f * MyConstants.TILE_SIZE; horizontalRotation = 90.0f;
            }else{
              x = 0.5f * MyConstants.TILE_SIZE; z = 0.5f * MyConstants.TILE_SIZE; horizontalRotation = -90.0f;
            }
          }
          break;

        case WEST:
          if(directionSecond == MyConstants.Direction.WEST)
          {
            x = -MyConstants.TILE_SIZE; z = 0; horizontalRotation = 0;
          }
          else
          {
            if(directionSecond == MyConstants.Direction.NORTH)
            {
              x = -0.5f * MyConstants.TILE_SIZE; z = -0.5f * MyConstants.TILE_SIZE; horizontalRotation = -90.0f;
            }else{
              x = -0.5f * MyConstants.TILE_SIZE; z = 0.5f * MyConstants.TILE_SIZE; horizontalRotation = 90.0f;
            }
          }
          break;

        case NORTH:
          if(directionSecond == MyConstants.Direction.NORTH)
          {
            x = 0; z = -MyConstants.TILE_SIZE; horizontalRotation = 0;
          }
          else
          {
            if(directionSecond == MyConstants.Direction.EAST)
            {
              x = 0.5f * MyConstants.TILE_SIZE; z = -0.5f * MyConstants.TILE_SIZE; horizontalRotation = -90.0f;
            }else{
              x = -0.5f * MyConstants.TILE_SIZE; z = -0.5f * MyConstants.TILE_SIZE; horizontalRotation = 90.0f;
            }
          }
          break;

        case SOUTH:
          if(directionSecond == MyConstants.Direction.SOUTH)
          {
            x = 0; z = MyConstants.TILE_SIZE; horizontalRotation = 0;
          }
          else
          {
            if(directionSecond == MyConstants.Direction.EAST)
            {
              x = 0.5f * MyConstants.TILE_SIZE; z = 0.5f * MyConstants.TILE_SIZE; horizontalRotation = 90.0f;
            }else{
              x = -0.5f * MyConstants.TILE_SIZE; z = 0.5f * MyConstants.TILE_SIZE; horizontalRotation = -90.0f;
            }
          }
          break;
      }
      vh.AddToTaskQueue(new PositionAnimation(1000, x, y, z, verticalRotationX, horizontalRotation, verticalRotationZ));
      directionFirst = directionSecond;
    }

    if(wasClimbing)
    {
      verticalRotationX *= -1.0f;
      verticalRotationZ *= -1.0f;
    }else{
      // reset
      verticalRotationX = 0;
      verticalRotationZ = 0;
    }
    switch(directionFirst)
    {
      case EAST: x = 0.5f * MyConstants.TILE_SIZE; z = 0; break;
      case WEST: x = -0.5f * MyConstants.TILE_SIZE; z = 0; break;
      case NORTH:  x = 0; z = -0.5f * MyConstants.TILE_SIZE; break;
      case SOUTH: x = 0; z = 0.5f * MyConstants.TILE_SIZE; break;
    }
    vh.AddToTaskQueue(new PositionAnimation(500, x, 0.0f, z, verticalRotationX, 0.0f, verticalRotationZ));
    vh.AddToTaskQueue(new PositionAnimation(0, 0.0f, 0.0f, 0.0f, 0.0f, 180.0f, 0.0f));
  }


  public void LoadBuildingDefinition(){
    final BuildingDef def = GameActivity.MyGameLogic.GameRules.getBuildingDefinition(buildingType);
    for(MaterialValue mv : def.GetProductionValues())
    {
      ProductionValues.put(mv.Material, planetDescriptor.GetMaterialAtLocation(mv.Material, pickingPoint.X, pickingPoint.Y));
      if(!ProducedGoods.containsKey(mv.Material))
        ProducedGoods.put(mv.Material, 0.0f);
    }
    
    for(MaterialValue mv : def.getResourceCosts()) {
        resourceCosts.put(mv.Material, mv.Value);
    }
    
    for(MaterialValue mv : def.getResourceProductions()) {
        resourceProductions.put(mv.Material, mv.Value);
    }
    
    for(MaterialValue mv : def.getStorageCapacities()) {
        storageCapacities.put(mv.Material, mv.Value);
    }
    
    ManufacturingTable = def.GetManufacturingTable();
  }

  public void Transport(float deltaMs) {
    for(final String key : readyForTransportation.keySet()) {
      if(readyForTransportation.get(key) && !transportInProgress && PathToTransportCenter != null) {
        CreateAnimationForTransport(PathToTransportCenter, key);
        transportInProgress = true;
      }
    }
    
    float storageFillquota = 100;
    if (!deliveryInProgress && PathToTransportCenter != null) {
      String cargoType = null;
      
      for(final String key : readyForDelivery.keySet()) {
        if (readyForDelivery.get(key) ) {
          float current = ProductionInputStorage.containsKey(key) ? ProductionInputStorage.get(key) : 0f;
          final float quota = current / storageCapacities.get(key);
          if(quota < storageFillquota) {
            cargoType = key;
            storageFillquota = quota;
          }
        }
      }
      
      if(cargoType != null) {
        CreateAnimationForDeliverance(PathToTransportCenter, cargoType);
        deliveryInProgress = true;
      }
    }
    
    for(final String key : readyForDelivery.keySet()) {
      if(readyForDelivery.get(key) && !deliveryInProgress && PathToTransportCenter != null) {
        
      }
    }

    if(transportVehicle != null)
      transportVehicle.Process(deltaMs);
    if(deliveryVehicle != null)
      deliveryVehicle.Process(deltaMs);
  }
  public void Production(float deltaMs) { }

  
  public Vector<MaterialValue> getInputMaterials() {
      return ManufacturingTable.InputMaterials;
  }

}
