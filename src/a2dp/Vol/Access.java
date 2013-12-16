package a2dp.Vol;

import java.util.List;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class Access extends AccessibilityService {


	private static final int EVENT_NOTIFICATION_TIMEOUT_MILLIS = 1000;
	private static String[] packages;
	private String packagelist;
	private MyApplication application;
	SharedPreferences preferences;
	private long lastWhen = 0;

	public Access() {
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		this.application = (MyApplication) this.getApplication();
        preferences = PreferenceManager
				.getDefaultSharedPreferences(this.application);
        IntentFilter reloadmessage = new IntentFilter("a2dp.vol.Access.Reload");
		this.registerReceiver(reloadprefs, reloadmessage);
        LoadPrefs();
		super.onCreate();
	}

	private final BroadcastReceiver reloadprefs = new BroadcastReceiver(){

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			LoadPrefs();
		}
		
	};


	public void LoadPrefs(){
		packagelist = preferences.getString("packages", "com.google.android.talk,com.android.email,com.android.calendar");	
        packages = packagelist.split(",");
        setServiceInfo(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_SPOKEN);
	}
	
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
/*		 Toast.makeText(getApplicationContext(), "Heard event " +
		 event.getPackageName() + " time: " + event.getEventTime(), Toast.LENGTH_LONG).show();*/
		if (event.getEventTime() > lastWhen && a2dp.Vol.service.talk) {
			String str = "";
			String pack = (String) event.getPackageName();
			if (pack.equalsIgnoreCase("com.google.android.calendar")
					|| pack.equalsIgnoreCase("com.android.calendar") || pack.equalsIgnoreCase("com.motorola.calendar")) {
				str = "Calendar Entry: ";
			}
			if (pack.equalsIgnoreCase("com.google.android.gm")) {
				str = "Gmail: ";
			}
			if (pack.equalsIgnoreCase("com.skype.raider")) {
				str = "Skype: ";
			}
			if (pack.equalsIgnoreCase("com.google.android.keep")) {
				str = "Keep reminder: ";
			}
			if (pack.equalsIgnoreCase("com.google.android.talk")) {
				str = "Hangouts: ";
			}
			if(pack.equalsIgnoreCase("com.android.mms")){
				str = "Text message: ";
			}

			List<CharSequence> notificationList = event.getText();
			for (int i = 0; i < notificationList.size(); i++) {
				// Toast.makeText(this.getApplicationContext(),
				// notificationList.get(i), Toast.LENGTH_LONG).show();

				String s = notificationList.get(i).toString();
				if (s.length() > 1) {
					str += s.trim();
				}

			}

			// make sure something is connected so the text reader is active
			int connected = 0;
			try {
				connected = a2dp.Vol.service.connects ;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// read out the message by sending it to the service
			if (connected > 0 ) {
				final String IRun = "a2dp.vol.service.MESSAGE";
				Intent intent = new Intent();
				intent.setAction(IRun);
				intent.putExtra("message", str);
				this.sendBroadcast(intent);
			}
			lastWhen = event.getEventType();
		}
	}

	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub

	}

	private void setServiceInfo(int feedbackType) {
		AccessibilityServiceInfo info = new AccessibilityServiceInfo();
		// We are interested in all types of accessibility events.
		info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
		// We want to provide specific type of feedback.
		info.feedbackType = feedbackType;
		// We want to receive events in a certain interval.
		info.notificationTimeout = EVENT_NOTIFICATION_TIMEOUT_MILLIS;
		// We want to receive accessibility events only from certain packages.
		info.packageNames = packages;
		setServiceInfo(info);
	}

	@Override
	protected void onServiceConnected() {

		/*
		 * Toast.makeText(this.getApplicationContext(), " connected",
		 * Toast.LENGTH_LONG).show();
		 */
		setServiceInfo(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_SPOKEN);
		//lastWhen = System.currentTimeMillis();
		super.onServiceConnected();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		this.unregisterReceiver(reloadprefs);
		super.onDestroy();
	}
}
