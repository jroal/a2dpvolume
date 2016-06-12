package a2dp.Vol;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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

        // Toast.makeText(application, "reading notification", Toast.LENGTH_LONG).show();
        boolean test = false;
        for (String p : packages) {
            if (p.equalsIgnoreCase(sbn.getPackageName())) test = true;
        }

        if (test) {
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

            str += appName + ", ";

            // We have to extract the information from the view
            Notification notification = sbn.getNotification();
            if (notification == null) return;

            if(sbn.getNotification().tickerText != null)str += sbn.getNotification().tickerText;
            if(sbn.getNotification().extras.get(Notification.EXTRA_BIG_TEXT) != null)
                str += ", big: " + sbn.getNotification().extras.get(Notification.EXTRA_BIG_TEXT);
            if(sbn.getNotification().extras.get(Notification.EXTRA_TEXT) != null)
                str += ", reg: " + sbn.getNotification().extras.get(Notification.EXTRA_TEXT);
            if(sbn.getNotification().extras.get(Notification.EXTRA_SUMMARY_TEXT) != null)
                str += ", sum: " + sbn.getNotification().extras.get(Notification.EXTRA_SUMMARY_TEXT);
            if(sbn.getNotification().extras.get(Notification.EXTRA_TEXT_LINES) != null)
                str += ", lines: " + sbn.getNotification().extras.get(Notification.EXTRA_TEXT_LINES);


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
                this.sendBroadcast(intent);
            }
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

    private String get_message(Notification notification) {
        Boolean got_name = false;
        String str = "";
        String name = "";

        // We have to extract the information from the view

        if (notification == null) return "I got nothing";
        RemoteViews views = notification.bigContentView;
        if (views == null)
            views = notification.contentView;
        //if (views == null) return null;

        // Use reflection to examine the m_actions member of the given RemoteViews object.
        // It's not pretty, but it works.
        List<String> text = new ArrayList<String>();
        try {
            java.lang.reflect.Field field = views.getClass()
                    .getDeclaredField("mActions");
            field.setAccessible(true);

            @SuppressWarnings("unchecked")
            ArrayList<Parcelable> actions = (ArrayList<Parcelable>) field
                    .get(views);

            // Find the setText() and setTime() reflection actions
            for (Parcelable p : actions) {
                Parcel parcel = Parcel.obtain();
                p.writeToParcel(parcel, 0);
                parcel.setDataPosition(0);

                // The tag tells which type of action it is (2 is ReflectionAction, from the source)
                int tag = parcel.readInt();
                if (tag != 2)
                    continue;

                // View ID
                parcel.readInt();

                String methodName = parcel.readString();
                if (methodName == null)
                    continue;

                    // Save strings
                else if (methodName.equals("setText")) {
                    // Parameter type (10 = Character Sequence)
                    parcel.readInt();

                    // Store the actual string
                    String t = TextUtils.CHAR_SEQUENCE_CREATOR
                            .createFromParcel(parcel).toString().trim();
                    text.add(t);

                }

                // Save times. Comment this section out if the notification time isn't important
                else if (methodName.equals("setTime")) {
                    // Parameter type (5 = Long)
/*			                parcel.readInt();

			                String t = new SimpleDateFormat("h:mm a").format(new Date(parcel.readLong()));
			                text.add(t);*/
                }

                parcel.recycle();
            }
        }

        // It's not usually good style to do this, but then again, neither is the use of reflection...
        catch (Exception e) {
            //Log.e("NotificationClassifier", e.toString());
        }

        if (text.size() > 0) {
            // if the message has returns we only want the last string (Hangouts fix)
            String temp = text.get(text.size() - 1);
            if (temp.indexOf("\n") > -1)
                str += name + ", "
                        + temp.substring(temp.lastIndexOf("\n"));
            else
                str += name + ", " + temp;
        } else {

            Bundle extras = notification.extras;
            String title = extras.getString("android.title");
            String message = extras.getCharSequence("android.text").toString();
            //String ticker = extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString();
            str += ", " + title + ", " + message;
        }


        return str;
    }

    private final BroadcastReceiver reloadprefs = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            LoadPrefs();
        }

    };

}


