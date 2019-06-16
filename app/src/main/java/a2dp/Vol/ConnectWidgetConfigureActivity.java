package a2dp.Vol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * The configuration screen for the {@link ConnectWidget ConnectWidget} AppWidget.
 */
public class ConnectWidgetConfigureActivity extends Activity {

    private static final String PREFS_NAME = "a2dp.Vol.ConnectWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private static String LOG_TAG = "Connect Widget Config";
    EditText mAppWidgetText;
    public final static String temp[][] = new String[50][2];
    String dname;
    public Context application;

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = ConnectWidgetConfigureActivity.this;

            // When the button is clicked, store the string locally
            String widgetText = mAppWidgetText.getText().toString();
            saveTitlePref(context, mAppWidgetId, widgetText);
            Log.i(LOG_TAG, "Button clicked");

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ConnectWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    public ConnectWidgetConfigureActivity() {
        super();
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void saveTitlePref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadTitlePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        if (titleValue != null) {
            return titleValue;
        } else {
            return context.getString(R.string.appwidget_text);
        }
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        application = getApplication();
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.connect_widget_configure);
        mAppWidgetText = (EditText) findViewById(R.id.appwidget_text);
        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        mAppWidgetText.setText(loadTitlePref(ConnectWidgetConfigureActivity.this, mAppWidgetId));

        config(mAppWidgetId);
    }

    public void config(final int id) {

        BluetoothAdapter mBTA = BluetoothAdapter.getDefaultAdapter();
        if (mBTA == null) {
            Toast.makeText(this, R.string.NobtSupport, Toast.LENGTH_LONG)
                    .show();
            return;
        }
        // If Bluetooth is not yet enabled, enable it
        if (!mBTA.isEnabled()) {
            Toast.makeText(application, R.string.btNotOn, Toast.LENGTH_LONG)
                    .show();
            this.finish();
            /*
             * Intent enableBluetooth = new Intent(
             * BluetoothAdapter.ACTION_REQUEST_ENABLE); try {
             * startActivity(enableBluetooth); } catch (Exception e) {
             * e.printStackTrace(); } // Now implement the onActivityResult()
             * and wait for it to // be invoked with ENABLE_BLUETOOTH //
             * onActivityResult(ENABLE_BLUETOOTH, result, enableBluetooth);
             */
            return;
        }
        // Toast.makeText(this, "Bluetooth", Toast.LENGTH_LONG).show();
 /*       if (!receiver_registered) {
            IntentFilter f1 = new IntentFilter(Bt_iadl.NameFilter);
            application.registerReceiver(receiver1, f1);
            receiver_registered = true;
        }*/
        createList();
    }

    void createList() {
        int i = 0;
        BluetoothAdapter mBTA = BluetoothAdapter.getDefaultAdapter();

        if (mBTA != null) {
            Set<BluetoothDevice> pairedDevices = mBTA.getBondedDevices();
            // If there are paired devices
            if (pairedDevices.size() > 0) {
                // Loop through paired devices
                for (BluetoothDevice device : pairedDevices) {
                    String dname = device.getName();

                    try {
                        Method m = device.getClass().getMethod("getAliasName");
                        Object res = m.invoke(device);
                        if (res != null)
                            dname = res.toString();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    if (dname == null)
                        dname = device.getName();
                    temp[i][0] = dname;
                    temp[i][1] = device.getAddress();
                    if (i > 48)
                        break;
                    i++;
                }
            }
        }

        String[] lstring = new String[i];
        for (int j = 0; j < i; j++) {
            lstring[j] = temp[j][0] + " - " + temp[j][1];
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.BuilderTitle);
        builder.setItems(lstring, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                // Use MODE_WORLD_READABLE and/or MODE_WORLD_WRITEABLE to grant
                // access to other applications
                SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = preferences.edit();
                String ws = String.valueOf(mAppWidgetId);
                editor.putString(PREF_PREFIX_KEY + ws, temp[item][1]);
                dname = temp[item][0];
                editor.putString(PREF_PREFIX_KEY + ws + "_name", dname);
                editor.commit();

                done();
            }
        });
        builder.show();
    }

    void done() {
        AppWidgetManager appWidgetManager = AppWidgetManager
                .getInstance(application);

        Intent intent = new Intent(application, Connector.class);
        intent.putExtra(PREF_PREFIX_KEY + "ID", mAppWidgetId);
        PendingIntent pendingIntent = PendingIntent.getService(application,
                mAppWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get the layout for the App Widget and attach an on-click listener
        // to the button
        RemoteViews views = new RemoteViews(application.getPackageName(),
                R.layout.connect_widget);
        views.setOnClickPendingIntent(R.id.WidgetButton, pendingIntent);

        views.setTextViewText(R.id.WidgetButton, dname);
        appWidgetManager.updateAppWidget(mAppWidgetId, views);
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        // resultValue.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        setResult(RESULT_OK, resultValue);
        Log.i(LOG_TAG, "app widget manager configured " + dname);
        finish();
    }
}

