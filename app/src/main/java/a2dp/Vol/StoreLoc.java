package a2dp.Vol;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import static a2dp.Vol.R.drawable.ic_launcher;

public class StoreLoc extends Service {


    public static final String PREFS_NAME = "btVol";
    float MAX_ACC = 20; // worst acceptable location in meters
    long MAX_TIME = 10000; // gps listener timout time in milliseconds and
    // oldest acceptable time
    SharedPreferences preferences;
    private MyApplication application;
    private LocationManager locationManager;
    private boolean toasts = true;
    private boolean usePass = false;
    private boolean useNet = true;
    String a2dpDir = "";
    boolean local;
    String car;
    private static final String LOG_TAG = "A2DP_Volume_StoreLoc";
    private DeviceDB DB; // database of device data stored in SQlite
    btDevice btdConn;
    Long dtime = null;
    Location l = null, lstore = null; // the most recent location
    Location l3 = null, l3store = null; // the most accurate location
    Location l4 = null, l4store = null; // the best location
    boolean gpsEnabled = false;
    int formatFlags;
    int formatFlags2;
    private NotificationManager mNotificationManager = null;
    private NotificationManagerCompat notificationManagerCompat = null;
    private static final String A2DP_STORLOC = "a2dp_storeloc_foreground";
    NotificationChannel channel_fs;
    Notification not;
    NotificationCompat.Builder mCBuilder;
    int grabLocationCount = 0;


    /**
     * @see android.app.Service#onBind(Intent)
     */
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Put your code here
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Service#onStart(android.content.Intent, int)
     */

    /*
     * (non-Javadoc)
     *
     * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            preferences = PreferenceManager
                    .getDefaultSharedPreferences(application);
            toasts = preferences.getBoolean("toasts", true);
            usePass = preferences.getBoolean("usePassive", false);
            useNet = preferences.getBoolean("useNetwork", true);

            Long yyy = Long.valueOf(preferences.getString("gpsTime", "15000"));
            MAX_TIME = yyy;

            Float xxx = Float.valueOf(preferences.getString("gpsDistance", "10"));
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
            car = "My Car";
            Toast.makeText(this, "prefs failed to load. " + e.getLocalizedMessage(),
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
            Log.e(LOG_TAG, "prefs failed to load " + e.getMessage());
        }
        l = null; // the most recent location
        l3 = null; // the most accurate location
        l4 = null; // the best location
        // get the device that just disconnected
        String device;
        try {
            device = intent.getStringExtra("device");
            btdConn = DB.getBTD(device);
            if (btdConn != null)
                car = btdConn.getDesc2();
        } catch (Exception e) {
            Toast.makeText(this, "Location service failed to start. " + e.getLocalizedMessage(),
                    Toast.LENGTH_LONG).show();
            Log.i(LOG_TAG, "Location service failed to start: " + e.getMessage());
            this.stopSelf();
            e.printStackTrace();
        }
        if (car == null || car.isEmpty()) car = "My Car";

        Log.i(LOG_TAG, "Storing location for " + car);

        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);

        dtime = System.currentTimeMillis(); // catch the time we disconnected

        // spawn the location listeners
        registerListeners();

        if ((channel_fs == null)) {
            createNotificationChannel();
        }
        //not = null;
        Intent notificationIntent = new Intent(this, main.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, 0);


        notificationManagerCompat.cancelAll();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mCBuilder = new NotificationCompat.Builder(application, A2DP_STORLOC)
                    .setContentTitle(
                            getString(R.string.storing_location))
                    .setContentIntent(contentIntent)
                    .setSmallIcon(ic_launcher)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setContentText(car)
                    .setProgress(100, 0, false)
                    .setChannelId(A2DP_STORLOC);
            not = mCBuilder.build();
            notificationManagerCompat.notify(1, not);
            //Toast.makeText(application, "Test off " + car + " " +not.getChannelId(), Toast.LENGTH_LONG).show();
        } else {
            mCBuilder = new NotificationCompat.Builder(application, A2DP_STORLOC)
                    .setContentTitle(getString(R.string.storing_location))
                    .setContentIntent(contentIntent)
                    .setSmallIcon(ic_launcher)
                    .setContentText(car)
                    .setProgress((int) MAX_TIME, 0, false);
            not = mCBuilder.build();
            notificationManagerCompat.notify(1, not);
        }
        this.startForeground(1, not);


        // start the timer
        if (MAX_TIME > 0) {
            CountDownTimer T;
            T = new CountDownTimer(MAX_TIME, 2000) {

                public void onTick(long millisUntilFinished) {
                    int prog = (int) (MAX_TIME - millisUntilFinished);
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                        mCBuilder.setProgress((int) MAX_TIME, prog, false);
                        notificationManagerCompat.notify(1, mCBuilder.build());
                    } else {
                        mCBuilder.setProgress((int) MAX_TIME, prog, false);
                        notificationManagerCompat.notify(1, mCBuilder.build());
                    }

                }

                public void onFinish() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                        mCBuilder.setProgress(0, 0, true);
                        notificationManagerCompat.notify(1, mCBuilder.build());
                    } else {
                        mCBuilder.setProgress(0, 0, true);
                        notificationManagerCompat.notify(1, mCBuilder.build());
                    }
                    clearLoc();
                }
            };

            T.start();
        }

        grabLocationCount = 0;

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * @see android.app.Service#onCreate()
     */
    @Override
    public void onCreate() {
        this.application = (MyApplication) this.getApplication();
        // open database instance
        this.DB = new DeviceDB(application);
        formatFlags = DateUtils.FORMAT_ABBREV_ALL;
        formatFlags |= DateUtils.FORMAT_SHOW_DATE;
        formatFlags |= DateUtils.FORMAT_SHOW_TIME;
        formatFlags |= DateUtils.FORMAT_SHOW_YEAR;

        if ((channel_fs == null)) {
            createNotificationChannel();
        }
    }

