package de.sereal.apps.genesisproject;

import android.content.Context;
import android.content.Intent;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import de.sereal.apps.genesisproject.event.TouchEvent;
import de.sereal.apps.genesisproject.obj.PlanetSurface;
import de.sereal.apps.genesisproject.util.Helpers;
import de.sereal.apps.genesisproject.util.Ray;
import de.sereal.apps.genesisproject.util.TextureHandler;

/**
 * Created by sereal on 01.08.2016.
 */
public class PlanetSurfaceRenderer extends Thread implements GLSurfaceView.Renderer, TouchEvent
{
  private GameActivity context;
  public PlanetSurface planetSurface;

  public GLcamera cameraPlanetSurface;
  private float PlanetSurfaceCamHorizontalAngel = 0.0f;
  private float PlanetSurfaceCamVerticalAngel = 55.0f;

//  private GameSurface gameSurface;
  private boolean mPick = false;
  private int mPickX, mPickY;

  private FloatBuffer vertexBuffer;
  private FloatBuffer colorBuffer;
  private ShortBuffer indexBuffer;

  private FloatBuffer vertexBufferLight;
  private ShortBuffer indexBufferLight;


  public static int programHandleWorldDaytime; // shader for the world at daytime
  public static int programHandleWorldNighttime; // shader for the world at daytime
  public static int programHandleWireframe; // shader for the grid
  public static int programHandleDepth; // shader for the depthmatrix/shadows

  // shader handles for the world at daytime
  public static int mMVPMatrixHandleDaytime;
  public static int mMVMatrixHandleDaytime;
  public static int mPositionHandleDaytime;
  public static int mColorHandleDaytime;
  public static int mNormalHandleDaytime;
  public static int mTexCoordHandleDaytime;
  public static int uUseTextureHandleDaytime;
  public static int uTextureIDHandleDaytime;
  public static int mLightColorHandleDaytime;
  public static int mShinyHandleDaytime;

  public static int mLightPosHandle;
  public static int mShadowProjMatrixHandle;
  public static int uTextureShadowHandler;
  public static int uxPixelOffsetHandle;
  public static int uyPixelOffsetHandle;

  // shader handles for nighttime
  public static int mMVPMatrixHandleNighttime;
  public static int mMVMatrixHandleNighttime;
  public static int mPositionHandleNighttime;
  public static int mColorHandleNighttime;
  public static int mNormalHandleNighttime;
  public static int mTexCoordHandleNighttime;
  public static int uUseTextureHandleNighttime;
  public static int uTextureIDHandleNighttime;
  public static int mLightColorHandleNighttime;


  // shader handles for shadow depth
  public static int mPositionHandleDepth;
  public static int mMVPMatrixHandleDepth;


  public static float[] mProjectionMatrixOrtho = new float[16];
  public static float[] mViewMatrixOrtho = new float[16];
  public static float[] mModelMatrixOrtho = new float[16];
  public static float[] mMVPMatrixOrtho = new float[16];
  TextureFullscreenQuad preview;

  public static int programHandleOrtho;
  public static int mMVPMatrixHandleOrtho;
  public static int mPositionHandleOrtho;
  public static int mColorHandleOrtho;
  public static int mTexCoordHandleOrtho;
  public static int mTexUniformHandleOrtho;


  // Handles for wireframe
  public static int mMVPMatrixWireHandle;
  public static int mPositionWireHandle;
  public static int mColorWireHandle;


  public static int programHandleParticle; // shader for the depthmatrix/shadows
  public static int mMVPMatrixParticleHandle;
  public static int mPositionParticleHandle;
  public static int uPointSizeHandle;
  public static int uTextureParticleHandle;
  public static int uCameraPosParticleHandle;
  public static int uColorParticleHandle;

  public static boolean NightTime = false;





  /** Here we store our viewport (0,0,width, height) */
  public int[] viewport = new int[4];

  /** Store the projection matrix. This is used to project the scene onto a 2D viewport. */
  public static float[] mProjectionMatrix = new float[16];

  /**
   * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
   * it positions things relative to our eye.
   */
  public static float[] mViewMatrix = new float[16];

  /**
   * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
   * of being located at the center of the universe) to world space.
   */
  public static float[] mModelMatrix = new float[16];

  /** Allocate storage for the final combined matrix. This will be passed into the shader program. */
  public static float[] mMVMatrix = new float[16];

  /** Allocate storage for the final combined matrix. This will be passed into the shader program. */
  public static float[] mMVPMatrix = new float[16];

  /**
   * Projection matrix from point of light source
   */
  private final float[] mLightProjectionMatrix = new float[16];

  /**
   * Stores a copy of the model matrix specifically for the light position.
   */
  private float[] mLightModelMatrix = new float[16];
  private final float[] mLightMvpMatrix_dynamicShapes = new float[16];
  private final float[] mLightViewMatrix = new float[16];


