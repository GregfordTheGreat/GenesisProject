package de.sereal.apps.genesisproject;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

import de.sereal.apps.genesisproject.util.Dimensions;

/**
 * Created by sereal on 08.08.2016.
 */
public class HudDialogTransport extends HudDialogStandard
{
  private Rect ConfirmButtonArea = new Rect();
  private Bitmap ConfirmButtonImage;

  public HudDialogTransport(GLHudOverlayView parent)
  {
    super(parent);
    ConfirmButtonImage = ParentView.LoadImageWithAspectRatio(R.drawable.ok, 128, 128);
    
    setDialogDimensions(500, 200, false);
    CenterHorizontal(ParentView.mMetrics.heightPixels - 200);
  }

  @Override
  public void drawContent(Canvas canvas)
  {
//
//		BuildingDef bDef = GameActivity.MyGameLogic.GameRules.GetBuildingDefinition(selectedBuilding);
//		String buildingName = bDef.getName();
      canvas.drawText("Transport", DialogMetrics.left+2, DialogMetrics.top+ParentView.textPaint.getTextSize(), ParentView.textPaint);
//		canvas.drawText("Rescource A:"+bDef.getCostsA(), DialogPosition.X+2, DialogPosition.Y+42, ParentView.textPaint);
//		canvas.drawText("Rescource B:"+bDef.getCostsB(), DialogPosition.X+2, DialogPosition.Y+62, ParentView.textPaint);

    canvas.drawBitmap(ConfirmButtonImage, ConfirmButtonArea.left, ConfirmButtonArea.top, GameActivity.MyGameLogic.ReadyToBuild ? ParentView.normalImagePaint : ParentView.grayScaleImagePaint);
  }


  @Override
  public boolean isButtonClicked(int x, int y)
  {
    if(ConfirmButtonArea.contains(x, y))
    {
      // TODO: check rescources first AKA money?
      if(GameActivity.MyGameLogic.ReadyToBuild)
      {
        GameActivity.MyGameLogic.AddTransport();
      }
      return true;
    }
    return false;
  }

  @Override
  public void onDimensionChanged()
  {
    super.onDimensionChanged();
    ConfirmButtonArea.left = DialogMetrics.right - 128;
    ConfirmButtonArea.top = DialogMetrics.bottom - 128;
    ConfirmButtonArea.right = DialogMetrics.right;
    ConfirmButtonArea.bottom = DialogMetrics.bottom;
  }

}
