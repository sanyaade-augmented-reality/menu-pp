package srdes.menupp;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ListView;



public class MenuList extends ListActivity {

	// declare class variables
	private ArrayList<Item> m_parts = new ArrayList<Item>();
	private Runnable viewParts;
	private ItemAdapter m_adapter;
	private Intent qcarEngine;
	private Typeface tf;

    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_list);

        tf = Typeface.createFromAsset(getAssets(),
                "fonts/SqueakyChalkSound.ttf");
        
        // instantiate our ItemAdapter class
        m_adapter = new ItemAdapter(this, R.layout.list_item, m_parts, tf);
        setListAdapter(m_adapter);

        // here we are defining our runnable thread.
        viewParts = new Runnable(){
        	public void run(){
        		handler.sendEmptyMessage(0);
        	}
        };

        // here we call the thread we just defined - it is sent to the handler below.
        Thread thread =  new Thread(null, viewParts, "MagentoBackground");
        thread.start();
	     // Initialize intent for Qcar Engine
        qcarEngine = new Intent(this, QcarEngine.class);
    }

    private Handler handler = new Handler()
	 {
		public void handleMessage(Message msg)
		{
			m_parts.add(new Item("Demo Menu"));
			m_adapter = new ItemAdapter(MenuList.this, R.layout.list_item, m_parts, Typefaces.get(getBaseContext(),"SqueakyChalkSound"));

			// display the list.
	        setListAdapter(m_adapter);
		}
	};
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		//String menu = menus[position];	//TODO: this will allow other menu configurations to be loaded for just one
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