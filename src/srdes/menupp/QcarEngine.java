package srdes.menupp;

import java.util.Vector;

import com.qualcomm.QCAR.QCAR;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
/**
 * \brief QCAR Engine.
 */
public class QcarEngine extends Activity {

	// Possible states of qcar engine
	private final int QCAR_UNINIT = -1;
	private final int QCAR_INIT = 0;
	private final int QCAR_INIT_AR = 1;
	private final int QCAR_INIT_TRACKER = 2;
	private final int QCAR_INITED = 3;
	private final int QCAR_CAMERA_STOPPED = 4;
	private final int QCAR_CAMERA_RUNNING = 5;
	
	// Status that determines current state of QcarEngine
	private int qcarStatus = QCAR_UNINIT;
	private boolean qcarInitComplete = false;
	
	// Asynchronous tasks that must be completed for initialization
	private InitQCARTask mInitQCARTask;
    private LoadTrackerTask mLoadTrackerTask;
	
	// Menu++ Renderer
	public menuppRenderer mRenderer;
	
	// Qcar init flags
	private int mQCARFlags;
	
	// Our Default Activity View
	private View loaderView;
		
    // Our OpenGL view:
    public QCARSampleGLView mGlView;
    
    // Gui Manager
    private GUIManager mGUIManager;
    
	// Textures for application
	private Vector<Texture> mTextures;
	
    private MenuItem checked;
    public static native boolean toggleFlash(boolean flash);
    private native boolean autofocus();
    private native boolean setFocusMode(int mode);
        
	// Native Function Prototypes
    /** native method for querying the OpenGL ES version.
     * Returns 1 for OpenGl ES 1.1, returns 2 for OpenGl ES 2.0. */
    public native int getOpenGlEsVersionNative();
    
    /** Native function to initialize the application. */
    private native void initApplicationNative(int width, int height);
    
    /** Native sample initialization. */
    public native void onQCARInitializedNative();    
	
    /** Native methods for starting and stopping the camera. */ 
    public static native void startCamera();
    private native void stopCamera();
    
    /** Native function to deinitialize the application.*/
    private native void deinitApplicationNative();
    
    /** Tells native code whether we are in portrait or landscape mode */
    private native void setActivityPortraitMode(boolean isPortrait);
    
    /** Native function to create/destroy a Virtual Button.
     *  Existing buttons will be destroyed and non-existing will be created. */
    private native void addButtonToToggle(int virtualButtonIdx);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		DebugLog.LOGD("QcarEngine::onCreate");
		super.onCreate(savedInstanceState);
		
		if (qcarStatus == QCAR_UNINIT) {
			
			// Set the loader view
			setContentView(R.layout.loader);
			loaderView = findViewById(R.id.loader_screen);
			
			// Query the QCAR initialization flags
			mQCARFlags = getInitializationFlags();
			
			// Load any sample specific textures
			mTextures = new Vector<Texture>();
			loadTextures();
			
			updateQcarStatus(QCAR_INIT);
		}
		
        // As long as this window is visible to the user, keep the device's
        // screen turned on and bright.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
	@Override
	protected void onResume() {
		DebugLog.LOGD("QcarEngine::onResume");
		super.onResume();
		
		menuppRenderer.context = this;
		menuppRenderer.buttonPressed = false;
		
		// QCAR-specific resume operation
		QCAR.onResume();
        
        // We may start the camera only if the QCAR SDK has already been 
        // initialized
        if (qcarStatus == QCAR_CAMERA_STOPPED)
            updateQcarStatus(QCAR_CAMERA_RUNNING);
        
        // Resume the GL view:
        if (mGlView != null)
        {
        	DebugLog.LOGD("Setting opengl view as visible");
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        } 
        
        if (mGUIManager != null)
        {
        	DebugLog.LOGD("Setting interface overlay as visible");
            mGUIManager.initButtons();
        }
	}

