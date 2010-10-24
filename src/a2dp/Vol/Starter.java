package a2dp.Vol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class Starter extends BroadcastReceiver{
 	SharedPreferences preferences;   
 	private MyApplication application;
 	public static final String PREFS_NAME = "a2dp.Vol_preferences";
 	
	@Override
	public void onReceive(Context context, Intent arg1) {
		
		//this.application = (MyApplication) this.;
		///data/data/a2dp.Vol/shared_prefs/a2dp.Vol_preferences.xml
		preferences = context.getSharedPreferences(PREFS_NAME,0);
		//preferences = PreferenceManager.getDefaultSharedPreferences(this);
		if(preferences.getBoolean("bootstart", false) ){
				context.startService(new Intent(context, service.class));
		}

	}
	


}
