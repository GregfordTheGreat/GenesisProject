package de.sereal.apps.genesisproject;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import de.sereal.apps.genesisproject.event.DragGestureDetector;
import de.sereal.apps.genesisproject.event.RotationGestureDetector;
import de.sereal.apps.genesisproject.event.TouchEvent;
import de.sereal.apps.genesisproject.obj.building.BuildingDescriptor;
import de.sereal.apps.genesisproject.obj.planet.PlanetDescriptor;
import de.sereal.apps.genesisproject.obj.vehicles.VehicleDescriptor;
import de.sereal.apps.genesisproject.util.ArrayHelpers;
import de.sereal.apps.genesisproject.util.MyConstants;
import de.sereal.apps.genesisproject.util.Position2D;
import de.sereal.apps.genesisproject.util.PositionAnimation;
import de.sereal.apps.genesisproject.util.RoadMap;
import de.sereal.apps.genesisproject.util.TerrainGen;
import de.sereal.apps.genesisproject.util.TextureHandler;
import de.sereal.apps.genesisproject.util.Vector3D;
import de.sereal.apps.genesisproject.util.VehicleJob;
import de.sereal.apps.genesisproject.util.VehicleTask;

import android.view.ViewGroup.LayoutParams;

/**
 * Created by sereal on 01.08.2016.
 */
public class GameActivity extends Activity implements OnTouchListener
{

  @Override
  public void onStart()
  {
    super.onStart();
  }

  @Override
  public void onStop()
  {
    super.onStop();
  }

  public enum ViewTypes
  {
    PLANET_VIEW(0),
    SYSTEM_VIEW(1);

    private int value;

    private ViewTypes(int v)
    {
      this.value = v;
    }
  }

  public enum ZoomAction
  {
    ZOOM_CANCEL,
    ZOOM_IN,
    ZOOM_OUT
  }

  private GLSurfaceView glSurfaceView;
  private GLHudOverlayView glHudOverlayView;

  public static GameLogic MyGameLogic;
  private SoundPool MySoundPool;
  private MediaPlayer mediaPlayer;
  public ScaleListener scaleListener;
  private ViewTypes viewType;
  private ZoomAction zoomAction = ZoomAction.ZOOM_CANCEL;
  private long zoomActionRequested;
  private DispatchingRenderer dispatchingRenderer;

  public GameActivity()
  {
    viewType = ViewTypes.PLANET_VIEW;
  }

  public void SetViewType(ViewTypes newViewType)
  {
    viewType = newViewType;
    dispatchingRenderer.SetActiveRenderer(newViewType);
  }


  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    TextureHandler.Init(this);
    MyGameLogic = new GameLogic(this);

    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


    // Check if the system supports OpenGL ES 2.0.
    final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
    final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

    glHudOverlayView = new GLHudOverlayView(this);
    glSurfaceView = new GLSurfaceView(this);
    glSurfaceView.setOnTouchListener(this);

    Log.e("GL ES version:", "" + configurationInfo.reqGlEsVersion);
    if (supportsEs2) {
      glSurfaceView.setEGLContextClientVersion(2);
      dispatchingRenderer = new DispatchingRenderer(this);
      glSurfaceView.setRenderer(dispatchingRenderer);

      SetViewType(ViewTypes.PLANET_VIEW);
    } else {
      // ES 1.x renderer?
      return;
    }

