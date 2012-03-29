/*
   		   Copyright [2012] [Menu++]

		   Licensed under the Apache License, Version 2.0 (the "License");
		   you may not use this file except in compliance with the License.
		   You may obtain a copy of the License at

		       http://www.apache.org/licenses/LICENSE-2.0

		   Unless required by applicable law or agreed to in writing, software
		   distributed under the License is distributed on an "AS IS" BASIS,
		   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
		   See the License for the specific language governing permissions and
		   limitations under the License.
*/

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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
/**
 * \brief Database interface for entrees/reviews.
 */
public class EntreeDbAdapter {
	
	 	public static final String KEY_TITLE = "title";
	    public static final String KEY_BODY = "body";
	    public static final String KEY_ROWID = "_id";
	    public static final String KEY_ENTREE = "entree";
	    public static final String KEY_RATING = "rating";

	    private static final String TAG = "EntreeDbAdapter";
	    private DatabaseHelper mDbHelper;
	    private SQLiteDatabase mDb;

	    /**
	     * Database creation sql statement
	     */
	    private static final String DATABASE_CREATE =
	        "create table entrees (_id integer primary key autoincrement, "
	        + "title text not null, body text not null, entree text not null, rating float not null);";

	    private static final String DATABASE_NAME = "data"; //"http://184.172.207.73/grid";
	    private static final String DATABASE_TABLE = "entrees";
	    private static final int DATABASE_VERSION = 2;

	    private final Context mCtx;

	    private static class DatabaseHelper extends SQLiteOpenHelper {

	        DatabaseHelper(Context context) {
	            super(context, DATABASE_NAME, null, DATABASE_VERSION);
	        }

	        @Override
	        public void onCreate(SQLiteDatabase db) {

	            db.execSQL(DATABASE_CREATE);
	        }

