package nl.techinc.notify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

	private static final int NOTE_ID = 1;
	private String key;

	@Override
	protected void onError(Context context, String errorId) {
		Log.e("GCM", "Error: "+errorId);
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		String stateStr = intent.getStringExtra("state");
		if(stateStr.startsWith("dbg"))
		{
			if(!sharedPref.getBoolean("debug", false))
				return;
			stateStr = new String(stateStr.substring(3));
		}
		if(!stateStr.equals("closed") && !stateStr.equals("open"))
			return;
		boolean state = !(stateStr.equals("closed"));
		SpaceState.broadcastState(context, state);
		if(!state && sharedPref.getBoolean("suppress", false))
			return;
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		int icon = R.drawable.techinclogo_white;
		CharSequence tickerText = state ? getString(R.string.ticker_open) : getString(R.string.ticker_closed);
		long when = System.currentTimeMillis();
		CharSequence contentTitle = getString(R.string.app_name);
		CharSequence contentText = state ? getString(R.string.notify_open) : getString(R.string.notify_closed);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, NotifyActivity.class), 0);
		Notification notification = new Notification(icon, tickerText, when);
		if(sharedPref.getBoolean("vibrate", false)) notification.defaults |= Notification.DEFAULT_VIBRATE;
		String ringtone = sharedPref.getString("ringtone", "");
		if(!ringtone.equals("")) notification.sound = Uri.parse(ringtone);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		notificationManager.notify(NOTE_ID, notification);
	}

	@Override
	protected void onRegistered(Context context, String regId) {
		try
		{
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
			String url = Uri.parse(sharedPref.getString("url", "http://techinc.notefaction.jit.su")).buildUpon().appendPath("register").appendQueryParameter("id", regId).build().toString();
			URLConnection connect = new URL(url).openConnection();
			connect.connect();
			BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			String input = in.readLine();
			in.close();
			key = input;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	protected void onUnregistered(Context context, String regId) {
		try
		{
			if(key == null)
			{
				return;
			}
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
			String url = Uri.parse(sharedPref.getString("url", "http://techinc.notefaction.jit.su")).buildUpon().appendPath("unregister").appendQueryParameter("id", regId).appendQueryParameter("key", key).build().toString();
			URLConnection connect = new URL(url).openConnection();
			connect.connect();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

}
