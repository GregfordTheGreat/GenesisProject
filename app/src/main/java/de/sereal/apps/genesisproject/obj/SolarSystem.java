package de.sereal.apps.genesisproject.obj;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import de.sereal.apps.genesisproject.PlanetSurfaceRenderer;
import de.sereal.apps.genesisproject.R;
import de.sereal.apps.genesisproject.SolarSystemRenderer;
import de.sereal.apps.genesisproject.obj.celestial.CelestialBody;
import de.sereal.apps.genesisproject.util.Helpers;
import de.sereal.apps.genesisproject.util.Ray;
import de.sereal.apps.genesisproject.util.Vector3D;

/**
 * Created by sereal on 04.01.2017.
 */
public class SolarSystem
{
  private Context context;
  private SolarSystemRenderer solarSystemRenderer;

  private CelestialBody Sun;
  private CelestialBody Planet1;
  private CelestialBody Planet2;
  private CelestialBody Planet3;
  private AsteroidBelt asteroidBelt;


  public SolarSystem(Context context, SolarSystemRenderer renderer)
  {
    this.solarSystemRenderer = renderer;
    this.context = context;

    Sun = new CelestialBody(0.0f, 0.0f, 4.0f);
    Sun.body = new Sphere(4.0f, 30, 30);
    Sun.body.Create(false);



    Planet1 = new CelestialBody(15.0f, 45.0f, 1.0f);
    Planet1.body.SetTexture(R.raw.planet_ganymede, 1.0f);
    Planet1.body.Create(false);

    Planet2 = new CelestialBody(23.0f, 120.0f, 2.0f);
    Planet2.body.SetTexture(R.raw.earthlike, 1.0f);
    Planet2.body.Create(false);
    Planet2.AddCloudCover();

    asteroidBelt = new AsteroidBelt(17.0f, 19.0f);


    Planet3 = new CelestialBody(32.0f, 160.0f, 3.0f);
    Planet3.body.SetTexture(R.raw.planet_gasgiant, 1.0f);
    Planet3.body.Create(false);
  }

  public void Pick(Ray ray)
  {
    if(Helpers.HasBoundingboxHit(ray, Sun.BoundingBox))
    {
      Log.d("Has hit","Sun");
      solarSystemRenderer.cameraSolarSystem.MoveTo(Sun.center);
    }

    if(Helpers.HasBoundingboxHit(ray, Planet1.BoundingBox))
    {
      Log.d("Has hit","Planet1");
      solarSystemRenderer.cameraSolarSystem.MoveTo(Planet1.center);
    }

    if(Helpers.HasBoundingboxHit(ray, Planet2.BoundingBox))
    {
      Log.d("Has hit","Planet2");
      solarSystemRenderer.cameraSolarSystem.MoveTo(Planet2.center);
    }

    if(Helpers.HasBoundingboxHit(ray, Planet3.BoundingBox))
    {
      Log.d("Has hit","Planet3");
      solarSystemRenderer.cameraSolarSystem.MoveTo(Planet3.center);
    }
  }

  public void DrawBlurParts(int positionHandle, int colorHandle, int normalHandle, int texIDHandle, int textureHandle, int texCoordHandle, int affectedByLightingHandle)
  {
    GLES20.glUniform1i(affectedByLightingHandle, 0);
    Sun.body.Draw(positionHandle, colorHandle, normalHandle, texIDHandle, textureHandle, texCoordHandle);

    solarSystemRenderer.MatrixLoadIdentity();
    solarSystemRenderer.MatrixTranslate(Planet1.center.x, Planet1.center.y, Planet1.center.z);
    solarSystemRenderer.MatrixConvertToView();
    GLES20.glUniform1i(affectedByLightingHandle, 1);
    Planet1.body.Draw(positionHandle, colorHandle, normalHandle, -1, 0, 0);

    solarSystemRenderer.MatrixLoadIdentity();
    solarSystemRenderer.MatrixTranslate(Planet2.center.x, Planet2.center.y, Planet2.center.z);
    solarSystemRenderer.MatrixConvertToView();
    GLES20.glUniform1i(affectedByLightingHandle, 1);
  //  Planet2.body.Draw(positionHandle, colorHandle, normalHandle, -1, 0, 0);

    solarSystemRenderer.MatrixLoadIdentity();
    solarSystemRenderer.MatrixConvertToView();

  }



