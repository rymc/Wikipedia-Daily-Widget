package com.rmc.dfaw;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

public class WikiWidgetActivity extends Activity {
	protected static final String WIDGET_TYPE_KEY = "WIDGET_TYPE_KEY";
	public static String WIFI_MOBILE_KEY = "WIFI_MOBILE_KEY";
	public static String SHARED_PREF_NAME = "WikiWidgetSettings";
	public static long FEATURED_OPTION = 0;
	public static long TODAY_HISTORY_OPTION = 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

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

		final Spinner widgetTypeSpinner = (Spinner) findViewById(R.id.spinner1);
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
