package de.sereal.apps.genesisproject.obj;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

import de.sereal.apps.genesisproject.util.Helpers;

/**
 * Created by sereal on 02.03.2017.
 */
public class Orbit
{
  private final FloatBuffer vertexBuffer;
  private final int vertextCount;

  private final float[] color = new float[] {0.5f, 0.5f, 0.5f, 1.0f};

  public Orbit(float radius, int slices)
  {
    float angleStep = ((2.0f * (float) Math.PI) / slices);

    vertexBuffer = Helpers.AllocateFloatBuffer(slices * 3);

    for (int j = 0; j < slices; j++)
    {
      float sinj = (float) Math.sin(angleStep * j);
      float cosj = (float) Math.cos(angleStep * j);

      vertexBuffer.put(sinj * radius);
      vertexBuffer.put(0.0f);
      vertexBuffer.put(cosj * radius);
    }
    vertexBuffer.position(0);
    vertextCount = slices;
  }

  public void Draw(int positionHandle, int colorHandle, int normalHandle, int texIDHandle, int textureHandle, int texCoordHandle)
  {
    GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
    GLES20.glEnableVertexAttribArray(positionHandle);

    GLES20.glVertexAttrib4fv(colorHandle, color, 0);
    GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, vertextCount);

    GLES20.glDisableVertexAttribArray(positionHandle);
  }

}
