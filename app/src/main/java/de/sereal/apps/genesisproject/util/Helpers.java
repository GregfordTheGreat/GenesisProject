package de.sereal.apps.genesisproject.util;

import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Vector;

import de.sereal.apps.genesisproject.obj.Triangle;

/**
 * Created by sereal on 14.08.2016.
 */
public class Helpers
{
  public static int[][] MatrixRotate90CW(int[][] mat) {
    final int M = mat.length;
    final int N = mat[0].length;
    int[][] ret = new int[N][M];
    for (int r = 0; r < M; r++) {
      for (int c = 0; c < N; c++) {
        ret[c][M-1-r] = mat[r][c];
      }
    }
    return ret;
  }

  public static int[][] MatrixRotate180(int[][] mat) {
    final int M = mat.length;
    final int N = mat[0].length;
    int[][] ret = new int[M][N];
    for (int r = 0; r < M; r++) {
      for (int c = 0; c < N; c++) {
        ret[r][c] = mat[M-r-1][N-c-1];
      }
    }
    return ret;
  }


  public static FloatBuffer CreateFloatBuffer(float[] abc)
  {
    FloatBuffer fb= ByteBuffer.allocateDirect(abc.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    fb.put(abc).position(0);
    return fb;
  }

  public static FloatBuffer AllocateFloatBuffer(int size)
  {
    FloatBuffer fb= ByteBuffer.allocateDirect(size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    fb.position(0);
    return fb;
  }

  public static ShortBuffer CreateShortBuffer(short[] abc)
  {
    ShortBuffer sb= ByteBuffer.allocateDirect(abc.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
    sb.put(abc).position(0);
    return sb;
  }

  public static ShortBuffer AllocateShortBuffer(int size)
  {
    ShortBuffer sb= ByteBuffer.allocateDirect(size * 4).order(ByteOrder.nativeOrder()).asShortBuffer();
    sb.position(0);
    return sb;
  }

  public static Ray GetUnprojectedRay(int x, int y, int[] viewport, float[] mMVPMatrix)
  {
    Ray ray = new Ray();
    ray.P0 = unProject(x, y, 1.0f, mMVPMatrix, viewport);
    ray.P1 = unProject(x, y, 100.0f, mMVPMatrix, viewport);
    return ray;
  }

  private static Vector3D unProject(float winx, float winy, float winz, float[] resultantMatrix, int[] viewport)
  {
    // viewport[2] = screen width
    // viewport[3] = screen height
    float[] m = new float[16],
            in = new float[4],
            out = new float[4];

    winy = viewport[3] - winy;
    Matrix.invertM(m, 0, resultantMatrix, 0);

    in[0] = (winx / (float)viewport[2]) * 2 - 1;
    in[1] = (winy / (float)viewport[3]) * 2 - 1;
    in[2] = 2 * winz - 1;
    in[3] = 1;

    Matrix.multiplyMV(out, 0, m, 0, in, 0);

    if (out[3]==0)
      return null;

    out[3] = 1/out[3];
    return new Vector3D(out[0] * out[3], out[1] * out[3], out[2] * out[3]);
  }


  public static boolean RayTriangleIntersect(Ray ray, Triangle triangle)
  {
    Vector3D u = triangle.GetVectorA();
    Vector3D v = triangle.GetVectorB();
    Vector3D n = Vector3D.Cross(u, v);

    if(n.IsZero()) // triangle is degenerate
    {
//			Log.e("n is zero",":(");
      return false;
    }

    Vector3D dir = ray.GetVector();
    Vector3D w0 = ray.P0.Substract(triangle.P0);

    float a = -(n.Dot(w0));
    float b = n.Dot(dir);
    if(Math.abs(b) < 0.0000001f)
    {
//			Log.e("ray is parallel",":(");
      return false;
    }

    float r = a / b;
    if(r < 0.0f)
    {
//			Log.e("ray goes away",":(");
      return false;
    }

    Vector3D I = ray.P0.Add(dir.Mult(r));
//		Log.d("I:","x:"+I.x+ " y:"+I.y+" z:"+I.z);

    // I inside T?
    float    uu, uv, vv, wu, wv, D;
    uu = u.Dot(u);
    uv = u.Dot(v);
    vv = v.Dot(v);
    Vector3D w = I.Substract(triangle.P0);
    wu = w.Dot(u);
    wv = w.Dot(v);

    D = uv * uv - uu * vv;
    float s, t;

    s = (uv * wv - vv * wu) / D;
    if (s < 0.0 || s > 1.0)         // I is outside T
    {
//	    	Log.d("outside","T");
      return false;
    }

    t = (uv * wu - uu * wv) / D;
    if (t < 0.0 || (s + t) > 1.0)  // I is outside T
    {
//	    	Log.d("outside","T");
      return false;
    }

//	    Log.d("inside","T");
    return true;
  }

  public static boolean HasBoundingboxHit(Ray ray, Vector<Vector3D> boundingBox)
  {
    Triangle triangle = new Triangle();
    int[][] indices = new int[][]{ {0,1,2,3}, {0,4,5,1}, {1,5,6,2}, {2,6,7,3}, {3,7,4,0}};

    for(int a=0; a<indices.length; a++)
    {
      triangle.P0 = boundingBox.get(indices[a][0]);
      triangle.P1 = boundingBox.get(indices[a][1]);
      triangle.P2 = boundingBox.get(indices[a][2]);

      if(RayTriangleIntersect(ray, triangle))
      {
        return true;
      }

      triangle.P1 = triangle.P2;
      triangle.P2 = boundingBox.get(indices[a][3]);

      if(RayTriangleIntersect(ray, triangle))
      {
        return true;
      }
    }
    return false;
  }


  public static MyConstants.Direction GetDirection(PathNode start, PathNode end)
  {
    int dx = end.x - start.x;
    int dy = end.y - start.y;
    if(dx == -1) return MyConstants.Direction.WEST;
    if(dy == -1) return MyConstants.Direction.NORTH;
    if(dy == 1) return MyConstants.Direction.SOUTH;
    return MyConstants.Direction.EAST;
  }



}
