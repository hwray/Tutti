package com.stanford.tutti;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import com.stanford.tutti.NanoHTTPD.Response.Status;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Path;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/*
 * Test Class for networking between phones. 
 */
public class StreamSongTestActivity extends Activity {
	
	private final int PORT = 1234;
	private Server server;
	private Globals g;
	
	private Button clientButton;
	private Button serverButton;
	private EditText editText;
	
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
	
	private void configureClientButton() {
		clientButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				(new ClientThread()).start();
			}
		});		
	}

	private void configureServerButton() {
		serverButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
		        server = new Server();
		        try {
					server.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_stream_song_test);
		System.out.println(getIpAddr());
		clientButton = (Button) this.findViewById(R.id.activity_stream_song_test_client_btn);
		serverButton = (Button) this.findViewById(R.id.activity_stream_song_test_server_btn);
		editText = (EditText) this.findViewById(R.id.ip_address);
		editText.setText(getIpAddr());
		g = (Globals) getApplication();
		configureClientButton();
		configureServerButton();
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {       
        onBackPressed(); 
        return true;
    }
    
    class Server extends NanoHTTPD {
    	public Server() {
    		super(PORT);
    	}
    	
        @Override
        public Response serve(final String uri, final Method method, 
                              Map<String, String> header,
                              Map<String, String> parameters,
                              Map<String, String> files)  {
        	System.out.println(uri);
        	System.out.println(method.toString());
        	for (String str : header.keySet())
        		System.out.println(str + ": " + header.get(str));
            for (String str : parameters.keySet())
            	System.out.println(str + " : " + parameters.get(str));
            runOnUiThread(new Runnable() {
				@Override
				public void run() {
                    editText.setText(method.toString() + uri);
				}
            	
            });
            FileInputStream fis = null;
            try {
            	String path = g.getArtistList().get(0).getAlbumList().get(0).getSongList().get(0).getPath();
            	System.out.println("returning file: " + path);
                fis = new FileInputStream(path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return new NanoHTTPD.Response(Status.OK, "audio/mpeg", fis);
        }
    }

    class ClientThread extends Thread {
    	public void run() {
    		try {
    			String ipAddr = editText.getText().toString();
    			Uri uri = Uri.parse("http://" + ipAddr + ":" + PORT);
    			System.out.println(uri.toString());
    			MediaPlayer mp = new MediaPlayer();
    			mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
    			mp.setDataSource(getApplicationContext(), uri);
    			mp.prepare();
    			mp.start();
    		}
    		catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    }
}

