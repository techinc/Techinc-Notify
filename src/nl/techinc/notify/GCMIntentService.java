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
import android.net.Uri;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

	private static final String GCM_URL = "http://techincnotify.appspot.com";
	private static final int NOTE_ID = 1;
	private String key;

	@Override
	protected void onError(Context context, String errorId) {
		Log.e("GCM", "Error: "+errorId);
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		try {
			boolean state = SpaceState.updateState();
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			int icon = state ? R.drawable.techinclogo : R.drawable.techinclogo_mono;
			CharSequence tickerText = state ? getString(R.string.ticker_open) : getString(R.string.ticker_closed);
			long when = System.currentTimeMillis();
			CharSequence contentTitle = getString(R.string.app_name);
			CharSequence contentText = state ? getString(R.string.notify_open) : getString(R.string.notify_closed);
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, NotifyActivity.class), 0);
			Notification notification = new Notification(icon, tickerText, when);
			notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
			notificationManager.notify(NOTE_ID, notification);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onRegistered(Context context, String regId) {
		Log.v("GCM", regId);
		try
		{
			String url = Uri.parse(GCM_URL).buildUpon().appendPath("register").appendQueryParameter("id", regId).build().toString();
			URLConnection connect = new URL(url).openConnection();
			connect.connect();
			BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			String input = in.readLine();
			in.close();
			key = input;
			Log.v("GCM", input);
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void onUnregistered(Context context, String regId) {
		try
		{
			String url = Uri.parse(GCM_URL).buildUpon().appendPath("unregister").appendQueryParameter("id", regId).appendQueryParameter("key", key).build().toString();
			URLConnection connect = new URL(url).openConnection();
			connect.connect();
			BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			String input = in.readLine();
			in.close();
			Log.i("GCM", input);
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

}