    /* (non-Javadoc)
     * @see android.app.Service#onDestroy()
     */
    @Override
    public void onDestroy() {
        this.DB.getDb().close();
        this.stopForeground(true);
        if (locationListener != null) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.removeUpdates(locationListener);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    /* (non-Javadoc)
     * @see android.app.Service#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        this.DB.getDb().close();
        this.stopForeground(true);
        notificationManagerCompat.cancelAll();

        if (locationListener != null)
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.removeUpdates(locationListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        super.finalize();
    }

    // finds the most recent and most accurate locations
    // get the location and write it to a file.
    void grabGPS() {

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = Objects.requireNonNull(lm).getProviders(true);

        long deltat = 9999999;
        long olddt = 9999999;
        float oldacc = 99999999;
        float bestacc = 99999999;
        Location l2 = null; // the temporary last known location

        if (l4 != null)
            if (l4.hasAccuracy()) {
                bestacc = l4.getAccuracy();
                if(l4store == null) l4store = l4;
            }
        if (l3 != null)
            if (l3.hasAccuracy()) {
                oldacc = l3.getAccuracy();
                if(l3store == null)l3store = l3;
            }
        if (l != null) {
            olddt = System.currentTimeMillis() - l.getTime();
            if(lstore == null)lstore = l;
        }
        try {

            if (!providers.isEmpty()) {
                for (int i = providers.size() - 1; i >= 0; i--) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    l2 = lm.getLastKnownLocation(providers.get(i));

                    if (l2 != null) {
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
                                l4 = l2; // the best sample since MAX_TIME before
                                // disconnect
                                bestacc = acc;
                            }

                        }
                        olddt = deltat;
                        deltat = System.currentTimeMillis() - l2.getTime();
                        if (deltat < olddt) // get the most recent update
                        {
                            l = l2;
                            lstore = l;
                        }
                    }
                }
            } else {
                Log.i(LOG_TAG, "No location data available ");
                return; // if no location data just abort here
            }

            // If we have a good location, turn OFF the gps listener.
            if (locationListener != null && l4 != null) {
                float x = l4.getAccuracy();
                if (x < MAX_ACC
                        && x > 0
                        && (System.currentTimeMillis() - l4.getTime()) < MAX_TIME)
                    clearLoc();
            }

        } catch (Exception e1) {
            return;
        }

        DecimalFormat df = new DecimalFormat("#.#");

        String locTime = "";
        // store the best location
        if (l4 != null) {
            l4store = l4;
            locTime = DateUtils.formatDateTime(application, l4.getTime(),
                    formatFlags);
            String urlStr;
            try {
                urlStr = URLEncoder.encode(
                        l4.getLatitude() + "," + l4.getLongitude() + "(" + car
                                + " " + locTime + " acc="
                                + df.format(l4.getAccuracy()) + ")", "UTF-8");
            } catch (UnsupportedEncodingException e1) {
                urlStr = "";
                /*urlStr = URLEncoder.encode(l4.getLatitude() + ","
                        + l4.getLongitude() + "(" + car + " " + locTime
                        + " acc=" + df.format(l4.getAccuracy()) + ")");*/
                e1.printStackTrace();
            }
            try {
                FileOutputStream fos = openFileOutput("My_Last_Location",
                        Context.MODE_PRIVATE);
                String temp = "http://maps.google.com/maps?q=" + urlStr;
                fos.write(temp.getBytes());
                fos.close();
                grabLocationCount++; // count stored locations
                // Toast.makeText(a2dp.Vol.service.this, temp,
                // Toast.LENGTH_LONG).show();
            } catch (FileNotFoundException e) {
                Toast.makeText(application, "FileNotFound", Toast.LENGTH_LONG)
                        .show();
                e.printStackTrace();
            } catch (IOException e) {
                Toast.makeText(application, "IOException", Toast.LENGTH_LONG)
                        .show();
                e.printStackTrace();
            }
        }

        // store most accurate location
        if (l3 != null) {
            l3store = l3;
            locTime = DateUtils.formatDateTime(application, l3.getTime(),
                    formatFlags);
            String urlStr;
            try {
                urlStr = URLEncoder.encode(
                        l3.getLatitude() + "," + l3.getLongitude() + "(" + car
                                + " " + locTime + " acc="
                                + df.format(l3.getAccuracy()) + ")", "UTF-8");
            } catch (UnsupportedEncodingException e1) {
                urlStr = (l3.getLatitude() + ","
                        + l3.getLongitude() + "(" + car + " " + locTime
                        + " acc=" + df.format(l3.getAccuracy()) + ")");
                e1.printStackTrace();
            }
            try {
                FileOutputStream fos = openFileOutput("My_Last_Location2",
                        Context.MODE_PRIVATE);
                String temp = "http://maps.google.com/maps?q=" + urlStr;
                fos.write(temp.getBytes());
                fos.close();
                grabLocationCount++; // count stored locations
                // Toast.makeText(a2dp.Vol.service.this, temp,
                // Toast.LENGTH_LONG).show();
            } catch (FileNotFoundException e) {
                Toast.makeText(application, "FileNotFound", Toast.LENGTH_LONG)
                        .show();
                e.printStackTrace();
            } catch (IOException e) {
                Toast.makeText(application, "IOException", Toast.LENGTH_LONG)
                        .show();
                e.printStackTrace();
            }
        }
    }

    // Define a listener that responds to location updates
    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the gps location
            // provider.
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
    private void clearLoc() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(locationListener);

        //String car = "My Car";
        DecimalFormat df = new DecimalFormat("#.#");

        String locTime = "";
        // store this vehicles location
        try {
            File exportDir = new File(a2dpDir);

            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            File file = new File(exportDir, car.replaceAll(" ", "_")
                    + ".html");

            String temp = null;

            if (l4store != null) {
                locTime = DateUtils.formatDateTime(application,
                        l4store.getTime(), formatFlags);
                String urlStr;
                try {
                    urlStr = URLEncoder.encode(
                            l4store.getLatitude() + "," + l4store.getLongitude()
                                    + "(" + car + " " + locTime + " acc="
                                    + df.format(l4store.getAccuracy()) + ")",
                            "UTF-8");
                } catch (Exception e) {
                    urlStr = (l4store.getLatitude() + ","
                            + l4store.getLongitude() + "(" + car + " " + locTime
                            + " acc=" + df.format(l4store.getAccuracy()) + ")");
                    e.printStackTrace();
                }

                temp = "<hr /><bold><a href=\"http://maps.google.com/maps?q="
                        + urlStr
                        + "\">"
                        + car
                        + "</a></bold> Best Location<br>Time: "
                        + locTime
                        + "<br>"
                        + "Location type: "
                        + l4store.getProvider()
                        + "<br>"
                        + "Accuracy: "
                        + l4store.getAccuracy()
                        + " meters<br>"
                        + "Elevation: "
                        + l4store.getAltitude()
                        + " meters<br>"
                        + "Lattitude: "
                        + l4store.getLatitude()
                        + "<br>" + "Longitude: " + l4store.getLongitude();
            } else {

                locTime = DateUtils.formatDateTime(application, dtime,
                        formatFlags);
                temp = "<hr />No Best Location Captured " + locTime + "<br>";
            }
            if (l3store != null) {
                locTime = DateUtils.formatDateTime(application,
                        l3store.getTime(), formatFlags);
                String urlStr;
                try {
                    urlStr = URLEncoder.encode(
                            l3store.getLatitude() + "," + l3store.getLongitude()
                                    + "(" + car + " " + locTime + " acc="
                                    + df.format(l3store.getAccuracy()) + ")",
                            "UTF-8");
                } catch (Exception e) {
                    urlStr = (l3store.getLatitude() + ","
                            + l3store.getLongitude() + "(" + car + " " + locTime
                            + " acc=" + df.format(l3store.getAccuracy()) + ")");
                    e.printStackTrace();
                }

                temp += "<hr /><bold><a href=\"http://maps.google.com/maps?q="
                        + urlStr
                        + "\">"
                        + car
                        + "</a></bold> Most Accurate Location<br>Time: "
                        + locTime
                        + "<br>"
                        + "Location type: "
                        + l3store.getProvider()
                        + "<br>"
                        + "Accuracy: "
                        + l3store.getAccuracy()
                        + " meters<br>"
                        + "Elevation: "
                        + l3store.getAltitude()
                        + " meters<br>"
                        + "Lattitude: "
                        + l3store.getLatitude()
                        + "<br>"
                        + "Longitude: "
                        + l3store.getLongitude();
            } else {
                locTime = DateUtils.formatDateTime(application, dtime,
                        formatFlags);
                temp += "<hr />No Most Accurate Location Captured " + locTime
                        + "<br>";
            }
            if (lstore != null) {
                locTime = DateUtils.formatDateTime(application,
                        lstore.getTime(), formatFlags);
                String urlStr;
                try {
                    urlStr = URLEncoder.encode(
                            lstore.getLatitude() + "," + lstore.getLongitude() + "("
                                    + car + " " + locTime + " acc="
                                    + df.format(lstore.getAccuracy()) + ")",
                            "UTF-8");
                } catch (Exception e) {
                    urlStr = (lstore.getLatitude() + ","
                            + lstore.getLongitude() + "(" + car + " " + locTime
                            + " acc=" + df.format(lstore.getAccuracy()) + ")");
                    e.printStackTrace();
                }

                temp += "<hr /><bold><a href=\"http://maps.google.com/maps?q="
                        + urlStr
                        + "\">"
                        + car
                        + "</a></bold> Most Recent Location<br>Time: "
                        + locTime
                        + "<br>"
                        + "Location type: "
                        + lstore.getProvider()
                        + "<br>"
                        + "Accuracy: "
                        + lstore.getAccuracy()
                        + " meters<br>"
                        + "Elevation: "
                        + lstore.getAltitude()
                        + " meters<br>"
                        + "Lattitude: "
                        + lstore.getLatitude()
                        + "<br>"
                        + "Longitude: "
                        + lstore.getLongitude();
            } else {
                locTime = DateUtils.formatDateTime(application, dtime,
                        formatFlags);
                temp += "<hr />No Most Recent Location Captured " + locTime
                        + "<br>";
            }

            if (!gpsEnabled)
                temp += "<br>GPS was not enabled";

            if (local) {
                FileOutputStream fos = openFileOutput(file.getName(),
                        Context.MODE_PRIVATE);
                fos.write(temp.getBytes());
                fos.close();
            } else {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(temp.getBytes());
                fos.close();
            }

        } catch (FileNotFoundException e) {
            Toast.makeText(application, "FileNotFound", Toast.LENGTH_LONG)
                    .show();
            e.printStackTrace();
            Log.e(LOG_TAG, "Error " + e.getMessage());
        } catch (IOException e) {
            Toast.makeText(application, "IOException", Toast.LENGTH_LONG)
                    .show();
            e.printStackTrace();
            Log.e(LOG_TAG, "Error " + e.getMessage());
        }

        // make sure we store a location for the widget and button
        if (grabLocationCount < 1) {

            // Try all the locations to find a valid one, store best if possible
            Location mlocation = l;
            if (l3 != null && l3.hasAccuracy()) mlocation = l3;
            if (l4 != null && l4.hasAccuracy()) mlocation = l4;

            // as long as we have any location, store it
            if (mlocation != null) {
                locTime = DateUtils.formatDateTime(application, mlocation.getTime(),
                        formatFlags);
                String urlStr;
                try {
                    urlStr = URLEncoder.encode(
                            mlocation.getLatitude() + "," + l3.getLongitude() + "(" + car
                                    + " " + locTime + " acc="
                                    + df.format(l3.getAccuracy()) + ")", "UTF-8");
                } catch (UnsupportedEncodingException e1) {
                    urlStr = (mlocation.getLatitude() + ","
                            + mlocation.getLongitude() + "(" + car + " " + locTime
                            + " acc=" + df.format(l3.getAccuracy()) + ")");
                    e1.printStackTrace();
                }
                try {
                    FileOutputStream fos = openFileOutput("My_Last_Location",
                            Context.MODE_PRIVATE);
                    String temp = "http://maps.google.com/maps?q=" + urlStr;
                    fos.write(temp.getBytes());
                    fos.close();
                    grabLocationCount++; // count stored locations
                    // Toast.makeText(a2dp.Vol.service.this, temp,
                    // Toast.LENGTH_LONG).show();
                } catch (FileNotFoundException e) {
                    Toast.makeText(application, "FileNotFound", Toast.LENGTH_LONG)
                            .show();
                    e.printStackTrace();
                } catch (IOException e) {
                    Toast.makeText(application, "IOException", Toast.LENGTH_LONG)
                            .show();
                    e.printStackTrace();
                }
            }
        }

        // reset all the location variables
        l = null; // the most recent location
        l3 = null; // the most accurate location
        l4 = null; // the best location
        btdConn = null;

        notificationManagerCompat.cancel(3);

        // Tell the world we are done capturing location
        final String done = "a2dp.vol.service.NOTIFY";
        Intent i = new Intent();
        i.setAction(done);
        application.sendBroadcast(i);

        Log.i(LOG_TAG, "Location capture done");

        this.stopForeground(true);
        // capture complete, close
        this.stopSelf();
    }

    private void registerListeners() {
        // start location provider GPS
        // Register the listener with the Location Manager to
        // receive location updates

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            gpsEnabled = true;
        } else
            gpsEnabled = false;
        if (useNet
                && locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }
        if (usePass
                && locationManager
                .isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            locationManager.requestLocationUpdates(
                    LocationManager.PASSIVE_PROVIDER, 0, 0, locationListener);
        }

    }

    private void createNotificationChannel() {
        mNotificationManager = getSystemService(NotificationManager.class);
        notificationManagerCompat = NotificationManagerCompat.from(application);

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // set up foreground notification channel
            CharSequence cname = getString(R.string.storeloc_channel_name);
            String desc = getString(R.string.storeloc_channel_desc);
            int importance2 = NotificationManager.IMPORTANCE_LOW;
            channel_fs = new NotificationChannel(A2DP_STORLOC, cname, importance2);
            channel_fs.setDescription(desc);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            mNotificationManager.createNotificationChannel(channel_fs);
        }
    }

    // make sure icon is valid
    private int checkIcon(int icon) {
        ArrayList<Integer> icons = new ArrayList<>();
        icons.add(R.drawable.car2);
        icons.add(R.drawable.headset);
        icons.add(R.drawable.ic_launcher);
        icons.add(R.drawable.icon5);
        icons.add(R.drawable.usb);
        icons.add(R.drawable.jack);

        if (icons.contains(icon)) {
            return icon;
        } else {
            return R.drawable.ic_launcher;
        }
    }

}
