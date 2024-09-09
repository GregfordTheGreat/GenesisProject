package de.sereal.apps.genesisproject.event;

import android.view.MotionEvent;

/**
 * Created by sereal on 03.08.2016.
 */
public class DragGestureDetector extends GeneralGestureDetector
{
  private OnDragGestureListener mListener;
  private float sX, sY, fY;
  private float mDragDistanceX = 0.0f;
  private float mDragDistanceY = 0.0f;
  private float mTiltDistance = 0.0f;


  public DragGestureDetector(OnDragGestureListener listener)
  {
    mListener= listener;
  }

  public float GetDragDistanceX()
  {
    return mDragDistanceX;
  }
  public float GetDragDistanceY()
  {
    return mDragDistanceY;
  }
  public float GetTiltDistance()
  {
    return mTiltDistance;
  }
  public int GetX(){ return (int)sX; }
  public int GetY(){ return (int)sY; }

  @Override
  public boolean onTouchEvent(MotionEvent event)
  {
    switch (event.getActionMasked())
    {
      case MotionEvent.ACTION_DOWN:
        ptrID1 = event.getPointerId(event.getActionIndex());
        sX = event.getX(event.findPointerIndex(ptrID1));
        sY = event.getY(event.findPointerIndex(ptrID1));
        mDragDistanceX = 0.0f;
        mDragDistanceY = 0.0f;
        break;

      case MotionEvent.ACTION_POINTER_DOWN:
        ptrID2 = event.getPointerId(event.getActionIndex());
        fY = event.getY(event.findPointerIndex(ptrID1));
        mTiltDistance = 0.0f;
        break;


        case MotionEvent.ACTION_UP:           // stop all events, when last finger is leaving
        ptrID1 = INVALID_POINTER_ID;
        break;
      case MotionEvent.ACTION_POINTER_UP:     // stop tilt event, when second finger is leaving
        if(event.getActionIndex() == ptrID1)  // if the first of the two fingers lifts before the other, we have to update swap fingers
        {
          ptrID1 = ptrID2;
        }
        ptrID2 = INVALID_POINTER_ID;

        sX = event.getX(event.findPointerIndex(ptrID1));
        sY = event.getY(event.findPointerIndex(ptrID1));
        break;

      case MotionEvent.ACTION_MOVE:
        if(ptrID1 != INVALID_POINTER_ID)
        {
          float nX = event.getX(event.findPointerIndex(ptrID1));
          float nY = event.getY(event.findPointerIndex(ptrID1));

          if(ptrID2 != INVALID_POINTER_ID)
          {
            mTiltDistance = nY - sY;
            sY = nY;

            if(mListener!= null)
            {
              mListener.OnTilt(this);
            }

          }else{
            mDragDistanceX = nX - sX;
            mDragDistanceY = nY - sY;
            sX = nX;
            sY = nY;

            if(mListener!= null)
            {
              mListener.OnDrag(this);
            }
          }
        }
        break;

      case MotionEvent.ACTION_CANCEL:
        ptrID1 = INVALID_POINTER_ID;
        ptrID2 = INVALID_POINTER_ID;
        break;

    }
    return true;
  }



  public interface OnDragGestureListener
  {
    public boolean OnDrag(DragGestureDetector dragDetector);
    public boolean OnTilt(DragGestureDetector dragDetector);
    public boolean OnMove(DragGestureDetector dragDetector);
  }

  public static class SimpleDragGestureListener implements OnDragGestureListener
  {
    public boolean OnDrag(DragGestureDetector dragDetector){ return false; }
    public boolean OnTilt(DragGestureDetector dragDetector){ return false; }
    public boolean OnMove(DragGestureDetector dragDetector){ return false; }
  }

}
