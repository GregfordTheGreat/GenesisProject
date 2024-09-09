package de.sereal.apps.genesisproject.obj;

import android.util.Log;

import de.sereal.apps.genesisproject.util.Color4f;
import de.sereal.apps.genesisproject.util.Vector3D;

public class Tile 
{
	public final static int TILE_TYPE_FLAT = 0;
	public final static int TILE_TYPE_FLAT_UP_NE = 1;
	public final static int TILE_TYPE_FLAT_UP_SE = 2;
	public final static int TILE_TYPE_FLAT_UP_SW = 3;
	public final static int TILE_TYPE_FLAT_UP_NW = 4;
	public final static int TILE_TYPE_FLAT_DOWN_NE = 5;
	public final static int TILE_TYPE_FLAT_DOWN_SE = 6;
	public final static int TILE_TYPE_FLAT_DOWN_SW = 7;
	public final static int TILE_TYPE_FLAT_DOWN_NW = 8;
	public final static int TILE_TYPE_RAMP_N = 9;
	public final static int TILE_TYPE_RAMP_E = 10;
	public final static int TILE_TYPE_RAMP_S = 11;
	public final static int TILE_TYPE_RAMP_W = 12;
	public final static int TILE_TYPE_UP_NE = 13;
	public final static int TILE_TYPE_UP_SE = 14;
	public final static int TILE_TYPE_UP_SW = 15;
	public final static int TILE_TYPE_UP_NW = 16;

	public Vector3D P0;
	public Vector3D P1;
	public Vector3D P2;
	public Vector3D P3;
	public Color4f C0;
	public Color4f C1;
	public Color4f C2;
	public Color4f C3;
	public Color4f C4;
	public Color4f C5;
	public int TileType = TILE_TYPE_FLAT;
	public boolean mirrored;
	
	public Tile(float x, float z, float w, float h, float el, int[] heights)
	{
		P0 = new Vector3D(x,	heights[0]*el, z);
		P1 = new Vector3D(x, 	heights[1]*el, z+h);
		P2 = new Vector3D(x+w, 	heights[2]*el, z+h);
		P3 = new Vector3D(x+w, 	heights[3]*el, z);
		SetTileType(heights);
	//	SetColor(heights);
	}

	private void SetTileType(int[] HeightMap)
	{
		int d0 = HeightMap[1] - HeightMap[0];
		int d1 = HeightMap[2] - HeightMap[1];
		int d2 = HeightMap[3] - HeightMap[2];
		int d3 = HeightMap[0] - HeightMap[3];
		
		if(d0 ==  0 && d1 ==  0 && d2 ==  0 && d3 ==  0) TileType = TILE_TYPE_FLAT; else
  	    if(d0 ==  0 && d1 ==  0 && d2 ==  1 && d3 == -1) TileType = TILE_TYPE_FLAT_UP_NE; else
  	    if(d0 ==  0 && d1 ==  1 && d2 == -1 && d3 ==  0){ TileType = TILE_TYPE_FLAT_UP_SE; mirrored = true; } else
	    if(d0 ==  1 && d1 == -1 && d2 ==  0 && d3 ==  0) TileType = TILE_TYPE_FLAT_UP_SW; else
  	    if(d0 == -1 && d1 ==  0 && d2 ==  0 && d3 ==  1){ TileType = TILE_TYPE_FLAT_UP_NW; mirrored = true; } else
  	    if(d0 ==  0 && d1 ==  0 && d2 == -1 && d3 ==  1) TileType = TILE_TYPE_FLAT_DOWN_NE; else
	    if(d0 ==  0 && d1 == -1 && d2 ==  1 && d3 ==  0){ TileType = TILE_TYPE_FLAT_DOWN_SE; mirrored= true; } else
  	    if(d0 == -1 && d1 ==  1 && d2 ==  0 && d3 ==  0) TileType = TILE_TYPE_FLAT_DOWN_SW; else
	    if(d0 ==  1 && d1 ==  0 && d2 ==  0 && d3 == -1){ TileType = TILE_TYPE_FLAT_DOWN_NW; mirrored = true; } else
	    if(d0 == -1 && d1 ==  0 && d2 ==  1 && d3 ==  0) TileType = TILE_TYPE_RAMP_N; else
	    if(d0 ==  0 && d1 ==  1 && d2 ==  0 && d3 == -1) TileType = TILE_TYPE_RAMP_E; else
	    if(d0 ==  1 && d1 ==  0 && d2 == -1 && d3 ==  0) TileType = TILE_TYPE_RAMP_S; else
	    if(d0 ==  0 && d1 == -1 && d2 ==  0 && d3 ==  1) TileType = TILE_TYPE_RAMP_W; else
	    if(d0 == -1 && d1 ==  1 && d2 ==  1 && d3 == -1) TileType = TILE_TYPE_UP_NE; else
	    if(d0 ==  1 && d1 ==  1 && d2 == -1 && d3 == -1) TileType = TILE_TYPE_UP_SE; else
	    if(d0 ==  1 && d1 == -1 && d2 == -1 && d3 ==  1) TileType = TILE_TYPE_UP_SW; else
	    if(d0 == -1 && d1 == -1 && d2 ==  1 && d3 ==  1) TileType = TILE_TYPE_UP_NW; else
  	    {
  	    	Log.d("SetTileType","This case neeeds implementation: " + HeightMap[0]+" "+ HeightMap[1]+" "+ HeightMap[2]+" "+ HeightMap[3]);
  	    }
	}
	
