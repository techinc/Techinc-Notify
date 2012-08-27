package nl.techinc.notify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class SpaceState {
	private static final String POLL_URL = "http://techinc.nl/space/spacestate";
	private static final String STATE_CLOSED = "closed";
	public static boolean state;
	
	public static boolean updateState() throws IOException
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
		return state;
	}
}
