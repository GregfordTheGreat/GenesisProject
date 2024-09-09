package de.sereal.apps.genesisproject.obj;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

import de.sereal.apps.genesisproject.R;
import de.sereal.apps.genesisproject.util.Helpers;
import de.sereal.apps.genesisproject.util.MyConstants;
import de.sereal.apps.genesisproject.util.TextureHandler;

/**
 * Created by sereal on 23.03.2017.
 */
public class AsteroidBelt
{
  private FloatBuffer vertexBuffer;
  private int NumberOfAsteroids = 1000;

  public AsteroidBelt(float innerRadius, float outerRadius)
  {
    float phi, rDiff;

    vertexBuffer = Helpers.AllocateFloatBuffer(4 * 1000);

    rDiff = outerRadius - innerRadius;
    for(int a = 0; a < NumberOfAsteroids; a++)
    {
      phi =  (float)Math.random();
      vertexBuffer.put((float)Math.sin(phi * MyConstants.TwoPi) * (innerRadius + (float)Math.random() * rDiff));
      vertexBuffer.put(0.5f - (float)Math.random());
      vertexBuffer.put((float)Math.cos(phi * MyConstants.TwoPi) * (innerRadius + (float)Math.random() * rDiff));
      vertexBuffer.put(5.0f + (float)Math.random() * 20.0f);
    }
    vertexBuffer.position(0);
  }

  float[] color = new float[]{  0.5f, 0.5f, 0.5f, 1.0f };
  public void Draw(int positionHandle, int colorHandle, int normalHandle, int texIDHandle, int textureHandle, int texCoordHandle)
  {
    GLES20.glDepthMask(false);
    GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

    GLES20.glBlendEquation(GLES20.GL_FUNC_ADD);
    GLES20.glEnable(GLES20.GL_BLEND);

    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, TextureHandler.GetTextureHandle(R.raw.particle_asteroid)); 				// set the handle of the texture from LoadTexture
    GLES20.glUniform1i(textureHandle, 0);

    GLES20.glVertexAttrib4fv(colorHandle, color, 0);

    GLES20.glVertexAttribPointer(positionHandle, 4, GLES20.GL_FLOAT, false, 0, vertexBuffer);
    GLES20.glEnableVertexAttribArray(positionHandle);

    GLES20.glDrawArrays(GLES20.GL_POINTS, 0, NumberOfAsteroids);

    GLES20.glDisableVertexAttribArray(positionHandle);
    GLES20.glDisable(GLES20.GL_BLEND);
    GLES20.glDepthMask(true);
  }

}
