package de.sereal.apps.genesisproject;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Vector;

import de.sereal.apps.genesisproject.obj.PlanetSurface;
import de.sereal.apps.genesisproject.obj.building.Building;
import de.sereal.apps.genesisproject.obj.building.BuildingDescriptor;
import de.sereal.apps.genesisproject.obj.building.BuildingDescriptorProcessing;
import de.sereal.apps.genesisproject.obj.building.BuildingDescriptorProduction;
import de.sereal.apps.genesisproject.obj.planet.PlanetDescriptor;
import de.sereal.apps.genesisproject.obj.vehicles.Vehicle;
import de.sereal.apps.genesisproject.obj.vehicles.VehicleTransport;
import de.sereal.apps.genesisproject.rules.BuildingDef;
import de.sereal.apps.genesisproject.rules.MaterialValue;
import de.sereal.apps.genesisproject.rules.Rules;
import de.sereal.apps.genesisproject.util.ArrayHelpers;
import de.sereal.apps.genesisproject.util.MyConstants;
import de.sereal.apps.genesisproject.util.Vector3D;

/**
 * Created by sereal on 08.08.2016.
 */
public class GameLogic
{

  public final static int MOUSE_MOVE = 0;
  public final static int MOUSE_DRAG = 1;
  public final static int MOUSE_DRAG_INTO_DIRECTION = 2;
  public final static int MOUSE_DRAG_FOR_PLANE = 3;
  public int MouseMoveAction =  MOUSE_MOVE;

  public Rules GameRules = new Rules();
  public int[][] SelectionGrid;
  public boolean ReadyToBuild = false;

  private boolean isPaused = true;
  private boolean GameLogicRunning = false;

  private Context mContext;
  private PlanetSurface planetSurface;
  private GLHudOverlayView refToHud;

  private HashMap<String, Float> AvailableMaterials = new HashMap<>();
  private HashMap<String, Float> AvailableMaterialAtLocation = new HashMap<>();
  private HashMap<String, MaterialValue> AvailableResources = new HashMap<>();

  private float MinuteOfTheDay = 0.0f;
  private int day = 1;
  private float GameSpeed = 600; // 60: 1 min = 1h, 600: 1 min = 10h gametime
  private long NextRefresh = 0;

  public Building SelectedBuilding = null;
  private String activePlanet = "";

  public float GetMinuteOfTheDay()
  {
    return MinuteOfTheDay;
  }
  
  public String getFormattedDateTime() {
      int h = (int)(MinuteOfTheDay / 60);
      int m = (int)(MinuteOfTheDay % 60);
      return String.format("Day %04d, %02d:%02d", day, h, m);
  }

  protected void SetMinuteOfTheDay(final float minutesOfTheDay){
    this.MinuteOfTheDay = minutesOfTheDay;
  }

  public GameLogic(Context context)
  {
    mContext = context;
    GameRules.LoadRules(mContext);

    AvailableMaterials.put("MATERIAL_RARE_EARTH", 100000.0f);
    AvailableMaterials.put("MATERIAL_CORE_ICE", 10000.0f);
    AvailableResources.put("RESOURCE_POWER", new MaterialValue("RESOURCE_POWER", 0));

    AvailableMaterialAtLocation.put("MATERIAL_RARE_EARTH", 0.0f);
    AvailableMaterialAtLocation.put("MATERIAL_CORE_ICE", 0.0f);
  }

  public void AddToStash(String materialKey, float value)
  {
    if(!AvailableMaterials.containsKey(materialKey))
      AvailableMaterials.put(materialKey, value);
    else
      AvailableMaterials.put(materialKey, AvailableMaterials.get(materialKey) + value);
    UpdateGui();
  }

  public void RemoveFromStash(String materialKey, float value)
  {
    AvailableMaterials.put(materialKey, AvailableMaterials.get(materialKey) - value);
    UpdateGui();
  }

  public void AttachHud(GLHudOverlayView refToHud)
  {
    this.refToHud = refToHud;
    SetGameLogicRunning(true);
  }

