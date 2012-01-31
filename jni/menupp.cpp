/*==============================================================================
            Copyright (c) 2010-2011 QUALCOMM Incorporated.
            All Rights Reserved.
            Qualcomm Confidential and Proprietary
            
@file 
    menupp.cpp

@brief
    Skeleton repo for menupp 

==============================================================================*/


#include <jni.h>
#include <android/log.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>

#ifdef USE_OPENGL_ES_1_1
#include <GLES/gl.h>
#include <GLES/glext.h>
#else
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#endif

#include <QCAR/QCAR.h>
#include <QCAR/CameraDevice.h>
#include <QCAR/Renderer.h>
#include <QCAR/ImageTarget.h>
#include <QCAR/VirtualButton.h>
#include <QCAR/Rectangle.h>
#include <QCAR/VideoBackgroundConfig.h>
#include <QCAR/Trackable.h>
#include <QCAR/Tool.h>
#include <QCAR/Tracker.h>
#include <QCAR/CameraCalibration.h>
#include <QCAR/UpdateCallback.h>

#include "SampleUtils.h"
#include "Texture.h"
#include "CubeShaders.h"
#include "Teapot.h"
#include "banana.h"

#ifdef __cplusplus
extern "C"
{
#endif

// Plane Definitions
// Define a plane to bind an image to..
static const float planeVertices[] =
{
    -0.5, -0.5, 0.0, 0.5, -0.5, 0.0, 0.5, 0.5, 0.0, -0.5, 0.5, 0.0,
};

static const float planeTexcoords[] =
{
    0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0
};

static const float planeNormals[] =
{
    0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0
};

static const unsigned short planeIndices[] =
{
    0, 1, 2, 0, 2, 3
};

enum BUTTONS
{
    BUTTON_1                    = 1,
    BUTTON_2                    = 2,
    BUTTON_3                    = 4,
    BUTTON_4                    = 8
};

static const char* lineMeshVertexShader = " \
  \
attribute vec4 vertexPosition; \
 \
uniform mat4 modelViewProjectionMatrix; \
 \
void main() \
{ \
   gl_Position = modelViewProjectionMatrix * vertexPosition; \
} \
";


static const char* lineFragmentShader = " \
 \
precision mediump float; \
 \
void main() \
{ \
   gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0); \
} \
";

// OpenGL ES 2.0 specific (Virtual Buttons):
unsigned int vbShaderProgramID  = 0;
GLint vbVertexHandle            = 0;
int buttonMask                  = 0;

// Virtual Button runtime creation:
bool updateBtns                   = false;
const char* virtualButtonColors[] = {"red", "blue", "yellow", "green"};
const int NUM_BUTTONS             = 4;


// Textures:
int textureCount                = 0;
Texture** textures              = 0;

// OpenGL ES 2.0 specific:
#ifdef USE_OPENGL_ES_2_0
unsigned int shaderProgramID    = 0;
GLint vertexHandle              = 0;
GLint normalHandle              = 0;
GLint textureCoordHandle        = 0;
GLint mvpMatrixHandle           = 0;
#endif

// Screen dimensions:
unsigned int screenWidth        = 0;
unsigned int screenHeight       = 0;

// Indicates whether screen is in portrait (true) or landscape (false) mode
bool isActivityInPortraitMode   = false;

// The projection matrix used for rendering virtual objects:
QCAR::Matrix44F projectionMatrix;

// Constants:
//static const float kObjectScale = 3.f;
static const float kObjectScale = 200;

JNIEXPORT int JNICALL
Java_srdes_menupp_QcarEngine_getOpenGlEsVersionNative(JNIEnv *, jobject)
{
#ifdef USE_OPENGL_ES_1_1        
    return 1;
#else
    return 2;
#endif
}


JNIEXPORT void JNICALL
Java_srdes_menupp_QcarEngine_setActivityPortraitMode(JNIEnv *, jobject, jboolean isPortrait)
{
    isActivityInPortraitMode = isPortrait;
}

// Add a button to the list of buttons which are toggled in the next update call
JNIEXPORT void JNICALL
Java_srdes_menupp_QcarEngine_addButtonToToggle(JNIEnv */*env*/, jobject /*obj*/, jint virtualButtonIdx)
{
    LOG("Java_com_qualcomm_QCARSamples_VirtualButtons_VirtualButtons_addButtonToToggle");

    assert(virtualButtonIdx >= 0 && virtualButtonIdx < NUM_BUTTONS);

    switch (virtualButtonIdx)
    {
        case 0:
            buttonMask |= BUTTON_1;
            break;

        case 1:
            buttonMask |= BUTTON_2;
            break;

        case 2:
            buttonMask |= BUTTON_3;
            break;

        case 3:
            buttonMask |= BUTTON_4;
            break;
    }
    updateBtns = true;
}

JNIEXPORT void JNICALL
Java_srdes_menupp_QcarEngine_onQCARInitializedNative(JNIEnv *, jobject)
{
    LOG("Java_srdes_menupp_QcarEngine_onQCARInitializedNative");
    QCAR::setHint(QCAR::HINT_MAX_SIMULTANEOUS_IMAGE_TARGETS, 5);
}

// Create/destroy a Virtual Button at runtime
//
// Note: This will NOT work if the tracker is active!
bool
toggleVirtualButton(QCAR::ImageTarget* imageTarget, const char* name,
                    float left, float top, float right, float bottom)
{
    LOG("toggleVirtualButton");

    bool buttonToggleSuccess = false;

    QCAR::VirtualButton* virtualButton = imageTarget->getVirtualButton(name);
    if (virtualButton != NULL)
    {
        LOG("Destroying Virtual Button");
        buttonToggleSuccess = imageTarget->destroyVirtualButton(virtualButton);
    }
    else
    {
        LOG("Creating Virtual Button");
        QCAR::Rectangle vbRectangle(left, top, right, bottom);
        QCAR::VirtualButton* virtualButton = imageTarget->createVirtualButton(name, vbRectangle);

        // This is just a showcase. The values used here a set by default on Virtual Button creation
        virtualButton->setEnabled(true);
        virtualButton->setSensitivity(QCAR::VirtualButton::MEDIUM);

        if (virtualButton != NULL)
            buttonToggleSuccess = true;
    }

    return buttonToggleSuccess;
}


// Object to receive update callbacks from QCAR SDK
class VirtualButton_UpdateCallback : public QCAR::UpdateCallback
{
    virtual void QCAR_onUpdate(QCAR::State& /*state*/)
    {
        if (updateBtns)
        {
            // Update runs in the tracking thread therefore it is guaranteed that the tracker is
            // not doing anything at this point. => Reconfiguration is possible.

            assert(QCAR::Tracker::getInstance().getNumTrackables() > 0);
            QCAR::Trackable* trackable = QCAR::Tracker::getInstance().getTrackable(0);

            assert(trackable);
            assert(trackable->getType() == QCAR::Trackable::IMAGE_TARGET);
            QCAR::ImageTarget* imageTarget = static_cast<QCAR::ImageTarget*>(trackable);


            if (buttonMask & BUTTON_1)
            {
                LOG("Toggle Button 1");

                toggleVirtualButton(imageTarget, "enchiladas", -180, 180, 180, -180);

            }
            if (buttonMask & BUTTON_2)
            {
                LOG("Toggle Button 2");

                toggleVirtualButton(imageTarget, "hotdog", -180, 180, 180, -180);
            }
            if (buttonMask & BUTTON_3)
            {
                LOG("Toggle Button 3");

                toggleVirtualButton(imageTarget, "pizza", -180, 180, 180, -180);
            }
            if (buttonMask & BUTTON_4)
            {
                LOG("Toggle Button 4");

                toggleVirtualButton(imageTarget, virtualButtonColors[3],
                                    76.57f, -53.52f, 109.50f, -65.87f);
            }

            buttonMask = 0;
            updateBtns = false;
        }
    }
} qcarUpdate;

JNIEXPORT void JNICALL
Java_srdes_menupp_menuppRenderer_renderFrame(JNIEnv *, jobject)
{
    //LOG("Java_com_qualcomm_QCARSamples_ImageTargets_GLRenderer_renderFrame");

    // Clear color and depth buffer 
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    // Render video background:
    QCAR::State state = QCAR::Renderer::getInstance().begin();
        
#ifdef USE_OPENGL_ES_1_1
    // Set GL11 flags:
    glEnableClientState(GL_VERTEX_ARRAY);
    glEnableClientState(GL_NORMAL_ARRAY);
    glEnableClientState(GL_TEXTURE_COORD_ARRAY);

    glEnable(GL_TEXTURE_2D);
    glDisable(GL_LIGHTING);
        
#endif

    glEnable(GL_DEPTH_TEST);
    glEnable(GL_CULL_FACE);

    // Did we find any trackables this frame?
    for(int tIdx = 0; tIdx < state.getNumActiveTrackables(); tIdx++)
    {
        // Get the trackable:
        const QCAR::Trackable* trackable = state.getActiveTrackable(tIdx);
        QCAR::Matrix44F modelViewMatrix =
            QCAR::Tool::convertPose2GLMatrix(trackable->getPose());

        // The image target:
        //assert(trackable->getType() == QCAR::Trackable::IMAGE_TARGET);
        //const QCAR::ImageTarget* target =
        //    static_cast<const QCAR::ImageTarget*>(trackable);

        //const QCAR::VirtualButton* button = target->getVirtualButton(0);



        // Choose the texture based on the target name:
        Texture* imgTexture;

        if(strcmp(trackable->getName(), "item_1") == 0) {
        	imgTexture = textures[0];
        } else if (strcmp(trackable->getName(), "item_2") == 0) {
        	imgTexture = textures[1];
        } else if (strcmp(trackable->getName(), "item_3") == 0) {
        	imgTexture = textures[2];
        } else if (strcmp(trackable->getName(), "item_4") == 0) {
        	imgTexture = textures[3];
        } else if (strcmp(trackable->getName(), "item_5") == 0) {
        	imgTexture = textures[4];
        } else continue;


        // If the button is pressed, than use this texture:
        //if (button->isPressed() && strcmp(trackable->getName(), "item_1" == 0))
        //{
        	//imgTexture = textures[2];
        //}



#ifdef USE_OPENGL_ES_1_1
        // Load projection matrix:
        glMatrixMode(GL_PROJECTION);
        glLoadMatrixf(projectionMatrix.data);

        // Load model view matrix:
        glMatrixMode(GL_MODELVIEW);
        glLoadMatrixf(modelViewMatrix.data);
        glTranslatef(0.f, 0.f, kObjectScale);
        glScalef(kObjectScale, kObjectScale, kObjectScale);

        // Draw object:
        glBindTexture(GL_TEXTURE_2D, thisTexture->mTextureID);
        glTexCoordPointer(2, GL_FLOAT, 0, (const GLvoid*) &teapotTexCoords[0]);
	//glTexCoordPointer(2, GL_FLOAT, 0, bananaTexCoords);

        glVertexPointer(3, GL_FLOAT, 0, (const GLvoid*) &teapotVertices[0]);
	//glVertexPointer(3, GL_FLOAT, 0, bananaVerts);

        glNormalPointer(GL_FLOAT, 0,  (const GLvoid*) &teapotNormals[0]);
	//glVertexPointer(3, GL_FLOAT, 0, bananaVerts);

        glDrawElements(GL_TRIANGLES, NUM_TEAPOT_OBJECT_INDEX, GL_UNSIGNED_SHORT,
                       (const GLvoid*) &teapotIndices[0]);

	//glDrawArrays(GL_TRIANGLES, 0, bananaNumVerts);
#else

        /*
        QCAR::Matrix44F modelViewProjection;

        SampleUtils::translatePoseMatrix(0.0f, 0.0f, kObjectScale,
                                         &modelViewMatrix.data[0]);
        SampleUtils::scalePoseMatrix(kObjectScale, kObjectScale, kObjectScale,
                                     &modelViewMatrix.data[0]);
        SampleUtils::multiplyMatrix(&projectionMatrix.data[0],
                                    &modelViewMatrix.data[0] ,
                                    &modelViewProjection.data[0]);

        glUseProgram(shaderProgramID);
         
        glVertexAttribPointer(vertexHandle, 3, GL_FLOAT, GL_FALSE, 0,
                              (const GLvoid*) &teapotVertices[0]);
                              //(const GLvoid*) bananaVerts);
        glVertexAttribPointer(normalHandle, 3, GL_FLOAT, GL_FALSE, 0,
                              (const GLvoid*) &teapotNormals[0]);
                              //(const GLvoid*) bananaNormals);
        glVertexAttribPointer(textureCoordHandle, 2, GL_FLOAT, GL_FALSE, 0,
                              (const GLvoid*) &teapotTexCoords[0]);
                              //(const GLvoid*) bananaTexCoords);
        
        glEnableVertexAttribArray(vertexHandle);
        glEnableVertexAttribArray(normalHandle);
        glEnableVertexAttribArray(textureCoordHandle);
        
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, thisTexture->mTextureID);
        glUniformMatrix4fv(mvpMatrixHandle, 1, GL_FALSE,
                           (GLfloat*)&modelViewProjection.data[0] );
        glDrawElements(GL_TRIANGLES, NUM_TEAPOT_OBJECT_INDEX, GL_UNSIGNED_SHORT,
                       (const GLvoid*) &teapotIndices[0]);

	//glDrawArrays(GL_TRIANGLES, 0, bananaNumVerts);

        SampleUtils::checkGlError("menupp renderFrame");
        */

    	// Place an image on the target using a 3D plane

            QCAR::Matrix44F modelViewProjection;

            SampleUtils::translatePoseMatrix(0.0f, 0.0f, kObjectScale,
                                             &modelViewMatrix.data[0]);
            SampleUtils::scalePoseMatrix(kObjectScale, kObjectScale, 1.0f,
                                         &modelViewMatrix.data[0]);
            SampleUtils::multiplyMatrix(&projectionMatrix.data[0],
                                        &modelViewMatrix.data[0] ,
                                        &modelViewProjection.data[0]);

            glUseProgram(shaderProgramID);

            glVertexAttribPointer(vertexHandle, 3, GL_FLOAT, GL_FALSE, 0,
                                  (const GLvoid*) &planeVertices[0]);
            glVertexAttribPointer(normalHandle, 3, GL_FLOAT, GL_FALSE, 0,
                                  (const GLvoid*) &planeNormals[0]);
            glVertexAttribPointer(textureCoordHandle, 2, GL_FLOAT, GL_FALSE, 0,
                                  (const GLvoid*) &planeTexcoords[0]);

            glEnableVertexAttribArray(vertexHandle);
            glEnableVertexAttribArray(normalHandle);
            glEnableVertexAttribArray(textureCoordHandle);

            glBindTexture(GL_TEXTURE_2D, imgTexture->mTextureID);
            glUniformMatrix4fv(mvpMatrixHandle, 1, GL_FALSE, (GLfloat*)&modelViewProjection.data[0] );
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, (const GLvoid*) &planeIndices[0]);
#endif

    }

    glDisable(GL_DEPTH_TEST);

#ifdef USE_OPENGL_ES_1_1        
    glDisable(GL_TEXTURE_2D);
    glDisableClientState(GL_VERTEX_ARRAY);
    glDisableClientState(GL_NORMAL_ARRAY);
    glDisableClientState(GL_TEXTURE_COORD_ARRAY);
#else
    glDisableVertexAttribArray(vertexHandle);
    glDisableVertexAttribArray(normalHandle);
    glDisableVertexAttribArray(textureCoordHandle);
#endif

    QCAR::Renderer::getInstance().end();
}



