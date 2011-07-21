package a2dp.Vol;

import android.content.pm.PackageManager;
import android.widget.ImageView;

public class AppItem extends SimplePropertyCollection {
	public AppItem() {
		super(ALARM_DEFAULTS_ALL);
	}

	public static final String KEY_PACKAGE_NAME = "alarm_package_name";
	public static final String KEY_CUSTOM_ACTION = "alarm_custom_action";
	public static final String KEY_CUSTOM_DATA = "alarm_custom_data";
	public static final String KEY_CUSTOM_TYPE = "alarm_custom_type";
	public static final String KEY_FORCE_RESTART = "alarm_force_restart";

	
	public static final SimpleProperty[] ALARM_DEFAULTS_ALL = new SimpleProperty[] {

		new SimpleProperty(KEY_PACKAGE_NAME, ""),
		new SimpleProperty(KEY_CUSTOM_ACTION, ""),
		new SimpleProperty(KEY_CUSTOM_DATA, ""),
		new SimpleProperty(KEY_CUSTOM_TYPE, ""),
		new SimpleProperty(KEY_FORCE_RESTART, true),
	};
	
	public static final SimpleProperty[] ALARM_DEFAULTS_LIST = new SimpleProperty[] {
		new SimpleProperty(KEY_PACKAGE_NAME, SimpleProperty.TYPE_TEXT, 0),
		new SimpleProperty(KEY_CUSTOM_ACTION, SimpleProperty.TYPE_TEXT, 0),
		new SimpleProperty(KEY_CUSTOM_DATA, SimpleProperty.TYPE_TEXT, 0),
	};
		
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
			
}


