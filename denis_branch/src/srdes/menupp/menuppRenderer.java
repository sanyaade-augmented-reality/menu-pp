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
import android.os.Bundle;

import com.qualcomm.QCAR.QCAR;


/** The renderer class for the ImageTargets sample. */
public class menuppRenderer implements GLSurfaceView.Renderer
{
    public boolean mIsActive = false;
    
    // Context that calls rendering frame
    public static Context context;
    
    /** Native function for initializing the renderer. */
    public native void initRendering();
    
    
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
        renderFrame();
    }
    
    public void entreeTabManage(int textureId) {
    	Intent intent = new Intent (context, EntreeTabManage.class);
    	Bundle extras = new Bundle();
    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	extras.putInt("key_entree_id", textureId);
    	intent.putExtra("extras", extras);
    	context.startActivity(intent);
    }
    
    public void initTargetInfo(int size){
    	menupp.entrees = new Entree[size];
		menupp.entreeIndex = 0;
		for(int i = 0; i < size; i++){
			menupp.entrees[i] = new Entree();
		}
        DebugLog.LOGD("Entree array created with size" + Integer.toString(size));
    }
    /*
    public void addTargetInfo(String name, int id){
    	//Entree e = new Entree(name, id);
    	String s = "adding entree to list: " + name + " " + id;
    	DebugLog.LOGD(s);
    	if(menupp.entreeIndex < menupp.entrees.length){
    		
			menupp.entrees[menupp.entreeIndex].setId(id);
			menupp.entrees[menupp.entreeIndex].setName(name);
			menupp.entreeIndex++;
		}
    	s = "added entree to list: " + name + " " + id;
    	DebugLog.LOGD(s);
    } */
    
    public void addTargetsInfo(String [] names, int [] ids){
    	menupp.entrees = new Entree[ids.length];
        DebugLog.LOGD("Entree array created with size " + Integer.toString(menupp.entrees.length) + " array length " + Integer.toString(names.length) + " " + Integer.toString(ids.length));
		//menupp.entreeIndex = 0;
		for(int i = 0; i < menupp.entrees.length; i++){
	        DebugLog.LOGD("Creating entree " + names[i] + " with id " + ids[i]);
	        menupp.entrees[i] = new Entree(names[i], ids[i]);
		}
		DebugLog.LOGD("Finished initializing target info. Returning to native code.");
    }
	
	public static Entree findEntreeById(int id){
		Entree to_return = null;
		for(int i = 0; i < menupp.entrees.length; i++){
			if(menupp.entrees[i].getId() == id){
				to_return = menupp.entrees[i];
				break;
			}
		}
		return to_return;
		//return menupp.entrees[id];
	}
}
