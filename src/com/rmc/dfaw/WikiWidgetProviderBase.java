package com.rmc.dfaw;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

public abstract class WikiWidgetProviderBase extends AppWidgetProvider {
	protected boolean backgroundEnabled;
	protected RemoteViews views;

	public void onDisabled(Context context) {
		ComponentName thisWidget = new ComponentName(context,
				WikiWidgetProviderBase.class);
		int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(thisWidget);
		System.out.println("ONDISABLED");
		for (int i = 0; i < ids.length; i++) {
			// check the sharedprefs for the id and remove it
			SharedPreferences settings = context.getSharedPreferences(
					WikiWidgetActivity.SHARED_PREF_NAME, Context.MODE_PRIVATE);
			SharedPreferences.Editor settingsEditor = settings.edit();
			if (settings.contains(Integer.toString(ids[i]))) {
				System.out.println("CONTAINS " + Integer.toString(ids[i]));
				settingsEditor.remove(Integer.toString(ids[i]));
			} else {
				System.out.println("NO CONTAINED");
			}
		}
	}

	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		int numberOfWidgets = appWidgetIds.length;

		// update for each widget that belongs to provider
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
