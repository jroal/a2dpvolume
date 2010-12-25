package a2dp.Vol;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.widget.Toast;

public class service extends Service {

	/* (non-Javadoc)  
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		//return super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}

	static AudioManager am2 = (AudioManager) null;
	static Integer OldVol2 = 5;
	public static boolean run = false;
	LocationManager lm2 = null;
	public static BluetoothDevice btConn = null;
	static btDevice btdConn = null;
	private DeviceDB DB; // database of device data stored in SQlite
	private LocationManager locationManager;
	private Location location2;
	private Location location_old;
	private boolean carMode = true;
	private boolean gettingLoc = false;
	private boolean toasts = true;
	private boolean notify = false;
	private Notification not = null;
	private NotificationManager mNotificationManager = null;

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
		try {
			preferences = PreferenceManager
					.getDefaultSharedPreferences(this.application);

			carMode = preferences.getBoolean("car_mode", true);
			toasts = preferences.getBoolean("toasts", true);
			notify = preferences.getBoolean("notify1", false);
			
			str = preferences.getString("gpsTime", "15");
			Long yyy = new Long(preferences.getString("gpsTime", "15000"));
			MAX_TIME = yyy;
			
			Float xxx = new Float(preferences.getString("gpsDistance", "10"));
			MAX_ACC = xxx;

		} catch (NumberFormatException e) {
			MAX_ACC = 10;
			MAX_TIME = 15000;
			Toast.makeText(this, "prefs failed to load " + str, Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}

		// create intent filter for a bluetooth stream connection
		IntentFilter filter = new IntentFilter(
				android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED);
		this.registerReceiver(mReceiver, filter);

		// create intent filter for a bluetooth stream disconnection
		IntentFilter filter2 = new IntentFilter(
				android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECTED);
		this.registerReceiver(mReceiver2, filter2);

		if (carMode) {
			// Create listener to grab GPS if car mode disconnects
			IntentFilter filter3 = new IntentFilter(
					android.app.UiModeManager.ACTION_EXIT_CAR_MODE);
			this.registerReceiver(mReceiver3, filter3);
			
		}

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

		// Tell the world we are running
		final String IRun = "a2dp.vol.service.RUNNING";
		Intent i = new Intent();
		i.setAction(IRun);
		this.application.sendBroadcast(i);

		if (notify) {
			// set up the notification and start foreground
			String ns = Context.NOTIFICATION_SERVICE;
			mNotificationManager = (NotificationManager) getSystemService(ns);
			not = new Notification(R.drawable.icon5, "A2DP", System
					.currentTimeMillis());
			Context context = getApplicationContext();
			CharSequence contentTitle = getResources().getString(R.string.app_name);
			CharSequence contentText = getResources().getString(R.string.ServRunning);
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
/*		FileOutputStream fos;
		try {
			fos = openFileOutput("My_Last_Location",
					Context.MODE_WORLD_READABLE);
			String temp = "http://maps.google.com/maps?q=40.7423612,-89.63056078333334+(Lambo 11/26/10, 05:59:46 pm acc=8)";
			fos.write(temp.getBytes());
			fos.close();	
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		// end test file maker
	}

	@Override
	public void onDestroy() {
		// let the GUI know we closed
		run = false;
		// in case the location listener is running, stop it
		clearLoc();
		// Tell the world we are running
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
				if(notify)updateNot(true,bt2.toString());
			} catch (Exception e) {
				if (toasts)
					Toast.makeText(context, btConn.getAddress() + "\n"
							+ e.getMessage(), Toast.LENGTH_LONG);
				bt2 = null;
			}

			if (bt2 != null) {
				maxvol = bt2.getDefVol();
				setvol = bt2.isSetV();
			}

			if (setvol)
				setVolume(maxvol, a2dp.Vol.service.this);
		}
	};

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

			setVolume(OldVol2, a2dp.Vol.service.this);
			BluetoothDevice bt = (BluetoothDevice) intent2.getExtras().get(
					BluetoothDevice.EXTRA_DEVICE);
			btConn = bt;

			btDevice bt2 = null;
			try {
				String addres = btConn.getAddress();
				bt2 = DB.getBTD(addres);
			} catch (Exception e) {
				if (toasts)
					Toast.makeText(context2, btConn.getAddress() + "\n"
							+ e.getMessage(), Toast.LENGTH_LONG);
				bt2 = null;
			}

			if(notify)updateNot(false,null);
			if (bt2 != null && bt2.isGetLoc()) {
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
							clearLoc();
						}
					}.start();
					// clear any previously running instances of the location
					// listener
					clearLoc();
					// start location provider GPS
					// Register the listener with the Location Manager to
					// receive location updates

					if (locationManager
							.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
						locationManager.requestLocationUpdates(
								LocationManager.GPS_PROVIDER, 0, 0,
								locationListener);
					}
					if (locationManager
							.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
						locationManager.requestLocationUpdates(
								LocationManager.NETWORK_PROVIDER, 0, 0,
								locationListener);
					}
				}
				// get best location and store it
				grabGPS();
			}
			else
				if(!gettingLoc)btConn = null;
			
/*			if(notify){
				not.icon = R.drawable.icon5;
				mNotificationManager.notify(1, not);
			}*/
		}
	};

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
			// make sure we turn OFF the location listener if we don't get a
			// loc in MAX_TIME
			if (MAX_TIME > 0 && !gettingLoc) {
				new CountDownTimer(MAX_TIME, 5000) {

					public void onTick(long millisUntilFinished) {
						if (toasts)
							Toast.makeText(a2dp.Vol.service.this,
									"Time left: " + millisUntilFinished / 1000,
									Toast.LENGTH_LONG).show();
					}

					public void onFinish() {
						clearLoc();
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
				// get best location and store it
				grabGPS();

				if(notify)updateNot(false,null);	
			}
		}
	};

	// makes the volume adjustment
	public static int setVolume(int inputVol, Context sender) {
		int outVol;
		if (inputVol < 0)
			inputVol = 0;
		if (inputVol > am2.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
			inputVol = am2.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		am2.setStreamVolume(AudioManager.STREAM_MUSIC, inputVol, 0);
		outVol = am2.getStreamVolume(AudioManager.STREAM_MUSIC);

		Toast.makeText(sender,
				"Stored Volume:" + OldVol2 + "  New Volume:" + outVol,
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
	private double[] getGPS2() {

		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = lm.getProviders(true);

		Location l = null;
		Location l2 = null;
		Location l3 = null;

		long deltat = 9999999;
		long olddt = 9999999;
		float oldacc = 999999;

		if (!providers.isEmpty()) {
			for (int i = providers.size() - 1; i >= 0; i--) {
				l2 = lm.getLastKnownLocation(providers.get(i));

				if (l2 != null) {
					if (l2.hasAccuracy()) // if we have accuracy, capture the
					// best
					{
						if (l2.getAccuracy() < oldacc) {
							l3 = l2;
							oldacc = l2.getAccuracy();
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
			return null;

		double[] gps = new double[8];

		if (l != null) {
			gps[0] = l.getLatitude();
			gps[1] = l.getLongitude();
			gps[2] = l.getAccuracy();
			gps[3] = l.getTime();
			location_old = l;
		} else
			return null;

		if (l3 != null) {
			gps[4] = l3.getLatitude();
			gps[5] = l3.getLongitude();
			gps[6] = l3.getAccuracy();
			gps[7] = l3.getTime();
		} else
			return null;

		// If we have a good location, turn OFF the gps listener.
		if (locationListener != null && l != null && location2 != null) {
			float x = location2.getAccuracy();
			if (x < MAX_ACC
					&& x > 0
					&& (System.currentTimeMillis() - location2.getTime()) < MAX_TIME)
				clearLoc();
		}

		return gps;
	}

	// get the location and write it to a file.
	void grabGPS() {
		double[] gloc;
		String car = "My Car";
		try {
			gloc = getGPS2();
		} catch (Exception e1) {
			return;
		}

		DecimalFormat df = new DecimalFormat("#.#");

		if (gloc != null) {
			if (btdConn != null) {
				car = btdConn.getDesc2();
			}
			try {
				FileOutputStream fos = openFileOutput("My_Last_Location",
						Context.MODE_WORLD_READABLE);

				Time t = new Time();
				t.set((long) gloc[3]);
				String temp = "http://maps.google.com/maps?q=" + gloc[0] + ","
						+ gloc[1] + "+" + "(" + car + " " + t.format("%D, %r")
						+ " acc=" + df.format(gloc[2]) + ")";
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

			try {
				FileOutputStream fos = openFileOutput("My_Last_Location2",
						Context.MODE_WORLD_READABLE);
				Time t = new Time();
				t.set((long) gloc[7]);
				String temp = "http://maps.google.com/maps?q=" + gloc[4] + ","
						+ gloc[5] + "+" + "(" + car + " " + t.format("%D, %r")
						+ " acc=" + df.format(gloc[6]) + ")";
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
			// since we know this is a new location, just check the accuracy
			float acc = location.getAccuracy();
			float acc2 = acc;

			// if we have an old location then use it to compare with the new
			// one.
			if (location_old != null) {
				if (location_old.hasAccuracy())
					acc2 = location_old.getAccuracy();
			}

			if ((acc < MAX_ACC || acc < acc2) && acc != 0) {
				grabGPS();
			}

		}

		public void onProviderEnabled(String provider) {
		}

		public void onProviderDisabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}
	};

	// just kills the location listener
	private void clearLoc() {
		locationManager.removeUpdates(locationListener);
		btConn = null;
		gettingLoc = false;
		// Toast.makeText(a2dp.Vol.service.this, " Location Manager stopped",
		// Toast.LENGTH_LONG).show();
	}
	
	private void updateNot(boolean connect, String car){
		
		String temp = car;
		if(car != null)
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
		
		if(connect)
			not.icon = R.drawable.car2;
		else
			not.icon = R.drawable.icon5;
		
		mNotificationManager.notify(1, not);
	}

}
