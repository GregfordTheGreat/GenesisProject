package de.sereal.apps.genesisproject;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by sereal on 14.02.2017.
 */
public class DispatchingRenderer implements GLSurfaceView.Renderer
{
  private PlanetSurfaceRenderer planetSurfaceRenderer;
  private SolarSystemRenderer solarSystemRenderer;
  private GLSurfaceView.Renderer currentRenderer;
  private GameActivity.ViewTypes activeViewType;

  public DispatchingRenderer(GameActivity context)
  {
    planetSurfaceRenderer = new PlanetSurfaceRenderer(context);
    solarSystemRenderer = new SolarSystemRenderer(context);

    this.currentRenderer = planetSurfaceRenderer;
  }

  public void SetActiveRenderer(GameActivity.ViewTypes viewType)
  {
    switch(viewType)
    {
      case PLANET_VIEW:
        currentRenderer = planetSurfaceRenderer;
        activeViewType = GameActivity.ViewTypes.PLANET_VIEW;
        break;

      case SYSTEM_VIEW:
        currentRenderer = solarSystemRenderer;
        activeViewType = GameActivity.ViewTypes.SYSTEM_VIEW;
        break;
    }
  }
  public void onSurfaceCreated(GL10 gl, EGLConfig config)
  {
    planetSurfaceRenderer.onSurfaceCreated(gl, config);
    solarSystemRenderer.onSurfaceCreated(gl, config);
    // do one-time setup
  }

  public void onSurfaceChanged(GL10 gl, int w, int h)
  {
    planetSurfaceRenderer.onSurfaceChanged(gl, w, h);
    solarSystemRenderer.onSurfaceChanged(gl, w, h);
  }

  public void onDrawFrame(GL10 gl)
  {
    this.currentRenderer.onDrawFrame(gl);
  }

  public void touchClick(float x, float y)
  {
    switch(activeViewType)
    {
      case PLANET_VIEW:
        planetSurfaceRenderer.touchClick(x, y);
        break;

      case SYSTEM_VIEW:
        solarSystemRenderer.touchClick(x, y);
        break;

    }
  }


}
