package com.stanford.tutti;

import java.util.*; 

import org.json.*; 

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;

/* 
 * Stores any state which must be globally accessible, eg. variables which cannot
 * be passed to activities using the intent. 
 * 
 * We are currently storing all of the music library metadata in-memory using this
 * class. It supports accessing and searching the library metadata as well as
 * checking and setting the current artist, album, and song.
 */
public class Globals extends Application {
	private HashMap<String, Song> songMap = new HashMap<String, Song>();
	public Jam jam = new Jam(); 
	public Boolean master = false; 
	
	public String currentArtistView = ""; 
	public String currentAlbumView = ""; 
	public Handler uiUpdateHandler; 
	
	DatabaseHandler db; 
	
	private static Context context; 
		
	@Override
	public void onCreate() {
		super.onCreate();
		Globals.context = getApplicationContext(); 
		db = new DatabaseHandler(this);
		uiUpdateHandler = new Handler(); 
	}
	
	public static Context getAppContext() {
        return Globals.context;
    }

	/*
	 * Associates a song with a unique key in the song map.
	 */
	public void addSong(Song song) {
		songMap.put(Integer.toString(song.hashCode()), song);
	}

	/*
	 * Gets a song associated with a specific key.
	 */
	public Song getSongForUniqueKey(String key) {
		return songMap.get(key);
	}
	
	/*
	 * Return a string representation of the current device's IP address. 
	 */
	public String getIpAddr() {
		WifiManager wifiManager = 
				(WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ip = wifiInfo.getIpAddress();

		String ipString = String.format(
				"%d.%d.%d.%d",
				(ip & 0xff),
				(ip >> 8 & 0xff),
				(ip >> 16 & 0xff),
				(ip >> 24 & 0xff));

		return ipString;
	}
}