    setContentView(glSurfaceView);
    addContentView(glHudOverlayView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));


    Log.e("Screen lock", "ON");
    glSurfaceView.setKeepScreenOn(true);

    scaleListener = new ScaleListener();
    myScaleGestureDetector = new ScaleGestureDetector(getApplicationContext(), scaleListener);
    myRotationGestureDetector = new RotationGestureDetector(new RotationListener());
    myDragGestureDetector = new DragGestureDetector(new DragListener());

    AudioAttributes attributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();

    MySoundPool = new SoundPool.Builder()
            .setAudioAttributes(attributes)
            .build();

    MySoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener()
    {
      @Override
      public void onLoadComplete(SoundPool soundPool, int sampleId, int status)
      {
        for (SoundpoolObject obj : SoundPoolMap.values()) {
          if (sampleId == obj.SampleID) {
            obj.ready = true;
            MySoundPool.play(sampleId, 0.5f, 0.5f, 1, 0, 1.0f);
            break;
          }
        }
      }
    });

    mediaPlayer = MediaPlayer.create(this, R.raw.background);
    mediaPlayer.seekTo((int) ((float) mediaPlayer.getDuration() * Math.random()));
    mediaPlayer.setLooping(true);
    mediaPlayer.start();

    if(getIntent().hasExtra("savegame")) {
      loadGame(getIntent().getStringExtra("savegame"));
    }else{
      createNewGame();
    }
  }

  private class SoundpoolObject
  {
    public int SampleID;
    public boolean ready;

    public SoundpoolObject(int sampleID)
    {
      SampleID = sampleID;
    }
  }

  private HashMap<Integer, SoundpoolObject> SoundPoolMap = new HashMap<>();

  public void PlaySoundEffect(int resourceID)
  {
    if (SoundPoolMap.containsKey(resourceID)) {
      if (SoundPoolMap.get(resourceID).ready) {
        MySoundPool.play(SoundPoolMap.get(resourceID).SampleID, 0.5f, 0.5f, 1, 0, 1.0f);
      }
    } else {
      SoundPoolMap.put(resourceID, new SoundpoolObject(MySoundPool.load(this, resourceID, 1)));
    }
  }

  public ScaleGestureDetector myScaleGestureDetector;
  private RotationGestureDetector myRotationGestureDetector;
  private DragGestureDetector myDragGestureDetector;

  private Vector<TouchEvent> TouchEventListeners = new Vector<TouchEvent>();

  public void AddTouchEventListener(TouchEvent listener)
  {
    TouchEventListeners.add(listener);
  }

  private class DragListener extends DragGestureDetector.SimpleDragGestureListener
  {
    @Override
    public boolean OnDrag(DragGestureDetector dragDetector)
    {
      // do some magic here to determine if we move or drag
      switch (MyGameLogic.MouseMoveAction) {
        case GameLogic.MOUSE_MOVE:
          for (TouchEvent te : TouchEventListeners) {
            te.OnMove(dragDetector.GetDragDistanceX(), dragDetector.GetDragDistanceY());
          }
          break;

        case GameLogic.MOUSE_DRAG:
        case GameLogic.MOUSE_DRAG_INTO_DIRECTION:
        case GameLogic.MOUSE_DRAG_FOR_PLANE:
          for (TouchEvent te : TouchEventListeners) {
            te.OnDrag(dragDetector.GetX(), dragDetector.GetY());
          }
          break;
      }


      return true;
    }

    @Override
    public boolean OnTilt(DragGestureDetector dragDetector)
    {
      for (TouchEvent te : TouchEventListeners) {
        te.OnTilt(dragDetector.GetTiltDistance());
      }
      return true;
    }
  }

  public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
  {
    private float ScaleFactor = 1.0f;
    private float StartSpan;
    private float currentSpan;

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector)
    {
      StartSpan = detector.getCurrentSpan();
      Log.d("ScaleListener", "Start");
      return super.onScaleBegin(detector);
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector)
    {
      float scale = StartSpan / detector.getCurrentSpan();
      ScaleFactor = Math.max(Math.min(ScaleFactor * scale, 4.0f), 0.5f);
//      Log.d("ScaleListener",String.format("Current: %1$s, Start: %2$s, scale: %3$s, scaleF: %4$s new ScaleF: %5$s",detector.getCurrentSpan(), StartSpan, scale, detector.getScaleFactor(), ScaleFactor));
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector)
    {
      currentSpan = detector.getCurrentSpan();
      float scale = StartSpan / currentSpan;
      scale = Math.max(Math.min(ScaleFactor * scale, 4.0f), 0.5f);


      if (scale == 0.5f && viewType.value < ViewTypes.SYSTEM_VIEW.value) {
        if (zoomAction == ZoomAction.ZOOM_OUT) {
          if (System.currentTimeMillis() - zoomActionRequested > 1000) {
            SetViewType(ViewTypes.SYSTEM_VIEW);
            zoomAction = ZoomAction.ZOOM_CANCEL;
            ResetTo(4.0f);
            Log.d("ZOOM", "END OUT");
          }
        } else {
          zoomActionRequested = System.currentTimeMillis();
          zoomAction = ZoomAction.ZOOM_OUT;
          Log.d("ZOOM", "START OUT");
        }
      } else if (scale == 4.0f && viewType.value > ViewTypes.PLANET_VIEW.value) {
        if (zoomAction == ZoomAction.ZOOM_IN) {
          if (System.currentTimeMillis() - zoomActionRequested > 500) {
            SetViewType(ViewTypes.PLANET_VIEW);
            zoomAction = ZoomAction.ZOOM_CANCEL;
            ResetTo(0.5f);
            Log.d("ZOOM", "END IN");
          }
        } else {
          zoomActionRequested = System.currentTimeMillis();
          zoomAction = ZoomAction.ZOOM_IN;
          Log.d("ZOOM", "START IN");
        }
      } else {
        zoomAction = ZoomAction.ZOOM_CANCEL;
        Log.d("ZOOM", "CANCEL");
      }

      Log.d("Calling listeners", "ScaleUpdate:" + scale);
      for (TouchEvent te : TouchEventListeners) {
        te.OnScale(scale);
      }

      return true;
    }

    private void ResetTo(float scale)
    {
      StartSpan = currentSpan;// * scale;
      ScaleFactor = scale;
    }
  }

  private class RotationListener extends RotationGestureDetector.SimpleOnRotationGestureListener
  {
    private float Angle = 0.0f;

    @Override
    public void OnRotationEnd(RotationGestureDetector rotationDetector)
    {
      float angle = rotationDetector.getAngle() + Angle;
      while (angle < 0.0f) angle += 360.0f;
      while (angle >= 360.0f) angle -= 360.0f;
      Angle = angle;
    }

    @Override
    public boolean OnRotation(RotationGestureDetector rotationDetector)
    {
      float angle = rotationDetector.getAngle() + Angle;
//      Log.d("RotationGestureDetector", "Rotation: " + Float.toString(angle));
      while (angle < 0.0f) angle += 360.0f;
      while (angle >= 360.0f) angle -= 360.0f;

      for (TouchEvent te : TouchEventListeners) {
        te.OnRotation(angle);
      }
      return true;
    }

  }

  @Override
  public boolean onTouch(View v, MotionEvent event)
  {
    if (!glHudOverlayView.onTouchEvent(event)) {

      if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
        dispatchingRenderer.touchClick(event.getX(), event.getY());
        //TODO: planetSurfaceRenderer.touchClick(event.getX(), event.getY());
      }


      myScaleGestureDetector.onTouchEvent(event);
      myRotationGestureDetector.onTouchEvent(event);
      myDragGestureDetector.onTouchEvent(event);
    }
    return true;
  }

  @Override
  public void onBackPressed()
  {
    closeApplication();
  }

  private void SavePrivateSettings()
  {
    try {
//           	hiddenSettings.SetFloat("cam_pos_x", mRenderer.glCamera.position.x);
//            hiddenSettings.SetFloat("cam_pos_y", mRenderer.glCamera.position.y);
//            hiddenSettings.SetFloat("cam_pos_z", mRenderer.glCamera.position.z);
//            hiddenSettings.SaveSettings();
    } catch (Exception e) {
      Log.e("", "");
    }
  }

  public String ReadRawFile(int id)
  {
    String retValue = "";
    InputStream inStream = getResources().openRawResource(id);
    int n;
    byte[] b = new byte[1024];
    try {
      while ((n = inStream.read(b, 0, 1024)) != -1) {
        retValue += new String(b, 0, n);
      }
    } catch (IOException e) {
      Log.e("OpenGL20Renderer", "Error reading raw file", e);
    }
    return retValue;
  }

  private int loadShader(int shaderType, String source)
  {
    // create shader
    int shader = GLES20.glCreateShader(shaderType);
    if (shader != 0) {
      // load source code string
      GLES20.glShaderSource(shader, source);
      // compile shader
      GLES20.glCompileShader(shader);
      // get compile status
      int[] compiled = new int[1];
      GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);

      if (compiled[0] == 0) {
        Log.e("LoadShader", "Could not compile shader " + shaderType + ": ");
        Log.e("LoadShader", GLES20.glGetShaderInfoLog(shader));
        GLES20.glDeleteShader(shader);
        shader = 0;
      }
    }

    if (shader == 0) Log.e("LoadShader", "Loading shader failed (" + shader + ")");
    else Log.d("LoadShader", "Successfully loaded shader (" + shader + ")");
    return shader;
  }

  public int createProgram(String vShader, String fShader)
  {
    int prog;

    // load vertex shader
    int vertexShaderHandle = loadShader(GLES20.GL_VERTEX_SHADER, vShader);
    // load fragment shader
    int fragmentShaderHandle = loadShader(GLES20.GL_FRAGMENT_SHADER, fShader);

    prog = GLES20.glCreateProgram();

    // check if failed
    if (prog == 0) {
      Log.e("CreateProgram", "Failed creating program");
      return 0;
    }

    // attach vertex shader
    GLES20.glAttachShader(prog, vertexShaderHandle);
    // attach fragment shader
    GLES20.glAttachShader(prog, fragmentShaderHandle);

    // link program
    GLES20.glLinkProgram(prog);

    // check the link
    int[] linked = new int[1];
    GLES20.glGetProgramiv(prog, GLES20.GL_LINK_STATUS, linked, 0);
    if (linked[0] == 0) {
      Log.e("Linker", "Error linking program:");
      Log.e("Linker", GLES20.glGetProgramInfoLog(prog));
      GLES20.glDeleteProgram(prog);
      prog = 0;
    }
    return prog;
  }

  private void saveGame(final String filename)
  {
    try{

      final File file = new File(getApplicationContext().getFilesDir(), filename + ".dat");
      final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));

      oos.writeBytes("1.00");
      oos.writeFloat(MyGameLogic.GetMinuteOfTheDay());
      final HashMap<String, PlanetDescriptor> planets = MyGameLogic.getAllPlanetDescriptions();
      oos.writeShort(planets.size());
      for (final String name : planets.keySet()) {
        final PlanetDescriptor pd = planets.get(name);

        oos.writeShort(name.length());
        oos.writeBytes(name);

        final int widthSegments = pd.getWidthSegments();
        final int heightSegments = pd.getHeightSegments();
        oos.writeShort(widthSegments);
        oos.writeShort(heightSegments);

        int[][] heightmap = pd.getHeightmap();
//        Log.d("savegame",heightmap.length +"x"+heightmap[0].length);
        for (int h=0; h < heightmap.length; h++)
          for (int w=0; w < heightmap[h].length; w++)
          {
            oos.writeInt(heightmap[h][w]);
          }

        // streets
        final RoadMap roadMap = pd.getRoadMap();
        oos.write(roadMap.getFlatRoadMap());

        // materials
        oos.write(ArrayHelpers.flatten(pd.getResource_RareEarth()));
        oos.write(ArrayHelpers.flatten(pd.getResource_CoreIce()));

        final CopyOnWriteArrayList<BuildingDescriptor> buildings = pd.getBuildings();
        oos.writeShort(buildings.size());
        for (final BuildingDescriptor bd : buildings)
        {
//          Log.d("savingPos","nextBuilding");
          oos.writeUTF(bd.getBuildingType());
          oos.writeInt(bd.getBuildingDirection().getValue());
          Vector3D buildingPos = bd.getBuildingPosition();
//          Log.d("savingPos",buildingPos.x+"//"+buildingPos.z);
          oos.writeFloat(buildingPos.x);
          oos.writeFloat(buildingPos.y);
          oos.writeFloat(buildingPos.z);
//          Log.d("SavingPickPos",bd.getPickingPoint().X+"//"+bd.getPickingPoint().Y);

          oos.writeInt(bd.getPickingPoint().X);
          oos.writeInt(bd.getPickingPoint().Y);
//          Log.d("BuildingGridSave",""+ArrayHelpers.flatten(bd.getBuildingGrid()));
          int[][] grid = bd.getBuildingGrid();
          oos.writeInt(grid.length);
          oos.writeInt(grid.length > 0 ? grid[0].length : 0);


          for (int h=0; h < grid.length; h++)
            for (int w=0; w < grid[h].length; w++)
            {
              oos.writeInt(grid[h][w]);
            }

          final HashMap<String, Float> producedGoods = bd.getProducedGoods();
          oos.writeShort(producedGoods.size());
          for(final String key : producedGoods.keySet()) {
            oos.writeShort(key.length());
            oos.writeBytes(key);
            oos.writeFloat(producedGoods.get(key));
          }

          final HashMap<String, Float> productionInputStorage = bd.getProductionInputStorage();
          oos.writeShort(productionInputStorage.size());
          for(final String key : productionInputStorage.keySet()) {
            oos.writeShort(key.length());
            oos.writeBytes(key);
            oos.writeFloat(productionInputStorage.get(key));
          }

          oos.writeBoolean(bd.ReadyForTransportation);
          oos.writeBoolean(bd.TransportationInProgress);
          oos.writeBoolean(bd.ReadyForDeliverance);
          oos.writeBoolean(bd.DeliveranceInProgress);
          saveVehicle(oos, bd.getTransportVehicle());
          saveVehicle(oos, bd.getDeliveryVehicle());
        }

      }



      oos.flush();
      oos.close();
    }catch (IOException e){

    }
  }

  private void saveVehicle(final ObjectOutputStream oos, final VehicleDescriptor vehicleDescriptor) {
    try {
      if(vehicleDescriptor != null) {
        oos.writeBoolean(true);
        oos.writeFloat(vehicleDescriptor.position.x);
        oos.writeFloat(vehicleDescriptor.position.y);
        oos.writeFloat(vehicleDescriptor.position.z);
        oos.writeFloat(vehicleDescriptor.rotation.x);
        oos.writeFloat(vehicleDescriptor.rotation.y);
        oos.writeFloat(vehicleDescriptor.rotation.z);

        final HashMap<String, Float> vehicleCargo = vehicleDescriptor.getCargo();
        oos.writeShort(vehicleCargo.size());
        for (final String key : vehicleCargo.keySet()) {
          oos.writeShort(key.length());
          oos.writeBytes(key);
          oos.writeFloat(vehicleCargo.get(key));
        }

        final Vector<VehicleTask> taskQueue = vehicleDescriptor.getTaskQueue();
        oos.writeShort(taskQueue.size());
        for(VehicleTask task : taskQueue) {
          if(task instanceof VehicleJob) {
            oos.writeShort(VehicleTask.VehicleTaskType.VEHICLE_JOB.getValue());
            oos.writeInt( ((VehicleJob)task).JobType.getValue() );
          }else
          if(task instanceof PositionAnimation){
            oos.writeShort(VehicleTask.VehicleTaskType.VEHICLE_POS_ANIM.getValue());
            PositionAnimation pa = (PositionAnimation)task;
            oos.writeInt(pa.TimeSpanMs);
            oos.writeFloat(pa.PositionChange.x);
            oos.writeFloat(pa.PositionChange.y);
            oos.writeFloat(pa.PositionChange.z);
            oos.writeFloat(pa.RotationX);
            oos.writeFloat(pa.RotationY);
            oos.writeFloat(pa.RotationZ);
          }
        }

      }else{
        oos.writeBoolean(false);
      }
    }catch (IOException ioex){
      Log.e("saveVehicle","failed",ioex);
    }
  }

  private VehicleDescriptor loadVehicle(final ObjectInputStream ois, final BuildingDescriptor buildingDescriptord) {
    try{
      if(ois.readBoolean()){
        final Vector3D position = new Vector3D(ois.readFloat(), ois.readFloat(), ois.readFloat());
        final VehicleDescriptor vehicleDescriptor = new VehicleDescriptor(position);
        vehicleDescriptor.rotation = new Vector3D(ois.readFloat(), ois.readFloat(), ois.readFloat());


        byte[] bytes;
        short count = ois.readShort();
        while(count-- > 0){
          bytes = readNBytes(ois, ois.readShort());
          vehicleDescriptor.AddCargo(new String(bytes), ois.readFloat());
        }

        Log.d("loadVehicle",position.x + "//"+position.y+"//"+position.z);

        count = ois.readShort();
//        Log.d("loadTasks",""+count);
        while(count-- > 0){
//          Log.d("loadTask",""+count);
          final VehicleTask.VehicleTaskType type = VehicleTask.VehicleTaskType.parse(ois.readShort());
          switch(type)
          {
            case VEHICLE_JOB:
//              Log.d("loadTask","job");
              vehicleDescriptor.AddToTaskQueue(new VehicleJob(VehicleJob.VehicleJobTypes.parse(ois.readInt()), buildingDescriptord));
              break;
            case VEHICLE_POS_ANIM:
              PositionAnimation posAnim = new PositionAnimation(
                      ois.readInt(),
                      ois.readFloat(),
                      ois.readFloat(),
                      ois.readFloat(),
                      ois.readFloat(),
                      ois.readFloat(),
                      ois.readFloat());
//              Log.d("loadTask","anim"+posAnim.PositionChange.x+"//"+posAnim.PositionChange.y+"//"+posAnim.PositionChange.z);
              vehicleDescriptor.AddToTaskQueue(posAnim);
              break;
          }
        }

        // all done
        return vehicleDescriptor;

      }
    }catch (IOException ioex){
      Log.e("loadVehicle","failed",ioex);
    }
    return null;
  }

  private byte[] readNBytes(InputStream in, int n)
  {
    byte[] result = new byte[n];
    int bytesRead = 0, count;
    try{
      while (bytesRead < n) {
        count = in.read(result, bytesRead, Math.min(n - bytesRead, 4096));
        if(count == -1) {
          throw new IOException("unexpected end of file");
        }
        bytesRead += count;
      }
    }catch(IOException ioex) {
      Log.d("readNBytes", "IO error:" +ioex.getMessage());
    }
    return result;
  }

  private void loadGame(final String filename)
  {
    try {
      final File file = new File(getApplicationContext().getFilesDir(), filename + ".dat");
      final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));

      byte[] bytes = new byte[4];
      ois.read(bytes);
      final String version = new String(bytes);
      switch(version) {
        case "1.00":
          Log.d("loadGame", "loading version 1.00");
          MyGameLogic.SetMinuteOfTheDay(ois.readFloat());

          final int num = ois.readShort();
          String planetName = "";
          for(int p=0; p<num; p++)
          {
            bytes = readNBytes(ois, ois.readShort());
            planetName = new String(bytes);

            
            final int width = ois.readShort();
            final int height = ois.readShort();
            final PlanetDescriptor pd = new PlanetDescriptor(planetName, width, height);
            final int fieldSize = width * height;
            final int[][] heightmap = new int[height+1][width+1];

            for(int h=0; h<=height; h++)
              for(int w=0; w<=width; w++)
                heightmap[h][w] = ois.readInt();
            pd.setHeightmap(heightmap);

            // read roadmap (streets)
            bytes = readNBytes(ois, width * height);
            pd.setRoadMap(new RoadMap(width, height, bytes));

            pd.setResource_RareEarth(ArrayHelpers.unflatten(width, height, readNBytes(ois, fieldSize)));
            pd.setResource_CoreIce(ArrayHelpers.unflatten(width, height, readNBytes(ois, fieldSize)));

            int buildingCount = ois.readShort();
//            Log.d("gonna load", buildingCount+" buildings");
            while(buildingCount-- >  0) {
              final String buildingKey = ois.readUTF();

              final BuildingDescriptor bd = MyGameLogic.getBuildingDescriptorByKey(buildingKey);
              bd.setPlanetDescriptor(pd);
              bd.setBuildingType(buildingKey);
              bd.setBuildingDirection(MyConstants.Direction.parse(ois.readInt()));
              bd.setBuildingPosition(new Vector3D(ois.readFloat(), ois.readFloat(), ois.readFloat()));
              bd.setPickingPoint(new Position2D(ois.readInt(), ois.readInt()));

              int gh = ois.readInt();
              int gw = ois.readInt();
              int[][] grid = new int[gh][gw];
              for(int h=0; h<gh; h++)
                for(int w=0; w<gw; w++)
                  grid[h][w] = ois.readInt();
              bd.setBuildingGrid(grid);

              short goodsCount = ois.readShort();
              while(goodsCount-- > 0) {
                bytes = readNBytes(ois, ois.readShort());
                bd.AddToProducedGoods(new String(bytes), ois.readFloat());
              }

              goodsCount = ois.readShort();
              while(goodsCount-- > 0) {
                bytes = readNBytes(ois, ois.readShort());
                bd.AddToProductionInputStorage(new String(bytes), ois.readFloat());
              }

              bd.LoadBuildingDefinition();

              bd.ReadyForTransportation = ois.readBoolean();
              bd.TransportationInProgress = ois.readBoolean();
              bd.ReadyForDeliverance = ois.readBoolean();
              bd.DeliveranceInProgress = ois.readBoolean();
              bd.setTransportVehicle(loadVehicle(ois, bd));
              bd.setDeliveryVehicle(loadVehicle(ois, bd));

              pd.addBuilding(bd);
            }


            MyGameLogic.addPlanetDescription(pd);
          }
          MyGameLogic.loadPlanet(planetName);

          break;
      }


      MyGameLogic.setIsPaused(false);
    }
    catch (IOException iox) {

    }
  }

  private void createNewGame() {
    // start new game at 09:00 am
    MyGameLogic.SetMinuteOfTheDay(540.0f);

    final int widthSegments = 200;
    final int heightSegments = 200;
    final String planetName = "Terra1";
    final PlanetDescriptor planetDescriptor = new PlanetDescriptor(planetName, widthSegments, heightSegments);

    planetDescriptor.setHeightmap(TerrainGen.GeneratePerlin(widthSegments + 1, heightSegments + 1));
    planetDescriptor.setResource_RareEarth(TerrainGen.GeneratePerlinByteMap(widthSegments, heightSegments));
    planetDescriptor.setResource_CoreIce(TerrainGen.GeneratePerlinByteMap(widthSegments, heightSegments));

    MyGameLogic.addPlanetDescription(planetDescriptor);
    MyGameLogic.loadPlanet(planetName);

    MyGameLogic.setIsPaused(false);
  }


  private void closeApplication()
  {
    Log.e("Screen lock", "OFF");

    // pause the gamelogic
    MyGameLogic.setIsPaused(true);

    saveGame("test");
    SavePrivateSettings();
    glSurfaceView.setKeepScreenOn(false);
    this.finish();

    Process.killProcess(Process.myPid());
  }

}