void
configureVideoBackground()
{
    // Get the default video mode:
    QCAR::CameraDevice& cameraDevice = QCAR::CameraDevice::getInstance();
    QCAR::VideoMode videoMode = cameraDevice.
                                getVideoMode(QCAR::CameraDevice::MODE_DEFAULT);


    // Configure the video background
    QCAR::VideoBackgroundConfig config;
    config.mEnabled = true;
    config.mSynchronous = true;
    config.mPosition.data[0] = 0.0f;
    config.mPosition.data[1] = 0.0f;
    
    if (isActivityInPortraitMode)
    {
        //LOG("configureVideoBackground PORTRAIT");
        config.mSize.data[0] = videoMode.mHeight
                                * (screenHeight / (float)videoMode.mWidth);
        config.mSize.data[1] = screenHeight;
    }
    else
    {
        //LOG("configureVideoBackground LANDSCAPE");
        config.mSize.data[0] = screenWidth;
        config.mSize.data[1] = videoMode.mHeight
                            * (screenWidth / (float)videoMode.mWidth);
    }

    // Set the config:
    QCAR::Renderer::getInstance().setVideoBackgroundConfig(config);
}


JNIEXPORT void JNICALL
Java_srdes_menupp_QcarEngine_initApplicationNative( JNIEnv* env, jobject obj, jint width, jint height)
{
    LOG("Java_srdes_menupp_menupp_initApplicationNative");
    
    // Store screen dimensions
    screenWidth = width;
    screenHeight = height;
        
    // Handle to the activity class:
    jclass activityClass = env->GetObjectClass(obj);

    jmethodID getTextureCountMethodID = env->GetMethodID(activityClass,
                                                    "getTextureCount", "()I");
    if (getTextureCountMethodID == 0)
    {
        LOG("Function getTextureCount() not found.");
        return;
    }

    textureCount = env->CallIntMethod(obj, getTextureCountMethodID);    
    if (!textureCount)
    {
        LOG("getTextureCount() returned zero.");
        return;
    }

    textures = new Texture*[textureCount];

    jmethodID getTextureMethodID = env->GetMethodID(activityClass,
        "getTexture", "(I)Lsrdes/menupp/Texture;");

    if (getTextureMethodID == 0)
    {
        LOG("Function getTexture() not found.");
        return;
    }

    // Register the textures
    for (int i = 0; i < textureCount; ++i)
    {

        jobject textureObject = env->CallObjectMethod(obj, getTextureMethodID, i); 
        if (textureObject == NULL)
        {
            LOG("GetTexture() returned zero pointer");
            return;
        }
        LOG("Texture Registered");
        textures[i] = Texture::create(env, textureObject);
    }
}


