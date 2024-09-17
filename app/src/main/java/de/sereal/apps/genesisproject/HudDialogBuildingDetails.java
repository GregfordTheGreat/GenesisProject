package de.sereal.apps.genesisproject;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Set;

import de.sereal.apps.genesisproject.obj.building.Building;
import de.sereal.apps.genesisproject.obj.building.BuildingDescriptor;
import de.sereal.apps.genesisproject.obj.building.Building_Mine_RareEarth;
import de.sereal.apps.genesisproject.obj.building.Building_Transport_Station;
import de.sereal.apps.genesisproject.rules.BuildingDef;
import de.sereal.apps.genesisproject.rules.MaterialDef;
import de.sereal.apps.genesisproject.rules.ResourceDef;
import de.sereal.apps.genesisproject.util.FloatArrayList;
import java.util.Vector;
import de.sereal.apps.genesisproject.rules.MaterialValue;
import android.util.DisplayMetrics;
import android.graphics.BlendMode;
import android.graphics.ColorMatrix;
import java.util.Optional;
import android.icu.text.CurrencyPluralInfo;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.List;
import de.sereal.apps.genesisproject.util.TextAnchor;

/**
 * Created by sereal on 30.08.2016.
 */
public class HudDialogBuildingDetails extends HudDialogStandard
{

  private Bitmap BuildingIcon;
  private DecimalFormat dc = new DecimalFormat("0.0");
  
  private RectF[] rectInputValues;
  private RectF[] rectResources;
  private Paint paintInput = new Paint();
  
  public HudDialogBuildingDetails(GLHudOverlayView parent) {
    super(parent);
    setDialogDimensions((int)(ParentView.mMetrics.widthPixels * 0.5f), (int)(ParentView.mMetrics.heightPixels * 0.5f), true);
    
    Building building = GameActivity.MyGameLogic.SelectedBuilding;
    BuildingDef buildingDef = GameActivity.MyGameLogic.GameRules.getBuildingDefinition(building.buildingDefinitionKey);
    BuildingIcon =  ParentView.GetIconByName(buildingDef.getIcon(), ParentView.IconSize, ParentView.IconSize);

    final BuildingDescriptor buildingDescriptor = building.getBuildingDescriptor();
    final Vector<MaterialValue> inputValues = buildingDescriptor.getInputMaterials();
    rectInputValues = new RectF[inputValues.size()];
    
    float y = DialogMetrics.top + (ParentView.IconSize + 10);
    paintInput.setShader(new LinearGradient(0, y, 0, y + ParentView.IconSize + 10, Color.parseColor("#FFAAAAFF"), Color.parseColor("#FF6666AA"), Shader.TileMode.REPEAT));
    for (int a=0; a<inputValues.size(); a++) {
        rectInputValues[a] = new RectF(
          DialogMetrics.left + 10 + ParentView.IconSize,
          y + a * (ParentView.IconSize + 10),
          DialogMetrics.left + 10 + 3 * ParentView.IconSize,
          y + a * (ParentView.IconSize + 10) + ParentView.IconSize
        );
    }
    
    float x = DialogMetrics.left + 10;
    final int resourceCostAmounts = buildingDescriptor.getResourceCosts().size();
    rectResources = new RectF[resourceCostAmounts];
    for (int a=0; a<resourceCostAmounts; a++) {
        rectResources[a] = new RectF(
            x,
            DialogMetrics.bottom - ParentView.IconSize - 10, 
            x + (2 * ParentView.IconSize),
            DialogMetrics.bottom - 10
        );
        x += (2 * ParentView.IconSize) + 10;
    }
    
    if(building instanceof Building_Mine_RareEarth) {
      ParentView.PlaySoundEffect(R.raw.mining);
    }
  }

