package de.sereal.apps.genesisproject.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import de.sereal.apps.genesisproject.R;
import de.sereal.apps.genesisproject.util.MyConstants.Direction;
import java.util.*;

public class RoadMap
{
	private byte[][] Map;
	public final static byte N = 0x01;
	public final static byte E = 0x02;
	public final static byte S = 0x04;
	public final static byte W = 0x08;
	
	public RoadMap(int width, int height) 
	{
		Map = new byte[height][width];
	}

	public RoadMap(int width, int height, byte[] bytes){
		Map = ArrayHelpers.unflatten(width, height, bytes);
	}

	public byte[] getFlatRoadMap() { return ArrayHelpers.flatten(Map); }

	/**
	 * 
	 * @param x (0 <= x <= w)
	 * @param y (0 <= y <= h)
	 * @param set a combination of N, E, S, W
	 * @return true, if we added any direction and the cell was not empty
	 */
	public boolean Add(int x, int y, byte set)
	{
		boolean added = false;
		if((Map[y][x] != 0) && (Map[y][x] & set) != 0x00)
		{
			added = true;
		}
		Map[y][x] |= set;
		return added;
	}

	public byte Get(int x, int y)
	{
		return Map[y][x];
	}


	public Direction GetDirection(int x, int y)
	{
		switch(Map[y][x])
		{
			case 1: return Direction.NORTH;
			case 2: return Direction.EAST;
			case 4: return Direction.SOUTH;
			case 8: return Direction.WEST;

			case 5: return Direction.NORTH;
			case 10:return Direction.EAST;

			case 3: return Direction.NORTH;
			case 6: return Direction.EAST;
			case 9: return Direction.WEST;
			case 12: return Direction.SOUTH;

			case 7: return Direction.NORTH;
			case 11: return Direction.EAST;
			case 13: return Direction.NORTH;
			case 14: return Direction.WEST;
		}
		return Direction.NORTH;
	}
	
	public int GetTextureId(int x, int y)
	{
		switch(Map[y][x])
		{
			case 1: return R.raw.street1n;
			case 2: return R.raw.street1e;
			case 4: return R.raw.street1s;
			case 8: return R.raw.street1w;

			case 5:  return R.raw.street2ns; 
			case 10: return R.raw.street2ew;

			case 3: return R.raw.streetln;
			case 6: return R.raw.streetle;
			case 12: return R.raw.streetls;
			case 9: return R.raw.streetlw;

			case 11: return R.raw.street3n;
			case 7: return R.raw.street3e;
			case 14: return R.raw.street3s;
			case 13: return R.raw.street3w;

			case 15: return R.raw.street4;

			case 0:
				break;

			default:
				Log.d("unknwon",""+Map[y][x]);
				break;
		}
		return 0;
	}


	private List<PathNode> GetNeighbors(PathNode current)
	{
		List<PathNode> successors = new ArrayList<>();
		byte rmS = Get(current.x,current.y);

		if( ((rmS & RoadMap.S) != 0) && (Get(current.x, current.y+1) & RoadMap.N) != 0)
		{
			successors.add(new PathNode(current.x, current.y+1));
		}
		if( ((rmS & RoadMap.N) != 0) && (Get(current.x, current.y-1) & RoadMap.S) != 0)
		{
			successors.add(new PathNode(current.x, current.y-1));
		}
		if( ((rmS & RoadMap.E) != 0) && (Get(current.x+1, current.y) & RoadMap.W) != 0)
		{
			successors.add(new PathNode(current.x+1, current.y));
		}
		if( ((rmS & RoadMap.W) != 0) && (Get(current.x-1, current.y) & RoadMap.E) != 0)
		{
			successors.add(new PathNode(current.x-1, current.y));
		}
		return successors;
	}
	
	private boolean containsXY(Set<PathNode> set, PathNode search) {
		for(PathNode n : set) {

			if(n.x == search.x && n.y == search.y){
			    return true;

			}
		}

		return false;
	}

	public List<PathNode> FindPathAStar(PathNode start, PathNode goal)
	{
		Set<PathNode> open = new HashSet<PathNode>();
		Set<PathNode> closed = new HashSet<PathNode>();

		start.g = 0;
		start.h = EstDistance(start, goal);
		start.f = start.h;

		open.add(start);
		for(;;)
		{
			PathNode current = null;
			if (open.size() == 0)
			{
				// no route
				return new ArrayList<PathNode>();
			}

			for (PathNode node : open) {
				if (current == null || node.f < current.f) {
					current = node;
				}
			}
			if (current.Is(goal)) {
				goal.parent = current.parent;
				break;
			}

			open.remove(current);
			closed.add(current);
			current.neighbors = GetNeighbors(current);

			for (PathNode neighbor : current.neighbors) {
				if (neighbor == null) {
					continue;
				}

				int nextG = current.g + neighbor.cost;

				if (nextG < neighbor.g) {
					open.remove(neighbor);
					closed.remove(neighbor);
				}

				if (!containsXY(open, neighbor) && !containsXY(closed, neighbor)) {
					neighbor.g = nextG;
					neighbor.h = EstDistance(neighbor, goal);
					neighbor.f = neighbor.g + neighbor.h;
					neighbor.parent = current;
					open.add(neighbor);
				}
			}
		}

		List<PathNode> nodes = new ArrayList<PathNode>();
		PathNode current = goal;
		while (current.parent != null) {
			nodes.add(current);
			current = current.parent;
		}
		nodes.add(start);

		return nodes;
	}

	private int EstDistance(PathNode A, PathNode B)
	{
		return Math.abs(A.x - B.x) + Math.abs(A.y - B.y);
	}

}
