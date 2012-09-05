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
		NotifyApp application = (NotifyApp) context.getApplicationContext();
		update(context, application.getSpaceState());
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.notify_appwidget);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, NotifyActivity.class), 0);
		views.setOnClickPendingIntent(R.id.stateWidget, pendingIntent);
		for(int appWidgetId : appWidgetIds)
		{
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}
	
	public void onReceive(Context context, Intent intent)
	{
		super.onReceive(context, intent);
		if(!intent.hasExtra("state"))
			return;
		boolean state = intent.getBooleanExtra("state", false);
		update(context, state);
	}
	
	public static void update(Context context, boolean state)
	{
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.notify_appwidget);
		views.setImageViewResource(R.id.stateImage, state ? R.drawable.open : R.drawable.closed);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		ComponentName componentName = new ComponentName(context, NotifyAppWidgetProvider.class);
		appWidgetManager.updateAppWidget(componentName, views);
	}
}