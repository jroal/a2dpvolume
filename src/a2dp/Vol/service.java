package a2dp.Vol;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.IBluetoothA2dp;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

public class service extends Service {

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		// return super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}

	static AudioManager am2 = (AudioManager) null;
	Integer OldVol2 = 5;
	public static boolean run = false;
	LocationManager lm2 = null;
	public static BluetoothDevice btConn = null;
	static btDevice btdConn = null; // n the device in the database that has
									// connected
	private DeviceDB DB; // database of device data stored in SQlite
	private LocationManager locationManager;
	private Location location2;
	private Location location_old;
	private boolean carMode = true;
	private boolean homeDock = false;
	private boolean gettingLoc = false;
	private boolean toasts = true;
	private boolean notify = false;
	private boolean usePass = false;
	private boolean useNet = true;
	private Notification not = null;
	private NotificationManager mNotificationManager = null;
	Long dtime = null;
	Location l = null; // the most recent location
	Location l3 = null; // the most accurate location
	Location l4 = null; // the best location
	boolean oldwifistate = true;
	WifiManager wifiManager;
	String a2dpDir = "";
	boolean local;
	private static final String LOG_TAG = "A2DP_Volume";
	private static final String MY_UUID_STRING = "af87c0d0-faac-11de-a839-0800200c9a66";

	public static final String PREFS_NAME = "btVol";
	float MAX_ACC = 20; // worst acceptable location in meters
	long MAX_TIME = 10000; // gps listener timout time in milliseconds and
	// oldest acceptable time
	SharedPreferences preferences;
	private MyApplication application;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {

		this.application = (MyApplication) this.getApplication();

		// get and load preferences

		String str = "";
		dtime = System.currentTimeMillis();
		try {
			preferences = PreferenceManager
					.getDefaultSharedPreferences(this.application);

			carMode = preferences.getBoolean("car_mode", true);
			homeDock = preferences.getBoolean("home_dock", false);
			toasts = preferences.getBoolean("toasts", true);
			notify = preferences.getBoolean("notify1", false);
			usePass = preferences.getBoolean("usePassive", false);
			useNet = preferences.getBoolean("useNetwork", true);

			str = preferences.getString("gpsTime", "15");
			Long yyy = new Long(preferences.getString("gpsTime", "15000"));
			MAX_TIME = yyy;

			Float xxx = new Float(preferences.getString("gpsDistance", "10"));
			MAX_ACC = xxx;

			local = preferences.getBoolean("useLocalStorage", false);
			if (local)
				a2dpDir = getFilesDir().toString();
			else
				a2dpDir = Environment.getExternalStorageDirectory()
						+ "/A2DPVol";
		} catch (NumberFormatException e) {
			MAX_ACC = 10;
			MAX_TIME = 15000;
			Toast.makeText(this, "prefs failed to load " + str,
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}

		registerRecievers();

		// capture original volume
		am2 = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		// set run flag to true. This is used for the GUI
		run = true;
		// open database instance
		this.DB = new DeviceDB(this);
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		location2 = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		wifiManager = (WifiManager) getBaseContext().getSystemService(
				Context.WIFI_SERVICE);

		// Tell the world we are running
		final String IRun = "a2dp.vol.service.RUNNING";
		Intent i = new Intent();
		i.setAction(IRun);
		this.application.sendBroadcast(i);

		if (notify) {
			// set up the notification and start foreground
			String ns = Context.NOTIFICATION_SERVICE;
			mNotificationManager = (NotificationManager) getSystemService(ns);
			not = new Notification(R.drawable.icon5, "A2DP",
					System.currentTimeMillis());
			Context context = getApplicationContext();
			CharSequence contentTitle = getResources().getString(
					R.string.app_name);
			CharSequence contentText = getResources().getString(
					R.string.ServRunning);
			Intent notificationIntent = new Intent(this, main.class);
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
					notificationIntent, 0);
			not.setLatestEventInfo(context, contentTitle, contentText,
					contentIntent);

			mNotificationManager.notify(1, not);
			this.startForeground(1, not);
		}

		// if all the above works, let the user know it is started
		if (toasts)
			Toast.makeText(this, R.string.ServiceStarted, Toast.LENGTH_LONG)
					.show();

		// test location file maker
		/*
		 * FileOutputStream fos; try { fos = openFileOutput("My_Last_Location",
		 * Context.MODE_WORLD_READABLE); String temp =
		 * "http://maps.google.com/maps?q=40.7423612,-89.63056078333334+(Lambo 11/26/10, 05:59:46 pm acc=8)"
		 * ; fos.write(temp.getBytes()); fos.close(); } catch
		 * (FileNotFoundException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } catch (IOException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); }
		 */
		// end test file maker
	}

