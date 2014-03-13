package com.example.tuttiui;

import java.io.Serializable;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class NewJamActivity extends Activity {


	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_jam);
		// Show the Up button in the action bar.
		setupActionBar();

		
		ListView listView1 = (ListView) findViewById(R.id.listView1);
		String[] items = { "About You Now", "Alejandro", "All I Ask of You", "Best Song Ever", "Boyfriend", "Bye Bye Bye", "C'mon, C'mon", "Call Me Maybe", "A Change in Me", "Clarity", "Crazy Town", "Dark Horse", "Defying Gravity", "Demons", "Does He Know?", "Don't Rain on My Parade", "Ever Ever After", "Feel Again", "Finish the Fight", "Fireflies", "Friday", "Gangam Style", "Happily", "Hoedown Throwdown", "I Dreamed a Dream", "It's Time", "Kiss You", "Let It Go", "Little Things", "Live While We're Young", "Midnight Memories", "Never Say Never", "One Thing", "The Phantom of the Opera", "Popular", "Radioactive", "Right Now", "Roar", "Safety Dance", "She's Not Afraid", "Somebody That I Used to Know", "Story of My Life", "They Don't Know About Us", "Tik Tok", "Titanium", "Up All Night", "Way Back Into Love", "When Will My Life Begin", "You & I", "22" };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
		listView1.setAdapter(adapter);
		
		listView1.setOnItemClickListener(new OnItemClickListener() {
			  @Override
			  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				  String item = ((TextView)view).getText().toString();
				  new AlertDialog.Builder(view.getContext())
		            .setMessage(item+" added to Jam").show();
				  MainActivity.jam.addSong(item);
			  }
			});
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_jam, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    public void viewJam(View view) {
    	Intent intent = new Intent(this, ViewJamActivity.class);
    	Bundle bundle = new Bundle();
    	bundle.putSerializable("jam", MainActivity.jam);
    	intent.putExtras(bundle);
    	startActivity(intent);
    }
}
