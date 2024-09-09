package de.sereal.apps.genesisproject.util;

import java.util.Random;
import java.util.Vector;

import android.util.Log;

public class TerrainGen 
{
	private static Random rand = new Random(System.currentTimeMillis());
	private static Vector<QueueItem> queue = new Vector<QueueItem>();
	private int[][] HeightMap;
	public static int OverSeaLevels = 5;

	public TerrainGen()
	{

	}

	public static int[][] GeneratePerlin(int width, int height) {
		TerrainGen tg = new TerrainGen();
		return tg.generatePerlin(width, height);
	}
	
	public static byte[][] GeneratePerlinByteMap(int width, int height)
	{
		float[][] noise = GenerateWhiteNoise(width, height);
		noise = GeneratePerlinNoise(noise, 4);

		byte[][] map = new byte[height][width];

		for(int h=0; h<height; h++)
			for(int w=0; w<width; w++)
				map[h][w] = (byte)(noise[h][w] * 0xFF);

		return map;
	}

	private static float[][] GenerateWhiteNoise(int width, int height)
	{
		float[][] noise = new float[height][width];
		
		for(int h=0; h<height; h++)
		{
			for(int w=0; w<width; w++)
			{
				noise[h][w] = rand.nextFloat();
			}
		}
		return noise;
	}
	
	private static float[][] GenerateSmoothNoise(float[][] baseNoise, int octave)
	{
		int width = baseNoise.length;
		int height = baseNoise[0].length;

		float[][] smoothNoise = new float[height][width];
		int samplePeriod = 1 << octave;
		float sampleFrequency = 1.0f / samplePeriod;
	
		for(int h=0; h<height; h++)
		{
			int sample_i0 = (h / samplePeriod) * samplePeriod;
			int sample_i1 = (sample_i0 + samplePeriod) % width;
			float horizontal_blend = (h - sample_i0) * sampleFrequency;
			
			for(int w=0; w<width; w++)
			{
				int sample_j0 = (w / samplePeriod) * samplePeriod;
				int sample_j1 = (sample_j0 + samplePeriod) % height; //wrap around
				float vertical_blend = (w - sample_j0) * sampleFrequency;
				
				//blend the top two corners
				float top = Interpolate(baseNoise[sample_i0][sample_j0], baseNoise[sample_i1][sample_j0], horizontal_blend);
				
				//blend the bottom two corners
				float bottom = Interpolate(baseNoise[sample_i0][sample_j1], baseNoise[sample_i1][sample_j1], horizontal_blend);
				
				//final blend
				smoothNoise[h][w] = Interpolate(top, bottom, vertical_blend);
			}
		}
		return smoothNoise;
	}
	
	private static float[][] GeneratePerlinNoise(float[][] baseNoise, int octaveCount)
	{
		int width = baseNoise.length;
		int height = baseNoise[0].length;
		 
		float[][][] smoothNoise = new float[octaveCount][][]; //an array of 2D arrays containing
		 
		float persistance = 0.1f;
		 
		//generate smooth noise
		for (int i = 0; i < octaveCount; i++)
		{
			smoothNoise[i] = GenerateSmoothNoise(baseNoise, i);
		}
		 
		float[][] perlinNoise = new float[height][width];
		float amplitude = 1.0f;
		float totalAmplitude = 0.0f;
		 
		//blend noise together
		for (int octave = octaveCount - 1; octave >= 0; octave--)
		{
			amplitude *= persistance;
			totalAmplitude += amplitude;
			 
			for (int h = 0; h < height; h++)
			{
				for (int w = 0; w < width; w++)
				{
					perlinNoise[h][w] += smoothNoise[octave][h][w] * amplitude;
				}
			}
		}
		 
		//normalisation
		for (int h = 0; h < height; h++)
		{
			for (int w = 0; w < width; w++)
			{
				perlinNoise[h][w] /= totalAmplitude;
			}
		}
		 
		return perlinNoise;
	}
	
	private static float Interpolate(float x0, float x1, float alpha)
	{
		return x0 * (1 - alpha) + alpha * x1;
	}

	public static void RecheckTerrainAt(int[][] heightmap, int x, int y, int w, int h)
	{
		final TerrainGen tg = new TerrainGen();
		tg.HeightMap = heightmap;
		tg.recheckTerrainAt(x, y, w, h);
	}

	public void recheckTerrainAt(int x, int y, int w, int h)
	{
		for(int a=0; a<=h; a++){
			for(int b=0; b<=w; b++) {
				queue.addElement(new QueueItem(x + b, y + a));
			}
		}

		QueueItem item;
		while(queue.size() > 0)
		{
			item = queue.get(0);
			queue.remove(0);
			CheckHeight(item.x, item.y);
		}
	}
	