	public void SetColor(Color4f normal, Color4f light, Color4f dark, Color4f seacolor, int[] hm)
	{
//		Color4f light = new Color4f(0.5f, 0.9f, 0.5f);
//		Color4f normal = new Color4f(0.5f, 0.85f, 0.5f);
//		Color4f dark = new Color4f(0.5f, 0.8f, 0.5f);
//		Color4f seacolor = new Color4f(0.5f, 0.5f, 0.8f);

		switch(TileType)
		{
			default:
			case TILE_TYPE_FLAT:
				C0 = C1 = C2 = C3 = C4 = C5 = ((hm[0] == 0) ? seacolor : normal);
				break;

			case TILE_TYPE_FLAT_UP_NE:
				C0 = C1 = C2 =  C4 = C5 = ((hm[0] == 0) ? seacolor : normal);
				C3 = light;
				break;

			case TILE_TYPE_FLAT_UP_SE:
				C0 = C1 = C3 = C4 = C5 = ((hm[0] == 0) ? seacolor : normal);
				C2 = dark;
				break;

			case TILE_TYPE_FLAT_UP_SW:
				C0 = C2 = C3 = C4 = C5 = ((hm[0] == 0) ? seacolor : normal);
				C1 = dark;
				break;

			case TILE_TYPE_FLAT_UP_NW:
				C1 = C2 = C3 = C4 = C5 = ((hm[1] == 0) ? seacolor : normal);
				C0 = light;
				break;

			case TILE_TYPE_FLAT_DOWN_NE:
				C0 = C1 = C2 = normal;
				C4 = C5 = dark;
				C3 = ((hm[3] == 0) ? seacolor : normal);
				break;

			case TILE_TYPE_FLAT_DOWN_SE:
				C0 = C1 = C3 = normal;
				C4 = C5 = light;
				C2 = ((hm[2] == 0) ? seacolor : normal);
				break;

			case TILE_TYPE_FLAT_DOWN_SW:
				C4 = C5 = C3 = normal;
				C0 = C2 = light;
				C1 = ((hm[1] == 0) ? seacolor : normal);
				break;

			case TILE_TYPE_FLAT_DOWN_NW:
				C4 = C2 = C5 = normal;
				C1 = C3 = dark;
				C0 = ((hm[0] == 0) ? seacolor : normal);
				break;

			case TILE_TYPE_RAMP_N:
				C0 = C3 = C4 = light;
				C1 = C2 = C5 = ((hm[1] == 0) ? seacolor : light);
				break;
			case TILE_TYPE_RAMP_E:
				C2 = C3 = C5 = normal;
				C0 = C1 = C4 = ((hm[1] == 0) ? seacolor : normal);
				break;
			case TILE_TYPE_RAMP_W:
				C0 = C1 = C4 = normal;
				C2 = C3 = C5 = ((hm[2] == 0) ? seacolor : normal);
				break;
			case TILE_TYPE_RAMP_S:
				C1 = C2 = C5 = dark;
				C0 = C3 = C4 = ((hm[0] == 0) ? seacolor : dark);
				break;

			case TILE_TYPE_UP_NE:
				C0 = C1 = C2 = C3 = C4 = C5 = light;
				break;
			case TILE_TYPE_UP_SE:
				C0 = C1 = C2 = C3 = C4 = C5 = dark;
				break;
			case TILE_TYPE_UP_SW:
				C0 = C1 = C2 = C3 = C4 = C5 = dark;
				break;
			case TILE_TYPE_UP_NW:
				C0 = C1 = C2 = C3 = C4 = C5 = light;
				break;
		}
	}
	
}
