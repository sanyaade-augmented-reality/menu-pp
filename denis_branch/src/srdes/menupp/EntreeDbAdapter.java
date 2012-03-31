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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * \brief Database interface for entrees/reviews. Includes a method for creating reviews
 */
public class EntreeDbAdapter {

	//column names also used as keys
 	public static final String KEY_TITLE = "title";
    public static final String KEY_BODY = "body";
    public static final String KEY_ROWID = "_id";
    public static final String KEY_ENTREE = "entree";
    public static final String KEY_RATING = "rating";

    private static final String INSERT_REVIEW_SCRIPT = "http://www.jsl.grid.webfactional.com/insert_review.php";

    /**
     * Create a new review using the title and body provided. If the review is
     * successfully created return the new rowId for that review, otherwise return
     * a -1 to indicate failure.
     * 
     * @param title the title of the note
     * @param body the body of the note
     * @param entree the name of the entree the review is for
     * @param rating the rating given to the entree in the review
     * @throws JSONException if cannot get rowID
     * @return rowId or -1 if failed
     */
    public static Long createReview(String title, String body, String entree, float rating) throws JSONException {

    	//add column information to pass to database
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair(KEY_TITLE, title));
        nameValuePairs.add(new BasicNameValuePair(KEY_BODY, body));
        nameValuePairs.add(new BasicNameValuePair(KEY_ENTREE, entree));
        nameValuePairs.add(new BasicNameValuePair(KEY_RATING, new Float(rating).toString()));

        //http post
        JSONObject response = null;
        try{
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(INSERT_REVIEW_SCRIPT);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                ResponseHandler<String> responseHandler=new BasicResponseHandler();
                String responseBody = httpclient.execute(httppost, responseHandler);
                response = new JSONObject(responseBody);
                
        }catch(ClientProtocolException e){
        	DebugLog.LOGD("Protocol error in http connection "+e.toString());
        }catch(UnsupportedEncodingException e){
        	DebugLog.LOGD("Encoding error in http connection "+e.toString());
        }catch(IOException e){
        	DebugLog.LOGD("IO error in http connection "+e.toString());
        }
        //rowID encoded in response
        return (Long) response.get(KEY_ROWID);
    }
}
