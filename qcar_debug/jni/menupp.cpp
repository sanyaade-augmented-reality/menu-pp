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

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

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
#include "EntreeTarget.h"

#ifdef __cplusplus
extern "C"
{
#endif

// Keep reference of the entree targets
//static EntreeTargets* entreeTargets;

// Plane Definitions
// Define a plane to bind an image to..
static const float planeVertices[] =
{//	  x	    y	 z
    -0.5, -0.5, 0.0,
     0.5, -0.5, 0.0,
     0.5,  0.5, 0.0,
    -0.5,  0.5, 0.0,
};

static const float rectPlaneVertices[] =
{//	  x 	y	 z
    -1.5, -0.5, 0.0,
     1.5, -0.5, 0.0,
     1.5,  0.5, 0.0,
    -1.5,  0.5, 0.0,
};

static const float planeTexcoords[] =
{//	 x	  y
    0.0, 0.0,
    1.0, 0.0,
    1.0, 1.0,
    0.0, 1.0
};

static const float rectTexCoords[] =
{//  x    y
    0.0, 0.0,
    1.0, 0.0,
    1.0, 1.0,
    0.0, 1.0
};

static const float planeNormals[] =
{//	 x    y    z
    0.0, 0.0, 1.0,
    0.0, 0.0, 1.0,
    0.0, 0.0, 1.0,
    0.0, 0.0, 1.0
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
    BUTTON_4                    = 8,
    BUTTON_5					= 16
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
const int NUM_BUTTONS             = 5;


// Textures:
int textureCount                = 0;
Texture** textures              = 0;

// Entrees
int entreeCount = 0;
EntreeTarget** entreeTargets = 0;
int entreeInfoBase = 0;

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
static const float kObjectScale = 300;


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

        case 4:
            buttonMask |= BUTTON_5;
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
                LOG("Toggle enchilada button");

                toggleVirtualButton(imageTarget, "enchiladas", -180, 180, 180, -180);

            }
            if (buttonMask & BUTTON_2)
            {
                LOG("Toggle hot dog button");

                toggleVirtualButton(imageTarget, "hotdog", -180, 180, 180, -180);
            }
            if (buttonMask & BUTTON_3)
            {
                LOG("Toggle pizza button");

                toggleVirtualButton(imageTarget, "pizza", -180, 180, 180, -180);
            }

            if (buttonMask & BUTTON_4)
            {
                LOG("Toggle omelete button");

                toggleVirtualButton(imageTarget, "omelete", -180, 180, 180, -180);
            }

            if (buttonMask & BUTTON_5)
            {
            	LOG("Toggle burger button.");

            	toggleVirtualButton(imageTarget, "burger", -180, 180, 180, -180);
            }

            buttonMask = 0;
            updateBtns = false;
        }
    }
} qcarUpdate;

