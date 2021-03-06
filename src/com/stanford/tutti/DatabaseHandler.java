package com.stanford.tutti;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

/**
 * Singleton object that handles all interactions with the local database. 
 */
public class DatabaseHandler extends SQLiteOpenHelper {

	// Database Version
	private static final int DATABASE_VERSION = 23;

	// Database Name
	private static final String DATABASE_NAME = "library";

	// Table names
	private static final String TABLE_SONGS = "songs";
	private static final String TABLE_JAM = "jam"; 
	private static final String TABLE_LOG = "log";

	// Song table columns names
	private static final String KEY_ID = "_id";
	private static final String KEY_TITLE = "title";
	private static final String KEY_ARTIST = "artist";
	private static final String KEY_ALBUM = "album";
	private static final String KEY_PATH = "path";
	private static final String KEY_LOCAL = "local";
	private static final String KEY_ART = "art";
	private static final String KEY_HASH = "hash";
	private static final String KEY_IP = "_ip";
	private static final String KEY_PORT = "port";
	private static final String KEY_TRACK_NUM = "trackNum"; 

	// Jam table-exclusive column names
	private static final String KEY_JAM_INDEX = "jamIndex"; 
	private static final String KEY_ADDED_BY = "addedBy"; 
	private static final String KEY_TIMESTAMP = "timestamp"; 
	
	// Log table exclusive column names
	private static final String KEY_START_TIME = "startTime";
	private static final String KEY_LATEST_TIME = "latestTime";
	private static final String KEY_NUM_SONGS = "numSongs";
	private static final String KEY_NUM_USERS = "numUsers";

	// Song table columns indices
	private static final int COL_ID = 0; 
	private static final int COL_TITLE = 1; 
	private static final int COL_ARTIST = 2; 
	private static final int COL_ALBUM = 3; 
	private static final int COL_PATH = 4; 
	private static final int COL_LOCAL = 5; 
	private static final int COL_ART = 6; 
	private static final int COL_HASH = 7; 
	private static final int COL_IP = 8;
	private static final int COL_PORT = 9;
	
	// Song table exclusive indices
	private static final int COL_TRACK_NUM = 10; 

	// Jam table-exclusive column indices
	private static final int COL_JAM_INDEX = 10; 
	private static final int COL_ADDED_BY = 11; 
	private static final int COL_TIMESTAMP = 12; 
	
	// Log table exclusive column indices
	private static final int COL_START_TIME = 1;
	private static final int COL_LATEST_TIME = 2;
	private static final int COL_NUM_SONGS = 3;
	private static final int COL_NUM_USERS = 4;


	private static final String[] SONG_COLUMNS = {KEY_ID, KEY_TITLE, KEY_ARTIST, KEY_ALBUM, KEY_PATH, KEY_LOCAL, KEY_ART, KEY_HASH, KEY_IP, KEY_PORT, KEY_TRACK_NUM};
	private static final String[] JAM_COLUMNS = {KEY_ID, KEY_TITLE, KEY_ARTIST, KEY_ALBUM, KEY_PATH, KEY_LOCAL, KEY_ART, KEY_HASH, KEY_IP, KEY_PORT, KEY_JAM_INDEX, KEY_ADDED_BY, KEY_TIMESTAMP};
	private static final String[] LOG_COLUMNS = {KEY_ID, KEY_START_TIME, KEY_LATEST_TIME, KEY_NUM_SONGS, KEY_NUM_USERS};
	