JNIEXPORT void JNICALL
Java_srdes_menupp_QcarEngine_deinitApplicationNative(
                                                        JNIEnv* env, jobject obj)
{
    LOG("Java_srdes_menupp_menupp_deinitApplicationNative");

    // Release texture resources
    if (textures != 0)
    {    
        for (int i = 0; i < textureCount; ++i)
        {
            delete textures[i];
            textures[i] = NULL;
        }
    
        delete[]textures;
        textures = NULL;
        
        textureCount = 0;
    }
}


JNIEXPORT void JNICALL
Java_srdes_menupp_QcarEngine_startCamera(JNIEnv *,
                                                                         jobject)
{
    LOG("Java_srdes_menupp_menupp_startCamera");

    // Initialize the camera:
    if (!QCAR::CameraDevice::getInstance().init())
        return;

    // Configure the video background
    configureVideoBackground();

    // Select the default mode:
    if (!QCAR::CameraDevice::getInstance().selectVideoMode(
                                QCAR::CameraDevice::MODE_DEFAULT))
        return;

    // Start the camera:
    if (!QCAR::CameraDevice::getInstance().start())
        return;

    // Uncomment to enable flash
    //if(QCAR::CameraDevice::getInstance().setFlashTorchMode(true))
    //	LOG("IMAGE TARGETS : enabled torch");

    // Uncomment to enable infinity focus mode, or any other supported focus mode
    // See CameraDevice.h for supported focus modes
    //if(QCAR::CameraDevice::getInstance().setFocusMode(QCAR::CameraDevice::FOCUS_MODE_INFINITY))
    //	LOG("IMAGE TARGETS : enabled infinity focus");

    // Start the tracker:
    QCAR::Tracker::getInstance().start();
 
    // Cache the projection matrix:
    const QCAR::Tracker& tracker = QCAR::Tracker::getInstance();
    const QCAR::CameraCalibration& cameraCalibration =
                                    tracker.getCameraCalibration();
    projectionMatrix = QCAR::Tool::getProjectionGL(cameraCalibration, 2.0f,
                                            2000.0f);
}


