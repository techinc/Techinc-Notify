package nl.techinc.notify;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class NotifyAppWidgetProvider extends AppWidgetProvider {

	public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		update(context, SpaceState.state);
		for(int appWidgetId : appWidgetIds)
		{
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.notify_appwidget);
			Intent intent = new Intent(context, NotifyActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
			views.setOnClickPendingIntent(R.id.stateWidget, pendingIntent);
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}
	
	public void onReceive(Context context, Intent intent)
	{
		super.onReceive(context, intent);
		if(!intent.hasExtra("state"))
			return;
		boolean state = intent.getBooleanExtra("state", false);
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.notify_appwidget);
		views.setImageViewResource(R.id.stateImage, state ? R.drawable.open : R.drawable.closed);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		ComponentName componentName = new ComponentName(context, NotifyAppWidgetProvider.class);
		appWidgetManager.updateAppWidget(componentName, views);
	}
	
	public static void update(Context context, boolean state)
	{
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.notify_appwidget);
		int resource;
		if(state)
			resource = R.drawable.open;
		else
			resource = R.drawable.closed;
		views.setImageViewResource(R.id.stateImage, resource);
	}
}
