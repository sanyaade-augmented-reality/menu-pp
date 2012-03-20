/*==============================================================================
            Copyright (c) 2010-2011 QUALCOMM Incorporated.
            All Rights Reserved.
            Qualcomm Confidential and Proprietary
==============================================================================*/

package srdes.menupp;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class GUIManager {

    // Custom views
    private View overlayView;
    private Button nextButton;
    private Button backButton;
    private Button flashOnButton;
    private Button flashOffButton;
    
    // Value that determines the state of the flash
    public static boolean mFlash = false;

    // The main application context
    private Context applicationContext;
    
    // A Handler for working with the gui from other threads
    private Handler mainActivityHandler;
    
    // Flags for our Handler
    public static final int TOGGLE_FLASH_BUTTON = 0;
    public static final int DISPLAY_INFO_TOAST = 1;
    
    // Native methods to handle button clicks
    public native void nativeNext();
    public native void nativeBack();
    private native boolean toggleFlash(boolean flash);
    
    /** Initialize the GUIManager. */
    public GUIManager(Context context)
    {
        // Load our overlay view
        // This view will add content on top of the camera / opengl view
        overlayView = View.inflate(context, R.layout.interface_overlay, null);
        
        // Store the application context
        applicationContext = context;
        
        // Create a Handler from the current thread
        // This is the only thread that can make changes to the GUI,
        // so we require a handler for other threads to make changes
        mainActivityHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case TOGGLE_FLASH_BUTTON:
                    if (flashOnButton != null) {
                        flashOnButton.setVisibility((mFlash) ? (View.INVISIBLE) : (View.VISIBLE));
                    }
                    if (flashOffButton != null) {
                        flashOffButton.setVisibility((mFlash) ? (View.VISIBLE) : (View.INVISIBLE));
                    }
                    break;
                case DISPLAY_INFO_TOAST:
                    String text = (String) msg.obj;
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(applicationContext, text, duration);
                    toast.setGravity(Gravity.CENTER, 0, -20);
                    toast.show();
                    break;
            }
            }
        };
    }
    
    
    /** Button clicks should call corresponding native functions. */
    public void initButtons()
    {
        if (overlayView == null)
            return;
        
        nextButton = (Button) overlayView.findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	nativeNext();
            }
        });
        
        backButton = (Button) overlayView.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	nativeBack();
            }
        });
        
        flashOnButton = (Button) overlayView.findViewById(R.id.flash_on_button);
        flashOnButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	mFlash = !mFlash;
            	boolean result = QcarEngine.toggleFlash(mFlash);
            	DebugLog.LOGI("Toggle flash "+(mFlash?"ON":"OFF")+" "+(result?"WORKED":"FAILED")+"!!");            }
        });
        
        flashOffButton = (Button) overlayView.findViewById(R.id.flash_off_button);
        flashOffButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	mFlash = !mFlash;
            	boolean result = QcarEngine.toggleFlash(mFlash);
            	DebugLog.LOGI("Toggle flash "+(mFlash?"ON":"OFF")+" "+(result?"WORKED":"FAILED")+"!!");            }
        });
        
    }
    
    /** Clear the button listeners. */
    public void deinitButtons()
    {
        if (overlayView == null)
            return;
        flashOnButton.setVisibility(View.VISIBLE);
        flashOffButton.setVisibility(View.INVISIBLE);
        
        nextButton.setOnClickListener(null);
        backButton.setOnClickListener(null);
        flashOnButton.setOnClickListener(null);
        flashOffButton.setOnClickListener(null);
        
        nextButton = null;
        backButton = null;
        flashOnButton = null;
        flashOffButton = null;
    }
    
    
    /** Send a message to our gui thread handler. */
    public void sendThreadSafeGUIMessage(Message message)
    {
        mainActivityHandler.sendMessage(message);
    }
    
    /** Getter for the overlay view. */
    public View getOverlayView()
    {
        return overlayView;
    }
}
