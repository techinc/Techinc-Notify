package nl.techinc.notify;

import java.io.IOException;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
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
		for(int appWidgetId : appWidgetIds)
		{
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.notify_appwidget);
			Intent intent = new Intent(context, NotifyActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
			views.setOnClickPendingIntent(R.id.stateWidget, pendingIntent);
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}
	
	public static void update(Context context, boolean state)
	{
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.notify_appwidget);
		views.setImageViewResource(R.id.stateImage, state ? R.drawable.open : R.drawable.closed);
	}
}
