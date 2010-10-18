package a2dp.Vol;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.text.format.Time;
import android.widget.Toast;



public class service extends Service {

	static AudioManager am2 = (AudioManager) null;
	static Integer OldVol2 = 5;
	public static boolean run = false;
    LocationManager lm2 = null;
    static BluetoothDevice btConn = null;
    private DeviceDB DB;  // database of device data stored in SQlite
    private LocationManager locationManager;
    private Location location2;
    private Location location_old;

    public static final String PREFS_NAME = "btVol";
    float MAX_ACC = 20; // worst acceptable location in meters
    long MAX_TIME = 10000;  // worst acceptable time in milliseconds
 	SharedPreferences preferences;   
 		
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {

		//preferences = PreferenceManager.getDefaultSharedPreferences(this);
		preferences = getSharedPreferences(PREFS_NAME,0);
		MAX_ACC = preferences.getFloat("gpsDistance", MAX_ACC);
		MAX_TIME = preferences.getLong("gpsTime", MAX_TIME);
		
        // create intent filter for a bluetooth stream connection
        IntentFilter filter = new IntentFilter(android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED);
        this.registerReceiver(mReceiver, filter);
        
        // create intent filter for a bluetooth stream disconnection
        IntentFilter filter2 = new IntentFilter(android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver2, filter2);
	    // capture original volume
        am2 = (AudioManager) getSystemService(Context.AUDIO_SERVICE) ;
        run = true;
        this.DB = new DeviceDB(this);
     // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        location2 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        Toast.makeText(this, "A2DP Vol Service Started", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onDestroy() {
        run = false;
        clearLoc();
		Toast.makeText(this, "A2DP Vol Service Stopped", Toast.LENGTH_LONG).show();
	}
	

	public void onStart() {
		getOldvol();
		run = true;
		MAX_ACC = preferences.getFloat("gpsDistance", MAX_ACC);
		MAX_TIME = preferences.getLong("gpsTime", MAX_TIME);
	}
	
	
	   private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	        	int maxvol = am2.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	        	boolean setvol = true;
		        getOldvol();        	
		        BluetoothDevice bt = (BluetoothDevice) intent.getExtras().get(BluetoothDevice.EXTRA_DEVICE);
	            btConn = bt;	            
	            
