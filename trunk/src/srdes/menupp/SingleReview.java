package srdes.menupp;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.TextView;

/**
 * 
 * @author dksokolov
 * \brief the activity for viewing a single review's title, rating, and body
 */
public class SingleReview extends Activity{
	TextView titleText;
	Typeface tf;
	TextView bodyText;
	RatingBar ratingBar;
	Bundle extras;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DebugLog.LOGD("setting view for single review");
        
        //set views
        setContentView(R.layout.view_single_review);
        titleText = (TextView) findViewById(R.id.review_title);
        tf = Typeface.createFromAsset(getAssets(),"fonts/SqueakyChalkSound.ttf");
        titleText.setTypeface(tf);
        bodyText = (TextView) findViewById(R.id.review_body);

        //get review info
        DebugLog.LOGD("getting row id");
        extras = getIntent().getExtras();
        String review_title = extras.getString(EntreeDbAdapter.KEY_TITLE);
        String review_body = extras.getString(EntreeDbAdapter.KEY_BODY);
        String review_rating = extras.getString(EntreeDbAdapter.KEY_RATING);
        
        ratingBar = (RatingBar) findViewById(R.id.ratingbar_s);
        ratingBar.setIsIndicator(true);
        
        //set relevant data
        DebugLog.LOGD("getting review info");
        titleText.setText(review_title);
        bodyText.setText(review_body);
        ratingBar.setRating(Float.parseFloat(review_rating));
    }
}
