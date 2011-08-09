package a2dp.Vol;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
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
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
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
	public static Integer connects = 0;
	public static boolean run = false;

	public static btDevice[] btdConn = new btDevice[5]; // n the devices in the
														// database that has
	// connected
	private DeviceDB DB; // database of device data stored in SQlite

	private boolean carMode = true;
	private boolean homeDock = false;
	private boolean toasts = true;
	private boolean notify = false;
	private Notification not = null;
	private NotificationManager mNotificationManager = null;

	boolean oldwifistate = true;
	WifiManager wifiManager;
	String a2dpDir = "";
	boolean local;
	private static final String LOG_TAG = "A2DP_Volume";
	private static final String MY_UUID_STRING = "af87c0d0-faac-11de-a839-0800200c9a66";
	private PackageManager mPackageManager;
	public static final String PREFS_NAME = "btVol";
	float MAX_ACC = 20; // worst acceptable location in meters
	long MAX_TIME = 10000; // gps listener timout time in milliseconds and
	// oldest acceptable time
	SharedPreferences preferences;
	private MyApplication application;
	private Intent recievedIntent = null;
	private boolean connecting = false;
	private boolean disconnecting = false;
	private int connectedIcon;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {

		this.application = (MyApplication) this.getApplication();

		// get and load preferences

		try {
			preferences = PreferenceManager
					.getDefaultSharedPreferences(application);

			carMode = preferences.getBoolean("car_mode", true);
			homeDock = preferences.getBoolean("home_dock", false);
			toasts = preferences.getBoolean("toasts", true);
			notify = preferences.getBoolean("notify1", false);
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
			
			String icon = preferences.getString("connectedIcon", "Car");
			if(icon.equalsIgnoreCase("Headset"))connectedIcon = R.drawable.headset;
			else connectedIcon = R.drawable.car2;
			
		} catch (NumberFormatException e) {
			MAX_ACC = 10;
			MAX_TIME = 15000;
			Toast.makeText(this, "prefs failed to load ", Toast.LENGTH_LONG)
					.show();
			e.printStackTrace();
			Log.e(LOG_TAG, "prefs failed to load " + e.getMessage());
		}

		registerRecievers();

		// capture original volume
		am2 = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		// open database instance
		this.DB = new DeviceDB(application);

		wifiManager = (WifiManager) getBaseContext().getSystemService(
				Context.WIFI_SERVICE);

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
		// set run flag to true. This is used for the GUI
		run = true;
		// if all the above works, let the user know it is started
		if (toasts)
			Toast.makeText(this, R.string.ServiceStarted, Toast.LENGTH_LONG)
					.show();
		// Tell the world we are running
		final String IRun = "a2dp.vol.service.RUNNING";
		Intent i = new Intent();
		i.setAction(IRun);
		application.sendBroadcast(i);

		mPackageManager = getPackageManager();
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

		IntentFilter btNotEnabled = new IntentFilter(
				android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED);
		this.registerReceiver(btOFFReciever, btNotEnabled);

		if (carMode) {
			// Create listener for when car mode disconnects
			IntentFilter filter3 = new IntentFilter(
					android.app.UiModeManager.ACTION_EXIT_CAR_MODE);
			this.registerReceiver(mReceiver2, filter3);

			// Create listener for when car mode connects
			IntentFilter filter4 = new IntentFilter(
					android.app.UiModeManager.ACTION_ENTER_CAR_MODE);
			this.registerReceiver(mReceiver, filter4);
		}

		if (homeDock) {
			// Create listener for when car mode disconnects
			IntentFilter filter5 = new IntentFilter(
					android.app.UiModeManager.ACTION_EXIT_DESK_MODE);
			this.registerReceiver(mReceiver2, filter5);

			// Create listener for when car mode connects
			IntentFilter filter6 = new IntentFilter(
					android.app.UiModeManager.ACTION_ENTER_DESK_MODE);
			this.registerReceiver(mReceiver, filter6);
		}

	}

	@Override
	public void onDestroy() {
		// let the GUI know we closed
		run = false;
		// in case the location listener is running, stop it
		stopService(new Intent(application, StoreLoc.class));
		// close the database
		this.unregisterReceiver(mReceiver);
		this.unregisterReceiver(mReceiver2);
		this.unregisterReceiver(btOFFReciever);
		DB.getDb().close();
		// Tell the world we are not running
		final String IStop = "a2dp.vol.service.STOPPED_RUNNING";
		Intent i = new Intent();
		i.setAction(IStop);
		this.application.sendBroadcast(i);
		
		// let the user know the service stopped
		if (toasts)
			Toast.makeText(this, R.string.ServiceStopped, Toast.LENGTH_LONG)
					.show();
		this.stopForeground(true);
	}

	public void onStart() {

		run = true;
		connecting = false;
		disconnecting = false;
		if (notify)
			updateNot(false, null);

	}

	// used to clear all the Bluetooth connections if the Bluetooth adapter has been turned OFF.
	private final BroadcastReceiver btOFFReciever = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			int state1 = android.bluetooth.BluetoothAdapter.STATE_OFF;
			int state2 = android.bluetooth.BluetoothAdapter.STATE_TURNING_OFF;
			BluetoothAdapter mBTA = BluetoothAdapter.getDefaultAdapter();
			String mac = "";
			if (mBTA.getState() == state1 || mBTA.getState() == state2) {

				for (int j = 0; j < btdConn.length; j++) {
					if (btdConn[j] != null)
						if (btdConn[j].getMac().length() > 2) {
							mac = btdConn[j].getMac();
							btdConn[j] = null;
						}
				}
				getConnects();
				if (mac != "") {
					if (notify)
						updateNot(false, null);
					setVolume(OldVol2, application);
					dowifi(oldwifistate);
				}
				String Ireload = "a2dp.Vol.main.RELOAD_LIST";
				Intent itent = new Intent();
				itent.setAction(Ireload);
				itent.putExtra("disconnect", mac);
				application.sendBroadcast(itent);
			}
		}
	};

	// a device has just connected. Do the on connect stuff
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
			String results = "";
			if (!connecting) {
				recievedIntent = intent;
				connecting = true;
				if (recievedIntent == null)
					return;

				BluetoothDevice bt = (BluetoothDevice) recievedIntent
						.getExtras().get(BluetoothDevice.EXTRA_DEVICE);

				btDevice bt2 = null;

				// first see if a bluetooth device connected
				if (bt != null) {
					try {
						String addres = bt.getAddress();
						bt2 = DB.getBTD(addres);
						results = bt2.toString();
					} catch (Exception e) {
						results = e.getMessage();
						bt2 = null;
						return;
					}
				} else
				// if not a bluetooth device, must be a special device
				{
					try {
						// Log.d(LOG_TAG, intent.toString());
						if (recievedIntent.getAction().equalsIgnoreCase(
								"android.app.action.ENTER_CAR_MODE")) {
							bt2 = DB.getBTD("1"); // get car mode data
						} else if (recievedIntent.getAction().equalsIgnoreCase(
								"android.app.action.ENTER_DESK_MODE")) {
							bt2 = DB.getBTD("2"); // get home dock data
						} else
							return;

					} catch (Exception e) {
						results = e.getMessage();
						bt2 = null;
						Log.e(LOG_TAG, "Error" + e.toString());
					}
				}
				if(bt2.getMac() == null)return;
				
				boolean done = false;
				int l = 0;
				for (int k = 0; k < btdConn.length; k++) {
					if (btdConn[k] != null)
						if (bt2.getMac().equalsIgnoreCase(btdConn[k].getMac())) {
							l = k;
							done = true;
						}
				}

				if (!done) {
					do {
						if (btdConn[l] == null) {
							btdConn[l] = bt2;
							done = true;
						}
						l++;
						if (l >= btdConn.length)
							done = true;
					} while (!done);
				}
				getConnects();
				if (connects <= 1)
					getOldvol();

				if (bt2.wifi) {
					try {
						oldwifistate = wifiManager.isWifiEnabled();
						dowifi(false);
					} catch (Exception e) {
						e.printStackTrace();
						results += " Unable to access wifi: " + e.toString();
						Log.e(LOG_TAG, "Error " + e.getMessage());
					}
				}

				if (bt2.bdevice != null)
					if (bt2.bdevice.length() == 17) {
						try {
							// connectBluetoothA2dp(bt2.bdevice);
							new ConnectBt().execute(bt2.getBdevice());
							Log.d(LOG_TAG, bt2.getBdevice());

						} catch (Exception e) {
							e.printStackTrace();
							results += "Unable to connect bluetooth: "
									+ e.toString();
							Log.e(LOG_TAG, "Error " + e.getMessage());
						}
					}

				if (bt2.isSetV())
					setVolume(bt2.getDefVol(), application);
				if (notify)
					updateNot(true, bt2.toString());
				if (toasts)
					Toast.makeText(application, bt2.toString(),
							Toast.LENGTH_LONG).show();

				// If we defined an app to auto-start then run it on connect
				if (bt2.hasIntent())
					runApp(bt2);

				String Ireload = "a2dp.Vol.main.RELOAD_LIST";
				Intent itent = new Intent();
				itent.setAction(Ireload);
				itent.putExtra("connect", bt2.getMac());
				application.sendBroadcast(itent);
				connecting = false;
			}
		}
	};

	// device disconnected
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
			btDevice bt2 = null;
			if (!disconnecting) {
				disconnecting = true;

				BluetoothDevice bt = (BluetoothDevice) intent2.getExtras().get(
						BluetoothDevice.EXTRA_DEVICE);

				if (bt != null) {
					try {
						String addres = bt.getAddress();
						bt2 = DB.getBTD(addres);
						if (bt2 == null)
							return;
					} catch (Exception e) {
						bt2 = null;
						Log.e(LOG_TAG, "Error" + e.toString());
						return;
					}
				} else
					try {
						// Log.d(LOG_TAG, intent3.toString());
						if (intent2.getAction().equalsIgnoreCase(
								"android.app.action.EXIT_CAR_MODE"))
							bt2 = DB.getBTD("1");
						else if (intent2.getAction().equalsIgnoreCase(
								"android.app.action.EXIT_DESK_MODE"))
							bt2 = DB.getBTD("2");
						else
							return;
					} catch (Exception e) {
						Log.e(LOG_TAG, e.toString());
					}

					if(bt2.getMac() == null)return;
					
				if (notify && (bt2.mac != null))
					updateNot(false, null);

				// if we opened a package for this device, close it now
				if (bt2.hasIntent() && bt2.getPname().length() > 3) {
					stopApp(bt2.getPname());
				}

				// start the location capture service
				if (bt2 != null && bt2.isGetLoc()) {
					Intent dolock = new Intent(a2dp.Vol.service.this,
							StoreLoc.class);
					dolock.putExtra("device", bt2.getMac());
					startService(dolock);
				}

				/*
				 * if(notify){ not.icon = R.drawable.icon5;
				 * mNotificationManager.notify(1, not); }
				 */
				if ((bt2 != null && bt2.isSetV()) || bt2 == null)
					setVolume(OldVol2, application);

				if (bt2.wifi) {
					dowifi(oldwifistate);
				}
				for (int k = 0; k < btdConn.length; k++)
					if (btdConn[k] != null)
						if (bt2.getMac().equalsIgnoreCase(btdConn[k].getMac()))
							btdConn[k] = null;

				getConnects();
			}
			final String Ireload = "a2dp.Vol.main.RELOAD_LIST";
			Intent itent = new Intent();
			itent.setAction(Ireload);
			itent.putExtra("disconnect", bt2.getMac());
			application.sendBroadcast(itent);
			disconnecting = false;
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

	private void updateNot(boolean connect, String car) {

		String temp = car;
		if (car != null)
			temp = getResources().getString(R.string.connectedTo) + " " + car;
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
			not.icon = connectedIcon;
		else
			not.icon = R.drawable.icon5;

		mNotificationManager.notify(1, not);
	}

	private boolean runApp(btDevice bt) {

		Intent i;
		String pname = bt.getPname();
		String cAction = bt.getAppaction();
		String cData = bt.getAppdata();
		String cType = bt.getApptype();

		if (pname == null || pname.equals("")) {
			return false;
		} else if (cData.length() > 1) {
			try {
				i = Intent.getIntent(cData);
			} catch (URISyntaxException e) {
				e.printStackTrace();
				return false;
			}
		} else if (!cAction.equals("")) {
			i = new Intent();
			i.setAction(cAction);
			if (!cData.equals("")) {
				i.setData(Uri.parse(cData));
			}
			if (!cType.equals("")) {
				i.setType(cType);
			}
		} else {
			try {
				i = mPackageManager.getLaunchIntentForPackage(pname);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		try {
			startActivity(i);
			return true;
		} catch (Exception e) {
			Toast t = Toast.makeText(getApplicationContext(),
					R.string.app_not_found, Toast.LENGTH_SHORT);
			if (notify)
				t.show();
			e.printStackTrace();
			return false;
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
				err.printStackTrace();
				Toast t = Toast.makeText(getApplicationContext(),
						R.string.app_not_found, Toast.LENGTH_SHORT);
				if (notify)
					t.show();
			}
		}
	}

	private class ConnectBt extends AsyncTask<String, Void, Boolean> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Boolean result) {

			super.onPostExecute(result);
		}

		BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();

		protected void onPreExecute() {
		}

		@Override
		protected Boolean doInBackground(String... arg0) {

			boolean try2 = true;
			BluetoothAdapter mBTA = BluetoothAdapter.getDefaultAdapter();
			if (mBTA == null || !mBTA.isEnabled())
				return false;

			Set<BluetoothDevice> pairedDevices = bta.getBondedDevices();
			BluetoothDevice device = null;
			for (BluetoothDevice dev : pairedDevices) {
				if (dev.getAddress().equalsIgnoreCase(arg0[0]))
					device = dev;
			}
			if (device == null)
				return false;

			IBluetoothA2dp ibta = getIBluetoothA2dp();
			try {
				Log.d(LOG_TAG, "Here: " + ibta.getSinkPriority(device));
				if (ibta != null && ibta.connectSink(device))
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

	/*
	 * private void connectBluetoothA2dp(String device) { new
	 * ConnectBt().execute(device); }
	 */

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

	private void getConnects() {
		if (true) {
			connects = 0;
			for (int i = 0; i < a2dp.Vol.service.btdConn.length; i++) {
				if (a2dp.Vol.service.btdConn[i] != null)
					connects++;
			}
		}
	}
}
