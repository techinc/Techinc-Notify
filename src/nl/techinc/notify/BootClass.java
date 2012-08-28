package nl.techinc.notify;

import com.google.android.gcm.*;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Boot Class is the Broadcast receiver that processes the boot up of the device.
 * It checks if the Service is configured to start on boot up of the device and start the service
 * as required.
 * 
 * @author rob gilham
 *
 */
public class BootClass extends BroadcastReceiver {
	
	private static final String SENDER_ID = "1093719656719";

	private static final String LOG_TAG = BootClass.class.getSimpleName();
	

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(LOG_TAG, "Device starting, checking if service should be started...");
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		if(sharedPreferences.getBoolean("monitor", false))
		{
			GCMRegistrar.checkDevice(context);
			GCMRegistrar.checkManifest(context);
			final String regId = GCMRegistrar.getRegistrationId(context);
			if (regId.equals("")) {
				GCMRegistrar.register(context, SENDER_ID);
			} else {
				Log.v("GCM", "Already registered");
			}
			return;
		}
		GCMRegistrar.unregister(context);
	}

}