	private void CheckHeight(int w, int h)
	{
		if(w > 0)
		{
			if(HeightMap[h][w] - HeightMap[h][w-1] > 1)
			{
				HeightMap[h][w-1] = HeightMap[h][w] - 1;
				queue.addElement(new QueueItem(w-1, h));
			}else
			if(HeightMap[h][w] - HeightMap[h][w-1] < -1)
			{
				HeightMap[h][w-1] = HeightMap[h][w] + 1;
				queue.addElement(new QueueItem(w-1, h));
			}
		}
		if(w < HeightMap[0].length-1)
		{
			if(HeightMap[h][w] - HeightMap[h][w+1] > 1)
			{
				HeightMap[h][w+1] = HeightMap[h][w] - 1;
				queue.addElement(new QueueItem(w+1, h));
			}else
			if(HeightMap[h][w] - HeightMap[h][w+1] < -1)
			{
				HeightMap[h][w+1] = HeightMap[h][w] + 1;
				queue.addElement(new QueueItem(w+1, h));
			}
		}
		if(h > 0)
		{
			if(HeightMap[h][w] - HeightMap[h-1][w] > 1)
			{
				HeightMap[h-1][w] = HeightMap[h][w] - 1;
				queue.addElement(new QueueItem(w, h-1));
			}else
			if(HeightMap[h][w] - HeightMap[h-1][w] < -1)
			{
				HeightMap[h-1][w] = HeightMap[h][w] + 1;
				queue.addElement(new QueueItem(w, h-1));
			}
		}
		if(h < HeightMap.length-1)
		{
			if(HeightMap[h][w] - HeightMap[h+1][w] > 1)
			{
				HeightMap[h+1][w] = HeightMap[h][w] - 1;
				queue.addElement(new QueueItem(w, h+1));
			}else
			if(HeightMap[h][w] - HeightMap[h+1][w] < -1)
			{
				HeightMap[h+1][w] = HeightMap[h][w] + 1;
				queue.addElement(new QueueItem(w, h+1));
			}
		}
	}
	
	private static float noise(int x, int y, int random){
		int n = x + y * 57 + random;
	 n = (n<<13) ^ n;
	 return (1.0f - ( (n * (n * n * 15731 + 789221) + 1376312589) & 0x7fffffff) / 1073741824f);
	}
	
	private static float Interpolate(float x, float y, float[] map){
		  int Xint = (int)x;
		  int Yint = (int)y;

		  float Xfrac = x - Xint;
		  float Yfrac = y - Yint;

		  int X0 = Xint % 32;
		  int Y0 = Yint % 32;
		  int X1 = (Xint + 1) % 32;
		  int Y1 = (Yint + 1) % 32;


		  float bot = map[X0*32 + Y0] + Xfrac * (map[X1*32 + Y0] - map[X0*32 + Y0]);
		  float top = map[X0*32 + Y1] + Xfrac * (map[X1*32 +  Y1] - map[X0*32 + Y1]);

		  return (bot + Yfrac * (top - bot));
	}

	private static void OverlapOctaves(float[] map32, float[] map256){
		for(int a=0; a<256*256; a++) map256[a]=0;

		float scale, noise, p;
		for (int octave=0; octave<4; octave++){
			scale = 1 / (float)Math.pow(2, 3-octave);
			p = (float)Math.pow(2, octave);
			for (int x=0; x<256; x++)
				for (int y=0; y<256; y++){
					noise = Interpolate(x*scale, y*scale , map32);

					map256[(y*256) + x] += noise / p;
				}
		}
		
	}
	
	private static void ExpFilter(float[]  map){
	  float cover = 20.0f;
	  float sharpness = 0.95f;
	  float c;
	  
	  for (int x=0; x<256*256; x++)
	  {
	 c = map[x] - (255.0f-cover);
	 if (c<0)  c = 0;
	 map[x] = 255.0f - ((float)(Math.pow(sharpness, c))*255.0f);
	  }
	}

	private static void LoopForever(float[] map32, float[] map256)
	{
	  OverlapOctaves(map32, map256);
	  ExpFilter(map256);
	}
	
	private static void SetNoise(float[] map32){
		float[][] temp = new float[34][34];
		// generate noise array
		for(int y=1; y<33; y++){
			for(int x=1; x<33; x++){
				temp[y][x] = 128.0f + (noise(x,y, rand.nextInt() % 5000) * 128.0f);
				Log.d("t",""+temp[y][x]);
			}
		}
		
		// mirror
		for (int x=1; x<33; x++){
			temp[0][x] = temp[32][x];
			temp[33][x] = temp[1][x];
			temp[x][0] = temp[x][32];
			temp[x][33] = temp[x][1];
		}
		temp[0][0] = temp[32][32];
		temp[33][33] = temp[1][1];
		temp[0][33] = temp[32][1];
		temp[33][0] = temp[1][32];
		
		float center, sides, corners;
		for (int y=1; y<33; y++)
			for (int x=1; x<33; x++){
				center = temp[x][y]/4.0f;
				sides = (temp[x+1][y] + temp[x-1][y] + temp[x][y+1] + temp[x][y-1])/8.0f;
				corners = (temp[x+1][y+1] + temp[x+1][y-1] + temp[x-1][y+1] + temp[x-1][y-1])/16.0f;
			 map32[((x-1)*32) + (y-1)] = center + sides + corners;
			}
	}


	public int[][] generatePerlin(int width, int height)
	{
		// first generate simple 2-dim matrix with random values 0 .. 1.0f
		float[][] noise = GenerateWhiteNoise(width, height);
		noise = GeneratePerlinNoise(noise, 4);

		HeightMap = new int[height][width];
		float n;

		for(int h=0; h<height; h++)
		{
			for(int w=0; w<width; w++)
			{
				n = noise[h][w];
				if(n < 0.1f)
				{
					HeightMap[h][w] = 0;
				}else{
					HeightMap[h][w] = (int)((n - 0.1f) * (float)OverSeaLevels / 0.9f);
				}
				queue.addElement(new QueueItem(w, h));
			}
		}

		QueueItem item;
		while(queue.size() > 0)
		{
			item = queue.get(0);
			queue.remove(0);
			CheckHeight(item.x, item.y);
		}

		return HeightMap;
	}




}
