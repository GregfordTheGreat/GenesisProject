package de.sereal.apps.genesisproject.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;
import android.util.SparseIntArray;

import java.nio.IntBuffer;

public class TextureHandler 
{
	private static Context context;
	
	public static SparseIntArray TextureHandles = new SparseIntArray();
	
	public static void Init(Context context) 
	{
		TextureHandler.context = context;
	}
	
	public static int GetTextureHandle(int resourceId)
	{
		int value = TextureHandles.get(resourceId);
		if(value <= 0)
			return -1;
		return value;
	}

	public static int LoadTexture(int resourceId)
	{
		return LoadTexture(resourceId,  GLES20.GL_CLAMP_TO_EDGE);
	}

	/**
	 * 
	 * @return id handle for this texture
	 */
	public static int LoadTexture(int resourceId, int wrap)
	{
		// check if we already have it
		int handle = GetTextureHandle(resourceId);
		if(handle >= 0)
			return handle;

		int[] textureHandle = new int[1];

		GLES20.glGenTextures(1, textureHandle, 0);
		if(textureHandle[0] != 0)
		{
			Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), resourceId);
			
			// Bind to the texture in OpenGL
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
			
			// Set filtering
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
	        
	        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, wrap);
          GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, wrap);
	        
	        // Load the bitmap into the bound texture.
	        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
			
	        // Recycle the bitmap, since its data has been loaded into OpenGL.
	        bmp.recycle();
	        
	        // register texture in our dictionary
	        TextureHandles.append(resourceId, textureHandle[0]);
			//Log.d("checking",""+GetTextureHandle(resourceId));
		}
		return textureHandle[0];
	}

  public static int GenerateViewTexture(int dimension)
  {
    int[] textureHandle = new int[1];
    GLES20.glGenTextures(1, textureHandle, 0);
    Log.d("generating",""+textureHandle[0]);

    if(textureHandle[0] <= 0)
      return -1;

    Bitmap bmp = Bitmap.createBitmap(dimension, dimension, Bitmap.Config.ARGB_8888);
    for(int h=0; h<dimension; h++)
      for(int w=0; w<dimension; w++)
        bmp.setPixel(h,w, 0xFF000000);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

    // Set filtering
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

    // Load the bitmap into the bound texture.
    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);

    // Recycle the bitmap, since its data has been loaded into OpenGL.
    bmp.recycle();


    return textureHandle[0];
  }

	public static int GenerateNoiseTexture(int dimension)
	{
		int[] textureHandle = new int[1];
		GLES20.glGenTextures(1, textureHandle, 0);
		Log.d("generating",""+textureHandle[0]);

		if(textureHandle[0] <= 0)
			return -1;

		byte[][] noise = TerrainGen.GeneratePerlinByteMap(dimension, dimension);

		int a,c;
		Bitmap bmp = Bitmap.createBitmap(dimension, dimension, Bitmap.Config.ARGB_8888);
		for(int y=0; y<dimension; y++)
			for(int x=0; x<dimension; x++)
			{
				a = noise[y][x];
				c  = (a << 24) + 0x00FFFFFF;
				bmp.setPixel(x, y, c);
			}
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

		// Set filtering
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

		// Load the bitmap into the bound texture.
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);

		// Recycle the bitmap, since its data has been loaded into OpenGL.
		bmp.recycle();
		return textureHandle[0];
	}

	public static void UpdateTexture(int handle, int size)
	{
		int b[]=new int[size*size];
		int bt[]=new int[size*size];
		IntBuffer ib = IntBuffer.wrap(b);
		ib.position(0);
		GLES20.glReadPixels(0, 0, size, size, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ib);

		for(int i=0, k=0; i<size; i++, k++)
		{//remember, that OpenGL bitmap is incompatible with Android bitmap
			//and so, some correction need.
			for(int j=0; j<size; j++)
			{
				int pix=b[i*size+j];
				int pb=(pix>>16)&0xff;
				int pr=(pix<<16)&0x00ff0000;
				int pix1=(pix&0xff00ff00) | pr | pb;
				bt[i*size+j]=pix1;
			}
		}

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, handle);
		Bitmap bmp = Bitmap.createBitmap(bt, size, size, Bitmap.Config.ARGB_8888);
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
	}

	public static int GenerateMaterialTexture(byte[][] values, Color4f color)
	{
		if(values == null || values.length == 0 || values[0].length == 0)
			return -1;

		int[] textureHandle = new int[1];
		GLES20.glGenTextures(1, textureHandle, 0);
		Log.d("generating",""+textureHandle[0]);

		if(textureHandle[0] <= 0)
			return -1;


		int h = values.length;
		int w = values[0].length;
		Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

		int a, r, g, b;
		int c;
    float v;
		for(int y=0; y<h; y++)
			for(int x=0; x<w; x++)
			{
        v = ((float) (values[y][x] & 0xFF)) / 255.0f;

        a = 222;
        r = (int)(  (v * color.r + (1.0f - v)) * 0xFF);
        g = (int)(  (v * color.g + (1.0f - v)) * 0xFF);
        b = (int)(  (v * color.b + (1.0f - v)) * 0xFF);

				c  = (a << 24) + (r << 16) + (g << 8) + b;
				bmp.setPixel(x, y, c);
			}

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

		// Set filtering
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

		// Load the bitmap into the bound texture.
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);

		// Recycle the bitmap, since its data has been loaded into OpenGL.
		bmp.recycle();

		return textureHandle[0];
	}

}
