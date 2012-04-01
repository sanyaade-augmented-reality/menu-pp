/**
 * @file	menupp.cpp
 *
 * @author	Aaron Alaniz (webheadz3@gmail.com)
 *
 * @brief	This code provides services for Menu++ to interface with Qualcomm's
 * 			Augmented Reality SDK. With these services we are able render textures
 * 			in the camera environment. The virtual buttons and tracking of the image
 * 			targets are thanks to Qualcomm's libraries.
 *
 * @note    Copyright (c) 2010-2011 QUALCOMM Incorporated.
 *          All Rights Reserved.
 *          Qualcomm Confidential and Proprietary
 **/
#include <jni.h>
#include <android/log.h>
#include <stdio.h>
#include <string.h>
#include <math.h>
#include <assert.h>
#include <unistd.h>
#include <time.h>
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

// Local imports
#include "Planes.h"
#include "Utils.h"
#include "Texture.h"
#include "CubeShaders.h"
#include "Menupp.h"

#ifdef __cplusplus
extern "C"
{
#endif

// OpenGL ES 2.0 specific (Virtual Buttons):
unsigned int vbShaderProgramID  = 0;
GLint vbVertexHandle            = 0;
int buttonMask                  = 0;
bool buttonPressed 				= false;

// Virtual Button runtime creation:
bool updateBtns                 = false;
const int NUM_BUTTONS           = 3;

// Touch screen button
bool displayedMessage 			= false;

// Texture defines
int textureCount                = 0;
Texture** textures              = 0;
int textureCeiling				= 0;
int textureFloor				= 0;

// Entrees defines
int entreeCount 				= 0;
int entreeImageBase 			= 0;
int entreeNameBase 				= 0;

// OpenGL ES 2.0 specific
unsigned int shaderProgramID    = 0;
GLint vertexHandle              = 0;
GLint normalHandle              = 0;
GLint textureCoordHandle        = 0;
GLint mvpMatrixHandle           = 0;

// Screen dimensions:
unsigned int screenWidth        = 0;
unsigned int screenHeight       = 0;
float halfScreenWidth 			= 0;
float halfScreenHeight			= 0;

// Parameters to internalize Java environment
JNIEnv* javaEnv;
jobject javaObj;
jclass javaClass;

// Indicates whether screen is in portrait (true) or landscape (false) mode
bool isActivityInPortraitMode   = false;

// The projection matrix used for rendering virtual objects:
QCAR::Matrix44F projectionMatrix;

// Timer variables used for throwing away frames.
static time_t updateBegin;
static time_t updateEnd;
static double updateTimeUsed 	= 3.5;
static bool captureUpdateTime 	= false;
static time_t focusBegin;
static time_t focusEnd;
static time_t focusTimeUsed 	= 3.5;
static bool captureFocusTime 	= false;

/**
 * @brief	Returns the current openGL ES version.
 */
JNIEXPORT int JNICALL
Java_srdes_menupp_QcarEngine_getOpenGlEsVersionNative(JNIEnv *, jobject)
{
#ifdef USE_OPENGL_ES_1_1
    return 1;
#else
    return 2;
#endif
}

/**
 * @brief	Passes message to java environment to display a toast message.
 */
void displayMessage(char* message)
{
    // Use the environment and class stored in initNativeCallback
    // to call a Java method that displays a message via a toast
    jstring js = javaEnv->NewStringUTF(message);
    jmethodID method = javaEnv->GetMethodID(javaClass, "displayMessage", "(Ljava/lang/String;)V");
    javaEnv->CallVoidMethod(javaObj, method, js);
}
/**
 * @brief	Initializes a callback functionality.
 */
JNIEXPORT void JNICALL
Java_srdes_menupp_menuppRenderer_initNativeCallback(JNIEnv* env, jobject obj)
{
    // Store the java environment for later use
    // Note that this environment is only safe for use in this thread
    javaEnv = env;

    // Store the calling object for later use
    // Make a global reference to keep it valid beyond the scope of this function
    javaObj = env->NewGlobalRef(obj);

    // Store the class of the calling object for later use
    jclass objClass = env->GetObjectClass(obj);
    javaClass = (jclass) env->NewGlobalRef(objClass);
}

/**
 * @brief	Sets activity mode.
 */
JNIEXPORT void JNICALL
Java_srdes_menupp_QcarEngine_setActivityPortraitMode(JNIEnv *, jobject, jboolean isPortrait)
{
    isActivityInPortraitMode = isPortrait;
}

/**
 * @brief	Add a button to the list of buttons which are toggled in the next update call.
 */
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
    }
    updateBtns = true;
}

