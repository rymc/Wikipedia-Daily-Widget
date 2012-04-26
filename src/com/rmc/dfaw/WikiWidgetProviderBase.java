package com.rmc.dfaw;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public abstract class WikiWidgetProviderBase extends AppWidgetProvider {
	protected boolean backgroundEnabled;
	protected RemoteViews views;

	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		int numberOfWidgets = appWidgetIds.length;

		//update for each widget that belongs to provider
		for (int i = 0; i < numberOfWidgets; i++) {

			// Build the intent to call the service
			Intent intent = new Intent(context.getApplicationContext(),
					UpdateStoryService.class);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

			// Update the widgets via the intent service
			context.startService(intent);

		}
	}

}
