package de.sereal.apps.genesisproject.obj;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Vector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLES20;
import android.util.Log;

import de.sereal.apps.genesisproject.PlanetSurfaceRenderer;
import de.sereal.apps.genesisproject.R;
import de.sereal.apps.genesisproject.util.MyConstants;
import de.sereal.apps.genesisproject.util.Point3D;
import de.sereal.apps.genesisproject.util.TextureHandler;

public class Icosphere 
{
	private float WORLD_RADIUS = 2.0f;
	private float WORLD_ELEVATION = 0.2f;

	private FloatBuffer vertexBuffer;
	private FloatBuffer colorBuffer;
	private ShortBuffer indexBuffer;
	private FloatBuffer normalBuffer;
	private FloatBuffer texCoordBuffer;
	private int lines;

	private final float[] color = new float[] {1.0f, 1.0f, 1.0f, 1.0f};
	private int textureID = -1;

	public Icosphere(float Radius, boolean backfaced)
	{
		WORLD_RADIUS = Radius;

		Vector<NormalizedTriangle> triangles = new Vector<>();

		float t = (1.0f + (float)Math.sqrt(5.0f)) / 2.0f;
		triangles.addElement(
				new NormalizedTriangle(
						new Point3D(-1, t, 0), // 0
						new Point3D(-t, 0, 1), // 11
						new Point3D(0, 1, t)   // 5
						, true)
		);
		triangles.addElement(
				new NormalizedTriangle(
						new Point3D(-1, t, 0).Normalize(),	// 0
						new Point3D(0, 1, t).Normalize(),	// 5
						new Point3D(1, t, 0).Normalize(),	// 1
						true)
		);
		triangles.addElement(
				new NormalizedTriangle(
						new Point3D(-1, t, 0),	// 0
						new Point3D(1, t, 0),	// 1
						new Point3D(0, 1, -t),	// 7
						true)
		);
		triangles.addElement(
				new NormalizedTriangle(
						new Point3D(-1, t, 0),	// 0
						new Point3D(0, 1, -t),	// 7
						new Point3D(-t, 0, -1),	// 10
						true)
		);
		triangles.addElement(
				new NormalizedTriangle(
						new Point3D(-1, t, 0),	// 0
						new Point3D(-t, 0, -1),	// 10
						new Point3D(-t, 0, 1),	// 11
						true)
		);
		
		
		triangles.addElement(
				new NormalizedTriangle(
						new Point3D(1, t, 0),	// 1
						new Point3D(0, 1, t),	// 5
						new Point3D(t, 0, 1),   	// 9
						true)
		);
		triangles.addElement(
				new NormalizedTriangle(
						new Point3D(0, 1, t),	// 5
						new Point3D(-t, 0, 1),	// 11
						new Point3D(0, -1, t),	// 4
						true)
		);
		triangles.addElement(
				new NormalizedTriangle(
						new Point3D(-t, 0, 1),	// 11
						new Point3D(-t, 0, -1),	// 10
						new Point3D(-1, -t, 0),	// 2
						true)
		);
		triangles.addElement(
				new NormalizedTriangle(
						new Point3D(-t, 0, -1),	// 10
						new Point3D(0, 1, -t),	// 7
						new Point3D(0, -1, -t),	// 6
						true)
		);
		triangles.addElement(
				new NormalizedTriangle(
						new Point3D(0, 1, -t),	// 7
						new Point3D(1, t, 0),	// 1
						new Point3D(t,  0, -1),	// 8
						true)
		);
		
		triangles.addElement(
				new NormalizedTriangle(
						new Point3D(1, -t,  0),	// 3
						new Point3D(t,  0,  1),	// 9
						new Point3D(0, -1,  t),	// 4
						true)
		);
		triangles.addElement(
				new NormalizedTriangle(
						new Point3D(1, -t,  0),	// 3
						new Point3D(0, -1,  t),	// 4
						new Point3D(-1, -t,  0),	// 2 
						true)
		);
		triangles.addElement(
				new NormalizedTriangle(
						new Point3D(1, -t,  0),		// 3
						new Point3D(-1, -t,  0),	// 2
						new Point3D(0, -1, -t),		// 6
						true)
		);
		triangles.addElement(
				new NormalizedTriangle(
						new Point3D(1, -t,  0),	// 3
						new Point3D(0, -1, -t),	// 6 
						new Point3D(t,  0, -1),	// 8
						true)
		);
		triangles.addElement(
				new NormalizedTriangle(
						new Point3D(1, -t,  0),	// 3
						new Point3D(t,  0, -1),	// 8
						new Point3D(t,  0,  1),	// 9
						true)
		);

		triangles.addElement(
				new NormalizedTriangle(
						new Point3D(0, -1,  t),	// 4
						new Point3D(t,  0,  1),	// 9
						new Point3D(0,  1,  t),	// 5
						true)
		);
		triangles.addElement(
				new NormalizedTriangle(
						new Point3D(-1, -t,  0),	// 2
						new Point3D(0, -1,  t),		// 4
						new Point3D(-t,  0,  1),		// 11
						true)
		);
		triangles.addElement(
				new NormalizedTriangle(
						new Point3D(0, -1, -t),		// 6
						new Point3D(-1, -t,  0),	// 2
						new Point3D(-t,  0, -1),		// 10
						true)
		);
		triangles.addElement(
				new NormalizedTriangle(
						new Point3D(t,  0, -1),	// 8
						new Point3D(0, -1, -t),	// 6
						new Point3D(0,  1, -t),	// 7
						true)
		);
		triangles.addElement(
				new NormalizedTriangle(
						new Point3D(t,  0,  1),	// 9
						new Point3D(t,  0, -1),	// 8
						new Point3D(1,  t,  0),	// 1
						true)
		);
		
		NormalizedTriangle tria;
		Point3D m0, m1, m2;
		int num;
		for(int rec = 0; rec < 4; rec++)
		{
			num = triangles.size();
			for(int a=0; a<num; a++)
			{
				tria = triangles.remove(0);
				m0 = getMiddlePoint(tria.P0, tria.P1);
				m1 = getMiddlePoint(tria.P1, tria.P2);
				m2 = getMiddlePoint(tria.P2, tria.P0);
				triangles.addElement(new NormalizedTriangle(tria.P0, m0, m2, true));
				triangles.addElement(new NormalizedTriangle(m0, tria.P1, m1, true));
				triangles.addElement(new NormalizedTriangle(m1, tria.P2, m2, true));
				triangles.addElement(new NormalizedTriangle(m0, m1, m2, true));
			}
		}
		
		
		float[] vertices = new float[triangles.size() * 3 * MyConstants.NumFloatPerVertex];
		float[] normals = new float[triangles.size() * 3 * MyConstants.NumFloatPerVertex];
		float[] colors = new float[triangles.size() * 3 * MyConstants.NumFloatPerColor];
		float[] texCoords = new float[triangles.size() * 3 * MyConstants.NumFloatPerTexCoord];
		short[] indices = new short[triangles.size() * 3];
		
		int index = 0;

		float[] uv;

		int seamMinCnt;
		int seamMaxCnt;
		for(int tr=0; tr<triangles.size(); tr++)
		{

			tria = triangles.get(tr);

			uv = getTextureCoord(tria.P0);
			texCoords[tr * 6 + 0] = uv[0];
			texCoords[tr * 6 + 1] = uv[1];

			uv = getTextureCoord(tria.P1);
			texCoords[tr * 6 + 2] = uv[0];
			texCoords[tr * 6 + 3] = uv[1];

			uv = getTextureCoord(tria.P2);
			texCoords[tr * 6 + 4] = uv[0];
			texCoords[tr * 6 + 5] = uv[1];

			seamMinCnt = 0;
			seamMaxCnt = 0;
			for(int a=0; a<3; a++)
			{
				if(texCoords[tr * 6 + (a*2)] < 0.1f) seamMinCnt++; else
				if(texCoords[tr * 6 + (a*2)] > 0.9f) seamMaxCnt++;
			}

			if((seamMaxCnt == 1 && seamMinCnt == 2) || (seamMaxCnt == 2 && seamMinCnt == 1))
			{
				Log.d("fixing seam","A&B");
				for(int a=0; a<3; a++)
				{
					if(texCoords[tr * 6 + (a*2)] > 0.9f) texCoords[tr * 6 + (a*2)] = 0.0f;
				}
			}

			vertices[tr * 9 + 0] = tria.P0.X * WORLD_RADIUS;
			vertices[tr * 9 + 1] = tria.P0.Y * WORLD_RADIUS;
			vertices[tr * 9 + 2] = tria.P0.Z * WORLD_RADIUS;
			
			vertices[tr * 9 + 3] = tria.P1.X * WORLD_RADIUS;
			vertices[tr * 9 + 4] = tria.P1.Y * WORLD_RADIUS;
			vertices[tr * 9 + 5] = tria.P1.Z * WORLD_RADIUS;
			
			vertices[tr * 9 + 6] = tria.P2.X * WORLD_RADIUS;
			vertices[tr * 9 + 7] = tria.P2.Y * WORLD_RADIUS;
			vertices[tr * 9 + 8] = tria.P2.Z * WORLD_RADIUS;

			colors[tr * 12 + 0] = 1.0f;
			colors[tr * 12 + 1] = 1.0f;
			colors[tr * 12 + 2] = 1.0f;
			colors[tr * 12 + 3] = 1.0f;

			colors[tr * 12 + 4] = 1.0f;
			colors[tr * 12 + 5] = 1.0f;
			colors[tr * 12 + 6] = 1.0f;
			colors[tr * 12 + 7] = 1.0f;
			
			colors[tr * 12 + 8] = 1.0f;
			colors[tr * 12 + 9] = 1.0f;
			colors[tr * 12 + 10] = 1.0f;
			colors[tr * 12 + 11] = 1.0f;

			indices[index + 0] = (short)(index);
			if(backfaced)
			{
				indices[index + 1] = (short)(index+2);
				indices[index + 2] = (short)(index+1);
			}else{
				indices[index + 1] = (short)(index+1);
				indices[index + 2] = (short)(index+2);
			}
			index += 3;

			
			normals[tr * 9 + 0] = tria.P0.X;
			normals[tr * 9 + 1] = tria.P0.Y;
			normals[tr * 9 + 2] = tria.P0.Z; 
			
			normals[tr * 9 + 3] = tria.P1.X;
			normals[tr * 9 + 4] = tria.P1.Y;
			normals[tr * 9 + 5] = tria.P1.Z;
			
			normals[tr * 9 + 6] = tria.P2.X;
			normals[tr * 9 + 7] = tria.P2.Y;
			normals[tr * 9 + 8] = tria.P2.Z;
		}
		lines = index;

		// a float is 4 bytes, therefore we multiply the number if vertices with 4.
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		vertexBuffer = vbb.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);
		
