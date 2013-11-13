package com.rmc.dfaw;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.widget.RemoteViews;

public abstract class WikiWidgetProviderBase extends AppWidgetProvider {
	protected boolean backgroundEnabled;
	protected RemoteViews views;

	public void onDeleted(Context context, int[] appWidgetIds) {
		SharedPreferences settings = context.getSharedPreferences(
				WikiWidgetActivity.SHARED_PREF_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor settingsEditor = settings.edit();

		for (int i = 0; i < appWidgetIds.length; i++) {
			// this should always exist.
			if (settings.contains(appWidgetIds[i]
					+ WikiWidgetActivity.WIDGET_TYPE_PREF
					+ WikiWidgetActivity.APP_EXTENSION)) {
				settingsEditor.remove(appWidgetIds[i]
						+ WikiWidgetActivity.NETWORK_TYPE_PREF
						+ WikiWidgetActivity.APP_EXTENSION);
			} else {
				Log.e("UpdateStory", "Unknown widget type not deleted");
			}
			if (settings.contains(appWidgetIds[i]
					+ WikiWidgetActivity.NETWORK_TYPE_PREF
					+ WikiWidgetActivity.APP_EXTENSION)) {
				settingsEditor.remove(appWidgetIds[i]
						+ WikiWidgetActivity.NETWORK_TYPE_PREF
						+ WikiWidgetActivity.APP_EXTENSION);
			} else {
				Log.e("UpdateStory",
						"Unknown network type for widget not deleted");
			}
		}
	}

	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		// Build the intent to call the service
		Intent intent = new Intent(context.getApplicationContext(),
				UpdateStoryService.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

		// Update the widgets via the intent service
		context.startService(intent);

	}

}