  public void Draw(int positionHandle, int colorHandle, int normalHandle, int textureIndexHandle, int textureIDHandle, int texCoordHandle, int affectedByLightingHandle)
  {
    GLES20.glUniform1i(affectedByLightingHandle, 0);
    Sun.body.Draw(positionHandle, colorHandle, normalHandle, textureIndexHandle, textureIDHandle, texCoordHandle);
    Planet1.orbit.Draw(positionHandle, colorHandle, normalHandle, textureIndexHandle, textureIDHandle, texCoordHandle);
    Planet2.orbit.Draw(positionHandle, colorHandle, normalHandle, textureIndexHandle, textureIDHandle, texCoordHandle);
    Planet3.orbit.Draw(positionHandle, colorHandle, normalHandle, textureIndexHandle, textureIDHandle, texCoordHandle);

    solarSystemRenderer.MatrixLoadIdentity();
    solarSystemRenderer.MatrixTranslate(Planet1.center.x, Planet1.center.y, Planet1.center.z);
    solarSystemRenderer.MatrixConvertToView();
    GLES20.glUniform1i(affectedByLightingHandle, 1);

    Planet1.body.Draw(positionHandle, colorHandle, normalHandle, textureIndexHandle, textureIDHandle, texCoordHandle);



    solarSystemRenderer.MatrixLoadIdentity();
    solarSystemRenderer.MatrixTranslate(Planet2.center.x, Planet2.center.y, Planet2.center.z);
    solarSystemRenderer.MatrixConvertToView();

    Planet2.body.Draw(positionHandle, colorHandle, normalHandle, textureIndexHandle, textureIDHandle, texCoordHandle);

    GLES20.glBlendEquation(GLES20.GL_FUNC_ADD);
    GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

    GLES20.glEnable(GLES20.GL_BLEND);
    Planet2.cloudCover.Draw(positionHandle, colorHandle, normalHandle, textureIndexHandle, textureIDHandle, texCoordHandle);
    GLES20.glDisable(GLES20.GL_BLEND);



    solarSystemRenderer.MatrixLoadIdentity();
    solarSystemRenderer.MatrixTranslate(Planet3.center.x, Planet3.center.y, Planet3.center.z);
    solarSystemRenderer.MatrixConvertToView();

    Planet3.body.Draw(positionHandle, colorHandle, normalHandle, textureIndexHandle, textureIDHandle, texCoordHandle);




    solarSystemRenderer.MatrixLoadIdentity();
    solarSystemRenderer.MatrixConvertToView();

    GLES20.glUseProgram(solarSystemRenderer.programHandleParticle);
    GLES20.glUniform3f(PlanetSurfaceRenderer.uCameraPosParticleHandle,
            solarSystemRenderer.cameraSolarSystem.position.x,
            solarSystemRenderer.cameraSolarSystem.position.y,
            solarSystemRenderer.cameraSolarSystem.position.z);

    GLES20.glUniform1f(solarSystemRenderer.uPointSizeHandle, 50.0f);
    GLES20.glUniformMatrix4fv(solarSystemRenderer.mMVPMatrixParticleHandle, 1, false, solarSystemRenderer.mMVPMatrix, 0);
    asteroidBelt.Draw(solarSystemRenderer.mPositionParticleHandle, solarSystemRenderer.uColorParticleHandle, -1, -1, solarSystemRenderer.uTextureParticleHandle, -1);
  }
}
