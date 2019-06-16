package a2dp.Vol;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link ConnectWidgetConfigureActivity ConnectWidgetConfigureActivity}
 */
public class ConnectWidget extends AppWidgetProvider {
    private static final String LOG_TAG = "Connect_widget";
    private static final String PREFS_NAME = "a2dp.Vol.ConnectWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = ConnectWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.connect_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
 /*       for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }*/


        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);
        // Perform this loop procedure for each App Widget that belongs to this
        // provider

        for (int appWidgetId : appWidgetIds) {
            // Create an Intent to launch
            Intent intent = new Intent(context, Connector.class);
            intent.putExtra(PREF_PREFIX_KEY + "ID", appWidgetId);
            PendingIntent pendingIntent;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                pendingIntent = PendingIntent.getService(context,
                        appWidgetId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            } else {
                pendingIntent = PendingIntent.getForegroundService(context,
                        appWidgetId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            }


            Log.i(LOG_TAG, "Widget ID = " + appWidgetId);
            // Get the layout for the App Widget and attach an on-click listener
            // to the button

            RemoteViews views = new RemoteViews(context.getPackageName(),
                    R.layout.connect_widget);
            views.setOnClickPendingIntent(R.id.WidgetButton, pendingIntent);
            String WidgetId = String.valueOf(appWidgetId);
            String bt_mac = preferences.getString(PREF_PREFIX_KEY + WidgetId, "O");
            String dname = preferences.getString(PREF_PREFIX_KEY + WidgetId + "_name", "Connect " + appWidgetId);
            views.setTextViewText(R.id.WidgetButton, dname);

            // Tell the AppWidgetManager to perform an update on the current App
            // Widget
            appWidgetManager.updateAppWidget(appWidgetId, views);

        }

    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            ConnectWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

