package a2dp.Vol;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jim on 1/18/2016.
 * This class replaces the old accessibility service for reading notifications. The old way was
 * deprecated in API 18. This service listens for notifications and sends them to the text reader
 * in service.java to be read out when devices are connected.
 */
public class NotificationCatcher extends NotificationListenerService {

    private static String[] packages;
    private String packagelist;
    private MyApplication application;
    SharedPreferences preferences;

    public NotificationCatcher() {
        super();
    }

    @Override
    public void onCreate() {

        this.application = (MyApplication) this.getApplication();
        preferences = PreferenceManager
                .getDefaultSharedPreferences(this.application);
        IntentFilter reloadmessage = new IntentFilter("a2dp.vol.Access.Reload");
        this.registerReceiver(reloadprefs, reloadmessage);
        LoadPrefs();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(reloadprefs);
        super.onDestroy();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn, RankingMap rankingMap) {
        super.onNotificationPosted(sbn, rankingMap);
        //Toast.makeText(application, "reading notification", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        new Readit().execute(sbn);

    }

    private class Readit extends AsyncTask<StatusBarNotification, Integer, Long> {

        @Override
        protected Long doInBackground(StatusBarNotification... params) {

            StatusBarNotification sbn = params[0];
            // Toast.makeText(application, "reading notification", Toast.LENGTH_LONG).show();
            boolean test = false;
            for (
                    String p
                    : packages)

            {
                if (p.equalsIgnoreCase(sbn.getPackageName())) test = true;
            }

            if (test)

            {
                String str = "";
                ApplicationInfo appInfo;
                PackageManager pm = getPackageManager();
                String pack = sbn.getPackageName();
                try {
                    appInfo = pm.getApplicationInfo(pack, 0);
                } catch (NameNotFoundException e1) {
                    Toast.makeText(application, "problem getting app info", Toast.LENGTH_LONG).show();
                    appInfo = null;
                }
                String appName = (String) (appInfo != null ? pm
                        .getApplicationLabel(appInfo) : pack);

                // add the app name to the string to be read
                str += appName + ", ";

                // abort if we can get the notification
                Notification notification = sbn.getNotification();
                if (notification == null) return null;


                // get the ticker text of this notification and add that to the message string
                String ticker = "";
                if (notification.tickerText != null)
                    ticker = (String) sbn.getNotification().tickerText;

                // get the lines of the notification
                String temp = "";
                Bundle bun = notification.extras;
                if (!bun.isEmpty()) {
                    CharSequence[] lines = bun.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
                    if (lines != null)
                        if (lines.length > 0) {
                            for (CharSequence line : lines) {
                                if (line != null)
                                    if (line.length() > 1) temp = line.toString();
                            }

                        }
                }

                // get the text string to see if there is something in it
                String text = "";
                if (bun.getString(Notification.EXTRA_TEXT) != null) {
                    if (!bun.getString(Notification.EXTRA_TEXT).isEmpty())
                        text = bun.getString(Notification.EXTRA_TEXT);
                }

                // figure out which have valid strings and which we want to communicate
                if (ticker.length() > 1) {
                    if (ticker.equalsIgnoreCase(temp) || temp.length() < 1)
                        str += ticker;
                    else
                        str += ticker + ", " + temp;
                } else if (!text.isEmpty())
                    if (text.equalsIgnoreCase(temp) || temp.isEmpty())
                        str += text;
                    else
                        str += text + ", " + temp;

                //if there is no ticker or strings then ignore it.
                if (temp.isEmpty() && ticker.isEmpty() && text.isEmpty()) return null;

                // make sure something is connected so the text reader is active
                int connected = 0;
                try {
                    connected = a2dp.Vol.service.connects;
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // read out the message by sending it to the service
                if (connected > 0 && str.length() > 0) {
                    final String IRun = "a2dp.vol.service.MESSAGE";
                    Intent intent = new Intent();
                    intent.setAction(IRun);
                    intent.putExtra("message", str);
                    application.sendBroadcast(intent);
                }
            }
            return null;
        }


    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }

    public void LoadPrefs() {

        packagelist = preferences
                .getString("packages",
                        "com.google.android.talk,com.android.email,com.android.calendar");
        packages = packagelist.split(",");

    }

    private final BroadcastReceiver reloadprefs = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            LoadPrefs();
        }

    };

}


