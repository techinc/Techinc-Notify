package nl.techinc.notify;

import com.google.android.gcm.GCMRegistrar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	
	private SharedPreferences sharedPreferences;
	
	private StateReceiver receiver;
	
	private NotifyApp application;
	
	public class StateReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(GCMIntentService.ACTION_REGISTER))
			{
				boolean enabled = intent.getBooleanExtra("enabled", true);
				if(sharedPreferences.getBoolean("monitor", enabled) != enabled)
					sharedPreferences.edit().putBoolean("monitor", enabled).commit();
				Preference monitorPref = findPreference("monitor");
				monitorPref.setSummary(R.string.monitor_summary);
				// monitorPref.setEnabled(true);
			}
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		addPreferencesFromResource(R.xml.preferences);
		receiver = new StateReceiver();
		registerReceiver(receiver, new IntentFilter(GCMIntentService.ACTION_REGISTER));
		application = (NotifyApp) getApplicationContext();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		findPreference("monitor").setEnabled(false);
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		if(application.isUpdating())
		{
			findPreference("monitor").setSummary(R.string.updating);
			return;
		}
		sharedPreferences.edit().putBoolean("monitor", GCMRegistrar.isRegistered(this)).commit();
		// findPreference("monitor").setEnabled(true);
		// TODO: Figure out how to fix this first
	}
	
	@Override
	protected void onPause()
	{
	    super.onPause();
	    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onStop()
	{
		super.onStop();
		if(receiver != null)
			unregisterReceiver(receiver);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals("monitor"))
		{
			Preference monitorPref = findPreference(key);
			monitorPref.setEnabled(false);
			monitorPref.setSummary(R.string.updating);
			boolean monitorEnabled = sharedPreferences.getBoolean("monitor", false);
			if(monitorEnabled)
			{
				GCMRegistrar.checkDevice(this);
				GCMRegistrar.checkManifest(this);
				final String regId = GCMRegistrar.getRegistrationId(this);
				if (regId.equals(""))
				{
					application.setUpdating(true);
					GCMRegistrar.register(this, GCMIntentService.SENDER_ID);
				}
				else
					monitorPref.setEnabled(true);
			}
			else
			{
				if(GCMRegistrar.isRegistered(this))
				{
					application.setUpdating(true);
					GCMRegistrar.unregister(this);
				}
				else
					monitorPref.setEnabled(true);
			}
		}
	}
}