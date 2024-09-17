package de.sereal.apps.genesisproject;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.Log;
import java.util.Vector;

import de.sereal.apps.genesisproject.util.Dimensions;

/**
 * Created by sereal on 02.09.2016.
 */
public class HudDialogBuildOptions extends HudDialog
{
  private Paint bottomBarPaint;
  private Paint backgroundPaint;

  private int bottomOffset = 10;
  private int bottomBarHeight = 0;
  private int backgroundHeight = 0;

  private Rect Container, Bar;
  private Vector<Rect> BuildingOptionMetrics = new Vector<>();
  private Vector<BuildOption> BuildingOptions = new Vector<>();
  private boolean DragEnabled = false;
  private int mousePointerY = 0;
  private Bitmap CloseButton;
  private Rect CloseButtonArea;

  private int maxBuildingOptionWidth = 0;
  private int BuildingOptionScrollWidth, BuildingOptionScroll=0;
  private int initialBuildingChoice = -1;

  public HudDialogBuildOptions(GLHudOverlayView parent, Vector<BuildOption> buildOptions)
  {
    super(parent);

    int width = ParentView.mMetrics.widthPixels;
    int height = ParentView.mMetrics.heightPixels;

    bottomBarHeight = height / 10;
    backgroundHeight = 3 * bottomBarHeight;
    
    Container = new Rect(0, 0, width, backgroundHeight);
    Bar = new Rect(0, backgroundHeight - bottomBarHeight, width, backgroundHeight);

    maxBuildingOptionWidth = 0;
    for(BuildOption bo : buildOptions)
    {
      BuildingOptions.addElement(bo);
      BuildingOptionMetrics.addElement(new Rect(maxBuildingOptionWidth, 0, maxBuildingOptionWidth + backgroundHeight-1, backgroundHeight-1));
      maxBuildingOptionWidth += backgroundHeight;
    }
    BuildingOptionScrollWidth = Math.max(0, maxBuildingOptionWidth - width);

    CloseButton = ParentView.LoadImageWithAspectRatio(R.drawable.close_dialog, 128, 128);
    CloseButtonArea = new Rect(width-CloseButton.getWidth() - bottomOffset, Bar.centerY()-CloseButton.getHeight()/2, width-bottomOffset, Bar.centerY()+CloseButton.getHeight()/2);


    bottomBarPaint = new Paint();
    bottomBarPaint.setShader(new LinearGradient(0, 0, 0, bottomBarHeight, Color.BLACK, Color.DKGRAY, Shader.TileMode.MIRROR));
    backgroundPaint = new Paint();
    backgroundPaint.setARGB(128,0,0,0);

    setDialogDimensions(width, backgroundHeight, false);
    CenterHorizontal(height - backgroundHeight - bottomOffset);
  }


  public boolean IsClicked(int x, int y)
  {
    if(DialogMetrics.contains(x,y))
    {
      DragEnabled = true;
      mousePointerY = y;

      initialBuildingChoice = -1;
      for(int a=0; a<BuildingOptionMetrics.size(); a++)
      {
        if(BuildingOptionMetrics.get(a).contains(x,y))
          initialBuildingChoice = a;
      }

      if(isButtonClicked(x, y))
      {
        return true;
      }else{
        return DialogMetrics.contains(x, y);
      }
    }
    return false;
  }

  public boolean OnDrag(int dx, int dy)
  {
    if(DragEnabled)
    {
      mousePointerY += dy;
      if(mousePointerY < Container.top)
      {
        Log.d("dragged","onto");

        if(initialBuildingChoice >= 0)
        {
          ParentView.showHudForConstruction(BuildingOptions.get(initialBuildingChoice).buildingKey);
          this.Hide();
          return true;
        }
      }

      int scrollX = dx;
      if(dx < 0 && Math.abs(BuildingOptionScroll + dx) > BuildingOptionScrollWidth)
        scrollX = BuildingOptionScrollWidth+BuildingOptionScroll;
      if(dx > 0 && BuildingOptionScroll + dx > 0)
        scrollX = BuildingOptionScroll;
      BuildingOptionScroll += scrollX;

      for(Rect r : BuildingOptionMetrics)
      {
        r.offset(scrollX, 0);
      }
      ParentView.invalidate(Container);
      return true;
    }
    return false;
  }

  public void OnRelease()
  {
    DragEnabled = false;
  }

  @Override
  public boolean isButtonClicked(int x, int y)
  {
    if(CloseButtonArea.contains(x,y))
    {
      this.Hide();
      ParentView.HideBuildingOption(true);
      return true;
    }
    return false;
  }

  @Override
  public void onDimensionChanged()
  {
    Container.offset(DialogMetrics.left, DialogMetrics.top);
      Bar.offset(DialogMetrics.left, DialogMetrics.top);
      CloseButtonArea.offset(DialogMetrics.left, DialogMetrics.top);
    for(Rect r : BuildingOptionMetrics)
    {
        r.offset(DialogMetrics.left, DialogMetrics.top);
    }
  }

  @Override
  public void drawDetails(Canvas canvas)
  {
    canvas.drawRect(Container, backgroundPaint);
    canvas.drawRect(Bar, bottomBarPaint);

    ParentView.shadowedTextPaint.setTextAlign(Paint.Align.CENTER);
    Rect r;
    BuildOption bo;
    for(int a=0; a<BuildingOptions.size(); a++)
    {
      r = BuildingOptionMetrics.get(a);
      if(r.left > DialogMetrics.width())
      {
        continue;
      }
      bo = BuildingOptions.get(a);

//      canvas.drawRect(r, ParentView.menuPaint);
      canvas.drawBitmap(bo.Picture, r.centerX() - bo.Picture.getWidth()/2, r.top, ParentView.normalImagePaint);
      canvas.drawText(bo.Name,r.centerX(), r.bottom-ParentView.FontSize, ParentView.shadowedTextPaint);
    }

    canvas.drawBitmap(CloseButton, CloseButtonArea.left, CloseButtonArea.top, ParentView.normalImagePaint);
  }
}