/**
 * @brief	Sets the maximum number of trackables.
 */
JNIEXPORT void JNICALL
Java_srdes_menupp_QcarEngine_onQCARInitializedNative(JNIEnv *, jobject)
{
    LOG("Java_srdes_menupp_QcarEngine_onQCARInitializedNative");
    QCAR::setHint(QCAR::HINT_MAX_SIMULTANEOUS_IMAGE_TARGETS, 3);
}

/**
 * @brief	Create/destroy a Virtual Button at runtime.
 *
 * @note	This will NOT work if the tracker is active!
 */
bool toggleVirtualButton(QCAR::ImageTarget* imageTarget, const char* name,
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

/**
 * @brief	Object to receive update callbacks from QCAR SDK.
 */
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
                LOG("Toggle button 1");

                toggleVirtualButton(imageTarget, "enchiladas", -180, 180, 180, -180);
            }
            if (buttonMask & BUTTON_2)
            {
                LOG("Toggle button 2");

                toggleVirtualButton(imageTarget, "hotdog", -180, 180, 180, -180);
            }
            if (buttonMask & BUTTON_3)
            {
                LOG("Toggle button 3");

                toggleVirtualButton(imageTarget, "pizza", -180, 180, 180, -180);
            }
            buttonMask = 0;
            updateBtns = false;
        }
    }
} qcarUpdate;

/**
 * @brief	This function renders frames and attempts to detect trackables.
 * 			If a trackable is detected the appropriate textures are loaded,
 * 			manipulated and bound to geometric planes in the camera environment.
 * 			If a virtual button is pressed this function passes control back to
 * 			the java environment for the user to inspect entree information.
 */
