package de.sereal.apps.genesisproject.obj;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class TexturedObject 
{
	public FloatBuffer vertexBuffer;
	public FloatBuffer colorBuffer;
	public FloatBuffer normalBuffer;
	public FloatBuffer texCoordBuffer;
	public ShortBuffer indexBuffer;
	public short numberOfIndices = 0;
	public int textureHandle1 = 0;

	public TexturedObject(int handle) 
	{
		vertexBuffer = ByteBuffer.allocateDirect(0).asFloatBuffer();
		colorBuffer = ByteBuffer.allocateDirect(0).asFloatBuffer();
		normalBuffer = ByteBuffer.allocateDirect(0).asFloatBuffer();
		texCoordBuffer = ByteBuffer.allocateDirect(0).asFloatBuffer();
		indexBuffer = ByteBuffer.allocateDirect(0).asShortBuffer();
		textureHandle1 = handle;
	}
}
