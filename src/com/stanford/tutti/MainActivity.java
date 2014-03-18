package com.stanford.tutti;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
		MusicLibraryLoader musicLibraryLoader = new MusicLibraryLoader();
		musicLibraryLoader.loadMusic(this);        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public void makeNewJam(View view) {
    	Intent intent = new Intent(this, NewJamActivity.class);
    	startActivity(intent);
    }


	public void joinJam(View view) {
		Intent intent = new Intent(this, JoinJamActivity.class);
		startActivity(intent);
	}
	
	public void settingsMenu(View view) {
		Intent intent = new Intent(this, SettingsMenuActivity.class);
		startActivity(intent);
	}
	
	public void helpMenu(View view) {
		Intent intent = new Intent(this, HelpMenuActivity.class);
		startActivity(intent);
	}
	
	public void testNetworking(View view) {
		Intent intent = new Intent(this, StreamSongTestActivity.class);
		startActivity(intent);
	}
}
