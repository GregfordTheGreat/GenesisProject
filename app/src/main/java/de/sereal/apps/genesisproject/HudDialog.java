package de.sereal.apps.genesisproject;

import android.graphics.Canvas;
import android.graphics.Rect;

import de.sereal.apps.genesisproject.util.Dimensions;
import de.sereal.apps.genesisproject.util.Position2D;
import android.util.Log;

public abstract class HudDialog
{
    
  private Dimensions DialogSize = new Dimensions(0, 0);
  private Position2D DialogPosition = new Position2D(0, 0);
  public Rect DialogMetrics = new Rect();
  public boolean IsVisible = false;

  public GLHudOverlayView ParentView;

  public abstract void onDimensionChanged();
  public abstract void drawDetails(Canvas canvas);
  public abstract boolean IsButtonClicked(int x, int y);
  

  public HudDialog(GLHudOverlayView parent)
  {
    ParentView = parent;
  }

  public void CenterHorizontal(int y)
  {
    DialogPosition.X = ParentView.mMetrics.widthPixels / 2 - DialogSize.Width / 2;
    DialogPosition.Y = y;

    recalculateDimensions();
  }
  
  private void centerDialog() {
      DialogPosition.X = (ParentView.mMetrics.widthPixels - DialogSize.Width) / 2;
      DialogPosition.Y = (ParentView.mMetrics.heightPixels - DialogSize.Height) / 2;
      recalculateDimensions();
  }

  public boolean IsClicked(int x, int y)
  {
    if(IsButtonClicked(x, y))
    {
      return true;
    }else{
      return DialogMetrics.contains(x, y);
    }
  }

  public void setDialogDimensions(int w, int h, boolean center){
      DialogSize.Width = w;
      DialogSize.Height = h;
      
      if(center){
          centerDialog();
      }else{
          recalculateDimensions();
      }
  }
  
  private void recalculateDimensions() {
      DialogMetrics.left = DialogPosition.X;
      DialogMetrics.top = DialogPosition.Y;
      DialogMetrics.right = DialogPosition.X+DialogSize.Width;
      DialogMetrics.bottom = DialogPosition.Y+DialogSize.Height;
      onDimensionChanged();
  }

  public void Show()
  {
    IsVisible = true;
  }
  
  public void Hide()
  {
    IsVisible = false;
  }
  
  public void Draw(final Canvas canvas) {
      drawDetails(canvas);
  }
}
