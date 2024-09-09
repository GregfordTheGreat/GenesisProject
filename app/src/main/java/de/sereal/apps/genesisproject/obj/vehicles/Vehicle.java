package de.sereal.apps.genesisproject.obj.vehicles;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Vector;

import de.sereal.apps.genesisproject.PlanetSurfaceRenderer;
import de.sereal.apps.genesisproject.util.FloatArrayList;
import de.sereal.apps.genesisproject.util.PositionAnimation;
import de.sereal.apps.genesisproject.util.ShortArrayList;
import de.sereal.apps.genesisproject.util.Vector3D;
import de.sereal.apps.genesisproject.util.VehicleJob;
import de.sereal.apps.genesisproject.util.VehicleTask;

/**
 * Created by sereal on 13.08.2016.
 */
public abstract class Vehicle
{
  protected FloatBuffer vertexBuffer;
  protected FloatBuffer colorBuffer;
  protected ShortBuffer indexBuffer;
  protected FloatBuffer normalBuffer;
  protected int numberOfIndices = 0;

  protected Context context;
  protected float unitSize;

  public Vector3D position;
  public float RotationX = 0.0f;
  public float RotationY = 90.0f;
  public float RotationZ = 0.0f;

  public abstract void Build();


  public Vehicle(Context context, float unitSize)
  {
    this.context = context;
    this.unitSize = unitSize;
    Build();
  }


  public void setPosition(float x, float y, float z)
  {
    position = new Vector3D(x+0.5f*unitSize,y,z+0.5f*unitSize);
  }

  private void BuildFromFileV2(BufferedReader reader)
  {
    try
    {
      FloatArrayList vertices = new FloatArrayList();
      FloatArrayList normals = new FloatArrayList();
      FloatArrayList colors = new FloatArrayList();
      ShortArrayList indices = new ShortArrayList();

      String line;
      String[] groups, tokens;
      while( (line = reader.readLine()) != null)
      {
        if(line.startsWith("v"))
        {
          groups = line.split("\t", -1);
          tokens = groups[1].split(" ", -1);
          for(String s : tokens)
          {
            vertices.Add(Float.parseFloat(s) * unitSize);
          }
          tokens = groups[2].split(" ", -1);
          for(String s : tokens)
          {
            normals.Add(Float.parseFloat(s));
          }

          tokens = groups[3].split(" ", -1);
          for(String s : tokens)
          {
            colors.Add(Float.parseFloat(s));
          }
        }
        else
        if(line.startsWith("f"))
        {
          tokens = line.substring(line.indexOf(" ")+1).split(" ", -1);
          for(int a=tokens.length; a>0; a--)
          {
            indices.Add(Short.parseShort(tokens[a-1]));
          }
        }
      }
      numberOfIndices = indices.GetSize();


      ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.GetSize() * 4);
      vbb.order(ByteOrder.nativeOrder());
      vertexBuffer = vbb.asFloatBuffer();
      vertexBuffer.put(vertices.ToArray());
      vertexBuffer.position(0);

      // a float is 4 bytes, therefore we multiply the number if vertices with 4.
      ByteBuffer nbb = ByteBuffer.allocateDirect(normals.GetSize() * 4);
      nbb.order(ByteOrder.nativeOrder());
      normalBuffer = nbb.asFloatBuffer();
      normalBuffer.put(normals.ToArray());
      normalBuffer.position(0);

      // a float is 4 bytes, therefore we multiply the number if vertices with 4.
      ByteBuffer cbb = ByteBuffer.allocateDirect(colors.GetSize() * 4);
      cbb.order(ByteOrder.nativeOrder());
      colorBuffer = cbb.asFloatBuffer();
      colorBuffer.put(colors.ToArray());
      colorBuffer.position(0);

      ByteBuffer ibb = ByteBuffer.allocateDirect(indices.GetSize() * 2);
      ibb.order(ByteOrder.nativeOrder());
      indexBuffer = ibb.asShortBuffer();
      indexBuffer.put(indices.ToArray());
      indexBuffer.position(0);
    }
    catch(Exception e)
    {
      Log.e("Vehicle", "Exception while building from v2 file", e);
    }
  }

  public void BuildFromFile(InputStream in)
  {
    try
    {
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      BuildFromFileV2(reader);
    }
    catch(Exception e)
    {
      Log.e("Building", "Exception while building from file", e);
    }
  }

  public void Draw(int positionHandle, int colorHandle, int normalHandle, int texIDHandle, int textureHandle, int texCoordHandle)
  {
    PlanetSurfaceRenderer.MatrixLoadIdentity();
    PlanetSurfaceRenderer.MatrixTranslate(position.x, position.y, position.z);
    PlanetSurfaceRenderer.MatrixRotate(RotationX, RotationY, RotationZ);
    PlanetSurfaceRenderer.MatrixConvertToView(true);

      GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
      GLES20.glEnableVertexAttribArray(positionHandle);

      GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);
      GLES20.glEnableVertexAttribArray(colorHandle);

      GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 0, normalBuffer);
      GLES20.glEnableVertexAttribArray(normalHandle);

      GLES20.glDrawElements(GLES20.GL_TRIANGLES, numberOfIndices, GLES20.GL_UNSIGNED_SHORT, indexBuffer);

      GLES20.glDisableVertexAttribArray(colorHandle);
      GLES20.glDisableVertexAttribArray(normalHandle);
      GLES20.glDisableVertexAttribArray(positionHandle);

    // reset changes made to world
    PlanetSurfaceRenderer.MatrixLoadIdentity();
    PlanetSurfaceRenderer.MatrixConvertToView(true);
  }

}
