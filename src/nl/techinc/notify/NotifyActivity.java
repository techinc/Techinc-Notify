package nl.techinc.notify;

import java.io.IOException;

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
import android.view.View;
import android.widget.Button;
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
				boolean state = intent.getBooleanExtra("state", false);
				statusLabel.setText(state ? R.string.open : R.string.closed);
			}
			else if(action.equals(GCMIntentService.ACTION_REGISTER))
			{
				boolean enabled = intent.getBooleanExtra("enabled", true);
				TextView label = (TextView) findViewById(R.id.monitoring);
				Button button = (Button) findViewById(R.id.toggle);
				button.setEnabled(true);
				label.setText(enabled ? R.string.monitoring_enabled : R.string.monitoring_disabled);
				button.setText(enabled ? R.string.disable : R.string.enable);
			}
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		enableMonitor();
		
		receiver = new StateReceiver();
		registerReceiver(receiver, new IntentFilter(SpaceState.ACTION_STATE));
		registerReceiver(receiver, new IntentFilter(GCMIntentService.ACTION_REGISTER));
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		try {
			refresh();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void refresh(View view) throws IOException
	{
		refresh();
	}
	
	public void refresh() throws IOException
	{
		final TextView statusLabel = (TextView) findViewById(R.id.status);
		statusLabel.setText(R.string.updating);
		final Context context = this;
		new Thread(new Runnable() {
			public void run()
			{
				try {
					SpaceState.updateState(context);
				} catch (IOException e) {
					e.printStackTrace();
					statusLabel.post(new Runnable(){
						public void run() {
							statusLabel.setText(R.string.unknown);
						}
					});
					return;
				}
			}
		}).start();
	}
	
	public void toggleMonitor(View view)
	{
		boolean oldState = sharedPreferences.getBoolean("monitor", true);
		sharedPreferences.edit().putBoolean("monitor", !oldState).commit();
		applyMonitor();
	}
	
	public void enableMonitor()
	{
		TextView label = (TextView) findViewById(R.id.monitoring);
		Button button = (Button) findViewById(R.id.toggle);
		button.setEnabled(false);
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		final String regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) {
			GCMRegistrar.register(this, SENDER_ID);
			label.setText(R.string.updating);
		} else {
			button.setEnabled(true);
			label.setText(R.string.monitoring_enabled);
			button.setText(R.string.disable);
		}
	}
	
	public void disableMonitor()
	{
		TextView label = (TextView) findViewById(R.id.monitoring);
		Button button = (Button) findViewById(R.id.toggle);
		button.setEnabled(false);
		if(!GCMRegistrar.isRegistered(this))
		{
			button.setEnabled(true);
			label.setText(R.string.monitoring_disabled);
			button.setText(R.string.enable);
			return;
		}
		GCMRegistrar.unregister(this);
		label.setText(R.string.updating);
	}
	
	public void applyMonitor()
	{
		Button button = (Button) findViewById(R.id.toggle);
		button.setEnabled(false);
		if(sharedPreferences.getBoolean("monitor", true))
		{
			enableMonitor();
		}
		else
		{
			disableMonitor();
		}
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
		case R.id.menu_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
