package de.sereal.apps.genesisproject.util;

public class MyConstants 
{
	public final static float TwoPi = (float)Math.PI * 2.0f;
	public final static int FloatSize = 4;
	public final static int NumFloatPerVertex = 3;
	public final static int NumFloatPerNormal = 3;
	public final static int NumFloatPerColor = 4;
	public final static int NumFloatPerTexCoord = 2;

	public final static int NumBytesPerShort = 2;
	public final static int NumBytesPerFloat = 4;

	public final static int NumVerticesPerQuad = 6;
	public final static int NumVerticesPerTriangle = 3;

	public final static float TILE_SIZE = 0.25f;
	public final static float HEIGHT_STEP = 0.125f;

	public enum Direction
	{
		SOUTH(0),
		EAST(1),
		NORTH(2),
		WEST(3);

		private int value;
		Direction(int value){ this.value = value; }
		public int getValue(){ return value; }

		public static Direction parse(int value) {
			for (Direction d : values()) {
				if(d.value == value)
					return d;
			}
			return null;
		}
	}

	public enum ResourceMapStyle
	{
		NORMAL,
		RARE_EARTH,
		CORE_ICE
	}
}
