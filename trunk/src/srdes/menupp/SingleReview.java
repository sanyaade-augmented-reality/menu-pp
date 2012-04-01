package srdes.menupp;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.TextView;

/**
 * 
 * @author dksokolov
 * \brief the activity for viewing a single review's title, rating, and body
 */
public class SingleReview extends Activity{

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DebugLog.LOGD("setting view for single review");
        
        //set views
        setContentView(R.layout.view_single_review);
        TextView titleText = (TextView) findViewById(R.id.review_title);
        TextView bodyText = (TextView) findViewById(R.id.review_body);

        //get review info
        DebugLog.LOGD("getting row id");
        Bundle extras = getIntent().getExtras();
        String review_title = extras.getString(EntreeDbAdapter.KEY_TITLE);
        String review_body = extras.getString(EntreeDbAdapter.KEY_BODY);
        String review_rating = extras.getString(EntreeDbAdapter.KEY_RATING);
        
        final RatingBar ratingbar = (RatingBar) findViewById(R.id.ratingbar_s);
        ratingbar.setIsIndicator(true);
        
        //set relevant data
        DebugLog.LOGD("getting review info");
        titleText.setText(review_title);
        bodyText.setText(review_body);
        ratingbar.setRating(Float.parseFloat(review_rating));
    }
}
