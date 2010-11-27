package a2dp.Vol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * @author Leveraged from an example, modified by Jim Roal This activity manages
 *         the database used to store devices
 */
public class ManageData extends Activity {

	private MyApplication application;

	private Button exportDbToSdButton;
	private Button exportDbXmlToSdButton;
	private Button importDB;
	private Button exportLoc;
	private TextView output = (TextView) null;
	private TextView path = (TextView) null;
	private String pathstr;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.application = (MyApplication) this.getApplication();

		this.setContentView(R.layout.managedata);

		this.output = (TextView) findViewById(R.id.Output);
		this.path = (TextView) findViewById(R.id.Path);
		// initially populate "output" view from database
		new SelectDataTask().execute();

		this.exportDbToSdButton = (Button) this
				.findViewById(R.id.exportdbtosdbutton);
		this.exportDbToSdButton.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				if (ManageData.this.isExternalStorageAvail()) {
					new ExportDatabaseFileTask().execute();
				} else {
					Toast
							.makeText(
									ManageData.this,
									"External storage is not available, unable to export data.",
									Toast.LENGTH_SHORT).show();
				}
			}
		});

		this.exportDbXmlToSdButton = (Button) this
				.findViewById(R.id.exportdbxmltosdbutton);
		this.exportDbXmlToSdButton.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				if (ManageData.this.isExternalStorageAvail()) {
					new ExportDataAsXmlTask()
							.execute("devices", "BluetoothVol");
				} else {
					Toast
							.makeText(
									ManageData.this,
									"External storage is not available, unable to export data.",
									Toast.LENGTH_SHORT).show();
				}
			}
		});

		this.importDB = (Button) this.findViewById(R.id.ImportDBButton);
		this.importDB.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				if (ManageData.this.isExternalStorageAvail()) {
					new ImportDatabaseFileTask().execute("devices",
							"BluetoothVol");
				} else {
					Toast
							.makeText(
									ManageData.this,
									"External storage is not available, unable to import data.",
									Toast.LENGTH_SHORT).show();
				}
			}
		});

		this.exportLoc = (Button) this.findViewById(R.id.ExportLoc);
		this.exportLoc.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				if (ManageData.this.isExternalStorageAvail()) {
					new ExportLocationTask().execute("My_Last_Location",
							"BluetoothVol");
				} else {
					Toast
							.makeText(
									ManageData.this,
									"External storage is not available, unable to export data.",
									Toast.LENGTH_SHORT).show();
				}
			}
		});

	}

	@Override
	public void onPause() {
		super.onPause();
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

		// can use UI thread here
		protected void onPreExecute() {
			this.dialog.setMessage("Exporting database...");
			this.dialog.show();
		}

		// automatically done on worker thread (separate from UI thread)
		protected Boolean doInBackground(final String... args) {

			// File dbFile = new File(Environment.getDataDirectory() +
			// "/data/a2dp.vol/databases/btdevices.db");
			File dbFile = new File(application.getDeviceDB().getDb().getPath());
			File exportDir = new File(
					Environment.getExternalStorageDirectory(), "BluetoothVol");

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

		// can use UI thread here
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
				if (outChannel != null)
					outChannel.close();
			}
		}

	}

	private class ExportDataAsXmlTask extends AsyncTask<String, Void, String> {
		private final ProgressDialog dialog = new ProgressDialog(
				ManageData.this);

		// can use UI thread here
		protected void onPreExecute() {
			this.dialog.setMessage("Exporting database as XML...");
			this.dialog.show();
		}

		// automatically done on worker thread (separate from UI thread)
		protected String doInBackground(final String... args) {
			DataXmlExporter dm = new DataXmlExporter(
					ManageData.this.application.getDeviceDB().getDb());
			try {
				String dbName = args[0];
				String exportFileName = args[1];
				dm.export(dbName, exportFileName);
				pathstr = Environment.getExternalStorageDirectory()
						+ "/BluetoothVol/" + exportFileName + ".xml";
			} catch (IOException e) {
				Log.e(MyApplication.APP_NAME, e.getMessage(), e);
				return e.getMessage();
			}
			return null;
		}

		// can use UI thread here
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

		// can use UI thread here
		protected void onPreExecute() {
			this.dialog.setMessage("Selecting data...");
			this.dialog.show();
		}

		// automatically done on worker thread (separate from UI thread)
		protected String doInBackground(final String... args) {
			List<String> names = ManageData.this.application.getDeviceDB()
					.selectAll();
			StringBuilder sb = new StringBuilder();
			for (String name : names) {
				sb.append(name + "\n");
			}
			return sb.toString();
		}

		// can use UI thread here
		protected void onPostExecute(final String result) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}

			ManageData.this.output.setText(result);
		}
	}

	// import database
	private class ImportDatabaseFileTask extends
			AsyncTask<String, Void, Boolean> {
		private final ProgressDialog dialog = new ProgressDialog(
				ManageData.this);

		// can use UI thread here
		protected void onPreExecute() {
			this.dialog.setMessage("Importing database...");
			this.dialog.show();
		}

		// automatically done on worker thread (separate from UI thread)
		protected Boolean doInBackground(final String... args) {

			// File dbFile = new File(Environment.getDataDirectory() +
			// "/data/a2dp.vol/databases/btdevices.db");
			File dbFile = new File(application.getDeviceDB().getDb().getPath());
			// application.getDeviceDB().getDb().close();
			File exportDir = new File(
					Environment.getExternalStorageDirectory(), "BluetoothVol");

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

		// can use UI thread here
		protected void onPostExecute(final Boolean success) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			if (success) {
				// Toast.makeText(ManageData.this, "Import successful!",
				// Toast.LENGTH_SHORT).show();
				ManageData.this.path.setText("Imported from: " + pathstr);
				// need a way to reopen the database at this point
				android.app.AlertDialog.Builder builder = new AlertDialog.Builder(
						ManageData.this);
				builder.setTitle("Import Complete");
				builder
						.setMessage("You must now close this program and reopen it for the new database to take affect. "
								+ "Click the back key twice to return to the main screen and then click menu and exit.  "
								+ "Doing anything else will cause a force close!");
				builder.show();

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
				if (outChannel != null)
					outChannel.close();
			}
		}

	}

	// export location
	private class ExportLocationTask extends AsyncTask<String, Void, Boolean> {
		private final ProgressDialog dialog = new ProgressDialog(
				ManageData.this);

		// can use UI thread here
		protected void onPreExecute() {
			this.dialog.setMessage("Exporting location data...");
			this.dialog.show();
		}

		// automatically done on worker thread (separate from UI thread)
		protected Boolean doInBackground(final String... args) {

			File LocFile = application.getFileStreamPath(args[0]);

			File exportDir = new File(
					Environment.getExternalStorageDirectory(), "BluetoothVol");

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

		// can use UI thread here
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
				if (outChannel != null)
					outChannel.close();
			}
		}

	}

}
