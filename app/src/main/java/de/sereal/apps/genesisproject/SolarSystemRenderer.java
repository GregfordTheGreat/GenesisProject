package de.sereal.apps.genesisproject;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import de.sereal.apps.genesisproject.event.TouchEvent;
import de.sereal.apps.genesisproject.obj.SolarSystem;
import de.sereal.apps.genesisproject.obj.Sphere;
import de.sereal.apps.genesisproject.util.Helpers;
import de.sereal.apps.genesisproject.util.Ray;

/**
 * Created by sereal on 14.02.2017.
 */
public class SolarSystemRenderer extends Thread implements GLSurfaceView.Renderer, TouchEvent
{
  private GameActivity context;
  public SolarSystem solarSystem;
  private Sphere sphere;

  public GLcamera cameraSolarSystem;
  private float PlanetSurfaceCamHorizontalAngel = 0.0f;
  private float PlanetSurfaceCamVerticalAngel = 55.0f;

  private int mDisplayWidth;
  private int mDisplayHeight;

  public static int programHandle;

  public static int mMVPMatrixHandle;
  public static int mMVMatrixHandle;

  public static int mPositionHandle;
  public static int mColorHandle;
  public static int mNormalHandle;
  public static int mTexCoordHandle;
  public static int uTextureIDHandle;
  public static int uTextureIndexHandle;
  public static int uAffectedByLighting;
  public static int uShadowTextureIDHandle;
  public static int uShadowTextureIndexHandle;

  public static int mLightPosHandle;
  public static int mLightColorHandle;


  public static float[] mProjectionMatrixOrtho = new float[16];
  public static float[] mViewMatrixOrtho = new float[16];
  public static float[] mModelMatrixOrtho = new float[16];
  public static float[] mMVPMatrixOrtho = new float[16];
  public static int programHandleOrtho;
  public static int mMVPMatrixHandleOrtho;
  public static int mPositionHandleOrtho;
  public static int mColorHandleOrtho;
  public static int mTexCoordHandleOrtho;
  public static int mTexUniformHandleOrtho;

  public static int programHandleParticle; // shader for the depthmatrix/shadows
  public static int mMVPMatrixParticleHandle;
  public static int mPositionParticleHandle;
  public static int uPointSizeHandle;
  public static int uTextureParticleHandle;
  public static int uCameraPosParticleHandle;
  public static int uColorParticleHandle;

  // FrameBufferObjects
  private int fboId, fboIdStep1, fboIdStep2;
  private int fboTex, fboTexStep1, fboTexStep2;

  // shader input for frame buffers
  private float[] m_fViewMatrix = new float[16];
  private float[] m_fProjMatrix = new float[16];
  private float[] m_fModel = new float[16];
  private float[] m_fMVPMatrix = new float[16];
  private int iProgIdBlur;
  private int iPosition;
  private int iColor;
  private int iTexCoords;
  private int iTexLoc;
  private int iVPMatrix;
  private int iDirection;
  private int iBlurRadius;
  private int iBlurResolution;
  private int blurRadius = 1;
  private int glowTextureWidth = 512;
  private int glowTextureHeight = 512;

  private boolean mPick = false;
  private int mPickX, mPickY;


  private FloatBuffer texBuffer, vertexBuffer, colorBuffer;
  private ShortBuffer indexBuffer;

  final float[] COORDS = {
          -1f,  1f, // top - left
           1f,  1f, // top - right
           1f, -1f, // bottom - right
          -1f, -1f  // bottom - left
  };

  final float[] COLORS = {
          1.0f, 0.0f, 0.0f, 1.0f, // top - left
          0.0f, 0.0f, 1.0f, 1.0f, // top - right
          0.0f, 0.0f, 0.0f, 1.0f, // bottom - left
          0.0f, 1.0f, 1.0f, 1.0f // bottom - right
  };

  final float[] TEX_COORDS = {
          0, 1, // top - left
          1, 1, // top - right
          1, 0, // bottom - right
          0, 0  // bottom - left
  };
  final short[] INDICES = new short[]{0, 2, 1, 0, 3, 2}; // counter clockwise = front

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

  private static final float[] mLightPosInEyeSpace = new float[]{ 0.0f, 0.0f, 0.0f, 0.0f };
  private final float[] mLightInUse = new float[] {1.0f, 1.0f, 0.9f, 1.0f};


