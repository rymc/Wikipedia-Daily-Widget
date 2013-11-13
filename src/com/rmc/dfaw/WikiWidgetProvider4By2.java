package com.rmc.dfaw;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;

public class WikiWidgetProvider4By2 extends WikiWidgetProviderBase {
	
	public void onDisabled(Context context) {
		System.out.println("DISABLED");
		ComponentName thisWidget = new ComponentName(
				context.getApplicationContext(), WikiWidgetProviderBase.class);

		// load in the sharefprefs to see the update settings.
		SharedPreferences settings = context.getSharedPreferences(
				WikiWidgetActivity.SHARED_PREF_NAME, context.MODE_PRIVATE);
		
		int [] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(thisWidget);
		System.out.println(ids.length +" ON DIABLED");

	}


	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

}
