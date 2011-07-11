package a2dp.Vol;

import java.io.BufferedReader;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.widget.ImageView;

public class AppItem extends SimplePropertyCollection {
	public AppItem() {
		super(ALARM_DEFAULTS_ALL);
	}
	public AppItem(BufferedReader reader, boolean skipId) throws Exception {
		super(ALARM_DEFAULTS_ALL, reader, skipId, KEY_ROWID);
//		LiteHelper.setProFeatures(this);
	}
	public AppItem(SimpleProperty[] defaults, Cursor cur) {
		super(defaults, cur);
//		LiteHelper.setProFeatures(this);
	}
	public AppItem(Cursor cur) {
		super(ALARM_DEFAULTS_ALL, cur);
//		LiteHelper.setProFeatures(this);
	}


	public static final String KEY_ROWID = "_id";

	public static final String KEY_NET_TEST = "alarm_net_test";
	public static final String KEY_NET_TEST_URL = "alarm_net_test_url";

	public static final String KEY_PACKAGE_NAME = "alarm_package_name";
	public static final String KEY_CUSTOM_ACTION = "alarm_custom_action";
	public static final String KEY_CUSTOM_DATA = "alarm_custom_data";
	public static final String KEY_CUSTOM_TYPE = "alarm_custom_type";
	
	public static final String KEY_STOP_APP_ON_TIMEOUT = "alarm_stop_app";
	
	public static final String KEY_FORCE_RESTART = "alarm_force_restart";
	public static final String KEY_LABEL = "alarm_label";

	
	public static final SimpleProperty[] ALARM_DEFAULTS_ALL = new SimpleProperty[] {
		new SimpleProperty(KEY_ROWID, 0),

		new SimpleProperty(KEY_NET_TEST, false),
		new SimpleProperty(KEY_NET_TEST_URL, "http://google.com"),

		new SimpleProperty(KEY_PACKAGE_NAME, ""),
		new SimpleProperty(KEY_CUSTOM_ACTION, ""),
		new SimpleProperty(KEY_CUSTOM_DATA, ""),
		new SimpleProperty(KEY_CUSTOM_TYPE, ""),

		new SimpleProperty(KEY_STOP_APP_ON_TIMEOUT, false),

		new SimpleProperty(KEY_FORCE_RESTART, true),
		new SimpleProperty(KEY_LABEL, "")
	};
	
	public static final SimpleProperty[] ALARM_DEFAULTS_LIST = new SimpleProperty[] {
		new SimpleProperty(KEY_ROWID, SimpleProperty.TYPE_INT, 0),

		new SimpleProperty(KEY_PACKAGE_NAME, SimpleProperty.TYPE_TEXT, 0),
		new SimpleProperty(KEY_CUSTOM_ACTION, SimpleProperty.TYPE_TEXT, 0),
		new SimpleProperty(KEY_CUSTOM_DATA, SimpleProperty.TYPE_TEXT, 0),
		new SimpleProperty(KEY_LABEL, SimpleProperty.TYPE_TEXT, 0)
	};
		
	public boolean isNew() {
		return (getInt(KEY_ROWID) == 0);
	}
	
	public boolean isShortcutIntent() {
		return isShortcutIntent(getString(KEY_CUSTOM_DATA));
	}
	public static boolean isShortcutIntent(String data) {
		String lcase = data.toLowerCase();
		return lcase.startsWith("intent:") || lcase.contains("#intent");
	}
	public boolean isCustomIntent() {
		return getString(KEY_PACKAGE_NAME).equals("custom");
	}
	public boolean hasPackageName() {
		if (getString(KEY_PACKAGE_NAME) == null || getString(KEY_PACKAGE_NAME).equals("")) {
			return false;
		} else {
			return !isCustomIntent();
		}
	}
	
	public String getAppName(PackageManager pm) {
		if (getString(KEY_PACKAGE_NAME) == null || getString(KEY_PACKAGE_NAME).equals("")) {
			return "";
		}
		if (isShortcutIntent()) {
			return getString(KEY_CUSTOM_ACTION);
		}
		if (isCustomIntent()) {
			return "Custom";
		}
		
		String rtr = "";
		try {
			rtr = (String) pm.getApplicationInfo(getString(KEY_PACKAGE_NAME), 0).loadLabel(pm);
		} catch (Exception e) {
			e.printStackTrace();
			rtr = "ERROR LOADING APP NAME";
		}
		return rtr;
	} 
	
//	public boolean isAppPandoraStation() {
//		if (packageName != null) {
//			if (packageName.equals("custom") && customData != null) {
//				if (customData.startsWith(PandoraPicker.PANDORA_URI_STRING)) {
//					return true;
//				}
//			} else if (packageName.equals(PandoraPicker.PANDORA_PACKAGE_NAME)) {
//    			if (customData.startsWith("intent:")) {
//    				return true;
//    			}
//    		}
//		}
//		
//		return false;
//	}
	
	public void setAppIconInImageView(ImageView iv, PackageManager pm) {
		if (!hasPackageName()) {
			//iv.setImageResource(R.drawable.icon);
		} else {
    		try {
				iv.setImageDrawable(pm.getApplicationIcon(getString(KEY_PACKAGE_NAME)));
			} catch (Exception e) {
				e.printStackTrace();
				//iv.setImageResource(R.drawable.icon);
			}
		}
		
	}
		
	public static String getTimeoutText(int timeoutInSeconds) {
		int wD = timeoutInSeconds;
		String rtr = "";
		
		if (wD%3600 == 0) {
			wD /= 3600;
			rtr += wD + " hour";
		} else if (wD%60 == 0) {
			wD /= 60;
			rtr += wD + " minute";
		} else {
			rtr += wD + " second";
		}
		
		if (wD > 1) {
			rtr += "s";
		}
		
		return rtr;
	}
	

	public String getLabel(PackageManager pm) {
		String label = getString(KEY_LABEL);
		if (label == null || label.equals("")) {
			label = getAppName(pm);
		}
		return label.trim();
	}

}