  public SolarSystemRenderer(GameActivity context)
  {
    this.context = context;

    cameraSolarSystem = new GLcamera();
    cameraSolarSystem.position.x = 0;
    cameraSolarSystem.position.y = 12.5f;
    cameraSolarSystem.position.z = 2f;
    cameraSolarSystem.setZoom(12.0f);
    cameraSolarSystem.rotate(PlanetSurfaceCamHorizontalAngel, PlanetSurfaceCamVerticalAngel);

    context.AddTouchEventListener(this);


    start();
  }

  public void touchClick(float x, float y)
  {
    mPick = true;
    mPickX = (int)x;
    mPickY = (int)y;

    blurRadius++;
    if(blurRadius > 20) blurRadius = 1;
  }

  @Override
  public void OnRotation(float angle)
  {
    PlanetSurfaceCamHorizontalAngel = angle;
    while(PlanetSurfaceCamHorizontalAngel < 0) PlanetSurfaceCamHorizontalAngel += 360.0f;
    while(PlanetSurfaceCamHorizontalAngel >= 360.0f) PlanetSurfaceCamHorizontalAngel -= 360.0f;

    cameraSolarSystem.rotate(PlanetSurfaceCamHorizontalAngel,PlanetSurfaceCamVerticalAngel);
  }

  @Override
  public void OnScale(float scale)
  {
    cameraSolarSystem.setZoom(24.0f / scale);
  }

  @Override
  public void OnDrag(int newScreenX, int newScreenY)
  {
  }

  @Override
  public void OnMove(float distanceX, float distanceY)
  {
  }

  @Override
  public void OnTilt(float distance)
  {
    PlanetSurfaceCamVerticalAngel += distance / 10.0f;
    PlanetSurfaceCamVerticalAngel = Math.min(Math.max(PlanetSurfaceCamVerticalAngel, -89.0f),89.00f);
    cameraSolarSystem.rotate(PlanetSurfaceCamHorizontalAngel,PlanetSurfaceCamVerticalAngel);
  }


  @Override
  public void onSurfaceCreated(GL10 glUnused, EGLConfig config)
  {
    Log.d("System","onSurfaceCreated");
    InitStandardShader();

    solarSystem = new SolarSystem(context, this);

    sphere = new Sphere(20.0f, 20, 20);
    sphere.SetTexture(R.raw.space, 2.0f);
    sphere.Create(true);
  }