JNIEXPORT void JNICALL
Java_srdes_menupp_menuppRenderer_renderFrame(JNIEnv *env, jobject obj)
{
	int trackableId;
    //LOG("Java_com_qualcomm_QCARSamples_ImageTargets_GLRenderer_renderFrame");

    // Clear color and depth buffer 
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    // Render video background:
    QCAR::State state = QCAR::Renderer::getInstance().begin();

    glEnable(GL_DEPTH_TEST);
    glEnable(GL_CULL_FACE);

    // Did we find any trackables this frame?
    for(int i = 0 ; i < state.getNumActiveTrackables(); i++)
    {
        // Get the trackable:
        const QCAR::Trackable* trackable = state.getActiveTrackable(i);
        QCAR::Matrix44F modelViewMatrix = QCAR::Tool::convertPose2GLMatrix(trackable->getPose());

        // The image target:
        assert(trackable->getType() == QCAR::Trackable::IMAGE_TARGET);
        const QCAR::ImageTarget* target = static_cast<const QCAR::ImageTarget*>(trackable);

        const QCAR::VirtualButton* button = target->getVirtualButton(0);

        // Capture Id
        trackableId = trackable->getId();

        // Choose the texture based on the target name:
        Texture* imgTexture;

        imgTexture = textures[trackableId];

		// Place an image on the target using a 3D plane
		QCAR::Matrix44F modelViewProjection;

		if (button->isPressed() || entreeTargets[trackableId]->itemSelected == true)
		{
			entreeTargets[trackableId]->itemSelected = true;
			SampleUtils::translatePoseMatrix(-900.0f, 0.0f, 0.0f, &modelViewMatrix.data[0]);
			//SampleUtils::scalePoseMatrix(kObjectScale, kObjectScale, 1.0f, &modelViewMatrix.data[0]);
			SampleUtils::scalePoseMatrix(400, 400, 1.0f, &modelViewMatrix.data[0]);
			SampleUtils::multiplyMatrix(&projectionMatrix.data[0], &modelViewMatrix.data[0], &modelViewProjection.data[0]);

			glUseProgram(shaderProgramID);

			glVertexAttribPointer(vertexHandle, 3, GL_FLOAT, GL_FALSE, 0, (const GLvoid*) &rectPlaneVertices[0]);
			glVertexAttribPointer(normalHandle, 3, GL_FLOAT, GL_FALSE, 0, (const GLvoid*) &planeNormals[0]);
			glVertexAttribPointer(textureCoordHandle, 2, GL_FLOAT, GL_FALSE, 0, (const GLvoid*) &rectTexCoords[0]);
			imgTexture = textures[entreeInfoBase + trackableId];
		}
		else
		{
			SampleUtils::translatePoseMatrix(0.0f, 0.0f, 0.0f, &modelViewMatrix.data[0]);
			SampleUtils::scalePoseMatrix(kObjectScale, kObjectScale, 1.0f, &modelViewMatrix.data[0]);
			SampleUtils::multiplyMatrix(&projectionMatrix.data[0], &modelViewMatrix.data[0], &modelViewProjection.data[0]);

			glUseProgram(shaderProgramID);

			glVertexAttribPointer(vertexHandle, 3, GL_FLOAT, GL_FALSE, 0, (const GLvoid*) &planeVertices[0]);
			glVertexAttribPointer(normalHandle, 3, GL_FLOAT, GL_FALSE, 0, (const GLvoid*) &planeNormals[0]);
			glVertexAttribPointer(textureCoordHandle, 2, GL_FLOAT, GL_FALSE, 0, (const GLvoid*) &planeTexcoords[0]);
		}

		glEnableVertexAttribArray(vertexHandle);
		glEnableVertexAttribArray(normalHandle);
		glEnableVertexAttribArray(textureCoordHandle);

		glBindTexture(GL_TEXTURE_2D, imgTexture->mTextureID);
		glUniformMatrix4fv(mvpMatrixHandle, 1, GL_FALSE, (GLfloat*)&modelViewProjection.data[0] );
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, (const GLvoid*) &planeIndices[0]);
    }

    glDisable(GL_DEPTH_TEST);

    glDisableVertexAttribArray(vertexHandle);
    glDisableVertexAttribArray(normalHandle);
    glDisableVertexAttribArray(textureCoordHandle);

    QCAR::Renderer::getInstance().end();
}



