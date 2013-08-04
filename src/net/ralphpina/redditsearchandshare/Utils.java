package net.ralphpina.redditsearchandshare;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;

public class Utils {
	
	//private static final String TAG = "Utils";
	
	private static Utils utils;
	private HttpURLConnection connection;
	private URL url;
	private MainActivity activity;
	private ArrayList<HashMap<String, String>> redditData;
	
	public static Utils getInstance(MainActivity activity) {
		if (utils == null) {
			utils = new Utils();
		}
		utils.activity = activity;
		return utils;
	}
	
	private Utils() {
		redditData = new ArrayList<HashMap<String, String>>();
	}
	
	/*
	 * We are going to pull data, so call progress bar.
	 */
	public void requestRedditData(String subreddit) {
		activity.progress.setMessage("Fetching subriddet : " + activity.subreddit);
		activity.progress.show();
		new ApiTransaction().execute(subreddit);
	}
	
	public ArrayList<HashMap<String, String>> getRedditData() {
		
		return redditData;
	}
	
	private class ApiTransaction extends AsyncTask<String, Integer, JSONObject> {

		@Override
		protected JSONObject doInBackground(String... params) {
			String subreddit = params[0];
			
			//accept no cookies
			CookieManager cookieManager = new CookieManager();
			cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_NONE);
			CookieHandler.setDefault(cookieManager);
			
			try {
				System.setProperty("http.keepAlive", "false");
				url = new URL(new URI("http://www.reddit.com/r/" + subreddit + ".json?limit=100").toASCIIString());
				connection = (HttpURLConnection) url.openConnection();
				connection.setDoInput(true);
				connection.setUseCaches(false);	
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				connection.setRequestProperty("Accept", "application/json");

				String response = readResponse(connection.getInputStream());
				
				return new JSONObject(response);
				
			} catch (Exception e) {
				e.printStackTrace();
			} 
						
			return null;
		}
		
		protected void onPostExecute(JSONObject result) {
			activity.progress.dismiss();
			processResponse(result);
		}
		
		private String readResponse(InputStream stream) throws IOException {
			ByteArrayBuffer baf = new ByteArrayBuffer(50);

			int bytesRead = -1;
			byte[] buffer = new byte[1024];
			while ((bytesRead = stream.read(buffer)) >= 0) {
				// process the buffer, "bytesRead" have been read, no more, no less
				baf.append(buffer, 0, bytesRead);
			}
			stream.close();
			return new String(baf.toByteArray());
		}
	}
	
	private void processResponse(JSONObject result) {
		try {
			JSONArray children = result.getJSONObject("data").getJSONArray("children");
			redditData.clear();
			for (int i = 0; i < children.length(); i++) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put(MainActivity.KEY_ID, Integer.toString(i));
				map.put(MainActivity.KEY_TITLE, ((JSONObject) children.get(i)).getJSONObject("data").getString("title"));
				map.put(MainActivity.KEY_AUTHOR, ((JSONObject) children.get(i)).getJSONObject("data").getString("author"));
				map.put(MainActivity.KEY_UPS, ((JSONObject) children.get(i)).getJSONObject("data").getString("ups"));
				map.put(MainActivity.KEY_THUMB_URL, ((JSONObject) children.get(i)).getJSONObject("data").getString("thumbnail"));

				// adding HashList to ArrayList
				redditData.add(map);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (NullPointerException ne) {
			ne.printStackTrace();
			/*
			 * If we get an error due to a bad reddit name, then we show an AlertDialog
			 */
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
			alertDialogBuilder.setTitle("Subreddit not found");
			alertDialogBuilder.setMessage(activity.subreddit + " does not appear to be a valid subreddit. Please enter another one");
			alertDialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   dialog.dismiss();
		           }
		    });
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		} finally {
			activity.dataChanged();
		}
		
	}
	
	public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
              int count=is.read(bytes, 0, buffer_size);
              if(count==-1)
                  break;
              os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }

}
