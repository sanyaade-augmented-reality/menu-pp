package srdes.menupp;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class EntreeTabManage extends TabActivity {

	public void onCreate(Bundle savedInstanceState) {
		DebugLog.LOGD("starting entree tab manage");
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);

	    DebugLog.LOGD("view set");
	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    DebugLog.LOGD("creating intent");
	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, ViewEntree.class);
	    Bundle extras = getIntent().getExtras();

	    if(extras != null){
	         intent.putExtras(extras);
	    } else {
	    	DebugLog.LOGD("null extras");
	    }
	    
	    DebugLog.LOGD("setting tabs");
	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("entree").setIndicator("Entree",
	                      res.getDrawable(R.drawable.ic_tab_entree))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    DebugLog.LOGD("setting intent for other tab");
	    intent = new Intent().setClass(this, ViewReview.class);
	    if(extras != null){
	         intent.putExtras(extras);
	    } else {
	    	DebugLog.LOGD("null extras");
	    }
	    spec = tabHost.newTabSpec("reviews").setIndicator("Reviews",
	                      res.getDrawable(R.drawable.ic_tab_reviews))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTab(2);
	}
}