	        @Override
	        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
	                    + newVersion + ", which will destroy all old data");
	            db.execSQL("DROP TABLE IF EXISTS entrees");
	            onCreate(db);
	        }
	    }

	    /**
	     * Constructor - takes the context to allow the database to be
	     * opened/created
	     * 
	     * @param ctx the Context within which to work
	     */
	    public EntreeDbAdapter(Context ctx) {
	        this.mCtx = ctx;
	    }

	    /**
	     * Open the notes database. If it cannot be opened, try to create a new
	     * instance of the database. If it cannot be created, throw an exception to
	     * signal the failure
	     * 
	     * @return this (self reference, allowing this to be chained in an
	     *         initialization call)
	     * @throws SQLException if the database could be neither opened or created
	     */
	    public EntreeDbAdapter open() throws SQLException {
	        mDbHelper = new DatabaseHelper(mCtx);
	        mDb = mDbHelper.getWritableDatabase();
	        return this;
	    }

	    public void close() {
	        mDbHelper.close();
	    }


	    /**
	     * Create a new note using the title and body provided. If the note is
	     * successfully created return the new rowId for that note, otherwise return
	     * a -1 to indicate failure.
	     * 
	     * @param title the title of the note
	     * @param body the body of the note
	     * @return rowId or -1 if failed
	     */
	    public long createReview(String title, String body, String entree, float rating) {
	        ContentValues initialValues = new ContentValues();
	        initialValues.put(KEY_TITLE, title);
	        initialValues.put(KEY_BODY, body);
	        initialValues.put(KEY_ENTREE, entree);
	        initialValues.put(KEY_RATING, rating);

	        return mDb.insert(DATABASE_TABLE, null, initialValues);
	    }
	 /*   private final String INSERT_REVIEW_SCRIPT = "http://www.jsl.grid.webfactional.com/insert_review.php";
	    public void createReview(String title, String body, String entree) {
	    	InputStream is = null;
	    	String result = null;
	        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	        nameValuePairs.add(new BasicNameValuePair("title", title));
	        nameValuePairs.add(new BasicNameValuePair("body", body));
	        nameValuePairs.add(new BasicNameValuePair("entree", entree));

	        //http post
	        try{
	                HttpClient httpclient = new DefaultHttpClient();
	                HttpPost httppost = new HttpPost(INSERT_REVIEW_SCRIPT);
	                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	                HttpResponse response = httpclient.execute(httppost);
	                HttpEntity entity = response.getEntity();
	                is = entity.getContent();
	        }catch(Exception e){
	                DebugLog.LOGD("Error in http connection "+e.toString());
	        }
	    }*/

	    /**
	     * Delete the note with the given rowId
	     * 
	     * @param rowId id of note to delete
	     * @return true if deleted, false otherwise
	     */
	    public boolean deleteReview(long rowId) {

	        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	    }

	    /**
	     * Return a Cursor over the list of all notes in the database
	     * 
	     * @return Cursor over all notes
	     */
	    public Cursor fetchAllReviews(String entree) {

	        return mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
	                KEY_BODY, KEY_ENTREE, KEY_RATING}, KEY_ENTREE + "='" + entree + "'", null, null, null, null, null);
	    }
	/*    private final String REVIEW_SELECTION_SCRIPT = "http://www.jsl.grid.webfactional.com/select_entree_reviews.php";
	    public Cursor fetchAllReviews(String entree) {
	    	
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
	        
	        Cursor notesCursor = new MatrixCursor(new String[]{"Title","Body","Entree"});//dbHelper.fetchAllReviews(entree);
	        
	        //parse json data
	        try{
	                JSONArray jArray = new JSONArray(result);
	                for(int i = 0 ; i < jArray.length() ;i++ ){
	                        JSONObject json_data = jArray.getJSONObject(i);

	                        String title = json_data.getString("Title");
	                        String body = json_data.getString("Body");
	                        String entreeName = json_data.getString("Entree");
	                        
	                        ((MatrixCursor) notesCursor).addRow(new Object[]{title,body,entreeName});
	                        
	                        //Get an output to the screen
	                        DebugLog.LOGD("Found Review " + title + " for " + entreeName);
	                        DebugLog.LOGD("Body of review: " + body);
	                }
	        }catch(JSONException e){
	                DebugLog.LOGD("Error parsing data "+e.toString());
	        }
	        
	        return notesCursor;

	       // return mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
	      //          KEY_BODY, KEY_ENTREE}, KEY_ENTREE + "='" + entree + "'", null, null, null, null, null);
	    }*/

	    /**
	     * Return a Cursor positioned at the note that matches the given rowId
	     * 
	     * @param rowId id of note to retrieve
	     * @return Cursor positioned to matching note, if found
	     * @throws SQLException if note could not be found/retrieved
	     */
	    public Cursor fetchReview(long rowId) throws SQLException {

	    	DebugLog.LOGD("preparing query");
	        Cursor mCursor =
	            mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID,
	                    KEY_TITLE, KEY_BODY, KEY_ENTREE, KEY_RATING}, KEY_ROWID + "=" + rowId, null,
	                    null, null, null, null);
	        if (mCursor != null) {
	        	DebugLog.LOGD("found cursor, moving to first");
	            mCursor.moveToFirst();
	            DebugLog.LOGD("cursor moved to first");
	        } else {
	        	DebugLog.LOGD("null cursor");
	        }
	        return mCursor;
	    }

	    /**
	     * Update the note using the details provided. The note to be updated is
	     * specified using the rowId, and it is altered to use the title and body
	     * values passed in
	     * 
	     * @param rowId id of note to update
	     * @param title value to set note title to
	     * @param body value to set note body to
	     * @return true if the note was successfully updated, false otherwise
	     */
	    public boolean updateReview(long rowId, /*String title, String body, String entree,*/ float rating) {
	        ContentValues args = new ContentValues();
	        //args.put(KEY_TITLE, title);
	        //args.put(KEY_BODY, body);
	        //args.put(KEY_ENTREE, entree);
	        args.put(KEY_RATING, rating);

	        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	    }
}
