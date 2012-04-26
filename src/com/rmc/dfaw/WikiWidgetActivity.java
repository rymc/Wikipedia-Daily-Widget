package com.rmc.dfaw;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

public class WikiWidgetActivity extends Activity {
	/** Called when the activity is first created. */
	public static String WIFI_MOBILE_KEY = "WIFI_MOBILE_KEY";
	public static String SHARED_PREF_NAME = "WikiWidgetSettings";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		//get checkbox
        CheckBox wifiOnlyCB = (CheckBox) findViewById(R.id.checkBox1);

		//check if setting exists already
		SharedPreferences settings = this.getSharedPreferences(
				SHARED_PREF_NAME, MODE_PRIVATE);
		//if already exists, set CB according to value
		if(settings.contains(WIFI_MOBILE_KEY)){
			wifiOnlyCB.setChecked(settings.getBoolean(WIFI_MOBILE_KEY, false));
		}else {
			//doesn't exist so set it according to the default state specified in xml
			Editor settingsEditor = getSPSettingsEditor();
			settingsEditor.putBoolean(WIFI_MOBILE_KEY, wifiOnlyCB.isChecked());
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
