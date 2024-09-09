package de.sereal.apps.genesisproject.obj;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;
import android.util.SparseArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Vector;

import de.sereal.apps.genesisproject.PlanetSurfaceRenderer;
import de.sereal.apps.genesisproject.util.FloatArrayList;
import de.sereal.apps.genesisproject.util.MyConstants;
import de.sereal.apps.genesisproject.util.ShortArrayList;
import de.sereal.apps.genesisproject.util.TextureHandler;
import de.sereal.apps.genesisproject.util.Vector3D;

/**
 * Created by sereal on 12.07.2017.
 */
public class WavefrontVBO extends VertexBufferObject
{
  private Vector<Integer> keys = new Vector<>();
  private SparseArray<Indices> VBOindicesByTextureId = new SparseArray<>();

  @Override
  public void DrawPositionOnly(int positionHandle)
  {
    GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
    GLES20.glEnableVertexAttribArray(positionHandle);

    Indices idx;
    for (int key : keys)
    {
      idx = VBOindicesByTextureId.get(key);
      GLES20.glDrawElements(GLES20.GL_TRIANGLES, idx.numberOfIndices, GLES20.GL_UNSIGNED_SHORT, idx.indexBuffer);
    }

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

    GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer);
    GLES20.glEnableVertexAttribArray(texCoordHandle);

    GLES20.glVertexAttribPointer(PlanetSurfaceRenderer.mShinyHandleDaytime, 1, GLES20.GL_FLOAT, false, 0, shininessBuffer);
    GLES20.glEnableVertexAttribArray(PlanetSurfaceRenderer.mShinyHandleDaytime);

    Indices idx;
    for (int key : keys)
    {
      idx = VBOindicesByTextureId.get(key);

      if(key != 0)
      {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glUniform1i(textureHandle, 1); 	// use GL_TEXTURE1
        GLES20.glUniform1i(texIDHandle, key);		// tell the shader, we want to use texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, key); 				// set the handle of the texture from LoadTexture
      }

      GLES20.glDrawElements(GLES20.GL_TRIANGLES, idx.numberOfIndices, GLES20.GL_UNSIGNED_SHORT, idx.indexBuffer);

      GLES20.glUniform1i(texIDHandle, 0);		// tell the shader, we're done using the texture and return to color mode
    }

    GLES20.glDisableVertexAttribArray(PlanetSurfaceRenderer.mShinyHandleDaytime);
    GLES20.glDisableVertexAttribArray(colorHandle);
    GLES20.glDisableVertexAttribArray(normalHandle);
    GLES20.glDisableVertexAttribArray(positionHandle);
    GLES20.glDisableVertexAttribArray(texCoordHandle);

