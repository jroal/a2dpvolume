package a2dp.Vol;

import java.util.ArrayList;
import java.util.List;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RemoteViews;
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

	private final BroadcastReceiver reloadprefs = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			LoadPrefs();
		}

	};

	public void LoadPrefs() {
		packagelist = preferences
				.getString("packages",
						"com.google.android.talk,com.android.email,com.android.calendar");
		packages = packagelist.split(",");
		//setServiceInfo(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_SPOKEN);
		setServiceInfo(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_GENERIC);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
/*		 Toast.makeText(getApplicationContext(), "Heard event " +
		 event.getPackageName() + " time: " + event.getEventTime(), Toast.LENGTH_LONG).show();*/
		ApplicationInfo appInfo;
		PackageManager pm = getPackageManager();
		if (event.getEventTime() > lastWhen && a2dp.Vol.service.talk) {

			String str = "";
			String pack = (String) event.getPackageName();
			try {
				appInfo = pm.getApplicationInfo(pack, 0);
			} catch (NameNotFoundException e1) {
				// TODO Auto-generated catch block
				appInfo = null;
			}
			String appName = (String) (appInfo != null ? pm
					.getApplicationLabel(appInfo) : pack);
/*			
			if (pack.equalsIgnoreCase("com.google.android.calendar")
					|| pack.equalsIgnoreCase("com.android.calendar")
					|| pack.equalsIgnoreCase("com.motorola.calendar")) {
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
			if (pack.equalsIgnoreCase("com.android.mms")) {
				str = "Text message: ";
			}*/

			if (android.os.Build.VERSION.SDK_INT >= 16) {
				String name = "";
				Boolean got_name = false;
				// We have to extract the information from the view
				Notification notification = (Notification) event
						.getParcelableData();
				if(notification == null)return;
				RemoteViews views = notification.bigContentView;
				if (views == null)
					views = notification.contentView;
				//if (views == null) return null;

				// Use reflection to examine the m_actions member of the given RemoteViews object.
				// It's not pretty, but it works.
				List<String> text = new ArrayList<String>();
				try {
					java.lang.reflect.Field field = views.getClass()
							.getDeclaredField("mActions");
					field.setAccessible(true);

					@SuppressWarnings("unchecked")
					ArrayList<Parcelable> actions = (ArrayList<Parcelable>) field
							.get(views);

					// Find the setText() and setTime() reflection actions
					for (Parcelable p : actions) {
						Parcel parcel = Parcel.obtain();
						p.writeToParcel(parcel, 0);
						parcel.setDataPosition(0);

						// The tag tells which type of action it is (2 is ReflectionAction, from the source)
						int tag = parcel.readInt();
						if (tag != 2)
							continue;

						// View ID
						parcel.readInt();

						String methodName = parcel.readString();
						if (methodName == null)
							continue;

						// Save strings
						else if (methodName.equals("setText")) {
							// Parameter type (10 = Character Sequence)
							parcel.readInt();

							// Store the actual string
							String t = TextUtils.CHAR_SEQUENCE_CREATOR
									.createFromParcel(parcel).toString().trim();
							text.add(t);
							if (!got_name) {
								if (t.equalsIgnoreCase(appName))
									name = appName + ", ";
								else
									name = appName + ", " + t;
								got_name = true;
							}
						}

						// Save times. Comment this section out if the notification time isn't important
						else if (methodName.equals("setTime")) {
							// Parameter type (5 = Long)
/*			                parcel.readInt();

			                String t = new SimpleDateFormat("h:mm a").format(new Date(parcel.readLong()));
			                text.add(t);*/
						}

						parcel.recycle();
					}
				}

				// It's not usually good style to do this, but then again, neither is the use of reflection...
				catch (Exception e) {
					//Log.e("NotificationClassifier", e.toString());
				}

				if (text.size() > 0) {
					// if the message has returns we only want the last string (Hangouts fix)
					String temp = text.get(text.size() - 1);
					if (temp.indexOf("\n") > -1)
						str += name + ", "
								+ temp.substring(temp.lastIndexOf("\n"));
					else
						str += name + ", " + temp;
				}
			} else {

				List<CharSequence> notificationList = event.getText();
				for (int i = 0; i < notificationList.size(); i++) {
					// Toast.makeText(this.getApplicationContext(),
					// notificationList.get(i), Toast.LENGTH_LONG).show();

					String s = notificationList.get(i).toString();
					if (s.length() > 1) {
						str += s.trim();
					}

				}
			}
			// make sure something is connected so the text reader is active
			int connected = 0;
			try {
				connected = a2dp.Vol.service.connects;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// read out the message by sending it to the service
			if (connected > 0 && str.length() > 0) {
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
		//setServiceInfo(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_SPOKEN);
		setServiceInfo(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_GENERIC);
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