	@Override
	protected void onPause() {
		DebugLog.LOGD("QcarEngine::onPause");
		super.onPause();
        
        if (mGlView != null)
        {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }
        
        // QCAR-specific pause operation
        QCAR.onPause();
        
        if (mGUIManager != null)
        {
            mGUIManager.deinitButtons();
        }
        
        // Turn flash off if it was left on
        if (GUIManager.mFlash == true) {
        	mGUIManager.mFlash = !mGUIManager.mFlash;
            boolean result = toggleFlash(mGUIManager.mFlash);
            DebugLog.LOGI("Toggle flash "+(mGUIManager.mFlash?"ON":"OFF")+" "+(result?"WORKED":"FAILED")+"!!");
        }
        
        if (qcarStatus == QCAR_CAMERA_RUNNING)
        {
            updateQcarStatus(QCAR_CAMERA_STOPPED);
        }

	}

    /** The final call you receive before your activity is destroyed.*/
    protected void onDestroy()
    {
        DebugLog.LOGD("QcarEngine::onDestroy");
        super.onDestroy();
        
        // Cancel potentially running tasks
        if (mInitQCARTask != null &&
            mInitQCARTask.getStatus() != InitQCARTask.Status.FINISHED)
        {
            mInitQCARTask.cancel(true);
            mInitQCARTask = null;
        }
        
        if (mLoadTrackerTask != null && mLoadTrackerTask.getStatus() != LoadTrackerTask.Status.FINISHED)
        {
            mLoadTrackerTask.cancel(true);
            mLoadTrackerTask = null;
        }
        
        // Do application deinitialization in native code
        deinitApplicationNative();
                
        // Deinitialize QCAR SDK
        QCAR.deinit();
        System.gc();        
    }
	
