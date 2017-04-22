package a2dp.Vol;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.Vector;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.IBluetooth;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

//import com.google.android.gms.appindexing.Action;
//import com.google.android.gms.appindexing.AppIndex;
//import com.google.android.gms.appindexing.Thing;
//import com.google.android.gms.common.api.GoogleApiClient;

public class main extends Activity {

    static AudioManager am = (AudioManager) null;
    static Button serv;
    boolean servrun = false;
    ListView lvl = null; // listview used on main screen for showing devices
    Vector<btDevice> vec = new Vector<btDevice>(); // vector of bluetooth
    // devices
    private DeviceDB myDB; // database of device data stored in SQlite
    String activebt = null;
    private MyApplication application;
    SharedPreferences preferences;
    public static final String PREFS_NAME = "btVol";
    String[] lstring = null; // string array used for the listview
    ArrayAdapter<String> ladapt; // listview adapter
    int connects;
    static final int ENABLE_BLUETOOTH = 1;
    static final int RELOAD = 2;
    static final int CHECK_TTS = 3;
    static final int EDITED_DATA = 4;
    boolean carMode = false;
    boolean homeDock = false;
    boolean headsetPlug = false;
    boolean power = false;
    boolean enableTTS = false;
    boolean toasts = true;
    boolean TTSignore = false;
    private String a2dpDir = "";
    private static final String LOG_TAG = "A2DP_Volume";
    private static int resourceID = android.R.layout.simple_list_item_1;
    private final int PERMISSION_READ_CONTACTS = 1;
    private final int PERMISSION_LOCATION = 2;
    private final int PERMISSION_PHONE = 3;
    private final int PERMISSION_SMS = 4;
    private final int PERMISSION_STORAGE = 5;
    Resources res;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    //private GoogleApiClient client;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    //private com.google.android.gms.common.api.GoogleApiClient client2;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @Handles item selections for the options menu
     */
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.Manage_data: // used to export the data
                this.myDB.getDb().close();
                Intent i = new Intent(getBaseContext(), ManageData.class);
                startActivityForResult(i, RELOAD);
                return true;

            case R.id.Exit:
                stopService(new Intent(a2dp.Vol.main.this, service.class));
                a2dp.Vol.main.this.finish();
                return true;

            case R.id.packages:
                Intent p = new Intent(a2dp.Vol.main.this, PackagesChooser.class);
                startActivity(p);
                return true;

            case R.id.prefs: // set preferences
                Intent j = new Intent(a2dp.Vol.main.this, Preferences.class);
                startActivity(j);
                return true;

