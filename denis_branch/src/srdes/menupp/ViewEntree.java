package srdes.menupp;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ViewEntree extends Activity {

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	DebugLog.LOGD("starting view entree");
        super.onCreate(savedInstanceState);
        DebugLog.LOGD("setting view");
        setContentView(R.layout.menu_list);
        DebugLog.LOGD("getting intent extras");
        Bundle extras = getIntent().getExtras();
        int textureId;
        if(extras != null){
        	DebugLog.LOGD("Found extras flag");
        	textureId = extras.getInt("key_entree_id");
        	DebugLog.LOGD("tracking texture: " + Integer.toString(textureId));
        	Entree cur_entree = findEntreeById(textureId);
        	if(cur_entree == null){
        		DebugLog.LOGD("null cur_entree");
        		cur_entree.getName();	//to exit here
        	}
        	DebugLog.LOGD("retrieving texture");
            //setListAdapter(new ArrayAdapter<String>(ViewEntree.this, R.layout.menu_list, (String []) cur_entree.getName()));
        	TextView t = (TextView) findViewById(R.id.list_item1);
        	if(t != null){
            	DebugLog.LOGD("setting text to " + cur_entree.getName());
        		t.setText(cur_entree.getName());
        	} else {
        		DebugLog.LOGD("null text view");
        	}
        } else {
        	DebugLog.LOGD("No extras found");
        }
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
	}
}