JNIEXPORT void JNICALL
Java_srdes_menupp_menuppRenderer_renderFrame(JNIEnv *env, jobject obj)
{
	int trackableId;
	QCAR::Matrix44F entreeImageMatrix, entreeNameMatrix;

    // Clear color and depth buffer
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    // Render video background:
    QCAR::State state = QCAR::Renderer::getInstance().begin();

    glEnable(GL_DEPTH_TEST);
    glEnable(GL_CULL_FACE);

    // If this is our first time seeing the target, display a tip
    if (!displayedMessage) {
        displayMessage("Tap the screen to focus at anytime.");
        displayedMessage = true;
    }

    if (captureUpdateTime)
    {
    	updateEnd = time(NULL);
    	updateTimeUsed = ((double) updateEnd - updateBegin);
    }

    if (captureFocusTime) {
    	focusEnd = time(NULL);
    	focusTimeUsed = ((double) focusEnd - focusBegin);
    }
    // Did we find any trackables this frame?
    for(int i = 0 ; i < state.getNumActiveTrackables() && i < textureCeiling; i++)
    {
        // Get the trackable:
        const QCAR::Trackable* trackable = state.getActiveTrackable(i);

        // Capture Id
        trackableId = trackable->getId();

        entreeImageMatrix = QCAR::Tool::convertPose2GLMatrix(trackable->getPose());

        entreeNameMatrix = QCAR::Tool::convertPose2GLMatrix(trackable->getPose());

        // The image target:
        assert(trackable->getType() == QCAR::Trackable::IMAGE_TARGET);
        const QCAR::ImageTarget* target = static_cast<const QCAR::ImageTarget*>(trackable);

        const QCAR::VirtualButton* button = target->getVirtualButton(0);

        // Choose the texture based on the target name:
        Texture* imgTexture = textures[trackableId];

		// Place an image on the target using a 3D plane
		QCAR::Matrix44F entreeImageProjection;
		QCAR::Matrix44F entreeNameProjection;

        // If the button is pressed, than use this texture:
        if (button->isPressed() && !buttonPressed && updateTimeUsed > 3 && focusTimeUsed > 3)
        {
			buttonPressed = true;
			captureUpdateTime = false;
			jclass javaClass = env->GetObjectClass(obj);
			jmethodID method = env->GetMethodID(javaClass, "entreeTabManage", "(I)V");
			env->CallVoidMethod(obj, method, imgTexture->getId());

			// Clean up
			glDisable(GL_DEPTH_TEST);
			glDisableVertexAttribArray(vertexHandle);
			glDisableVertexAttribArray(normalHandle);
			glDisableVertexAttribArray(textureCoordHandle);
			QCAR::Renderer::getInstance().end();
            return;
        }

        //  Position and size the plane for the entree description
		Utils::translatePoseMatrix(0.0f, 250.0f, 0.0f, &entreeNameMatrix.data[0]);
		Utils::scalePoseMatrix(150, 100, 1.0f, &entreeNameMatrix.data[0]);
		Utils::multiplyMatrix(&projectionMatrix.data[0], &entreeNameMatrix.data[0], &entreeNameProjection.data[0]);

		// Install program object to be apart of renderering
		glUseProgram(shaderProgramID);

	    // Enable 2D Textures
	    glEnable(GL_TEXTURE_2D);

		// Establish dimensions of the plane and bound texture
		glVertexAttribPointer(vertexHandle, 3, GL_FLOAT, GL_FALSE, 0, (const GLvoid*) &rectPlaneVertices[0]);
		glVertexAttribPointer(normalHandle, 3, GL_FLOAT, GL_FALSE, 0, (const GLvoid*) &rectNormals[0]);
		glVertexAttribPointer(textureCoordHandle, 2, GL_FLOAT, GL_FALSE, 0, (const GLvoid*) &rectTexCoords[0]);

		// Enable vertex handles
		glEnableVertexAttribArray(vertexHandle);
		glEnableVertexAttribArray(normalHandle);
		glEnableVertexAttribArray(textureCoordHandle);

		// Bind the appropriate entree description to the plane and draw the image
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, textures[entreeNameBase + (trackableId % textureCeiling)]->mTextureID);
		glUniformMatrix4fv(mvpMatrixHandle, 1, GL_FALSE, (GLfloat*)&entreeNameProjection.data[0] );
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, (const GLvoid*) &rectIndices[0]);

		// Position and size the plane for the entree image
		Utils::translatePoseMatrix(0.0f, 0.0f, 0.0f, &entreeImageMatrix.data[0]);
		Utils::scalePoseMatrix(375, 375, 1.0f, &entreeImageMatrix.data[0]);
		Utils::multiplyMatrix(&projectionMatrix.data[0], &entreeImageMatrix.data[0], &entreeImageProjection.data[0]);

		// Establish dimensions of the plane and bound textures
		glVertexAttribPointer(vertexHandle, 3, GL_FLOAT, GL_FALSE, 0, (const GLvoid*) &planeVertices[0]);
		glVertexAttribPointer(normalHandle, 3, GL_FLOAT, GL_FALSE, 0, (const GLvoid*) &planeNormals[0]);
		glVertexAttribPointer(textureCoordHandle, 2, GL_FLOAT, GL_FALSE, 0, (const GLvoid*) &planeTexCoords[0]);

		// Enable vertex handles again
		glEnableVertexAttribArray(vertexHandle);
		glEnableVertexAttribArray(normalHandle);
		glEnableVertexAttribArray(textureCoordHandle);

		// Bind the correct entree image and draw
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, textures[entreeImageBase + (trackableId % textureCeiling)]->mTextureID);
		glUniformMatrix4fv(mvpMatrixHandle, 1, GL_FALSE, (GLfloat*)&entreeImageProjection.data[0] );

		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, (const GLvoid*) &planeIndices[0]);
    }

    // Clean up
    glDisable(GL_DEPTH_TEST);
    glDisableVertexAttribArray(vertexHandle);
    glDisableVertexAttribArray(normalHandle);
    glDisableVertexAttribArray(textureCoordHandle);
    QCAR::Renderer::getInstance().end();
}
/**
 * @brief	Configures the video background and orientation of the screen.
 */
