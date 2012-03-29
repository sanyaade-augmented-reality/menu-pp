package srdes.menupp;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.TextView;

public class SingleReview extends Activity{

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DebugLog.LOGD("setting view for single review");
        setContentView(R.layout.view_single_review);

        DebugLog.LOGD("creating db adapter");
        EntreeDbAdapter mDbHelper = new EntreeDbAdapter(this);
        DebugLog.LOGD("opening database");
        mDbHelper.open();
        DebugLog.LOGD("opening database");
        TextView titleText = (TextView) findViewById(R.id.review_title);
        TextView bodyText = (TextView) findViewById(R.id.review_body);

        DebugLog.LOGD("getting row id");
        //long mRowId = (savedInstanceState == null) ? null : (Long) savedInstanceState.getSerializable(EntreeDbAdapter.KEY_ROWID);
        Bundle extras = getIntent().getExtras();
        long mRowId = (extras != null) ? extras.getLong(EntreeDbAdapter.KEY_ROWID) : null;
        
        final RatingBar ratingbar = (RatingBar) findViewById(R.id.ratingbar_s);
        ratingbar.setIsIndicator(true);
        
        DebugLog.LOGD("getting review info");
        Cursor note = mDbHelper.fetchReview(mRowId);
        titleText.setText(note.getString(note.getColumnIndexOrThrow(EntreeDbAdapter.KEY_TITLE)));
		bodyText.setText(note.getString(note.getColumnIndexOrThrow(EntreeDbAdapter.KEY_BODY)));
		ratingbar.setRating(note.getFloat(note.getColumnIndexOrThrow(EntreeDbAdapter.KEY_RATING)));
    }
}
