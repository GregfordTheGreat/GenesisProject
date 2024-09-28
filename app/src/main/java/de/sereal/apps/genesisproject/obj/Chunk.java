package de.sereal.apps.genesisproject.obj;

public class Chunk {
   private int x;
   private int y;
   private int width;
   private int height;
   
   public Chunk(final int x, final int y, final int width, final int height) {
       this.x = x;
       this.y = y;
       this.width = width;
       this.height = height;
   }
   
   public int getX() {
       return x;
   }
   
   public int getY() {
       return y;
   }
   
   public int getWidth() {
       return width;
   }
   
   public int getHeight() {
       return height;
   }
}
