package com.rmc.dfaw;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RemoteViews;
import android.widget.Spinner;

public class WikiWidgetActivity extends Activity {
	protected static final String WIDGET_TYPE_KEY = "WIDGET_TYPE_KEY";
	public static String WIFI_MOBILE_KEY = "WIFI_MOBILE_KEY";
	public static String SHARED_PREF_NAME = "WikiWidgetSettings";
	public static long FEATURED_OPTION = 0;
	public static long TODAY_HISTORY_OPTION = 1;
	public static final String APP_EXTENSION = "WDW";
	public static final String WIDGET_TYPE_PREF = "WIDGET_TYPE";
	public static final String NETWORK_TYPE_PREF = "NETWORK_TYPE";
	private int widgetID;
	private Spinner widgetTypeSpinner;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setResult(RESULT_CANCELED);
		// get checkbox
		CheckBox wifiOnlyCB = (CheckBox) findViewById(R.id.checkBox1);

		// check if setting exists already
		SharedPreferences settings = this.getSharedPreferences(
				SHARED_PREF_NAME, MODE_PRIVATE);
		// if already exists, set CB according to value
		if (settings.contains(WIFI_MOBILE_KEY)) {
			wifiOnlyCB.setChecked(settings.getBoolean(WIFI_MOBILE_KEY, false));
		} else {
			// doesn't exist so set it according to the default state specified
			// in xml
			Editor settingsEditor = getSPSettingsEditor();
			settingsEditor.putBoolean(WIFI_MOBILE_KEY, wifiOnlyCB.isChecked());
			settingsEditor.commit();
		}

		widgetTypeSpinner = (Spinner) findViewById(R.id.spinner1);
		ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(this,
				R.array.widgetTypes, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		widgetTypeSpinner.setAdapter(adapter);

		widgetTypeSpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						Editor settingsEditor = getSPSettingsEditor();
						// updated setting
						settingsEditor.putLong(WIDGET_TYPE_KEY,
								widgetTypeSpinner.getSelectedItemId());
						settingsEditor.commit();
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub

					}
				});

		if (settings.contains(WIDGET_TYPE_KEY)) {
			widgetTypeSpinner.setSelection((int) settings.getLong(
					WIDGET_TYPE_KEY, 0));
		} else {
			// doesn't exist so set it according to the default state
			Editor settingsEditor = getSPSettingsEditor();
			settingsEditor.putLong(WIDGET_TYPE_KEY,
					widgetTypeSpinner.getSelectedItemId());
			settingsEditor.commit();
		}

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}

	}

	public void onSave(View v) {

		long spinnerId = widgetTypeSpinner.getSelectedItemId();
		SharedPreferences settings = this.getSharedPreferences(
				SHARED_PREF_NAME, MODE_PRIVATE);
		SharedPreferences.Editor settingsEditor = settings.edit();
		settingsEditor.putLong(widgetID + WikiWidgetActivity.WIDGET_TYPE_PREF
				+ WikiWidgetActivity.APP_EXTENSION, spinnerId);
		Log.v("WDW Configuration","onSave " + widgetID + spinnerId);
		boolean wifiOnly = settings.getBoolean(WIFI_MOBILE_KEY, false);
		settingsEditor.putBoolean(widgetID + NETWORK_TYPE_PREF + APP_EXTENSION,
				wifiOnly);

		settingsEditor.commit();

		// // Build the intent to call the service
		Intent intent = new Intent(this.getApplicationContext(),
				UpdateStoryService.class);
		int[] mAppWidgetArr = new int[1];
		mAppWidgetArr[0] = widgetID;
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, mAppWidgetArr);

		// Update the widgets via the intent service
		this.startService(intent);

		// Make sure we pass back the original appWidgetId
		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
		setResult(RESULT_OK, resultValue);
		finish();
	}

	public void removeIcon(View v) {
		ComponentName componentToDisable = new ComponentName("com.rmc.dfaw",
				"com.rmc.dfaw.WikiWidgetActivity");

		getPackageManager().setComponentEnabledSetting(componentToDisable,
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				PackageManager.DONT_KILL_APP);
	}

	public void onCheckboxClicked(View v) {
		Editor settingsEditor = getSPSettingsEditor();
		// updated setting
		settingsEditor
				.putBoolean(WIFI_MOBILE_KEY, (((CheckBox) v).isChecked()));
		settingsEditor.commit();
	}

	private Editor getSPSettingsEditor() {
		SharedPreferences settings = this.getSharedPreferences(
				SHARED_PREF_NAME, MODE_PRIVATE);
		SharedPreferences.Editor settingsEditor = settings.edit();
		return settingsEditor;
	}
}