void
configureVideoBackground()
{
    // Get the default video mode:
    QCAR::CameraDevice& cameraDevice = QCAR::CameraDevice::getInstance();
    QCAR::VideoMode videoMode = cameraDevice.getVideoMode(QCAR::CameraDevice::MODE_DEFAULT);


    // Configure the video background
    QCAR::VideoBackgroundConfig config;
    config.mEnabled = true;
    config.mSynchronous = true;
    config.mPosition.data[0] = 0.0f;
    config.mPosition.data[1] = 0.0f;
    
    if (isActivityInPortraitMode)
    {
        //LOG("configureVideoBackground PORTRAIT");
        config.mSize.data[0] = videoMode.mHeight * (screenHeight / (float)videoMode.mWidth);
        config.mSize.data[1] = screenHeight;
    }
    else
    {
        //LOG("configureVideoBackground LANDSCAPE");
        config.mSize.data[0] = screenWidth;
        config.mSize.data[1] = videoMode.mHeight * (screenWidth / (float)videoMode.mWidth);
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

    jmethodID getTextureCountMethodID = env->GetMethodID(activityClass, "getTextureCount", "()I");
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

    jmethodID getTextureMethodID = env->GetMethodID(activityClass, "getTexture", "(I)Lsrdes/menupp/Texture;");

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
        textures[i] = Texture::create(env, textureObject);
    }
}


JNIEXPORT void JNICALL
Java_srdes_menupp_QcarEngine_deinitApplicationNative(JNIEnv* env, jobject obj)
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
Java_srdes_menupp_QcarEngine_startCamera(JNIEnv *, jobject)
{
    LOG("Java_srdes_menupp_menupp_startCamera");

    // Initialize the camera:
    if (!QCAR::CameraDevice::getInstance().init())
        return;

    // Configure the video background
    configureVideoBackground();

    // Select the default mode:
    if (!QCAR::CameraDevice::getInstance().selectVideoMode(QCAR::CameraDevice::MODE_DEFAULT))
        return;

    // Start the camera:
    if (!QCAR::CameraDevice::getInstance().start())
        return;

    // Start the tracker:
    QCAR::Tracker::getInstance().start();
 
    // Cache the projection matrix:
    const QCAR::Tracker& tracker = QCAR::Tracker::getInstance();
    const QCAR::CameraCalibration& cameraCalibration = tracker.getCameraCalibration();
    projectionMatrix = QCAR::Tool::getProjectionGL(cameraCalibration, 2.0f, 2000.0f);
}


JNIEXPORT void JNICALL
Java_srdes_menupp_QcarEngine_stopCamera(JNIEnv *, jobject)
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
    
    glEnable(GL_TEXTURE_2D);

    // Now generate the OpenGL texture objects and add settings
    for (int i = 0; i < textureCount; ++i)
    {
        glGenTextures(1, &(textures[i]->mTextureID));
        glBindTexture(GL_TEXTURE_2D, textures[i]->mTextureID);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, textures[i]->mWidth, textures[i]->mHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, (GLvoid*)  textures[i]->mData);
    }
#ifndef USE_OPENGL_ES_1_1

    shaderProgramID     = SampleUtils::createProgramFromBuffer(cubeMeshVertexShader, cubeFragmentShader);
    vertexHandle        = glGetAttribLocation(shaderProgramID, "vertexPosition");
    normalHandle        = glGetAttribLocation(shaderProgramID, "vertexNormal");
    textureCoordHandle  = glGetAttribLocation(shaderProgramID, "vertexTexCoord");
    mvpMatrixHandle     = glGetUniformLocation(shaderProgramID, "modelViewProjectionMatrix");

#endif

    // OpenGL setup for Virtual Buttons
    vbShaderProgramID   = SampleUtils::createProgramFromBuffer(lineMeshVertexShader, lineFragmentShader);
    vbVertexHandle      = glGetAttribLocation(vbShaderProgramID, "vertexPosition");

    // Render video background:
    QCAR::State state = QCAR::Renderer::getInstance().begin();

	entreeCount = state.getNumTrackables();
	entreeInfoBase = entreeCount;
	entreeTargets = new EntreeTarget*[entreeCount];

	for (int i = 0 ; i < entreeCount ; i++)
	{
		string trackableName = (string) state.getTrackable(i)->getName();
		int trackableId = state.getTrackable(i)->getId();
		entreeTargets[i] = new EntreeTarget(trackableName, trackableId);
		LOG("Created entree %s with id %d", trackableName, trackableId);
	}
	QCAR::Renderer::getInstance().end();

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
