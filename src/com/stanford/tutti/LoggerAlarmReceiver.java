package com.stanford.tutti;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Random;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;

public class LoggerAlarmReceiver extends WakefulBroadcastReceiver {
	private AlarmManager alarmManager;
	private PendingIntent alarmIntent;
	private static final String ALARM_SET_FLAG = "alarmSet";

	@Override
	public void onReceive(Context context, final Intent intent) {
		final Globals g = (Globals) context.getApplicationContext();
		if (g.db == null) {
			g.db = new DatabaseHandler(g);
		}
		
		JSONObject jsonJamLog = g.db.getLogDataAsJson();
		
		try {
			if (jsonJamLog == null || jsonJamLog.getInt("numEntries") <= 0) {
				System.out.println("jsonJamLog is null or empty -- not logging to server");
				System.out.println("jsonJamLog: " + jsonJamLog);
				LoggerAlarmReceiver.completeWakefulIntent(intent);
				return;
			}

			String serverUrl = context.getString(R.string.ec2_server);
			String logUrl = "http://" + serverUrl + "/log";
			AsyncHttpClient client = new AsyncHttpClient();
	        StringEntity entity = new StringEntity(jsonJamLog.toString());
	        
	        client.post(context, logUrl, entity, "application/json",
	            new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
						System.out.println("Logged jam data to server");
						System.out.println("deleted " + g.db.deleteLogData() + " jams from log");
						LoggerAlarmReceiver.completeWakefulIntent(intent);
					}
		
					@Override
					public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
						System.out.println("Failed to log jam data to server");
						LoggerAlarmReceiver.completeWakefulIntent(intent);
					}		
				});
		} catch (JSONException e) {
			e.printStackTrace();
			return;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}

	public void setAlarm(Context context) {
		System.out.println("set alarm called in logger alarm");
		alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, LoggerAlarmReceiver.class);
		alarmIntent = PendingIntent.getBroadcast(context, 1234, intent, 0);
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

		// only set alarm if not already set!
		// note: this call still returns not null even if the alarm has been canceled -- bad for testing
		if (!preferences.getBoolean(ALARM_SET_FLAG,  false)) {
			Random random = new Random();
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			// Set the alarm's trigger to be a random time during the next ~6 hours, and repeat every ~6 hours
			calendar.add(Calendar.HOUR_OF_DAY, random.nextInt(6));
			calendar.add(Calendar.MINUTE, random.nextInt(60));
			
			System.out.println("Setting alarm trigger for logger: " + calendar);

			//alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, 5000, alarmIntent);
			alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 6 * AlarmManager.INTERVAL_HOUR, alarmIntent);

			preferences.edit().putBoolean(ALARM_SET_FLAG, true).apply();
			
			/*
	        // Enable {@code SampleBootReceiver} to automatically restart the alarm when the
	        // device is rebooted.
	        ComponentName receiver = new ComponentName(context, SampleBootReceiver.class);
	        PackageManager pm = context.getPackageManager();
	        pm.setComponentEnabledSetting(receiver,
	                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
	                PackageManager.DONT_KILL_APP);
			 */
		}
		else {
			alarmManager.cancel(alarmIntent);
		}
	}

}
