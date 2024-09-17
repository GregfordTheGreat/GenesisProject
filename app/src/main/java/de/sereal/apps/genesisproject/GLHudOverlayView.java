package de.sereal.apps.genesisproject;

import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import de.sereal.apps.genesisproject.rules.*;
import de.sereal.apps.genesisproject.util.*;
import java.util.*;

/**
 * Created by sereal on 05.08.2016.
 */
public class GLHudOverlayView extends ImageView
{
  final static int INACTIVE = -1;


  public Paint normalImagePaint;
  public Paint grayScaleImagePaint;
  public Paint textPaint = new Paint();
  public Paint textPaintStroke = new Paint();
  public Paint bigTextPaint;
  public Paint bigTextPaintStroke;
  public Paint menuPaint = new Paint();
  public Paint menuBorderPaint = new Paint();
  public Paint progressBackgroundPaint = new Paint();
  public Paint progressStrokePaint = new Paint();
  public Paint shadowedTextPaint = new Paint();
  public Paint modalBackgroundPaint = new Paint();
  
  private Context mContext;
  public DisplayMetrics mMetrics;

  private Rect[] gameMenuRanges = new Rect[10];
  private Bitmap[] gameMenuIcons = new Bitmap[gameMenuRanges.length];
  private int activeGameMenu = INACTIVE;

  private Rect[] gameAltMenuRanges = new Rect[10];
  private Bitmap[] gameAltMenuIcons = new Bitmap[gameAltMenuRanges.length];
  private int activeAltGameMenu = INACTIVE;

  private RectF[] materialValRanges = new RectF[2];
  public HashMap<String, Bitmap> materialIcons = new HashMap<>();

  private RectF[] resourceValRanges = new RectF[2];
  public HashMap<String, Bitmap> resourceIcons = new HashMap<>();
  
  private RectF timeOfDayRange = new RectF();

  private HudDialog mHudDialog;
  private HudDialogBuildOptions mHudBuildingChoiceDialog;

  public int IconSize = 0;
  public int HalfIconSize = 0;
  public int BorderRadius = 0;
  private int LineHeight = 0;
  public int HalfLineHeight = 0;
  private int TextOffsetY = 0;
  private int IconOffsetY = 0;
  public float RoundedCornerIcon=40;
  public int FontSize = 30;
  public int BigFontSize = 50;
  int oldX, oldY, newX, newY;

