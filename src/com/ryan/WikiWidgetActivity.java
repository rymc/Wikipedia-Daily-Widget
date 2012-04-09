package com.ryan;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

public class WikiWidgetActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //default 
        Editor settingsEditor = getSPSettingsEditor();
        settingsEditor.putBoolean(Settings.BACKGROUND_KEY, false);
        settingsEditor.commit();
        
        

    }
    
    
    
    public void onCheckboxClicked(View v) {
    Editor settingsEditor = getSPSettingsEditor();
        // Perform action on clicks, depending on whether it's now checked
        if (((CheckBox) v).isChecked()) {
            settingsEditor.putBoolean(Settings.BACKGROUND_KEY, true);
        } else {
            settingsEditor.putBoolean(Settings.BACKGROUND_KEY, false);
        }
        settingsEditor.commit();
    }
    
    
    private Editor getSPSettingsEditor(){
     	SharedPreferences settings = this.getSharedPreferences(Settings.SHARED_PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor settingsEditor = settings.edit();
        return settingsEditor;

    }

     
}