	public synchronized void updateQcarStatus(int status) {
		
		// Exit if no status change
		if (qcarStatus == status)
			return;
		
		// Store new status
		qcarStatus = status;
		
		// Execute code based on status update
		switch (qcarStatus) {
		
		case QCAR_INIT:
          // Initialize QCAR SDK asynchronously to avoid blocking the
          // main (UI) thread.
          // This task instance must be created and invoked on the UI
          // thread and it can be executed only once!
          try
          {
              mInitQCARTask = new InitQCARTask();
              mInitQCARTask.execute();
          }
          catch (Exception e)
          {
              DebugLog.LOGE("Initializing QCAR SDK failed");
          }
          break;
          
		case QCAR_INIT_AR:
            // Initialize Augmented Reality-specific application elements
            // that may rely on the fact that the QCAR SDK has been
            // already initialized
            initApplicationAR();
            
            // Unload textures
            mTextures.clear();
            mTextures = null;
            
            // Proceed to next application initialization status
            updateQcarStatus(QCAR_INIT_TRACKER);
            break;
            
		case QCAR_INIT_TRACKER:
            // Load the tracking data set
            //
            // This task instance must be created and invoked on the UI
            // thread and it can be executed only once!
            try
            {
                mLoadTrackerTask = new LoadTrackerTask();
                mLoadTrackerTask.execute();
            }
            catch (Exception e)
            {
                DebugLog.LOGE("Loading tracking data set failed");
            }
            break;
            
		case QCAR_INITED:
            // Hint to the virtual machine that it would be a good time to
            // run the garbage collector.
            //
            // NOTE: This is only a hint. There is no guarantee that the
            // garbage collector will actually be run.
            System.gc();
            
            int screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            
            // Apply screen orientation
            setRequestedOrientation(screenOrientation);
            
            // Pass on screen orientation info to native code
            setActivityPortraitMode(screenOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            
            // Activate the renderer
            mRenderer.mIsActive = true;

            // Native post initialization:
            onQCARInitializedNative();
                           
            loaderView.setVisibility(View.INVISIBLE);
            
            // Now add the GL surface view. It is important
            // that the OpenGL ES surface view gets added
            // BEFORE the camera is started and video
            // background is configured.
            addContentView(mGlView, new LayoutParams(
                            LayoutParams.FILL_PARENT,
                            LayoutParams.FILL_PARENT));
            
            addContentView(mGUIManager.getOverlayView(), new LayoutParams(
                    LayoutParams.FILL_PARENT,
                    LayoutParams.FILL_PARENT));
            
            mGUIManager.initButtons();
            
            // Start the camera:
            updateQcarStatus(QCAR_CAMERA_RUNNING);
            break;
            
        case QCAR_CAMERA_STOPPED:
            // Call the native function to stop the camera
            stopCamera();
            break;
            
        case QCAR_CAMERA_RUNNING:
            // Call the native function to start the camera
            startCamera(); 
            break;
            
        default:
            throw new RuntimeException("QcarEngine::Invalid application state");
            
		}
		
	}
	
    /** We want to load specific textures from the APK, which we will later
    use for rendering. */
    private void loadTextures()
    {
        mTextures.add(Texture.loadTextureFromApk("chicken_fried_steak.png", getAssets()));
        mTextures.add(Texture.loadTextureFromApk("chorizo_stuffed_chicken.png", getAssets()));
        mTextures.add(Texture.loadTextureFromApk("enchiladas.png", getAssets()));
        mTextures.add(Texture.loadTextureFromApk("fish_tacos.png", getAssets()));
        mTextures.add(Texture.loadTextureFromApk("flautas.png", getAssets()));     
        mTextures.add(Texture.loadTextureFromApk("mixed_grill.png", getAssets()));
        mTextures.add(Texture.loadTextureFromApk("pollo_a_la_plancha.png", getAssets()));
        mTextures.add(Texture.loadTextureFromApk("quesadillas.png", getAssets()));
        mTextures.add(Texture.loadTextureFromApk("stuffed_avacado.png", getAssets()));
        
        mTextures.add(Texture.loadTextureFromApk("chicken_fried_steak_info.png", getAssets()));
        mTextures.add(Texture.loadTextureFromApk("chorizo_stuffed_chicken_info.png", getAssets()));
        mTextures.add(Texture.loadTextureFromApk("enchiladas_info.png", getAssets()));
        mTextures.add(Texture.loadTextureFromApk("fish_tacos_info.png", getAssets()));
        mTextures.add(Texture.loadTextureFromApk("flautas_info.png", getAssets()));
        mTextures.add(Texture.loadTextureFromApk("mixed_grill_info.png", getAssets()));
        mTextures.add(Texture.loadTextureFromApk("pollo_a_la_plancha_info.png", getAssets()));
        mTextures.add(Texture.loadTextureFromApk("quesadillas_info.png", getAssets()));
        mTextures.add(Texture.loadTextureFromApk("stuffed_avacado_info.png", getAssets()));
    }
    
    /** Configure QCAR with the desired version of OpenGL ES. */
    private int getInitializationFlags()
    {
        int flags = 0;
        
        // Query the native code:
        if (getOpenGlEsVersionNative() == 1)
        {
            flags = QCAR.GL_11;
        }
        else
        {
            flags = QCAR.GL_20;
        }
        
        return flags;
    }
    
    /** Initializes AR application components. */
    private void initApplicationAR()
    {        
        // Do application initialization in native code (e.g. registering
        // callbacks, etc.)
        initApplicationNative(menupp.mScreenWidth, menupp.mScreenHeight);

        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = QCAR.requiresAlpha();
        
        mGlView = new QCARSampleGLView(this);
        mGlView.init(mQCARFlags, translucent, depthSize, stencilSize);
        
        mRenderer = new menuppRenderer();
        mGlView.setRenderer(mRenderer);
        
        mGUIManager = new GUIManager(getApplicationContext());
        mRenderer.setGUIManager(mGUIManager);
 
    }
    
    /** Returns the number of registered textures. */
    public int getTextureCount()
    {
        return mTextures.size();
    }

    
    /** Returns the texture object at the specified index. */
    public Texture getTexture(int i)
    {
        return mTextures.elementAt(i);
    }
    
    /** Deletes a texture object once it has been copied to native code */
    public void deleteTexture(int i){
    	mTextures.remove(i);
    }
    
	private class InitQCARTask extends AsyncTask<Void, Integer, Boolean> {
	    // Initialize with invalid value
	    private int mProgressValue = -1;
	    
	    protected Boolean doInBackground(Void... params)
	    {
	        QCAR.setInitParameters(QcarEngine.this, mQCARFlags);
	        
	        do
	        {
	            // QCAR.init() blocks until an initialization step is complete,
	            // then it proceeds to the next step and reports progress in
	            // percents (0 ... 100%)
	            // If QCAR.init() returns -1, it indicates an error.
	            // Initialization is done when progress has reached 100%.
	            mProgressValue = QCAR.init();
	           	            
	            // We check whether the task has been canceled in the meantime
	            // (by calling AsyncTask.cancel(true))
	            // and bail out if it has, thus stopping this thread.
	            // This is necessary as the AsyncTask will run to completion
	            // regardless of the status of the component that started is.
	        } while (!isCancelled() && mProgressValue >= 0 && mProgressValue < 100);
	        
	        return (mProgressValue > 0);
	    }

	    
	    protected void onPostExecute(Boolean result)
	    {
	        // Done initializing QCAR, proceed to next application
	        // initialization status:
	        if (result)
	        {
	            DebugLog.LOGD("InitQCARTask::onPostExecute: QCAR initialization" + " successful");

	            updateQcarStatus(QCAR_INIT_AR);
	        }
	        else
	        {
	            // Create dialog box for display error:
	            AlertDialog dialogError = new AlertDialog.Builder(QcarEngine.this).create();
	            dialogError.setButton(
	                "Close",
	                new DialogInterface.OnClickListener()
	                {
	                    public void onClick(DialogInterface dialog, int which)
	                    {
	                        // Exiting application
	                        System.exit(1);
	                    }
	                }
	            ); 
	            
	            String logMessage;

	            // NOTE: Check if initialization failed because the device is
	            // not supported. At this point the user should be informed
	            // with a message.
	            if (mProgressValue == QCAR.INIT_DEVICE_NOT_SUPPORTED)
	            {
	                logMessage = "Failed to initialize QCAR because this " +
	                    "device is not supported.";
	            }
	            else if (mProgressValue ==
	                        QCAR.INIT_CANNOT_DOWNLOAD_DEVICE_SETTINGS)
	            {
	                logMessage = 
	                    "Network connection required to initialize camera " +
	                    "settings. Please check your connection and restart " +
	                    "the application. If you are still experiencing " +
	                    "problems, then your device may not be currently " +
	                    "supported.";
	            }
	            else
	            {
	                logMessage = "Failed to initialize QCAR.";
	            }
	            
	            // Log error:
	            DebugLog.LOGE("InitQCARTask::onPostExecute: " + logMessage +
	                            " Exiting.");
	            
	            // Show dialog box with error message:
	            dialogError.setMessage(logMessage);  
	            dialogError.show();
	        }
	    }
	}
	
    /** An async task to load the tracker data asynchronously. */
    private class LoadTrackerTask extends AsyncTask<Void, Integer, Boolean>
    {
        protected Boolean doInBackground(Void... params)
        {
            // Initialize with invalid value
            int progressValue = -1;

            do
            {
                progressValue = QCAR.load();
                publishProgress(progressValue);
                
            } while (!isCancelled() && progressValue >= 0 &&
                        progressValue < 100);
            
            return (progressValue > 0);
        }
           
        protected void onProgressUpdate(Integer... values)
        {
            // Do something with the progress value "values[0]", e.g. update
            // splash screen, progress bar, etc.
        }
        
        
        protected void onPostExecute(Boolean result)
        {
            DebugLog.LOGD("LoadTrackerTask::onPostExecute: execution " +
                        (result ? "successful" : "failed"));

            // Done loading the tracker, update application status: 
            updateQcarStatus(QCAR_INITED);
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        boolean result = autofocus();
    	return true;
    }
}
