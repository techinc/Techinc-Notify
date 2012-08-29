package nl.techinc.notify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.content.Intent;

public class SpaceState {
	private static final String POLL_URL = "http://techinc.nl/space/spacestate";
	private static final String STATE_CLOSED = "closed";
	public static final String ACTION_STATE = "nl.techinc.notify.intent.action.STATE";
	public static final String PARAM_STATE = "state";
	public static boolean state;
	
	public static void updateState(Context context) throws IOException
	{
		try {
			URLConnection connect = new URL(POLL_URL).openConnection();
			connect.connect();
			BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			String input = in.readLine();
			in.close();
			state = !(STATE_CLOSED.equalsIgnoreCase(input.trim()));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		broadcastState(context, state);
	}
	
	public static void broadcastState(Context context, boolean state)
	{
		SpaceState.state = state;
		broadcastState(context);
	}
	
	public static void broadcastState(Context context)
	{
		Intent intent = new Intent();
		intent.setAction(ACTION_STATE);
		intent.putExtra(PARAM_STATE, state);
		context.sendBroadcast(intent);
		intent = new Intent(context, NotifyAppWidgetProvider.class);
		intent.putExtra(PARAM_STATE, state);
		context.sendBroadcast(intent);
	}
}