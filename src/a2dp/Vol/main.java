package a2dp.Vol;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.Vector;


import a2dp.Vol.R.menu;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.ContentProviderOperation.Builder;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class main extends Activity {
	String str = "Find A2DP Devices";  
	static String str2 = "No data";
    static TextView tx1 = (TextView) null; 
	static Integer OldVol = 5;
	static AudioManager am = (AudioManager) null;
	boolean servrun = false;
	ListView lvl = null;
	Vector<btDevice> vec;
	private DeviceDB myDB;
	String activebt = null;
	private MyApplication application;
	SharedPreferences preferences;
	public static final String PREFS_NAME = "btVol";
	String[] lstring = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu, menu);
    	return true;
    }
    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
 
        switch (item.getItemId()) {
        case R.id.Manage_data:
        	Intent i = new Intent(a2dp.Vol.main.this, ManageData.class);
            startActivity(i);
            return true;
            
        case R.id.Save:
        	
            return true;
            
        case R.id.prefs:
        	Intent j = new Intent(a2dp.Vol.main.this, Preferences.class);
            startActivity(j);
            return true;
            
        case R.id.DelData:
        	myDB.deleteAll();
        	refreshList(loadFromDB());
        	return true;
        }
        return false;
    }
    
/** Called when the activity is first created. */
    @Override
 
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    	//preferences = PreferenceManager.getDefaultSharedPreferences(this);
    	preferences = getSharedPreferences(PREFS_NAME,0);
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE) ;
        final Button btn = (Button) findViewById(R.id.Button01);
        final Button uptn = (Button) findViewById(R.id.Upbtn);
        final Button dntn = (Button) findViewById(R.id.Downbtn);
        final Button locbtn = (Button) findViewById(R.id.Locationbtn);
        final Button serv = (Button) findViewById(R.id.ServButton);
        // get "Application" object for shared state or creating of expensive resources - like DataHelper
        // (this is not recreated as often as each Activity)
        this.application = (MyApplication) this.getApplication();

        vec = new Vector<btDevice>();
        tx1 = (TextView) findViewById(R.id.TextView01); 
        final ListView lvl = (ListView)findViewById(R.id.ListView01);
        this.myDB = new DeviceDB(this);
        //this.myDB.deleteAll();
        lstring = new String[] {"no data"};
		lvl.setAdapter(new ArrayAdapter<String>(application, android.R.layout.simple_list_item_1 , lstring));
             
		
	    tx1.setText("List Devices");
	    btn.setText("Find A2DP Devices");
	    try{
	    	if(a2dp.Vol.service.run)
	    	{
	    		servrun = true;
			    serv.setText("Kill Service");
			  }
	    	else
	    	{
		    	servrun = false;
			    serv.setText("Start Service");	
	    	}
	    }
	    catch(Exception x){
	    	servrun = false;
		    serv.setText("Start Service");
	    }
    
	    // capture original volume
		if(OldVol < am.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
		{
			OldVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		}
		else
		{
			OldVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		}
	    
		
		tx1.setText("Stored Volume:" + OldVol + "\n" + str2);
		
        btn.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				
				int test = getBtDevices();
				if(test > 0){
					lstring = new String[test];
					for(int i =0;i<test;i++)
					{
						lstring[i] = vec.get(i).toString();
					}
				
					refreshList(loadFromDB());
				lvl.setAdapter(new ArrayAdapter<String>(application, android.R.layout.simple_list_item_1 , lstring));
	        	//lvl.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
				lvl.invalidateViews();
				lvl.forceLayout();
				
				}

				btn.setText(str);
	    	    tx1.setText("Current Volume:" + OldVol + " Devices found=" + test + "\n");
	    	    
	    	    }
		});
        
        lvl.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                int position, long id) {
              // When clicked, show a toast with the TextView text
              //Toast.makeText(getApplicationContext(), ((TextView) view).getText(), Toast.LENGTH_SHORT).show();
              btDevice bt = new btDevice();
              bt = vec.get(position);
              android.app.AlertDialog.Builder builder = new AlertDialog.Builder(a2dp.Vol.main.this);
              builder.setTitle(bt.toString()); 
              builder.setMessage(bt.desc1 + "\n" + bt.desc2  + "\n" + bt.mac  + "\nConnected Volume: " + bt.defVol  + "\nTrigger: " + bt.setV);
              builder.setPositiveButton("OK", null);
              builder.setNeutralButton("Edit", null);
              builder.show();
			return servrun;			
            }       
          });

        lvl.setOnItemClickListener(new OnItemClickListener(){
        	public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                  // When clicked, show a toast with the TextView text
                  //Toast.makeText(getApplicationContext(), ((TextView) view).getText(), Toast.LENGTH_SHORT).show();
                  final btDevice bt = vec.get(position);
                  btDevice bt2 = myDB.getBTD(bt.mac);
                  android.app.AlertDialog.Builder builder = new AlertDialog.Builder(a2dp.Vol.main.this);
                  builder.setTitle(bt.toString()); 
                  builder.setMessage(bt2.desc1 + "\n" + bt2.desc2  + "\n" + bt2.mac  + "\nConnected Volume: " + bt2.defVol  + "\nTrigger: " + bt2.setV);
                  builder.setPositiveButton("OK", null);
                  builder.setNeutralButton("Edit", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Dialog dl = new Dialog(a2dp.Vol.main.this);
						dl.setContentView(R.layout.editdata);
						dl.setCancelable(true);
					    
					     final TextView t1 = (TextView) dl.findViewById(R.id.Textd1);
					     final TextView tmac = (TextView) dl.findViewById(R.id.Textmac);
					     final EditText t2 = (EditText) dl.findViewById(R.id.EditText01);
					     final CheckBox dv = (CheckBox) dl.findViewById(R.id.DoVol);							
						t1.setText(bt.getDesc1());
						tmac.setText(bt.getMac());
						t2.setText(bt.getDesc2());
						dv.setChecked(bt.isSetV());
						dl.setOnDismissListener(new OnDismissListener(){
							public void onDismiss(DialogInterface dialog) {
								bt.setDesc2(t2.getText().toString());
								bt.setSetV(dv.isChecked());
								myDB.update(bt);
						        refreshList(loadFromDB());
								/*lvl.setAdapter(new ArrayAdapter<String>(a2dp.Vol.main.this, android.R.layout.simple_list_item_1 , lstring));
								lvl.invalidateViews();
								lvl.forceLayout();*/
							}
							
						});
						dl.show(); 		
					}
                  });
                  builder.show();   	              
                }       
        });
        
        uptn.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				btn.setText(str);
				OldVol = setVolume(OldVol + 1, a2dp.Vol.main.this);				
	    	    }
		});
        
        dntn.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				btn.setText(str);
				OldVol = setVolume(OldVol - 1, a2dp.Vol.main.this);	
	    	    }
		});
        
        locbtn.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				Locationbtn();
	    	    }
		});
        
        
        locbtn.setOnLongClickListener(new View.OnLongClickListener() {
			
			public boolean onLongClick(View v) {
				try {
					byte[] buff = new byte[250];
					FileInputStream fs = openFileInput("My_Last_Location2");
					fs.read(buff);
					fs.close();
					String st = new String(buff).trim();
					Toast.makeText(a2dp.Vol.main.this, st, Toast.LENGTH_LONG).show();
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(st) ));
				} catch (FileNotFoundException e) {
					Toast.makeText(a2dp.Vol.main.this, "No data", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				} catch (IOException e) {
					Toast.makeText(a2dp.Vol.main.this, "Some IO issue", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
				return false;
			}
		}) ;
        
        serv.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				if(servrun)
				{
				stopService(new Intent(a2dp.Vol.main.this, service.class));
				serv.setText("Start Service");
				servrun = false;
				}
				else
				{
				startService(new Intent(a2dp.Vol.main.this, service.class));
				serv.setText("Kill Service");
				servrun = true;
				}
			}
		});
        
        refreshList(loadFromDB());
		lvl.setAdapter(new ArrayAdapter<String>(application, android.R.layout.simple_list_item_1 , lstring));
		lvl.invalidateViews();
		lvl.forceLayout();
    }
  

	private void Locationbtn()
    {
		try {
			byte[] buff = new byte[250];
			FileInputStream fs = openFileInput("My_Last_Location");
			fs.read(buff);
			fs.close();
			String st = new String(buff).trim();
			Toast.makeText(a2dp.Vol.main.this, st, Toast.LENGTH_LONG).show();
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(st) ));
		} catch (FileNotFoundException e) {
			Toast.makeText(a2dp.Vol.main.this, "No data", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		} catch (IOException e) {
			Toast.makeText(a2dp.Vol.main.this, "Some IO issue", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}		
    }
    
    private int getBtDevices(){
     	
    	str2 = "No devices found";
    	int i = 0;
    	vec.clear();
    	//a2dp.Vol.main.this.myDB.deleteAll();
    	BluetoothAdapter mBTA = BluetoothAdapter.getDefaultAdapter();
    	if(mBTA != null)
    	{
	    	Set<BluetoothDevice> pairedDevices = mBTA.getBondedDevices();
	    	// If there are paired devices
	    	
	    	if (pairedDevices.size() > 0) 
	    	{
					str2 = "Devices: ";	    	    
					// Loop through paired devices
	    	    for (BluetoothDevice device : pairedDevices) 
	    	    {  	    	
	    	    	// Add the name and address to an array adapter to show in a ListView
	    	    	if(device.getBluetoothClass().hasService(BluetoothClass.Service.AUDIO))
	    	    		{	 
	    	    		btDevice bt = new btDevice();    	    		
	    	    		str2 += "\n" + device.getName()+ "  " + device.getAddress() + "\n"; 
	    	    		i++;

		    	    	bt.setBluetoothDevice(device, device.getName(), am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
	    	    		btDevice bt2 = myDB.getBTD(bt.mac);	
	    	    		if(bt2.mac == null)
	    	    		{
	    	    			a2dp.Vol.main.this.myDB.insert(bt);	    	    			    	    			
	    	    			vec.add(bt);
	    	    		}
	    	    		else
	    	    			vec.add(bt2);
	    	    		}
	    	    }
	    	}
    	}
    	
    	// the section below is for testing only.  Comment out before building the application for use.
    	btDevice bt = new btDevice();
    	bt.setBluetoothDevice("Device 1", "Porsche", "00:22:33:44:55:66:77", 15);
    	i = 1;
    	btDevice btx = myDB.getBTD(bt.mac);	
		if(btx.mac == null)
		{
			a2dp.Vol.main.this.myDB.insert(bt);	    	    			    	    			
			vec.add(bt);
		}
		else
			vec.add(btx);
		
    	btDevice bt2 = new btDevice();
    	bt2.setBluetoothDevice("Device 2", "Jaguar", "33:44:55:66:77:00:22", 14);
    	btDevice bty = myDB.getBTD(bt2.mac);	
    	i = 2;
		if(bty.mac == null)
		{
			a2dp.Vol.main.this.myDB.insert(bt2);	    	    			    	    			
			vec.add(bt2);
		}
		else
			vec.add(bty);
		
        List<String> names = this.myDB.selectAll();
        StringBuilder sb = new StringBuilder();
        sb.append("Names in database:\n");
        for (String name : names) {
           sb.append(name + "\n");
        }
        // end of testing code
        
    	refreshList(loadFromDB());

	return i;
	}
    
    private static int setVolume(int inputVol, Context sender)
    {
       	int outVol;
       	if(inputVol < 0) inputVol = 0;
       	if(inputVol > am.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
       		inputVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
       	
		am.setStreamVolume(AudioManager.STREAM_MUSIC, inputVol, 0);
		outVol = am.getStreamVolume(AudioManager.STREAM_MUSIC); 
		tx1.setText("Old Volume:" + OldVol + "  New Volume:" + outVol + "\n");
		
		//Toast.makeText(sender, "Stored Volume:" + OldVol + "  New Volume:" + outVol, Toast.LENGTH_LONG).show();
		return outVol;
    }
           
    @Override
    protected void onStop(){
       super.onStop();

      // We need an Editor object to make preference changes.
      // All objects are from android.context.Context
      SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
      SharedPreferences.Editor editor = settings.edit();
      //editor.putBoolean("silentMode", mSilentMode);
      
      // Commit the edits!
      editor.commit();
    }
    
    private void refreshList(int test){
		if(test > 0){
			lstring = new String[test];
			for(int i =0;i<test;i++)
			{
				lstring[i] = vec.get(i).toString();
			}
			// somehow I need to update the listview here but I can't from a thread that is not on the UI
		}
		else 
		Toast.makeText(this, "No data", Toast.LENGTH_LONG);
    }
    
    private int loadFromDB(){
    	vec = myDB.selectAlldb();
    	
    	return vec.size();
    }
}