  /** Used to hold a light centered on the origin in model space. We need a 4th coordinate so we can get translations to work when
   *  we multiply this by our transformation matrices. */
  private final float[] mLightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};

  /** Used to hold the current position of the light in world space (after transformation via model matrix). */
  private final float[] mLightPosInWorldSpace = new float[4];

  /** Used to hold the transformed position of the light in eye space (after transformation via modelview matrix) */
  private final float[] mLightPosInEyeSpace = new float[4];
  private float angleInDegrees = 0;
  private float fMinuteOfTheDay;

  private final float[] mLight_Day = new float[] {0.6f, 0.6f, 0.6f, 1.0f};
  private final float[] mLight_Night = new float[] {0.2f, 0.2f, 0.4f, 1.0f};
  private final float[] mLight_Sunrise = new float[] {0.65f, 0.7f, 0.65f, 1.0f};
  private final float[] mLight_Sunset = new float[] {0.5f, 0.5f, 0.45f, 1.0f};
  private float[] mLightInUse = new float[4];


  public PlanetSurfaceRenderer(GameActivity context)
  {
    this.context = context;
    PreparePickingVis();

    cameraPlanetSurface = new GLcamera();
    cameraPlanetSurface.position.x = 0;
    cameraPlanetSurface.position.y = 12.5f;
    cameraPlanetSurface.position.z = 2f;
    cameraPlanetSurface.setZoom(12.0f);
    cameraPlanetSurface.rotate(PlanetSurfaceCamHorizontalAngel, PlanetSurfaceCamVerticalAngel);

    planetSurface = new PlanetSurface(context, this);

    context.AddTouchEventListener(this);

    start();
  }


  @Override
  public void OnRotation(float angle)
  {
    PlanetSurfaceCamHorizontalAngel = -angle;
    while(PlanetSurfaceCamHorizontalAngel < 0) PlanetSurfaceCamHorizontalAngel += 360.0f;
    while(PlanetSurfaceCamHorizontalAngel >= 360.0f) PlanetSurfaceCamHorizontalAngel -= 360.0f;

    cameraPlanetSurface.rotate(PlanetSurfaceCamHorizontalAngel,PlanetSurfaceCamVerticalAngel);
  }

  @Override
  public void OnScale(float scale)
  {
    Log.d("PlanetSurfaceRenderer","Updating Zoom??"+scale);
    cameraPlanetSurface.setZoom(12.0f / scale);
  }

  @Override
  public void OnDrag(int newScreenX, int newScreenY)
  {
    Ray ray = Helpers.GetUnprojectedRay(newScreenX, newScreenY, viewport, mMVPMatrix);
    planetSurface.PickUpdate(ray);
  }

  @Override
  public void OnMove(float distanceX, float distanceY)
  {
    cameraPlanetSurface.MoveXZ(distanceY, distanceX);
  }

  @Override
  public void OnTilt(float distance)
  {
    PlanetSurfaceCamVerticalAngel += distance / 10.0f;
    PlanetSurfaceCamVerticalAngel = Math.min(Math.max(PlanetSurfaceCamVerticalAngel, 10.0f),89.00f);
    cameraPlanetSurface.rotate(PlanetSurfaceCamHorizontalAngel,PlanetSurfaceCamVerticalAngel);
  }

  @Override
  public void onSurfaceCreated(GL10 glUnused, EGLConfig config)
  {
    Log.d("Planet","onSurfaceCreated");


    String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);

    mHasDepthTextureExtension = extensions.contains("OES_depth_texture");
    Log.d("OES_depth_texture",""+mHasDepthTextureExtension);

    InitWireShader();
    InitStandardShader();
  }

  @Override
  public void onSurfaceChanged(GL10 glUnused, int width, int height)
  {
    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    GLES20.glEnable(GLES20.GL_CULL_FACE);
    GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    GLES20.glEnable(GLES20.GL_TEXTURE_2D);

    Log.d("Planet","onSurfaceChanged");
    mDisplayWidth = width;
    mDisplayHeight = height;

    GLES20.glViewport(0, 0, width, height);

    // save the viewport, so we later can user it, even at times, where viewport is zero (predrawing)
    GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, viewport, 0);

    generateShadowFBO();

    // Create a new perspective projection matrix. The height will stay the same
    // while the width will vary as per aspect ratio.
    final float ratio = (float) width / height;
    final float left = -ratio;
    final float right = ratio;
    final float bottom = -1.0f;
    final float top = 1.0f;
    final float near = 2.0f;
    final float far = 50.0f;

    Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    Matrix.frustumM(mLightProjectionMatrix, 0, 1.1f*left, 1.1f*right, 1.1f*bottom, 1.1f*top, near, far);
    preview = new TextureFullscreenQuad(mDisplayWidth, mDisplayHeight);
  }

  /**
   * Current display sizes
   */
  private int mDisplayWidth;
  private int mDisplayHeight;

  /**
   * Current shadow map sizes
   */
  private int mShadowMapWidth;
  private int mShadowMapHeight;
  int[] fboId;
  int[] depthTextureId;
  int[] renderTextureId;
  private boolean mHasDepthTextureExtension = false;

  /**
   * Sets up the framebuffer and renderbuffer to render to texture
   */
  public void generateShadowFBO()
  {
    mShadowMapWidth = Math.round((float)mDisplayWidth * 1.0f);
    mShadowMapHeight = Math.round((float)mDisplayHeight * 1.0f);

    fboId = new int[1];
    depthTextureId = new int[1];
    renderTextureId = new int[1];

    // create a framebuffer object
    GLES20.glGenFramebuffers(1, fboId, 0);
    Log.d("FrameBuffer", fboId[0]+"");

    // create render buffer and bind 16-bit depth buffer
    GLES20.glGenRenderbuffers(1, depthTextureId, 0);
    Log.d("depthTextureId", depthTextureId+" -> "+mShadowMapWidth+"/"+mShadowMapHeight);
    GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthTextureId[0]);
    GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, mShadowMapWidth, mShadowMapHeight);

    // Try to use a texture depth component
    GLES20.glGenTextures(1, renderTextureId, 0);
    Log.d("renderTextureId", renderTextureId[0]+"");
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTextureId[0]);

    // GL_LINEAR does not make sense for depth texture. However, next tutorial shows usage of GL_LINEAR and PCF. Using GL_NEAREST
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

    // Remove artifact on the edges of the shadowmap
    GLES20.glTexParameteri( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE );
    GLES20.glTexParameteri( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE );

    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId[0]);

    if (!mHasDepthTextureExtension) {
      GLES20.glTexImage2D( GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mShadowMapWidth, mShadowMapHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

      // specify texture as color attachment
      GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, renderTextureId[0], 0);

      // attach the texture to FBO depth attachment point
      // (not supported with gl_texture_2d)
      GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, depthTextureId[0]);
    }
    else
    {
      Log.d("mHasDepthTextureExt","Use a depth texture");
      // Use a depth texture
      GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_DEPTH_COMPONENT, mShadowMapWidth, mShadowMapHeight, 0, GLES20.GL_DEPTH_COMPONENT, GLES20.GL_UNSIGNED_INT, null);

      // Attach the depth texture to FBO depth attachment point
      GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_TEXTURE_2D, renderTextureId[0], 0);
    }

    // check FBO status
    int FBOstatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
    if(FBOstatus != GLES20.GL_FRAMEBUFFER_COMPLETE) {
      Log.e("generateShadowFBO", "GL_FRAMEBUFFER_COMPLETE failed, CANNOT use FBO");
      throw new RuntimeException("GL_FRAMEBUFFER_COMPLETE failed, CANNOT use FBO"+FBOstatus);
    }
  }


  /**
   * Resets the world matrix
   */
  public static void MatrixLoadIdentity()
  {
    Matrix.setIdentityM(mModelMatrix, 0);
  }

  public static void MatrixTranslate(float x, float y, float z)
  {
    Matrix.translateM(mModelMatrix, 0,x, y, z);
  }

  public static void MatrixRotate(float xRot, float yRot, float zRot)
  {
    Matrix.rotateM(mModelMatrix, 0, xRot, 1.0f, 0.0f, 0.0f);
    Matrix.rotateM(mModelMatrix, 0, zRot, 0.0f, 0.0f, 1.0f);

    Matrix.rotateM(mModelMatrix, 0, yRot, 0.0f, 1.0f, 0.0f);
  }

  public static void MatrixConvertToView(boolean shaderActive)
  {
    Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

    // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
    // (which now contains model * view * projection).
    Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0);

    // CANT USE UNIFORMS TO SHADER HERE, AS NO PROGRAM IS ACTIVE YET

    if(shaderActive) {
      if (NightTime) {
        GLES20.glUniformMatrix4fv(mMVMatrixHandleNighttime, 1, false, mMVMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandleNighttime, 1, false, mMVPMatrix, 0);
      } else {
        GLES20.glUniformMatrix4fv(mMVMatrixHandleDaytime, 1, false, mMVMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandleDaytime, 1, false, mMVPMatrix, 0);
      }
    }
 }





  @Override
  public void onDrawFrame(GL10 glUnused)
  {
//    // clear color buffer
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

//    GLES20.glUseProgram(SurfaceRenderer.programHandleWorldDaytime);

    if(cameraPlanetSurface!=null)
    {
      // Set the camera position (View matrix)
      Matrix.setLookAtM(mViewMatrix, 0,
              cameraPlanetSurface.position.x, cameraPlanetSurface.position.y, cameraPlanetSurface.position.z,
              cameraPlanetSurface.richtung.x, cameraPlanetSurface.richtung.y, cameraPlanetSurface.richtung.z,
              cameraPlanetSurface.oben.x, cameraPlanetSurface.oben.y, cameraPlanetSurface.oben.z);
    }


    // Calculate position of the light. Rotate and then push into the distance.
//    Matrix.setIdentityM(mLightModelMatrix, 0);
//    Matrix.rotateM(mLightModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
//    Matrix.translateM(mLightModelMatrix, 0, 0.0f, 100.0f, 100.0f);
//    Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
//    Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0);
//    if(angleInDegrees > 180 && angleInDegrees < 270) angleInDegrees += 180;

// ###### THE OLD WAY ##########
//    Matrix.setIdentityM(mModelMatrix, 0);
//    // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
//    // (which currently contains model * view).
//    Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
//    // Pass in the modelview matrix.
//    GLES20.glUniformMatrix4fv(mMVMatrixHandleDaytime, 1, false, mMVPMatrix, 0);
//
//    // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
//    // (which now contains model * view * projection).
//    Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
//
//    // Pass in the combined matrix.
//    GLES20.glUniformMatrix4fv(mMVPMatrixHandleDaytime, 1, false, mMVPMatrix, 0);
//
// ###### THE OLD WAY ##########

//    MatrixLoadIdentity();
//    MatrixConvertToView();
//
//    // play with light
    fMinuteOfTheDay = ((GameActivity)context).MyGameLogic.GetMinuteOfTheDay();
    angleInDegrees = fMinuteOfTheDay / 1440.0f * 360f + 90.0f;
    while(angleInDegrees >= 360.0f) angleInDegrees -= 360.0f;
//
    float x, y, z;
    x = (float)Math.cos(angleInDegrees / 360.0f * 6.28) * 20.0f;
    y = 5.0f;
    z = (float)Math.sin(angleInDegrees / 360.0f * 6.28) * 20.0f;
    mLightPosInEyeSpace[0] = x;
    mLightPosInEyeSpace[1] = y * (float)Math.cos(60.0f / 360.0f * 6.28) - z * (float)Math.sin(60.0f / 360.0f * 6.28);
    mLightPosInEyeSpace[2] = y * (float)Math.sin(60.0f / 360.0f * 6.28) + z * (float)Math.cos(60.0f / 360.0f * 6.28);

    NightTime = mLightPosInEyeSpace[1] < 0;
    float p;
    if(fMinuteOfTheDay >= 0.0f && fMinuteOfTheDay < 720.0f){
      p = fMinuteOfTheDay / 720.0f;
      for(int a=0; a<mLightInUse.length; a++)
      {
        mLightInUse[a] = mLight_Night[a] * (1.0f - p) + mLight_Day[a] * p;
      }
    }else{
      p = (fMinuteOfTheDay - 720.0f) / 720.0f;
      for(int a=0; a<mLightInUse.length; a++)
      {
        mLightInUse[a] = mLight_Day[a] * (1.0f - p) + mLight_Night[a] * p;
      }
    }

//    NightTime = false;
//    float p;
//    if(fMinuteOfTheDay >= 360.0f && fMinuteOfTheDay < 540.0f)
//    {
//      p = (fMinuteOfTheDay - 360.0f) / 180.0f;
//      for(int a=0; a<mLightInUse.length; a++)
//      {
//        mLightInUse[a] = mLight_Night[a] * (1.0f - p) + mLight_Sunrise[a] * p;
//      }
//      Log.d("night","sunrise");
////      GLES20.glUniform4f(mLightColorHandleDaytime, mLight_Sunrise[0], mLight_Sunrise[1], mLight_Sunrise[2], mLight_Sunrise[3]);
//    }else
//    if(fMinuteOfTheDay >= 540.0f && fMinuteOfTheDay < 900.0f)
//    {
//      p = (fMinuteOfTheDay - 540.0f) / 360.0f;
//      for(int a=0; a<mLightInUse.length; a++)
//      {
//        mLightInUse[a] = mLight_Sunrise[a] * (1.0f - p) + mLight_Day[a] * p;
//      }
//      Log.d("sunrise", "day");
////      GLES20.glUniform4f(mLightColorHandleDaytime, mLight_Day[0], mLight_Day[1], mLight_Day[2], mLight_Day[3]);
//    }else
//    if(fMinuteOfTheDay >= 900.0f && fMinuteOfTheDay < 1080.0f)
//    {
//      p = (fMinuteOfTheDay - 900.0f) / 180.0f;
//      for(int a=0; a<mLightInUse.length; a++)
//      {
//        mLightInUse[a] = mLight_Day[a] * (1.0f - p) + mLight_Sunset[a] * p;
//      }
//      Log.d("day", "sunset");
////      GLES20.glUniform4f(mLightColorHandleDaytime, mLight_Sunset[0], mLight_Sunset[1], mLight_Sunset[2], mLight_Sunset[3]);
//    }else
//    if(fMinuteOfTheDay >= 1080.0f && fMinuteOfTheDay < 1260.0f)
//    {
//      NightTime = true;
//      p = (fMinuteOfTheDay - 1080.0f) / 180.0f;
//      for(int a=0; a<mLightInUse.length; a++)
//      {
//        mLightInUse[a] = mLight_Sunset[a] * (1.0f - p) + mLight_Night[a] * p;
//      }
//      Log.d("sunset", "night");
//    }
//    else
//    {
//      NightTime = true;
//      for(int a=0; a<mLightInUse.length; a++)
//      {
//        mLightInUse[a] = mLight_Night[a];
//      }
//      Log.d("night", "night");
////      GLES20.glUniform4f(mLightColorHandleNighttime, mLight_Night[0], mLight_Night[1], mLight_Night[2], mLight_Night[3]);
//    }
//

    //------------------------- render depth map --------------------------

    // Cull front faces for shadow generation to avoid self shadowing
    GLES20.glCullFace(GLES20.GL_FRONT);
    renderShadowMap();

    //------------------------- render scene ------------------------------

    // Cull back faces for normal render
    GLES20.glCullFace(GLES20.GL_BACK);
    renderScene();


    // Print openGL errors to console
    int debugInfo = GLES20.glGetError();

    if (debugInfo != GLES20.GL_NO_ERROR) {
      String msg = "OpenGL error: " + debugInfo;
      //Log.w("onDrawFrame", msg);
    }

   // renderSecondView();
  }

  private void renderSecondView()
  {

    int x = mShadowMapWidth / 2;
    int y = mShadowMapHeight / 2;
    GLES20.glViewport(mDisplayWidth - x, 0, x,y);
    final float ratio = (float) mDisplayWidth / (float)mDisplayHeight;
    final float left = -ratio;
    final float right = ratio;
    final float bottom = -1.0f;
    final float top = 1.0f;
    final float near = 1.0f;
    final float far = 100.0f;
    Matrix.orthoM(mProjectionMatrixOrtho, 0, -ratio, ratio, -1, 1, 0, 100);
    Matrix.setLookAtM(mViewMatrixOrtho, 0, 0f, 0f, -1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

    Matrix.setIdentityM(mModelMatrixOrtho, 0);
    Matrix.multiplyMM(mMVPMatrixOrtho, 0, mViewMatrixOrtho, 0, mModelMatrixOrtho, 0);
    Matrix.multiplyMM(mMVPMatrixOrtho, 0, mProjectionMatrixOrtho, 0, mMVPMatrixOrtho, 0);

    GLES20.glUseProgram(programHandleOrtho);

    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTextureId[0]);
    GLES20.glUniform1i(mTexUniformHandleOrtho, 0); 	// use GL_TEXTURE0

    preview.Draw(mMVPMatrixHandleOrtho, mMVPMatrixOrtho, mPositionHandleOrtho, mColorHandleOrtho, mTexCoordHandleOrtho);
  }

  private void renderShadowMap()
  {
    Matrix.orthoM(mLightProjectionMatrix, 0, -20.0f, 20.0f, -20.0f, 20.0f, 1.0f, 50.0f);

    Matrix.setLookAtM(mLightViewMatrix, 0,
            mLightPosInEyeSpace[0]+cameraPlanetSurface.position.x, mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]+cameraPlanetSurface.position.z,
            cameraPlanetSurface.position.x, 0.0f, cameraPlanetSurface.position.z,
            0.0f, 1.0f, 0.0f);