JNIEXPORT void JNICALL
Java_srdes_menupp_QcarEngine_stopCamera(JNIEnv *,
                                                                   jobject)
{
    LOG("Java_srdes_menupp_menupp_stopCamera");

    QCAR::Tracker::getInstance().stop();

    QCAR::CameraDevice::getInstance().stop();
    QCAR::CameraDevice::getInstance().deinit();
}

JNIEXPORT jboolean JNICALL
Java_srdes_menupp_QcarEngine_toggleFlash(JNIEnv*, jobject, jboolean flash)
{
    return QCAR::CameraDevice::getInstance().setFlashTorchMode((flash==JNI_TRUE)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_srdes_menupp_QcarEngine_autofocus(JNIEnv*, jobject)
{
    return QCAR::CameraDevice::getInstance().startAutoFocus()?JNI_TRUE:JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_srdes_menupp_QcarEngine_setFocusMode(JNIEnv*, jobject, jint mode)
{
    return QCAR::CameraDevice::getInstance().setFocusMode(mode)?JNI_TRUE:JNI_FALSE;
}


JNIEXPORT void JNICALL
Java_srdes_menupp_menuppRenderer_initRendering(
                                                    JNIEnv* env, jobject obj)
{
    LOG("Java_srdes_menupp_menuppRenderer_initRendering");

    // Define clear color
    glClearColor(0.0f, 0.0f, 0.0f, QCAR::requiresAlpha() ? 0.0f : 1.0f);
    
    // Now generate the OpenGL texture objects and add settings
    for (int i = 0; i < textureCount; ++i)
    {
        glGenTextures(1, &(textures[i]->mTextureID));
        glBindTexture(GL_TEXTURE_2D, textures[i]->mTextureID);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, textures[i]->mWidth,
                textures[i]->mHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE,
                (GLvoid*)  textures[i]->mData);
    }
#ifndef USE_OPENGL_ES_1_1
  
    shaderProgramID     = SampleUtils::createProgramFromBuffer(cubeMeshVertexShader,
                                                            cubeFragmentShader);

    vertexHandle        = glGetAttribLocation(shaderProgramID,
                                                "vertexPosition");
    normalHandle        = glGetAttribLocation(shaderProgramID,
                                                "vertexNormal");
    textureCoordHandle  = glGetAttribLocation(shaderProgramID,
                                                "vertexTexCoord");
    mvpMatrixHandle     = glGetUniformLocation(shaderProgramID,
                                                "modelViewProjectionMatrix");

#endif

    // OpenGL setup for Virtual Buttons
    vbShaderProgramID   = SampleUtils::createProgramFromBuffer(lineMeshVertexShader,
                                                               lineFragmentShader);

    vbVertexHandle      = glGetAttribLocation(vbShaderProgramID, "vertexPosition");

}


JNIEXPORT void JNICALL
Java_srdes_menupp_menuppRenderer_updateRendering(
                        JNIEnv* env, jobject obj, jint width, jint height)
{
    LOG("Java_srdes_menupp_menuppRenderer_updateRendering");
    
    // Update screen dimensions
    screenWidth = width;
    screenHeight = height;

    // Reconfigure the video background
    configureVideoBackground();
}


#ifdef __cplusplus
}
#endif