  @Override
  protected void drawContent(Canvas canvas) {
    float x, y;

    final Building building = GameActivity.MyGameLogic.SelectedBuilding;
    final BuildingDef buildingDef = GameActivity.MyGameLogic.GameRules.getBuildingDefinition(building.buildingDefinitionKey);
    final BuildingDescriptor buildingDescriptor = building.getBuildingDescriptor();

    canvas.drawText(buildingDef.getName(), DialogMetrics.left + 10 + ParentView.IconSize, DialogMetrics.top + (ParentView.FontSize + ParentView.IconSize)/2, darkTextPaint);
    canvas.drawBitmap(BuildingIcon, DialogMetrics.left+10, DialogMetrics.top+10, ParentView.normalImagePaint);
    
    HashMap<String, Float> materials;
    if(building instanceof Building_Transport_Station) materials = GameActivity.MyGameLogic.GetAllAvailableMaterialsInStash();
    else materials = buildingDescriptor.getProductionInputStorage();
    
    final Vector<MaterialValue> inputValues = buildingDescriptor.getInputMaterials();
    for (int a=0; a<inputValues.size(); a++) {
       final String materialKey = inputValues.get(a).Material;
       final float current = Optional.ofNullable(materials.get(materialKey)).orElse(0f);
       final float capacity = buildingDescriptor.getStorageCapacity(materialKey);
       drawMaterial(canvas, materialKey, rectInputValues[a], current, capacity);
    }
    
    final Map<String, Float> resourceCosts = buildingDescriptor.getResourceCosts();
    final List<String> resourceCostKeys = resourceCosts.keySet().stream().collect(Collectors.toList());
    for (int a=0; a<resourceCostKeys.size(); a++) {
       final String resourceKey = resourceCostKeys.get(a);
        drawResource(canvas, resourceKey, rectResources[a], resourceCosts.get(resourceKey));
    }
    
    final HashMap<String, Float> producedGoods = buildingDescriptor.getProducedGoods();
    x = ParentView.mMetrics.widthPixels / 2;
    y = ParentView.mMetrics.heightPixels - ParentView.IconSize * 2 - 20;
    x -= producedGoods.size() * (ParentView.IconSize * 2 + 20) / 2;

    float progress;
    RectF rect;
    Path path, path2;
    PathMeasure pathMeasure;
    ParentView.textPaint.setTextAlign(Paint.Align.RIGHT);
    
    for(final String materialKey : producedGoods.keySet()) {
      progress = producedGoods.get(materialKey) / buildingDescriptor.getStorageCapacity(materialKey);

      rect = new RectF(x, y, x+ParentView.IconSize*2, y+ParentView.IconSize*2);
      path = GLHudOverlayView.ComposeRoundedRectPath(rect, ParentView.RoundedCornerIcon);
      pathMeasure = new PathMeasure(path, true);
      path2 = new Path();
      pathMeasure.getSegment(0,pathMeasure.getLength() * progress, path2, true);

      canvas.drawPath(path, ParentView.menuPaint);
      canvas.drawPath(path, ParentView.progressBackgroundPaint);
      canvas.drawPath(path2, ParentView.progressStrokePaint);
      canvas.drawBitmap(ParentView.GetMaterialIcon(materialKey), x+ParentView.IconSize/2, y+ParentView.RoundedCornerIcon, ParentView.normalImagePaint);

      canvas.drawText(dc.format(producedGoods.get(materialKey)), x +ParentView.IconSize * 2 - 30.0f ,y+ParentView.IconSize * 2 - 30.0f, ParentView.textPaint);

      x += ParentView.IconSize * 2 + 10;
    }
  }
  
  private void drawMaterial(final Canvas canvas, final String materialKey, final RectF rect, final float current, float capacity) {
     final MaterialDef mDef = GameActivity.MyGameLogic.GameRules.GetMaterialDefinition(materialKey);
     drawWithIcon(canvas, mDef.Icon, rect, current, capacity);
  }
  
  private void drawResource(final Canvas canvas, final String resourceKey, final RectF rect, final float current) {
      final ResourceDef rDef = GameActivity.MyGameLogic.GameRules.GetResourceDefinition(resourceKey);
      canvas.drawBitmap(ParentView.GetIconByName(rDef.Icon, ParentView.IconSize, ParentView.IconSize), rect.left, rect.bottom - ParentView.IconSize, ParentView.normalImagePaint);
      ParentView.drawStrokedTextAt(dc.format(current), rect, canvas, TextAnchor.MIDDLE_RIGHT);
  }
  
  private void drawWithIcon(final Canvas canvas, final String icon, final RectF rect, final float current, float capacity) {
     canvas.drawBitmap(ParentView.GetIconByName(icon, ParentView.IconSize, ParentView.IconSize), rect.left - ParentView.IconSize, rect.bottom - ParentView.IconSize, ParentView.normalImagePaint);
     canvas.drawRect(rect, ParentView.menuBorderPaint);
     canvas.drawRect(rect, ParentView.menuPaint);
     canvas.drawRect(rect.left, rect.top, rect.left + (current/capacity) * rect.width(), rect.bottom, paintInput);
     ParentView.drawCenteredAt(dc.format(current), rect, canvas);
  }
  
  
  public boolean isButtonClick(int x, int y) {
      return super.isButtonClicked(x, y);
  }

}
