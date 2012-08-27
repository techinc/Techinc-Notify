package nl.techinc.notify;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class NotifyActivity extends Activity {
	boolean monitorEnabled = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
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
		new Thread(new Runnable() {
			public void run()
			{
				final boolean state;
				try {
					state = SpaceState.updateState();
				} catch (IOException e) {
					e.printStackTrace();
					statusLabel.post(new Runnable(){
						public void run() {
							statusLabel.setText(R.string.unknown);
						}
					});
					return;
				}
				statusLabel.post(new Runnable(){
					public void run() {
						if(state)
							statusLabel.setText(R.string.open);
						else
							statusLabel.setText(R.string.closed);
					}
				});
			}
		}).start();
	}
	
	public void toggleMonitor(View view)
	{
		TextView label = (TextView) findViewById(R.id.monitoring);
		Button button = (Button) findViewById(R.id.toggle);
		if(!monitorEnabled)
		{
			monitorEnabled = true;
			label.setText(R.string.monitoring_enabled);
			button.setText(R.string.disable);
			return;
		}
		monitorEnabled = false;
		label.setText(R.string.monitoring_disabled);
		button.setText(R.string.enable);
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
			startActivity(new Intent(this, PreferenceSettings.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
