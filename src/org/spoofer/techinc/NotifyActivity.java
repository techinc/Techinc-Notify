package org.spoofer.techinc;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class NotifyActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}
	
	public void refresh(View view) throws IOException
	{
		new Thread(new Runnable() {
			public void run()
			{
				final boolean state;
				try {
					state = SpaceState.updateState();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				final TextView statusLabel = (TextView) findViewById(R.id.status);
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