		// a float is 4 bytes, therefore we multiply the number if vertices with 4.
		ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
		cbb.order(ByteOrder.nativeOrder());
		colorBuffer = cbb.asFloatBuffer();
		colorBuffer.put(colors);
		colorBuffer.position(0);

		ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 2);
		ibb.order(ByteOrder.nativeOrder());
		indexBuffer = ibb.asShortBuffer();
		indexBuffer.put(indices);
		indexBuffer.position(0);
		
		// a float is 4 bytes, therefore we multiply the number if vertices with 4.
		ByteBuffer nbb = ByteBuffer.allocateDirect(normals.length * 4);
		nbb.order(ByteOrder.nativeOrder());
		normalBuffer = nbb.asFloatBuffer();
		normalBuffer.put(normals);
		normalBuffer.position(0);

		// a float is 4 bytes, therefore we multiply the number if vertices with 4.
		ByteBuffer tbb = ByteBuffer.allocateDirect(texCoords.length * 4);
		tbb.order(ByteOrder.nativeOrder());
		texCoordBuffer = tbb.asFloatBuffer();
		texCoordBuffer.put(texCoords);
		texCoordBuffer.position(0);
	}

	// buggy, cant just add height
	public void SetHeightMap(Context context, float Elevation)
	{
		WORLD_ELEVATION = Elevation;

		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.raw.heightmap);
		int hm_height = bmp.getHeight();
		int hm_width = bmp.getWidth();

		float h;
		float x,y,z;
		int px, py;

		int verticeCount = vertexBuffer.capacity() / 3;
		int verticeIndex, texIndex;
		for(int a=0; a<verticeCount; a++)
		{
			verticeIndex = a * 3;
			x = vertexBuffer.get(verticeIndex);
			y = vertexBuffer.get(verticeIndex+1);
			z = vertexBuffer.get(verticeIndex+2);

			texIndex = a * 2;
			px = (int)((hm_width-1) * texCoordBuffer.get(texIndex));
			py = (int)((hm_height-1) * texCoordBuffer.get(texIndex+1));
			h = (float)Color.red(bmp.getPixel(px, py)) / 255.0f * WORLD_ELEVATION;

			vertexBuffer.put(verticeIndex  , x + h);
			vertexBuffer.put(verticeIndex+1, y + h);
			vertexBuffer.put(verticeIndex+2, z + h);
		}

	}

	public void SetTexture(int ResourceID)
	{
		textureID = TextureHandler.LoadTexture(ResourceID);
		Log.d("SetTexture",""+textureID);
	}


	private float[] getTextureCoord(Point3D p)
	{
//	    float targetU;
//	    float targetV;
//	    float normalisedX = 0;
//	    float normalisedZ = -1;
//	    if (((p.X * p.X) + (p.Z * p.Z)) > 0) {
//	      normalisedX = (float)Math.sqrt((p.X * p.X) / ((p.X * p.X) + (p.Z * p.Z)));
//	      if (p.X < 0) { normalisedX = -normalisedX; }
//	      normalisedZ = (float)Math.sqrt((p.Z * p.Z) / ((p.X * p.X) + (p.Z * p.Z)));
//	      if (p.Z < 0) { normalisedZ = -normalisedZ; }
//	    }
//	    if (normalisedZ == 0) {
//	      targetU = (normalisedX * (float)Math.PI) / 2.0f;
//	    } else {
//	      targetU = (float)Math.atan(normalisedX / normalisedZ);
//	      if (normalisedZ < 0) { targetU += (float)Math.PI; }
//	    }
//	    if (targetU < 0) { targetU += 2.0f * (float)Math.PI; }
//	    targetU /= 2.0f * (float)Math.PI;
//	    targetV = (-p.Y + 1.0f) / 2.0f;
//	    return new float[] { targetU, targetV };
		
		double u,v;
		
		u = 1.0f - (Math.atan2(p.Z, p.X) / (2.0f * Math.PI) + 0.5f);
		v = 1.0f - ((Math.asin(p.Y) / Math.PI) + 0.5f);
		if(u == 0.0) u = 1.0;
		return new float[] { (float)u, (float)v };
	}

	
	private Point3D getMiddlePoint(Point3D p1, Point3D p2)
	{
		Point3D middle = new Point3D(
				(p1.X + p2.X) / 2.0f,
				(p1.Y + p2.Y) / 2.0f,
				(p1.Z + p2.Z) / 2.0f);
		return middle;
	}

	public void SetColor(float[] rgba)
	{
		int index = 0;
		for(int a=0; a<lines; a++, index+= 4)
		{
			colorBuffer.put(index + 0, rgba[0]);
			colorBuffer.put(index + 1, rgba[1]);
			colorBuffer.put(index + 2, rgba[2]);
			colorBuffer.put(index + 3, rgba[3]);
		}
	//	color = rgba;
	}


	public void Draw(int positionHandle, int colorHandle, int normalHandle, int texIDHandle, int textureHandle, int texCoordHandle)
	{
		GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
		GLES20.glEnableVertexAttribArray(positionHandle);
		
		GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);
    GLES20.glEnableVertexAttribArray(colorHandle);
//		GLES20.glVertexAttrib4fv(colorHandle, color, 0);

		GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 0, normalBuffer);
		GLES20.glEnableVertexAttribArray(normalHandle);


		// if we have a texture assigned and the uniform handle is set
		if(textureID >= 0 && texIDHandle >= 0)
		{

			GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
			GLES20.glUniform1i(textureHandle, 1); 	// use GL_TEXTURE1

			GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0,texCoordBuffer);
			GLES20.glEnableVertexAttribArray(texCoordHandle);

			GLES20.glUniform1i(texIDHandle, textureID);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
		}

		GLES20.glDrawElements(GLES20.GL_TRIANGLES, lines, GLES20.GL_UNSIGNED_SHORT, indexBuffer);


		GLES20.glUniform1i(texIDHandle, 0);		// tell the shader, we're done using the texture and return to color mode
		GLES20.glDisableVertexAttribArray(texCoordHandle);
		GLES20.glDisableVertexAttribArray(colorHandle);
		GLES20.glDisableVertexAttribArray(normalHandle);
		GLES20.glDisableVertexAttribArray(positionHandle);
	}

}
