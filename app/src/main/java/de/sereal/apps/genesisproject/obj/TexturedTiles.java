package de.sereal.apps.genesisproject.obj;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;
import android.util.Log;
import android.util.SparseArray;

import de.sereal.apps.genesisproject.util.MyConstants;
import de.sereal.apps.genesisproject.util.TextureHandler;
import de.sereal.apps.genesisproject.util.TwoDimList;
import de.sereal.apps.genesisproject.util.MyConstants.Direction;

public class TexturedTiles 
{
	private SparseArray<TexturedObject> texturedObjects = new SparseArray<TexturedObject>();
	private TwoDimList<Indices> bufferIndices = new TwoDimList<Indices>();
	
	private class Indices
	{
		int index;
		int id;
	}
	
	public TexturedTiles() 
	{
	}
	
	public void RemoveSquare(int w, int h)
	{
		if(bufferIndices.HasKey(w, h))
		{
			Indices _indices = bufferIndices.Get(w, h);
			float[] oldValues;

		}
	}
	
	public void AddQuare(int id, int w, int h, float[] vertices, Direction direction)
	{
		TexturedObject obj = texturedObjects.get(id);
		if(obj == null)
		{
			obj = new TexturedObject(TextureHandler.GetTextureHandle(id));
			texturedObjects.append(id, obj);
		}
		
		Indices _indices;
		if(bufferIndices.HasKey(w, h))
		{
			_indices = bufferIndices.Get(w, h);
		}else{
			_indices = new Indices();
		}
		

		float[] oldValues;
		int oldsize;
		FloatBuffer tmp;
		int verticeCount;
		
		oldsize = obj.vertexBuffer.remaining();
		verticeCount = (oldsize / 3);
		oldValues = new float[oldsize + 12];
		obj.vertexBuffer.get(oldValues, 0, oldsize);
		
		tmp = ByteBuffer.allocateDirect(oldValues.length * MyConstants.NumBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
		tmp.put(oldValues, 0, oldsize);
		_indices.index = oldsize / 12;
		_indices.id = id;
		bufferIndices.Put(w, h, _indices);
		for(int a=0; a<12; a++)
		{
			if(a % 3 == 1)vertices[a] += 0.001f; 
			tmp.put(vertices[a]);
		}
		tmp.position(0);
		obj.vertexBuffer = tmp;
		
		// normals
		oldsize = obj.normalBuffer.remaining();
		oldValues = new float[oldsize + 12];
		obj.normalBuffer.get(oldValues, 0, oldsize);
		tmp = ByteBuffer.allocateDirect(oldValues.length * MyConstants.NumBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
		tmp.put(oldValues, 0, oldsize);
		for(int a=0; a<4; a++);
		{
			tmp.put(0.0f);
			tmp.put(1.0f);
			tmp.put(0.0f);
		}
		tmp.position(0);
		obj.normalBuffer = tmp;

		
		// texture coords
		oldsize = obj.texCoordBuffer.remaining();
		oldValues = new float[oldsize + 8];
		obj.texCoordBuffer.get(oldValues, 0, oldsize);
		tmp = ByteBuffer.allocateDirect(oldValues.length * MyConstants.NumBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
		tmp.put(oldValues, 0, oldsize);
		
		switch(direction)
		{
			case SOUTH:
				tmp.put(1.0f); tmp.put(1.0f);
				tmp.put(1.0f); tmp.put(0.0f);
				tmp.put(0.0f); tmp.put(0.0f);
				tmp.put(0.0f); tmp.put(1.0f);
				break;

			case WEST:
				tmp.put(1.0f); tmp.put(0.0f);
				tmp.put(0.0f); tmp.put(0.0f);
				tmp.put(0.0f); tmp.put(1.0f);
				tmp.put(1.0f); tmp.put(1.0f);
				break;
			
			case EAST:
				tmp.put(0.0f); tmp.put(1.0f);
				tmp.put(1.0f); tmp.put(1.0f);
				tmp.put(1.0f); tmp.put(0.0f);
				tmp.put(0.0f); tmp.put(0.0f);
				break;
			
			default:	
			case NORTH:
				tmp.put(0.0f); tmp.put(0.0f);
				tmp.put(0.0f); tmp.put(1.0f);
				tmp.put(1.0f); tmp.put(1.0f);
				tmp.put(1.0f); tmp.put(0.0f);
				break;
		}
		tmp.position(0);
		obj.texCoordBuffer = tmp;

		// colors
		oldsize = obj.colorBuffer.remaining();
		oldValues = new float[oldsize + 16];
		obj.colorBuffer.get(oldValues, 0, oldsize);
		tmp = ByteBuffer.allocateDirect(oldValues.length * MyConstants.NumBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer();
		tmp.put(oldValues, 0, oldsize);
		for(int a=0; a<16; a++)
			tmp.put(1.0f);
		tmp.position(0);
		obj.colorBuffer = tmp;

		// Indices
		oldsize = obj.indexBuffer.remaining();
		short[] oldValuesShort = new short[oldsize + 6];
		obj.indexBuffer.get(oldValuesShort, 0, oldsize);
		ShortBuffer tmpS = ByteBuffer.allocateDirect(oldValuesShort.length * MyConstants.NumBytesPerShort).order(ByteOrder.nativeOrder()).asShortBuffer();
		tmpS.put(oldValuesShort, 0, oldsize);
		tmpS.put((short)verticeCount);
		tmpS.put((short)(verticeCount + 1));
		tmpS.put((short)(verticeCount + 2));
		tmpS.put((short)verticeCount);
		tmpS.put((short)(verticeCount + 2));
		tmpS.put((short)(verticeCount + 3));
		tmpS.position(0);
		obj.indexBuffer = tmpS;
		
//		Log.d("Vertex 1", vertexBuffer.get(0)+"/"+vertexBuffer.get(1)+"/"+vertexBuffer.get(2));
//		Log.d("Vertex 2", vertexBuffer.get(3)+"/"+vertexBuffer.get(4)+"/"+vertexBuffer.get(5));
//		Log.d("Vertex 3", vertexBuffer.get(6)+"/"+vertexBuffer.get(7)+"/"+vertexBuffer.get(8));
//		Log.d("Vertex 4", vertexBuffer.get(9)+"/"+vertexBuffer.get(10)+"/"+vertexBuffer.get(11));
//		Log.d("TexCoord 1", texCoordBuffer.get(0)+"/"+texCoordBuffer.get(1));
//		Log.d("TexCoord 2", texCoordBuffer.get(2)+"/"+texCoordBuffer.get(3));
//		Log.d("TexCoord 3", texCoordBuffer.get(4)+"/"+texCoordBuffer.get(5));
//		Log.d("TexCoord 4", texCoordBuffer.get(6)+"/"+texCoordBuffer.get(7));
//		Log.d("Index 1", indexBuffer.get(0)+"/"+indexBuffer.get(1)+"/"+indexBuffer.get(2));
//		Log.d("Index 2", indexBuffer.get(3)+"/"+indexBuffer.get(4)+"/"+indexBuffer.get(5) +"-->"+textureHandle1);
		obj.numberOfIndices += 6;
	}

	public void Draw(int positionHandle, int colorHandle, int normalHandle, int texIDHandle, int textureHandle, int texCoordHandle)
	{
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_SRC_COLOR);
        GLES20.glUniform1i(textureHandle, 1);		// tell the shader, we want to use texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glUniform1i(texIDHandle, 1); 	// use GL_TEXTURE1
        
        int key;
        TexturedObject obj;
        for(int a=0; a<texturedObjects.size(); a++)
        {
					Log.d("TexTiles","Draw"+a);
        	key = texturedObjects.keyAt(a);
        	obj = texturedObjects.get(key);

        	GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, obj.textureHandle1); 				// set the handle of the texture from LoadTexture

					GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, obj.texCoordBuffer);
					GLES20.glEnableVertexAttribArray(texCoordHandle);

					GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, obj.vertexBuffer);
					GLES20.glEnableVertexAttribArray(positionHandle);

					GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, obj.colorBuffer);
					GLES20.glEnableVertexAttribArray(colorHandle);

					GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 0, obj.normalBuffer);
					GLES20.glEnableVertexAttribArray(normalHandle);

					GLES20.glDrawElements(GLES20.GL_TRIANGLES, obj.numberOfIndices, GLES20.GL_UNSIGNED_SHORT, obj.indexBuffer);

					GLES20.glDisableVertexAttribArray(colorHandle);
					GLES20.glDisableVertexAttribArray(normalHandle);
					GLES20.glDisableVertexAttribArray(positionHandle);
        }

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.

		GLES20.glUniform1i(texIDHandle, 0);		// tell the shader, we're done using the texture and return to color mode
		GLES20.glDisable(GLES20.GL_BLEND);

	}

}