  public void BuildingClicked(Building building)
  {
    SelectedBuilding = building;
    refToHud.ShowDetailsForSelectedBuilding();
  }

  public void SetPlanetSurface(final PlanetSurface planetSurface)
  {
    this.planetSurface = planetSurface;
  }

  public void SetMouseMoveAction(int action)
  {
    MouseMoveAction = action;
  }

  public void SetSelectionGrid(int[][] grid)
  {
    SelectionGrid = grid;
    planetSurface.Deselect();
  }

  public void SetShowConnectionsPoints(boolean value)
  {
    planetSurface.ShowConnectionPoints = value;
  }

  public void SetPickType(int type)
  {
    planetSurface.SetPickType(type);
  }

  public void SetResourceMap(MyConstants.ResourceMapStyle style)
  {
    planetSurface.SetResourceMapStyle(style);
  }

  public void LandscapeRaise()
  {
    planetSurface.ChangeLandscape(true);
  }

  public void LandscapeLower()
  {
    planetSurface.ChangeLandscape(false);
  }

  /**
   * Construction af street confirmed, now add it to the planet surface for drawing etc
   */
  public void AddTransport()
  {
    planetSurface.AddTransport();
  }

  public Vehicle AddCargoTransport(int x, int y)
  {
    return planetSurface.addVehicle(VehicleTransport.KEY, x, y);
  }

  public void RemoveCargoTransport(Vehicle vehicle)
  {
    planetSurface.RemoveVehicle(vehicle);
  }

  public void SetReadyToBuild(boolean value)
  {
    ReadyToBuild = value;
    UpdateGui();
  }

  private void UpdateGui()
  {
    ((GameActivity)mContext).runOnUiThread(new Runnable() {
      @Override
      public void run()
      {
          
        if(refToHud != null)
          refToHud.invalidate();
      }
    });
  }

  public void SetMaterialAtLocation(String key, float value)
  {
    if(AvailableMaterialAtLocation.containsKey(key))
    {
      AvailableMaterialAtLocation.put(key, value);
    }
  }

  public float GetMaterialAtLocation(String key)
  {
    if(!AvailableMaterialAtLocation.containsKey(key))
      return 0.0f;

    return AvailableMaterialAtLocation.get(key);
  }

  public void confirmBuild(final String buildingKey)
  {
    if(planetSurface != null)
    {
      final BuildingDef buildingDef = GameRules.getBuildingDefinition(buildingKey);

      boolean hasMaterials = true;
      for(MaterialValue mv : buildingDef.GetConstructionCosts())
      {
        if(!AvailableMaterials.containsKey(mv.Material))
        {
          Log.d("ConfirmBuild","Material " + mv.Material+" not found! :(");
        }else{
          if(AvailableMaterials.get(mv.Material) < mv.Value)
          {
            hasMaterials = false;
          }
        }
      }

      if(hasMaterials)
      {
        final BuildingDescriptor bd = getBuildingDescriptorByKey(buildingKey);
        bd.setPlanetDescriptor(planetDescriptions.get(activePlanet));
        bd.setBuildingType(buildingKey);
        bd.setBuildingDirection(planetSurface.getBuildDirection());
        final Vector3D pos = planetSurface.getBuildPosition();
        bd.setBuildingPosition(new Vector3D(pos.x, pos.y, pos.z));
        bd.setPickingPoint(planetSurface.getPickingPoint());
        bd.setBuildingGrid(ArrayHelpers.deepCopyIntMatrix(SelectionGrid));
        bd.LoadBuildingDefinition();
        Log.d("BuildingGridAdd",""+ArrayHelpers.flatten(bd.getBuildingGrid()));

        if(planetSurface.AddBuilding(bd))
        {
          planetDescriptions.get(activePlanet).addBuilding(bd);
          for(MaterialValue mv : buildingDef.GetConstructionCosts())
          {
            AvailableMaterials.put(mv.Material, AvailableMaterials.get(mv.Material) + mv.Value);
          }
          for(MaterialValue mv : buildingDef.GetResourceValues())
          {
            AvailableResources.get(mv.Material).Value += mv.Value;
          }
        }
      }
    }
    SetReadyToBuild(false);
  }

