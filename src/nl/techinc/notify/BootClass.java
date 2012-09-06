package nl.techinc.notify;

import com.google.android.gcm.*;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class BootClass extends BroadcastReceiver {
	
	private static final String SENDER_ID = "1093719656719";
	

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		if(sharedPreferences.getBoolean("gcm_enabled", false))
		{
			final String regId = GCMRegistrar.getRegistrationId(context);
			if (regId.equals("")) {
				GCMRegistrar.register(context, SENDER_ID);
			} else {
				//Log.v("GCM", "Already registered");
			}
			return;
		}
		GCMRegistrar.unregister(context);
	}

}