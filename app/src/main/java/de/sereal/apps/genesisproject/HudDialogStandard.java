package de.sereal.apps.genesisproject;
import android.graphics.*;
import android.util.Log;
import android.text.*;

public abstract class HudDialogStandard extends HudDialog
{
    private Path dialogBackgroundPath;
    private RectF closeDialogDim;
    private Path closeButtonPath;
    private Paint closeButtonBackgroundPaint;
    protected Paint darkTextPaint;
    
    protected abstract void drawContent(Canvas canvas);
    
    public HudDialogStandard(final GLHudOverlayView parent){
        super(parent);
        closeButtonBackgroundPaint = new Paint();
        closeButtonBackgroundPaint.setARGB(255, 155, 0, 0);
        
        darkTextPaint = new Paint();
        darkTextPaint.setARGB(255, 50, 50, 50);
        darkTextPaint.setTextSize(ParentView.FontSize);
    }

    @Override
    public void onDimensionChanged()
    {
        dialogBackgroundPath = new Path();
        float[] dialogBackgroundRadius = { ParentView.BorderRadius, ParentView.BorderRadius, ParentView.BorderRadius, ParentView.BorderRadius, ParentView.BorderRadius, ParentView.BorderRadius, ParentView.BorderRadius, ParentView.BorderRadius};
        dialogBackgroundPath.addRoundRect(DialogMetrics.left, DialogMetrics.top, DialogMetrics.right, DialogMetrics.bottom, dialogBackgroundRadius, Path.Direction.CW);
        
        closeDialogDim = new RectF(DialogMetrics.right - ParentView.IconSize, DialogMetrics.top, DialogMetrics.right , DialogMetrics.top + ParentView.IconSize);
        
        closeButtonPath = new Path();
        float[] closeButtonRadius = { 0, 0, ParentView.BorderRadius, ParentView.BorderRadius, 0, 0, 0, 0 };
        closeButtonPath.addRoundRect(closeDialogDim, closeButtonRadius, Path.Direction.CW);   
    }

    
    @Override
    public boolean IsButtonClicked(int x, int y){
    
        if(closeDialogDim.contains(x,y)){
            Log.d("Close","requested");
            ParentView.ResetHud();
            return true;
        }
        
        return false;
    }
    
    @Override
    public void drawDetails(Canvas canvas)
    {
        canvas.drawPath(dialogBackgroundPath, ParentView.menuBorderPaint);
        canvas.drawPath(dialogBackgroundPath, ParentView.modalBackgroundPaint);
        canvas.drawPath(closeButtonPath, closeButtonBackgroundPaint);
        
        ParentView.drawCenteredAt("×", closeDialogDim, canvas);
       
        drawContent(canvas);
        
    }
    
    
}
