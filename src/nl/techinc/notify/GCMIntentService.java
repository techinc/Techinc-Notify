package nl.techinc.notify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

public class GCMIntentService extends GCMBaseIntentService {
	
	public static final String ACTION_REGISTER = "nl.techinc.notify.intent.action.register";

	private static final int NOTE_ID = 1;
	private String key;
	
	public boolean getRegistered(Context context)
	{
		return GCMRegistrar.isRegistered(context);
	}

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
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		final String url = Uri.parse(sharedPref.getString("url", "http://techinc.notefaction.jit.su")).buildUpon().appendPath("register").appendQueryParameter("id", regId).build().toString();
		sharedPref.edit().remove("backoff").commit();
		register(context, url);
	}
	
	private void register(final Context context, final String url)
	{
		try
		{
			URLConnection connect = new URL(url).openConnection();
			connect.connect();
			HttpURLConnection httpConnection = (HttpURLConnection) connect;
			BufferedReader in = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
			String input = in.readLine();
			in.close();
			key = input;
			int response = httpConnection.getResponseCode();
			if(!(response == 200))
				throw new IOException("Response: "+Integer.toString(response));
			Intent intent = new Intent();
			intent.setAction(ACTION_REGISTER);
			intent.putExtra("enabled", true);
			context.sendBroadcast(intent);
		}
		catch(IOException e)
		{
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
			int delayMillis = sharedPref.getInt("backoff", 1000);
			e.printStackTrace();
			if(delayMillis > 16000)
			{
				Intent intent = new Intent();
				intent.setAction(ACTION_REGISTER);
				intent.putExtra("enabled", false);
				context.sendBroadcast(intent);
			}
			Runnable runnable = new Runnable()
			{
				public void run() {
					register(context, url);
				}
			};
			new Handler().postDelayed(runnable, delayMillis);
			sharedPref.edit().putInt("backoff", delayMillis*2);
		}
	}

	@Override
	protected void onUnregistered(Context context, String regId) {
		if(key == null)
		{
			return;
		}
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		String url = Uri.parse(sharedPref.getString("url", "http://techinc.notefaction.jit.su")).buildUpon().appendPath("unregister").appendQueryParameter("id", regId).appendQueryParameter("key", key).build().toString();
		sharedPref.edit().remove("backoff").commit();
		unregister(context, url);
	}

	private void unregister(final Context context, final String url)
	{
		Intent intent = new Intent();
		intent.setAction(ACTION_REGISTER);
		intent.putExtra("enabled", false);
		context.sendBroadcast(intent);
		try
		{
			URLConnection connect = new URL(url).openConnection();
			connect.connect();
			HttpURLConnection httpConnection = (HttpURLConnection) connect;
			int response = httpConnection.getResponseCode();
			if(!(response == 200))
				throw new IOException("Response: "+Integer.toString(response));
		}
		catch(IOException e)
		{
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
			int delayMillis = sharedPref.getInt("backoff", 1000);
			e.printStackTrace();
			if(delayMillis > 16000)
				return;
			Runnable runnable = new Runnable()
			{
				public void run() {
					unregister(context, url);
				}
			};
			new Handler().postDelayed(runnable, delayMillis);
			sharedPref.edit().putInt("backoff", delayMillis*2);
		}
	}
}
