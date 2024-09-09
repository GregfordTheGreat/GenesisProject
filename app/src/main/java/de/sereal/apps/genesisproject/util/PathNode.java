package de.sereal.apps.genesisproject.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sereal on 14.08.2016.
 */
public class PathNode
{
  public int x;
  public int y;
  public int NS;
  public int EW;

  List<PathNode> neighbors = new ArrayList<>();
  PathNode parent;
  int h; // estimated Distance
  int g;
  int f;
  int cost = 1;

  public PathNode(int x, int y)
  {
    this.x = x;
    this.y = y;
  }

  public boolean Is(PathNode n)
  {
    return (x == n.x && y == n.y);
  }
}
