package a2dp.Vol;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.List;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

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
    private static final String LOG_TAG = "A2DP_Volume";
    private DeviceDB DB; // database of device data stored in SQlite
    btDevice btdConn;
    Long dtime = null;
    Location l = null; // the most recent location
    Location l3 = null; // the most accurate location
    Location l4 = null; // the best location
    boolean gpsEnabled = false;
    int formatFlags;
    int formatFlags2;


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
            Toast.makeText(this, "prefs failed to load. " + e.getMessage(),
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
        } catch (Exception e) {
            Toast.makeText(this, "Location service failed to start. " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            this.stopSelf();
            e.printStackTrace();
        }


        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);

        dtime = System.currentTimeMillis(); // catch the time we disconnected

        // spawn the location listeners
        registerListeners();
        // start the timer
        if (MAX_TIME > 0) {
            CountDownTimer T;
            T = new CountDownTimer(MAX_TIME, 5000) {

                public void onTick(long millisUntilFinished) {
                    if (toasts)
                        Toast.makeText(
                                application,
                                "Time left: " + (millisUntilFinished + 20)
                                        / 1000, Toast.LENGTH_LONG).show();
                }

                public void onFinish() {
                    clearLoc(true);
                }
            };
            T.start();
        }


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
    }

    /* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
    @Override
    public void onDestroy() {
        this.DB.getDb().close();
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
        if (l3 != null)
            if (l3.hasAccuracy())
                oldacc = l3.getAccuracy();
        if (l != null)
            olddt = System.currentTimeMillis() - l.getTime();

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
                        }
                    }
                }
            } else
                return; // if no location data just abort here


            // If we have a good location, turn OFF the gps listener.
            if (locationListener != null && l4 != null) {
                float x = l4.getAccuracy();
                if (x < MAX_ACC
                        && x > 0
                        && (System.currentTimeMillis() - l4.getTime()) < MAX_TIME)
                    clearLoc(true);
            }

        } catch (Exception e1) {
            return;
        }

        DecimalFormat df = new DecimalFormat("#.#");
        // figure out which device we are disconnecting from
        if (btdConn != null)
            car = btdConn.getDesc2();

        String locTime = "";
        // store the best location
        if (l4 != null) {
            locTime = DateUtils.formatDateTime(application, l4.getTime(),
                    formatFlags);
            String urlStr;
            try {
                urlStr = URLEncoder.encode(
                        l4.getLatitude() + "," + l4.getLongitude() + "(" + car
                                + " " + locTime + " acc="
                                + df.format(l4.getAccuracy()) + ")", "UTF-8");
            } catch (UnsupportedEncodingException e1) {
                urlStr = URLEncoder.encode(l4.getLatitude() + ","
                        + l4.getLongitude() + "(" + car + " " + locTime
                        + " acc=" + df.format(l4.getAccuracy()) + ")");
                e1.printStackTrace();
            }
            try {
                FileOutputStream fos = openFileOutput("My_Last_Location",
                        Context.MODE_WORLD_READABLE);
                String temp = "http://maps.google.com/maps?q=" + urlStr;
                fos.write(temp.getBytes());
                fos.close();
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
            locTime = DateUtils.formatDateTime(application, l3.getTime(),
                    formatFlags);
            String urlStr;
            try {
                urlStr = URLEncoder.encode(
                        l3.getLatitude() + "," + l3.getLongitude() + "(" + car
                                + " " + locTime + " acc="
                                + df.format(l3.getAccuracy()) + ")", "UTF-8");
            } catch (UnsupportedEncodingException e1) {
                urlStr = URLEncoder.encode(l3.getLatitude() + ","
                        + l3.getLongitude() + "(" + car + " " + locTime
                        + " acc=" + df.format(l3.getAccuracy()) + ")");
                e1.printStackTrace();
            }
            try {
                FileOutputStream fos = openFileOutput("My_Last_Location2",
                        Context.MODE_WORLD_READABLE);
                String temp = "http://maps.google.com/maps?q=" + urlStr;
                fos.write(temp.getBytes());
                fos.close();
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
    private void clearLoc(boolean doGps) {
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

        String car = "My Car";
        DecimalFormat df = new DecimalFormat("#.#");
        // figure out which device we are disconnecting from
        if (btdConn != null)
            car = btdConn.getDesc2();

        String locTime = "";
        // store this vehicles location
        if (true) {
            try {
                File exportDir = new File(a2dpDir);

                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }
                File file = new File(exportDir, car.replaceAll(" ", "_")
                        + ".html");

                String temp = null;

                if (l4 != null) {
                    locTime = DateUtils.formatDateTime(application,
                            l4.getTime(), formatFlags);
                    String urlStr;
                    try {
                        urlStr = URLEncoder.encode(
                                l4.getLatitude() + "," + l4.getLongitude()
                                        + "(" + car + " " + locTime + " acc="
                                        + df.format(l4.getAccuracy()) + ")",
                                "UTF-8");
                    } catch (Exception e) {
                        urlStr = URLEncoder.encode(l4.getLatitude() + ","
                                + l4.getLongitude() + "(" + car + " " + locTime
                                + " acc=" + df.format(l4.getAccuracy()) + ")");
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
                            + "<br>" + "Longitude: " + l4.getLongitude();
                } else {

                    locTime = DateUtils.formatDateTime(application, dtime,
                            formatFlags);
                    temp = "No Best Location Captured " + locTime + "<br>";
                }
                if (l3 != null) {
                    locTime = DateUtils.formatDateTime(application,
                            l3.getTime(), formatFlags);
                    String urlStr;
                    try {
                        urlStr = URLEncoder.encode(
                                l3.getLatitude() + "," + l3.getLongitude()
                                        + "(" + car + " " + locTime + " acc="
                                        + df.format(l3.getAccuracy()) + ")",
                                "UTF-8");
                    } catch (Exception e) {
                        urlStr = URLEncoder.encode(l3.getLatitude() + ","
                                + l3.getLongitude() + "(" + car + " " + locTime
                                + " acc=" + df.format(l3.getAccuracy()) + ")");
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
                } else {
                    locTime = DateUtils.formatDateTime(application, dtime,
                            formatFlags);
                    temp += "No Most Accurate Location Captured " + locTime
                            + "<br>";
                }
                if (l != null) {
                    locTime = DateUtils.formatDateTime(application,
                            l.getTime(), formatFlags);
                    String urlStr;
                    try {
                        urlStr = URLEncoder.encode(
                                l.getLatitude() + "," + l.getLongitude() + "("
                                        + car + " " + locTime + " acc="
                                        + df.format(l.getAccuracy()) + ")",
                                "UTF-8");
                    } catch (Exception e) {
                        urlStr = URLEncoder.encode(l.getLatitude() + ","
                                + l.getLongitude() + "(" + car + " " + locTime
                                + " acc=" + df.format(l.getAccuracy()) + ")");
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
                } else {
                    locTime = DateUtils.formatDateTime(application, dtime,
                            formatFlags);
                    temp += "No Most Recent Location Captured " + locTime
                            + "<br>";
                }

                if (!gpsEnabled)
                    temp += "<br>GPS was not enabled";

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
        }
        // reset all the location variables
        l = null; // the most recent location
        l3 = null; // the most accurate location
        l4 = null; // the best location
        btdConn = null;
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
}
