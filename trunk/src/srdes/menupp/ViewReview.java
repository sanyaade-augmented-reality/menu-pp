package srdes.menupp;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
/**
 * \brief Displays a review.
 */
public class ViewReview extends ListActivity {
	private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;

    private static final int INSERT_ID = Menu.FIRST;
    //private static final int DELETE_ID = Menu.FIRST + 1;
	
	private EntreeDbAdapter dbHelper;
	
    /** Called when the activity first starts or the user navigates back
     * to an activity. */
    protected void onCreate(Bundle savedInstanceState)
    {
        DebugLog.LOGD("ViewEntree::onCreate");
        super.onCreate(savedInstanceState);
               
        // Set the app view
        String entree = getEntreeName();
        setContentView(R.layout.reviews_list); 
        dbHelper = new EntreeDbAdapter(this);
        dbHelper.open();
        fillData(entree);
        registerForContextMenu(getListView());
    }
    
    private String getEntreeName(){
    	int textureId = getIntent().getIntExtra("key_entree_id", -1);
    	if(textureId == -1){
    		DebugLog.LOGD("key_entree_id not found");
    	}
    	DebugLog.LOGD("tracking texture: " + Integer.toString(textureId));
    	Entree cur_entree = ViewEntree.findEntreeById(textureId);
    	if(cur_entree == null){
    		DebugLog.LOGD("null cur_entree");
    		cur_entree.getName();	//to exit here for obvious error message
    	}
    	return cur_entree.getName();
    }

    private void fillData(String entree) {
        // Get all of the rows from the database and create the item list
        Cursor notesCursor = dbHelper.fetchAllReviews(entree);
        startManagingCursor(notesCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{EntreeDbAdapter.KEY_TITLE};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.text1};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter notes = new SimpleCursorAdapter(this, R.layout.review_row, notesCursor, from, to);
        setListAdapter(notes);
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case INSERT_ID:
            	String entree = getEntreeName();
                createNote(entree);
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        //menu.add(0, DELETE_ID, 0, R.string.menu_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            /*case DELETE_ID:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                dbHelper.deleteNote(info.id);
                fillData();
                return true;*/
        }
        return super.onContextItemSelected(item);
    }

    private void createNote(String entree) {
        Intent i = new Intent(this, EntreeEdit.class);
        i.putExtra("key_entree_name", entree);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, SingleReview.class);
    	i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(dbHelper.KEY_ROWID, id);
        startActivity(i);
    }
/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }*/

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