void configureVideoBackground()
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
        LOG("configureVideoBackground PORTRAIT");
        config.mSize.data[0] = videoMode.mHeight * (screenHeight / (float)videoMode.mWidth);
        config.mSize.data[1] = screenHeight;
    }
    else
    {
        LOG("configureVideoBackground LANDSCAPE");
        config.mSize.data[0] = screenWidth;
        config.mSize.data[1] = videoMode.mHeight * (screenWidth / (float)videoMode.mWidth);
    }

    // Set the config:
    QCAR::Renderer::getInstance().setVideoBackgroundConfig(config);
}

/**
 * @brief	Initializes screen size and stores all the textures
 * 			in the native environment.
 */
JNIEXPORT void JNICALL
Java_srdes_menupp_QcarEngine_initApplicationNative( JNIEnv* env, jobject obj, jint width, jint height)
{
    LOG("Java_srdes_menupp_menupp_initApplicationNative");

    // Store screen dimensions
    screenWidth = width;
    screenHeight = height;
    halfScreenWidth = screenWidth / 2.0;
    halfScreenHeight = screenHeight / 2.0;

    // Handle to the activity class:
    jclass activityClass = env->GetObjectClass(obj);

    // Register callback function that gets called every time a tracking cycle
    // has finished and we have a new AR state avaible
    QCAR::registerCallback(&qcarUpdate);

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

    textureCeiling = ((textureCount / 2) < MAX_TRACKABLES) ? (textureCount / 2) : (MAX_TRACKABLES);

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

/**
 * @brief	Deinits the application in the native environment
 * 			and frees the texture resources.
 */
JNIEXPORT void JNICALL
Java_srdes_menupp_QcarEngine_deinitApplicationNative(JNIEnv* env, jobject obj)
{
    LOG("Java_srdes_menupp_menupp_deinitApplicationNative");

    displayedMessage = false;
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

/**
 * @brief	Initializes and starts the camera.
 */
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

/**
 * @brief	Stops and turns off the camera.
 */
JNIEXPORT void JNICALL
Java_srdes_menupp_QcarEngine_stopCamera(JNIEnv *, jobject)
{
    LOG("Java_srdes_menupp_menupp_stopCamera");

    QCAR::Tracker::getInstance().stop();

    QCAR::CameraDevice::getInstance().stop();
    QCAR::CameraDevice::getInstance().deinit();
}

/**
 * @brief	Toggles the flash.
 */
JNIEXPORT jboolean JNICALL
Java_srdes_menupp_QcarEngine_toggleFlash(JNIEnv*, jobject, jboolean flash)
{
    jmethodID method = javaEnv->GetMethodID(javaClass, "toggleFlashButton", "()V");
    javaEnv->CallVoidMethod(javaObj, method);
    return QCAR::CameraDevice::getInstance().setFlashTorchMode((flash==JNI_TRUE)) ? JNI_TRUE : JNI_FALSE;
}

/**
 * @brief	This function focuses the camera.
 */
JNIEXPORT jboolean JNICALL
Java_srdes_menupp_QcarEngine_autofocus(JNIEnv*, jobject)
{
	focusBegin = time(NULL);
	captureFocusTime = true;
    return QCAR::CameraDevice::getInstance().startAutoFocus()?JNI_TRUE:JNI_FALSE;
}

/**
 * @brief	Called prior to rendering the first frame. This function
 * 			initializes the textures created to represent the entrees
 * 			in the augmented reality. After initialized the texture information
 * 			is passed back to the java code to be cached and used later.
 */
JNIEXPORT void JNICALL
Java_srdes_menupp_menuppRenderer_initRendering(JNIEnv* env, jobject obj)
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
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, textures[i]->mWidth, textures[i]->mHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, (GLvoid*)  textures[i]->mData);
    }

    shaderProgramID     = Utils::createProgramFromBuffer(cubeMeshVertexShader, cubeFragmentShader);
    vertexHandle        = glGetAttribLocation(shaderProgramID, "vertexPosition");
    normalHandle        = glGetAttribLocation(shaderProgramID, "vertexNormal");
    textureCoordHandle  = glGetAttribLocation(shaderProgramID, "vertexTexCoord");
    mvpMatrixHandle     = glGetUniformLocation(shaderProgramID, "modelViewProjectionMatrix");

    // OpenGL setup for Virtual Buttons
    vbShaderProgramID   = Utils::createProgramFromBuffer(lineMeshVertexShader, lineFragmentShader);
    vbVertexHandle      = glGetAttribLocation(vbShaderProgramID, "vertexPosition");

    // Render video background:
    QCAR::State state = QCAR::Renderer::getInstance().begin();

    entreeImageBase = 0;
    entreeNameBase = textureCount / 2;

	// Java types to be passed back to menuppRenderer
    int trackableId;
	jclass jstringClass = env->FindClass("java/lang/String");
	jintArray jids = env->NewIntArray(textureCount / 2);
	jobjectArray jnames = env->NewObjectArray(textureCount / 2, jstringClass, env->NewStringUTF(""));
	jclass javaClass = env->GetObjectClass(obj);
	jmethodID method = env->GetMethodID(javaClass, "addTargetsInfo", "([Ljava/lang/String;[I)V");

	for (int i = 0 ; i < textureCount / 2 ; i++)
	{
		string trackableName = (string) textures[i]->getName();
		trackableId = (int) textures[i]->getId();
		env->SetIntArrayRegion(jids, i, 1, &trackableId);
		env->SetObjectArrayElement(jnames, i, env->NewStringUTF(trackableName));
	}

	env->CallVoidMethod(obj, method, jnames, jids);
	QCAR::Renderer::getInstance().end();
}