  public GLHudOverlayView(Context context)
  {
    super(context);
    mContext = context;
    mMetrics = context.getResources().getDisplayMetrics();
    int width = mMetrics.widthPixels;

    normalImagePaint = new Paint();
    normalImagePaint.setAntiAlias(true);
    normalImagePaint.setFilterBitmap(true);
    normalImagePaint.setDither(true);

    grayScaleImagePaint= new Paint();
    grayScaleImagePaint.setAntiAlias(true);
    grayScaleImagePaint.setFilterBitmap(true);
    grayScaleImagePaint.setDither(true);

    shadowedTextPaint.setTextSize(FontSize);
    shadowedTextPaint.setColor(Color.WHITE);
    shadowedTextPaint.setAntiAlias(true);
    shadowedTextPaint.setShadowLayer(3.0f, 3.0f, 3.0f, Color.DKGRAY);

    menuPaint.setARGB(128, 0, 0, 0);
    menuBorderPaint.setARGB(255, 0, 0, 0);
    menuBorderPaint.setStyle(Paint.Style.STROKE);
    menuBorderPaint.setStrokeWidth(1.0f);

    progressBackgroundPaint.setARGB(255, 100, 100, 100);
    progressBackgroundPaint.setStyle(Paint.Style.STROKE);
    progressBackgroundPaint.setStrokeWidth(30.0f);

    progressStrokePaint.setARGB(128,255,255,0);
    progressStrokePaint.setStyle(Paint.Style.STROKE);
    progressStrokePaint.setStrokeWidth(30.0f);


    textPaint.setARGB(255, 255, 255, 255);
    textPaint.setTextSize(FontSize);
    textPaintStroke.setARGB(255, 0, 0, 0);
    textPaintStroke.setTextSize(FontSize);
    textPaintStroke.setStyle(Paint.Style.STROKE);
    textPaintStroke.setStrokeWidth(10);
    bigTextPaintStroke = new Paint(textPaintStroke);
    bigTextPaint = new Paint(textPaint);
    bigTextPaintStroke.setTextSize(BigFontSize);
    bigTextPaint.setTextSize(BigFontSize);
    
    modalBackgroundPaint.setARGB(255, 247, 223, 186);
    
    GameActivity.MyGameLogic.AttachHud(this);

    ColorMatrix cm = new ColorMatrix();
    cm.setSaturation(0);
    ColorMatrixColorFilter GrayScaleFilter = new ColorMatrixColorFilter(cm);
    grayScaleImagePaint.setColorFilter(GrayScaleFilter);

    // gamemenu
    {
      IconSize = convertDpToPixel(32, context);
      LineHeight = convertDpToPixel(24, context);
      HalfIconSize = IconSize / 2;
      BorderRadius = IconSize / 4;

      int topOffset = 160;
      int topIndent = 10;

      //while(IconSize * 2 < maxMenuWidth) IconSize *= 2;
      RoundedCornerIcon = (float)IconSize / 10.0f;
      HalfLineHeight = (int)( LineHeight / 2.0f);
      TextOffsetY = (int)((LineHeight + FontSize) / 2.0f);
      IconOffsetY = (int)((IconSize - LineHeight) / 2.0f);

      int x = 10;
      int y = topOffset;
      for(int a=0; a<gameMenuRanges.length; a++)
      {
        gameMenuRanges[a] = new Rect(x,y, x+IconSize, y+IconSize);
        gameAltMenuRanges[a] = new Rect(width - x - IconSize, y, width - x, y + IconSize);
        y += IconSize + topIndent;
      }

      gameMenuIcons[0] = LoadImageWithAspectRatio(R.drawable.menu_road, IconSize, IconSize);
      gameMenuIcons[1] = LoadImageWithAspectRatio(R.drawable.menu_power, IconSize, IconSize);
      gameMenuIcons[2] = LoadImageWithAspectRatio(R.drawable.menu_production, IconSize, IconSize);
      gameMenuIcons[3] = LoadImageWithAspectRatio(R.drawable.menu_manufacturing, IconSize, IconSize);
      gameMenuIcons[4] = LoadImageWithAspectRatio(R.drawable.menu_transport, IconSize, IconSize);

      gameAltMenuIcons[0] = LoadImageWithAspectRatio(R.drawable.geo, IconSize, IconSize);

      gameAltMenuIcons[1] = LoadImageWithAspectRatio(R.drawable.geo, IconSize, IconSize);

      
      y = 10;
      shadowedTextPaint.setTextAlign(Paint.Align.LEFT);
      for(int a = 0; a< materialValRanges.length; a++)
      {
        materialValRanges[a] = new RectF(x, y, x + 300, y + LineHeight);
        x += 300 + 10f;
      }
      for(int a = 0; a< resourceValRanges.length; a++)
      {
        resourceValRanges[a] = new RectF(x, y, x + 300, y + LineHeight);
        x += 300 + 10f;
      }

      Resources resources = getResources();
      materialIcons.put(resources.getResourceEntryName(R.drawable.material_rare_earth), LoadImageWithAspectRatio(R.drawable.material_rare_earth, IconSize, IconSize));
      materialIcons.put(resources.getResourceEntryName(R.drawable.material_core_ice), LoadImageWithAspectRatio(R.drawable.material_core_ice, IconSize, IconSize));
      materialIcons.put(resources.getResourceEntryName(R.drawable.material_tit_alloy), LoadImageWithAspectRatio(R.drawable.material_tit_alloy, IconSize, IconSize));
      resourceIcons.put(resources.getResourceEntryName(R.drawable.resource_power), LoadImageWithAspectRatio(R.drawable.resource_power, IconSize, IconSize));
      
      timeOfDayRange = new RectF(width-300, y, width-10, y+LineHeight);
      
    }
  }
  