            case R.id.DelData: // clears the database of all devices and settings.
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.DeleteDataMsg)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        if (myDB.getDb().isOpen())
                                            myDB.deleteAll();
                                        refreshList(loadFromDB());
                                    }
                                })
                        .setNegativeButton(android.R.string.no,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        // put your code here
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                return true;

            case R.id.help: // launches help website
                String st = "https://github.com/jroal/a2dpvolume/wiki";
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(st)));
                return true;
        }
        return false;
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        res = getResources();
        setContentView(R.layout.main);
        ComponentName comp = new ComponentName("a2dp.Vol", "main");
        PackageInfo pinfo;
        String ver = null;
        try {
            pinfo = getPackageManager()
                    .getPackageInfo(comp.getPackageName(), 0);
            ver = pinfo.versionName;
        } catch (NameNotFoundException e) {
            Log.e(LOG_TAG, "error" + e.getMessage());
        }

        setTitle(res.getString(R.string.app_name) + " Version: "
                + ver);
        // get "Application" object for shared state or creating of expensive
        // resources - like DataHelper
        // (this is not recreated as often as each Activity)
        this.application = (MyApplication) this.getApplication();

        preferences = PreferenceManager
                .getDefaultSharedPreferences(this.application);

        try {
            boolean local = preferences.getBoolean("useLocalStorage", false);
            if (local)
                a2dpDir = getFilesDir().toString();
            else
                a2dpDir = Environment.getExternalStorageDirectory()
                        + "/A2DPVol";

            File exportDir = new File(a2dpDir);

            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            carMode = preferences.getBoolean("car_mode", true);
            homeDock = preferences.getBoolean("home_dock", false);
            headsetPlug = preferences.getBoolean("headset", false);
            power = preferences.getBoolean("power", false);
            enableTTS = preferences.getBoolean("enableTTS", false);
            toasts = preferences.getBoolean("toasts", true);
            TTSignore = preferences.getBoolean("TTSignore", false);
        } catch (Exception e2) {
            Log.e(LOG_TAG, "error" + e2.getMessage());
        }
        connects = 0;
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        final Button btn = (Button) findViewById(R.id.Button01);

        final Button locbtn = (Button) findViewById(R.id.Locationbtn);
        serv = (Button) findViewById(R.id.ServButton);

        // these 2 intents are sent from the service to inform us of the running
        // state
        IntentFilter filter3 = new IntentFilter("a2dp.vol.service.RUNNING");
        try {
            this.registerReceiver(sRunning, filter3);
        } catch (Exception e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }

        IntentFilter filter4 = new IntentFilter(
                "a2dp.vol.service.STOPPED_RUNNING");
        try {
            this.registerReceiver(sRunning, filter4);
        } catch (Exception e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }

        // this reciever is used to tell this main activity about devices
        // connecting and disconnecting.
        IntentFilter filter5 = new IntentFilter("a2dp.Vol.main.RELOAD_LIST");
        this.registerReceiver(mReceiver5, filter5);

        IntentFilter filter6 = new IntentFilter("a2dp.vol.preferences.UPDATED");
        this.registerReceiver(mReceiver6, filter6);

        lstring = new String[]{res.getString(R.string.NoData)};

        this.myDB = new DeviceDB(application);

        // do this stuff if it is the first time through for this power cycle.
        if (savedInstanceState == null) {
            int devicemin = 1;
            if (carMode)
                devicemin++;
            if (homeDock)
                devicemin++;
            try {
                if (myDB.getLength() < devicemin) {
                    getBtDevices(1);
                }
            } catch (Exception e1) {
                Log.e(LOG_TAG, "error" + e1.getMessage());
            }

            serv.setText(R.string.StartService);
            // start the service if this is the first time through. The intent
            // will report when the service has
            // started and toggle button text

            startService(new Intent(a2dp.Vol.main.this, service.class));
            if (enableTTS) {
                // Fire off an intent to check if a TTS engine is installed
                try {
                    Intent checkIntent = new Intent();
                    checkIntent
                            .setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
                    startActivityForResult(checkIntent, CHECK_TTS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            startService(new Intent(a2dp.Vol.main.this, NotificationCatcher.class));
//			Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
//			startActivity(intent);
        }

        this.ladapt = new ArrayAdapter<String>(application, resourceID, lstring);
        this.lvl = (ListView) findViewById(R.id.ListView01);
        this.lvl.setAdapter(ladapt);

        // find bonded devices and load into the database and listview
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getBtDevices(1);
            }
        });

        // This shows the details of the bluetooth device
        lvl.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {

                if (vec.isEmpty())
                    return false;
                BluetoothAdapter mBTA = BluetoothAdapter.getDefaultAdapter();

                btDevice bt = new btDevice();
                bt = vec.get(position);
                BluetoothDevice btd = null;
                if (mBTA != null) {
                    Set<BluetoothDevice> pairedDevices = mBTA
                            .getBondedDevices();
                    for (BluetoothDevice device : pairedDevices) {
                        if (device.getAddress().equalsIgnoreCase(bt.mac)) {
                            btd = device;
                        }
                    }
                }

                android.app.AlertDialog.Builder builder = new AlertDialog.Builder(
                        a2dp.Vol.main.this);
                builder.setTitle(bt.toString());
                final String car = bt.toString();
                String mesg;
                if (btd != null) {
                    mesg = bt.desc1 + "\n" + bt.mac + "\n" + res.getString(R.string.Bonded);
                    switch (btd.getBondState()) {
                        case BluetoothDevice.BOND_BONDED:
                            mesg += " = " + res.getString(R.string.Bonded);
                            break;
                        case BluetoothDevice.BOND_BONDING:
                            mesg += " = " + res.getString(R.string.Bonding);
                            break;
                        case BluetoothDevice.BOND_NONE:
                            mesg += " = " + res.getString(R.string.NotBonded);
                            break;
                        case BluetoothDevice.ERROR:
                            mesg += " = " + res.getString(R.string.Error);
                            break;
                    }

                    mesg += "\n" + res.getString(R.string.Class) + " = " + getBTClassDev(btd);
                    mesg += "\nMajor " + res.getString(R.string.Class) + " = " + getBTClassDevMaj(btd);
                    mesg += "\nService " + res.getString(R.string.Class) + " = " + getBTClassServ(btd);
                } else {
                    mesg = (String) getText(R.string.btNotOn);
                }

                builder.setMessage(mesg);
                builder.setPositiveButton("OK", null);
                builder.setNeutralButton(R.string.LocationString,
                        new OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                File exportDir = new File(a2dpDir);

                                if (!exportDir.exists())
                                    return;
                                // String file =
                                // "content://com.android.htmlfileprovider"
                                String file = "file:///" + exportDir.getPath()
                                        + "/" + car.replaceAll(" ", "_")
                                        + ".html";
                                String st = new String(file).trim();

                                Uri uri = Uri.parse(st);
                                Intent intent = new Intent();
                                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setDataAndType(uri, "text/html");
                                try {
                                    PackageInfo pi = getPackageManager().getPackageInfo("com.android.chrome", 0);
                                    intent.setClassName("com.android.chrome",
                                            "com.google.android.apps.chrome.Main");
                                } catch (NameNotFoundException e1) {
                                    intent.setClassName("com.android.browser",
                                            "com.android.browser.BrowserActivity");
                                    e1.printStackTrace();
                                }


                                try {
                                    startActivity(intent);
                                    /*WebView myWebView = (WebView) findViewById(R.id.webview);
                                    myWebView.loadUrl(uri.toString());*/
                                } catch (Exception e) {
                                    // TODO Auto-generated catch block
                                    Toast.makeText(application, e.toString(),
                                            Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }

                            }
                        });
                builder.show();

                return true;
            }
        });

        // display the selected item and allow editing
        lvl.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (vec.isEmpty())
                    return;

                final btDevice bt = vec.get(position);
                final btDevice bt2 = myDB.getBTD(bt.mac);
                android.app.AlertDialog.Builder builder = new AlertDialog.Builder(
                        a2dp.Vol.main.this);
                builder.setTitle(bt.toString());
                builder.setMessage(bt2.desc1 + "\n" + bt2.desc2 + "\n"
                        + bt2.mac);
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setNegativeButton(R.string.Delete,
                        new OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                myDB.delete(bt2);
                                refreshList(loadFromDB());
                            }
                        });
                builder.setNeutralButton(R.string.Edit, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(a2dp.Vol.main.this,
                                EditDevice.class);
                        i.putExtra("btd", bt.mac);
                        startActivityForResult(i, EDITED_DATA);
                    }
                });
                builder.show();
            }
        });

        locbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Locationbtn();
            }
        });

        // long click opens the most accurate location
        locbtn.setOnLongClickListener(new View.OnLongClickListener() {

            public boolean onLongClick(View v) {
                try {
                    byte[] buff = new byte[250];
                    FileInputStream fs = openFileInput("My_Last_Location2");
                    fs.read(buff);
                    fs.close();
                    String st = new String(buff).trim();
                    Toast.makeText(a2dp.Vol.main.this, st, Toast.LENGTH_LONG)
                            .show();
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(st)));
                } catch (FileNotFoundException e) {
                    Toast.makeText(a2dp.Vol.main.this, R.string.NoData,
                            Toast.LENGTH_LONG).show();
                    Log.e(LOG_TAG, "error" + e.getMessage());
                } catch (IOException e) {
                    Toast.makeText(a2dp.Vol.main.this, "Some IO issue",
                            Toast.LENGTH_LONG).show();
                    Log.e(LOG_TAG, "error" + e.getMessage());
                }
                return false;
            }
        });

        // toggle the service ON or OFF and change the button text to reflect
        // the new state
        serv.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                if (servrun) {
                    stopService(new Intent(a2dp.Vol.main.this, service.class));

                } else {
                    startService(new Intent(a2dp.Vol.main.this, service.class));

                }
            }
        });

        new CountDownTimer(2000, 1000) {

            public void onTick(long millisUntilFinished) {
                try {
                    if (a2dp.Vol.service.run) {
                        servrun = true;
                        serv.setText(R.string.StopService);
                    } else {
                        servrun = false;
                        serv.setText(R.string.StartService);
                    }
                } catch (Exception x) {
                    servrun = false;
                    serv.setText(R.string.StartService);
                    Log.e(LOG_TAG, "error" + x.getMessage());
                }
            }

            public void onFinish() {
                try {
                    if (a2dp.Vol.service.run) {
                        servrun = true;
                        serv.setText(R.string.StopService);
                        getConnects();
                        refreshList(loadFromDB());
                    } else {
                        servrun = false;
                        serv.setText(R.string.StartService);
                    }
                } catch (Exception x) {
                    servrun = false;
                    serv.setText(R.string.StartService);
                    Log.e(LOG_TAG, "error" + x.getMessage());
                }
            }
        }.start();

        // load the list from the database
        getConnects();
        refreshList(loadFromDB());

        int ps = permission_scan();
        if(ps > 0)check_permissions(ps);

        super.onCreate(savedInstanceState);
    }


    private int permission_scan(){

        int ret = 0;
        if(!preferences.getBoolean("ReadContactsPermission",false))ret = 1;

        if(!preferences.getBoolean("LocationPermission",false))ret = 2;

        if(!preferences.getBoolean("PhonePermission",false))ret = 3;

        if(!preferences.getBoolean("SMSPermission",false))ret = 4;

        if(!preferences.getBoolean("StoragePermission",false))ret = 5;

        return ret;
    }

    private void check_permissions(int perm) {

        // Check permissions

        switch (perm) {
            case PERMISSION_READ_CONTACTS:
                if (ContextCompat.checkSelfPermission(application,
                        Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.READ_CONTACTS)) {
                        askPermission(R.string.askContacts, PERMISSION_READ_CONTACTS, Manifest.permission.READ_CONTACTS);
                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.

                    } else {

                        // No explanation needed, we can request the permission.

                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.READ_CONTACTS},
                                PERMISSION_READ_CONTACTS);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                }
                break;

            case PERMISSION_LOCATION:
                if (ContextCompat.checkSelfPermission(application,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                        askPermission(R.string.askLocation, PERMISSION_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.

                    } else {

                        // No explanation needed, we can request the permission.

                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISSION_LOCATION);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                }
                break;

            case PERMISSION_SMS:
                if (ContextCompat.checkSelfPermission(application,
                        Manifest.permission.RECEIVE_SMS)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.RECEIVE_SMS)) {
                        askPermission(R.string.askSms, PERMISSION_SMS, Manifest.permission.RECEIVE_SMS);
                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.

                    } else {

                        // No explanation needed, we can request the permission.

                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.RECEIVE_SMS},
                                PERMISSION_SMS);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                }
                break;

            case PERMISSION_PHONE:
                if (ContextCompat.checkSelfPermission(application,
                        Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.READ_PHONE_STATE)) {

                        askPermission(R.string.askPhone, PERMISSION_PHONE, Manifest.permission.READ_PHONE_STATE);
                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.

                    } else {

                        // No explanation needed, we can request the permission.

                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.READ_PHONE_STATE},
                                PERMISSION_PHONE);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                }
                break;

            case PERMISSION_STORAGE:
                if (ContextCompat.checkSelfPermission(application,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        askPermission(R.string.askStorage, PERMISSION_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.

                    } else {

                        // No explanation needed, we can request the permission.

                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PERMISSION_STORAGE);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                }
                break;
        }

    }

    private void askPermission(int message, final int mperm, final String permission) {
        //This fucnction is used to ask the user for permissions, explaining why they are needed
        AlertDialog.Builder dial = new AlertDialog.Builder(this);
        dial.setMessage(message)
                .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton(R.string.askSetPerm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        ActivityCompat.requestPermissions(a2dp.Vol.main.this, new String[]{permission}, mperm);
                    }
                }).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("ReadContactsPermission", true);
                editor.commit();

                break;
            }
            case PERMISSION_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("LocationPermission", true);
                editor.commit();

                break;
            }
            case PERMISSION_PHONE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("PhonePermission", true);
                editor.commit();

                break;
            }
            case PERMISSION_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("SMSPermission", true);
                editor.commit();

                break;
            }
            case PERMISSION_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("StoragePermission", true);
                editor.commit();

                break;
            }
            // other 'case' lines to check for other
            // permissions this app might request

        }
        int ps = permission_scan();
        if(ps > 0)check_permissions(ps);

        return;
    }


    private void getConnects() {
        if (servrun) {
            connects = a2dp.Vol.service.connects;
        } else
            connects = 0;
    }

    @Override
    protected void onStop() {

        super.onStop();

    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        try {
            this.unregisterReceiver(sRunning);
            this.unregisterReceiver(mReceiver5);
            this.unregisterReceiver(mReceiver6);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.myDB.getDb().close();
        super.onDestroy();
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        getConnects();
        refreshList(loadFromDB());
        super.onResume();
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onRestart()
     */
    @Override
    protected void onRestart() {

        super.onRestart();
    }

    /**
     * Retrieves the last stored location and sends it as a URL
     */
    public void Locationbtn() {
        try {
            byte[] buff = new byte[250];
            FileInputStream fs = openFileInput("My_Last_Location");
            fs.read(buff);
            fs.close();
            String st = new String(buff).trim();
            //Toast.makeText(a2dp.Vol.main.this, st, Toast.LENGTH_LONG).show();
            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(st)));
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(st));
            startActivity(i);
        } catch (FileNotFoundException e) {
            Toast.makeText(a2dp.Vol.main.this, R.string.NoData,
                    Toast.LENGTH_LONG).show();
            Log.e(LOG_TAG, "error" + e.getMessage());
        } catch (IOException e) {
            Toast.makeText(a2dp.Vol.main.this, "Some IO issue",
                    Toast.LENGTH_LONG).show();
            Log.e(LOG_TAG, "error" + e.getMessage());
        }
    }

    // function to get all bonded audio devices, load into database, and load
    // the vector and listview.

    /**
     * @return the number of devices listed
     */
    private int getBtDevices(int mode) {
        int i = 0;
        vec.clear();

        // the section below is for testing only. Comment out before building
        // the application for use.
        /*
         * btDevice bt3 = new btDevice(); bt3.setBluetoothDevice("Device 1",
		 * "Porsche", "00:22:33:44:55:66:77", 15); i = 1; btDevice btx =
		 * myDB.getBTD(bt3.mac); if(btx.mac == null) {
		 * a2dp.Vol.main.this.myDB.insert(bt3); vec.add(bt3); } else
		 * vec.add(btx);
		 * 
		 * btDevice bt4 = new btDevice();
		 * bt4.setBluetoothDevice("Motorola T605", "Jaguar",
		 * "33:44:55:66:77:00:22", 14); btDevice bty = myDB.getBTD(bt4.mac); i =
		 * 2; if(bty.mac == null) { a2dp.Vol.main.this.myDB.insert(bt4);
		 * vec.add(bt4); } else vec.add(bty);
		 * 
		 * List<String> names = this.myDB.selectAll(); StringBuilder sb = new
		 * StringBuilder(); sb.append("Names in database:\n"); for (String name
		 * : names) { sb.append(name + "\n"); } str2 += " " + i;
		 * refreshList(loadFromDB());
		 */
        // end of testing code

        if (carMode) {
            // add the car dock false device if car mode check is enabled
            btDevice fbt = new btDevice();
            String str = getString(R.string.carDockName);
            fbt.setBluetoothDevice(str, str, "1",
                    am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            btDevice fbt2 = myDB.getBTD(fbt.mac);
            if (fbt2.mac == null) {
                fbt.setIcon(R.drawable.car2);
                a2dp.Vol.main.this.myDB.insert(fbt);
                vec.add(fbt);
            } else
                vec.add(fbt2);

            refreshList(loadFromDB()); // make sure it is relisted
        }

        if (homeDock) {
            // add the home dock false device if car mode check is enabled
            btDevice fbt = new btDevice();
            String str = getString(R.string.homeDockName);
            fbt.setBluetoothDevice(str, str, "2",
                    am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            btDevice fbt2 = myDB.getBTD(fbt.mac);
            if (fbt2.mac == null) {
                fbt.setGetLoc(false);
                fbt.setIcon(R.drawable.usb);
                a2dp.Vol.main.this.myDB.insert(fbt);
                vec.add(fbt);
            } else
                vec.add(fbt2);

            refreshList(loadFromDB()); // make sure it is relisted
        }
        if (headsetPlug) {
            // add the headset plug false device if headset plug check is
            // enabled
            btDevice fbt = new btDevice();
            String str = getString(R.string.audioJackName);
            fbt.setBluetoothDevice(str, str, "3",
                    am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            btDevice fbt2 = myDB.getBTD(fbt.mac);
            if (fbt2.mac == null) {
                fbt.setGetLoc(false);
                fbt.setIcon(R.drawable.jack);
                a2dp.Vol.main.this.myDB.insert(fbt);
                vec.add(fbt);
            } else
                vec.add(fbt2);

            refreshList(loadFromDB()); // make sure it is relisted
        }

        if (power) {
            // add the power false device if power check is
            // enabled
            btDevice fbt = new btDevice();
            String str = getString(R.string.powerPlugName);
            fbt.setBluetoothDevice(str, str, "4",
                    am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            btDevice fbt2 = myDB.getBTD(fbt.mac);
            if (fbt2.mac == null) {
                fbt.setGetLoc(false);
                fbt.setIcon(R.drawable.usb);
                a2dp.Vol.main.this.myDB.insert(fbt);
                vec.add(fbt);
            } else
                vec.add(fbt2);

            refreshList(loadFromDB()); // make sure it is relisted
        }

        if (mode >= 1) {
            BluetoothAdapter mBTA = BluetoothAdapter.getDefaultAdapter();
            if (mBTA == null) {
                Toast.makeText(application, R.string.NobtSupport,
                        Toast.LENGTH_LONG).show();
                return 0;
            }
            // If Bluetooth is not yet enabled, enable it
            if (!mBTA.isEnabled()) {
                Intent enableBluetooth = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                try {
                    startActivityForResult(enableBluetooth, ENABLE_BLUETOOTH);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // Now implement the onActivityResult() and wait for it to
                // be invoked with ENABLE_BLUETOOTH
                // onActivityResult(ENABLE_BLUETOOTH, result, enableBluetooth);
                return 0;
            }
            if (mBTA != null) {
                Set<BluetoothDevice> pairedDevices = mBTA.getBondedDevices();
                // If there are paired devices

                if (pairedDevices.size() > 0) {
                    //IBluetooth ibta = getIBluetooth();
                    // Loop through paired devices
                    for (BluetoothDevice device : pairedDevices) {
                        // Add the name and address to an array adapter to show in a
                        // ListView
                        if (device.getAddress() != null) {
                            btDevice bt = new btDevice();
                            i++;
                            String name = null;

                            try {
                                Method m = device.getClass().getMethod("getAlias");
                                Object res = m.invoke(device);
                                if (res != null)
                                    name = res.toString();
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }

                            if (name == null)
                                name = device.getName();

                            bt.setBluetoothDevice(
                                    device,
                                    name,
                                    am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));

                            bt.setSetV(true);

                            btDevice bt2 = myDB.getBTD(bt.mac);

                            if (bt2.mac == null) {
                                myDB.insert(bt);
                                vec.add(bt);
                            } else
                                vec.add(bt2);
                        }
                    }

                }
            }
            refreshList(loadFromDB());
            Toast.makeText(application, "Found " + i + " Bluetooth Devices",
                    Toast.LENGTH_LONG).show();
        }
        return i;
    }

    // Listen for results.
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // See which child activity is calling us back.

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ENABLE_BLUETOOTH:
                    // This is the standard resultCode that is sent back if the
                    // activity crashed or didn't doesn't supply an explicit result.
                    if (resultCode == RESULT_CANCELED) {
                        Toast.makeText(application, R.string.btEnableFail,
                                Toast.LENGTH_LONG).show();
                        refreshList(loadFromDB());
                    } else {

                        int test = getBtDevices(1);
                        if (test > 0) {
                            lstring = new String[test];
                            for (int i = 0; i < test; i++) {
                                lstring[i] = vec.get(i).toString();
                            }
                            refreshList(loadFromDB());
                        }
                    }
                    break;
                case RELOAD:
                    refreshList(loadFromDB());
                    break;

                default:
                    break;
            }
        }
        if (requestCode == EDITED_DATA) {
            enableTTS = preferences.getBoolean("enableTTS", false);
            if (enableTTS) {
                // Fire off an intent to check if a TTS engine is installed
                try {
                    Intent checkIntent = new Intent();
                    checkIntent
                            .setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
                    startActivityForResult(checkIntent, CHECK_TTS);
                } catch (Exception e) {
                    Toast.makeText(application, "TTS missing fault", Toast.LENGTH_LONG).show();
                }
            }
        }
        if (requestCode == CHECK_TTS) {

            switch (resultCode) {
                case TextToSpeech.Engine.CHECK_VOICE_DATA_PASS:
                    if (toasts) Toast.makeText(application, R.string.TTSready, Toast.LENGTH_SHORT)
                            .show();
                    break;

                case TextToSpeech.Engine.CHECK_VOICE_DATA_MISSING_DATA:
                    if (TTSignore) {
                        // do something maybe?
                    } else {
                        // missing data, install it
                        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(
                                a2dp.Vol.main.this);
                        builder.setTitle(getString(R.string.app_name));
                        builder.setPositiveButton(R.string.Yes,
                                new OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        Intent installIntent = new Intent();
                                        installIntent
                                                .setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                                        startActivityForResult(installIntent,
                                                CHECK_TTS);
                                    }
                                });
                        builder.setNegativeButton(R.string.No, null);
                        OnClickListener ignoreListener = setIgnore();
                        builder.setNeutralButton(R.string.ignoreTTSMissing,
                                ignoreListener);
                        builder.setMessage(R.string.needTTS);
                        builder.show();
                    }
                    break;

                case TextToSpeech.Engine.CHECK_VOICE_DATA_MISSING_VOLUME:
                    if (toasts)
                        Toast.makeText(application, "TTS Missing Volume", Toast.LENGTH_SHORT)
                                .show();
                    break;

                case TextToSpeech.Engine.CHECK_VOICE_DATA_BAD_DATA:
                    if (toasts) Toast.makeText(application, "TTS Bad Data", Toast.LENGTH_SHORT)
                            .show();
                    break;

                case TextToSpeech.Engine.CHECK_VOICE_DATA_FAIL:
                    if (toasts)
                        Toast.makeText(application, "TTS Voice data fail", Toast.LENGTH_SHORT)
                                .show();
                    break;
            }

        }
    }

    private OnClickListener setIgnore() {
        SharedPreferences.Editor editor = preferences.edit();
        TTSignore = true;
        editor.putBoolean("TTSignore", true);
        editor.commit();
        return null;
    }

    // this is called to update the list from the database
    private void refreshList(int test) {

        if (test > 0) {
            lstring = new String[test];
            for (int i = 0; i < test; i++) {
                lstring[i] = vec.get(i).toString();
                if (connects > 0 && servrun) {
                    for (int j = 0; j < a2dp.Vol.service.btdConn.length; j++) {
                        if (a2dp.Vol.service.btdConn[j] != null)
                            if (vec.get(i)
                                    .getMac()
                                    .equalsIgnoreCase(
                                            a2dp.Vol.service.btdConn[j]
                                                    .getMac()))
                                lstring[i] += " **";
                    }
                }
            }
        } else {
            lstring = new String[]{"no data"};

            // Toast.makeText(this, "No data", Toast.LENGTH_LONG);
        }
        a2dp.Vol.main.this.ladapt = new ArrayAdapter<String>(application,
                resourceID, lstring);
        a2dp.Vol.main.this.lvl.setAdapter(ladapt);
        a2dp.Vol.main.this.ladapt.notifyDataSetChanged();
        a2dp.Vol.main.this.lvl.invalidateViews();
        a2dp.Vol.main.this.lvl.forceLayout();
    }

    // this just loads the bluetooth device array from the database
    private int loadFromDB() {
        myDB.getDb().close();
        if (!myDB.getDb().isOpen())
            try {
                myDB = new DeviceDB(application);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }

        try {
            vec = myDB.selectAlldb();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        if (vec.isEmpty() || vec == null)
            return 0;

        return vec.size();
    }

    /**
     * received the reload list intent
     */
    private final BroadcastReceiver mReceiver5 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context2, Intent intent2) {
            getConnects();
            refreshList(loadFromDB());
            // Toast.makeText(context2, "mReceiver5", Toast.LENGTH_LONG).show();
        }
    };

    /**
     * preferences have changed, reload new
     */
    private final BroadcastReceiver mReceiver6 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context2, Intent intent2) {
            boolean carModeOld = carMode;
            boolean homeDockOld = homeDock;
            boolean headsetPlugOld = headsetPlug;
            boolean powerOld = power;

            try {
                carMode = preferences.getBoolean("car_mode", false);
                homeDock = preferences.getBoolean("home_dock", false);
                headsetPlug = preferences.getBoolean("headset", false);
                power = preferences.getBoolean("power", false);
                enableTTS = preferences.getBoolean("enableTTS", false);
                boolean local = preferences
                        .getBoolean("useLocalStorage", false);
                if (local)
                    a2dpDir = getFilesDir().toString();
                else
                    a2dpDir = Environment.getExternalStorageDirectory()
                            + "/A2DPVol";

                File exportDir = new File(a2dpDir);

                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                Log.e(LOG_TAG, "error" + e2.getMessage());
            }
            // if we added a special device make sure to insert it in the
            // database
            if ((!carModeOld && carMode) || (!homeDockOld && homeDock) || (!headsetPlugOld && headsetPlug) || (!powerOld && power))
                getBtDevices(0);

            if (enableTTS) {
                // Fire off an intent to check if a TTS engine is installed
                try {
                    Intent checkIntent = new Intent();
                    checkIntent
                            .setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
                    startActivityForResult(checkIntent, CHECK_TTS);
                } catch (Exception e) {
                    e.printStackTrace();
                }

// If the user enabled reading notifications, check settings and launch setting dialog if its not enabled
                Set<String> list = NotificationManagerCompat.getEnabledListenerPackages(getBaseContext());
                Boolean listenerEnabled = false;
                for (String item : list) {
                    if (item.equalsIgnoreCase("a2dp.Vol")) listenerEnabled = true;
                }

                if (preferences.getBoolean("enableGTalk", false) && !listenerEnabled) {
                    Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                    startActivity(intent);
                }
            }
        }
    };

    private final BroadcastReceiver sRunning = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // toggle the service button depending on the state of the service
            try {
                if (a2dp.Vol.service.run) {
                    servrun = true;
                    serv.setText(R.string.StopService);
                    getConnects();
                } else {
                    servrun = false;
                    serv.setText(R.string.StartService);
                    connects = 0;
                }
            } catch (Exception x) {
                x.printStackTrace();
                servrun = false;
                serv.setText(R.string.StartService);
                connects = 0;
                Log.e(LOG_TAG, "error" + x.getMessage());
            }
            refreshList(loadFromDB());
        }

    };

    // Returns the bluetooth services supported as a string
    private String getBTClassServ(BluetoothDevice btd) {
        String temp = "";
        if (btd == null)
            return temp;
        if (btd.getBluetoothClass().hasService(BluetoothClass.Service.AUDIO))
            temp = "Audio, ";
        if (btd.getBluetoothClass()
                .hasService(BluetoothClass.Service.TELEPHONY))
            temp += "Telophony, ";
        if (btd.getBluetoothClass().hasService(
                BluetoothClass.Service.INFORMATION))
            temp += "Information, ";
        if (btd.getBluetoothClass().hasService(
                BluetoothClass.Service.LIMITED_DISCOVERABILITY))
            temp += "Limited Discoverability, ";
        if (btd.getBluetoothClass().hasService(
                BluetoothClass.Service.NETWORKING))
            temp += "Networking, ";
        if (btd.getBluetoothClass().hasService(
                BluetoothClass.Service.OBJECT_TRANSFER))
            temp += "Object Transfer, ";
        if (btd.getBluetoothClass().hasService(
                BluetoothClass.Service.POSITIONING))
            temp += "Positioning, ";
        if (btd.getBluetoothClass().hasService(BluetoothClass.Service.RENDER))
            temp += "Render, ";
        if (btd.getBluetoothClass().hasService(BluetoothClass.Service.CAPTURE))
            temp += "Capture, ";
        // trim off the extra comma and space
        if (temp.length() > 5)
            temp = temp.substring(0, temp.length() - 2);
        // return the list of supported service classes
        return temp;
    }

    // Get the bluetooth device classes we care about most. Not an exhaustive
    // list.

    /**
     * @param btd is the BluetoothDevice to check
     * @return a list of the bluetooth services this device supports
     */
    private String getBTClassDev(BluetoothDevice btd) {
        String temp = "";
        if (btd == null)
            return temp;
        if (btd.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO)
            temp = "Car Audio, ";
        if (btd.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE)
            temp += "Handsfree, ";
        if (btd.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES)
            temp += "Headphones, ";
        if (btd.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.AUDIO_VIDEO_HIFI_AUDIO)
            temp += "HiFi Audio, ";
        if (btd.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER)
            temp += "Loudspeaker, ";
        if (btd.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.AUDIO_VIDEO_PORTABLE_AUDIO)
            temp += "Portable Audio, ";
        if (btd.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.AUDIO_VIDEO_CAMCORDER)
            temp += "Camcorder, ";
        if (btd.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.AUDIO_VIDEO_SET_TOP_BOX)
            temp += "Set Top Box, ";
        if (btd.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER)
            temp += "A/V Display/Speaker, ";
        if (btd.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.AUDIO_VIDEO_VIDEO_MONITOR)
            temp += "Video Monitor, ";
        if (btd.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.AUDIO_VIDEO_VCR)
            temp += "VCR, ";
        if (btd.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.PHONE_CELLULAR)
            temp += "Cellular Phone, ";
        if (btd.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.PHONE_SMART)
            temp += "Smart Phone, ";
        if (btd.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.PHONE_CORDLESS)
            temp += "Cordless Phone, ";
        if (btd.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.PHONE_ISDN)
            temp += "ISDN Phone, ";
        if (btd.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.PHONE_MODEM_OR_GATEWAY)
            temp += "Phone Modem/Gateway, ";
        if (btd.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.PHONE_UNCATEGORIZED)
            temp += "Other Phone, ";
        if (btd.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET)
            temp += "Wearable Headset, ";
        if (btd.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.AUDIO_VIDEO_UNCATEGORIZED)
            temp += "Uncategorized A/V, ";
        if (btd.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.PHONE_UNCATEGORIZED)
            temp += "Uncategorized Phone, ";
        if (btd.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.TOY_UNCATEGORIZED)
            temp += "Incategorized Toy, ";
        if (btd.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.COMPUTER_DESKTOP)
            temp += "Desktop PC, ";
        if (btd.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.COMPUTER_HANDHELD_PC_PDA)
            temp += "Handheld PC, ";
        if (btd.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.COMPUTER_LAPTOP)
            temp += "Laptop PC, ";
        if (btd.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.COMPUTER_PALM_SIZE_PC_PDA)
            temp += "Palm Sized PC/PDA, ";
        if (btd.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.COMPUTER_WEARABLE)
            temp += "Wearable PC, ";
        if (btd.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.COMPUTER_SERVER)
            temp += "Server PC, ";
        if (btd.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.COMPUTER_UNCATEGORIZED)
            temp += "Computer, ";
        // trim off the extra comma and space. If the class was not found,
        // return other.
        if (temp.length() > 3)
            temp = temp.substring(0, temp.length() - 2);
        else
            temp = "other";

        // return device class
        return temp;
    }

    // Get the bluetooth major device classes we care about most. Not an
    // exhaustive list.

    /**
     * @param btd the bluetooth device to test.
     * @return the major bluetooth device type
     */
    private String getBTClassDevMaj(BluetoothDevice btd) {
        String temp = "";
        if (btd == null)
            return temp;
        if (btd.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.AUDIO_VIDEO)
            temp = "Audio Video, ";
        if (btd.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.COMPUTER)
            temp += "Computer, ";
        if (btd.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.HEALTH)
            temp += "Health, ";
        if (btd.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.MISC)
            temp += "Misc, ";
        if (btd.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.NETWORKING)
            temp += "Networking, ";
        if (btd.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.PERIPHERAL)
            temp += "Peripheral, ";
        if (btd.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.PHONE)
            temp += "Phone, ";
        if (btd.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.UNCATEGORIZED)
            temp += "Uncategorized, ";
        if (btd.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.WEARABLE)
            temp += "Wearable, ";
        if (btd.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.TOY)
            temp += "Toy, ";
        if (btd.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.IMAGING)
            temp += "Imaging, ";

        // trim off the extra comma and space. If the class was not found,
        // return other.
        if (temp.length() >= 3)
            temp = temp.substring(0, temp.length() - 2);
        else
            temp = "other";

        // return device class
        return temp;
    }

    private IBluetooth getIBluetooth() {

        IBluetooth ibta = null;

        try {

            Class<?> c2 = Class.forName("android.os.ServiceManager");

            Method m2 = c2.getDeclaredMethod("getService", String.class);
            IBinder b = (IBinder) m2.invoke(null, "bluetooth");

            Log.d(LOG_TAG, "Test2: " + b.getInterfaceDescriptor());

            Class<?> c3 = Class.forName("android.bluetooth.IBluetooth");

            Class[] s2 = c3.getDeclaredClasses();

            Class<?> c = s2[0];
            // printMethods(c);
            Method m = c.getDeclaredMethod("asInterface", IBinder.class);

            m.setAccessible(true);
            ibta = (IBluetooth) m.invoke(null, b);

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error " + e.getMessage());
        }
        return ibta;
    }

}