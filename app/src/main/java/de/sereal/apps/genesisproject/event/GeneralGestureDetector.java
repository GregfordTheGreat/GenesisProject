package de.sereal.apps.genesisproject.event;

import android.view.MotionEvent;

/**
 * Created by sereal on 03.08.2016.
 */
public abstract class GeneralGestureDetector
{
  public static final int INVALID_POINTER_ID = -1;
  public int ptrID1 = INVALID_POINTER_ID;
  public int ptrID2 = INVALID_POINTER_ID;

  public boolean onTouchEvent(MotionEvent event){ return false; }
}
