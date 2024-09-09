package de.sereal.apps.genesisproject.obj;

import android.util.Log;

import java.nio.FloatBuffer;
import java.util.Vector;

import de.sereal.apps.genesisproject.util.MyConstants;
import de.sereal.apps.genesisproject.util.Vector3D;

/**
 * Created by sereal on 12.07.2017.
 */
public abstract class VertexBufferObject
{
  public abstract void DrawPositionOnly(int positionHandle);
  public abstract void Draw(int positionHandle, int colorHandle, int normalHandle, int texIDHandle, int textureHandle, int texCoordHandle);

  public FloatBuffer vertexBuffer;
  public FloatBuffer colorBuffer;
  public FloatBuffer normalBuffer;
  public FloatBuffer texCoordBuffer;
  public FloatBuffer shininessBuffer;
  public boolean valid = false;

  public Vector<Vector3D> getBoundingBox()
  {
    Vector<Vector3D> BoundingBox = new Vector<>();
    float x,y,z;
    float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
    float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
    float minZ = Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;

    for(int a=0; a<vertexBuffer.capacity(); a += 3)
    {
      x = vertexBuffer.get(a  );
      y = vertexBuffer.get(a+1);
      z = vertexBuffer.get(a+2);

      minX = Math.min(minX, x);
      maxX = Math.max(maxX, x);
      minY = Math.min(minY, y);
      maxY = Math.max(maxY, y);
      minZ = Math.min(minZ, z);
      maxZ = Math.max(maxZ, z);
    }

    BoundingBox.addElement(new Vector3D(minX, maxY, minZ)); // top points
    BoundingBox.addElement(new Vector3D(maxX, maxY, minZ));
    BoundingBox.addElement(new Vector3D(maxX, maxY, maxZ));
    BoundingBox.addElement(new Vector3D(minX, maxY, maxZ));

    BoundingBox.addElement(new Vector3D(minX, minY, minZ)); // bottom points
    BoundingBox.addElement(new Vector3D(maxX, minY, minZ));
    BoundingBox.addElement(new Vector3D(maxX, minY, maxZ));
    BoundingBox.addElement(new Vector3D(minX, minY, maxZ));

    return BoundingBox;
  }

  public void SetPosition(float x, float y, float z)
  {
    for(int a=0; a<vertexBuffer.capacity(); a += 3)
    {
      vertexBuffer.put(a  , vertexBuffer.get(a  ) + x);
      vertexBuffer.put(a+1, vertexBuffer.get(a+1) + y);
      vertexBuffer.put(a+2, vertexBuffer.get(a+2) + z);
    }
  }

  public void SetDirection(MyConstants.Direction direction)
  {
    float x, z;

    switch(direction)
    {
      case EAST: // 90 deg counter clockwise
        for(int a=0; a<vertexBuffer.capacity(); a += 3)
        {
          x = vertexBuffer.get(a);
          z = vertexBuffer.get(a+2);
          vertexBuffer.put(a  , z);
          vertexBuffer.put(a+2, -x);

          x = normalBuffer.get(a);
          z = normalBuffer.get(a+2);
          normalBuffer.put(a  , z);
          normalBuffer.put(a+2, -x);
        }
        break;

      case NORTH: // 180
        Log.d("NORTH","2");
        for(int a=0; a<vertexBuffer.capacity(); a += 3)
        {
          x = vertexBuffer.get(a);
          z = vertexBuffer.get(a+2);
          vertexBuffer.put(a  , -x);
          vertexBuffer.put(a+2, -z);

          x = normalBuffer.get(a);
          z = normalBuffer.get(a+2);
          normalBuffer.put(a  , -x);
          normalBuffer.put(a+2, -z);
        }
        break;

      case WEST: // 90 deg clockwise
        for(int a=0; a<vertexBuffer.capacity(); a += 3)
        {
          x = vertexBuffer.get(a);
          z = vertexBuffer.get(a+2);
          vertexBuffer.put(a  , -z);
          vertexBuffer.put(a+2, x);

          x = normalBuffer.get(a);
          z = normalBuffer.get(a+2);
          normalBuffer.put(a  , -z);
          normalBuffer.put(a+2, x);
        }
        break;
    }
  }


}
