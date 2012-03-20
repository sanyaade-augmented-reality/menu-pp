package srdes.menupp;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ViewEntree extends Activity {

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	DebugLog.LOGD("starting view entree");
        super.onCreate(savedInstanceState);
        DebugLog.LOGD("setting view");
        setContentView(R.layout.view_entree);
        DebugLog.LOGD("getting intent extras");

        int textureId;

    	DebugLog.LOGD("Found extras flag");

    	textureId = getIntent().getIntExtra("key_entree_id", -1);
    	if(textureId == -1){
    		DebugLog.LOGD("key_entree_id not found");
    	}
    	DebugLog.LOGD("tracking texture: " + Integer.toString(textureId));
    	Entree cur_entree = findEntreeById(textureId);
    	if(cur_entree == null){
    		DebugLog.LOGD("null cur_entree");
    		cur_entree.getName();	//to exit here
    	}
    	DebugLog.LOGD("retrieving texture");

    	TextView nameText = (TextView) findViewById(R.id.entree_name);
    	if(nameText != null){
        	DebugLog.LOGD("setting text to " + cur_entree.getName());
    		nameText.setText(cur_entree.getName());
    	} else {
    		DebugLog.LOGD("null name text view");
    	}
    	ImageView imageID = (ImageView) findViewById(R.id.entree_image);
    	if(imageID != null){
        	DebugLog.LOGD("setting image to " + cur_entree.getImage());
        	imageID.setImageResource(getResources().getIdentifier(cur_entree.getImage(), null, null));
    	} else {
    		DebugLog.LOGD("null entree image view");
    	}
    	TextView descriptionText = (TextView) findViewById(R.id.entree_desc);
    	if(descriptionText != null){
    		String [] descriptions = getResources().getStringArray(R.array.descriptions);
        	DebugLog.LOGD("setting text image to " + cur_entree.getDescriptionIndex());
        	descriptionText.setText(descriptions[cur_entree.getDescriptionIndex()]);
    	} else {
    		DebugLog.LOGD("null description text view");
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
