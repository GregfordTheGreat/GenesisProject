package de.sereal.apps.genesisproject.obj;

import android.opengl.GLES20;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Vector;

import de.sereal.apps.genesisproject.R;
import de.sereal.apps.genesisproject.SolarSystemRenderer;
import de.sereal.apps.genesisproject.util.AdvPoint3D;
import de.sereal.apps.genesisproject.util.Helpers;
import de.sereal.apps.genesisproject.util.TextureHandler;

/**
 * Created by sereal on 26.02.2017.
 */
public class Sphere
{
  private FloatBuffer vertexBuffer;
  private FloatBuffer normalBuffer;
  private FloatBuffer colorBuffer;
  private FloatBuffer texCoordBuffer;
  private ShortBuffer indexBuffer;
  private int VerticeCount;

  private final float[] color = new float[] {1.0f, 1.0f, 1.0f, 1.0f};
  private int textureID = -1;
  private int shadowTextureID = -1;


  private float Radius = 0.0f;
  private int Latitudes = 0;
  private int Longitudes = 0;

  private Vector<AdvPoint3D> VertexPoints = new Vector<>();

  public Sphere(float radius, int latitudes, int longitudes)
  {
    Radius = radius;
    Latitudes = latitudes;
    Longitudes = longitudes;

    float angleStepI = ((float) Math.PI / Latitudes);
    float angleStepJ = ((2.0f * (float) Math.PI) / Longitudes);

    VertexPoints.addElement(new AdvPoint3D(0.0f, Radius, 0.0f, 0.0f, 0.0f, color));
    for(int i=1; i<Latitudes-1; i++)
    {
      float sini = (float) Math.sin(angleStepI * i);
      float cosi = (float) Math.cos(angleStepI * i);

      for(int j=0; j<Longitudes; j++)
      {
        float sinj = (float) Math.sin(angleStepJ * j);
        float cosj = (float) Math.cos(angleStepJ * j);

        VertexPoints.addElement(new AdvPoint3D(
                sini * sinj * Radius,
                cosi * Radius,
                sini * cosj * Radius,
                (float)j / (float)Longitudes,
                (float)i / (float)Latitudes,
                color
        ));
      }
    }
    VertexPoints.addElement(new AdvPoint3D(0.0f, -Radius, 0.0f, 0.0f, 1.0f, color));
  }



  public void SetHeightMap()
  {
    float x,y,z, h;
    for(AdvPoint3D p : VertexPoints)
    {
      x = p.x;
      y = p.y;
      z = p.z;

      h = 1.0f;

      p.x += (x / Radius) * h;
      p.y += (y / Radius) * h;
      p.z += (z / Radius) * h;
    }
  }

