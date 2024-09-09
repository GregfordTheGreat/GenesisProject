package de.sereal.apps.genesisproject;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import de.sereal.apps.genesisproject.util.Dimensions;

/**
 * Created by sereal on 08.08.2016.
 */
public class HudDialogLandscaping extends HudDialogStandard
{
  private Rect ButtonUpArea = new Rect();
  private Rect ButtonDownArea = new Rect();
  private Bitmap ButtonImageUp;
  private Bitmap ButtonImageDown;

  public HudDialogLandscaping(GLHudOverlayView parent)
  {
    super(parent);
    ButtonImageUp = ParentView.LoadImageWithAspectRatio(R.drawable.arrow_up, 128, 128);
    ButtonImageDown = ParentView.LoadImageWithAspectRatio(R.drawable.arrow_down, 128, 128);
    
    setDialogDimensions(500, 200, false);
    CenterHorizontal(ParentView.mMetrics.heightPixels - 200);
  }

  @Override
  public void drawContent(Canvas canvas)
  {
    canvas.drawBitmap(ButtonImageUp, ButtonUpArea.left, ButtonUpArea.top, ParentView.normalImagePaint);
    canvas.drawBitmap(ButtonImageDown, ButtonDownArea.left, ButtonDownArea.top, ParentView.normalImagePaint);
  }


  @Override
  public boolean IsButtonClicked(int x, int y)
  {
    if(ButtonUpArea.contains(x, y))
    {
      GameActivity.MyGameLogic.LandscapeRaise();
      return true;
    }else
    if(ButtonDownArea.contains(x, y))
    {
      GameActivity.MyGameLogic.LandscapeLower();
      return true;
    }
    return false;
  }

  @Override
  public void onDimensionChanged()
  {
      super.onDimensionChanged();
    int x = DialogMetrics.left + 10;
    int y = DialogMetrics.top + 10;
    ButtonUpArea = new Rect(x, y, x+128, y+128);
    x += 138;
    ButtonDownArea = new Rect(x, y, x+128, y+128);
  }

}