  public static int convertDpToPixel(int dp, Context context){
    
      return (int)(dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));

  }
  
  public void refreshCritical()
  {
      this.invalidate((int)timeOfDayRange.left, (int)timeOfDayRange.top, (int)timeOfDayRange.right, (int)timeOfDayRange.bottom);
  }

  public Bitmap LoadImageWithAspectRatio(int id, int maxWidth, int maxHeight)
  {
    Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), id);

    int width = bmp.getWidth();
    int height = bmp.getHeight();
    float ratioBitmap = (float) width / (float) height;
    float ratioMax = (float) maxWidth / (float) maxHeight;

    int finalWidth = maxWidth;
    int finalHeight = maxHeight;
    if (ratioMax > 1)
    {
      finalWidth = (int) ((float)maxHeight * ratioBitmap);
    } else {
      finalHeight = (int) ((float)maxWidth / ratioBitmap);
    }
    return Bitmap.createScaledBitmap(bmp, finalWidth, finalHeight, true);
  }

  public void Refresh()
  {
    if(mHudDialog != null)
    {
      ((GameActivity)mContext).runOnUiThread(new Runnable() {
        @Override
        public void run()
        {
          invalidate();
        }
      });
    }
  }

  @Override
  protected void onDraw(Canvas canvas)
  {
    super.onDraw(canvas);
    
    
    int a;
    for(a=0; a<gameMenuIcons.length; a++)
    {
      if(gameMenuIcons[a] == null) break;
      canvas.drawBitmap(gameMenuIcons[a], gameMenuRanges[a].left, gameMenuRanges[a].top, activeGameMenu == a ? normalImagePaint : grayScaleImagePaint);
    }
    for(a=0; a<gameAltMenuIcons.length; a++)
    {
      if(gameAltMenuIcons[a] == null) break;
      canvas.drawBitmap(gameAltMenuIcons[a], gameAltMenuRanges[a].left, gameAltMenuRanges[a].top, activeAltGameMenu == a ? normalImagePaint : grayScaleImagePaint);
    }

    
    canvas.drawRoundRect(timeOfDayRange, HalfLineHeight, HalfLineHeight, menuPaint);
    canvas.drawText(GameActivity.MyGameLogic.getFormattedDateTime(), timeOfDayRange.left + IconSize, timeOfDayRange.top + TextOffsetY, shadowedTextPaint);

    Vector<MaterialValue> visibleValues = GameActivity.MyGameLogic.GetVisibleMaterialValues();
    a = 0;
    shadowedTextPaint.setTextAlign(Paint.Align.LEFT);
    for(MaterialValue mv : visibleValues)
    {
        canvas.drawRoundRect(materialValRanges[a], HalfLineHeight, HalfLineHeight, menuPaint);
        canvas.drawBitmap(GetMaterialIcon(mv.Material), materialValRanges[a].left, materialValRanges[a].top - IconOffsetY, normalImagePaint);
        drawStrokedTextAt(((int)mv.Value)+"", canvas, materialValRanges[a].left + IconSize, materialValRanges[a].top + TextOffsetY);
        //canvas.drawText(((int)mv.Value)+"", materialValRanges[a].left + IconSize, materialValRanges[a].top + TextOffsetY, shadowedTextPaint);
        a++;
    }

    visibleValues = GameActivity.MyGameLogic.GetAvailableResources();
    ResourceDef rDef;
    a=0;
    for(MaterialValue mv : visibleValues)
    {
      rDef = GameActivity.MyGameLogic.GameRules.GetResourceDefinition(mv.Material);
      canvas.drawRoundRect(resourceValRanges[a], HalfLineHeight, HalfLineHeight, menuPaint);
      canvas.drawBitmap(resourceIcons.get(rDef.Icon), resourceValRanges[a].left, resourceValRanges[a].top - IconOffsetY, normalImagePaint);
      canvas.drawText(((int)mv.Value)+"", resourceValRanges[a].left + IconSize, resourceValRanges[a].top + TextOffsetY, shadowedTextPaint);
      a++;
    }


    if(mHudDialog != null)
    {
      mHudDialog.Draw(canvas);
    }
    if(mHudBuildingChoiceDialog != null)
    {
      mHudBuildingChoiceDialog.Draw(canvas);
    }
  }

  public Bitmap GetMaterialIcon(String key)
  {
    MaterialDef mDef = GameActivity.MyGameLogic.GameRules.GetMaterialDefinition(key);

    if(mDef == null)
      return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

    if(materialIcons.containsKey(mDef.Icon))
      return materialIcons.get(mDef.Icon);

    return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
  }

  public Bitmap GetIconByName(String key, int width, int height)
  {
    //Log.d("GetIconByName",key+"");
    int id = getResources().getIdentifier(key, "drawable", mContext.getPackageName());
    if(id==0) return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
    return LoadImageWithAspectRatio(id, width, height);
  }

  public void PlaySoundEffect(int resourceID)
  {
    ((GameActivity)mContext).PlaySoundEffect(resourceID);
  }


  public void ShowDetailsForSelectedBuilding()
  {
    ResetHud();
    mHudDialog = new HudDialogBuildingDetails(this);
    mHudDialog.Show();

    ((GameActivity)mContext).runOnUiThread(new Runnable() {
      @Override
      public void run()
      {
        invalidate();
      }
    });
  }

  public void ResetHud()
  {
    GameActivity.MyGameLogic.SetMouseMoveAction(GameLogic.MOUSE_MOVE);
    GameActivity.MyGameLogic.SetSelectionGrid( null );
    GameActivity.MyGameLogic.SetShowConnectionsPoints(false);
    GameActivity.MyGameLogic.SetPickType(1);
    GameActivity.MyGameLogic.SetResourceMap(MyConstants.ResourceMapStyle.NORMAL);
    activeGameMenu = INACTIVE;


    if(mHudDialog != null)
      mHudDialog.Hide();
    mHudDialog = null;

    ((GameActivity)mContext).runOnUiThread(new Runnable() {
      @Override
      public void run()
      {
        invalidate();
      }
    });
  }

  @Override
  public boolean onTouchEvent(MotionEvent event)
  {
    if(event != null)
    {
      oldX = newX;
      oldY = newY;
      newX = (int)event.getX();
      newY = (int)event.getY();
      Vector<BuildOption> buildOptions;
      BuildOption bo;

        switch(event.getActionMasked())
        {
          case MotionEvent.ACTION_DOWN:
            int a;
            for(a=0; a<gameMenuIcons.length; a++)
            {
              if(gameMenuIcons[a] == null) break;
              if(gameMenuRanges[a].contains(newX,newY))
              {
                PlaySoundEffect(R.raw.menu_click);
                activeGameMenu = (activeGameMenu != a) ? a : INACTIVE;
                GameActivity.MyGameLogic.SetResourceMap(MyConstants.ResourceMapStyle.NORMAL);

                if(activeGameMenu!=INACTIVE)
                  GameActivity.MyGameLogic.SetPickType(0);

                switch(activeGameMenu)
                {
                  case 0:
                    GameActivity.MyGameLogic.SetMouseMoveAction(GameLogic.MOUSE_DRAG);
                    GameActivity.MyGameLogic.SetSelectionGrid( new int[][]{{1}} );
                    GameActivity.MyGameLogic.SetShowConnectionsPoints(true);

                    mHudDialog = new HudDialogTransport(this);
                    mHudDialog.Show();
                    break;

                  case 1:
                    ResetHud();
                    buildOptions = new Vector<>();
                    bo = new BuildOption();
                    bo.Name = "Solar Panels";
                    bo.buildingKey = "SOLAR_PANELS";
                    bo.Picture = LoadImageWithAspectRatio(R.drawable.building_solar_panels, 356, 356);
                    buildOptions.addElement(bo);
                    bo = new BuildOption();
                    bo.Name = "Fusion Plant";
                    bo.buildingKey = "FUSION_PLANT";
                    bo.Picture = LoadImageWithAspectRatio(R.drawable.building_fusion_plant, 356, 356);
                    buildOptions.addElement(bo);
                    mHudBuildingChoiceDialog = new HudDialogBuildOptions(this, buildOptions);
                    mHudBuildingChoiceDialog.Show();
                    break;

                  case 2:
                    ResetHud();
                    buildOptions = new Vector<>();

                    bo = new BuildOption();
                    bo.Name = "KREEP Mine";
                    bo.buildingKey = "RE_MINE";
                    bo.Picture = LoadImageWithAspectRatio(R.drawable.building_re_mine, 356, 356);
                    buildOptions.addElement(bo);

                    bo = new BuildOption();
                    bo.Name = "Core Ice Mine";
                    bo.buildingKey = "MINE_CORE_ICE";
                    bo.Picture = LoadImageWithAspectRatio(R.drawable.building_mine_coreice, 356, 356);
                    buildOptions.addElement(bo);

                    bo = new BuildOption();
                    bo.Name = "Helium3-Mine";
                    bo.buildingKey = "HE3_MINE";
                    bo.Picture = LoadImageWithAspectRatio(R.drawable.building_he3, 356, 356);
                    buildOptions.addElement(bo);

                    mHudBuildingChoiceDialog = new HudDialogBuildOptions(this, buildOptions);
                    mHudBuildingChoiceDialog.Show();

//                    GameActivity.MyGameLogic.SetMouseMoveAction(GameLogic.MOUSE_DRAG_INTO_DIRECTION);
//                    GameActivity.MyGameLogic.SetShowConnectionsPoints(false);
//                    GameActivity.MyGameLogic.SetResourceMap(MyConstants.ResourceMapStyle.CORE_ICE);
//
//                    mHudDialog = new HudDialogBuilding(this);
//                    ((HudDialogBuilding)mHudDialog).SetSelectedBuildingType(1);
//                    mHudDialog.CenterHorizontal(mMetrics.heightPixels - mHudDialog.DialogSize.Height);
//                    mHudDialog.Show();
                    break;

                  case 3:
                    ResetHud();
                    buildOptions = new Vector<>();

                    bo = new BuildOption();
                    bo.Name = "Melter";
                    bo.buildingKey = "MELTER";
                    bo.Picture = LoadImageWithAspectRatio(R.drawable.building_melter, 356, 356);
                    buildOptions.addElement(bo);

                    bo = new BuildOption();
                    bo.Name = "Oxygen Lab";
                    bo.buildingKey = "PROD_OXYGEN";
                    bo.Picture = LoadImageWithAspectRatio(R.drawable.building_prod_oxygen, 356, 356);
                    buildOptions.addElement(bo);

                    mHudBuildingChoiceDialog = new HudDialogBuildOptions(this, buildOptions);
                    mHudBuildingChoiceDialog.Show();
                    break;

                  case 4:
                    ResetHud();
                    buildOptions = new Vector<>();

                    bo = new BuildOption();
                    bo.Name = "TransportStation";
                    bo.buildingKey = "STATION";
                    bo.Picture = LoadImageWithAspectRatio(R.drawable.building_station, 356, 356);
                    buildOptions.addElement(bo);

                    bo = new BuildOption();
                    bo.Name = "Science center";
                    bo.buildingKey = "SCIENCE_CENTER";
                    bo.Picture = LoadImageWithAspectRatio(R.drawable.building_science_center, 356, 356);
                    buildOptions.addElement(bo);
                    
                    bo = new BuildOption();
                    bo.Name = "Center";
                    bo.Picture = LoadImageWithAspectRatio(R.drawable.cube, 356, 356);
                    bo.buildingKey = "CENTER";
                    buildOptions.add(bo);

                    mHudBuildingChoiceDialog = new HudDialogBuildOptions(this, buildOptions);
                    mHudBuildingChoiceDialog.Show();

//                    GameActivity.MyGameLogic.SetMouseMoveAction(GameLogic.MOUSE_DRAG_INTO_DIRECTION);
//                    GameActivity.MyGameLogic.SetShowConnectionsPoints(false);
//                    GameActivity.MyGameLogic.SetResourceMap(MyConstants.ResourceMapStyle.RARE_EARTH);
//
//                    mHudDialog = new HudDialogBuilding(this);
//                    ((HudDialogBuilding)mHudDialog).SetSelectedBuildingType(2);
//                    mHudDialog.CenterHorizontal(mMetrics.heightPixels - mHudDialog.DialogSize.Height);
//                    mHudDialog.Show();
                    break;

                  case INACTIVE:
                  default:
                    ResetHud();
                    break;
                }

                invalidate();
                return true;
              }
            }


            // right game menu (terrain and stuff)
            for(a=0; a<gameAltMenuIcons.length; a++)
            {
              if (gameAltMenuIcons[a] == null) break;
              if (gameAltMenuRanges[a].contains(newX, newY))
              {
                activeAltGameMenu = (activeAltGameMenu != a) ? a : INACTIVE;
                activeGameMenu = INACTIVE;
                GameActivity.MyGameLogic.SetResourceMap(MyConstants.ResourceMapStyle.NORMAL);

                if(activeAltGameMenu != INACTIVE)
                  GameActivity.MyGameLogic.SetPickType(0);

                switch(activeAltGameMenu)
                {
                  case 0:
                    GameActivity.MyGameLogic.SetMouseMoveAction(GameLogic.MOUSE_DRAG_FOR_PLANE);
                    GameActivity.MyGameLogic.SetSelectionGrid( new int[][]{{1}} );
                    GameActivity.MyGameLogic.SetShowConnectionsPoints(false);

                    mHudDialog = new HudDialogLandscaping(this);
                    mHudDialog.Show();
                    break;
                    
                  case 1:
                      mHudDialog = new HudDialogResearch(this);
                      mHudDialog.Show();
                      break;

                  case INACTIVE:
                  default:
                    ResetHud();
                    break;
                }

                invalidate();
                return true;
              }
            }


            if(mHudDialog != null && mHudDialog.IsClicked(newX, newY))
            {
              return true;
            }
            if(mHudBuildingChoiceDialog != null && mHudBuildingChoiceDialog.IsClicked(newX, newY))
            {
              return true;
            }
            break;

          case MotionEvent.ACTION_MOVE:
            if(mHudBuildingChoiceDialog != null && mHudBuildingChoiceDialog.OnDrag(newX - oldX, newY - oldY))
            {
              return true;
            }
            break;

          case MotionEvent.ACTION_UP:
          case MotionEvent.ACTION_CANCEL:
            if(mHudBuildingChoiceDialog != null)
            {
              mHudBuildingChoiceDialog.OnRelease();
            }
            break;
        }
    }

    return false;
  }

  public void HideBuildingOption(boolean reset)
  {
    if(mHudBuildingChoiceDialog!=null)
      mHudBuildingChoiceDialog = null;

    if(reset)
    {
      activeGameMenu = INACTIVE;
      ResetHud();
      invalidate();
    }
  }

  public void showHudForConstruction(final String buildingKey)
  {
    HideBuildingOption(false);

    GameActivity.MyGameLogic.SetMouseMoveAction(GameLogic.MOUSE_DRAG_INTO_DIRECTION);
    GameActivity.MyGameLogic.SetShowConnectionsPoints(false);

    mHudDialog = new HudDialogBuilding(this);
    ((HudDialogBuilding)mHudDialog).setSelectedBuildingKey(buildingKey);
    mHudDialog.Show();

    invalidate();
  }
  
  
  public void drawStrokedTextAt(final String text, final RectF rect, final Canvas canvas, final TextAnchor anchor) {
      drawText(text, rect, canvas, textPaint, textPaintStroke, anchor);
  }
  
  public void drawStrokedTextAt(final String text, final Canvas canvas, final float x, final float y) {
      canvas.drawText(text, x, y, textPaintStroke);
      canvas.drawText(text, x, y, textPaint);
  }
  
  private void drawText(final String text, final RectF rect, final Canvas canvas, final Paint paint, final Paint strokePaint, final TextAnchor anchor) {
      float x;
      float y;
      if(anchor == TextAnchor.BOTTOM_LEFT) {
          x = rect.left;
          y = rect.bottom;
      } else {
          final Rect centerRect = new Rect();
          paint.getTextBounds(text, 0, text.length(), centerRect);
          
          switch(anchor) {
              default:
              case MIDDLE_LEFT:
              case TOP_LEFT:
                  x = rect.left;
                  break;
                  
              case BOTTOM_CENTER:
              case MIDDLE_CENTER:
              case TOP_CENTER:
                  x = rect.left + ((rect.width() - centerRect.width()) / 2f);
                  break;
              
              case BOTTOM_RIGHT:
              case MIDDLE_RIGHT:
              case TOP_RIGHT:
                  x = rect.right - centerRect.width();
                  break;
          }
          
          switch(anchor) {
              default:
              case BOTTOM_RIGHT:
              case BOTTOM_CENTER :
                  y = rect.bottom;
                  break;
                  
              case MIDDLE_LEFT :
              case MIDDLE_CENTER:   
              case MIDDLE_RIGHT:
                  y = rect.bottom - ((rect.height() - centerRect.height()) / 2f);
                  break;
                  
              case TOP_LEFT:
              case TOP_CENTER:
              case TOP_RIGHT:
                  y = rect.top - centerRect.height();
                  break;
           }
      }
      
      canvas.drawText(text, x, y, strokePaint);
      canvas.drawText(text, x, y, paint);
  }
  
  public void drawCenteredAt(final String text, final RectF rect, final Canvas canvas) {
      drawText(text, rect, canvas, bigTextPaint, bigTextPaintStroke, TextAnchor.MIDDLE_CENTER);
  }

  public static Path ComposeRoundedRectPath(RectF rect, float radius){
    Path path = new Path();
    radius = radius < 0 ? 0 : radius;


    path.moveTo((rect.left + rect.right)/2 ,rect.top);
    path.lineTo(rect.right - radius,rect.top);
    path.quadTo(rect.right, rect.top, rect.right, rect.top + radius);
    path.lineTo(rect.right ,rect.bottom - radius);
    path.quadTo(rect.right ,rect.bottom, rect.right - radius/2, rect.bottom);
    path.lineTo(rect.left +  radius,rect.bottom);
    path.quadTo(rect.left,rect.bottom,rect.left, rect.bottom - radius);
    path.lineTo(rect.left,rect.top  + radius);
    path.quadTo(rect.left, rect.top, rect.left + radius, rect.top);
    path.lineTo((rect.left  + rect.right)/2, rect.top);
    path.close();          
                                           
    return path;
  }
}
