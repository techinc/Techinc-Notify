package nl.techinc.notify;

import java.io.IOException;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

public class NotifyAppWidgetProvider extends AppWidgetProvider {
	
	public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		new Thread(new Runnable() {
			public void run()
			{
				try {
					SpaceState.updateState(context);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public static void update(Context context, boolean state)
	{
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.notify_appwidget);
		views.setImageViewResource(R.id.stateImage, state ? R.drawable.open : R.drawable.closed);
	}
}