//    Matrix.setLookAtM(mLightViewMatrix, 0,
//            //lightX, lightY, lightZ,
//            mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2],
//            //lookX, lookY, lookZ,
//            //look in direction -y
////            mLightPosInEyeSpace[0], -mLightPosInEyeSpace[1], mLightPosInEyeSpace[2],
//            0, 0, 0,
//            //upX, upY, upZ
//            //up vector in the direction of axisY
//            -mLightPosInEyeSpace[0], 0, -mLightPosInEyeSpace[2]);

    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId[0]);
    GLES20.glViewport(0, 0, mShadowMapWidth, mShadowMapHeight);

    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

    GLES20.glUseProgram(programHandleDepth);

    float[] tempResultMatrix = new float[16];
    Matrix.multiplyMM(mLightMvpMatrix_dynamicShapes, 0, mLightViewMatrix, 0, mModelMatrix, 0);

    Matrix.multiplyMM(tempResultMatrix, 0, mLightProjectionMatrix, 0, mLightMvpMatrix_dynamicShapes, 0);
    System.arraycopy(tempResultMatrix, 0, mLightMvpMatrix_dynamicShapes, 0, 16);

//    Log.d("mLightMvpMatrix_dynamic", Arrays.toString(mLightPosInEyeSpace)+"");
//    Log.d("mLightMvpMatrix_dynamic", Arrays.toString(mLightMvpMatrix_dynamicShapes)+"");

    GLES20.glUniformMatrix4fv(mMVPMatrixHandleDepth, 1, false, mLightMvpMatrix_dynamicShapes, 0);
    planetSurface.DrawPositionOnly(mPositionHandleDepth);
  }

  private void renderScene()
  {
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
//    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    GLES20.glViewport(0, 0, mDisplayWidth, mDisplayHeight);

    MatrixLoadIdentity();
    MatrixConvertToView(false);

    if(NightTime)
    {
      GLES20.glUseProgram(programHandleWorldNighttime);
      // Pass in the modelview matrix.
      GLES20.glUniformMatrix4fv(mMVMatrixHandleNighttime, 1, false, mMVMatrix, 0);
      GLES20.glUniformMatrix4fv(mMVPMatrixHandleNighttime, 1, false, mMVPMatrix, 0);
      GLES20.glUniform4f(mLightColorHandleNighttime, mLightInUse[0], mLightInUse[1], mLightInUse[2], mLightInUse[3]);

      planetSurface.Draw(mPositionHandleNighttime, mColorHandleNighttime, mNormalHandleNighttime, uUseTextureHandleNighttime, uTextureIDHandleNighttime, mTexCoordHandleNighttime);
    }
    else
    {
      GLES20.glUseProgram(programHandleWorldDaytime);
      // Pass in the modelview matrix.
      GLES20.glUniformMatrix4fv(mMVMatrixHandleDaytime, 1, false, mMVMatrix, 0);
      // Pass in the mvp matrix.
      GLES20.glUniformMatrix4fv(mMVPMatrixHandleDaytime, 1, false, mMVPMatrix, 0);

      GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);
      GLES20.glUniform4f(mLightColorHandleDaytime, mLightInUse[0], mLightInUse[1], mLightInUse[2], mLightInUse[3]);


      // at daytime we need to do some shadow calc

      GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
      GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTextureId[0]);
      GLES20.glUniform1i(uTextureShadowHandler, 0);

      float bias[] = new float [] {
              0.5f, 0.0f, 0.0f, 0.0f,
              0.0f, 0.5f, 0.0f, 0.0f,
              0.0f, 0.0f, 0.5f, 0.0f,
              0.5f, 0.5f, 0.5f, 1.0f};

      float[] depthBiasMVP = new float[16];
      Matrix.multiplyMM(depthBiasMVP, 0, bias, 0, mLightMvpMatrix_dynamicShapes, 0);
      System.arraycopy(depthBiasMVP, 0, mLightMvpMatrix_dynamicShapes, 0, 16);

      GLES20.glUniformMatrix4fv(mShadowProjMatrixHandle, 1, false, mLightMvpMatrix_dynamicShapes, 0);
      GLES20.glUniform1f(uxPixelOffsetHandle, (1.0f / (float)mShadowMapWidth));
      GLES20.glUniform1f(uyPixelOffsetHandle, (1.0f / (float)mShadowMapHeight));

      planetSurface.Draw(mPositionHandleDaytime, mColorHandleDaytime, mNormalHandleDaytime, uUseTextureHandleDaytime, uTextureIDHandleDaytime, mTexCoordHandleDaytime);
    }

    vertexBufferLight.put(0, mLightPosInEyeSpace[0]);
    vertexBufferLight.put(1, mLightPosInEyeSpace[1]);
    vertexBufferLight.put(2, mLightPosInEyeSpace[2]);

    GLES20.glVertexAttribPointer(PlanetSurfaceRenderer.mPositionWireHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBufferLight);
    GLES20.glEnableVertexAttribArray(PlanetSurfaceRenderer.mPositionWireHandle);
    GLES20.glDrawElements(GLES20.GL_LINES, 2, GLES20.GL_UNSIGNED_SHORT, indexBufferLight);

    planetSurface.DrawParticles();

    // draw wireframe etc.
    GLES20.glUseProgram(PlanetSurfaceRenderer.programHandleWireframe);
    // Pass in the combined matrix.
    GLES20.glUniformMatrix4fv(mMVPMatrixWireHandle, 1, false, mMVPMatrix, 0);
    planetSurface.DrawWire();




    DrawPickingVis();


    if(mPick)
    {
      picking();
    }
  }


  private void InitWireShader()
  {
    String vertexShader = context.ReadRawFile(R.raw.vertexshaderwire);
    String fragmentShader = context.ReadRawFile(R.raw.fragmentshaderwire);
    programHandleWireframe = context.createProgram(vertexShader, fragmentShader);

    // bind attribute locations
    String[] attributes = {"a_Position", "a_Color"};
    for(int i=0; i<attributes.length; i++)
      GLES20.glBindAttribLocation(programHandleWireframe, i, attributes[i]);

    mMVPMatrixWireHandle = GLES20.glGetUniformLocation(programHandleWireframe, "u_MVPMatrix");
    mPositionWireHandle = GLES20.glGetAttribLocation(programHandleWireframe, "a_Position");
    mColorWireHandle =  GLES20.glGetAttribLocation(programHandleWireframe, "a_Color");
  }

  private void InitStandardShader()
  {
    String vertexShader, fragmentShader;
    String[] attributes;

    vertexShader = context.ReadRawFile(R.raw.vertexshader);
    fragmentShader = context.ReadRawFile(R.raw.fragmentshader);
    programHandleWorldDaytime = context.createProgram(vertexShader, fragmentShader);

    // bind attribute locations
    attributes = new String[]{"a_Position", "a_Color", "a_Normal", "a_TexCoordinate"};
    GLES20.glBindAttribLocation(programHandleWorldDaytime, 20, "a_Shinyness");
//    for(int i=0; i<attributes.length; i++)
//      GLES20.glBindAttribLocation(programHandleWorldDaytime, i, attributes[i]);

    mMVPMatrixHandleDaytime = GLES20.glGetUniformLocation(programHandleWorldDaytime, "u_MVPMatrix");
    mMVMatrixHandleDaytime = GLES20.glGetUniformLocation(programHandleWorldDaytime, "u_MVMatrix");
    mLightPosHandle = GLES20.glGetUniformLocation(programHandleWorldDaytime, "u_LightPos");
    mLightColorHandleDaytime = GLES20.glGetUniformLocation(programHandleWorldDaytime, "u_LightColor");
    uUseTextureHandleDaytime = GLES20.glGetUniformLocation(programHandleWorldDaytime, "u_TextureHandle"); // texture ID
    uTextureShadowHandler = GLES20.glGetUniformLocation(programHandleWorldDaytime, "uShadowTexture"); // texture ID
    mPositionHandleDaytime = GLES20.glGetAttribLocation(programHandleWorldDaytime, "a_Position");
    mColorHandleDaytime =  GLES20.glGetAttribLocation(programHandleWorldDaytime, "a_Color");
    mNormalHandleDaytime = GLES20.glGetAttribLocation(programHandleWorldDaytime, "a_Normal");
    mShinyHandleDaytime = GLES20.glGetAttribLocation(programHandleWorldDaytime, "a_Shinyness");

    mTexCoordHandleDaytime = GLES20.glGetAttribLocation(programHandleWorldDaytime, "a_TexCoordinate");
    uTextureIDHandleDaytime = GLES20.glGetUniformLocation(programHandleWorldDaytime, "u_Texture");
    mShadowProjMatrixHandle = GLES20.glGetUniformLocation(programHandleWorldDaytime, "uShadowProjMatrix");
    uxPixelOffsetHandle =  GLES20.glGetUniformLocation(programHandleWorldDaytime, "uxPixelOffset");
    uyPixelOffsetHandle =  GLES20.glGetUniformLocation(programHandleWorldDaytime, "uyPixelOffset");
    Log.d("Handles"+mShinyHandleDaytime,"("+ mPositionHandleDaytime +","+ mColorHandleDaytime +","+ mNormalHandleDaytime +","+ mTexCoordHandleDaytime +","+ uTextureIDHandleDaytime +")");


    // ################ NIGHT TIME SHADER #######################
    vertexShader = context.ReadRawFile(R.raw.vertexshader_nighttime);
    fragmentShader = context.ReadRawFile(R.raw.fragmentshader_nighttime);
    programHandleWorldNighttime = context.createProgram(vertexShader, fragmentShader);

    // bind attribute locations
    for(int i=0; i<attributes.length; i++)
      GLES20.glBindAttribLocation(programHandleWorldNighttime, i, attributes[i]);

    mMVPMatrixHandleNighttime = GLES20.glGetUniformLocation(programHandleWorldNighttime, "u_MVPMatrix");
    mMVMatrixHandleNighttime = GLES20.glGetUniformLocation(programHandleWorldNighttime, "u_MVMatrix");
    mPositionHandleNighttime = GLES20.glGetAttribLocation(programHandleWorldNighttime, "a_Position");
    mColorHandleNighttime =  GLES20.glGetAttribLocation(programHandleWorldNighttime, "a_Color");
    mNormalHandleNighttime = GLES20.glGetAttribLocation(programHandleWorldNighttime, "a_Normal");
    mTexCoordHandleNighttime = GLES20.glGetAttribLocation(programHandleWorldNighttime, "a_TexCoordinate");
    uUseTextureHandleNighttime = GLES20.glGetUniformLocation(programHandleWorldNighttime, "u_TextureHandle"); // texture ID
    uTextureIDHandleNighttime = GLES20.glGetUniformLocation(programHandleWorldNighttime, "u_Texture");
    mLightColorHandleNighttime = GLES20.glGetUniformLocation(programHandleWorldNighttime, "u_LightColor");
    Log.d("Handles","("+ mPositionHandleNighttime +","+ mColorHandleNighttime +","+ mNormalHandleNighttime +","+ mTexCoordHandleNighttime +","+ uTextureIDHandleNighttime +")");


    // ################ Particle shader #######################
    vertexShader = context.ReadRawFile(R.raw.vertexshaderparticle);
    fragmentShader = context.ReadRawFile(R.raw.fragmentshaderparticle);
    programHandleParticle = context.createProgram(vertexShader, fragmentShader);
    mPositionParticleHandle = GLES20.glGetAttribLocation(programHandleParticle, "a_Position");
    uPointSizeHandle = GLES20.glGetUniformLocation(programHandleParticle, "u_PointSize");
    uTextureParticleHandle = GLES20.glGetUniformLocation(programHandleParticle, "u_Texture");
    mMVPMatrixParticleHandle = GLES20.glGetUniformLocation(programHandleParticle, "u_MVPMatrix");
    uCameraPosParticleHandle = GLES20.glGetUniformLocation(programHandleParticle, "u_CameraPos");
    uColorParticleHandle =  GLES20.glGetAttribLocation(programHandleParticle, "a_Color");

    vertexShader = context.ReadRawFile(R.raw.vertexshaderortho);
    fragmentShader = context.ReadRawFile(R.raw.fragmentshaderortho);
    programHandleOrtho = context.createProgram(vertexShader, fragmentShader);
    mMVPMatrixHandleOrtho = GLES20.glGetUniformLocation(programHandleOrtho, "u_MVPMatrix");
    mPositionHandleOrtho = GLES20.glGetAttribLocation(programHandleOrtho, "a_Position");
    mColorHandleOrtho = GLES20.glGetAttribLocation(programHandleOrtho, "a_Color");
    mTexCoordHandleOrtho = GLES20.glGetAttribLocation(programHandleOrtho, "a_TexCoordinate");
    mTexUniformHandleOrtho = GLES20.glGetUniformLocation(programHandleOrtho, "u_Texture");



    // prepare depth shader program
    vertexShader = context.ReadRawFile(R.raw.vertexshaderdepth);
    fragmentShader = context.ReadRawFile(R.raw.fragmentshaderdepth);
    programHandleDepth = context.createProgram(vertexShader, fragmentShader);
    mMVPMatrixHandleDepth = GLES20.glGetUniformLocation(programHandleDepth, "uMVPMatrix");
    mPositionHandleDepth = GLES20.glGetAttribLocation(programHandleDepth, "aShadowPosition");


    LoadTextures();
  }

  public void LoadTextures() {
    TextureHandler.LoadTexture(R.raw.like_a_sir);
    TextureHandler.LoadTexture(R.raw.street1n);
    TextureHandler.LoadTexture(R.raw.street1e);
    TextureHandler.LoadTexture(R.raw.street1s);
    TextureHandler.LoadTexture(R.raw.street1w);
    TextureHandler.LoadTexture(R.raw.street2ns);
    TextureHandler.LoadTexture(R.raw.street2ew);
    TextureHandler.LoadTexture(R.raw.streetln);
    TextureHandler.LoadTexture(R.raw.streetle);
    TextureHandler.LoadTexture(R.raw.streetls);
    TextureHandler.LoadTexture(R.raw.streetlw);
    TextureHandler.LoadTexture(R.raw.street3n);
    TextureHandler.LoadTexture(R.raw.street3e);
    TextureHandler.LoadTexture(R.raw.street3s);
    TextureHandler.LoadTexture(R.raw.street3w);
    TextureHandler.LoadTexture(R.raw.street4);
    TextureHandler.LoadTexture(R.raw.zone);
    TextureHandler.LoadTexture(R.raw.particle);
    TextureHandler.LoadTexture(R.raw.particle_asteroid);
//
//    //TODO: should be somewhere else
    planetSurface.GenerateMaterials();
  }

  public void touchClick(float x, float y)
  {
    mPick = true;
    mPickX = (int)x;
    mPickY = (int)y;
  }

  private void picking()
  {
    mPick = false;

    Ray ray = Helpers.GetUnprojectedRay(mPickX, mPickY, viewport, mMVPMatrix);

    vertexBuffer.put(0, ray.P0.x);
    vertexBuffer.put(1, ray.P0.y);
    vertexBuffer.put(2, ray.P0.z);
    vertexBuffer.put(3, ray.P1.x);
    vertexBuffer.put(4, ray.P1.y);
    vertexBuffer.put(5, ray.P1.z);

    planetSurface.Pick(ray);
  }

  private void PreparePickingVis()
  {
    float[] vertices = new float[2 * 3];
    float[] colors = new float[2 * 4];
    short[] indices = new short[2];

    vertices[0] = 0.0f; vertices[1] = -1.0f; vertices[2] = 0.0f;
    vertices[3] = 0.0f; vertices[4] = 2.0f; vertices[5] = 0.0f;

    colors[0] = 1.0f; colors[1] = 1.0f; colors[2] = 0.0f; colors[3] = 1.0f;
    colors[4] = 1.0f; colors[5] = 1.0f; colors[6] = 1.0f; colors[7] = 1.0f;

    indices[0] = 0;
    indices[1] = 1;

    ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
    vbb.order(ByteOrder.nativeOrder());
    vertexBuffer = vbb.asFloatBuffer();
    vertexBuffer.put(vertices);
    vertexBuffer.position(0);

    // a float is 4 bytes, therefore we multiply the number if vertices with 4.
    ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
    cbb.order(ByteOrder.nativeOrder());
    colorBuffer = cbb.asFloatBuffer();
    colorBuffer.put(colors);
    colorBuffer.position(0);

    ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 2);
    ibb.order(ByteOrder.nativeOrder());
    indexBuffer = ibb.asShortBuffer();
    indexBuffer.put(indices);
    indexBuffer.position(0);

    ByteBuffer vlbb = ByteBuffer.allocateDirect(6 * 4);
    vlbb.order(ByteOrder.nativeOrder());
    vertexBufferLight = vlbb.asFloatBuffer();
    vertexBufferLight.put(0.0f);
    vertexBufferLight.put(0.0f);
    vertexBufferLight.put(0.0f);
    vertexBufferLight.put(0.0f);
    vertexBufferLight.put(0.0f);
    vertexBufferLight.put(0.0f);
    vertexBufferLight.position(0);

    ByteBuffer ilbb = ByteBuffer.allocateDirect(indices.length * 2);
    ilbb.order(ByteOrder.nativeOrder());
    indexBufferLight = ilbb.asShortBuffer();
    indexBufferLight.put((short)0);
    indexBufferLight.put((short)1);
    indexBufferLight.position(0);

  }

  private void DrawPickingVis()
  {
    GLES20.glVertexAttribPointer(PlanetSurfaceRenderer.mPositionWireHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
    GLES20.glEnableVertexAttribArray(PlanetSurfaceRenderer.mPositionWireHandle);

    GLES20.glVertexAttribPointer(PlanetSurfaceRenderer.mColorWireHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);
    GLES20.glEnableVertexAttribArray(PlanetSurfaceRenderer.mColorWireHandle);

    GLES20.glLineWidth(2.0f);
    GLES20.glDrawElements(GLES20.GL_LINES, 2, GLES20.GL_UNSIGNED_SHORT, indexBuffer);

    GLES20.glDisableVertexAttribArray(PlanetSurfaceRenderer.mColorWireHandle);
    GLES20.glDisableVertexAttribArray(PlanetSurfaceRenderer.mPositionWireHandle);
    GLES20.glLineWidth(1.0f);
  }



  public void run()
  {
    while(true)
    {
      // check if it is time to update the max magnitude, thus getting more sensible for low amplitudes
      try{ sleep(1); }
      catch(Exception e){}
    }
  }

}
