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

/**
 * \brief Displays list of menus.
 */
public class MenuList extends ListActivity{
	
	private String [] menus;
	private Intent qcarEngine;
		
	protected void onCreate(Bundle savedInstanceState) {
        DebugLog.LOGD("MenuList::onCreate");
        super.onCreate(savedInstanceState);
                
        // Initialize Menus
        menus = getResources().getStringArray(R.array.menus);
        setListAdapter(new ArrayAdapter<String>(MenuList.this, R.layout.menu_list, menus));
        
        // Initialize intent for Qcar Engine
        qcarEngine = new Intent(this, QcarEngine.class);
	}
	/**
     *\brief Called when a menu in the list is tapped
     */
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		String menu = menus[position];	//TODO: this will allow other menu configurations to be loaded for just one
	    startActivity(new Intent(this, QcarEngine.class));
	}
	
    protected void onResume()
    {
        DebugLog.LOGD("MenuList::onResume");
        super.onResume();
    }
	protected void onPause() {
		
		DebugLog.LOGD("MenuList::onPause");
		super.onPause();	
	}
    
    

}
