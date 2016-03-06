package a2dp.Vol;

import android.app.Notification;
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
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

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
                // TODO Auto-generated catch block
                appInfo = null;
            }
            String appName = (String) (appInfo != null ? pm
                    .getApplicationLabel(appInfo) : pack);

            str += appName + ", ";


            // We have to extract the information from the view
            Notification notification = sbn.getNotification();
            if(notification == null)return;


            Bundle extras = sbn.getNotification().extras;
            str += extras.getCharSequence("android.text").toString();
            String bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString();

            if(bigText.contains("\n")){
                int beginOfLast = 0;

                for(int i = 0; i < bigText.length()-2; i++){
                    if(bigText.indexOf('\n',i) < bigText.lastIndexOf('\n'))beginOfLast = bigText.indexOf('\n',i);
                }
                str += ", " + bigText.substring(beginOfLast).trim();
            }else{
                str += ", " + bigText;
            }


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

    private final BroadcastReceiver reloadprefs = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            LoadPrefs();
        }

    };

}
