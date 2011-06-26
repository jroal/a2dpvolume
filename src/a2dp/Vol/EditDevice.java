package a2dp.Vol;

import java.util.List;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
		
		
		btd = getIntent().getStringExtra("btd"); // get the mac address of the device to edit
		
		device = myDB.getBTD(btd);
		
		fdesc2.setText(device.desc2);
		fgloc.setChecked(device.isGetLoc());
		fsetvol.setChecked(device.isSetV());
		fvol.setMax(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
		fvol.setProgress(device.defVol);
		fapp.setText(device.getPname());
		fbt.setText(device.getBdevice());
		fwifi.setChecked(device.isWifi());
		if(device == null)connbt.setEnabled(false);
		
		sb.setOnClickListener(new OnClickListener(){
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
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				EditDevice.this.finish();
			}
		});
		

		startapp.setOnClickListener(new OnClickListener(){			
			public void onClick(View arg0) {
				final PackageManager pm = getPackageManager();

				List<ApplicationInfo> packages = pm
						.getInstalledApplications(PackageManager.GET_META_DATA);		
				final String[] lstring = new String[packages.size()];
				int i = 0;
				for (ApplicationInfo packageInfo : packages) {
					lstring[i] = packageInfo.packageName;
					i++;
				}			

				AlertDialog.Builder builder = new AlertDialog.Builder(a2dp.Vol.EditDevice.this);
				builder.setTitle("Pick a package");
				builder.setItems(lstring, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				    	fapp.setText(lstring[item]);
				    }
				});
				AlertDialog alert = builder.create();
				alert.show();
				
			}} );

		connbt.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				final Vector<btDevice> vec = myDB.selectAlldb();
				final String[] lstring = new String[vec.size()];
				for(int i=0; i<vec.size(); i++){
					lstring[i] = vec.get(i).desc2;
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(a2dp.Vol.EditDevice.this);
				builder.setTitle("Bluetooth Device");
				builder.setItems(lstring, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				    			fbt.setText(vec.get(item).mac);
				    }
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
			
		});
	}
}
