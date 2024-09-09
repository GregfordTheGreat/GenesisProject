package de.sereal.apps.genesisproject.obj;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import de.sereal.apps.genesisproject.PlanetSurfaceRenderer;

/**
 * Created by sereal on 24.09.2016.
 */
public class Cube
{
  private FloatBuffer vertexBuffer;
  private FloatBuffer normalBuffer;
  private FloatBuffer colorBuffer;
  private ShortBuffer indexBuffer;


  public Cube()
  {
    float[] colors = new float[]{
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f
    };

    float[] vertices = new float[]{
            1f, 2f, 1f,
            1f, 2f, 2f,
            2f, 2f, 2f,
            2f, 2f, 1f,
            1f, 1.99f, 1f,
            1f, 1.99f, 2f,
            2f, 1.99f, 2f,
            2f, 1.99f, 1f,
    };

    float[] normals = new float[]{
            0f, 1f, 0f,
            0f, 1f, 0f,
            0f, 1f, 0f,
            0f, 1f, 0f,
            0f,-1f, 0f,
            0f,-1f, 0f,
            0f,-1f, 0f,
            0f,-1f, 0f
    };
    short[] indices = new short[]{
            0, 1, 2, 0, 2, 3,
            7, 6, 5, 7, 5, 4
    };

    ByteBuffer vbb =  ByteBuffer.allocateDirect(vertices.length * 4);
    vbb.order(ByteOrder.nativeOrder());
    vertexBuffer = vbb.asFloatBuffer();
    vertexBuffer.put(vertices);
    vertexBuffer.position(0);

    ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
    cbb.order(ByteOrder.nativeOrder());
    colorBuffer = cbb.asFloatBuffer();
    colorBuffer.put(colors);
    colorBuffer.position(0);

    ByteBuffer nbb = ByteBuffer.allocateDirect(normals.length * 4);
    nbb.order(ByteOrder.nativeOrder());
    normalBuffer = nbb.asFloatBuffer();
    normalBuffer.put(normals);
    normalBuffer.position(0);

    ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 2);
    ibb.order(ByteOrder.nativeOrder());
    indexBuffer = ibb.asShortBuffer();
    indexBuffer.put(indices);
    indexBuffer.position(0);
  }

  public void Draw(boolean posOnly)
  {
    if(posOnly)
    {
      GLES20.glVertexAttribPointer(PlanetSurfaceRenderer.mPositionHandleDepth, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
      GLES20.glEnableVertexAttribArray(PlanetSurfaceRenderer.mPositionHandleDepth);
      GLES20.glDrawElements(GLES20.GL_TRIANGLES, 12, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
      return;
    }

    GLES20.glVertexAttribPointer(PlanetSurfaceRenderer.mPositionHandleDaytime, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
    GLES20.glEnableVertexAttribArray(PlanetSurfaceRenderer.mPositionHandleDaytime);
    GLES20.glVertexAttribPointer(PlanetSurfaceRenderer.mColorHandleDaytime, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);
    GLES20.glEnableVertexAttribArray(PlanetSurfaceRenderer.mColorHandleDaytime);
    GLES20.glVertexAttribPointer(PlanetSurfaceRenderer.mNormalHandleDaytime, 3, GLES20.GL_FLOAT, false, 0, normalBuffer);
    GLES20.glEnableVertexAttribArray(PlanetSurfaceRenderer.mNormalHandleDaytime);

    GLES20.glDrawElements(GLES20.GL_TRIANGLES, 12, GLES20.GL_UNSIGNED_SHORT, indexBuffer);

    GLES20.glDisableVertexAttribArray(PlanetSurfaceRenderer.mColorHandleDaytime);
    GLES20.glDisableVertexAttribArray(PlanetSurfaceRenderer.mNormalHandleDaytime);
    GLES20.glDisableVertexAttribArray(PlanetSurfaceRenderer.mPositionHandleDaytime);
  }

}