	private void registerRecievers() {
		// create intent filter for a bluetooth stream connection
		IntentFilter filter = new IntentFilter(
				android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED);
		this.registerReceiver(mReceiver, filter);

		// create intent filter for a bluetooth stream disconnection
		IntentFilter filter2 = new IntentFilter(
				android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECTED);
		this.registerReceiver(mReceiver2, filter2);

		if (carMode) {
			// Create listener for when car mode disconnects
			IntentFilter filter3 = new IntentFilter(
					android.app.UiModeManager.ACTION_EXIT_CAR_MODE);
			this.registerReceiver(mReceiver3, filter3);

			// Create listener for when car mode connects
			IntentFilter filter4 = new IntentFilter(
					android.app.UiModeManager.ACTION_ENTER_CAR_MODE);
			this.registerReceiver(mReceiver4, filter4);
		}

		if (homeDock) {
			// Create listener for when car mode disconnects
			IntentFilter filter5 = new IntentFilter(
					android.app.UiModeManager.ACTION_EXIT_DESK_MODE);
			this.registerReceiver(mReceiver3, filter5);

			// Create listener for when car mode connects
			IntentFilter filter6 = new IntentFilter(
					android.app.UiModeManager.ACTION_ENTER_DESK_MODE);
			this.registerReceiver(mReceiver4, filter6);
		}
	}

	@Override
	public void onDestroy() {
		// let the GUI know we closed
		run = false;
		// in case the location listener is running, stop it
		clearLoc(false);
		// Tell the world we are not running
		final String IStop = "a2dp.vol.service.STOPPED_RUNNING";
		Intent i = new Intent();
		i.setAction(IStop);
		this.application.sendBroadcast(i);
		this.stopForeground(true);
		// let the user know the service stopped
		if (toasts)
			Toast.makeText(this, R.string.ServiceStopped, Toast.LENGTH_LONG)
					.show();
	}

	public void onStart() {

		run = true;

	}

