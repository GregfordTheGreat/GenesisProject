package de.sereal.apps.genesisproject.obj;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import de.sereal.apps.genesisproject.util.FloatArrayList;
import de.sereal.apps.genesisproject.util.ShortArrayList;

/**
 * Created by sereal on 09.11.2016.
 */
public class SimpleVBO extends VertexBufferObject
{
  public ShortBuffer indexBuffer;
  public int numberOfIndices = 0;

  @Override
  public void DrawPositionOnly(int positionHandle)
  {
    GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
    GLES20.glEnableVertexAttribArray(positionHandle);

    GLES20.glDrawElements(GLES20.GL_TRIANGLES, numberOfIndices, GLES20.GL_UNSIGNED_SHORT, indexBuffer);

    GLES20.glDisableVertexAttribArray(positionHandle);
  }

  @Override
  public void Draw(int positionHandle, int colorHandle, int normalHandle, int texIDHandle, int textureHandle, int texCoordHandle)
  {
    GLES20.glEnable(GLES20.GL_BLEND);
    GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

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

    GLES20.glDisable(GLES20.GL_BLEND);
  }




  public static SimpleVBO BuildFromFile(Context context, int idMesh, float unitSize)
  {
    SimpleVBO retVal = new SimpleVBO();
    try
    {
      FloatArrayList vertices = new FloatArrayList();
      FloatArrayList normals = new FloatArrayList();
      FloatArrayList colors = new FloatArrayList();
      ShortArrayList indices = new ShortArrayList();

      BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(idMesh)));
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
      retVal.numberOfIndices = indices.GetSize();


      retVal.vertexBuffer = ByteBuffer.allocateDirect(vertices.GetSize() * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertices.ToArray());
      retVal.vertexBuffer.position(0);

      retVal.normalBuffer = ByteBuffer.allocateDirect(normals.GetSize() * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(normals.ToArray());
      retVal.normalBuffer.position(0);

      retVal.colorBuffer = ByteBuffer.allocateDirect(colors.GetSize() * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(colors.ToArray());
      retVal.colorBuffer.position(0);

      retVal.indexBuffer = ByteBuffer.allocateDirect(indices.GetSize() * 2).order(ByteOrder.nativeOrder()).asShortBuffer().put(indices.ToArray());
      retVal.indexBuffer.position(0);

      retVal.valid = true;
    }catch(Exception e){
      Log.e("Building", "Exception while building from v2 file", e);
    }
    return retVal;
  }

}
