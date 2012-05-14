package com.rmc.dfaw;

import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

public class WikiWidgetLauncherScreen extends Activity {
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.launcherscreen);
	}
	
	public void onRemove(View v){
	    ComponentName componentToDisable =
	    	      new ComponentName("com.rmc.dfaw",
	    	      "com.rmc.dfaw.WikiWidgetLauncherScreen");
	    	     
	    	    getPackageManager().setComponentEnabledSetting(
	    	      componentToDisable,
	    	      PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
	    	      PackageManager.DONT_KILL_APP);

	}

}
