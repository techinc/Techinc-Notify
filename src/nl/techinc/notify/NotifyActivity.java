package nl.techinc.notify;

import com.google.android.gcm.GCMRegistrar;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class NotifyActivity extends Activity {
	private static final String SENDER_ID = "1093719656719"; // Also in BootClass
	boolean monitorEnabled = false;
	private SharedPreferences sharedPreferences;
	
	private StateReceiver receiver;
	
	public class StateReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(SpaceState.ACTION_STATE))
			{
				final TextView statusLabel = (TextView) findViewById(R.id.status);
				if(intent.getBooleanExtra("error", false))
				{
					statusLabel.setText(R.string.unknown);
				}
				boolean state = intent.getBooleanExtra("state", false);
				statusLabel.setText(state ? R.string.open : R.string.closed);
			}
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		setMonitor();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		receiver = new StateReceiver();
		registerReceiver(receiver, new IntentFilter(SpaceState.ACTION_STATE));
		refresh();
	}
	
	@Override
	protected void onPause()
	{
		super.onStop();
		if(receiver != null)
			unregisterReceiver(receiver);
		receiver = null;
	}
	
	public void refresh()
	{
		final TextView statusLabel = (TextView) findViewById(R.id.status);
		statusLabel.setText(R.string.updating);
		final Context context = this;
		new Thread(new Runnable() {
			public void run()
			{
				SpaceState.updateState(context);
			}
		}).start();
		setMonitor();
	}
	
	public void setMonitor()
	{
		if(!sharedPreferences.getBoolean("gcm_supported", true))
			return;
		if(!sharedPreferences.getBoolean("gcm_enabled", true))
		{
			if(GCMRegistrar.isRegistered(this))
				GCMRegistrar.unregister(this);
			return;
		}
		TextView label = (TextView) findViewById(R.id.monitoring);
		try
		{
			GCMRegistrar.checkDevice(this);
			sharedPreferences.edit().putBoolean("gcm_supported", true).commit();
		}
		catch (UnsupportedOperationException e)
		{
			sharedPreferences.edit().putBoolean("gcm_supported", false).putBoolean("gcm_enabled", false).commit();
			label.setText(R.string.monitoring_unsupported);
		}
		final String regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) {
			GCMRegistrar.register(this, SENDER_ID);
		}
		label.setText(R.string.monitoring_enabled);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			refresh();
			return true;
		case R.id.menu_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.menu_quit:
			if(GCMRegistrar.isRegistered(this))
				GCMRegistrar.unregister(this);
			finish();
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
