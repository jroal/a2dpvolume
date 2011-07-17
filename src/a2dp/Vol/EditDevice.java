package a2dp.Vol;

import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;

import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;

public class EditDevice extends Activity {

	private Button sb;
	private Button startapp;
	private Button connbt;
	private EditText fdesc2;
	private CheckBox fgloc;
	private CheckBox fsetvol;
	private SeekBar fvol;
	private EditText fapp;
	private EditText fbt;
	private CheckBox fwifi;
	public String btd;
	private btDevice device;
	private MyApplication application;
	private DeviceDB myDB; // database of device data stored in SQlite

	private static final int DIALOG_PICK_APP_TYPE = 3;
	private static final int DIALOG_WARN_STOP_APP = 5;
	private static final int DIALOG_BITLY = 6;
	private static final String[] APP_TYPE_OPTIONS = { "Choose App",
			"Create Shortcut", "Home Screen Shortcut", "Pandora Radio Station",
			"Custom Intent", "Clear App Selection" };
	private static final int ACTION_CHOOSE_APP = 2;
	private static final int ACTION_CUSTOM_INTENT = 6;
	private static final int ACTION_CHOOSE_FROM_PROVIDER = 11;
	private static final int ACTION_CREATE_HOME_SCREEN_SHORTCUT = 14;
	private static final int FETCH_HOME_SCREEN_SHORTCUT = 15;
	private static final int ACTION_CHOOSE_APP_CUSTOM = 16;
	private CheckBox mChkStopApp, mChkForceRestart;
	private AppItem mAppItem;

	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.edit_item);

		this.application = (MyApplication) this.getApplication();
		this.myDB = new DeviceDB(this);
		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		this.sb = (Button) this.findViewById(R.id.EditDevSavebutton);
		this.startapp = (Button) this.findViewById(R.id.chooseAppButton);
		this.connbt = (Button) this.findViewById(R.id.chooseBTbutton);
		this.fdesc2 = (EditText) this.findViewById(R.id.editDesc2);
		this.fgloc = (CheckBox) this.findViewById(R.id.checkCaptLoc);
		this.fsetvol = (CheckBox) this.findViewById(R.id.checkSetVol);
		this.fvol = (SeekBar) this.findViewById(R.id.seekBarVol);
		this.fapp = (EditText) this.findViewById(R.id.editApp);
		this.fbt = (EditText) this.findViewById(R.id.editBtConnect);
		this.fwifi = (CheckBox) this.findViewById(R.id.checkwifi);
		mAppItem = new AppItem();

		btd = getIntent().getStringExtra("btd"); // get the mac address of the
													// device to edit

		device = myDB.getBTD(btd);

		fdesc2.setText(device.desc2);
		fgloc.setChecked(device.isGetLoc());
		fsetvol.setChecked(device.isSetV());
		fvol.setMax(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
		fvol.setProgress(device.defVol);
		fapp.setText(device.getPname());
		fbt.setText(device.getBdevice());
		fwifi.setChecked(device.isWifi());
		if (device == null)
			connbt.setEnabled(false);

		sb.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				if (fdesc2.length() < 1)
					device.setDesc2(device.desc1);
				else
					device.setDesc2(fdesc2.getText().toString());

				device.setSetV(fsetvol.isChecked());
				device.setDefVol(fvol.getProgress());
				device.setGetLoc(fgloc.isChecked());
				device.setPname(fapp.getText().toString());
				device.setBdevice(fbt.getText().toString());
				device.setWifi(fwifi.isChecked());
				sb.setText("Saving");
				try {
					myDB.update(device);
					// Reload the device list in the main page
					final String Ireload = "a2dp.vol.ManageData.RELOAD_LIST";
					Intent itent = new Intent();
					itent.setAction(Ireload);
					application.sendBroadcast(itent);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				EditDevice.this.finish();
			}
		});

		// Show list of packages. This one loads fast but is too cryptic for
		// normal users
		startapp.setOnLongClickListener(new OnLongClickListener() {

			public boolean onLongClick(View arg0) {
				final PackageManager pm = getPackageManager();
				List<ApplicationInfo> packages = pm.getInstalledApplications(0);
				final String[] lstring = new String[packages.size()];
				int i = 0;
				for (int n = 0; n < packages.size(); n++) {
					Intent itent = pm.getLaunchIntentForPackage(packages.get(n).packageName);
					if (packages.get(n).icon > 0 && packages.get(n).enabled
							&& itent != null) {
						lstring[i] = packages.get(n).packageName;
						// lstring[i] = itent.toUri(0);
						i++;
					} else {
						// This does not have an icon or is not enabled
					}
				}

				// get just the ones with an icon. This assumes packages without
				// icons are likely not ones a user needs.
				final String[] ls2 = new String[i];
				for (int j = 0; j < i; j++) {
					ls2[j] = lstring[j];
				}
				java.util.Arrays.sort(ls2); // sort the array

				AlertDialog.Builder builder = new AlertDialog.Builder(
						a2dp.Vol.EditDevice.this);
				builder.setTitle("Pick a package");
				builder.setItems(ls2, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						fapp.setText(ls2[item]);
					}
				});
				AlertDialog alert = builder.create();

				alert.show();

				return false;
			}
		});

		// The more friendly app chooser. However, this loads slow.
		startapp.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {

				AlertDialog.Builder adb2 = new AlertDialog.Builder(
						a2dp.Vol.EditDevice.this);
				adb2.setTitle(R.string.ea_ti_app);
				adb2.setItems(APP_TYPE_OPTIONS, mAppTypeDialogOnClick);
				adb2.create();
				adb2.show();

				// Intent in = new Intent(getBaseContext(), AppChooser.class);
				// startActivityForResult(in, 0);
			}
		});

		connbt.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final Vector<btDevice> vec = myDB.selectAlldb();
				int j = vec.size();
				for (int i = 0; i < j; i++) {
					if ((vec.get(i).mac.length() < 17)
							|| btd.equalsIgnoreCase(vec.get(i).mac)) {
						vec.remove(i);
						j--;
						i--;
					}
				}

				vec.trimToSize();
				final String[] lstring = new String[vec.size()];
				for (int i = 0; i < vec.size(); i++) {
					lstring[i] = vec.get(i).desc2;
				}

				AlertDialog.Builder builder = new AlertDialog.Builder(
						a2dp.Vol.EditDevice.this);
				builder.setTitle("Bluetooth Device");
				builder.setItems(lstring,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {
								fbt.setText(vec.get(item).mac);
							}
						});
				AlertDialog alert = builder.create();
				alert.show();
			}

		});
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case ACTION_CHOOSE_APP:
				mAppItem.set(AppItem.KEY_PACKAGE_NAME,
						data.getStringExtra(AppChooser.EXTRA_PACKAGE_NAME));
				mAppItem.set(AppItem.KEY_CUSTOM_ACTION, "");
				mAppItem.set(AppItem.KEY_CUSTOM_DATA, "");
				mAppItem.set(AppItem.KEY_CUSTOM_TYPE, "");
				vUpdateApp();
				break;
			/*
			 * case ACTION_INPUT_LABEL: mAppItem.set(AppItem.KEY_LABEL,
			 * data.getStringExtra(StringInputDialog.EXTRA_VALUE));
			 * vUpdateLabel(); break;
			 */
			case ACTION_CHOOSE_APP_CUSTOM:
				mAppItem.set(AppItem.KEY_PACKAGE_NAME,
						data.getStringExtra(AppChooser.EXTRA_PACKAGE_NAME));
				vUpdateApp();
				break;
			case ACTION_CHOOSE_FROM_PROVIDER:
				processShortcut(data);
				break;
			case ACTION_CUSTOM_INTENT:
				String dataString = data
						.getStringExtra(AppItem.KEY_CUSTOM_DATA);
				mAppItem.set(AppItem.KEY_PACKAGE_NAME, "");
				mAppItem.set(AppItem.KEY_CUSTOM_ACTION,
						data.getStringExtra(AppItem.KEY_CUSTOM_ACTION));
				mAppItem.set(AppItem.KEY_CUSTOM_DATA, dataString);
				mAppItem.set(AppItem.KEY_CUSTOM_TYPE,
						data.getStringExtra(AppItem.KEY_CUSTOM_TYPE));
				if (mAppItem.isShortcutIntent()) {
					try {
						mAppItem.set(
								AppItem.KEY_PACKAGE_NAME,
								Intent.getIntent(
										mAppItem.getString(AppItem.KEY_CUSTOM_DATA))
										.getComponent().getPackageName());
					} catch (URISyntaxException e) {
						mAppItem.set(AppItem.KEY_PACKAGE_NAME, "custom");
						e.printStackTrace();
					}
				}
				if (mAppItem.getString(AppItem.KEY_PACKAGE_NAME).equals("")) {
					mAppItem.set(AppItem.KEY_PACKAGE_NAME, "custom");
				}
				vUpdateApp();
				break;

			case ACTION_CREATE_HOME_SCREEN_SHORTCUT:
				startActivityForResult(data, FETCH_HOME_SCREEN_SHORTCUT);
				break;
			case FETCH_HOME_SCREEN_SHORTCUT:
				processShortcut(data);
				break;
			}

		} else {
			switch (requestCode) {
			case ACTION_CHOOSE_APP_CUSTOM:
				mChkStopApp.setChecked(false);
			}
		}

		super.onActivityResult(requestCode, resultCode, data);

		/*
		 * if(resultCode == RESULT_OK){
		 * fapp.setText(data.getStringExtra("package_name")); }
		 */
	};

	private DialogInterface.OnClickListener mAppTypeDialogOnClick = new DialogInterface.OnClickListener() {

		public void onClick(DialogInterface dialog, int which) {
			Intent i;
			switch (which) {
			case 0:
				// Select App
				i = new Intent(getBaseContext(), AppChooser.class);
				startActivityForResult(i, ACTION_CHOOSE_APP);
				break;
			case 1:
				// Create Shortcut
				i = new Intent(Intent.ACTION_PICK_ACTIVITY);
				i.putExtra(Intent.EXTRA_INTENT, new Intent(
						Intent.ACTION_CREATE_SHORTCUT));
				i.putExtra(Intent.EXTRA_TITLE, "Create a Shortcut");
				startActivityForResult(i, ACTION_CREATE_HOME_SCREEN_SHORTCUT);
				break;
			case 2:
				// Home Screen Shortcut
				i = new Intent(getBaseContext(), ProviderList.class);
				i.putExtra(ProviderList.EXTRA_PROVIDER,
						ProviderList.PROVIDER_HOMESCREEN);
				startActivityForResult(i, ACTION_CHOOSE_FROM_PROVIDER);
				break;
			case 3:
				// Pandora Station
				i = new Intent(getBaseContext(), ProviderList.class);
				i.putExtra(ProviderList.EXTRA_PROVIDER,
						ProviderList.PROVIDER_PANDORA);
				startActivityForResult(i, ACTION_CHOOSE_FROM_PROVIDER);
				break;
			// case 4:
			// //Google Listen Feed
			// //TODO: Need to check out what queue looks like in db
			// i = new Intent(getBaseContext(), CustomActionActivity.class);
			// i.putExtra(CustomActionActivity.EXTRA_ACTION_TYPE,
			// CustomActionActivity.ACTION_TYPE_LATEST_UNHEARD_LISTEN_PODCAST);
			// mAlarmItem.packageName =
			// CustomActionActivity.GOOGLE_LISTEN_PACKAGE_NAME);
			// mAlarmItem.customAction = "Latest Podcast on Google Listen";
			// mAlarmItem.customData = AalService.getIntentUri(i);
			// mAlarmItem.customType = "";
			// vUpdateApp();
			// break;
			case 4:
				// Custom Intent
				i = new Intent(getBaseContext(), CustomIntentMaker.class);
				i.putExtra(AppItem.KEY_CUSTOM_ACTION,
						mAppItem.getString(AppItem.KEY_CUSTOM_ACTION));
				i.putExtra(AppItem.KEY_CUSTOM_DATA,
						mAppItem.getString(AppItem.KEY_CUSTOM_DATA));
				i.putExtra(AppItem.KEY_CUSTOM_TYPE,
						mAppItem.getString(AppItem.KEY_CUSTOM_TYPE));
				// i.putExtra(AppItem.KEY_PACKAGE_NAME, mAlarmItem.packageName);
				startActivityForResult(i, ACTION_CUSTOM_INTENT);
				break;

			case 5:
				// Clear App
				mAppItem.set(AppItem.KEY_PACKAGE_NAME, "");
				mAppItem.set(AppItem.KEY_CUSTOM_ACTION, "");
				mAppItem.set(AppItem.KEY_CUSTOM_DATA, "");
				mAppItem.set(AppItem.KEY_CUSTOM_TYPE, "");
				vUpdateApp();
				break;
			}
		}

	};

	private void vUpdateApp() {
		PackageManager pm = getPackageManager();
		// mTvApp.setText(mAppItem.getAppName(pm));
		// mAppItem.setAppIconInImageView(mIvAppIcon, pm);
		//fapp.setText(mAppItem.getAppName(pm));
		fapp.setText(mAppItem.toString());
		checkCustomAppPackage();
		pm = null;
	}

	private void checkCustomAppPackage() {
		if ((mAppItem.getBool(AppItem.KEY_STOP_APP_ON_TIMEOUT) || mAppItem
				.getBool(AppItem.KEY_FORCE_RESTART))
				&& mAppItem.isCustomIntent()) {
			showDialog(DIALOG_WARN_STOP_APP);
		}
	}

	private void processShortcut(Intent data) {
		Intent i = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
		mAppItem.set(AppItem.KEY_CUSTOM_DATA, getIntentUri(i));
		if (data.hasExtra(ProviderList.EXTRA_PACKAGE_NAME)) {
			mAppItem.set(AppItem.KEY_PACKAGE_NAME,
					data.getStringExtra(ProviderList.EXTRA_PACKAGE_NAME));
		} else {
			try {
				mAppItem.set(AppItem.KEY_PACKAGE_NAME, i.getComponent()
						.getPackageName());
			} catch (Exception e) {
				mAppItem.set(AppItem.KEY_PACKAGE_NAME, "");
				e.printStackTrace();
			}

		}

		if (!mAppItem.hasPackageName()) {
			mAppItem.set(AppItem.KEY_PACKAGE_NAME, "custom");
		}
		mAppItem.set(AppItem.KEY_CUSTOM_ACTION,
				data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME));
		mAppItem.set(AppItem.KEY_CUSTOM_TYPE, "");
		vUpdateApp();
	}

	public static String getIntentUri(Intent i) {
		String rtr = "";
		try {
			Method m = Intent.class.getMethod("toUri",
					new Class[] { int.class });
			rtr = (String) m.invoke(i,
					Intent.class.getField("URI_INTENT_SCHEME").getInt(null));
		} catch (Exception e) {
			rtr = i.toURI();
		}
		return rtr;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {

		case DIALOG_PICK_APP_TYPE:
			AlertDialog.Builder adb2 = new AlertDialog.Builder(this);
			adb2.setTitle(R.string.ea_ti_app);
			adb2.setItems(APP_TYPE_OPTIONS, mAppTypeDialogOnClick);
			return adb2.create();

		case DIALOG_BITLY:
			ProgressDialog pd = new ProgressDialog(this);
			pd.setIndeterminate(true);
			pd.setMessage("Shortenting Url with Bit.ly...");
			pd.setCancelable(false);
			return pd;
		case DIALOG_WARN_STOP_APP:
			AlertDialog.Builder adb4 = new AlertDialog.Builder(this);
			adb4.setTitle(R.string.ae_stop_app_warning_title);
			adb4.setMessage(R.string.ae_stop_app_warning_message);
			adb4.setCancelable(false);
			adb4.setPositiveButton("Select App",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							Intent i = new Intent(getBaseContext(),
									AppChooser.class);
							startActivityForResult(i, ACTION_CHOOSE_APP_CUSTOM);
						}

					});
			adb4.setNegativeButton("Ignore",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							mChkStopApp.setChecked(false);
							mChkForceRestart.setChecked(false);
						}

					});
			return adb4.create();

		}

		return super.onCreateDialog(id);
	}

}
