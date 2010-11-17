package a2dp.Vol;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * @author Jim Roal This is the preference activity. It loads and saves the
 *         preferences
 */
public class Preferences extends PreferenceActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
