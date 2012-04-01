package srdes.menupp;

import java.io.BufferedReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

/**
 * \brief Displays a list of reviews in the reviews tab.
 */
public class ViewReview extends ListActivity {
	
	//menu constants
	private static final int ACTIVITY_CREATE=0;
    private static final int INSERT_ID = Menu.FIRST;
	
    //php request script
    private final String REVIEW_SELECTION_SCRIPT = "http://www.jsl.grid.webfactional.com/select_entree_reviews.php";
    
    //hold data for use between listener methods
    private ArrayList<Review> cur_reviews;
    private String this_entree;

    /** 
     * Called when the activity first starts or the user navigates back
     * to an activity. Gets the entree name and displays all reviews for it.
     * 
     * @param savedInstanceState is the saved information on the activity
     * @return void
     */
    protected void onCreate(Bundle savedInstanceState)
    {
        DebugLog.LOGD("ViewEntree::onCreate");
        super.onCreate(savedInstanceState);

        // Set and initialize the app view
        String entree = getEntreeName();
        this_entree = entree;
        setContentView(R.layout.reviews_list);
        fillData(entree);
        registerForContextMenu(getListView());
        
        //show instruction message
        Toast.makeText(ViewReview.this, "To add a review, click the phone's menu button and select \"Add Review\"", Toast.LENGTH_LONG).show();
    }
    
    /**
     * Gets entree name from the extras
     * @return String which is the entree name obtained from extras
     */
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

    /**
     * Sets a list adapter to display all the reviews on the page
     * @param entree is the entree currently being tracked
     */
    private void fillData(String entree) {

    	cur_reviews = new ArrayList<Review>();
    	InputStream is = null;
    	String result = null;
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("entree", entree));

        //http post to get reviews from DB
        try{
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(REVIEW_SELECTION_SCRIPT);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();
        }catch(Exception e){
                DebugLog.LOGD("Error in http connection "+e.toString());
        }
        
        //convert response to string
        try{
                BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                }
                is.close();
                result = sb.toString();
        }catch(Exception e){
                DebugLog.LOGD("Error converting result "+e.toString());
        }
        
        //hold the strings to display on the list of reviews
        ArrayList<String> reviewTitles = new ArrayList<String>();
        
        //parse json data
        try{
                JSONArray jArray = new JSONArray(result);
                String title;
                String body;
                String entreeName;
                float rating;
                int unique_id;
                
                //get each item from the JSON array returned from the DB
                for(int i = 0 ; i < jArray.length() ;i++ ){
                        JSONObject json_data = jArray.getJSONObject(i);

                        unique_id = json_data.getInt(EntreeDbAdapter.KEY_ROWID);
                        title = json_data.getString(EntreeDbAdapter.KEY_TITLE);
                        body = json_data.getString(EntreeDbAdapter.KEY_BODY);
                        entreeName = json_data.getString(EntreeDbAdapter.KEY_ENTREE);
                        rating = Float.parseFloat(json_data.getString(EntreeDbAdapter.KEY_RATING));
                        cur_reviews.add(new Review(unique_id, title, body, entreeName, rating));
                        
                        reviewTitles.add(title+"  ("+new Float(rating).toString()+"/5)");
                        
                        //Get an output to the screen
                        DebugLog.LOGD("Found Review " + title);
                }
        }catch(JSONException e){
                DebugLog.LOGD("Error parsing data "+e.toString());
        }
        
        //display list of reviews with a list adapter
        setListAdapter(new ArrayAdapter<String>(ViewReview.this, R.layout.review_row, reviewTitles));
    }

    /**
     * Run when user hits the menu key. Adds a "Add Review" button to the menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        return true;
    }

    /**
     * Run when user selects a button on the menu. Run the creation activity to make a review
     */
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

    /**
     * Starts the creation activity for a review for an entree
     * @param entree the entree being tracked
     */
    private void createNote(String entree) {
        Intent i = new Intent(this, EntreeEdit.class);
        i.putExtra("key_entree_name", entree);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    /**
     * Goes to the view for a single review, passing in all relevant info through a bundle 
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, SingleReview.class);
    	i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(EntreeDbAdapter.KEY_ROWID, id);
        Review r = cur_reviews.get((int)id);
        DebugLog.LOGD("Found review, getting title");
        i.putExtra(EntreeDbAdapter.KEY_TITLE, r.getTitle());
        DebugLog.LOGD("Found title");
        i.putExtra(EntreeDbAdapter.KEY_BODY, r.getBody());
        DebugLog.LOGD("Found body");
        i.putExtra(EntreeDbAdapter.KEY_ENTREE, r.getName());
        DebugLog.LOGD("Found name");
        i.putExtra(EntreeDbAdapter.KEY_RATING, new Float(r.getRating()).toString());
        DebugLog.LOGD("Found rating");
        startActivity(i);
    }
    
    /**
     * Find a review in the list of reviews for an entree
     * @param id the index of the review in the list of reviews for the entree
     * @return a Review object containing all the information for the found review
     */
    public Review findReviewById(long id){
    	Review r = null;
    	DebugLog.LOGD("Looking for review with id=" + id);
    	for(int i = 0; i < cur_reviews.size(); i++){
    		DebugLog.LOGD("current id is " + cur_reviews.get(i).getId());
    		if(cur_reviews.get(i).getId() == id){
    			r = cur_reviews.get(i);
    			break;
    		}
    	}
    	return r;
    }
    
    /**
     * Run when the creation activity for a new review returns.
     * Fills data again to make sure new review is included in the list.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData(this_entree);
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
