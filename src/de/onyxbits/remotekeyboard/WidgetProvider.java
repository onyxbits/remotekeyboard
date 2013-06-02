package de.onyxbits.remotekeyboard;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {

	public WidgetProvider() {
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		RemoteViews view = new RemoteViews(getClass().getPackage().getName(),
				R.layout.widget);
		Intent intent = new Intent(context, WidgetActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
				0);

		view.setOnClickPendingIntent(R.id.widgeticon, pendingIntent);
		ComponentName componentName = new ComponentName(context.getPackageName(),
				WidgetProvider.class.getName());
		appWidgetManager.updateAppWidget(componentName, view);
	}

}