	private Globals g;

	
	/**
	 * Constructs a new DatabaseHandler object 
	 * for performing database transactions. 
	 * 
	 * @param Context context
	 */
	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.g = (Globals) context.getApplicationContext(); 
	}

	/**
	 * Initializes three database tables: 
	 * songs, jam, and log. 
	 * 
	 * @param SQLiteDatabase db
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_SONGS_TABLE = "CREATE TABLE " + TABLE_SONGS + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," 
				+ KEY_TITLE + " TEXT,"
				+ KEY_ARTIST + " TEXT,"
				+ KEY_ALBUM + " TEXT,"
				+ KEY_PATH + " TEXT,"
				+ KEY_LOCAL + " INTEGER," 
				+ KEY_ART + " TEXT,"
				+ KEY_HASH + " TEXT," 
				+ KEY_IP + " TEXT,"
				+ KEY_PORT + " INTEGER,"
				+ KEY_TRACK_NUM + " INTEGER,"
				+ " UNIQUE (" 
				+ KEY_TITLE + ", " 
				+ KEY_ARTIST + ", "
				+ KEY_ALBUM + ")"
				+ " ON CONFLICT IGNORE)";
		db.execSQL(CREATE_SONGS_TABLE);

		String CREATE_JAM_TABLE = "CREATE TABLE " + TABLE_JAM + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," 
				+ KEY_TITLE + " TEXT,"
				+ KEY_ARTIST + " TEXT,"
				+ KEY_ALBUM + " TEXT,"
				+ KEY_PATH + " TEXT,"
				+ KEY_LOCAL + " INTEGER," 
				+ KEY_ART + " TEXT,"
				+ KEY_HASH + " TEXT," 
				+ KEY_IP + " TEXT,"
				+ KEY_PORT + " INTEGER,"
				+ KEY_JAM_INDEX + " INTEGER,"
				+ KEY_ADDED_BY + " TEXT," 
				+ KEY_TIMESTAMP + " TEXT)";
		db.execSQL(CREATE_JAM_TABLE); 
		
		String CREATE_LOG_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_LOG + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," 
				+ KEY_START_TIME + " INTEGER,"
				+ KEY_LATEST_TIME + " INTEGER," 
				+ KEY_NUM_SONGS + " INTEGER DEFAULT 0,"
				+ KEY_NUM_USERS + " INTEGER)";
		db.execSQL(CREATE_LOG_TABLE);
	}

	/**
	 * Upgrades the database to a newer version. 
	 * 
	 * @param SQLiteDatabase db
	 * @param int oldVersion
	 * @param int newVersion
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older tables if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_JAM);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOG);
		
		// Create tables again
		onCreate(db);
	}
	
	/**
	 * Drops the given table from the database. 
	 * 
	 * @param String table
	 */
	public void dropTable(String table) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(table, null, null); 
	}
	
	/**
	 * Adds a new song to the music library,
	 * stored in the songs table. 
	 * 
	 * @param Song song
	 */
	public void addSongToLibrary(Song song){
		// 1. get reference to writable DB
		SQLiteDatabase db = this.getWritableDatabase();

		// 2. create ContentValues to add key "column"/value
		// key/value -> keys = column names/ values = column values
		ContentValues values = new ContentValues();
		values.put(KEY_TITLE, song.getTitle()); 
		values.put(KEY_ARTIST, song.getArtist());
		values.put(KEY_ALBUM, song.getAlbum());
		values.put(KEY_PATH, song.getPath());
		values.put(KEY_ART, song.getAlbumArt());
		values.put(KEY_HASH, Integer.toString(song.hashCode())); 
		values.put(KEY_IP, song.getIpAddr());
		values.put(KEY_PORT, song.getPort());
		values.put(KEY_TRACK_NUM, song.getTrackNum()); 

		int local = 0; 
		if (song.isLocal()) {
			local = 1; 
		}
		values.put(KEY_LOCAL, local);

		// 3. insert
		db.insert(TABLE_SONGS, null, values); 
	}

	/**
	 * Adds a new song to the Jam at the given 
	 * index with the given timestamp as an ID. 
	 * 
	 * @param Song song
	 * @param int index
	 * @param String timestamp
	 */
	public void addSongToJam(Song song, int index, String timestamp) {
		// 1. get reference to writable DB
		SQLiteDatabase db = this.getWritableDatabase();


		// 2. create ContentValues to add key "column"/value
		// key/value -> keys = column names/ values = column values
		ContentValues values = new ContentValues();
		values.put(KEY_TITLE, song.getTitle()); 
		values.put(KEY_ARTIST, song.getArtist());
		values.put(KEY_ALBUM, song.getAlbum());
		values.put(KEY_PATH, song.getPath());
		values.put(KEY_ART, song.getAlbumArt());
		values.put(KEY_HASH, Integer.toString(song.hashCode())); 
		values.put(KEY_IP, song.getIpAddr());
		values.put(KEY_PORT, song.getPort());
		values.put(KEY_JAM_INDEX, index); 
		values.put(KEY_ADDED_BY, song.getAddedBy());		
		values.put(KEY_TIMESTAMP, timestamp); 
		
		int local = 0; 
		if (song.isLocal()) {
			local = 1; 
		}
		values.put(KEY_LOCAL, local);

		// 3. insert
		db.insert(TABLE_JAM, null, values); 
	}

	/**
	 * Returns a cursor grouped by artists from the songs table. 
	 * 
	 * @returns Cursor artistsCursor
	 */
	public Cursor getAllArtists() {
		String query = "SELECT * FROM " + TABLE_SONGS + " "
				+ "GROUP BY " + KEY_ARTIST; 

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		return cursor; 
	}

	/**
	 * Returns a cursor containing all songs from the songs table. 
	 * 
	 * @returns Cursor songsCursor
	 */
	public Cursor getAllSongs() {  
		String query = "SELECT * FROM " + TABLE_SONGS + " "
				+ "ORDER BY " + KEY_ARTIST + " ASC, " 
				+ KEY_ALBUM + " ASC, " 
				+ KEY_TRACK_NUM + " ASC";

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		return cursor; 
	}
	
	/**
	 * Returns a cursor containing all songs by the given artist. 
	 * 
	 * @param String artist
	 * @returns Cursor songsByArtistCursor
	 */
	public Cursor getSongsByArtist(String artist) {
		String escapedArtist = artist.replace("'", "''");
		String query = "SELECT * FROM " + TABLE_SONGS + " WHERE " + KEY_ARTIST + " = '" + escapedArtist + "' ORDER BY " + KEY_ALBUM + " ASC, " + KEY_TRACK_NUM + " ASC";

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		return cursor; 
	}
	
	/**
	 * Gets a Song object from the database by 
	 * the song's unique hash code. 
	 * 
	 * @param String hash
	 * @returns Song matchingSong, or null if song not found
	 */
	public Song getSongByHash(String hash) {
		String query = "SELECT * FROM " + TABLE_SONGS + " WHERE " + KEY_HASH + " = '" + hash + "'";

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		if (cursor.moveToFirst()) {
			Song song = rowToSong(cursor); 
			cursor.close(); 
			return song; 
		} else { 
			cursor.close(); 
			return null; 
		}
	}
	
	/**
	 * Returns a string path to the album art 
	 * for the song with the given hash code. 
	 * 
	 * @param String hash
	 * @returns String albumArtPath
	 */
	public String getAlbumArtByHash(String hash) {
		String query = "SELECT * FROM " + TABLE_SONGS + " WHERE " + KEY_HASH + " = '" + hash + "'";

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		if (cursor.moveToFirst()) {
			String artPath = cursor.getString(COL_ART); 
			cursor.close(); 
			return artPath;  
		} else {
			cursor.close(); 
			return ""; 
		}
	}

	/**
	 * Returns a cursor grouped by albums from the songs table.  
	 * 
	 * @returns Cursor albumsCursor
	 */
	public Cursor getAllAlbums() {
		String query = "SELECT * FROM " + TABLE_SONGS + " GROUP BY " + KEY_ALBUM; 

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		return cursor; 
	}

	/**
	 * Returns a cursor grouped by albums by the given artist. 
	 * 
	 * @param String artist
	 * @returns Cursor albumsByArtistCursor
	 */
	public Cursor getAlbumsByArtist(String artist) {
		String escapedArtist = artist.replace("'",  "''");
		String query = "SELECT * FROM " + TABLE_SONGS + " WHERE " + KEY_ARTIST + " = '" + escapedArtist + "' GROUP BY " + KEY_ALBUM; 

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		return cursor; 
	}

	/**
	 * Returns a cursor containing all the songs
	 * by the given artist on the given album. 
	 * 
	 * @param String artist
	 * @param String album
	 * @returns Cursor songsCursor
	 */
	public Cursor getSongsByArtistAndAlbum(String artist, String album) {
		String escapedArtist = artist.replace("'", "''"); 
		String escapedAlbum = album.replace("'", "''");
		String query = "SELECT * FROM " + TABLE_SONGS + " WHERE " + KEY_ARTIST + " = '" + escapedArtist + "' AND " + KEY_ALBUM + " = '" + escapedAlbum + "'"; 

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		return cursor; 
	}

	/**
	 * Returns a Song object with the given title from 
	 * the songs table, or null if none exists. 
	 * 
	 * @param String title
	 * @returns Song song, or null if none exists
	 */
	public Song getSongByTitle(String title) {
		String escapedTitle = title.replace("'",  "''");
		String query = "SELECT * FROM " + TABLE_SONGS + " WHERE " + KEY_TITLE + " = '" + escapedTitle + "'"; 

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		if (cursor.moveToFirst()) {
			Song song = rowToSong(cursor); 
			cursor.close();
			return song; 
		} else {
			cursor.close(); 
			return null; 
		}
	}

	/**
	 * Returns a cursor containing all the songs in the jam table, 
	 * ordered by index. 
	 * 
	 * @returns Cursor jamCursor
	 */
	public Cursor getSongsInJam() {
		String query = "SELECT * FROM " + TABLE_JAM + " ORDER BY " + KEY_JAM_INDEX + " ASC";

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		return cursor; 
	}

	/**
	 * Returns a Song object from the jam at the given index, 
	 * or null if none exists. 
	 * 
	 * @param int index
	 * @returns Song song, or null if no song exists with the given index
	 */
	public Song getSongInJamByIndex(int index) {
		String query = "SELECT * FROM " + TABLE_JAM + " WHERE " + KEY_JAM_INDEX + " = " + index;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		if (cursor.moveToFirst()) { 
			Song song = rowToSong(cursor); 
			cursor.close(); 
			return song; 
		} else {
			cursor.close(); 
			return null; 
		}
	}
	
	/**
	 * Returns a cursor pointing to the song in the jam table
	 * with the given timestamp id, or null if none exists. 
	 * 
	 * @param String jamSongID
	 * @returns Cursor songCursor, or null if none exists
	 */
	public Cursor getSongInJamByID(String jamSongID) {
		String query = "SELECT * FROM " + TABLE_JAM + " WHERE " + KEY_TIMESTAMP + " = '" + jamSongID + "'";

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);
		
		if (cursor.moveToFirst()) {
			return cursor; 
		} else {
			cursor.close(); 
			return null; 
		}
	}
	
	/**
	 * Returns the index of the song in the jam with the 
	 * given timestamp ID, or null if none exists. 
	 * 
	 * @param String jamSongID
	 * @returns int index, or -1 if non exists
	 */
	public int getIndexInJamByID(String jamSongID) {
		String query = "SELECT * FROM " + TABLE_JAM + " WHERE " + KEY_TIMESTAMP + " = '" + jamSongID + "'";

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);
		
		if (cursor.moveToFirst()) {
			int index = cursor.getInt(COL_JAM_INDEX); 
			cursor.close(); 
			return index; 
		} else {
			cursor.close(); 
			return -1; 
		}
	}
	
	/**
	 * Returns the timestamp ID of the song in the Jam at the 
	 * given index, or the empty string if none exists. 
	 * 
	 * @param int index
	 * @returns String jamSongID, or empty string if none exists
	 */
	public String getJamSongIDByIndex(int index) {
		String query = "SELECT * FROM " + TABLE_JAM + " WHERE " + KEY_JAM_INDEX + " = " + index;

		SQLiteDatabase db = this.getWritableDatabase();
		
		Cursor cursor = db.rawQuery(query, null);
		
		if (cursor.moveToFirst()) { 
			String timestamp = cursor.getString(cursor.getColumnIndex(KEY_TIMESTAMP)); 
			cursor.close(); 
			return timestamp;
		} else {
			cursor.close(); 
			return ""; 
		}
	}

	/**
	 * Changes the index of the song in the jam 
	 * with the given timestamp ID. 
	 * 
	 * @param String jamSongID
	 * @param int to
	 * @returns int index that the song was moved from
	 */
	public int changeSongIndexInJam(String jamSongId, int to) {
		SQLiteDatabase db = this.getWritableDatabase();

		int from = getIndexInJamByID(jamSongId); 
		
		// Overwrite the song that is moving with a temp placeholder index
		ContentValues args = new ContentValues();
		args.put(KEY_JAM_INDEX, -2);
		
		db.update(TABLE_JAM, args, KEY_TIMESTAMP + " = '" + jamSongId + "'", null);

		
		// Increment/decrement the indices of all the songs 
		// in-between the to and from songs as necessary
		String restructureQuery = ""; 
		if (from < to) {
			restructureQuery = "UPDATE " + TABLE_JAM + " SET " + KEY_JAM_INDEX + " = " + KEY_JAM_INDEX + "-1 WHERE " + KEY_JAM_INDEX + " <= ? AND " + KEY_JAM_INDEX + " > ?";
		} else if (from > to) {
			restructureQuery = "UPDATE " + TABLE_JAM + " SET " + KEY_JAM_INDEX + " = " + KEY_JAM_INDEX + "+1 WHERE " + KEY_JAM_INDEX + " >= ? AND " + KEY_JAM_INDEX + " < ?";
		}
		String[] updateArgs = new String[] {"" + to, "" + from}; 
		db.execSQL(restructureQuery, updateArgs);

		// Move the original "from" song to its final "to" index
		args = new ContentValues();
		args.put(KEY_JAM_INDEX, to);
		db.update(TABLE_JAM, args, KEY_TIMESTAMP + " = '" + jamSongId + "'", null);
		
		return from; 
	}

	/**
	 * Shuffle all the songs in the jam after the currently-playing
	 * index, by randomly rearranging their indices in the jam table. 
	 * 
	 * @param int currentIndex
	 * @param int lastIndex
	 */
	public void shuffleJam(int currentIndex, int lastIndex) {
		ArrayList<String> ids = new ArrayList<String>(); 
		for (int i = currentIndex + 1; i <= lastIndex; i++) {
			ids.add(getJamSongIDByIndex(i)); 
		}

		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues args; 
		Random generator = new Random(); 
		
		for (int i = currentIndex + 1; i <= lastIndex; i++) {
			args = new ContentValues();
			int rand = generator.nextInt(ids.size());
			String jamSongID = ids.remove(rand); 
			args.put(KEY_TIMESTAMP, jamSongID);
			db.update(TABLE_JAM, args, KEY_JAM_INDEX + " = " + i + "", null);
		}

	}
	
	/**
	 * Checks if the jam contains a given song. 
	 * 
	 * @param Song song
	 * @returns True if the song exists in the Jam, or False otherwise
	 */
	public boolean jamContainsSong(Song song) {
		String query = "SELECT * FROM " + TABLE_JAM + " WHERE " + KEY_HASH + " = " + song.hashCode(); 
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		if (cursor.getCount() > 0) {
			return true; 
		} else {
			return false; 
		}
	}

	/**
	 * Removes the song with the given jamSongID from the jam. 
	 * 
	 * @param String jamSongID
	 * @returns int index of the removed song
	 */
	public int removeSongFromJam(String jamSongID) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		Cursor cursor = getSongInJamByID(jamSongID); 
		int removedIndex = cursor.getInt(cursor.getColumnIndex(KEY_JAM_INDEX)); 
		cursor.close(); 
		
		db.delete(TABLE_JAM, KEY_TIMESTAMP + " = '" + jamSongID + "'", null);

		String restructureQuery = "UPDATE " + TABLE_JAM + " SET " + KEY_JAM_INDEX + " = " + KEY_JAM_INDEX + "-1 WHERE " + KEY_JAM_INDEX + " > ?";
		String[] updateArgs = new String[] {"" + removedIndex}; 
		db.execSQL(restructureQuery, updateArgs);
		
		return removedIndex; 
	}

	/**
	 * Resets the jam table. 
	 * 
	 */
	public void clearJam() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_JAM, null, null);
	}

	/**
	 * Sets the album art field for all rows in the songs table
	 * with the given album title.  
	 * 
	 * @param String albumTitle
	 * @param String albumArtPath
	 */
	public void setAlbumArt(String albumTitle, String path) {
		String escapedAlbumTitle = albumTitle.replace("'",  "''");
		ContentValues args = new ContentValues();
		args.put(KEY_ART, path);
		SQLiteDatabase db = this.getWritableDatabase();
		db.update(TABLE_SONGS, args, KEY_ALBUM + " = '" + escapedAlbumTitle + "'", null);
		db.update(TABLE_JAM, args, KEY_ALBUM + " = '" + escapedAlbumTitle + "'", null);
	}

	/**
	 * Searches the songs table for artists
	 * containing the character constraint.  
	 * 
	 * @param CharSequence constraint
	 */
	public Cursor searchArtists(CharSequence constraint) {
		String query; 
		if (constraint == null || constraint.length() == 0) {
			query = "SELECT * FROM " + TABLE_SONGS 
					+ " GROUP BY " + KEY_ARTIST; 
		} else {
			query = "SELECT * FROM " + TABLE_SONGS 
					+ " WHERE " + KEY_ARTIST + " LIKE '%" + constraint.toString() + "%' "
					+ "GROUP BY " + KEY_ARTIST; 
		}

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		return cursor; 
	}

	/**
	 * Searches the songs table for songs
	 * containing the character constraint.  
	 * 
	 * @param CharSequence constraint
	 */
	public Cursor searchSongs(CharSequence constraint) {
		String query; 
		if (constraint == null || constraint.length() == 0) {
			query = "SELECT * FROM " + TABLE_SONGS
					+ " ORDER BY " + KEY_ARTIST + " ASC, " 
					+ KEY_ALBUM + " ASC, " 
					+ KEY_TRACK_NUM + " ASC"; ;  
		} else {
			query = "SELECT * FROM " + TABLE_SONGS + " "
					+ "WHERE " + KEY_TITLE + " LIKE '%" + constraint.toString() + "%' "
					+ "OR " + KEY_ALBUM + " LIKE '%" + constraint.toString() + "%' "
					+ "OR " + KEY_ARTIST + " LIKE '%" + constraint.toString() + "%' "
					+ "ORDER BY " + KEY_ARTIST + " ASC, " 
					+ KEY_ALBUM + " ASC, " 
					+ KEY_TRACK_NUM + " ASC"; 
		}

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		return cursor; 
	}

	/**
	 * Searches the songs table for songs by the 
	 * given artist containing the character constraint. 
	 * 
	 * @param CharSequence constraint
	 */
	public Cursor searchSongsByArtist(CharSequence constraint, String artist) {
		String escapedArtist = artist.replace("'", "''"); 
		String query; 
		if (constraint == null || constraint.length() == 0) {
			query = "SELECT * FROM " + TABLE_SONGS 
					+ " WHERE " + KEY_ARTIST + " = '" + escapedArtist + "' "
					+ "ORDER BY " + KEY_ARTIST + " ASC, " 
					+ KEY_ALBUM + " ASC, " 
					+ KEY_TRACK_NUM + " ASC"; 
		} else {
			query = "SELECT * FROM " + TABLE_SONGS 
					+ " WHERE (" + KEY_TITLE + " LIKE '%" + constraint.toString() + "%' "
					+ "OR " + KEY_ALBUM + " LIKE '%" + constraint.toString() + "%') "
					+ "AND " + KEY_ARTIST + " = '" + escapedArtist + "' "
					+ "ORDER BY " + KEY_ARTIST + " ASC, " 
					+ KEY_ALBUM + " ASC, " 
					+ KEY_TRACK_NUM + " ASC"; 
		}

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		return cursor; 
	}

	/**
	 * Converts a row from the database to a Song object. 
	 * 
	 * @param Cursor cursor
	 * @returns Song song
	 */
	public Song rowToSong(Cursor cursor) {
		boolean local = false; 
		if (Integer.parseInt(cursor.getString(COL_LOCAL)) == 1) {
			local = true; 
		} 
		Song song = new Song(cursor.getString(COL_TITLE), cursor.getString(COL_PATH), local); 
		song.setArtist(cursor.getString(COL_ARTIST));
		song.setAlbum(cursor.getString(COL_ALBUM));
		song.setIpAddr(cursor.getString(COL_IP));
		song.setPort(cursor.getInt(COL_PORT));
		song.setAlbumArt(cursor.getString(COL_ART));
		
		if (cursor.getColumnIndex(KEY_TIMESTAMP) != -1) {
			song.setJamID(cursor.getString(cursor.getColumnIndex(KEY_TIMESTAMP)));
		}
		if (cursor.getColumnIndex(KEY_ADDED_BY) != -1) {
			song.setAddedBy(cursor.getString(cursor.getColumnIndex(KEY_ADDED_BY))); 
		}
		
		return song; 
	}

	/**
	 * Converts the songs table to a JSONObject. 
	 * 
	 * @returns JSONObject songLibrary
	 */
	public JSONObject getLibraryAsJSON() {
		JSONObject json = new JSONObject(); 

		//synchronized (this) {
	
			JSONArray artistArray = getArtistsAsJSON(); 
	
			try {
				json.put("artists", artistArray); 
			} catch (JSONException e) {
				e.printStackTrace(); 
			}
		//}
		return json; 

	}

	/**
	 * Converts the artists from the songs table
	 * to a JSONObject. 
	 * Helper method for getLibraryAsJSON(). 
	 * 
	 * @returns JSONArray artistsArray
	 */
	private JSONArray getArtistsAsJSON() {
		JSONArray artistArray = new JSONArray(); 
		Cursor artistCursor = getAllArtists(); 

		if (artistCursor.moveToFirst()) {
			do {
				String artistName = artistCursor.getString(COL_ARTIST); 	        	
				JSONObject artist = new JSONObject(); 

				JSONArray albumArray = getAlbumsAsJSON(artistName); 

				try {
					artist.put("name", artistName); 
					artist.put("albums", albumArray); 
					artistArray.put(artist); 
				} catch (JSONException e) {
					e.printStackTrace();
				}

			} while (artistCursor.moveToNext());
		}

		artistCursor.close(); 

		return artistArray; 
	}

	/**
	 * Converts the albums from the songs table
	 * by the given artist to a JSONObject. 
	 * Helper method for getLibraryAsJSON(). 
	 * 
	 * @param String artistName
	 * @returns JSONArray albumsArray
	 */
	private JSONArray getAlbumsAsJSON(String artistName) {
		JSONArray albumArray = new JSONArray(); 
		Cursor albumCursor = getAlbumsByArtist(artistName); 

		if (albumCursor.moveToFirst()) {
			do {
				String albumTitle = albumCursor.getString(COL_ALBUM); 
				JSONObject album = new JSONObject(); 

				JSONArray songArray = getSongsAsJSON(artistName, albumTitle); 

				try {
					album.put("title", albumTitle);
					album.put("songs", songArray); 
					albumArray.put(album); 
				} catch (JSONException e) {
					e.printStackTrace();
				} 

			} while (albumCursor.moveToNext()); 
		}

		albumCursor.close(); 

		return albumArray; 
	}

	/**
	 * Converts the songs from the songs table
	 * by the given artist and album to a JSONObject. 
	 * Helper method for getLibraryAsJSON(). 
	 * 
	 * @returns JSONArray songsArray
	 */
	private JSONArray getSongsAsJSON(String artistName, String albumTitle) {
		JSONArray songArray = new JSONArray(); 
		Cursor songCursor = getSongsByArtistAndAlbum(artistName, albumTitle); 

		if (songCursor.moveToFirst()) {
			do {
				JSONObject song = new JSONObject(); 
				String title = songCursor.getString(COL_TITLE);
				String path = songCursor.getString(COL_PATH);
				String ip = songCursor.getString(COL_IP); 
				int port = songCursor.getInt(COL_PORT);
				int trackNum = songCursor.getInt(COL_TRACK_NUM); 

				try {
					song.put("title", title);
					song.put("path", path);
					song.put("ip", ip); 	
					song.put("port", port);
					song.put("num", trackNum); 
					songArray.put(song); 
				} catch (JSONException e) {
					e.printStackTrace();
				} 

			} while (songCursor.moveToNext()); 
		}

		songCursor.close(); 

		return songArray; 
	}

	/**
	 * Encodes all album art from the songs table
	 * in Base64 format and returns as JSONObject.  
	 * 
	 * @returns JSONObject encodedAlbumArt
	 */
	public JSONObject getAlbumArtAsJSON() {
		JSONObject albumArt = new JSONObject(); 
		JSONArray albumArray = new JSONArray(); 
		JSONArray artArray = new JSONArray(); 

		Cursor artCursor = getAllAlbums(); 

		if (artCursor.moveToFirst()) {
			do {

				String albumName = artCursor.getString(COL_ALBUM); 
				String artPath = artCursor.getString(COL_ART); 	        	
				String encodedImage = ""; 
				if (artPath != null && !artPath.equals("")) {
					Bitmap bitmap = BitmapFactory.decodeFile(artPath); 
					ByteArrayOutputStream byteStream = new ByteArrayOutputStream();  
					bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteStream);
					byte[] byteArrayImage = byteStream.toByteArray(); 
					encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);	
					albumArray.put(albumName); 
					artArray.put(encodedImage); 
				}
			} while (artCursor.moveToNext()); 
		}

		try {
			albumArt.put("albums", albumArray);
			albumArt.put("art", artArray); 
		} catch (JSONException e) {
			e.printStackTrace();
		}

		artCursor.close(); 

		return albumArt; 
	}


	/**
	 * Loads music metadata into the songs table 
	 * by parsing a remote JSON response from another phone. 
	 * 
	 * @param JSONArray jsonArtists
	 */
	public void loadMusicFromJSON(JSONArray artists) { 
		//synchronized (this) {
			for (int i = 0; i < artists.length(); i++) {
				try {
					JSONObject jsonArtist = artists.getJSONObject(i); 
					String artistName = (String)jsonArtist.get("name"); 
					JSONArray albums = jsonArtist.getJSONArray("albums"); 
	
					for (int j = 0; j < albums.length(); j++) {
						JSONObject jsonAlbum = albums.getJSONObject(j); 
						String albumTitle = (String)jsonAlbum.get("title");
						JSONArray songs = jsonAlbum.getJSONArray("songs"); 
	
						for (int k = 0; k < songs.length(); k++) {
							JSONObject jsonSong = songs.getJSONObject(k); 
							String songTitle = (String)jsonSong.get("title"); 
							String songPath = (String)jsonSong.get("path");
							String songIp = (String)jsonSong.get("ip"); 
							int port = jsonSong.getInt("port");
							int trackNum = jsonSong.getInt("num"); 
							Song song = new Song(songTitle, songPath, false);
							song.setArtist(artistName); 
							song.setAlbum(albumTitle); 
							song.setIpAddr(songIp);
							song.setPort(port);
							song.setAlbumArt("");
							song.setTrackNum(trackNum);
	
							addSongToLibrary(song); 
						}
	
						g.sendUIMessage(0); 
					}
				} catch (JSONException e) {
					e.printStackTrace();
				} 
			}
		//}
	}

	/**
	 * Decodes and saves Base64-encoded album art 
	 * by parsing a remote JSON response from another phone. 
	 * 
	 * @param JSONArray jsonAlbumArt
	 */
	public void loadAlbumArtFromJSON(JSONObject albumArt) {
		JSONArray albumArray = null; 
		JSONArray artArray = null; 
		try {
			albumArray = albumArt.getJSONArray("albums");
			artArray = albumArt.getJSONArray("art"); 
		} catch (JSONException e1) {
			e1.printStackTrace();
		} 

		if (albumArray != null && artArray != null) {
			for (int i = 0; i < albumArray.length(); i++) {
				try {
					String albumTitle = (String)albumArray.get(i); 
					String escapedAlbumTitle = albumTitle.replaceAll("/", ""); 
					String artJSON = (String)artArray.get(i); 

					String artPath = ""; 
					if (artJSON != null && !artJSON.equals("")) {
						byte[] artBytes = Base64.decode(artJSON, Base64.DEFAULT);
						Bitmap bitmap = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length); 
						String filename = escapedAlbumTitle; 
						FileOutputStream outputStream;
						try {
							outputStream = g.openFileOutput(filename, Context.MODE_PRIVATE);
							bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream); 
							outputStream.close();
							artPath = g.getFileStreamPath(filename).getAbsolutePath(); 
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					setAlbumArt(albumTitle, artPath); 

				} catch (JSONException e) {
					e.printStackTrace();
				} 
			}
			g.sendUIMessage(0); 
		}
	}

	/**
	 * Deletes all songs associated with the given ip address from the jam table.
	 * 
	 * @param String ipAddr
	 * @returns int number of rows deleted
	 */
	public int deleteJamSongsFromIp(String ipAddr) {
		SQLiteDatabase db = this.getWritableDatabase();
		return db.delete(TABLE_JAM, KEY_IP + "='" + ipAddr + "'", null);
	}

	/**
	 * Deletes all songs associated with the given ip address from the song table.
	 * 
	 * @param String ipAddr
	 * @returns int number of rows deleted.
	 */
	public int deleteSongsFromIp(String ipAddr) {
		SQLiteDatabase db = this.getWritableDatabase();
		return db.delete(TABLE_SONGS, KEY_IP + "='" + ipAddr + "'", null);
	}
	
	/**
	 * Updates the port number for all local songs based on the server port.
	 * 
	 * @returns int number of rows updated
	 */
	public int updatePortForLocalSongs() {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues args = new ContentValues();
		args.put(KEY_PORT,  g.getServerPort());
		int updated = db.update(TABLE_SONGS,  args,  KEY_LOCAL + "='" + 1 + "'", null);
		updated += db.update(TABLE_JAM,  args,  KEY_LOCAL + "='" + 1 + "'", null);
		return updated;
	}
	
	/**
	 * Creates a jam in the log database.
	 * Sets the start_time and latest_time timestamps to the current system time.
	 * 
	 * @returns long id of the jam
	 */
	public long createJamInLog() {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		int timestamp = (int) (System.currentTimeMillis() / 1000L);
		values.put(KEY_NUM_USERS, 1);
		values.put(KEY_START_TIME, timestamp);
		values.put(KEY_LATEST_TIME, timestamp);
		return db.insert(TABLE_LOG, null, values);
	}
	
	/**
	 * Updates the latest_time timestamp for the provided jamId.
	 * 
	 * @param long jamId
	 * @returns the number of rows updated, which should always just be 1.
	 */
	public int updateJamTimestamp(long jamId) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_LATEST_TIME, (int) (System.currentTimeMillis() / 1000L));
		return db.update(TABLE_LOG, values, KEY_ID + "='" + String.valueOf(jamId)+ "'", null);
	}
	
	/**
	 * Increments the number of users in the jam specified by the provided id.
	 * 
	 * @param long jamId
	 */
	public void incrementJamNumUsers(long jamId) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("UPDATE " + TABLE_LOG + " SET "
                + KEY_NUM_USERS + " = " + KEY_NUM_USERS + " +1 WHERE "
                + KEY_ID + "='" + String.valueOf(jamId) + "'");
	}
	
	/**
	 * Updates the number of songs in the jam log specified by the provided id.
	 * The number of songs is set to the number of entries in the song table at
	 * the time that the method is executed.
	 * 
	 * @param longJamId
	 * @returns int number of rows updated, which should always be 1
	 */
	public int updateNumSongs(long jamId) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		// number of songs should be equals to the number of entries in the 
		// song table
		long numSongs = DatabaseUtils.queryNumEntries(db, TABLE_SONGS);

		ContentValues values = new ContentValues();
		values.put(KEY_NUM_SONGS, numSongs);
		String where = KEY_ID + "=? AND " + KEY_NUM_SONGS + "<?";
		String whereArgs[] = new String[] {String.valueOf(jamId), String.valueOf(numSongs)};
				
		// only update the number of songs if it is greater than the previous number of songs.
		return db.update(TABLE_LOG, values, where, whereArgs);	
	}
	
	/**
	 * Returns all jam logs which have not been updated in the last 15 seconds as a JSONObject.
	 * The object has one record: jam_list, which is a list of json jams. Each json jam has
	 * four fields: start_time, length,  
	 */
	public JSONObject getLogDataAsJson() {
		JSONObject jsonJamLog = new JSONObject();
		JSONArray jsonJamLogArray = new JSONArray();
		
		int timestamp = (int) (System.currentTimeMillis() / 1000L);
		
		String query = "SELECT * FROM " + TABLE_LOG + " WHERE " + KEY_LATEST_TIME + " < " + (timestamp - 15);

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);
		int numEntries = 0;
		
		if (cursor.moveToFirst()) {
			do {
				JSONObject jsonJamData = new JSONObject();
				try {
					int startTime = cursor.getInt(COL_START_TIME);
					jsonJamData.put("start_time", startTime);
					jsonJamData.put("length", cursor.getInt(COL_LATEST_TIME) - startTime);
					jsonJamData.put("num_users", cursor.getInt(COL_NUM_USERS));
					jsonJamData.put("num_songs", cursor.getInt(COL_NUM_SONGS));
					jsonJamData.put("id", cursor.getInt(COL_ID));
					jsonJamData.put("latest_time", cursor.getInt(COL_LATEST_TIME));
					jsonJamLogArray.put(jsonJamData);
					numEntries++;
				} catch (JSONException e) {
					e.printStackTrace();
					db.delete(TABLE_LOG, null, null); // if there is an error processing the json
					// it was entered into the table and logging is impossible
					// just delete all entries in the table the return null
					return null;
				}
			}
			while (cursor.moveToNext());
		} 
		else {
			return null;
		}
		
		cursor.close();
		
		try {
			jsonJamLog.put("jam_list",  jsonJamLogArray);
			jsonJamLog.put("numEntries", numEntries);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
		return jsonJamLog;
	}
	
	/**
	 * Deletes all jam log data from the database with latest_time timestamps older than 15 seconds.
	 * 
	 * @returns int number of rows affected (removed)
	 */
	public int deleteLogData() {
		int timestamp = (int) (System.currentTimeMillis() / 1000L);
		
		SQLiteDatabase db = this.getWritableDatabase();
		
		return db.delete(TABLE_LOG, KEY_LATEST_TIME + "<?", new String[]{String.valueOf(timestamp - 15)});
	}
}
