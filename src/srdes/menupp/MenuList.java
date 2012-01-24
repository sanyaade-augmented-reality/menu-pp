package srdes.menupp;

import com.qualcomm.QCAR.QCAR;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;

public class MenuList extends ListActivity{
	
	protected void onCreate(Bundle savedInstanceState) {
        DebugLog.LOGD("MenuList::onCreate");
        super.onCreate(savedInstanceState);
        
        // Set the menu list screen
        setContentView(R.layout.menu_list);
	}
	
	protected void onPause() {
		
		DebugLog.LOGD("MenuList::onPause");
		super.onPause();
		
	}
	
    protected void onResume()
    {
        DebugLog.LOGD("MenuList::onResume");
        super.onResume();
     
    }

}