  public void Create(boolean inverted)
  {
    int vertexCount = 2 * (Longitudes * 3);
    vertexCount += (Latitudes - 3) * (4 * Longitudes);

    int triangleCount = 2 * Latitudes;
    triangleCount += (Latitudes - 3) * (2 * Longitudes);

    vertexBuffer = Helpers.AllocateFloatBuffer(vertexCount * 3);
    colorBuffer = Helpers.AllocateFloatBuffer(vertexCount * 4);
    normalBuffer = Helpers.AllocateFloatBuffer(vertexCount * 3);
    texCoordBuffer = Helpers.AllocateFloatBuffer(vertexCount * 2);
    indexBuffer = Helpers.AllocateShortBuffer(triangleCount * 3);

    AdvPoint3D P0, P1, P2, P3;
    int index;
    int vertexIndex = 0;

    boolean seam;
    P0 = VertexPoints.get(0);
    for (int j = 0; j < Longitudes; j++)
    {
      seam = (j == Longitudes - 1);
      index = 1 + j;
      P1 = VertexPoints.get(index);
      index++;
      if(seam) index = 1;
      P2 = VertexPoints.get(index);

      vertexBuffer.put(P0.x);
      vertexBuffer.put(P0.y);
      vertexBuffer.put(P0.z);
      vertexBuffer.put(P1.x);
      vertexBuffer.put(P1.y);
      vertexBuffer.put(P1.z);
      vertexBuffer.put(P2.x);
      vertexBuffer.put(P2.y);
      vertexBuffer.put(P2.z);

      colorBuffer.put(P0.r);
      colorBuffer.put(P0.g);
      colorBuffer.put(P0.b);
      colorBuffer.put(P0.a);
      colorBuffer.put(P1.r);
      colorBuffer.put(P1.g);
      colorBuffer.put(P1.b);
      colorBuffer.put(P1.a);
      colorBuffer.put(P2.r);
      colorBuffer.put(P2.g);
      colorBuffer.put(P2.b);
      colorBuffer.put(P2.a);

      normalBuffer.put(P0.x);
      normalBuffer.put(P0.y);
      normalBuffer.put(P0.z);
      normalBuffer.put(P1.x);
      normalBuffer.put(P1.y);
      normalBuffer.put(P1.z);
      normalBuffer.put(P2.x);
      normalBuffer.put(P2.y);
      normalBuffer.put(P2.z);

      texCoordBuffer.put((P1.u + (seam ? 1.0f : P2.u)) / 2.0f);
      texCoordBuffer.put(P0.v);
      texCoordBuffer.put(P1.u);
      texCoordBuffer.put(P1.v);
      texCoordBuffer.put(seam? 1.0f : P2.u);
      texCoordBuffer.put(P2.v);

      if(inverted)
      {
        indexBuffer.put((short)(vertexIndex + 0));
        indexBuffer.put((short)(vertexIndex + 2));
        indexBuffer.put((short)(vertexIndex + 1));
      }else{
        indexBuffer.put((short)(vertexIndex + 0));
        indexBuffer.put((short)(vertexIndex + 1));
        indexBuffer.put((short)(vertexIndex + 2));
      }

      vertexIndex += 3;
    }
    VerticeCount = vertexIndex;

    for(int i=1; i<Latitudes-2; i++) {
      for (int j = 0; j < Longitudes; j++) {
        seam = (j >= Longitudes - 1);

        index = 1 + (i-1) * Longitudes + j;

        P0 = VertexPoints.get(index);
        vertexBuffer.put(P0.x);
        vertexBuffer.put(P0.y);
        vertexBuffer.put(P0.z);

        index = 1 + (i-1) * Longitudes;
        if(!seam) index += j+1;
        P1 = VertexPoints.get(index);
        vertexBuffer.put(P1.x);
        vertexBuffer.put(P1.y);
        vertexBuffer.put(P1.z);

        index = 1 + i * Longitudes;
        if(!seam) index += j+1;
        P2 = VertexPoints.get(index);
        vertexBuffer.put(P2.x);
        vertexBuffer.put(P2.y);
        vertexBuffer.put(P2.z);

        index = 1 + i * Longitudes+j;
        P3 = VertexPoints.get(index);
        vertexBuffer.put(P3.x);
        vertexBuffer.put(P3.y);
        vertexBuffer.put(P3.z);

        colorBuffer.put(P0.r);
        colorBuffer.put(P0.g);
        colorBuffer.put(P0.b);
        colorBuffer.put(P0.a);
        colorBuffer.put(P1.r);
        colorBuffer.put(P1.g);
        colorBuffer.put(P1.b);
        colorBuffer.put(P1.a);
        colorBuffer.put(P2.r);
        colorBuffer.put(P2.g);
        colorBuffer.put(P2.b);
        colorBuffer.put(P2.a);
        colorBuffer.put(P3.r);
        colorBuffer.put(P3.g);
        colorBuffer.put(P3.b);
        colorBuffer.put(P3.a);

        normalBuffer.put(P0.x);
        normalBuffer.put(P0.y);
        normalBuffer.put(P0.z);
        normalBuffer.put(P1.x);
        normalBuffer.put(P1.y);
        normalBuffer.put(P1.z);
        normalBuffer.put(P2.x);
        normalBuffer.put(P2.y);
        normalBuffer.put(P2.z);
        normalBuffer.put(P3.x);
        normalBuffer.put(P3.y);
        normalBuffer.put(P3.z);

        texCoordBuffer.put(P0.u);
        texCoordBuffer.put(P0.v);
        texCoordBuffer.put(seam ? 1.0f : P1.u);
        texCoordBuffer.put(P1.v);
        texCoordBuffer.put(seam ? 1.0f : P2.u);
        texCoordBuffer.put(P2.v);
        texCoordBuffer.put(P3.u);
        texCoordBuffer.put(P3.v);

        if(inverted)
        {
          indexBuffer.put((short)(vertexIndex + 0));
          indexBuffer.put((short)(vertexIndex + 1));
          indexBuffer.put((short)(vertexIndex + 2));

          indexBuffer.put((short)(vertexIndex + 0));
          indexBuffer.put((short)(vertexIndex + 2));
          indexBuffer.put((short)(vertexIndex + 3));
        }else{
          indexBuffer.put((short)(vertexIndex + 0));
          indexBuffer.put((short)(vertexIndex + 2));
          indexBuffer.put((short)(vertexIndex + 1));

          indexBuffer.put((short)(vertexIndex + 0));
          indexBuffer.put((short)(vertexIndex + 3));
          indexBuffer.put((short)(vertexIndex + 2));
        }

        vertexIndex += 4;
        VerticeCount += 6;
      }
    }


    P0 = VertexPoints.get(VertexPoints.size()-1);
    for (int j = 0; j < Longitudes; j++)
    {
      seam =(j == Longitudes - 1);
      index = 1 + ((Latitudes - 3) * (Longitudes)) + j;
      P1 = VertexPoints.get(index);
      index++;
      if(seam) index = 1 + ((Latitudes - 3) * (Longitudes));
      P2 = VertexPoints.get(index);


      vertexBuffer.put(P0.x);
      vertexBuffer.put(P0.y);
      vertexBuffer.put(P0.z);
      vertexBuffer.put(P1.x);
      vertexBuffer.put(P1.y);
      vertexBuffer.put(P1.z);
      vertexBuffer.put(P2.x);
      vertexBuffer.put(P2.y);
      vertexBuffer.put(P2.z);

      colorBuffer.put(P0.r);
      colorBuffer.put(P0.g);
      colorBuffer.put(P0.b);
      colorBuffer.put(P0.a);
      colorBuffer.put(P1.r);
      colorBuffer.put(P1.g);
      colorBuffer.put(P1.b);
      colorBuffer.put(P1.a);
      colorBuffer.put(P2.r);
      colorBuffer.put(P2.g);
      colorBuffer.put(P2.b);
      colorBuffer.put(P2.a);

      normalBuffer.put(P0.x);
      normalBuffer.put(P0.y);
      normalBuffer.put(P0.z);
      normalBuffer.put(P1.x);
      normalBuffer.put(P1.y);
      normalBuffer.put(P1.z);
      normalBuffer.put(P2.x);
      normalBuffer.put(P2.y);
      normalBuffer.put(P2.z);

      texCoordBuffer.put((P1.u + (seam ? 1.0f : P2.u)) / 2.0f);
      texCoordBuffer.put(P0.v);
      texCoordBuffer.put(P1.u);
      texCoordBuffer.put(P1.v);
      texCoordBuffer.put(seam ? 1.0f : P2.u);
      texCoordBuffer.put(P2.v);

      if(inverted)
      {
        indexBuffer.put((short)(vertexIndex + 0));
        indexBuffer.put((short)(vertexIndex + 1));
        indexBuffer.put((short)(vertexIndex + 2));
      }else{
        indexBuffer.put((short)(vertexIndex + 0));
        indexBuffer.put((short)(vertexIndex + 2));
        indexBuffer.put((short)(vertexIndex + 1));
      }

      vertexIndex += 3;
      VerticeCount += 3;
    }

    vertexBuffer.position(0);
    normalBuffer.position(0);
    texCoordBuffer.position(0);
    colorBuffer.position(0);
    indexBuffer.position(0);

  }

