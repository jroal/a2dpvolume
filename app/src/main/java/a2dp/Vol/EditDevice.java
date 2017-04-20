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
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class EditDevice extends Activity {

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		Save();
		closedb();
		EditDevice.this.finish();
		super.onBackPressed();
	}

	private Button sb, cb;
	private Button startapp;
	private Button connbt;
	private EditText fdesc2;
	private CheckBox fgloc;
	private CheckBox fsetvol;
	private SeekBar fvol;
	private EditText fapp;
	private EditText fbt;
	private CheckBox fwifi;
	private CheckBox fapprestart;
	private CheckBox fappkill;
	private CheckBox fenableGPS;
	private CheckBox fenableTTS;
	private CheckBox fsetpv;
	private SeekBar fphonev;
	private SeekBar fsmsdelaybar;
	private TextView fsmsdelaybox, tv2, ttsdelay, mediadelay, tvstream, tvmediavol, tvincallVol;
	private SeekBar fvoldelaybar;
	private TextView fvoldelaybox;
	private CheckBox frampVol;
	private CheckBox fautoVol;
	private CheckBox fsilent;
	private CheckBox fsleepBox;
	private CheckBox fcarmodeBox;
	private RadioButton iconradio0, iconradio1, iconradio2, iconradio3, iconradio4, streamradio0, streamradio1, streamradio2;
	private RadioGroup streamgroup, icongroup;
	private LinearLayout l1, l2;
	
	SharedPreferences preferences;
	private boolean TTsEnabled;

	public String btd;
	private btDevice device;
	private MyApplication application;
	private DeviceDB myDB; // database of device data stored in SQlite
	private String pname;
	private String appaction;
	private String appdata;
	private String apptype;
	private boolean apprestart;
	private boolean appkill;
	private boolean enablegps;
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
	private static final int ACTION_ADD_PACKAGE = 17;
	private static final int MUSIC_STREAM = 0;
	private static final int IN_CALL_STREAM = 1;
	private static final int ALARM_STREAM = 2;

	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.edit_item);

		this.application = (MyApplication) this.getApplication();
		this.myDB = new DeviceDB(application);
		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		this.sb = (Button) this.findViewById(R.id.EditDevSavebutton);
		this.cb = (Button) this.findViewById(R.id.EditDevCancelbutton);
		this.startapp = (Button) this.findViewById(R.id.chooseAppButton);
		this.connbt = (Button) this.findViewById(R.id.chooseBTbutton);
		this.fdesc2 = (EditText) this.findViewById(R.id.editDesc2);
		this.fgloc = (CheckBox) this.findViewById(R.id.checkCaptLoc);
		this.fsetvol = (CheckBox) this.findViewById(R.id.checkSetVol);
		this.fvol = (SeekBar) this.findViewById(R.id.seekBarVol);
		this.fapp = (EditText) this.findViewById(R.id.editApp);
		this.fapprestart = (CheckBox) this
				.findViewById(R.id.appRestartCheckbox);
		this.fappkill = (CheckBox) this.findViewById(R.id.appKillCheckbox);
		this.fbt = (EditText) this.findViewById(R.id.editBtConnect);
		this.fwifi = (CheckBox) this.findViewById(R.id.checkwifi);
		
		this.fenableTTS = (CheckBox) this.findViewById(R.id.enableTTSBox);
		this.fsetpv = (CheckBox) this.findViewById(R.id.checkSetpv);
		this.fsilent = (CheckBox) this.findViewById(R.id.silentBox);
		this.fphonev = (SeekBar) this.findViewById(R.id.seekPhoneVol);
		this.fsmsdelaybar = (SeekBar) this.findViewById(R.id.SMSdelayseekBar);
		this.fsmsdelaybox = (TextView) this.findViewById(R.id.SMSdelaytextView);
		this.fvoldelaybar = (SeekBar) this.findViewById(R.id.VolDelaySeekBar);
		this.fvoldelaybox = (TextView) this.findViewById(R.id.VolDelayTextView);
		this.tv2 = (TextView) this.findViewById(R.id.textView2);
		this.frampVol = (CheckBox) this.findViewById(R.id.rampBox);
		this.fautoVol = (CheckBox) this.findViewById(R.id.autoVolcheckBox);
		this.icongroup = (RadioGroup) this.findViewById(R.id.radioGroupIcon);
		this.iconradio0 = (RadioButton) this.findViewById(R.id.iconradio0);
		this.iconradio1 = (RadioButton) this.findViewById(R.id.iconradio1);
		this.iconradio2 = (RadioButton) this.findViewById(R.id.iconradio2);
		this.iconradio3 = (RadioButton) this.findViewById(R.id.iconradio3);
		this.iconradio4 = (RadioButton) this.findViewById(R.id.iconradio4);
		this.streamgroup = (RadioGroup) this.findViewById(R.id.radioGroupStream);
		this.streamradio0 = (RadioButton) this.findViewById(R.id.streamradio0);
		this.streamradio1 = (RadioButton) this.findViewById(R.id.streamradio1);
		this.streamradio2 = (RadioButton) this.findViewById(R.id.streamradio2);
		this.l1 = (LinearLayout) this.findViewById(R.id.LinearLayout1);
		this.l2 = (LinearLayout) this.findViewById(R.id.LinearLayout2);
		this.ttsdelay = (TextView) this.findViewById(R.id.textViewTTSDelay);
		this.mediadelay = (TextView) this.findViewById(R.id.textViewMediaDelay);
		this.tvstream = (TextView) this.findViewById(R.id.textViewStream);
		this.tvmediavol = (TextView) this.findViewById(R.id.textViewMediaVolume);
		this.tvincallVol = (TextView) this.findViewById(R.id.textViewInCallVol);
		this.fsleepBox = (CheckBox) this.findViewById(R.id.checkBoxSleep);
		this.fcarmodeBox = (CheckBox) this.findViewById(R.id.checkBoxLaunchCar);
		
		preferences = PreferenceManager
				.getDefaultSharedPreferences(application);
		TTsEnabled = preferences.getBoolean("enableTTS", false);

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
		pname = device.getPname();
		appaction = device.getAppaction();
		appdata = device.getAppdata();
		apptype = device.getApptype();
		apprestart = device.isApprestart();
		appkill = device.isAppkill();
		
		fapprestart.setChecked(apprestart);
		fappkill.setChecked(appkill);
		
		fenableTTS.setChecked(device.isEnableTTS());
		fsetpv.setChecked(device.isSetpv());
		fphonev.setMax(am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL));
		fphonev.setProgress(device.getPhonev());
		fsmsdelaybar.setMax(20);
		fsmsdelaybar.setOnSeekBarChangeListener(smsdelaySeekBarProgress);
		fsmsdelaybox.setText(device.smsdelay + "s");
		fsmsdelaybar.setProgress(device.getSmsdelay());
		fvoldelaybar.setMax(20);
		fvoldelaybar.setOnSeekBarChangeListener(voldelaySeekBarProgress);
		fvoldelaybox.setText(device.voldelay + "s");
		fvoldelaybar.setProgress(device.getVoldelay());
		frampVol.setChecked(device.isVolramp());
		fautoVol.setChecked(device.isAutovol());
		fsilent.setChecked(device.isSilent());
		fsleepBox.setChecked(device.isSleep());
		fcarmodeBox.setChecked(device.isCarmode());
		
		switch(device.getIcon()){
		case R.drawable.car2: iconradio0.setChecked(true); break;
		case R.drawable.headset: iconradio1.setChecked(true); break;
		case R.drawable.jack: iconradio2.setChecked(true); break;
		case R.drawable.usb: iconradio3.setChecked(true); break;
		case R.drawable.icon5: iconradio4.setChecked(true); break;
		default: iconradio0.setChecked(true); break;
		}
		
		switch(device.getSmsstream()){
		case MUSIC_STREAM: streamradio0.setChecked(true); break;
		case IN_CALL_STREAM: streamradio1.setChecked(true); break;
		case ALARM_STREAM: streamradio2.setChecked(true); break;
		default: streamradio0.setChecked(true);
		}
		
		
		
		setTTSVisibility();
		fenableTTS.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				setTTSVisibility();
			}
			
		});
		
		setMediaVisibility();
		fsetvol.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				setMediaVisibility();
				
			}
			
		});
		
		setInCallVisibility();
		fsetpv.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				setInCallVisibility();
			}
			
		});
		
		setAppVisibility();
		
		tv2.requestFocus(); // prevent jumping around screen
		vUpdateApp();

		sb.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				Save();
				closedb();
				EditDevice.this.finish();
			}
		});

		cb.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				closedb();
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
				if (!myDB.getDb().isOpen()
						&& !myDB.getDb().isDbLockedByCurrentThread())
					myDB = new DeviceDB(application);
				final Vector<btDevice> vec = myDB.selectAlldb();
				int j = vec.size();
				for (int i = 0; i < j; i++) {
					if ((vec.get(i).mac.length() < 17)) {
						vec.remove(i);
						j--;
						i--;
					}
				}

				vec.trimToSize();
				final String[] lstring = new String[vec.size() + 1];
				for (int i = 0; i < vec.size(); i++) {
					lstring[i] = vec.get(i).desc2;
				}
				lstring[vec.size()] = "none";

				AlertDialog.Builder builder = new AlertDialog.Builder(
						a2dp.Vol.EditDevice.this);
				builder.setTitle("Bluetooth Device");
				builder.setItems(lstring,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {
								if (item < vec.size())
									fbt.setText(vec.get(item).mac);
								else
									fbt.setText("");
							}
						});
				AlertDialog alert = builder.create();
				alert.show();
			}

		});

	}


	private void setMediaVisibility(){
		if(fsetvol.isChecked()){
			tvmediavol.setVisibility(TextView.VISIBLE);
			fvol.setVisibility(SeekBar.VISIBLE);
			fautoVol.setVisibility(CheckBox.VISIBLE);
			frampVol.setVisibility(CheckBox.VISIBLE);
			l2.setVisibility(LinearLayout.VISIBLE);
			mediadelay.setVisibility(TextView.VISIBLE);
		}else{
			tvmediavol.setVisibility(TextView.GONE);
			fvol.setVisibility(SeekBar.GONE);
			fautoVol.setVisibility(CheckBox.GONE);
			frampVol.setVisibility(CheckBox.GONE);
			l2.setVisibility(LinearLayout.GONE);
			mediadelay.setVisibility(TextView.GONE);
		}
	}
	
	private void setTTSVisibility(){
		if(fenableTTS.isChecked()){
			l1.setVisibility(LinearLayout.VISIBLE);
			ttsdelay.setVisibility(TextView.VISIBLE);
			tvstream.setVisibility(TextView.VISIBLE);
			streamgroup.setVisibility(RadioGroup.VISIBLE);
		}
		else{
			l1.setVisibility(LinearLayout.GONE);
			ttsdelay.setVisibility(TextView.GONE);
			tvstream.setVisibility(TextView.GONE);
			streamgroup.setVisibility(RadioGroup.GONE);
		}
	}
	
	private void setInCallVisibility(){
		if(fsetpv.isChecked()){
			tvincallVol.setVisibility(TextView.VISIBLE);
			fphonev.setVisibility(SeekBar.VISIBLE);
		}else{
			tvincallVol.setVisibility(TextView.GONE);
			fphonev.setVisibility(SeekBar.GONE);
		}
	}
	
	private void setAppVisibility(){
		if(fapp.getText().length() > 0){
			fapp.setVisibility(EditText.VISIBLE);
			fapprestart.setVisibility(CheckBox.VISIBLE);
			fappkill.setVisibility(CheckBox.VISIBLE);
			fsleepBox.setVisibility(CheckBox.GONE);
		}else{
			fapp.setVisibility(EditText.GONE);
			fapprestart.setVisibility(CheckBox.GONE);
			fappkill.setVisibility(CheckBox.GONE);
			fsleepBox.setVisibility(CheckBox.GONE);
		}
	}
	
	
	OnSeekBarChangeListener smsdelaySeekBarProgress = new OnSeekBarChangeListener() {

		public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
			fsmsdelaybox.setText(progress + "s");
			//fsmsdelaybar.requestFocus();

		}

		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub

		}

		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub

		}
	};

	OnSeekBarChangeListener voldelaySeekBarProgress = new OnSeekBarChangeListener() {

		public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
			fvoldelaybox.setText(progress + "s");
			//fvoldelaybar.requestFocus();

		}

		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub

		}

		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub

		}
	};

	private void Save(){
		if (fdesc2.length() < 1)
			device.setDesc2(device.desc1);
		else
			device.setDesc2(fdesc2.getText().toString());

		device.setSetV(fsetvol.isChecked());
		device.setDefVol(fvol.getProgress());
		device.setGetLoc(fgloc.isChecked());
		device.setPname(pname);
		device.setBdevice(fbt.getText().toString());
		device.setWifi(fwifi.isChecked());
		//device.setEnablegps(fenableGPS.isChecked());
		device.setAppaction(appaction);
		device.setAppdata(appdata);
		device.setApptype(apptype);
		apprestart = fapprestart.isChecked();
		device.setApprestart(apprestart);
		appkill = fappkill.isChecked();
		device.setAppkill(appkill);
		enablegps = fenableTTS.isChecked();
		device.setEnableTTS(enablegps);
		device.setSetpv(fsetpv.isChecked());
		device.setPhonev(fphonev.getProgress());
		device.setSmsdelay(fsmsdelaybar.getProgress());
		device.setVoldelay(fvoldelaybar.getProgress());
		device.setVolramp(frampVol.isChecked());
		device.setAutovol(fautoVol.isChecked());
		device.setSilent(fsilent.isChecked());
		device.setSleep(fsleepBox.isChecked());
		device.setCarmode(fcarmodeBox.isChecked());
		
		switch(icongroup.getCheckedRadioButtonId()){
		case R.id.iconradio0: device.setIcon(R.drawable.car2); break;
		case R.id.iconradio1: device.setIcon(R.drawable.headset); break;
		case R.id.iconradio2: device.setIcon(R.drawable.jack); break;
		case R.id.iconradio3: device.setIcon(R.drawable.usb); break;
		case R.id.iconradio4: device.setIcon(R.drawable.icon5); break;
		}
		
		switch(streamgroup.getCheckedRadioButtonId()){
		case R.id.streamradio0: device.setSmsstream(MUSIC_STREAM); break;
		case R.id.streamradio1: device.setSmsstream(IN_CALL_STREAM); break;
		case R.id.streamradio2: device.setSmsstream(ALARM_STREAM); break;
		}

		// if the user want TTS but it was not enabled
		if (!TTsEnabled && fenableTTS.isChecked()) {
			SharedPreferences.Editor editor = preferences.edit();
			editor.putBoolean("enableTTS", true);
			editor.commit();
		}

		sb.setText("Saving");
		try {
			myDB.update(device);
			// Reload the device list in the main page
			final String Ireload = "a2dp.Vol.main.RELOAD_LIST";
			Intent itent = new Intent();
			itent.setAction(Ireload);
			itent.putExtra("device", "");
			application.sendBroadcast(itent);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void closedb() {
		myDB.getDb().close();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case ACTION_CHOOSE_APP:
				pname = data.getStringExtra(AppChooser.EXTRA_PACKAGE_NAME);
				appaction = "";
				apptype = "";
				appdata = "";

				vUpdateApp();
				break;
			case ACTION_ADD_PACKAGE:
				pname = data.getStringExtra(AppChooser.EXTRA_PACKAGE_NAME);
				vUpdateApp();
				break;
			/*
			 * case ACTION_INPUT_LABEL: mAppItem.set(AppItem.KEY_LABEL,
			 * data.getStringExtra(StringInputDialog.EXTRA_VALUE));
			 * vUpdateLabel(); break;
			 */
			case ACTION_CHOOSE_APP_CUSTOM:
				pname = data.getStringExtra(AppChooser.EXTRA_PACKAGE_NAME);
				vUpdateApp();
				break;
			case ACTION_CHOOSE_FROM_PROVIDER:
				processShortcut(data);
				break;
			case ACTION_CUSTOM_INTENT:

				pname = "";
				appaction = data.getStringExtra("alarm_custom_action");
				appdata = data.getStringExtra("alarm_custom_data");
				apptype = data.getStringExtra("alarm_custom_type");

				if (appdata.length() > 3) {
					try {
						pname = Intent.getIntent(pname).getComponent()
								.getPackageName();
					} catch (URISyntaxException e) {
						pname = "custom";
						e.printStackTrace();
					}
				}
				if (pname.equals("")) {
					pname = "custom";
				}
				vUpdateApp();
				break;

			case ACTION_CREATE_HOME_SCREEN_SHORTCUT:
				startActivityForResult(data, FETCH_HOME_SCREEN_SHORTCUT);
				break;
			case FETCH_HOME_SCREEN_SHORTCUT:
				processShortcut(data);
				if (pname.length() < 3 || pname.equalsIgnoreCase("Custom"))
					showDialog(DIALOG_WARN_STOP_APP);
				break;
			}

		} else {

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
				i.putExtra("alarm_custom_action", appaction);
				i.putExtra("alarm_custom_data", appdata);
				i.putExtra("alarm_custom_type", apptype);
				i.putExtra("alarm_package_name", pname);
				startActivityForResult(i, ACTION_CUSTOM_INTENT);
				break;

			case 5:
				// Clear App
				pname = "";
				appaction = "";
				appdata = "";
				apptype = "";

				vUpdateApp();
				break;
			}
		}

	};

	private void vUpdateApp() {
		device.setAppaction(appaction);
		device.setAppdata(appdata);
		device.setApptype(apptype);
		device.setPname(pname);

		if (device.hasIntent()) {
			if (pname != null && pname.length() > 3)
				fapp.setText(pname);
			else if (appdata != null) {
				fapp.setText(appdata);
			} else if (appaction != null) {
				fapp.setText(appaction);
			} else {
				fapp.setText("Custom");
			}
		} else
			fapp.setText("");
		setAppVisibility();
	}

	/*
	 * private void checkCustomAppPackage() { if
	 * ((mAppItem.getBool(AppItem.KEY_FORCE_RESTART)) &&
	 * mAppItem.isCustomIntent()) { showDialog(DIALOG_WARN_STOP_APP); } }
	 */

	private void processShortcut(Intent data) {
		Intent i = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
		appdata = getIntentUri(i);
		if (data.hasExtra(ProviderList.EXTRA_PACKAGE_NAME)) {
			pname = data.getStringExtra(ProviderList.EXTRA_PACKAGE_NAME);
		} else {
			try {
				pname = i.getComponent().getPackageName();
			} catch (Exception e) {
				pname = "";
				e.printStackTrace();
			}

		}

		if (pname.length() < 3) {
			pname = "custom";
		}
		appaction = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
		apptype = "";
		vUpdateApp();
	}

	public static String getIntentUri(Intent i) {
		String rtr = "";
		try {
			Method m = Intent.class.getMethod("toUri",
					int.class);
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
							startActivityForResult(i, ACTION_ADD_PACKAGE);
						}

					});
			adb4.setNegativeButton("Ignore",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {

						}

					});
			return adb4.create();

		}

		return super.onCreateDialog(id);
	}

}
