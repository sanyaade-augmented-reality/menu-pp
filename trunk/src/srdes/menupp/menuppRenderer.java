/*==============================================================================
            Copyright (c) 2010-2011 QUALCOMM Incorporated.
            All Rights Reserved.
            Qualcomm Confidential and Proprietary
            
@file 
    ImageTargetsRenderer.java

@brief
    Sample for ImageTargets

==============================================================================*/


package srdes.menupp;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Message;
import android.view.View;

import com.qualcomm.QCAR.QCAR;

/** \brief The renderer class for the ImageTargets sample. */
public class menuppRenderer implements GLSurfaceView.Renderer
{
    public boolean mIsActive = false;
    public static boolean buttonPressed = false;

	private GUIManager mGUIManager;
    
    // Context that calls rendering frame
    public static Context context;
    
    /** Native function for initializing the renderer. */
    public native void initRendering();
    /** Native function to store Java environment information for callbacks. */
    public native void initNativeCallback();
    
    /** Native function to update the renderer. */
    public native void updateRendering(int width, int height);

    
    /** Called when the surface is created or recreated. */
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        DebugLog.LOGD("GLRenderer::onSurfaceCreated");

        // Call native function to initialize rendering:
        initRendering();
        
        // Call QCAR function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        QCAR.onSurfaceCreated();
        
        // Call native function to store information about the Java environment
        // It is important that we make this call from this thread (the rendering thread)
        // as the native code will want to make callbacks from this thread
        initNativeCallback();
    }
    
    
    /** Called when the surface changed size. */
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        DebugLog.LOGD("GLRenderer::onSurfaceChanged");
        
        // Call native function to update rendering when render surface parameters have changed:
        updateRendering(width, height);

        // Call QCAR function to handle render surface size changes:
        QCAR.onSurfaceChanged(width, height);
    }    
        
    /** The native render function. */    
    public native void renderFrame();
    
    /** Called to draw the current frame. */
    public void onDrawFrame(GL10 gl)
    {
        if (!mIsActive)
            return;

        // Call our native function to render content
        if (!buttonPressed)
        	renderFrame();
    }
    /**
     *\brief manages entree tabs
     */
    public void entreeTabManage(int textureId) {

    	buttonPressed = true;
    	Intent intent = new Intent (context, EntreeTabManage.class);
    	DebugLog.LOGD("Putting extra " + textureId);
    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	intent.putExtra("key_entree_id", textureId);
    	context.startActivity(intent);
    }
    /**
     *\brief initializes the image targets
     */
    public void initTargetInfo(int size){
    	menupp.entrees = new Entree[size];
		menupp.entreeIndex = 0;
		for(int i = 0; i < size; i++){
			menupp.entrees[i] = new Entree();
		}
        DebugLog.LOGD("Entree array created with size" + Integer.toString(size));
    }
    /**
     *\brief adds info to the image targets
     */
    public void addTargetsInfo(String [] names, int [] ids){
    	menupp.entrees = new Entree[ids.length];
        DebugLog.LOGD("Entree array created with size " + Integer.toString(menupp.entrees.length) + " array length " + Integer.toString(names.length) + " " + Integer.toString(ids.length));
		for(int i = 0; i < menupp.entrees.length; i++){
	        DebugLog.LOGD("Creating entree " + names[i] + " with id " + ids[i]);
	        menupp.entrees[i] = new Entree(names[i], ids[i], i);
		}
		DebugLog.LOGD("Finished initializing target info. Returning to native code.");
    }
    /**
     *\brief returns entree with the given id
     */
	public static Entree findEntreeById(int id){
		Entree to_return = null;
		for(int i = 0; i < menupp.entrees.length; i++){
			if(menupp.entrees[i].getId() == id){
				to_return = menupp.entrees[i];
				break;
			}
		}
		return to_return;
	}
    
    /** Called from native to display a message. */
    public void displayMessage(String text)
    {
        Message message = new Message();
        message.what = GUIManager.DISPLAY_INFO_TOAST;
        message.obj = text;
        mGUIManager.sendThreadSafeGUIMessage(message);
    }
    
    /** Called from native to toggle the start button. */
    public void toggleFlashButton()
    {
        Message message = new Message();
        message.what = GUIManager.TOGGLE_FLASH_BUTTON;
        mGUIManager.sendThreadSafeGUIMessage(message);
    }
    
    /** Setter for the gui manager. */
    public void setGUIManager(GUIManager guiManager)
    {
        mGUIManager = guiManager;
    }
}
