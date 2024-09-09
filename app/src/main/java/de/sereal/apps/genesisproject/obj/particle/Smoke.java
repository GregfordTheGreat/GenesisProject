package de.sereal.apps.genesisproject.obj.particle;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import de.sereal.apps.genesisproject.R;
import de.sereal.apps.genesisproject.PlanetSurfaceRenderer;
import de.sereal.apps.genesisproject.util.TextureHandler;

/**
 * Created by sereal on 13.10.2016.
 */
public class Smoke extends ParticleEffect
{
  private int ParticleCount = 200;
  private FloatBuffer vertexBuffer;

  private Particle[] particles = new Particle[ParticleCount];

  public Smoke()
  {
    int a;
    float[] vertices = new float[ParticleCount * 4];
    for(a=0; a<ParticleCount; a++)
    {
      vertices[a * 4 + 0] = 0.0f; // x
      vertices[a * 4 + 1] = 1.0f; // y
      vertices[a * 4 + 2] = 0.0f; // z
      vertices[a * 4 + 3] = 1.0f; // size

      particles[a] = new Particle();
      particles[a].LifeTimeMs = (int)(Math.random() * (float)Particle.LIFE_SPAN);
      particles[a].Velocity.x = 0.001f + (float)(Math.random() * 0.0002f);
      particles[a].Velocity.y = 0.0005f + (float)(Math.random() * 0.0002f);
      particles[a].Velocity.z = 0.0f;
    }


    ByteBuffer vbb =  ByteBuffer.allocateDirect(vertices.length * 4);
    vbb.order(ByteOrder.nativeOrder());
    vertexBuffer = vbb.asFloatBuffer();
    vertexBuffer.put(vertices);
    vertexBuffer.position(0);
  }

  public void Animate(float ms)
  {
    float age;
    for(int a=0; a<ParticleCount; a++)
    {
      particles[a].LifeTimeMs += ms;
      if(particles[a].LifeTimeMs > Particle.LIFE_SPAN)
      {
        // renew
        particles[a].LifeTimeMs = (int)(Math.random() * Particle.LIFE_SPAN / 10.0f);
        vertexBuffer.put(a*4, Origin.x);
        vertexBuffer.put(a*4+1, Origin.y);
        vertexBuffer.put(a*4+2, Origin.z);
      }

      age = (float)particles[a].LifeTimeMs / (float)Particle.LIFE_SPAN;
      particles[a].Velocity.x = age * 0.001f;

      vertexBuffer.put(a*4, vertexBuffer.get(a*4) + particles[a].Velocity.x * ms);
      vertexBuffer.put(a*4+1, vertexBuffer.get(a*4+1) + particles[a].Velocity.y * ms);
      vertexBuffer.put(a*4+2, vertexBuffer.get(a*4+2) + particles[a].Velocity.z * ms);
      vertexBuffer.put(a*4+3, 50.0f + age * 50.0f);
    }
  }

  float[] color = new float[]{  0.5f, 0.5f, 0.5f, 0.2f };
  public void Draw()
  {

    GLES20.glDepthMask(false);
    GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

    GLES20.glBlendEquation(GLES20.GL_FUNC_ADD);
    GLES20.glEnable(GLES20.GL_BLEND);

    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, TextureHandler.GetTextureHandle(R.raw.particle)); 				// set the handle of the texture from LoadTexture
    GLES20.glUniform1i(PlanetSurfaceRenderer.uTextureParticleHandle, 0);

    GLES20.glVertexAttrib4fv(PlanetSurfaceRenderer.uColorParticleHandle, color, 0);

    GLES20.glVertexAttribPointer(PlanetSurfaceRenderer.mPositionHandleDaytime, 4, GLES20.GL_FLOAT, false, 0, vertexBuffer);
    GLES20.glEnableVertexAttribArray(PlanetSurfaceRenderer.mPositionHandleDaytime);

    GLES20.glDrawArrays(GLES20.GL_POINTS, 0, ParticleCount);

    GLES20.glDisableVertexAttribArray(PlanetSurfaceRenderer.mPositionHandleDaytime);
    GLES20.glDisable(GLES20.GL_BLEND);
    GLES20.glDepthMask(true);

  }

}
