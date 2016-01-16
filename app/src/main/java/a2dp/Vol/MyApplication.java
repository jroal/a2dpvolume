package a2dp.Vol;

import android.app.Application;
import android.util.Log;

/**
 * @author Jim Roal This is used to manage the application as a single
 *         application context
 */
public class MyApplication extends Application {

	public static final String APP_NAME = "A2DP Volume";

	private DeviceDB dataHelper;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(APP_NAME, "APPLICATION onCreate");
		try {
			this.dataHelper = new DeviceDB(this);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(APP_NAME, "APPLICATION onCreate failed to open DB");
			
		}
	}

	@Override
	public void onTerminate() {
		Log.d(APP_NAME, "APPLICATION onTerminate");
		super.onTerminate();
	}

	public DeviceDB getDeviceDB() {
		return this.dataHelper;
	}

	public void setDeviceDB(DeviceDB dataHelper) {
		this.dataHelper = dataHelper;
	}
}
