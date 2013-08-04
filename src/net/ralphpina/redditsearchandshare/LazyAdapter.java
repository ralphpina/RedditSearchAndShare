/*
 * This class was taken from a blog post: http://www.androidhive.info/2012/02/android-custom-listview-with-image-and-text/
 */
package net.ralphpina.redditsearchandshare;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class LazyAdapter extends BaseAdapter {
    
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader; 
   
	private Typeface tf;
    
    public LazyAdapter(Activity a, ArrayList<HashMap<String, String>> d, ListView list) {
        activity = a;
        data = d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(activity.getApplicationContext());
        tf = Typeface.createFromAsset(a.getAssets(),
                "fonts/bebasneue.ttf");
        list.setAdapter(this);
    }
    
    public void updateData(ArrayList<HashMap<String, String>> d) {
    	data = d;
    	this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.list_row, null);

        TextView title = (TextView)vi.findViewById(R.id.title); // title
        TextView author = (TextView)vi.findViewById(R.id.author); // artist name
        author.setTypeface(tf);
        TextView duration = (TextView)vi.findViewById(R.id.ups); // duration
        ImageView thumb_image = (ImageView)vi.findViewById(R.id.list_image); // thumb image
        
        HashMap<String, String> subreddit = new HashMap<String, String>();
        subreddit = data.get(position);
        
        // Setting all values in listview
        title.setText(subreddit.get(MainActivity.KEY_TITLE));
        author.setText(subreddit.get(MainActivity.KEY_AUTHOR));
        duration.setText(subreddit.get(MainActivity.KEY_UPS));
        imageLoader.DisplayImage(subreddit.get(MainActivity.KEY_THUMB_URL), thumb_image);
        return vi;
    }
}