    GLES20.glDisable(GLES20.GL_BLEND);
  }


  public static WavefrontVBO BuildFromWavefrontObj(Context context, int idMesh)
  {
    WavefrontVBO retVal = new WavefrontVBO();

    try
    {
      FloatArrayList vertices = new FloatArrayList();
      FloatArrayList normals = new FloatArrayList();
      FloatArrayList colors = new FloatArrayList();
      FloatArrayList texCoords = new FloatArrayList();
      FloatArrayList shininess = new FloatArrayList();

      // lists of indices, indexed by texture ID
      SparseArray<ShortArrayList> newVBOindicesByTextureId = new SparseArray<ShortArrayList>();
      ShortArrayList current = null;

      Vector<Vector3D> verticesOfObj = new Vector<>();
      Vector<Vector3D> normalsOfObj = new Vector<>();
      Vector<Vector3D> texCoordsOfObj = new Vector<>();

      BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(idMesh)));
      String line;
      String[] tokens, subtokens;
      Vector3D temp;
      int a;
      short vertexIndex=0;
      ObjMaterial activeMaterial = null;
      HashMap<String, ObjMaterial> materials = null;

      while ((line = reader.readLine()) != null)
      {
        // ignore comments
        if(line.startsWith("#"))
          continue;

        tokens = line.split(" ");

        switch(tokens[0])
        {
          case "mtllib": // extern material lib
            materials = ParseMaterialLibrary(context, tokens[1]);
            break;

          case "o": // start of object
          //  verticesOfObj = new Vector<>();
           // normalsOfObj = new Vector<>();
           // texCoordsOfObj = new Vector<>();
            break;

          case "v": // vertice
            verticesOfObj.add(new Vector3D(Float.parseFloat(tokens[1]) * MyConstants.TILE_SIZE, Float.parseFloat(tokens[2]) * MyConstants.TILE_SIZE, Float.parseFloat(tokens[3]) * MyConstants.TILE_SIZE));
            break;

          case "vn": // vertice normal
            normalsOfObj.add(new Vector3D(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])));
            break;

          case "vt": // vertice texture coord
            if(tokens.length == 3) texCoordsOfObj.add(new Vector3D(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), 0.0f));
            else
            if(tokens.length == 4) texCoordsOfObj.add(new Vector3D(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])));
            break;

          case "usemtl": // use material xxx
            if(materials != null && materials.containsKey(tokens[1]))
            {
              activeMaterial = materials.get(tokens[1]);

              // only if the active material has a texture, we'll load it
              if(activeMaterial.TextureResourceId != 0)
              {
                if((activeMaterial.TextureHandle = TextureHandler.GetTextureHandle(activeMaterial.TextureResourceId)) < 0)
                {
                  activeMaterial.TextureHandle = TextureHandler.LoadTexture(activeMaterial.TextureResourceId);
                }
              }

              current = newVBOindicesByTextureId.get(activeMaterial.TextureHandle);
              if(current == null)
              {
                current = new ShortArrayList();
                newVBOindicesByTextureId.append(activeMaterial.TextureHandle, current);
              }
            }
            else
            {
              activeMaterial = null;
            }
            break;

          case "f": // face
            if(tokens.length != 4)
              throw new IllegalArgumentException(line+">"+tokens.length);

            // for all 3 vertices
            for(a=1; a<=3; a++)
            {
              subtokens = tokens[a].split("/", -1);
              switch(subtokens.length)
              {
                case 1: // f 1 2 3
                  temp = verticesOfObj.get(Integer.parseInt(subtokens[0])-1);
                  vertices.Add(temp.x);
                  vertices.Add(temp.y);
                  vertices.Add(temp.z);

                  // no normals?
                  break;

                case 2: // f v1/vt1 v2/vt2 v3/vt3
                  temp = verticesOfObj.get(Integer.parseInt(subtokens[0])-1);
                  vertices.Add(temp.x);
                  vertices.Add(temp.y);
                  vertices.Add(temp.z);

                  temp = texCoordsOfObj.get(Integer.parseInt(subtokens[1])-1);
                  texCoords.Add(temp.x);
                  texCoords.Add(temp.y);

                  // no normals?
                  break;

                case 3:
                  // f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3
                  // f v1//vn1 v2//vn2 v3//vn3
                  int aa = Integer.parseInt(subtokens[0]);
                  if(aa > verticesOfObj.size()) {
                      Log.d("aa", verticesOfObj.size()+"line:"+line);
                  }
                  temp = verticesOfObj.get(Integer.parseInt(subtokens[0])-1);
                  vertices.Add(temp.x);
                  vertices.Add(temp.y);
                  vertices.Add(temp.z);

                  if(subtokens[1].length() > 0)
                  {
                    temp = texCoordsOfObj.get(Integer.parseInt(subtokens[1])-1);
                    texCoords.Add(temp.x);
                    texCoords.Add(1.0f-temp.y);
                  }

                  temp = normalsOfObj.get(Integer.parseInt(subtokens[2])-1);
                  normals.Add(temp.x);
                  normals.Add(temp.y);
                  normals.Add(temp.z);
                  break;
              }

              if(activeMaterial != null)
              {
                colors.Add(activeMaterial.DiffuseColor.x);
                colors.Add(activeMaterial.DiffuseColor.y);
                colors.Add(activeMaterial.DiffuseColor.z);
                colors.Add(activeMaterial.opacity);
                shininess.Add(activeMaterial.shininess);
              }

            }

            current.Add(vertexIndex);
            current.Add(vertexIndex+1);
            current.Add(vertexIndex+2);
            vertexIndex += 3;
            break;

          default: // unknown stuff
            break;
        }
      }

      Indices idx;
      for(a=0; a<newVBOindicesByTextureId.size(); a++)
      {
        int key = newVBOindicesByTextureId.keyAt(a);
        retVal.keys.add(key);

        idx = retVal.VBOindicesByTextureId.get(key);
        if(idx == null)
        {
          idx = new Indices();
          retVal.VBOindicesByTextureId.append(key, idx);
        }

        short[] elems = newVBOindicesByTextureId.get(key).ToArray();
        idx.indexBuffer = ByteBuffer.allocateDirect(elems.length * MyConstants.NumBytesPerShort).order(ByteOrder.nativeOrder()).asShortBuffer();
        idx.indexBuffer.put(elems);
        idx.indexBuffer.position(0);
        idx.numberOfIndices = elems.length;
      }

      retVal.vertexBuffer = ByteBuffer.allocateDirect(vertices.GetSize() * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertices.ToArray());
      retVal.vertexBuffer.position(0);

      retVal.normalBuffer = ByteBuffer.allocateDirect(normals.GetSize() * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(normals.ToArray());
      retVal.normalBuffer.position(0);

      retVal.colorBuffer = ByteBuffer.allocateDirect(colors.GetSize() * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(colors.ToArray());
      retVal.colorBuffer.position(0);

      retVal.texCoordBuffer = ByteBuffer.allocateDirect(texCoords.GetSize() * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(texCoords.ToArray());
      retVal.texCoordBuffer.position(0);

      retVal.shininessBuffer = ByteBuffer.allocateDirect(shininess.GetSize() * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(shininess.ToArray());
      retVal.shininessBuffer.position(0);

      retVal.valid = true;
    }
    catch (IOException ioex)
    {
      Log.e("Building", "Exception while building from wavefront obj file", ioex);
    }
    return retVal;
  }

  public static HashMap<String, ObjMaterial> ParseMaterialLibrary(Context context, String filename)
  {
    HashMap<String, ObjMaterial> retVal = new HashMap<>();
    String line;
    String[] tokens, subtokens;

    int idLib = context.getResources().getIdentifier(filename, "raw", context.getPackageName());
    BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(idLib)));
    ObjMaterial objMat = new ObjMaterial();

    try
    {
      while ((line = reader.readLine()) != null)
      {
        // ignore comments
        if (line.startsWith("#") || line.trim().isEmpty())
          continue;

        tokens = line.split(" ", -1);
        switch(tokens[0])
        {
          case "newmtl":
            objMat = new ObjMaterial();
            retVal.put(tokens[1], objMat);
            break;

          case "Ka":
            objMat.AmbientColor = new Vector3D(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3]));
            break;

          case "Kd":
            objMat.DiffuseColor = new Vector3D(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3]));
            break;

          case "Ks":
            objMat.SpecularColor = new Vector3D(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3]));
            break;

          case "d":
            objMat.opacity = Float.parseFloat(tokens[1]);
            break;

          case "Ns":
            objMat.shininess = Float.parseFloat(tokens[1]) / 1000.0f * 128.0f;

            break;

          case "map_Kd": // texture
            filename = tokens[1];
            objMat.TextureResourceId = context.getResources().getIdentifier(filename, "raw", context.getPackageName());
            break;
        }

      }
      objMat.shininess *= objMat.SpecularColor.x;

    }
    catch(Exception ex)
    {

    }
    return retVal;
  }

}