  public void SetTexture(int ResourceID, float textureScale)
  {
    textureID = TextureHandler.LoadTexture(ResourceID, GLES20.GL_REPEAT);
    for(AdvPoint3D p : VertexPoints)
    {
      p.u *= textureScale;
      p.v *= textureScale;
    }
  }

  public void SetShadowTexture(int id)
  {
    shadowTextureID = id;
  }

  public int GenerateTexture( float textureScale)
  {

    //textureID = TextureHandler.GenerateNoiseTexture(512);
    textureID = TextureHandler.LoadTexture(R.raw.cloud_cover);
    for(AdvPoint3D p : VertexPoints)
    {
      p.u *= textureScale;
      p.v *= textureScale;
    }
    return textureID;
  }


  public void Draw(int positionHandle, int colorHandle, int normalHandle, int textureIDHandle, int textureIndexHandle, int texCoordHandle)
  {
    GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
    GLES20.glEnableVertexAttribArray(positionHandle);

		GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);
    GLES20.glEnableVertexAttribArray(colorHandle);

    if(textureID >= 0)
    {
      if(shadowTextureID >= 0)
      {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(SolarSystemRenderer.uShadowTextureIndexHandle, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, shadowTextureID);
        GLES20.glUniform1i(SolarSystemRenderer.uShadowTextureIDHandle, shadowTextureID);
      }

      GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
      GLES20.glUniform1i(textureIndexHandle, 1); 	// use GL_TEXTURE1

      GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer);
      GLES20.glEnableVertexAttribArray(texCoordHandle);

      GLES20.glUniform1i(textureIDHandle, textureID); // useTexture
      GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);

    }


    GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 0, normalBuffer);
    GLES20.glEnableVertexAttribArray(normalHandle);

    GLES20.glDrawElements(GLES20.GL_TRIANGLES, VerticeCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer);


    GLES20.glDisableVertexAttribArray(colorHandle);
    GLES20.glDisableVertexAttribArray(normalHandle);
    GLES20.glDisableVertexAttribArray(positionHandle);
    GLES20.glDisableVertexAttribArray(texCoordHandle);
    GLES20.glUniform1i(textureIDHandle, 0);		// tell the shader, we're done using the texture and return to color mode
    GLES20.glUniform1i(SolarSystemRenderer.uShadowTextureIDHandle, 0);		// tell the shader, we're done using the texture and return to color mode
  }
}
