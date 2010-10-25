package a2dp.Vol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;


public class Starter extends BroadcastReceiver{
 	SharedPreferences preferences;   
 	
 	public static final String PREFS_NAME = "a2dp.Vol_preferences";
 	
	@Override
	public void onReceive(Context context, Intent arg1) {
		
		preferences = context.getSharedPreferences(PREFS_NAME,0);

		if(preferences.getBoolean("bootstart", false) ){
				context.startService(new Intent(context, service.class));
		}

	}
	


}
