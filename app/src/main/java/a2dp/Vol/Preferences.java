package a2dp.Vol;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * @author Jim Roal This is the preference activity. It loads and saves the
 *         preferences
 */
public class Preferences extends PreferenceActivity {
	private MyApplication application;
	public static final String PREFS_NAME = "btVol";

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.preference.PreferenceActivity#onContentChanged()
	 */
	@Override
	public void onContentChanged() {
		// stop the service while changes are made
		stopService(new Intent(a2dp.Vol.Preferences.this, service.class));
		super.onContentChanged();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.preference.PreferenceActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();

		// Commit the edits!
		editor.commit();



		// restart the service
		startService(new Intent(a2dp.Vol.Preferences.this, service.class));
		// Tell the world we updated preferences
		this.application = (MyApplication) this.getApplication();
		final String IRun = "a2dp.vol.preferences.UPDATED";
		Intent i = new Intent();
		i.setAction(IRun);
		this.application.sendBroadcast(i);


		super.onDestroy();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
	}
}
