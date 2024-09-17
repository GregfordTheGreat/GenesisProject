package de.sereal.apps.genesisproject;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import java.text.DecimalFormat;
import java.util.Vector;

import de.sereal.apps.genesisproject.rules.BuildingDef;
import de.sereal.apps.genesisproject.rules.MaterialValue;
import de.sereal.apps.genesisproject.rules.ResourceDef;
import de.sereal.apps.genesisproject.util.Dimensions;
import de.sereal.apps.genesisproject.util.MyConstants;
import java.util.List;

/**
 * Created by sereal on 08.08.2016.
 */
public class HudDialogBuilding extends HudDialogStandard
{
  private Rect ConfirmButtonArea = new Rect();
  private Bitmap ConfirmButtonImage;
  private String selectedBuildingDefKey;
  private DecimalFormat decimalFormat = new DecimalFormat("0.0");

  public HudDialogBuilding(GLHudOverlayView parent)
  {
    super(parent);
    ConfirmButtonImage = ParentView.LoadImageWithAspectRatio(R.drawable.ok, 128, 128);
    setDialogDimensions(600, 350, false);
    CenterHorizontal(ParentView.mMetrics.heightPixels - 350);
  }

  @Override
  public void onDimensionChanged() {
    super.onDimensionChanged();
    ConfirmButtonArea.left = DialogMetrics.right - ConfirmButtonImage.getWidth();
    ConfirmButtonArea.top = DialogMetrics.bottom - ConfirmButtonImage.getHeight();
    ConfirmButtonArea.right = DialogMetrics.right;
    ConfirmButtonArea.bottom = DialogMetrics.bottom;
  }

  @Override
  public boolean isButtonClicked(int x, int y)
  {
    if(ConfirmButtonArea.contains(x, y))
    {
      // TODO: check rescources first
      if(GameActivity.MyGameLogic.ReadyToBuild)
      {
        GameActivity.MyGameLogic.confirmBuild(selectedBuildingDefKey);
        ParentView.ResetHud();
      }
      return true;
    }
    return false;
  }

  @Override
  public void drawContent(Canvas canvas)
  {
    ParentView.textPaint.setTextAlign(Paint.Align.LEFT);
    
    BuildingDef bDef = GameActivity.MyGameLogic.GameRules.getBuildingDefinition(selectedBuildingDefKey);
    String buildingName = bDef.getName();
      canvas.drawText(buildingName, DialogMetrics.left+2, DialogMetrics.top+ParentView.FontSize, ParentView.textPaint);

    Bitmap icon;
    int yOffset = ParentView.FontSize;
    for(MaterialValue mv : bDef.GetConstructionCosts())
    {
      icon = ParentView.GetMaterialIcon(mv.Material);
      if(icon != null)
      {
          canvas.drawBitmap(icon, DialogMetrics.left+2, DialogMetrics.top+yOffset, ParentView.normalImagePaint);
          canvas.drawText(""+mv.Value, DialogMetrics.left+128, DialogMetrics.top+yOffset+(ParentView.IconSize - ParentView.FontSize), darkTextPaint);
        yOffset += ParentView.IconSize;
      }
    }


    for(MaterialValue mv : bDef.GetProductionValues())
    {
      float prod = mv.Value;
      prod *= GameActivity.MyGameLogic.GetMaterialAtLocation(mv.Material);
      icon = ParentView.GetMaterialIcon(mv.Material);
      if(icon != null)
      {
          canvas.drawBitmap(icon, DialogMetrics.left+2, DialogMetrics.top+yOffset, ParentView.normalImagePaint);
          canvas.drawText(decimalFormat.format(prod), DialogMetrics.left+128, DialogMetrics.top+yOffset+(ParentView.IconSize - ParentView.FontSize), darkTextPaint);
        yOffset += ParentView.IconSize;
      }
    }

    // produced or required resources
    ResourceDef rDef;
    for(MaterialValue mv : bDef.getResourceCosts()) {
      rDef = GameActivity.MyGameLogic.GameRules.GetResourceDefinition(mv.Material);
      float prod = mv.Value;
      icon = ParentView.GetIconByName(rDef.Icon, ParentView.IconSize, ParentView.IconSize);
      if(icon != null) {
          canvas.drawBitmap(icon, DialogMetrics.left+2, DialogMetrics.top+yOffset, ParentView.normalImagePaint);
          canvas.drawText(decimalFormat.format(prod), DialogMetrics.left+128, DialogMetrics.top+yOffset+(ParentView.IconSize - ParentView.FontSize), darkTextPaint);
          yOffset += ParentView.IconSize;
      }
    }
    
    for(MaterialValue mv : bDef.getResourceProductions()) {
        rDef = GameActivity.MyGameLogic.GameRules.GetResourceDefinition(mv.Material);
        float prod = mv.Value;
        icon = ParentView.GetIconByName(rDef.Icon, ParentView.IconSize, ParentView.IconSize);
        if(icon != null) {
            canvas.drawBitmap(icon, DialogMetrics.left+2, DialogMetrics.top+yOffset, ParentView.normalImagePaint);
            canvas.drawText(decimalFormat.format(prod), DialogMetrics.left+128, DialogMetrics.top+yOffset+(ParentView.IconSize - ParentView.FontSize), darkTextPaint);
            yOffset += ParentView.IconSize;
        }
    }

    canvas.drawBitmap(ConfirmButtonImage, ConfirmButtonArea.left, ConfirmButtonArea.top, GameActivity.MyGameLogic.ReadyToBuild ? ParentView.normalImagePaint : ParentView.grayScaleImagePaint);
  }

  public void setSelectedBuildingKey(final String buildingKey)
  {
    selectedBuildingDefKey = buildingKey;
    BuildingDef def = GameActivity.MyGameLogic.GameRules.getBuildingDefinition(selectedBuildingDefKey);
    Vector<MaterialValue> resources = def.GetProductionValues();
    if(resources.size() > 0)
    {
      switch(resources.firstElement().Material)
      {
        case "MATERIAL_RARE_EARTH":
          GameActivity.MyGameLogic.SetResourceMap(MyConstants.ResourceMapStyle.RARE_EARTH);
          break;

        case "MATERIAL_CORE_ICE":
          GameActivity.MyGameLogic.SetResourceMap(MyConstants.ResourceMapStyle.CORE_ICE);
          break;

      }
    }

    GameActivity.MyGameLogic.SetSelectionGrid(def.getGrid());
  }


}
