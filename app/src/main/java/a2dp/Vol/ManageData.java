package a2dp.Vol;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

/**
 * @author Leveraged from an example, modified by Jim Roal This activity manages
 *         the database used to store devices
 */
public class ManageData extends Activity {

    @Override
    public void finish() {
        Intent i = new Intent();
        this.setResult(Activity.RESULT_OK, i);
        super.finish();
    }

    String a2dpDir;
    private DeviceDB myDB; // database of device data stored in SQlite

    @Override
    protected void onDestroy() {
        Intent i = new Intent();
        this.setResult(Activity.RESULT_OK, i);
        super.onDestroy();
    }

    private MyApplication application;

    private Button exportDbToSdButton;
    private Button exportDbXmlToSdButton;
    private Button importDB;
    private Button exportLoc;
    private Button pushNamesButton;
    private TextView output = (TextView) null;
    private TextView path = (TextView) null;
    private String pathstr;
    private final int MY_PERMISSIONS = 4327;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.managedata);

        // 1. Always initialize application reference first to prevent NPEs
        this.application = (MyApplication) this.getApplication();

        // Show the Up button in the action bar.
        setupActionBar();

        // 2. Bind all UI components unconditionally before binding listeners or changing states
        this.output = findViewById(R.id.Output);
        this.path = findViewById(R.id.Path);
        this.pushNamesButton = this.findViewById(R.id.pushNamesButton);
        this.exportDbToSdButton = this.findViewById(R.id.exportdbtosdbutton);
        this.exportDbXmlToSdButton = this.findViewById(R.id.exportdbxmltosdbutton);
        this.importDB = this.findViewById(R.id.ImportDBButton);
        this.exportLoc = this.findViewById(R.id.ExportLoc);

        // Populate initial database view
        new SelectDataTask().execute();

        // Internal feature listener: Bluetooth alias push operates independently of storage state
        this.pushNamesButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Vector<btDevice> vec = new Vector<btDevice>();
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

                try {
                    myDB = new DeviceDB(application);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    vec = myDB.selectAlldb();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                int count = 0;
                for (btDevice bt : vec) {
                    if (bt.getMac().length() > 16)
                        try {
                            BluetoothDevice bdevice = adapter.getRemoteDevice(bt.getMac());
                            Method m = bdevice.getClass().getMethod("setAlias", String.class);
                            m.invoke(bdevice, bt.getDesc2());
                            ++count;
                        } catch (Exception e) {
                            //Log.d(TAG, "error renaming device:" + e.getMessage());
                        }
                }
                Toast.makeText(ManageData.this, getString(R.string.ListName) + " - " + count, Toast.LENGTH_LONG).show();
            }
        });

        // Register storage task listeners with explicit availability checks
        this.exportDbToSdButton.setOnClickListener(new OnClickListener() {
            public void onClick(final View v) {
                if (ManageData.this.isExternalStorageAvail()) {
                    new ExportDatabaseFileTask().execute();
                } else {
                    Toast.makeText(ManageData.this, R.string.needStorage, Toast.LENGTH_SHORT).show();
                }
            }
        });

        this.exportDbXmlToSdButton.setOnClickListener(new OnClickListener() {
            public void onClick(final View v) {
                if (ManageData.this.isExternalStorageAvail()) {
                    new ExportDataAsXmlTask().execute("devices", "A2DPDevices");
                } else {
                    Toast.makeText(ManageData.this, R.string.needStorage, Toast.LENGTH_SHORT).show();
                }
            }
        });

        this.importDB.setOnClickListener(new OnClickListener() {
            public void onClick(final View v) {
                if (ManageData.this.isExternalStorageAvail()) {
                    new ImportDatabaseFileTask().execute("devices", a2dpDir);
                } else {
                    Toast.makeText(ManageData.this, R.string.needStorage, Toast.LENGTH_SHORT).show();
                }
            }
        });

        this.exportLoc.setOnClickListener(new OnClickListener() {
            public void onClick(final View v) {
                if (ManageData.this.isExternalStorageAvail()) {
                    new ExportLocationTask().execute("My_Last_Location", a2dpDir);
                } else {
                    Toast.makeText(ManageData.this, R.string.needStorage, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 3. Configure storage paths and update UI feature states safely
        updateStorageFeatureState();
    }

    private void updateStorageFeatureState() {
        boolean available = isExternalStorageAvail();

        if (available) {
            // Attempt to resolve secondary physical SD card first, fallback to primary external storage
            File[] externalDirs = ContextCompat.getExternalFilesDirs(this, null);
            if (externalDirs.length > 1 && externalDirs[1] != null) {
                a2dpDir = externalDirs[1].getAbsolutePath() + "/A2DPVol";
            } else {
                a2dpDir = Environment.getExternalStorageDirectory() + "/A2DPVol";
            }

            if ((ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED)) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS);
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS);
                }
            }
        } else {
            Toast.makeText(this, R.string.needStorage, Toast.LENGTH_LONG).show();
        }

        // Enable/Disable external storage reliant controls dynamically
        if (exportDbToSdButton != null) exportDbToSdButton.setEnabled(available);
        if (exportDbXmlToSdButton != null) exportDbXmlToSdButton.setEnabled(available);
        if (importDB != null) importDB.setEnabled(available);
        if (exportLoc != null) exportLoc.setEnabled(available);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void setupActionBar() {
        Objects.requireNonNull(getActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS: {
                boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;

                if (!granted) {
                    Toast.makeText(application, R.string.needStorage, Toast.LENGTH_LONG).show();
                }

                // Toggle storage button state on permission result without ending Activity
                if (exportDbToSdButton != null) exportDbToSdButton.setEnabled(granted);
                if (exportDbXmlToSdButton != null) exportDbXmlToSdButton.setEnabled(granted);
                if (importDB != null) importDB.setEnabled(granted);
                if (exportLoc != null) exportLoc.setEnabled(granted);

                return;
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(final Bundle saveState) {
        super.onSaveInstanceState(saveState);
    }

    private boolean isExternalStorageAvail() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    private class ExportDatabaseFileTask extends
            AsyncTask<String, Void, Boolean> {
        private final ProgressDialog dialog = new ProgressDialog(
                ManageData.this);

        protected void onPreExecute() {
            this.dialog.setMessage("Exporting database...");
            this.dialog.show();
        }

        protected Boolean doInBackground(final String... args) {
            File dbFile = new File(application.getDeviceDB().getDb().getPath());
            File exportDir = new File(a2dpDir);

            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            File file = new File(exportDir, dbFile.getName());
            pathstr = file.getPath();
            try {
                file.createNewFile();
                this.copyFile(dbFile, file);
                return true;
            } catch (IOException e) {
                Log.e(MyApplication.APP_NAME, e.getMessage(), e);
                return false;
            }
        }

        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            if (success) {
                Toast.makeText(ManageData.this, "Export successful!",
                        Toast.LENGTH_SHORT).show();
                ManageData.this.path.setText("Exported to: " + pathstr);
            } else {
                Toast.makeText(ManageData.this, "Export failed",
                        Toast.LENGTH_SHORT).show();
                ManageData.this.path.setText("Export Failed");
            }
        }

        void copyFile(File src, File dst) throws IOException {
            FileChannel inChannel = new FileInputStream(src).getChannel();
            FileChannel outChannel = new FileOutputStream(dst).getChannel();
            try {
                inChannel.transferTo(0, inChannel.size(), outChannel);
            } finally {
                if (inChannel != null)
                    inChannel.close();
                outChannel.close();
            }
        }
    }

    private class ExportDataAsXmlTask extends AsyncTask<String, Void, String> {
        private final ProgressDialog dialog = new ProgressDialog(
                ManageData.this);

        protected void onPreExecute() {
            this.dialog.setMessage("Exporting database as XML...");
            this.dialog.show();
        }

        protected String doInBackground(final String... args) {
            DataXmlExporter dm = new DataXmlExporter(
                    ManageData.this.application.getDeviceDB().getDb());
            try {
                String dbName = args[0];
                String exportFileName = args[1];
                dm.export(dbName, exportFileName);
                pathstr = a2dpDir + "/" + exportFileName + ".xml";
            } catch (IOException e) {
                Log.e(MyApplication.APP_NAME, e.getMessage(), e);
                return e.getMessage();
            }
            return null;
        }

        protected void onPostExecute(final String errMsg) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            if (errMsg == null) {
                Toast.makeText(ManageData.this, "Export successful!",
                        Toast.LENGTH_SHORT).show();
                ManageData.this.path.setText("Exported to: " + pathstr);
            } else {
                Toast.makeText(ManageData.this, "Export failed - " + errMsg,
                        Toast.LENGTH_SHORT).show();
                ManageData.this.path.setText("Export Failed");
            }
        }
    }

    private class SelectDataTask extends AsyncTask<String, Void, String> {
        private final ProgressDialog dialog = new ProgressDialog(
                ManageData.this);

        protected void onPreExecute() {
            this.dialog.setMessage("Selecting data...");
            this.dialog.show();
        }

        protected String doInBackground(final String... args) {
            List<String> names = ManageData.this.application.getDeviceDB()
                    .selectAll();
            StringBuilder sb = new StringBuilder();
            for (String name : names) {
                sb.append(name + "\n");
            }
            return sb.toString();
        }

        protected void onPostExecute(final String result) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            ManageData.this.output.setText(result);
        }
    }

    private class ImportDatabaseFileTask extends
            AsyncTask<String, Void, Boolean> {
        private final ProgressDialog dialog = new ProgressDialog(
                ManageData.this);

        protected void onPreExecute() {
            this.dialog.setMessage("Importing database...");
            this.dialog.show();
        }

        protected Boolean doInBackground(final String... args) {
            File dbFile = new File(application.getDeviceDB().getDb().getPath());
            File exportDir = new File(a2dpDir);

            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            File file = new File(exportDir, dbFile.getName());
            pathstr = file.getPath();
            try {
                file.createNewFile();
                this.copyFile(file, dbFile);
                return true;
            } catch (IOException e) {
                Log.e(MyApplication.APP_NAME, e.getMessage(), e);
                return false;
            }
        }

        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            if (success) {
                ManageData.this.path.setText("Imported from: " + pathstr);
                final String Ireload = "a2dp.vol.Main.RELOAD_LIST";
                Intent itent = new Intent();
                itent.setAction(Ireload);
                itent.putExtra("device", "");
                application.sendBroadcast(itent);
                Toast.makeText(ManageData.this, R.string.ImportCompletedText,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ManageData.this, "Import failed",
                        Toast.LENGTH_SHORT).show();
                ManageData.this.path.setText("Import Failed");
            }
        }

        void copyFile(File src, File dst) throws IOException {
            FileChannel inChannel = new FileInputStream(src).getChannel();
            FileChannel outChannel = new FileOutputStream(dst).getChannel();
            try {
                inChannel.transferTo(0, inChannel.size(), outChannel);
            } finally {
                if (inChannel != null)
                    inChannel.close();
                outChannel.close();
            }
        }
    }

    private class ExportLocationTask extends AsyncTask<String, Void, Boolean> {
        private final ProgressDialog dialog = new ProgressDialog(
                ManageData.this);

        protected void onPreExecute() {
            this.dialog.setMessage("Exporting location data...");
            this.dialog.show();
        }

        protected Boolean doInBackground(final String... args) {
            File LocFile = application.getFileStreamPath(args[0]);
            File exportDir = new File(a2dpDir);

            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            File file = new File(exportDir, LocFile.getName() + ".txt");
            pathstr = file.getPath();
            try {
                file.createNewFile();
                this.copyFile(LocFile, file);
                return true;
            } catch (IOException e) {
                Log.e(MyApplication.APP_NAME, e.getMessage(), e);
                return false;
            }
        }

        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            if (success) {
                ManageData.this.path.setText("Exported to: " + pathstr);
                Toast.makeText(ManageData.this, "Location data exported",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ManageData.this, "Export failed",
                        Toast.LENGTH_SHORT).show();
                ManageData.this.path.setText("Export Failed");
            }
        }

        void copyFile(File src, File dst) throws IOException {
            FileChannel inChannel = new FileInputStream(src).getChannel();
            FileChannel outChannel = new FileOutputStream(dst).getChannel();
            try {
                inChannel.transferTo(0, inChannel.size(), outChannel);
            } finally {
                if (inChannel != null)
                    inChannel.close();
                outChannel.close();
            }
        }
    }
}