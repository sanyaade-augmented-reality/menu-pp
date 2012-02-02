package srdes.menupp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class ViewEntree extends Activity {
    /** Called when the activity first starts or the user navigates back
     * to an activity. */
    protected void onCreate(Bundle savedInstanceState)
    {
        DebugLog.LOGD("ViewEntree::onCreate");
        super.onCreate(savedInstanceState);
               
        // Set the app view
        setContentView(R.layout.view_entree);        	      
    }


   /** Called when the activity will start interacting with the user.*/
    protected void onResume()
    {
        DebugLog.LOGD("ViewEntree::onResume");
        super.onResume();
        
 
    }
    

    /** Called when the system is about to start resuming a previous activity.*/
    protected void onPause()
    {
        DebugLog.LOGD("ViewEntree::onPause");
        super.onPause();        
    }
    
    
    /** The final call you receive before your activity is destroyed.*/
    protected void onDestroy()
    {
        DebugLog.LOGD("ViewEntree::onDestroy");
        super.onDestroy();
        
    }

}
