package a2dp.Vol;

import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.text.MessageFormat;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.UiModeManager;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.IBluetoothA2dp;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.PhoneLookup;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class service extends Service implements OnAudioFocusChangeListener {

    /*
     * (non-Javadoc)
     *
     * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return START_STICKY;
    }

    private TextToSpeech mTts;
    public static boolean mTtsReady = false;
    static AudioManager am2 = (AudioManager) null;
    private static Integer OldVol = 5;
    private static Integer OldVol2 = 5;
    private static Integer Oldsilent;
    public static Integer connects = 0;
    public static boolean run = false;
    public static boolean talk = false;
    private static boolean mvolsLeft = false;
    private static boolean pvolsLeft = false;
    private static String notify_pref = "always";
    public static btDevice[] btdConn = new btDevice[5]; // n the devices in the
    // database that has
    // connected
    private DeviceDB DB; // database of device data stored in SQlite
    static boolean mIsBound = false;
    static IBluetoothA2dp ibta2;
    static String DeviceToConnect = null;
    private boolean carMode = true;
    private boolean homeDock = false;
    private boolean headsetPlug = false;
    private boolean power = false;
    private boolean enableGTalk = false;
    private boolean enableSMS = false;
    private static boolean ramp_vol = false;
    HashMap<String, String> myHash;
    private boolean toasts = true;
    private boolean notify = true;
    private static boolean hideVolUi = false;
    private NotificationManager mNotificationManager = null;
    private boolean speakerPhoneWasOn = true;
    private boolean musicWasPlaying = false;
    private boolean bluetoothWasOff = false;
    private boolean clearedTts = true;
    private static final String FIX_STREAM = "fix_stream";

    boolean oldwifistate = true;
    boolean oldgpsstate = true;
    boolean tmessageRegistered = false; // tracks state of the message reader receiver
    WifiManager wifiManager;
    LocationManager locmanager;
    String a2dpDir = "";
    boolean local;
    private static final String A2DP_Vol = "A2DP_Vol";
    private static final String LOG_TAG = "A2DP_Volume";
    // private static final String MY_UUID_STRING =
    // "af87c0d0-faac-11de-a839-0800200c9a66";
    private static final String OLD_VOLUME = "old_vol";
    private static final String OLD_PH_VOL = "old_phone_vol";
    private static final int MUSIC_STREAM = 0;
    private static final int IN_CALL_STREAM = 1;
    private static final int ALARM_STREAM = 2;

    private PackageManager mPackageManager;
    public static final String PREFS_NAME = "btVol";
    private int MAX_MESSAGE_LENGTH = 350;
    float MAX_ACC = 10; // worst acceptable location in meters
    long MAX_TIME = 20000; // gps listener timout time in milliseconds and
    private long SMS_delay = 3000; // delay before reading SMS
    private int SMSstream = 0;
    private long vol_delay = 5000; // delay time between the device connection
    // and the volume adjustment

    SharedPreferences preferences;
    private static MyApplication application;

    private volatile boolean connecting = false;
    private volatile boolean disconnecting = false;
    private int connectedIcon;
    private TelephonyManager tm; // Context.getSystemService(Context.TELEPHONY_SERVICE);

    // permissions status flags
    Boolean permReadContacts = true;
    Boolean permLocation = true;
    Boolean permPhone = true;
    Boolean permSMS = true;
    Boolean permStorage = true;


    /*
     * private HandlerThread thread; private LinkedList<String> addresses;
     * private TalkObserver observer;
     */
    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {

        service.application = (MyApplication) this.getApplication();

        // get and load preferences

        try {
            preferences = PreferenceManager
                    .getDefaultSharedPreferences(application);

            carMode = preferences.getBoolean("car_mode", true);
            homeDock = preferences.getBoolean("home_dock", false);
            headsetPlug = preferences.getBoolean("headset", false);
            power = preferences.getBoolean("power", false);
            toasts = preferences.getBoolean("toasts", true);
            // notify = preferences.getBoolean("notify1", true);
            enableSMS = preferences.getBoolean("enableTTS", false);
            enableGTalk = preferences.getBoolean("enableGTalk", true);
            notify_pref = preferences.getString("notify_pref", "always");
            hideVolUi = preferences.getBoolean("hideVolUi", false);
            // Long yyy = new Long(preferences.getString("gpsTime", "15000"));
            MAX_TIME = Long.valueOf(preferences.getString("gpsTime", "15000"));

            // Float xxx = new Float(preferences.getString("gpsDistance",
            // "10"));
            MAX_ACC = Float.valueOf(preferences.getString("gpsDistance", "10"));

            local = preferences.getBoolean("useLocalStorage", false);
            if (local)
                a2dpDir = getFilesDir().toString();
            else
                a2dpDir = Environment.getExternalStorageDirectory()
                        + "/A2DPVol";

            OldVol2 = preferences.getInt(OLD_VOLUME, 10);
            OldVol = preferences.getInt(OLD_PH_VOL, 5);
            Oldsilent = preferences.getInt("oldsilent", 10);

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

        wifiManager = (WifiManager) getApplicationContext().getSystemService(
                Context.WIFI_SERVICE);

        locmanager = (LocationManager) getBaseContext().getSystemService(
                Context.LOCATION_SERVICE);
        if (notify_pref.equalsIgnoreCase("always")
                || notify_pref.equalsIgnoreCase("connected_only")) {
            notify = true;
        } else
            notify = false;

        if (notify) {
            // set up the notification and start foreground
            String ns = Context.NOTIFICATION_SERVICE;
            mNotificationManager = (NotificationManager) getSystemService(ns);

            Intent notificationIntent = new Intent(this, main.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            Notification not;
            not = new Notification.Builder(application)
                    .setContentTitle(
                            getResources().getString(R.string.app_name))
                    .setContentIntent(contentIntent)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentText(
                            getResources().getString(R.string.ServRunning))
                    .setPriority(Notification.PRIORITY_MIN).build();


            if (notify_pref.equalsIgnoreCase("always")) {
                mNotificationManager.notify(1, not);
                this.startForeground(1, not);
            }
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

        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

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
        /*
		 * if (enableTTS) { mTts = new TextToSpeech(application,
		 * listenerStarted); }
		 */

    }

    private void registerRecievers() {
        // create intent filter for a bluetooth stream connection
        IntentFilter filter = new IntentFilter(
                android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED);
        // this.registerReceiver(mReceiver, filter);

        // create intent filter for a bluetooth stream disconnection
        IntentFilter filter2 = new IntentFilter(
                android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECTED);

        IntentFilter btNotEnabled = new IntentFilter(
                android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(btOFFReciever, btNotEnabled);

        IntentFilter clearMessage = new IntentFilter("a2dp.vol.service.CLEAR");
        this.registerReceiver(messageClear, clearMessage);

        if (carMode) {
            // Create listener for when car mode disconnects
            filter2.addAction(android.app.UiModeManager.ACTION_EXIT_CAR_MODE);

            // Create listener for when car mode connects
            filter.addAction(android.app.UiModeManager.ACTION_ENTER_CAR_MODE);
        }

        if (homeDock) {
            // Create listener for when car mode disconnects
            filter2.addAction(android.app.UiModeManager.ACTION_EXIT_DESK_MODE);

            // Create listener for when car mode connects
            filter.addAction(android.app.UiModeManager.ACTION_ENTER_DESK_MODE);
        }

        if (power) {
            // Create listener for when power disconnects
            filter2.addAction(Intent.ACTION_POWER_DISCONNECTED);

            // Create listener for when power connects
            filter.addAction(Intent.ACTION_POWER_CONNECTED);
        }

        if (headsetPlug) {
            // create listener for headset plug
            IntentFilter filter7 = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
            this.registerReceiver(headSetReceiver, filter7);
        }
        this.registerReceiver(mReceiver, filter);
        this.registerReceiver(mReceiver2, filter2);

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
            if (mTtsReady) {
                try {
                    if (!clearedTts) {
                        clearTts();
                    }
                    mTts.shutdown();
                    mTtsReady = false;
                    unregisterReceiver(SMScatcher);
                    unregisterReceiver(sco_change);
                    unregisterReceiver(tmessage);
                    // if (enableGTalk)
                    // stopTalk();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            DB.getDb().close();
            this.unregisterReceiver(messageClear);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Tell the world we are not running
        final String IStop = "a2dp.vol.service.STOPPED_RUNNING";
        Intent i = new Intent();
        i.setAction(IStop);
        application.sendBroadcast(i);


        // let the user know the service stopped
        if (toasts)
            Toast.makeText(this, R.string.ServiceStopped, Toast.LENGTH_LONG)
                    .show();
        if (mIsBound) {
            try {
                //this.unbindService(mConnection);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        this.stopForeground(true);
    }

    public void onStart() {

        run = true;
        connecting = false;
        disconnecting = false;
        if (notify)
            updateNot(false, null);


    }

    private final BroadcastReceiver tmessage = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            TextReader(intent.getStringExtra("message"));

        }

    };

    private final BroadcastReceiver messageClear = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            clearTts();

        }

    };

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
                    if (!mvolsLeft)
                        setVolume(OldVol2, application);
                    if (!pvolsLeft)
                        setPVolume(OldVol);
                    dowifi(oldwifistate);
                }
                if (mTtsReady) {
                    try {
                        if (!clearedTts) {
                            clearTts();
                        }
                        mTts.shutdown();
                        mTtsReady = false;
                        unregisterReceiver(SMScatcher);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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

                BluetoothDevice bt;
                try {
                    bt = (BluetoothDevice) intent.getExtras().get(
                            BluetoothDevice.EXTRA_DEVICE);
                } catch (Exception e1) {
                    bt = null;
                    e1.printStackTrace();
                }

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
                        } else if (intent.getAction().equalsIgnoreCase(
                                Intent.ACTION_POWER_CONNECTED)) {
                            bt2 = DB.getBTD("4"); // get power data

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
            oldwifistate = wifiManager.isWifiEnabled();
            oldgpsstate = locmanager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
        }

        connectedIcon = bt2.getIcon();
        SMSstream = bt2.getSmsstream();
        vol_delay = bt2.getVoldelay() * 1000;
        SMS_delay = bt2.getSmsdelay() * 1000;
        ramp_vol = bt2.isVolramp();

        if (bt2.wifi) {
            try {
                oldwifistate = wifiManager.isWifiEnabled();
                dowifi(false);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error " + e.getMessage());
            }
        }

        // connect the selected BT device
        if (bt2.getBdevice() != null && bt2.getBdevice().length() == 17) {
            final btDevice tempBT = bt2;
            DeviceToConnect = bt2.getBdevice();
            getIBluetoothA2dp(application);
        }

        if (notify)
            updateNot(true, bt2.toString());
        if (toasts)
            Toast.makeText(application, bt2.toString(), Toast.LENGTH_LONG)
                    .show();

        // If we defined an app to auto-start then run it on connect if not in
        // call
        // tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (bt2.hasIntent()) {
            if (tm != null) {
                if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE)
                    runApp(bt2);
            } else
                runApp(bt2);
        }


        if (enableGTalk && bt2.isEnableTTS()) {
            mTts = new TextToSpeech(application, listenerStarted);
            IntentFilter messageFilter = new IntentFilter(
                    "a2dp.vol.service.MESSAGE");
            application.registerReceiver(tmessage, messageFilter);
            tmessageRegistered = true;
            IntentFilter sco_filter = new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
            this.registerReceiver(sco_change, sco_filter);
            talk = true;
        }
        if (bt2.isEnableTTS() && enableSMS) {
            application.registerReceiver(SMScatcher, new IntentFilter(
                    "android.provider.Telephony.SMS_RECEIVED"));
        }

        String Ireload = "a2dp.Vol.main.RELOAD_LIST";
        Intent itent = new Intent();
        itent.setAction(Ireload);
        itent.putExtra("connect", bt2.getMac());
        application.sendBroadcast(itent);
        connecting = false;

        application.sendBroadcast(new Intent("a2dp.Vol.Clear"));

        if (bt2.isSetpv()) {
            final int vol1 = bt2.getPhonev();
            new CountDownTimer(vol_delay + 500, vol_delay + 500) {

                @Override
                public void onFinish() {
                    setPVolume(vol1);
                }

                @Override
                public void onTick(long arg0) {
                    // TODO Auto-generated method stub

                }
            }.start();
        }

        if (bt2.isSilent())
            am2.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0);

        if (bt2.isSetV()) {
            final int vol = bt2.getDefVol();
            new CountDownTimer(vol_delay, vol_delay) {

                @Override
                public void onFinish() {
                    setVolume(vol, application);
                }

                @Override
                public void onTick(long arg0) {
                    // TODO Auto-generated method stub

                }
            }.start();
        }

        if (bt2.isCarmode()) {
            set_car_mode(true);
        }

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

                BluetoothDevice bt;
                try {
                    bt = (BluetoothDevice) intent2.getExtras().get(
                            BluetoothDevice.EXTRA_DEVICE);
                } catch (Exception e1) {
                    bt = null;
                    e1.printStackTrace();
                }

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
                        else if (intent2.getAction().equalsIgnoreCase(
                                Intent.ACTION_POWER_DISCONNECTED))
                            bt2 = DB.getBTD("4");
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

        int SavVol = am2.getStreamVolume(AudioManager.STREAM_MUSIC);

        if (bt2.hasIntent()) {
            // if music is playing, pause it
            if (am2.isMusicActive()) {
                // first pause the music so it removes the notify icon
                Intent i = new Intent("com.android.music.musicservicecommand");
                i.putExtra("command", "pause");
                sendBroadcast(i);
                // Try telling the system the headset just disconnected to stop
                // other players
                Intent j = new Intent("android.intent.action.HEADSET_PLUG");
                j.putExtra("state", 0);
                try {
                    sendBroadcast(j);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // for more stubborn players, try this too...
                Intent downIntent2 = new Intent(Intent.ACTION_MEDIA_BUTTON,
                        null);
                KeyEvent downEvent2 = new KeyEvent(KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_MEDIA_STOP);
                downIntent2.putExtra(Intent.EXTRA_KEY_EVENT, downEvent2);
                sendOrderedBroadcast(downIntent2, null);
            }

            // if we opened a package for this device, try to close it now
            if (bt2.getPname().length() > 3 && bt2.isAppkill()) {
                // also open the home screen to make music app revert to
                // background
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);
                // now we can kill the app is asked to

                final String kpackage = bt2.getPname();
                CountDownTimer killTimer = new CountDownTimer(3000, 3000) {
                    @Override
                    public void onFinish() {
                        if (am2.isMusicActive()) {
                            // first pause the music so it removes the notify
                            // icon
                            Intent i = new Intent(
                                    "com.android.music.musicservicecommand");
                            i.putExtra("command", "pause");
                            sendBroadcast(i);
                            // Try telling the system the headset just
                            // disconnected to stop other players
                            Intent j = new Intent(
                                    "android.intent.action.HEADSET_PLUG");
                            j.putExtra("state", 0);
                            try {
                                sendBroadcast(j);
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            // for more stubborn players, try this too...
                            Intent downIntent2 = new Intent(
                                    Intent.ACTION_MEDIA_BUTTON, null);
                            KeyEvent downEvent2 = new KeyEvent(
                                    KeyEvent.ACTION_DOWN,
                                    KeyEvent.KEYCODE_MEDIA_STOP);
                            downIntent2.putExtra(Intent.EXTRA_KEY_EVENT,
                                    downEvent2);
                            sendOrderedBroadcast(downIntent2, null);
                        }
                        try {
                            stopApp(kpackage);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(LOG_TAG, "Error " + e.getMessage());
                        }

                    }

                    @Override
                    public void onTick(long arg0) {

                        if (am2.isMusicActive()) {
                            // first pause the music so it removes the notify
                            // icon
                            Intent i = new Intent(
                                    "com.android.music.musicservicecommand");
                            i.putExtra("command", "pause");
                            sendBroadcast(i);

                            // for more stubborn players, try this too...
                            Intent downIntent2 = new Intent(
                                    Intent.ACTION_MEDIA_BUTTON, null);
                            KeyEvent downEvent2 = new KeyEvent(
                                    KeyEvent.ACTION_DOWN,
                                    KeyEvent.KEYCODE_MEDIA_STOP);
                            downIntent2.putExtra(Intent.EXTRA_KEY_EVENT,
                                    downEvent2);
                            sendOrderedBroadcast(downIntent2, null);
                        }

                        try {
                            stopApp(kpackage);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(LOG_TAG, "Error " + e.getMessage());
                        }
                    }
                };
                killTimer.start();

            }
        }

        // start the location capture service
        if (bt2 != null && bt2.isGetLoc()) {
            Intent dolock = new Intent(a2dp.Vol.service.this, StoreLoc.class);
            dolock.putExtra("device", bt2.getMac());
            try {
                startService(dolock);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (bt2.wifi) {
            dowifi(oldwifistate);
        }


        for (int k = 0; k < btdConn.length; k++)
            if (btdConn[k] != null)
                if (bt2.getMac().equalsIgnoreCase(btdConn[k].getMac()))
                    btdConn[k] = null;

        getConnects();
        if ((bt2 != null && bt2.isSetV()) || bt2 == null)
            if (!mvolsLeft)
                setVolume(OldVol2, application);
        if ((bt2 != null && bt2.isSetpv()) || bt2 == null)
            if (!pvolsLeft)
                setPVolume(OldVol);
        if (notify && (bt2.mac != null))
            updateNot(false, null);
        if (mTtsReady && ((bt2.isEnableTTS() || enableGTalk) || connects < 1)) {
            try {
                if (!clearedTts) {
                    clearTts();
                }
                mTts.shutdown();
                mTtsReady = false;
                if (enableGTalk) {
                    unregisterReceiver(sco_change);
                    talk = false;
                }
                if (enableSMS) application.unregisterReceiver(SMScatcher);

                // Toast.makeText(application, "do disconnected",
                // Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                // Toast.makeText(application, e.getMessage(),
                // Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

        }

        if(tmessageRegistered) {
            try {
                application.unregisterReceiver(tmessage);

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            tmessageRegistered = false;
        }

        if (bt2.isSilent())
            am2.setStreamVolume(AudioManager.STREAM_NOTIFICATION, Oldsilent, 0);

        if (bt2.getBdevice() != null && bt2.getBdevice().length() == 17) {
            BluetoothAdapter mBTA = BluetoothAdapter.getDefaultAdapter();
            if (mBTA != null) {
                if (mBTA.isEnabled() && bluetoothWasOff) {
                    // If Bluetooth was off turn it back off
                    mBTA.disable();
                }
                // unbind the service used to connect the device
                doUnbind(application);
            }
        }

        if (bt2.isAutovol()) {
            bt2.setDefVol(SavVol);
            DB.update(bt2);
        }
        if (bt2.isCarmode()) {
            set_car_mode(false);
        }

        final String Ireload = "a2dp.Vol.main.RELOAD_LIST";
        Intent itent = new Intent();
        itent.setAction(Ireload);
        itent.putExtra("disconnect", bt2.getMac());
        application.sendBroadcast(itent);


        disconnecting = false;
    }

    // makes the media volume adjustment
    public static void setVolume(int inputVol, Context sender) {


        int curvol = am2.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (inputVol < 0)
            inputVol = 0;
        if (inputVol > am2.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
            inputVol = am2.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        if (ramp_vol && (inputVol > curvol)) {
            final int minputVol = inputVol;

            new CountDownTimer(((inputVol - curvol) * 1000), 1000) {

                @Override
                public void onFinish() {
                    int ui = 0;
                    if (hideVolUi) ui = 0;
                    else ui = AudioManager.FLAG_SHOW_UI;
                    am2.setStreamVolume(AudioManager.STREAM_MUSIC, minputVol,
                            ui);
                }

                @Override
                public void onTick(long millisUntilFinished) {
                    int ui = 0;
                    if (hideVolUi) ui = 0;
                    else ui = AudioManager.FLAG_SHOW_UI;
                    int cvol = am2.getStreamVolume(AudioManager.STREAM_MUSIC);
                    int newvol = cvol;
                    if ((cvol + 1) < minputVol)
                        ++newvol;
                    am2.setStreamVolume(AudioManager.STREAM_MUSIC, newvol,
                            ui);
                }

            }.start();
        } else {
            int ui = 0;
            if (hideVolUi) ui = 0;
            else ui = AudioManager.FLAG_SHOW_UI;
            am2.setStreamVolume(AudioManager.STREAM_MUSIC, inputVol,
                    ui);
        }
    }

    // captures the media volume so it can be later restored
    private void getOldvol() {
        OldVol2 = am2.getStreamVolume(AudioManager.STREAM_MUSIC);
        // Store the old volume in preferences so it can be extracted if another
        // instance starts or the service is killed and restarted
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(OLD_VOLUME, OldVol2);
        editor.commit();
    }

    // captures the phone volume so it can be later restored
    private void getOldPvol() {
        OldVol = am2.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        Oldsilent = am2.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        // Store the old volume in preferences so it can be extracted if another
        // instance starts or the service is killed and restarted
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(OLD_PH_VOL, OldVol);
        editor.putInt("oldsilent", Oldsilent);
        editor.commit();
    }

    // makes the phone volume adjustment
    public static int setPVolume(int inputVol) {
        int outVol;
        if (inputVol < 0)
            inputVol = 0;
        if (inputVol > am2.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL))
            inputVol = am2.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        if (hideVolUi) {
            am2.setStreamVolume(AudioManager.STREAM_VOICE_CALL, inputVol,
                    AudioManager.FLAG_SHOW_UI);
        } else {
            am2.setStreamVolume(AudioManager.STREAM_VOICE_CALL, inputVol, 0);
        }
        outVol = am2.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        return outVol;
    }

    private void updateNot(boolean connect, String car) {

        String temp = car;
        if (car != null)
            temp = getResources().getString(R.string.connectedTo) + " " + car;
        else {
            if (connects > 0) {
                String tmp = null;
                for (int k = 0; k < btdConn.length; k++)
                    if (btdConn[k] != null)
                        tmp = btdConn[k].toString();

                temp = getResources().getString(R.string.connectedTo) + " "
                        + tmp;
                connect = true;
            } else
                temp = getResources().getString(R.string.ServRunning);
        }
        if (connect) {

            Notification not = null;
            Intent notificationIntent = new Intent(this, main.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, 0);
            not = new NotificationCompat.Builder(application)
                    .setContentTitle(
                            getResources().getString(R.string.app_name))
                    .setContentIntent(contentIntent)
                    .setSmallIcon(connectedIcon).setContentText(temp)
                    .setPriority(Notification.PRIORITY_MIN).build();
            mNotificationManager.notify(1, not);
        } else {
            mNotificationManager.cancel(1);

            if (notify_pref.equalsIgnoreCase("always")) {
                Notification not = null;
                Intent notificationIntent = new Intent(this, main.class);
                PendingIntent contentIntent = PendingIntent.getActivity(
                        this, 0, notificationIntent, 0);
                not = new Notification.Builder(application)
                        .setContentTitle(
                                getResources().getString(R.string.app_name))
                        .setContentIntent(contentIntent)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentText(temp)
                        .setPriority(Notification.PRIORITY_MIN).build();
                mNotificationManager.notify(1, not);
            }
        }
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
        try {
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        } catch (Exception e1) {
            e1.printStackTrace();
        }

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
                act1.killBackgroundProcesses(packageName);
                List<ActivityManager.RunningAppProcessInfo> processes;
                processes = act1.getRunningAppProcesses();
                for (ActivityManager.RunningAppProcessInfo info : processes) {
                    for (int i = 0; i < info.pkgList.length; i++) {
                        if (info.pkgList[i].contains(packageName)) {
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


    public void getIBluetoothA2dp(Context context) {

        //Intent i = new Intent(context, android.bluetooth.IBluetoothA2dp.class);
        Intent i = new Intent(IBluetoothA2dp.class.getName());

        // Need explicit intent on API 20 and up
        String filter;
        filter = getPackageManager().resolveService(i, PackageManager.GET_RESOLVED_FILTER).serviceInfo.packageName;
        i.setPackage(filter);

        if (context.bindService(i, mConnection, Context.BIND_AUTO_CREATE)) {
            //Toast.makeText(context, "started service connection", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "start service connection failed", Toast.LENGTH_SHORT).show();
            // Log.e(TAG, "Could not bind to Bluetooth A2DP Service");
        }

    }

    public static ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            mIsBound = true;
            ibta2 = IBluetoothA2dp.Stub.asInterface(service);
            BluetoothAdapter mBTA = BluetoothAdapter.getDefaultAdapter();
            // if (mBTA == null || !mBTA.isEnabled())
            // return false;

            Set<BluetoothDevice> pairedDevices = mBTA.getBondedDevices();
            BluetoothDevice device = null;
            for (BluetoothDevice dev : pairedDevices) {
                if (dev.getAddress().equalsIgnoreCase(DeviceToConnect))
                    device = dev;
            }
            if (device != null)
                try {
                    ibta2.connect(device);
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsBound = false;
            doUnbind(application);
        }
    };

    static void doUnbind(Context context) {
        if (mIsBound) {
            try {
                context.unbindService(mConnection);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


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
            mvolsLeft = false;
            pvolsLeft = false;
            for (int i = 0; i < btdConn.length; i++) {
                if (btdConn[i] != null) {
                    connects++;
                    if (btdConn[i].isSetV())
                        mvolsLeft = true;
                    if (btdConn[i].isSetpv())
                        pvolsLeft = true;
                }
            }
        }
    }

    private final BroadcastReceiver SMScatcher = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            // tm = (TelephonyManager)
            // getSystemService(Context.TELEPHONY_SERVICE);
            if (intent.getAction().equals(
                    "android.provider.Telephony.SMS_RECEIVED")
                    && tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
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
                                                GetName(currentMessage
                                                        .getDisplayOriginatingAddress()),
                                                currentMessage
                                                        .getDisplayMessageBody()))
                                .append(' ');
                    }
                    final String str = sb.toString().trim();

                    // send to text reader
                    TextReader(str);
                }

            }
        }

    };


    public void TextReader(String rawinput) {
        if (mTtsReady) {
            myHash = new HashMap<String, String>();

            if (rawinput == null) {
                Toast.makeText(application, "No input", Toast.LENGTH_LONG).show();
                return;
            }

            String input = rawinput.replaceAll("http.*? ", ", URL, ");

            myHash.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, A2DP_Vol);

            // trim off very long strings
            if (input.length() > MAX_MESSAGE_LENGTH) {
                input = input.substring(0, MAX_MESSAGE_LENGTH);
                input += " , , , message truncated";
            }

            musicWasPlaying = am2.isMusicActive();

            switch (SMSstream) {
                case IN_CALL_STREAM:
                    if (am2.isBluetoothScoAvailableOffCall()) {
                        am2.startBluetoothSco();
                    }

                    if (musicWasPlaying) {
                        // first pause the music
                        Intent i = new Intent(
                                "com.android.music.musicservicecommand");
                        i.putExtra("command", "pause");
                        sendBroadcast(i);

                    }
                    am2.requestAudioFocus(a2dp.Vol.service.this,
                            AudioManager.STREAM_VOICE_CALL,
                            AudioManager.AUDIOFOCUS_GAIN);
                    myHash.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
                            String.valueOf(AudioManager.STREAM_VOICE_CALL));
                    clearedTts = false;
                    break;

                case MUSIC_STREAM:
                    am2.requestAudioFocus(a2dp.Vol.service.this,
                            AudioManager.STREAM_MUSIC,
                            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                    myHash.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
                            String.valueOf(AudioManager.STREAM_MUSIC));
                    clearedTts = false;
                    break;
                case ALARM_STREAM:
                    am2.requestAudioFocus(a2dp.Vol.service.this,
                            AudioManager.STREAM_ALARM,
                            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                    myHash.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
                            String.valueOf(AudioManager.STREAM_ALARM));
                    clearedTts = false;
                    break;
            }

            final String str = input;
            if (toasts)
                Toast.makeText(application, str, Toast.LENGTH_LONG).show();

            if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {

                new CountDownTimer(SMS_delay, SMS_delay / 2) {

                    @Override
                    public void onFinish() {
                        try {
                            mTts.speak(str, TextToSpeech.QUEUE_ADD, myHash);
                        } catch (Exception e) {
                            Toast.makeText(application, R.string.TTSNotReady,
                                    Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onTick(long arg0) {

                    }

                }.start();
            }

        }
    }

    public TextToSpeech.OnInitListener listenerStarted = new TextToSpeech.OnInitListener() {
        // TTS engine now running so start the message receivers

        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS) {
                mTtsReady = true;
                mTts.setOnUtteranceProgressListener(ul);
            }
        }
    };

    public android.speech.tts.UtteranceProgressListener ul = new UtteranceProgressListener() {

        @Override
        public void onDone(String uttId) {

            int result = AudioManager.AUDIOFOCUS_REQUEST_FAILED;
            if (A2DP_Vol.equalsIgnoreCase(uttId)) {
                // unmute the stream
                switch (SMSstream) {
                    case IN_CALL_STREAM:
/*
					if (am2.isBluetoothScoAvailableOffCall()) {
						am2.stopBluetoothSco();
					}
					if (!speakerPhoneWasOn) {
						am2.setSpeakerphoneOn(false);
					}*/

                        if (!clearedTts) {
                            // clearTts();
                            Intent c = new Intent();
                            c.setAction("a2dp.vol.service.CLEAR");
                            application.sendBroadcast(c);
                        }
                        result = am2.abandonAudioFocus(a2dp.Vol.service.this);

                        break;
                    case MUSIC_STREAM:
                        result = am2.abandonAudioFocus(a2dp.Vol.service.this);
                        break;
                    case ALARM_STREAM:
                        if (!clearedTts) {
                            // clearTts();
                            Intent c = new Intent();
                            c.setAction("a2dp.vol.service.CLEAR");
                            application.sendBroadcast(c);
                        }
                        result = am2.abandonAudioFocus(a2dp.Vol.service.this);
                        break;
                }

                if (result == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                    result = am2.abandonAudioFocus(a2dp.Vol.service.this);
                }
                am2.setMode(AudioManager.MODE_NORMAL);
            }
            if (FIX_STREAM.equalsIgnoreCase(uttId)) {
                result = am2.abandonAudioFocus(a2dp.Vol.service.this);
            }
			/*
			 * if (musicWasPlaying) {
			 * 
			 * Intent downIntent2 = new Intent(Intent.ACTION_MEDIA_BUTTON,null);
			 * KeyEvent downEvent2 = new KeyEvent(KeyEvent.ACTION_DOWN,
			 * KeyEvent.KEYCODE_MEDIA_PLAY);
			 * downIntent2.putExtra(Intent.EXTRA_KEY_EVENT, downEvent2);
			 * sendOrderedBroadcast(downIntent2, null);
			 * 
			 * }
			 */

            am2.setMode(AudioManager.MODE_NORMAL);
        }

        @Override
        public void onError(String utteranceId) { // TODO

        }

        @Override
        public void onStart(String utteranceId) { // TODO

        }

    };

    /*
     * public TextToSpeech.OnUtteranceCompletedListener utteranceDone = new
     * TextToSpeech.OnUtteranceCompletedListener() { public void
     * onUtteranceCompleted(String uttId) { int result =
     * AudioManager.AUDIOFOCUS_REQUEST_FAILED;
     *
     * if (A2DP_Vol.equalsIgnoreCase(uttId)) { // unmute the stream
     *
     * switch (SMSstream) { case IN_CALL_STREAM: if
     * (am2.isBluetoothScoAvailableOffCall()) { am2.stopBluetoothSco(); } if
     * (!speakerPhoneWasOn) { am2.setSpeakerphoneOn(false); } if (!clearedTts) {
     * //clearTts(); Intent c = new Intent();
     * c.setAction("a2dp.vol.service.CLEAR"); application.sendBroadcast(c); }
     * result = am2.abandonAudioFocus(a2dp.Vol.service.this);
     *
     * break; case MUSIC_STREAM: result =
     * am2.abandonAudioFocus(a2dp.Vol.service.this); break; case ALARM_STREAM:
     * if (!clearedTts) { clearTts(); } result =
     * am2.abandonAudioFocus(a2dp.Vol.service.this); break; }
     *
     * if (result == AudioManager.AUDIOFOCUS_REQUEST_FAILED) { result =
     * am2.abandonAudioFocus(a2dp.Vol.service.this); }
     * am2.setMode(AudioManager.MODE_NORMAL); } if
     * (FIX_STREAM.equalsIgnoreCase(uttId)) { result =
     * am2.abandonAudioFocus(a2dp.Vol.service.this); }
     *
     * } };
     */
    private void clearTts() {
        //Toast.makeText(application, "clearing tts", Toast.LENGTH_LONG).show();
		/*if (!speakerPhoneWasOn) {
			am2.setSpeakerphoneOn(false);
		}*/
        if (am2.isBluetoothScoAvailableOffCall()) {
            am2.stopBluetoothSco();
        }


    }

    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:

                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // am2.setMode(0);
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // am2.setMode(0);
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // am2.setMode(0);
                break;
        }

    }

    private String GetName(String number) {

        // check permission
        int permissionCheck = ContextCompat.checkSelfPermission(application,
                Manifest.permission.READ_CONTACTS);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED)
            permReadContacts = true;
        else
            permReadContacts = false;

        if (permReadContacts) {
            ContentResolver cr = getContentResolver();

            Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(number));
            Cursor c = cr.query(uri, new String[]{PhoneLookup.DISPLAY_NAME},
                    null, null, null);

            if (c.moveToFirst()) {
                String name = c.getString(c
                        .getColumnIndex(PhoneLookup.DISPLAY_NAME));
                return name;
            }
			if (c != null) {
				c.close();
			}
        }
        return number;
    }

    public BroadcastReceiver sco_change = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            int state = arg1.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, 0);
            if (state == AudioManager.SCO_AUDIO_STATE_DISCONNECTED && !clearedTts) {
                if (!mTtsReady)
                    mTts = new TextToSpeech(application, listenerStarted);
                HashMap<String, String> myHash2 = new HashMap<String, String>();

                myHash2.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, FIX_STREAM);
                am2.requestAudioFocus(a2dp.Vol.service.this, AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                myHash2.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
                        String.valueOf(AudioManager.STREAM_MUSIC));
                if (mTtsReady) {
                    try {
                        //mTts.speak("... Done", TextToSpeech.QUEUE_ADD, myHash2);
                    } catch (Exception e) {
                        Toast.makeText(application, R.string.TTSNotReady,
                                Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
                am2.abandonAudioFocus(a2dp.Vol.service.this);
                am2.setMode(AudioManager.MODE_NORMAL);
                if (musicWasPlaying) {
                    new CountDownTimer(1000, 6000) {

                        @Override
                        public void onFinish() {
							/*Intent downIntent2 = new Intent(Intent.ACTION_MEDIA_BUTTON,
									null);
							KeyEvent downEvent2 = new KeyEvent(KeyEvent.ACTION_DOWN,
									KeyEvent.KEYCODE_MEDIA_PLAY);
							downIntent2.putExtra(Intent.EXTRA_KEY_EVENT, downEvent2);
							sendOrderedBroadcast(downIntent2, null);*/

                            Intent i = new Intent(
                                    "com.android.music.musicservicecommand");
                            i.putExtra("command", "play");
                            sendBroadcast(i);

                        }

                        @Override
                        public void onTick(long millisUntilFinished) {
                            Intent downIntent2 = new Intent(Intent.ACTION_MEDIA_BUTTON,
                                    null);
                            KeyEvent downEvent2 = new KeyEvent(KeyEvent.ACTION_DOWN,
                                    KeyEvent.KEYCODE_MEDIA_PLAY);
                            downIntent2.putExtra(Intent.EXTRA_KEY_EVENT, downEvent2);
                            sendOrderedBroadcast(downIntent2, null);

                        }

                    }.start();

                }
                clearedTts = true;
            }
        }

    };

    private void set_car_mode(boolean mode) {
        try {
            UiModeManager mm = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
            if (mode)
                mm.enableCarMode(UiModeManager.ENABLE_CAR_MODE_GO_CAR_HOME);
            else
                mm.disableCarMode(0);

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