  @Override
  public void onSurfaceChanged(GL10 glUnused, int width, int height)
  {
    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
//    GLES20.glEnable(GLES20.GL_CULL_FACE);
//    GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    GLES20.glEnable(GLES20.GL_TEXTURE_2D);

    Log.d("System","onSurfaceChanged");
    mDisplayWidth = width;
    mDisplayHeight = height;

    GLES20.glViewport(0, 0, width, height);

    // save the viewport, so we later can user it, even at times, where viewport is zero (predrawing)
    GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, viewport, 0);
  }

  private void InitStandardShader()
  {
    String vertexShader, fragmentShader;

    vertexShader = context.ReadRawFile(R.raw.vertexshader_solar);
    fragmentShader = context.ReadRawFile(R.raw.fragmentshader_solar);
    programHandle = context.createProgram(vertexShader, fragmentShader);

    // bind attribute locations
    //attributes = new String[]{"a_Position", "a_Color", "a_Normal", "a_TexCoordinate"};
    mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");
    mMVMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVMatrix");
    mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
    mColorHandle =  GLES20.glGetAttribLocation(programHandle, "a_Color");
    mNormalHandle = GLES20.glGetAttribLocation(programHandle, "a_Normal");
    uTextureIDHandle = GLES20.glGetUniformLocation(programHandle, "uTextureID");
    mTexCoordHandle = GLES20.glGetAttribLocation(programHandle, "a_TexCoordinate");
    uTextureIndexHandle = GLES20.glGetUniformLocation(programHandle, "u_Texture"); // 0 for GL_TEXTURE0, 1 for GL_TEXTURE1, ...
    uAffectedByLighting = GLES20.glGetUniformLocation(programHandle, "u_AffectedByLighting");

    uShadowTextureIndexHandle = GLES20.glGetUniformLocation(programHandle, "u_ShadowTexture"); // 0 for GL_TEXTURE0, 1 for GL_TEXTURE1, ...
    uShadowTextureIDHandle = GLES20.glGetUniformLocation(programHandle, "uShadowTextureID");

    mLightPosHandle = GLES20.glGetUniformLocation(programHandle, "u_LightPos");
    mLightColorHandle = GLES20.glGetUniformLocation(programHandle, "u_LightColor");

    // blur shader
    vertexShader = context.ReadRawFile(R.raw.vertexshader_blur);
    fragmentShader = context.ReadRawFile(R.raw.fragmentshader_blur);
    iProgIdBlur = context.createProgram(vertexShader, fragmentShader);
    iPosition = GLES20.glGetAttribLocation(iProgIdBlur, "a_Position");
    iColor = GLES20.glGetAttribLocation(iProgIdBlur, "a_Color");
    iTexCoords = GLES20.glGetAttribLocation(iProgIdBlur, "a_TexCoordinate");
    iTexLoc = GLES20.glGetUniformLocation(iProgIdBlur, "u_Texture");
    iVPMatrix = GLES20.glGetUniformLocation(iProgIdBlur, "u_MVPMatrix");

    iDirection = GLES20.glGetUniformLocation(iProgIdBlur, "u_BlurDirection");
    iBlurResolution = GLES20.glGetUniformLocation(iProgIdBlur, "u_Resolution");
    iBlurRadius = GLES20.glGetUniformLocation(iProgIdBlur, "u_Radius");


    // 2d shader
    vertexShader = context.ReadRawFile(R.raw.vertexshader_2d);
    fragmentShader = context.ReadRawFile(R.raw.fragmentshader_2d);
    programHandleOrtho = context.createProgram(vertexShader, fragmentShader);
    mMVPMatrixHandleOrtho = GLES20.glGetUniformLocation(programHandleOrtho, "u_MVPMatrix");
    mPositionHandleOrtho = GLES20.glGetAttribLocation(programHandleOrtho, "a_Position");
    mColorHandleOrtho = GLES20.glGetAttribLocation(programHandleOrtho, "a_Color");
    mTexCoordHandleOrtho = GLES20.glGetAttribLocation(programHandleOrtho, "a_TexCoordinate");
    mTexUniformHandleOrtho = GLES20.glGetUniformLocation(programHandleOrtho, "u_Texture");


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


    CreateFrameBuffers();
  }



  private void CreateFrameBuffers()
  {
    int[] temp = new int[1];
    GLES20.glGenFramebuffers(1, temp, 0);
    fboId = temp[0];
    GLES20.glGenTextures(1, temp, 0);
    fboTex = temp[0];
    GLES20.glGenRenderbuffers(1, temp, 0);
    int renderBufferId = temp[0];
    InitiateFrameBuffer(fboId, fboTex, renderBufferId);

    GLES20.glGenFramebuffers(1, temp, 0);
    fboIdStep1 = temp[0];
    GLES20.glGenTextures(1, temp, 0);
    fboTexStep1 = temp[0];
    GLES20.glGenRenderbuffers(1, temp, 0);
    int renderBufferIdStep1 = temp[0];
    InitiateFrameBuffer(fboIdStep1, fboTexStep1, renderBufferIdStep1);

    GLES20.glGenFramebuffers(1, temp, 0);
    fboIdStep2 = temp[0];
    GLES20.glGenTextures(1, temp, 0);
    fboTexStep2 = temp[0];
    GLES20.glGenRenderbuffers(1, temp, 0);
    int renderBufferIdStep2 = temp[0];
    InitiateFrameBuffer(fboIdStep2, fboTexStep2, renderBufferIdStep2);

    vertexBuffer = Helpers.CreateFloatBuffer(COORDS);
    colorBuffer = Helpers.CreateFloatBuffer(COLORS);
    texBuffer = Helpers.CreateFloatBuffer(TEX_COORDS);
    indexBuffer = Helpers.CreateShortBuffer(INDICES);
  }

  public int InitiateFrameBuffer(int fbo, int tex, int rid)
  {
    //Bind Frame buffer
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo);
    //Bind texture
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex);
    //Define texture parameters
    GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, glowTextureWidth, glowTextureHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
    //Bind render buffer and define buffer dimension
    GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, rid);
    GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, glowTextureWidth, glowTextureHeight);
    //Attach texture FBO color attachment
    GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, tex, 0);
    //Attach render buffer to depth attachment
    GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, rid);
    //we are done, reset
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    return tex;
  }


  @Override
  public void onDrawFrame(GL10 glUnused)
  {
    // clear color buffer
    GLES20.glViewport(0, 0, mDisplayWidth, mDisplayHeight);
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    if(cameraSolarSystem != null)
    {
      Matrix.setLookAtM(mViewMatrix, 0,
              cameraSolarSystem.position.x, cameraSolarSystem.position.y, cameraSolarSystem.position.z,
              cameraSolarSystem.richtung.x, cameraSolarSystem.richtung.y, cameraSolarSystem.richtung.z,
              cameraSolarSystem.oben.x, cameraSolarSystem.oben.y, cameraSolarSystem.oben.z);
    }

    GLES20.glCullFace(GLES20.GL_BACK);
    renderScene();

    // Print openGL errors to console
    int debugInfo = GLES20.glGetError();

    if (debugInfo != GLES20.GL_NO_ERROR) {
      String msg = "OpenGL error: " + debugInfo;
      Log.w("onDrawFrame", msg);
    }
  }

  private void RenderFullscreenQuad()
  {
    float ratio = (float)glowTextureWidth / (float)glowTextureHeight;
    Matrix.orthoM(mProjectionMatrixOrtho, 0, -ratio, ratio, -1, 1, 0, 10.0f);
    Matrix.setLookAtM(mViewMatrixOrtho, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

    Matrix.setIdentityM(mModelMatrixOrtho, 0);
    Matrix.multiplyMM(mMVPMatrixOrtho, 0, mViewMatrixOrtho, 0, mModelMatrixOrtho, 0);
    Matrix.multiplyMM(mMVPMatrixOrtho, 0, mProjectionMatrixOrtho, 0, mMVPMatrixOrtho, 0);

    GLES20.glUseProgram(programHandleOrtho);
    GLES20.glUniformMatrix4fv(mMVPMatrixHandleOrtho, 1, false, mMVPMatrixOrtho, 0);

//    backgroundSphere.Draw(mPositionHandleOrtho, mColorHandleOrtho, 0, mTexUniformHandleOrtho, uTextureIndexHandle, mTexCoordHandleOrtho);



    GLES20.glVertexAttribPointer(mPositionHandleOrtho, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
    GLES20.glEnableVertexAttribArray(mPositionHandleOrtho);
//    GLES20.glVertexAttribPointer(mColorHandleOrtho, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);
//    GLES20.glEnableVertexAttribArray(mColorHandleOrtho);
    GLES20.glVertexAttribPointer(mTexCoordHandleOrtho, 2, GLES20.GL_FLOAT, false, 0, texBuffer);
    GLES20.glEnableVertexAttribArray(mTexCoordHandleOrtho);

    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fboTexStep2);
    GLES20.glUniform1i(mTexUniformHandleOrtho, 0); 	// use GL_TEXTURE0

    GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);
    GLES20.glBlendEquation(GLES20.GL_FUNC_ADD);
    GLES20.glEnable(GLES20.GL_BLEND);
    GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
    GLES20.glDisable(GLES20.GL_BLEND);

    GLES20.glDisableVertexAttribArray(mPositionHandleOrtho);
//    GLES20.glDisableVertexAttribArray(mColorHandleOrtho);
    GLES20.glDisableVertexAttribArray(mTexCoordHandleOrtho);
  }

  private void renderScene()
  {
    GLES20.glViewport(0, 0, glowTextureWidth, glowTextureHeight);
    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

    RenderToTexture();
    BlurTextureOld();


    // now for the onscreen rendering
    GLES20.glViewport(0, 0, mDisplayWidth, mDisplayHeight);
    GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    GLES20.glDisable(GLES20.GL_DEPTH_TEST);
    RenderBackground();
    GLES20.glEnable(GLES20.GL_DEPTH_TEST);

    render3dScene();

    GLES20.glDisable(GLES20.GL_DEPTH_TEST);
    RenderFullscreenQuad();
    GLES20.glEnable(GLES20.GL_DEPTH_TEST);

    if(mPick)
    {
      picking();
    }
  }

  private void picking()
  {
    mPick = false;

    Ray ray = Helpers.GetUnprojectedRay(mPickX, mPickY, viewport, mMVPMatrix);

    solarSystem.Pick(ray);
  }


  private void RenderBackground()
  {
    float ratio = (float) mDisplayWidth / (float)mDisplayHeight;
    float left = -ratio;
    float right = ratio;
    float bottom = -1.0f;
    float top = 1.0f;
    float near = 2.0f;
    float far = 100.0f;

    GLES20.glUseProgram(programHandle);
    Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    MatrixLoadIdentity();

    MatrixTranslate(cameraSolarSystem.position.x, cameraSolarSystem.position.y,cameraSolarSystem.position.z); //cameraSolarSystem.position.y, cameraSolarSystem.position.z);
    MatrixConvertToView();
//    backgroundSphere.Draw(mPositionHandle, mColorHandle, mNormalHandle, uTextureIDHandle, uTextureIndexHandle, mTexCoordHandle);
    GLES20.glUniform1i(uAffectedByLighting, 0);
    sphere.Draw(mPositionHandle, mColorHandle, mNormalHandle, uTextureIDHandle, uTextureIndexHandle, mTexCoordHandle);
  }

  private void render3dScene()
  {
    float ratio = (float) mDisplayWidth / (float)mDisplayHeight;
    float left = -ratio;
    float right = ratio;
    float bottom = -1.0f;
    float top = 1.0f;
    float near = 2.0f;
    float far = 100.0f;

    GLES20.glUseProgram(programHandle);
    Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    MatrixLoadIdentity();
    MatrixConvertToView();
    solarSystem.Draw(mPositionHandle, mColorHandle, mNormalHandle, uTextureIDHandle, uTextureIndexHandle, mTexCoordHandle, uAffectedByLighting);
  }

  private void RenderToTexture()
  {
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    GLES20.glUseProgram(programHandle);

    MatrixLoadIdentity();
    MatrixConvertToView();

    // Pass in the modelview matrix.
    GLES20.glUniform4f(mLightColorHandle, mLightInUse[0], mLightInUse[1], mLightInUse[2], mLightInUse[3]);
    solarSystem.DrawBlurParts(mPositionHandle, mColorHandle, mNormalHandle, uTextureIDHandle, uTextureIndexHandle, mTexCoordHandle, uAffectedByLighting);
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
  }

  private void BlurTextureOld()
  {
    float ratio = (float)glowTextureWidth / (float)glowTextureHeight;
    Matrix.orthoM(m_fProjMatrix, 0, -ratio, ratio, -1, 1, 0, 10.0f);
    Matrix.setLookAtM(m_fViewMatrix, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
    Matrix.setIdentityM(m_fModel, 0);
    Matrix.multiplyMM(m_fMVPMatrix, 0, m_fViewMatrix, 0, m_fModel, 0);
    Matrix.multiplyMM(m_fMVPMatrix, 0, m_fProjMatrix, 0, m_fMVPMatrix, 0);

    BlurStepOld(fboIdStep1, fboTex, 0.0f, 1.0f);
    BlurStepOld(fboIdStep2, fboTexStep1, 0.5625f,0.0f);
    BlurStepOld(fboIdStep1, fboTexStep2, 0.0f, 0.5f);
    BlurStepOld(fboIdStep2, fboTexStep1, 0.2812f,0.0f);
  }

  private void BlurStepOld(int targetFBO, int sourceFBOTexture, float horizontal, float vertical)
  {
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, targetFBO);
    GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    GLES20.glUseProgram(iProgIdBlur);

    GLES20.glVertexAttribPointer(iPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
    GLES20.glEnableVertexAttribArray(iPosition);
    GLES20.glVertexAttribPointer(iTexCoords, 2, GLES20.GL_FLOAT, false, 0, texBuffer);
    GLES20.glEnableVertexAttribArray(iTexCoords);

    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    GLES20.glUniform1i(iTexLoc, 0); // use texture 0
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, sourceFBOTexture);

    GLES20.glUniformMatrix4fv(iVPMatrix, 1, false, m_fMVPMatrix, 0);


    GLES20.glUniform2f(iDirection, horizontal, vertical);
    GLES20.glUniform1f(iBlurRadius,  (float)blurRadius);
    GLES20.glUniform1f(iBlurResolution,  (float)glowTextureWidth);


    GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, indexBuffer);

    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
  }


  /**
   * Resets the world matrix
   */
  public static void MatrixLoadIdentity()
  {
    Matrix.setIdentityM(mModelMatrix, 0);
    mLightPosInEyeSpace[0] = 0.0f;
    mLightPosInEyeSpace[1] = 0.0f;
    mLightPosInEyeSpace[2] = 0.0f;
  }

  public static void MatrixTranslate(float x, float y, float z)
  {
    Matrix.translateM(mModelMatrix, 0, x, y, z);
    mLightPosInEyeSpace[0] = -x;
    mLightPosInEyeSpace[1] = -y;
    mLightPosInEyeSpace[2] = -z;
  }

  public static void MatrixRotate(float xRot, float yRot, float zRot)
  {
    Matrix.rotateM(mModelMatrix, 0, xRot, 1.0f, 0.0f, 0.0f);
    Matrix.rotateM(mModelMatrix, 0, zRot, 0.0f, 0.0f, 1.0f);

    Matrix.rotateM(mModelMatrix, 0, yRot, 0.0f, 1.0f, 0.0f);
  }

  public static void MatrixConvertToView()
  {
    Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
    // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
    // (which now contains model * view * projection).
    Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0);

    GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVMatrix, 0);
    GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
    GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);
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