	            if(true) 
	            {
	            	btDevice bt2 = null;
					try {
						String addres = btConn.getAddress();
						bt2 = DB.getBTD(addres);
	            	Toast.makeText(context, bt2.desc2, Toast.LENGTH_LONG).show();					
	            	} catch (Exception e) {
	            		Toast.makeText(context, btConn.getAddress() + "\n" + e.getMessage(), Toast.LENGTH_LONG);
						bt2 = null;
					}

		        	if(bt2 != null)
		        	{
		        		maxvol = bt2.getDefVol();
		        		setvol = bt2.isSetV();
		        	}

		        	if(setvol) setVolume(maxvol, a2dp.Vol.service.this);	            	
	            }
	        	
	          }
	        };
	    
	    private final BroadcastReceiver mReceiver2 = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context2, Intent intent2) {
	        	
	            setVolume(OldVol2,  a2dp.Vol.service.this);
	            // make sure we turn OFF the location listener if we don't get a loc in 15s
	            new CountDownTimer(15000, 5000) {

	                public void onTick(long millisUntilFinished) {
	                	//Toast.makeText(a2dp.Vol.service.this, "Time left: " + millisUntilFinished / 1000, Toast.LENGTH_LONG).show();
	                }

	                public void onFinish() {
	                    clearLoc();
	                }
	             }.start();
	             
	            // start location provider GPS 
		         // Register the listener with the Location Manager to receive location updates
	             if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
		            {
		            	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		            	//Toast.makeText(this, " Location Manager stated", Toast.LENGTH_LONG).show();
		            	
		            }
	             
	             // get best location and store it
	            grabGPS();
	            
	            }
	        };
	        
	        public static int setVolume(int inputVol, Context sender)
	        {
	           	int outVol;
	           	if(inputVol < 0) inputVol = 0;
	           	if(inputVol > am2.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
	           		inputVol = am2.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	           	
	    		am2.setStreamVolume(AudioManager.STREAM_MUSIC, inputVol, 0);
	    		outVol = am2.getStreamVolume(AudioManager.STREAM_MUSIC); 
	
	    		Toast.makeText(sender, "Stored Volume:" + OldVol2 + "  New Volume:" + outVol, Toast.LENGTH_LONG).show();
	    		return outVol;
	        }
	        
	        private void getOldvol(){
	    		if(OldVol2 < am2.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
	    		{
	    			OldVol2 = am2.getStreamVolume(AudioManager.STREAM_MUSIC);
	    		}
	    		else
	    		{
	    			OldVol2 = am2.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	    		}
	        }
	        
	        private double[] getGPS2()  {
	        	
	        	LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);  
	        	List<String> providers = lm.getProviders(true);
	        
	        	Location l = null;
	        	Location l2 = null;
	        	Location l3 = null;

	        	long deltat = 9999999;
        		long olddt = 9999999;
        		float oldacc = 999999;
        		
	        	if(!providers.isEmpty()){
	        		for (int i=providers.size()-1; i>=0; i--) {
		        		l2 = lm.getLastKnownLocation(providers.get(i));
	
		        		if (l2 != null) 
		        		{
							if(l2.hasAccuracy()) // if we have accuracy, capture the best
							{
								if(l2.getAccuracy() < oldacc)
									{
									l3 = l2;
									oldacc = l2.getAccuracy();
									}
							}
		            		olddt = deltat;	
		            		deltat = System.currentTimeMillis() - l2.getTime() ;
		        			if( deltat < olddt) // get the most recent update
		            			{      			
		            			l = l2;
		            			}      		
		        		}
		        	}
	        	}
	        	else 
	        		return null;
	        	
	        	double[] gps = new double[8];
	        	
	        	if(l != null){
	        		gps[0] = l.getLatitude();
	        		gps[1] = l.getLongitude();
	        		gps[2] = l.getAccuracy();
	        		gps[3] = l.getTime();
	        		location_old = l;
	        	}
	        	else
	        		return null;
	        	
	        	if(l3 != null){
	        		gps[4] = l3.getLatitude();
	        		gps[5] = l3.getLongitude();
	        		gps[6] = l3.getAccuracy();
	        		gps[7] = l3.getTime();
	        	}
	        	else 
	        		return null;
	        	
	        	if(locationListener != null && l != null){
	        		if(location2.getAccuracy() < MAX_ACC && (System.currentTimeMillis() - location2.getTime()) < MAX_TIME)
	        			clearLoc();
	        	}
					
	        	return gps;
	        	}
	        
	        void grabGPS()
	        {        
	        	double[] gloc;
				try {
					gloc = getGPS2();
				} catch (Exception e1) {
					return;
				}	            
					
	        	DecimalFormat df = new DecimalFormat("#.#"); 				

				if(gloc != null){
					try {
						FileOutputStream fos = openFileOutput("My_Last_Location", Context.MODE_WORLD_READABLE);
						Time t = new Time();
						t.set((long)gloc[3]);
						String temp = "http://maps.google.com/maps?q=" + gloc[0] + "," + gloc[1] + "+" +
								"(My Car " + t.format("%D, %r") + " acc=" + df.format(gloc[2]) + ")";
						fos.write(temp.getBytes());
						fos.close();
		            	//Toast.makeText(a2dp.Vol.service.this, temp, Toast.LENGTH_LONG).show();
					} 
		            catch (FileNotFoundException e) {
		            	Toast.makeText(a2dp.Vol.service.this, "FileNotFound", Toast.LENGTH_LONG).show();
						e.printStackTrace();
					} 
		            catch (IOException e) {
		            	Toast.makeText(a2dp.Vol.service.this, "IOException", Toast.LENGTH_LONG).show();
		            	e.printStackTrace();
					}
		            
					try {
						FileOutputStream fos = openFileOutput("My_Last_Location2", Context.MODE_WORLD_READABLE);
						Time t = new Time();
						t.set((long)gloc[7]);
						String temp = "http://maps.google.com/maps?q=" + gloc[4] + "," + gloc[5] + "+" +
								"(My Car " + t.format("%D, %r") + " acc=" + df.format(gloc[6]) + ")";
						fos.write(temp.getBytes());
						fos.close();
		            	//Toast.makeText(a2dp.Vol.service.this, temp, Toast.LENGTH_LONG).show();
					} 
		            catch (FileNotFoundException e) {
		            	Toast.makeText(a2dp.Vol.service.this, "FileNotFound", Toast.LENGTH_LONG).show();
						e.printStackTrace();
					} 
		            catch (IOException e) {
		            	Toast.makeText(a2dp.Vol.service.this, "IOException", Toast.LENGTH_LONG).show();
		            	e.printStackTrace();
					}
				}
	        }
	        
			// Define a listener that responds to location updates
			LocationListener locationListener = new LocationListener() {
			    public void onLocationChanged(Location location) {
			      // Called when a new location is found by the network location provider.
			      location2 = location;
			      // since we know this is a new location, just check the accuracy
			      float acc = location.getAccuracy();
			      float acc2 = acc;
			      if(location_old.hasAccuracy())acc2 = location_old.getAccuracy();
			      
			      if((acc < MAX_ACC || acc < acc2) && acc != 0){
			    	  grabGPS();
			      }

			    }

			    public void onProviderEnabled(String provider) {}

			    public void onProviderDisabled(String provider) {}

				public void onStatusChanged(String provider, int status,
						Bundle extras) {
					// TODO Auto-generated method stub
					
				}
			  };
			  
			  private void clearLoc(){
					locationManager.removeUpdates(locationListener);
					//Toast.makeText(a2dp.Vol.service.this, " Location Manager stopped", Toast.LENGTH_LONG).show();
			  }
			  
}
