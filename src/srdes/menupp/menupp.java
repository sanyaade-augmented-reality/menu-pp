/*==============================================================================
            Copyright (c) 2010-2011 QUALCOMM Incorporated.
            All Rights Reserved.
            Qualcomm Confidential and Proprietary
            
@file 
    menupp.java

@brief
    Sample for menupp

==============================================================================*/


package srdes.menupp;

import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

import com.qualcomm.QCAR.QCAR;


/** The main activity for the menupp sample. */
public class menupp extends Activity implements android.view.View.OnClickListener
{
    // Application status constants:
    private static final int APPSTATUS_UNINITED         = -1;
    private static final int APPSTATUS_INIT_APP         = 0;
    private static final int APPSTATUS_INIT_QCAR_ENGINE = 1;
    private static final int APPSTATUS_INIT_APP_AR      = 2;
    private static final int APPSTATUS_INIT_TRACKER     = 3;
    private static final int APPSTATUS_INITED           = 4;
    
    // Name of the native dynamic libraries to load:
    private static final String NATIVE_LIB_SAMPLE = "menupp";    
    private static final String NATIVE_LIB_QCAR = "QCAR"; 

    // Application activities
    private MenuList menuList;
    private QcarEngine qcarEngine;
    
    // Our OpenGL view:
    public static QCARSampleGLView mGlView;
    
    // The view to display the sample splash screen:
    private View loaderScreen;
    
    // Buttons relevant for the home screen
    private Button selectRestButton;
    private Button userGuideButton;
    private Button aboutUsButton;
    
    // Our renderer:
    public static menuppRenderer mRenderer;
    
    // Display size of the device
    public static int mScreenWidth = 0;
    public static int mScreenHeight = 0;
    
    // The current application status
    private static int mAppStatus = APPSTATUS_UNINITED;

    // QCAR initialization flags
    private int mQCARFlags = 0;
    
    // The textures we will use for rendering:
    private Vector<Texture> mTextures;
    
    // Flags status of activity
    private static boolean appInitComplete = false;
    
    /** Static initializer block to load native libraries on start-up. */
    static
    {
        loadLibrary(NATIVE_LIB_QCAR);
        loadLibrary(NATIVE_LIB_SAMPLE);
    }
    
    
    /** Called when the activity first starts or the user navigates back
     * to an activity. */
    protected void onCreate(Bundle savedInstanceState)
    {
        DebugLog.LOGD("menupp::onCreate");
        super.onCreate(savedInstanceState);

    }


   /** Called when the activity will start interacting with the user.*/
    protected void onResume()
    {
        DebugLog.LOGD("menupp::onResume");
        super.onResume();
        
        if (mAppStatus == APPSTATUS_UNINITED) {
        	// Update the application status to start initializing application
        	updateApplicationStatus(APPSTATUS_INIT_APP);
        } else {
        	updateApplicationStatus(APPSTATUS_INITED);
        }    
    }
    

    /** Called when the system is about to start resuming a previous activity.*/
    protected void onPause()
    {
        DebugLog.LOGD("menupp::onPause");
        super.onPause();
        
    }
    
    
    /** The final call you receive before your activity is destroyed.*/
    protected void onDestroy()
    {
        DebugLog.LOGD("menupp::onDestroy");
        super.onDestroy();
        
    }

    
    /** NOTE: this method is synchronized because of a potential concurrent
     * access by menupp::onResume() and InitQCARTask::onPostExecute(). */
    public void updateApplicationStatus(int appStatus)
    {
        // Exit if there is no change in status
        if (mAppStatus == appStatus)
            return;

        // Store new status value      
        mAppStatus = appStatus;

        // Execute application state-specific actions
        switch (mAppStatus)
        {
            case APPSTATUS_INIT_APP:
                // Initialize application elements that do not rely on QCAR
                // initialization  
                initApplication();
                
                // Proceed to next application initialization status
                updateApplicationStatus(APPSTATUS_INIT_QCAR_ENGINE);
                break;
                
            case APPSTATUS_INIT_QCAR_ENGINE:
            	// Initialize qcar elements
            	startActivity(new Intent(this, QcarEngine.class));
            	break;
                
            case APPSTATUS_INITED:
                // Set the app view
                setContentView(R.layout.home_screen);
                
                // Initialize buttons on home view
                selectRestButton = (Button) findViewById(R.id.select_rest);
                selectRestButton.setOnClickListener(this);      
                userGuideButton = (Button) findViewById(R.id.user_guide);
                userGuideButton.setOnClickListener(this);
                aboutUsButton = (Button) findViewById(R.id.about_us);
                aboutUsButton.setOnClickListener(this);
                
                // Flag that class is all ready init
                appInitComplete = true;
                           	      
                break;
                
            default:
                throw new RuntimeException("Invalid application state");
        }
    }
        
    
    /** Initialize application GUI elements that are not related to AR. */
    private void initApplication()
    {
        // Set the screen orientation
        //
        // NOTE: It is recommended to set this because of the following reasons:
        //
        //    1.) Before Android 2.2 there is no reliable way to query the
        //        absolute screen orientation from an activity, therefore using 
        //        an undefined orientation is not recommended. Screen 
        //        orientation matching orientation sensor measurements is also
        //        not recommended as every screen orientation change triggers
        //        deinitialization / (re)initialization steps in internal QCAR 
        //        SDK components resulting in unnecessary overhead during 
        //        application run-time.
        //
        //    2.) Android camera drivers seem to always deliver landscape images
        //        thus QCAR SDK components (e.g. camera capturing) need to know 
        //        when we are in portrait mode. Before Android 2.2 there is no 
        //        standard, device-independent way to let the camera driver know 
        //        that we are in portrait mode as each device seems to require a
        //        different combination of settings to rotate camera preview 
        //        frames images to match portrait mode views. Because of this,
        //        we suggest that the activity using the QCAR SDK be locked
        //        to landscape mode if you plan to support Android 2.1 devices
        //        as well. Froyo is fine with both orientations.
        int screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        
        // Apply screen orientation
        setRequestedOrientation(screenOrientation);
                
        // Query display dimensions
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;

        // As long as this window is visible to the user, keep the device's
        // screen turned on and bright.
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    
    
    /** Native function to initialize the application. */
    private native void initApplicationNative(int width, int height);  

    
    /** A helper for loading native libraries stored in "libs/armeabi*". */
    public static boolean loadLibrary(String nLibName)
    {
        try
        {
            System.loadLibrary(nLibName);
            DebugLog.LOGI("Native library lib" + nLibName + ".so loaded");
            return true;
        }
        catch (UnsatisfiedLinkError ulee)
        {
            DebugLog.LOGE("The library lib" + nLibName +
                            ".so could not be loaded");
        }
        catch (SecurityException se)
        {
            DebugLog.LOGE("The library lib" + nLibName +
                            ".so was not allowed to be loaded");
        }
        
        return false;
    }

	public void onClick(View v) {
		
		switch(v.getId()) {
		
		case R.id.select_rest:
			startActivity(new Intent(this, MenuList.class));
			break;
			
		case R.id.user_guide:
	        startActivity(new Intent(this, UserGuide.class));
			break;
			
		case R.id.about_us:
			startActivity(new Intent(this, AboutUs.class));
			break;
		}
	}    
	
}
