package srdes.menupp;

import com.qualcomm.QCAR.QCAR;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.qualcomm.QCAR.QCAR;


public class MenuList extends ListActivity{
	
	private String [] menus;
		
	protected void onCreate(Bundle savedInstanceState) {
        DebugLog.LOGD("MenuList::onCreate");
        super.onCreate(savedInstanceState);
        
        
        //Initialize Menus
        menus = getResources().getStringArray(R.array.menus);
        setListAdapter(new ArrayAdapter<String>(MenuList.this, R.layout.menu_list, menus));
       
        // Set the menu list screen
        //setContentView(R.layout.menu_list);
	}
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		String menu = menus[position];	//TODO: this will allow other menu configurations to be loaded for just one
	    //menupp.applicationStatus = 33;
	    startActivity(new Intent(this, QcarEngine.class));

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