/**
 * @brief	Called after an openGl Surface is updated.
 *
 * @param	Screen width.
 *
 * @param	Screen height.
 */
JNIEXPORT void JNICALL
Java_srdes_menupp_menuppRenderer_updateRendering(
                        JNIEnv* env, jobject obj, jint width, jint height)
{
    LOG("Java_srdes_menupp_menuppRenderer_updateRendering");

    // Update screen dimensions
    screenWidth = width;
    screenHeight = height;

    if (buttonPressed) {
    	buttonPressed = false;
    	captureUpdateTime = true;
    	updateBegin = time(NULL);
    }

    // Reconfigure the video background
    configureVideoBackground();
}

/**
 * @brief	Updates image and name indices to reflect turning a virtual
 * 			menu page.
 */
JNIEXPORT void JNICALL
Java_srdes_menupp_GUIManager_nativeNext(JNIEnv* env, jobject obj)
{
	entreeImageBase = (entreeImageBase + textureCeiling) % (textureCount / 2);
	entreeNameBase = entreeImageBase + (textureCount / 2);
	textureCeiling = ((textureCount / 2) - entreeImageBase < MAX_TRACKABLES) ?  (textureCount / 2 - entreeImageBase) : (MAX_TRACKABLES);
}

/**
 * @brief	Updates image and name indices to reflect turning
 * 			a virtual page back.
 */
JNIEXPORT void JNICALL
Java_srdes_menupp_GUIManager_nativeBack(JNIEnv* env, jobject obj)
{
	entreeImageBase = (entreeImageBase - MAX_TRACKABLES < 0) ? ((textureCount / 2) + (entreeImageBase - MAX_TRACKABLES) + (MAX_TRACKABLES - (((textureCount / 2) % MAX_TRACKABLES) ? ((textureCount / 2) % MAX_TRACKABLES) : (MAX_TRACKABLES)))) : (entreeImageBase - MAX_TRACKABLES);
	entreeNameBase = entreeImageBase + (textureCount / 2);
	textureCeiling = ((textureCount / 2) - entreeImageBase < MAX_TRACKABLES) ?  (textureCount / 2 - entreeImageBase) : (MAX_TRACKABLES);
}
#ifdef __cplusplus
}
#endif