  public BuildingDescriptor getBuildingDescriptorByKey(final String buildingKey){
    switch(buildingKey)
    {
      case "BASE_CENTER":
      case "MINE_CORE_ICE":
      case "RE_MINE":
      case "HE3_MINE":
        return new BuildingDescriptorProduction();

      case "PROD_OXYGEN":
      case "MELTER":
        return new BuildingDescriptorProcessing();

      default:
        return new BuildingDescriptor();
    }
  }

  public Vector<MaterialValue> GetAvailableResources()
  {
    Vector<MaterialValue> returnVal = new Vector<>();
    for(MaterialValue mv : AvailableResources.values())
    {
      returnVal.addElement(mv);
    }
    return returnVal;
  }

  // TODO: only visible values!
  public Vector<MaterialValue> GetVisibleMaterialValues()
  {
    Vector<MaterialValue> returnVal = new Vector<>();
    for(String materialKey : AvailableMaterials.keySet())
    {
      if(materialKey == "MATERIAL_RARE_EARTH" || materialKey == "MATERIAL_CORE_ICE")
        returnVal.addElement(new MaterialValue(materialKey, AvailableMaterials.get(materialKey)));
    }
    return returnVal;
  }

  public HashMap<String, Float> GetAllAvailableMaterialsInStash()
  {
    return AvailableMaterials;
  }

  private HashMap<String, PlanetDescriptor> planetDescriptions = new HashMap<>();

  public HashMap<String, PlanetDescriptor> getAllPlanetDescriptions() {
    return planetDescriptions;
  }

  public void addPlanetDescription(final PlanetDescriptor planetDescriptor) {
    planetDescriptions.put(planetDescriptor.getName(), planetDescriptor);
  }

  public void loadPlanet(final String name) {
    for (final PlanetDescriptor pd : planetDescriptions.values())
    {
      pd.setActive(pd.getName().equals(name));
    }
    this.activePlanet = name;
    this.planetSurface.setPlanetDescription(planetDescriptions.get(name));
  }


  public void SetGameLogicRunning(boolean value)
  {
    GameLogicRunning = value;
    if(GameLogicRunning)
      StartGameLogic();
  }

  private void StartGameLogic()
  {
    GameLogicRunning = true;

    new Thread()
    {
      public void run()
      {
        long t1 = System.currentTimeMillis();
        long t2;

        while(GameLogicRunning)
        {
          try
          {
            // This is your target delta. 25ms = 40fps
            Thread.sleep(25);
          }
          catch (InterruptedException e)
          {
            e.printStackTrace();
          }

          t2 = System.currentTimeMillis();
          if(NextRefresh < t2)
          {
            NextRefresh = t2 + 1000;
            refToHud.Refresh();
          }
          long delta = t2 - t1;
          t1 = t2;

          if(!isPaused) {
            UpdateLogic((int)delta);
          }
        }
      }
    }.start();
  }

  public void setIsPaused(final boolean isPaused) {
    this.isPaused = isPaused;
  }

  private void UpdateLogic(int deltaMs)
  {
    // every second 10 minutes pass
    MinuteOfTheDay = (MinuteOfTheDay  + (float)deltaMs / 60000.0f * GameSpeed) % 1440.0f;

    for (PlanetDescriptor pd : planetDescriptions.values())
    {
      for(BuildingDescriptor bd : pd.getBuildings()){
        bd.Production(deltaMs);
        bd.Transport(deltaMs);
      }
    }

    if(planetSurface!=null)
    {
      planetSurface.Animate(deltaMs);
    }

    refToHud.refreshCritical();

//    ((GameActivity)mContext).runOnUiThread(new Runnable() {
//			@Override
//			public void run()
//			{
//			}
//		});
  }



}
