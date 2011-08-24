package a2dp.Vol;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.text.MessageFormat;
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
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsMessage;
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

	private TextToSpeech mTts;
	public static boolean mTtsReady = false;
	static AudioManager am2 = (AudioManager) null;
	private static Integer OldVol = 5;
	private static Integer OldVol2 = 5;
	public static Integer connects = 0;
	public static boolean run = false;

	public static btDevice[] btdConn = new btDevice[5]; // n the devices in the
														// database that has
	// connected
	private DeviceDB DB; // database of device data stored in SQlite

	private boolean carMode = true;
	private boolean homeDock = false;
	private boolean headsetPlug = false;
	private boolean enableTTS = false;
	HashMap<String, String> myHash;
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
	private static final String OLD_VOLUME = "old_vol";
	private static final String OLD_PH_VOL = "old_phone_vol";
	private PackageManager mPackageManager;
	public static final String PREFS_NAME = "btVol";
	float MAX_ACC = 20; // worst acceptable location in meters
	long MAX_TIME = 10000; // gps listener timout time in milliseconds and
	// oldest acceptable time
	SharedPreferences preferences;
	private MyApplication application;

	private volatile boolean connecting = false;
	private volatile boolean disconnecting = false;
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
			headsetPlug = preferences.getBoolean("headset", false);
			toasts = preferences.getBoolean("toasts", true);
			notify = preferences.getBoolean("notify1", false);
			enableTTS = preferences.getBoolean("enableTTS", false);
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
			if (icon.equalsIgnoreCase("Headset"))
				connectedIcon = R.drawable.headset;
			else
				connectedIcon = R.drawable.car2;

			OldVol2 = preferences.getInt(OLD_VOLUME, 10);
			OldVol = preferences.getInt(OLD_PH_VOL, 5);
		} catch (NumberFormatException e) {
			MAX_ACC = 10;
			MAX_TIME = 15000;
			Toast.makeText(this, "prefs failed to load ", Toast.LENGTH_LONG)
					.show();
			e.printStackTrace();
			Log.e(LOG_TAG, "prefs failed to load " + e.getMessage());
		}

		registerRecievers();

		// create audio manager instance
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
		if (enableTTS) {
			myHash = new HashMap<String, String>();
			myHash.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
					String.valueOf(AudioManager.STREAM_VOICE_CALL));
			myHash.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "A2DP_Vol");
			mTts = new TextToSpeech(application, listenerStarted);
		}
	}

	public TextToSpeech.OnInitListener listenerStarted = new TextToSpeech.OnInitListener() {

		public void onInit(int status) {
			if (status == TextToSpeech.SUCCESS) {
				mTtsReady = true;
				mTts.setOnUtteranceCompletedListener(utteranceDone);
			}
		}
	};
	public TextToSpeech.OnUtteranceCompletedListener utteranceDone = new TextToSpeech.OnUtteranceCompletedListener() {
		public void onUtteranceCompleted(String uttId) {
			if ("A2DP_Vol".equalsIgnoreCase(uttId)) {
				// unmute the stream
				if (am2.isBluetoothScoAvailableOffCall()) {
					am2.setSpeakerphoneOn(false);
					am2.stopBluetoothSco();
				}				
				/*am2.requestAudioFocus(changed, AudioManager.STREAM_MUSIC,
						AudioManager.AUDIOFOCUS_LOSS);*/
				am2.abandonAudioFocus(changed);
			}
		}
	};

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

		if (headsetPlug) {
			// create listener for headset plug
			IntentFilter filter7 = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
			this.registerReceiver(headSetReceiver, filter7);
		}
	}

	@Override
	public void onDestroy() {
		// let the GUI know we closed
		run = false;
		// in case the location listener is running, stop it
		stopService(new Intent(application, StoreLoc.class));
		// close the database
		try {
			this.unregisterReceiver(mReceiver);
			this.unregisterReceiver(mReceiver2);
			this.unregisterReceiver(btOFFReciever);
			if (headsetPlug)
				this.unregisterReceiver(headSetReceiver);
			// this.unregisterReceiver(SMScatcher);
			if (mTtsReady)
				mTts.shutdown();
			DB.getDb().close();
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	// used to clear all the Bluetooth connections if the Bluetooth adapter has
	// been turned OFF.
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

	private final BroadcastReceiver headSetReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			int state = intent.getIntExtra("state", -1);
			btDevice bt2 = null;
			try {
				bt2 = DB.getBTD("3"); // get headset plug data
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			if (bt2 != null && "3".equalsIgnoreCase(bt2.getMac())) {
				if (state == 0 && connects > 0) {
					disconnecting = true;
					DoDisconnected(bt2);
				} else if (state == 1) {
					connecting = true;
					DoConnected(bt2);
				}
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

			if (!connecting) {
				connecting = true;

				BluetoothDevice bt = (BluetoothDevice) intent.getExtras().get(
						BluetoothDevice.EXTRA_DEVICE);

				btDevice bt2 = null;

				// first see if a bluetooth device connected
				if (bt != null) {
					try {
						String addres = bt.getAddress();
						bt2 = DB.getBTD(addres);

					} catch (Exception e) {

						bt2 = null;
					}
				} else
				// if not a bluetooth device, must be a special device
				{
					try {
						// Log.d(LOG_TAG, intent.toString());
						if (intent.getAction().equalsIgnoreCase(
								"android.app.action.ENTER_CAR_MODE")) {
							bt2 = DB.getBTD("1"); // get car mode data
						} else if (intent.getAction().equalsIgnoreCase(
								"android.app.action.ENTER_DESK_MODE")) {
							bt2 = DB.getBTD("2"); // get home dock data
						} else
							bt2 = null;

					} catch (Exception e) {

						bt2 = null;
						Log.e(LOG_TAG, "Error" + e.toString());
					}
				}
				// if it is none of the devices in the database, exit here
				if (bt2 == null || bt2.getMac() == null) {
					connecting = false;
				} else
					DoConnected(bt2);

			}
		}
	};

	protected void DoConnected(btDevice bt2) {
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
		if (connects <= 1) {
			getOldvol();
			getOldPvol();
		}

		if (bt2.wifi) {
			try {
				oldwifistate = wifiManager.isWifiEnabled();
				dowifi(false);
			} catch (Exception e) {
				e.printStackTrace();
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

					Log.e(LOG_TAG, "Error " + e.getMessage());
				}
			}

		if (bt2.isSetV())
			setVolume(bt2.getDefVol(), application);
		if (bt2.isSetpv()) {
			setPVolume(bt2.getPhonev());
		}
		if (notify)
			updateNot(true, bt2.toString());
		if (toasts)
			Toast.makeText(application, bt2.toString(), Toast.LENGTH_LONG)
					.show();

		// If we defined an app to auto-start then run it on connect
		if (bt2.hasIntent())
			runApp(bt2);

		if (mTtsReady && bt2.isEnableTTS()) {
			this.registerReceiver(SMScatcher, new IntentFilter(
					"android.provider.Telephony.SMS_RECEIVED"));
			// am2.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
		}
		String Ireload = "a2dp.Vol.main.RELOAD_LIST";
		Intent itent = new Intent();
		itent.setAction(Ireload);
		itent.putExtra("connect", bt2.getMac());
		application.sendBroadcast(itent);
		connecting = false;
	}

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
					} catch (Exception e) {
						bt2 = null;
						Log.e(LOG_TAG, "Error" + e.toString());
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
							bt2 = null;
					} catch (Exception e) {
						bt2 = null;
						Log.e(LOG_TAG, e.toString());
					}
				// if it is none of the devices in the database, exit here
				if (bt2 == null || bt2.getMac() == null) {
					disconnecting = false;
				} else
					DoDisconnected(bt2);
			}
		}
	};

	protected void DoDisconnected(btDevice bt2) {
		if (notify && (bt2.mac != null))
			updateNot(false, null);

		// if we opened a package for this device, try to close it now
		if (bt2.hasIntent() && bt2.getPname().length() > 3) {
			stopApp(bt2.getPname());
		}

		// start the location capture service
		if (bt2 != null && bt2.isGetLoc()) {
			Intent dolock = new Intent(a2dp.Vol.service.this, StoreLoc.class);
			dolock.putExtra("device", bt2.getMac());
			startService(dolock);
		}

		/*
		 * if(notify){ not.icon = R.drawable.icon5;
		 * mNotificationManager.notify(1, not); }
		 */
		if ((bt2 != null && bt2.isSetV()) || bt2 == null)
			setVolume(OldVol2, application);
		if ((bt2 != null && bt2.isSetpv()) || bt2 == null)
			setPVolume(OldVol);

		if (bt2.wifi) {
			dowifi(oldwifistate);
		}
		for (int k = 0; k < btdConn.length; k++)
			if (btdConn[k] != null)
				if (bt2.getMac().equalsIgnoreCase(btdConn[k].getMac()))
					btdConn[k] = null;

		getConnects();

		if (mTtsReady && (bt2.isEnableTTS() || connects < 1)) {
			try {
				this.unregisterReceiver(SMScatcher);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// am2.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
		}
		final String Ireload = "a2dp.Vol.main.RELOAD_LIST";
		Intent itent = new Intent();
		itent.setAction(Ireload);
		itent.putExtra("disconnect", bt2.getMac());
		application.sendBroadcast(itent);
		disconnecting = false;

	}

	// makes the media volume adjustment
	public static int setVolume(int inputVol, Context sender) {
		int outVol;
		if (inputVol < 0)
			inputVol = 0;
		if (inputVol > am2.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
			inputVol = am2.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		am2.setStreamVolume(AudioManager.STREAM_MUSIC, inputVol,
				AudioManager.FLAG_SHOW_UI);
		outVol = am2.getStreamVolume(AudioManager.STREAM_MUSIC);
		return outVol;
	}

	// captures the media volume so it can be later restored
	private void getOldvol() {
		if (OldVol2 < am2.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
			OldVol2 = am2.getStreamVolume(AudioManager.STREAM_MUSIC);
		} else {
			OldVol2 = am2.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		}
		// Store the old volume in preferences so it can be extracted if another
		// instance starts or the service is killed and restarted
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(OLD_VOLUME, OldVol2);
		editor.commit();
	}

	// captures the phone volume so it can be later restored
	private void getOldPvol() {
		if (OldVol < am2.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)) {
			OldVol = am2.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
		} else {
			OldVol = am2.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
		}
		// Store the old volume in preferences so it can be extracted if another
		// instance starts or the service is killed and restarted
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(OLD_PH_VOL, OldVol);
		editor.commit();
	}

	// makes the phone volume adjustment
	public static int setPVolume(int inputVol) {
		int outVol;
		if (inputVol < 0)
			inputVol = 0;
		if (inputVol > am2.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL))
			inputVol = am2.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
		am2.setStreamVolume(AudioManager.STREAM_VOICE_CALL, inputVol,
				AudioManager.FLAG_SHOW_UI);
		outVol = am2.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
		return outVol;
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
		boolean restart = bt.isApprestart();

		if (restart && pname != null && pname.length() > 3) {
			try {
				ActivityManager act1 = (ActivityManager) this
						.getSystemService(ACTIVITY_SERVICE);
				// act1.restartPackage(pname);
				act1.killBackgroundProcesses(pname);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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

	private final BroadcastReceiver SMScatcher = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (intent.getAction().equals(
					"android.provider.Telephony.SMS_RECEIVED")) {
				// if(message starts with SMStretcher recognize BYTE)

				/*
				 * The SMS-Messages are 'hiding' within the extras of the
				 * Intent.
				 */
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					/* Get all messages contained in the Intent */
					Object[] pdusObj = (Object[]) bundle.get("pdus");
					SmsMessage[] messages = new SmsMessage[pdusObj.length];
					for (int i = 0; i < pdusObj.length; i++) {
						messages[i] = SmsMessage
								.createFromPdu((byte[]) pdusObj[i]);
					}
					/* Feed StringBuilder with all Messages found. */
					final StringBuilder sb = new StringBuilder();
					for (SmsMessage currentMessage : messages) {
						sb.append(
								MessageFormat
										.format(getString(R.string.msgTemplate),
												currentMessage
														.getDisplayOriginatingAddress(),
												currentMessage
														.getDisplayMessageBody()))
								.append(' ');
					}
					final String str = sb.toString().trim();
					// Toast.makeText(application, str,
					// Toast.LENGTH_LONG).show();
					if (mTtsReady) {
						// am2.setStreamMute(AudioManager.STREAM_NOTIFICATION,
						// true);
						new CountDownTimer(10000, 5000) {

							@Override
							public void onFinish() {
								try {
									mTts.speak(str, TextToSpeech.QUEUE_ADD,
											myHash);
								} catch (Exception e) {
									Toast.makeText(application,
											R.string.TTSNotReady,
											Toast.LENGTH_LONG).show();
									e.printStackTrace();
								}

							}

							@Override
							public void onTick(long arg0) {
								// am2.setStreamSolo(AudioManager.STREAM_VOICE_CALL,
								// true);
								/*am2.requestAudioFocus(changed,
										AudioManager.STREAM_MUSIC,
										AudioManager.AUDIOFOCUS_GAIN);*/
								if (am2.isBluetoothScoAvailableOffCall()) {
									am2.startBluetoothSco();
									am2.setSpeakerphoneOn(true);
								}

							}

						}.start();

					}
				}

			}
		}

	};

	AudioManager.OnAudioFocusChangeListener changed = new AudioManager.OnAudioFocusChangeListener() {

		public void onAudioFocusChange(int focusChange) {
			if (focusChange != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
				// could not get audio focus.
			}

		}
	};

}
