package srdes.menupp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
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

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

/**
 * 
 * @author dksokolov
 * \brief activity to view the entree information in a full screen
 */
public class ViewEntree extends Activity {
	
	private RatingBar ratingbar;
	private Entree cur_entree;
    public static final String REVIEW_SELECTION_SCRIPT = "http://www.jsl.grid.webfactional.com/select_entree_reviews.php";
    //private Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/SqueakyChalkSound.ttf");
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	DebugLog.LOGD("starting view entree");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_entree);
        
        //find entree from extras
        int textureId;
    	textureId = getIntent().getIntExtra("key_entree_id", -1);
    	if(textureId == -1){
    		DebugLog.LOGD("key_entree_id not found");
    	}
    	DebugLog.LOGD("tracking texture: " + Integer.toString(textureId));
    	cur_entree = findEntreeById(textureId);
    	if(cur_entree == null){
    		DebugLog.LOGD("null cur_entree");
    		cur_entree.getName();	//to exit here
    	}
    	DebugLog.LOGD("retrieving texture");

    	//find views from layout and set their respective texts/information
    	TextView nameText = (TextView) findViewById(R.id.entree_name);
    	if(nameText != null){
        	DebugLog.LOGD("setting text to " + cur_entree.getName());
    		nameText.setText(cur_entree.getName());
    		//Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/SqueakyChalkSound.ttf");
    		nameText.setTypeface(Typefaces.get(getBaseContext(),"SqueakyChalkSound"));
    	} else {
    		DebugLog.LOGD("null name text view");
    	}
    	
        AssetManager am = this.getAssets();
        BufferedInputStream buf = null;
		try {
			buf = new BufferedInputStream(am.open(cur_entree.getFileName()));
		} catch (IOException e) {
			e.printStackTrace();
		}
        Bitmap bitmap = BitmapFactory.decodeStream(buf);
        ImageView imageView = (ImageView) findViewById(R.id.entree_image);
        imageView.setImageBitmap(bitmap);
        try {
			buf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	ratingbar = (RatingBar) findViewById(R.id.ratingbar_e);
        ratingbar.setIsIndicator(true);
        float averageRating = getAverageRating(cur_entree.getName());
        ratingbar.setRating(averageRating);
    	TextView descriptionText = (TextView) findViewById(R.id.entree_desc);
    	if(descriptionText != null){
    		String [] descriptions = getResources().getStringArray(R.array.descriptions);
        	DebugLog.LOGD("setting text image to " + cur_entree.getDescriptionIndex());
        	descriptionText.setText(descriptions[cur_entree.getDescriptionIndex()]);
    	} else {
    		DebugLog.LOGD("null description text view");
    	}
    }
    
    /**
     * Run when the activity is resumed from being paused.
     * Gets the average rating again to make sure it is most recent.
     */
    @Override
    public void onResume(){
    	
        DebugLog.LOGD("ViewEntree::onResume");
    	super.onResume();
    	ratingbar = (RatingBar) findViewById(R.id.ratingbar_e);
        ratingbar.setIsIndicator(true);
        float averageRating = getAverageRating(cur_entree.getName());
        ratingbar.setRating(averageRating);
    }
    
    /**
     * finds an entree from the global array of entrees given the id
     * @param id is the id of the entree
     * @return Entree object holding entree information
     */
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
	
	/**
	 * gets the average rating from all the reviews for an entree
	 * @param entree the entree being reviewed
	 * @return the average review rating for the entree
	 */
	public static float getAverageRating(String entree){
		float average = 0;
		InputStream is = null;
    	String result = null;
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("entree", entree));

        //http post
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

        //parse json data
        try{
                JSONArray jArray = new JSONArray(result);
                for(int i = 0 ; i < jArray.length() ;i++ ){
                        JSONObject json_data = jArray.getJSONObject(i);
                        //accumulate total
                        average += Float.parseFloat(json_data.getString(EntreeDbAdapter.KEY_RATING));
                }
                //divide by number of reviews to get average rating
                average = average/jArray.length();
        }catch(JSONException e){
                DebugLog.LOGD("Error parsing data "+e.toString());
        }
		return average;
	}
}
