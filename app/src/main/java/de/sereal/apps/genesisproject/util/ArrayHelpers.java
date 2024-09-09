package de.sereal.apps.genesisproject.util;

import android.util.Log;

import java.util.Arrays;

/**
 * Created by sereal on 11.01.2018.
 */
public class ArrayHelpers
{
  public static byte[] flatten(byte[][] twoDimArray) {
    final int height = twoDimArray.length;
    final int width = twoDimArray[0].length;
    byte[] result = new byte[height * width];


    for (int h = 0; h < height; h++) {
      System.arraycopy(twoDimArray[h], 0, result, h*width, width);
    }
    return result;
  }

  public static byte[][] unflatten(int width, int height, byte[] bytes) {
    byte[][] result = new byte[height][width];
    for (int h = 0; h < height; h++) {
      System.arraycopy(bytes, h*width, result[h], 0, width);
    }
    return result;
  }

  public static int[][] deepCopyIntMatrix(int[][] input) {
    if (input == null)
      return null;
    int[][] result = new int[input.length][];
    for (int r = 0; r < input.length; r++) {
      result[r] = input[r].clone();
    }
    return result;
  }

  public static int[] flatten(int[][] twoDimArray) {
    final int height = twoDimArray.length;
    final int width = twoDimArray[0].length;
    int[] result = new int[height * width];


    for (int h = 0; h < height; h++) {
      System.arraycopy(twoDimArray[h], 0, result, h*width, width);
    }
    return result;
  }

}
