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
	public static final String SENDER_ID = "1093719656719";

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
		boolean state;
		long msgTime;
		long curTime = System.currentTimeMillis() / 1000L;
		String timeStr = intent.getStringExtra("time").trim().replace(".", "");
		msgTime = Long.parseLong(timeStr) / 1000L;
		boolean changed = true;
		if(curTime - msgTime > 3600)
		{
			state = SpaceState.updateState(context);
		}
		else
		{
			NotifyApp application = (NotifyApp) context.getApplicationContext();
			if(msgTime < application.getLastUpdated())	// If this message was sent earlier than when we last checked
				return; 								// Ignore it
			application.setLastUpdated(msgTime);
			state = !(stateStr.equals("closed"));
			changed = SpaceState.broadcastState(context, state);
		}
		if(!state && sharedPref.getBoolean("suppress", false))
			return;
		if(!changed)
			return;
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		int icon = R.drawable.techinclogo_white;
		CharSequence tickerText = state ? getString(R.string.ticker_open) : getString(R.string.ticker_closed);
		long when = System.currentTimeMillis();
		CharSequence contentTitle = getString(R.string.app_name);
		CharSequence contentText = state ? getString(R.string.notify_open) : getString(R.string.notify_closed);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, NotifyActivity.class), 0);
		Notification notification = new Notification(icon, tickerText, when);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		if(sharedPref.getBoolean("vibrate", false)) notification.defaults |= Notification.DEFAULT_VIBRATE;
		String ringtone = sharedPref.getString("ringtone", "");
		if(!ringtone.equals("")) notification.sound = Uri.parse(ringtone);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		notificationManager.notify(NOTE_ID, notification);
	}

	@Override
	protected void onRegistered(Context context, String regId) {
		NotifyApp application = (NotifyApp) getApplicationContext();
		application.setUpdating(false);
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		final String url = Uri.parse(sharedPref.getString("url", "http://techinc.notefaction.jit.su")).buildUpon().appendPath("register").appendQueryParameter("id", regId).build().toString();
		sharedPref.edit().remove("backoff").commit();
		register(context, url, 1000);
	}
	
	private void register(final Context context, final String url, final int delayMillis)
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
					register(context, url, delayMillis*2);
				}
			};
			new Handler().postDelayed(runnable, delayMillis);
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
		unregister(context, url, 1000);
	}

	private void unregister(final Context context, final String url, final int delayMillis)
	{
		NotifyApp application = (NotifyApp) getApplicationContext();
		application.setUpdating(false);
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
			e.printStackTrace();
			if(delayMillis > 16000)
				return;
			Runnable runnable = new Runnable()
			{
				public void run() {
					unregister(context, url, delayMillis*2);
				}
			};
			new Handler().postDelayed(runnable, delayMillis);
		}
	}
}
