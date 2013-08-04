package net.ralphpina.redditsearchandshare;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;

public class MainActivity extends SherlockActivity implements SearchView.OnQueryTextListener, 
PullToRefreshAttacher.OnRefreshListener {
	
	//private static final String TAG = "MainActivity";
	
	/*
	 * Constants to use with LazyAdapter 
	 */
	static final String KEY_SONG = "song"; // parent node
	static final String KEY_ID = "id";
	static final String KEY_TITLE = "title";
	static final String KEY_AUTHOR = "artist";
	static final String KEY_UPS = "duration";
	static final String KEY_THUMB_URL = "thumb_url";
	
	public static int THEME = R.style.Theme_Sherlock;
	
	private ListView list;
	private LazyAdapter adapter;
	
	private Utils utils;
	public ProgressDialog progress;
	private PullToRefreshAttacher mPullToRefreshAttacher;
	
	public SearchView searchView;
	public String subreddit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(THEME); //Used for theme switching in samples
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		/*
		 * Getting border image and scalling it
		 */
		ImageView foreground = (ImageView) findViewById(R.id.border);
		foreground.setScaleType(ScaleType.FIT_XY);
		
		/*
		 * Utils is a singleton where I do all networking
		 */
		utils = Utils.getInstance(this);
		
		list = (ListView)findViewById(R.id.mainActivityListView);
		adapter = new LazyAdapter(this, utils.getRedditData(), list);
		
		progress = new ProgressDialog(this);
		
		/*
		 * Loading funny subreddit
		 */
		tryConnectingAndLoadingData("funny");
		
		/*
		 * Clicking for sharing
		 */
        list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View v, int arg2,
					long arg3) {
				TextView title = (TextView) v.findViewById(R.id.title);
				TextView author = (TextView) v.findViewById(R.id.author);
			    String titleOfItem = title.getText().toString();
				Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
				shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Check out this reddit by " + author);
				shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, titleOfItem);
				shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, "Share this reddit..."));
			}
		});
        
        
        /*
         * Attaching pull to refresh
         */
        mPullToRefreshAttacher = PullToRefreshAttacher.get(this);
        mPullToRefreshAttacher.addRefreshableView(list, this);
       
	}

	/*
	 * SearchView in ActionBarSherlock
	 */
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //Create the search view
		searchView = new SearchView(getSupportActionBar().getThemedContext());
		searchView.setQueryHint("Search for subreddits…");
        searchView.setOnQueryTextListener(this);

        menu.add("Search")
            .setIcon(R.drawable.abs__ic_search)
            .setActionView(searchView)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        return true;
    }

	@Override
	public boolean onQueryTextChange(String arg0) {
		return false;
	}

	/*
	 * When text is entered in SearchView, it goes and fetches it
	 */
	@Override
	public boolean onQueryTextSubmit(String query) {
		tryConnectingAndLoadingData(query);
		Toast.makeText(this, "Fetching subreddit : " + query, Toast.LENGTH_LONG).show();
		return true;
	}
	
	/*
	 * Before calling the networking, it checks if there is a connection.
	 * Otherwise, it shows an error.
	 */
	private void tryConnectingAndLoadingData(String subreddit) {
		this.subreddit = subreddit;
		ConnectivityManager cm =
		        (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
		 
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected;
		if (activeNetwork == null) {
			isConnected = false;
		} else {
			isConnected = activeNetwork.isConnectedOrConnecting();
		}
	
		if (isConnected) {
			utils.requestRedditData(this.subreddit);
		} else {
			showNetworkErrorDialog();
		}
	}
	
	/*
	 * Network error dialog
	 */
	private void showNetworkErrorDialog() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(R.string.network_error_title);
		alertDialogBuilder.setMessage(R.string.network_error_body);
		alertDialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   dialog.dismiss();
	           }
	    });
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	/*
	 * Notify the adapter that ListView data changed. Also, kill pull to refresh if that had
	 * been initiated.
	 */
	public void dataChanged() {
		adapter.notifyDataSetChanged();
		mPullToRefreshAttacher.setRefreshComplete();
	}

	/*
	 * If pull to refresh, pull the same data.
	 */
	@Override
	public void onRefreshStarted(View view) {
		 /**
         * Simulate Refresh with 4 seconds sleep
         */
		tryConnectingAndLoadingData(subreddit);
	}
	
}
