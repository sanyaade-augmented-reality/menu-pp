/*
 * Copyright (C) 2012 Menu++
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package srdes.menupp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

public class EntreeEdit extends Activity {

    private EditText mTitleText;
    private EditText mBodyText;
    private float mRatingFloat;
    private Long mRowId;
    private EntreeDbAdapter mDbHelper;
    private RatingBar ratingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new EntreeDbAdapter(this);
        mDbHelper.open();
        setContentView(R.layout.review_create);
        setTitle(R.string.review_write);

        mTitleText = (EditText) findViewById(R.id.review_title);
        mBodyText = (EditText) findViewById(R.id.review_body);
        mRatingFloat = 0;

        Button confirmButton = (Button) findViewById(R.id.confirm);

        DebugLog.LOGD("getting row id");
        mRowId = (savedInstanceState == null) ? null : (Long) savedInstanceState.getSerializable(EntreeDbAdapter.KEY_ROWID);

        DebugLog.LOGD("row id is " + mRowId);
        populateFields();
        
        ratingbar = (RatingBar) findViewById(R.id.ratingbar);
        ratingbar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                //Toast.makeText(EntreeEdit.this, "Rating: " + rating, Toast.LENGTH_SHORT).show();
            	//mDbHelper.updateReview(mRowId, rating);
            	mRatingFloat = rating;
            }
        });
        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }
        });
    }
    
    private void populateFields(){
    	if(mRowId != null){
    		Cursor note = mDbHelper.fetchReview(mRowId);
    		startManagingCursor(note);
    		if(note == null){
    			DebugLog.LOGD("null note");
    		}
    		mTitleText.setText(note.getString(note.getColumnIndexOrThrow(EntreeDbAdapter.KEY_TITLE)));
    		mBodyText.setText(note.getString(note.getColumnIndexOrThrow(EntreeDbAdapter.KEY_BODY)));
    	}
    }
    
    protected void onSavedInstanceState(Bundle outState){
    	super.onSaveInstanceState(outState);
    	Bundle extras = getIntent().getExtras();
    	String entree = null;
    	if(extras != null){
    		entree = extras.getString("key_entree_name");
    	} else {
    		DebugLog.LOGD("null extras");
    	}
    	saveState(entree);
    	outState.putSerializable(EntreeDbAdapter.KEY_ROWID, mRowId);
    }
    
    protected void onPause(){
    	super.onPause();
    	Bundle extras = getIntent().getExtras();
    	String entree = null;
    	if(extras != null){
    		entree = extras.getString("key_entree_name");
    	} else {
    		DebugLog.LOGD("null extras");
    	}
    	saveState(entree);
    }
    
    protected void onResume(){
    	super.onResume();
    	populateFields();
    }
    
    private void saveState(String entree){
    	DebugLog.LOGD("saving state");
    	String title = mTitleText.getText().toString();
    	String body = mBodyText.getText().toString();

    	if(mRowId == null){
    		/*long id = */mDbHelper.createReview(title, body, entree, mRatingFloat);
    		/*if(id > 0){
    			mRowId = id;
    		}*/
    	}/* else {
    		mDbHelper.updateNote(mRowId, title, body);
    	}*/
    }
}
