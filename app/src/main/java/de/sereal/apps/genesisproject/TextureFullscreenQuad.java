package de.sereal.apps.genesisproject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;

public class TextureFullscreenQuad 
{
	private FloatBuffer vertexBuffer;
	private FloatBuffer colorBuffer;
	private FloatBuffer textureBuffer;
	private ShortBuffer indexBuffer;
	
	public TextureFullscreenQuad(float width, float height)
	{
		float[] colors = new float[]{
				 1.0f, 1.0f, 1.0f, 1.0f,
				 1.0f, 1.0f, 1.0f, 1.0f,
				 1.0f, 1.0f, 1.0f, 1.0f,
				 1.0f, 1.0f, 1.0f, 1.0f
		};

		float[] vertices = new float[]{
				+1.77f, +1.0f,
				+1.77f, -1.0f,
				-1.77f, -1.0f,
				-1.77f, +1.0f
		};

		float[] texCoords = new float[]{
				 0.0f, 1.0f,
				 0.0f, 0.0f,
				 1.0f, 0.0f,
				 1.0f, 1.0f
		};
		short[] indices = new short[]{0, 1, 2, 0, 2, 3};
		
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

		ByteBuffer tbb =  ByteBuffer.allocateDirect(texCoords.length * 4);
		tbb.order(ByteOrder.nativeOrder());
		textureBuffer = tbb.asFloatBuffer();
		textureBuffer.put(texCoords);
		textureBuffer.position(0);

		ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 2);
		ibb.order(ByteOrder.nativeOrder());
		indexBuffer = ibb.asShortBuffer();
		indexBuffer.put(indices);
		indexBuffer.position(0);
	}
	
	public void Draw(int MVPhandle, float[] mvpmatrix, int positionHandle, int colorHandle, int texCoordHandle)
	{
		GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
	  GLES20.glEnableVertexAttribArray(positionHandle);

    GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);
    GLES20.glEnableVertexAttribArray(colorHandle);

    GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
    GLES20.glEnableVertexAttribArray(texCoordHandle);

		GLES20.glUniformMatrix4fv(MVPhandle, 1, false, mvpmatrix, 0);
    GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
	}
}