	// a bluetooth device has just connected. Do the on connect stuff
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.content.BroadcastReceiver#onReceive(android.content.Context,
		 * android.content.Intent)
		 */
		@Override
		public void onReceive(Context context, Intent intent) {
			int maxvol = am2.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

			boolean setvol = true;
			getOldvol();
			BluetoothDevice bt = (BluetoothDevice) intent.getExtras().get(
					BluetoothDevice.EXTRA_DEVICE);
			btConn = bt;

			btDevice bt2 = null;
			try {
				String addres = btConn.getAddress();
				bt2 = DB.getBTD(addres);
				btdConn = bt2;
				if (toasts)
					Toast.makeText(context, bt2.desc2, Toast.LENGTH_LONG)
							.show();
			} catch (Exception e) {
				if (toasts)
					Toast.makeText(context,
							btConn.getAddress() + "\n" + e.getMessage(),
							Toast.LENGTH_LONG);
				bt2 = null;
			}

			if (bt2.mac != null) {
				maxvol = bt2.getDefVol();
				setvol = bt2.isSetV();
				if (notify)
					updateNot(true, bt2.toString());
			}

			if (setvol)
				setVolume(maxvol, a2dp.Vol.service.this);

			// If we defined an app to auto-start then run it on connect
			if (bt2.pname != null)
				if (bt2.pname.length() > 3) {
					launchApp(bt2.pname);
				}

			if (bt2.wifi) {
				try {
					oldwifistate = wifiManager.isWifiEnabled();
					dowifi(false);
				} catch (Exception e) {
					Toast.makeText(application,
							"Unable to access wifi: " + e.toString(),
							Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
			}

			if (bt2.bdevice != null)
				if (bt2.bdevice.length() == 17) {
					try {

						connectBluetoothA2dp(bt2.bdevice);
						Log.d(LOG_TAG, bt2.bdevice);
					} catch (Exception e) {
						Toast.makeText(application,
								"Unable to connect bluetooth: " + e.toString(),
								Toast.LENGTH_LONG).show();
						Log.e(LOG_TAG, "Error " + e.getMessage());
					}
				}

		}
	};

	// disable wifi is requested
	private void dowifi(boolean s) {
		try {
			wifiManager.setWifiEnabled(s);
		} catch (Exception e) {
			Toast.makeText(application,
					"Unable to switch wifi: " + e.toString(), Toast.LENGTH_LONG)
					.show();
			e.printStackTrace();
		}
	}

	// a Car Dock or home dock has just connected. Do the on connect stuff
	private final BroadcastReceiver mReceiver4 = new BroadcastReceiver() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.content.BroadcastReceiver#onReceive(android.content.Context,
		 * android.content.Intent)
		 */
		@Override
		public void onReceive(Context context, Intent intent) {
			int maxvol = am2.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

			boolean setvol = true;
			getOldvol();

			btDevice bt2 = null;
			try {
				Log.d(LOG_TAG, intent.toString());
				if (intent.getAction().equalsIgnoreCase(
						"android.app.action.ENTER_CAR_MODE"))
					bt2 = DB.getBTD("1");
				else
					bt2 = DB.getBTD("2");

			} catch (Exception e) {
				if (toasts)
					Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG);
				bt2 = null;
				btdConn = null;
				Log.e(LOG_TAG, e.toString());
			}

			if (bt2.mac != null) {
				maxvol = bt2.getDefVol();
				setvol = bt2.isSetV();
				if (notify)
					updateNot(true, bt2.toString());
				btdConn = bt2;
				if (toasts)
					Toast.makeText(context, bt2.desc2, Toast.LENGTH_LONG)
							.show();
			} else
				return;

			if (setvol)
				setVolume(maxvol, a2dp.Vol.service.this);

			// If we defined an app to auto-start then run it on connect
			if (bt2.pname != null && bt2.pname != null)
				if (bt2.pname.length() > 3) {
					launchApp(bt2.pname);
				}

			if (bt2 != null && bt2.wifi) {
				try {
					oldwifistate = wifiManager.isWifiEnabled();
					dowifi(false);
				} catch (Exception e) {
					Toast.makeText(application,
							"Unable to access wifi: " + e.toString(),
							Toast.LENGTH_LONG).show();
					Log.e(LOG_TAG, e.toString());
				}
			}

			if (bt2.bdevice != null)
				// Toast.makeText(application, bt2.bdevice + " L:" +
				// bt2.bdevice.length(), Toast.LENGTH_LONG).show();
				if (bt2.bdevice.length() == 17) {
					try {
						
						connectBluetoothA2dp(bt2.bdevice);
					} catch (Exception e) {
						Toast.makeText(application,
								"Unable to connect bluetooth: " + e.toString(),
								Toast.LENGTH_LONG).show();
						Log.e(LOG_TAG, "Error " + e.getMessage());
					}
				}

		}
	};

	// bluetooth disconnected
	private final BroadcastReceiver mReceiver2 = new BroadcastReceiver() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.content.BroadcastReceiver#onReceive(android.content.Context,
		 * android.content.Intent) Triggered on bluetooth disconnect
		 */
		@Override
		public void onReceive(Context context2, Intent intent2) {
			BluetoothDevice bt = (BluetoothDevice) intent2.getExtras().get(
					BluetoothDevice.EXTRA_DEVICE);
			btConn = bt;

			btDevice bt2 = null;
			dtime = System.currentTimeMillis(); // catch the time we
												// disconnected
			location2 = null; // clear this so a new location is stored

			try {
				String addres = btConn.getAddress();
				bt2 = DB.getBTD(addres);
				btdConn = bt2;
			} catch (Exception e) {
				if (toasts)
					Toast.makeText(context2,
							btConn.getAddress() + "\n" + e.getMessage(),
							Toast.LENGTH_LONG);
				bt2 = null;
				Log.e(LOG_TAG, e.toString());
			}

			if (notify && (bt2.mac != null))
				updateNot(false, null);

			// if we opened a package for this device, close it now
			if (bt2 != null && bt2.pname.length() > 3) {
				stopApp(bt2.pname);
			}

			if (bt2 != null && bt2.isGetLoc() && !gettingLoc) {
				// make sure we turn OFF the location listener if we don't get a
				// loc in MAX_TIME
				gettingLoc = true;
				if (MAX_TIME > 0) {
					new CountDownTimer(MAX_TIME, 5000) {

						public void onTick(long millisUntilFinished) {
							if (toasts)
								Toast.makeText(
										a2dp.Vol.service.this,
										"Time left: " + millisUntilFinished
												/ 1000, Toast.LENGTH_LONG)
										.show();
						}

						public void onFinish() {
							clearLoc(true);
						}
					}.start();

					// start location provider GPS
					// Register the listener with the Location Manager to
					// receive location updates

					if (locationManager
							.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
						locationManager.requestLocationUpdates(
								LocationManager.GPS_PROVIDER, 0, 0,
								locationListener);
					}
					if (useNet
							&& locationManager
									.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
						locationManager.requestLocationUpdates(
								LocationManager.NETWORK_PROVIDER, 0, 0,
								locationListener);
					}
					if (usePass
							&& locationManager
									.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
						locationManager.requestLocationUpdates(
								LocationManager.PASSIVE_PROVIDER, 0, 0,
								locationListener);
					}

				}
				// get best location and store it
				grabGPS();
			} else if (!gettingLoc)
				btConn = null;

			/*
			 * if(notify){ not.icon = R.drawable.icon5;
			 * mNotificationManager.notify(1, not); }
			 */
			if ((bt2 != null && bt2.isSetV()) || bt2 == null)
				setVolume(OldVol2, a2dp.Vol.service.this);

			if (bt2.wifi) {
				dowifi(oldwifistate);
			}
		}
	};

	// car mode exit
	private final BroadcastReceiver mReceiver3 = new BroadcastReceiver() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.content.BroadcastReceiver#onReceive(android.content.Context,
		 * android.content.Intent) Triggered car mode exit
		 */
		@Override
		public void onReceive(Context context3, Intent intent3) {

			btDevice bt2 = null;
			try {
				// Log.d(LOG_TAG, intent3.toString());
				if (intent3.getAction().equalsIgnoreCase(
						"android.app.action.EXIT_CAR_MODE"))
					bt2 = DB.getBTD("1");
				else
					bt2 = DB.getBTD("2");
			} catch (Exception e) {
				Log.e(LOG_TAG, e.toString());
			}
			if (bt2.mac == null)
				return;
			else {
				btdConn = bt2;
				if (notify)
					updateNot(false, null);
			}
			dtime = System.currentTimeMillis(); // catch the time we
												// disconnected
			location2 = null; // clear this so a new location is stored

			// if we opened a package for this device, close it now
			if (bt2.pname.length() > 3) {
				stopApp(bt2.pname);
			}
			if (bt2 != null && bt2.isGetLoc() && !gettingLoc) {
				new CountDownTimer(MAX_TIME, 5000) {

					public void onTick(long millisUntilFinished) {
						if (toasts)
							Toast.makeText(a2dp.Vol.service.this,
									"Time left: " + millisUntilFinished / 1000,
									Toast.LENGTH_LONG).show();
					}

					public void onFinish() {
						clearLoc(true);
					}
				}.start();

				// start location provider GPS
				// Register the listener with the Location Manager to
				// receive location updates

				if (locationManager
						.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					locationManager.requestLocationUpdates(
							LocationManager.GPS_PROVIDER, 0, 0,
							locationListener);
				}
				if (useNet
						&& locationManager
								.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
					locationManager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER, 0, 0,
							locationListener);
				}
				if (usePass
						&& locationManager
								.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
					locationManager.requestLocationUpdates(
							LocationManager.PASSIVE_PROVIDER, 0, 0,
							locationListener);
				}
				// get best location and store it
				grabGPS();
			}
			if ((bt2 != null && bt2.isSetV()) || bt2 == null)
				setVolume(OldVol2, a2dp.Vol.service.this);

			if (bt2.wifi) {
				dowifi(oldwifistate);
			}

		}
	};

	// makes the volume adjustment
	public static int setVolume(int inputVol, Context sender) {
		int outVol, curvol;
		if (inputVol < 0)
			inputVol = 0;
		if (inputVol > am2.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
			inputVol = am2.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		curvol = am2.getStreamVolume(AudioManager.STREAM_MUSIC);
		am2.setStreamVolume(AudioManager.STREAM_MUSIC, inputVol, 0);
		outVol = am2.getStreamVolume(AudioManager.STREAM_MUSIC);

		Toast.makeText(sender,
				"Old Volume:" + curvol + "  New Volume:" + outVol,
				Toast.LENGTH_LONG).show();
		return outVol;
	}

	// captures the media volume so it can be later restored
	private void getOldvol() {
		if (OldVol2 < am2.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
			OldVol2 = am2.getStreamVolume(AudioManager.STREAM_MUSIC);
		} else {
			OldVol2 = am2.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		}
		try {
			main.OldVol = OldVol2;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// finds the most recent and most accurate locations
	// get the location and write it to a file.
	void grabGPS() {

		String car = "My Car";
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = lm.getProviders(true);

		long deltat = 9999999;
		long olddt = 9999999;
		float oldacc = 99999999;
		float bestacc = 99999999;
		Location l2 = null; // the temporary last known location
		if (l4 != null)
			if (l4.hasAccuracy())
				bestacc = l4.getAccuracy();

		try {

			if (!providers.isEmpty()) {
				for (int i = providers.size() - 1; i >= 0; i--) {
					l2 = lm.getLastKnownLocation(providers.get(i));

					if (l2 != null) {
						if (location_old != null)
							if (location_old.getTime() < (dtime - MAX_TIME))
								location_old = l2; // reset this if its too old

						if (l2.hasAccuracy()) // if we have accuracy, capture
												// the best
						{
							float acc = l2.getAccuracy();
							if (acc < oldacc) {
								l3 = l2; // the sample with the best accuracy
								oldacc = acc;
							}
							if ((acc < bestacc)
									&& (l2.getTime() > (dtime - MAX_TIME))) {
								l4 = l2; // the best sample since 15s before
											// disconnect
								bestacc = acc;
							}

						}
						olddt = deltat;
						deltat = System.currentTimeMillis() - l2.getTime();
						if (deltat < olddt) // get the most recent update
						{
							l = l2;
						}
					}
				}
			} else
				return; // if no location data just abort here

			if (l4 != null)
				location_old = l4;

			// If we have a good location, turn OFF the gps listener.
			if (locationListener != null && l4 != null && location2 != null) {
				float x = location2.getAccuracy();
				if (x < MAX_ACC
						&& x > 0
						&& (System.currentTimeMillis() - location2.getTime()) < MAX_TIME)
					clearLoc(true);
			}

		} catch (Exception e1) {
			return;
		}

		DecimalFormat df = new DecimalFormat("#.#");
		// figure out which device we are disconnecting from
		if (btdConn != null)
			car = btdConn.getDesc2();

		// store the best location
		if (l4 != null) {
			try {
				FileOutputStream fos = openFileOutput("My_Last_Location",
						Context.MODE_WORLD_READABLE);

				Time t = new Time();
				t.set((long) l4.getTime());
				String temp = "http://maps.google.com/maps?q="
						+ l4.getLatitude() + "," + l4.getLongitude() + "+"
						+ "(" + car + " " + t.format("%D, %r") + " acc="
						+ df.format(l4.getAccuracy()) + ")";
				fos.write(temp.getBytes());
				fos.close();
				// Toast.makeText(a2dp.Vol.service.this, temp,
				// Toast.LENGTH_LONG).show();
			} catch (FileNotFoundException e) {
				Toast.makeText(a2dp.Vol.service.this, "FileNotFound",
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
			} catch (IOException e) {
				Toast.makeText(a2dp.Vol.service.this, "IOException",
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
		}

		// store most accurate location
		if (l3 != null) {
			try {
				FileOutputStream fos = openFileOutput("My_Last_Location2",
						Context.MODE_WORLD_READABLE);
				Time t = new Time();
				t.set((long) l3.getTime());
				String temp = "http://maps.google.com/maps?q="
						+ l3.getLatitude() + "," + l3.getLongitude() + "+"
						+ "(" + car + " " + t.format("%D, %r") + " acc="
						+ df.format(l3.getAccuracy()) + ")";
				fos.write(temp.getBytes());
				fos.close();
				// Toast.makeText(a2dp.Vol.service.this, temp,
				// Toast.LENGTH_LONG).show();
			} catch (FileNotFoundException e) {
				Toast.makeText(a2dp.Vol.service.this, "FileNotFound",
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
			} catch (IOException e) {
				Toast.makeText(a2dp.Vol.service.this, "IOException",
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
		}
	}

	// Define a listener that responds to location updates
	LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			// Called when a new location is found by the gps location
			// provider.
			location2 = location;
			grabGPS();
		}

		public void onProviderEnabled(String provider) {
		}

		public void onProviderDisabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}
	};

	// kills the location listener and writes the location file
	private void clearLoc(boolean doGps) {
		locationManager.removeUpdates(locationListener);
		btConn = null;
		gettingLoc = false;
		String car = "My Car";
		DecimalFormat df = new DecimalFormat("#.#");
		// figure out which device we are disconnecting from
		if (btdConn != null)
			car = btdConn.getDesc2();

		// store this vehicles location
		if (doGps) {
			try {
				File exportDir = new File(a2dpDir);

				if (!exportDir.exists()) {
					exportDir.mkdirs();
				}
				File file = new File(exportDir, car.replaceAll(" ", "_")
						+ ".html");

				Time t = new Time();
				String temp = null;

				if (l4 != null) {
					t.set((long) l4.getTime());
					temp = "<hr /><bold><a href=\"http://maps.google.com/maps?q="
							+ l4.getLatitude()
							+ ","
							+ l4.getLongitude()
							+ "+"
							+ "("
							+ car
							+ " "
							+ t.format("%D, %r")
							+ " acc="
							+ df.format(l4.getAccuracy())
							+ ")\">"
							+ car
							+ "</a></bold> Best Location<br>Time: "
							+ t.format("%D, %r")
							+ "<br>"
							+ "Location type: "
							+ l4.getProvider()
							+ "<br>"
							+ "Accuracy: "
							+ l4.getAccuracy()
							+ " meters<br>"
							+ "Elevation: "
							+ l4.getAltitude()
							+ " meters<br>"
							+ "Lattitude: "
							+ l4.getLatitude()
							+ "<br>"
							+ "Longitude: "
							+ l4.getLongitude();
				} else {
					temp = "No Best Location Captured";
				}
				if (l3 != null) {
					t.set((long) l3.getTime());
					temp += "<hr /><bold><a href=\"http://maps.google.com/maps?q="
							+ l3.getLatitude()
							+ ","
							+ l3.getLongitude()
							+ "+"
							+ "("
							+ car
							+ " "
							+ t.format("%D, %r")
							+ " acc="
							+ df.format(l3.getAccuracy())
							+ ")\">"
							+ car
							+ "</a></bold> Most Accurate Location<br>Time: "
							+ t.format("%D, %r")
							+ "<br>"
							+ "Location type: "
							+ l3.getProvider()
							+ "<br>"
							+ "Accuracy: "
							+ l3.getAccuracy()
							+ " meters<br>"
							+ "Elevation: "
							+ l3.getAltitude()
							+ " meters<br>"
							+ "Lattitude: "
							+ l3.getLatitude()
							+ "<br>"
							+ "Longitude: "
							+ l3.getLongitude();
				}
				if (l != null) {
					t.set((long) l.getTime());
					temp += "<hr /><bold><a href=\"http://maps.google.com/maps?q="
							+ l.getLatitude()
							+ ","
							+ l.getLongitude()
							+ "+"
							+ "("
							+ car
							+ " "
							+ t.format("%D, %r")
							+ " acc="
							+ df.format(l.getAccuracy())
							+ ")\">"
							+ car
							+ "</a></bold> Most Recent Location<br>Time: "
							+ t.format("%D, %r")
							+ "<br>"
							+ "Location type: "
							+ l.getProvider()
							+ "<br>"
							+ "Accuracy: "
							+ l.getAccuracy()
							+ " meters<br>"
							+ "Elevation: "
							+ l.getAltitude()
							+ " meters<br>"
							+ "Lattitude: "
							+ l.getLatitude()
							+ "<br>"
							+ "Longitude: "
							+ l.getLongitude();
				}

				if (local) {
					FileOutputStream fos = openFileOutput(file.getName(),
							Context.MODE_WORLD_READABLE);
					fos.write(temp.getBytes());
					fos.close();
				} else {
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(temp.getBytes());
					fos.close();
				}

			} catch (FileNotFoundException e) {
				Toast.makeText(a2dp.Vol.service.this, "FileNotFound",
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
				Log.e(LOG_TAG, "Error " + e.getMessage());
			} catch (IOException e) {
				Toast.makeText(a2dp.Vol.service.this, "IOException",
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
				Log.e(LOG_TAG, "Error " + e.getMessage());
			}
		}
		// reset all the location variables
		l = null; // the most recent location
		l3 = null; // the most accurate location
		l4 = null; // the best location
		// Toast.makeText(a2dp.Vol.service.this, " Location Manager stopped",
		// Toast.LENGTH_LONG).show();
	}

	private void updateNot(boolean connect, String car) {

		String temp = car;
		if (car != null)
			temp = "Connected to " + car;
		else
			temp = getResources().getString(R.string.ServRunning);

		Context context = getApplicationContext();
		CharSequence contentTitle = getResources().getString(R.string.app_name);
		CharSequence contentText = temp;
		Intent notificationIntent = new Intent(this, main.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		not.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);

		if (connect)
			not.icon = R.drawable.car2;
		else
			not.icon = R.drawable.icon5;

		mNotificationManager.notify(1, not);
	}

	protected void launchApp(String packageName) {
		Intent mIntent = getPackageManager().getLaunchIntentForPackage(
				packageName);
		if (mIntent != null) {
			try {
				startActivity(mIntent);
			} catch (ActivityNotFoundException err) {
				Toast t = Toast.makeText(getApplicationContext(),
						R.string.app_not_found, Toast.LENGTH_SHORT);
				if (notify)
					t.show();
			}
		}
	}

	protected void stopApp(String packageName) {
		Intent mIntent = getPackageManager().getLaunchIntentForPackage(
				packageName);
		if (mIntent != null) {
			try {
				ActivityManager act1 = (ActivityManager) this
						.getSystemService(ACTIVITY_SERVICE);
				// act1.restartPackage(packageName);
				act1.killBackgroundProcesses(packageName);
				List<ActivityManager.RunningAppProcessInfo> processes;
				processes = act1.getRunningAppProcesses();
				for (ActivityManager.RunningAppProcessInfo info : processes) {
					for (int i = 0; i < info.pkgList.length; i++) {
						if (info.pkgList[i].matches(packageName)) {
							android.os.Process.killProcess(info.pid);
						}
					}
				}
			} catch (ActivityNotFoundException err) {
				Toast t = Toast.makeText(getApplicationContext(),
						R.string.app_not_found, Toast.LENGTH_SHORT);
				if (notify)
					t.show();
			}
		}
	}

	private class ConnectBt extends AsyncTask<String, Void, Boolean> {

		BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
		protected void onPreExecute() {}
		
		@Override
		protected Boolean doInBackground(String... arg0) {

			boolean try2 = true;
			
			Set<BluetoothDevice> pairedDevices = bta.getBondedDevices();
			BluetoothDevice device = null;
			for (BluetoothDevice dev : pairedDevices) {
				if(dev.getAddress().equalsIgnoreCase(arg0[0]))device = dev;
			}
			if(device == null)return false;
			
			IBluetoothA2dp ibta = getIBluetoothA2dp();
			try {
				Log.d(LOG_TAG, "Here: " + ibta.getSinkPriority(device));
				if (ibta.connectSink(device))
					Toast.makeText(application,
							"Connected 1: " + device.getName(),
							Toast.LENGTH_LONG).show();
				try2 = false;
			} catch (Exception e) {
				Log.e(LOG_TAG, "Error " + e.getMessage());
				try2 = true;
			}

			// if the above does not work, give below a try...
			if (try2) {
				// UUID for your application
				UUID MY_UUID = UUID.fromString(MY_UUID_STRING);
				// Get the adapter
				BluetoothAdapter btAdapter = BluetoothAdapter
						.getDefaultAdapter();
				// The socket
				BluetoothSocket socket = null;
				Log.d(LOG_TAG, "BT connect 1 failed, trying 2...");
				try {
					// Your app UUID string (is also used by the server)
					socket = device.createRfcommSocketToServiceRecord(MY_UUID);
				} catch (IOException e) {
					Log.e(LOG_TAG, "Error " + e.getMessage());
				}
				// For performance reasons
				btAdapter.cancelDiscovery();
				try {
					// Be aware that this is a blocking operation. You probably
					// want
					// to use this in a thread
					socket.connect();
					Toast.makeText(application,
							"Connected 2: " + device.getName(),
							Toast.LENGTH_LONG).show();
				} catch (IOException connectException) {
					// Unable to connect; close the socket and get out
					Log.e(LOG_TAG, "Error " + connectException.getMessage());
					try {
						socket.close();
					} catch (IOException closeException) {
						Log.e(LOG_TAG, "Error " + closeException.getMessage());
					}
					return false;
				}
			}

			// Now manage your connection (in a separate thread)
			// myConnectionManager(socket);

			return true;
		}

		private IBluetoothA2dp getIBluetoothA2dp() {

			IBluetoothA2dp ibta = null;

			try {

				Class<?> c2 = Class.forName("android.os.ServiceManager");

				Method m2 = c2.getDeclaredMethod("getService", String.class);
				IBinder b = (IBinder) m2.invoke(null, "bluetooth_a2dp");

				Log.d(LOG_TAG, "Test2: " + b.getInterfaceDescriptor());

				Class<?> c3 = Class.forName("android.bluetooth.IBluetoothA2dp");

				Class[] s2 = c3.getDeclaredClasses();

				Class<?> c = s2[0];
				// printMethods(c);
				Method m = c.getDeclaredMethod("asInterface", IBinder.class);

				m.setAccessible(true);
				ibta = (IBluetoothA2dp) m.invoke(null, b);

			} catch (Exception e) {
				Log.e(LOG_TAG, "Error " + e.getMessage());
			}
			return ibta;
		}
	}

	private void connectBluetoothA2dp(String device) {
		new ConnectBt().execute(device);
	}

}
