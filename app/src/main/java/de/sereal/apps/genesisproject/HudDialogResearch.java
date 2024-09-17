package de.sereal.apps.genesisproject;

import de.sereal.apps.genesisproject.*;
import android.graphics.*;
import de.sereal.apps.genesisproject.util.*;

public class HudDialogResearch extends HudDialogStandard {
     
     public HudDialogResearch(GLHudOverlayView parent){
         super(parent);
         setDialogDimensions((int)(ParentView.mMetrics.widthPixels * 0.9f), (int)(ParentView.mMetrics.heightPixels * 0.9f), true);
     }

     @Override
     protected void drawContent(Canvas canvas)
     {
         // TODO: Implement this method
     }

     @Override
     public boolean isButtonClicked(int x, int y)
     {
     // TODO: Implement this method 
         return super.isButtonClicked(x, y);
     }
     
}








