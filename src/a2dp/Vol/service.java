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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.IBinder;
import android.text.format.Time;
import android.widget.Toast;



public class service extends Service {

	static AudioManager am2 = (AudioManager) null;
	static Integer OldVol2 = 5;
	public static boolean run = false;
    LocationManager lm2 = null;
    public static BluetoothDevice btConn = null;
    
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		
        // create intent filter for a bluetooth stream connection
        IntentFilter filter = new IntentFilter(android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED);
        this.registerReceiver(mReceiver, filter);
        
        // create intent filter for a bluetooth stream disconnection
        IntentFilter filter2 = new IntentFilter(android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver2, filter2);
	    // capture original volume
        am2 = (AudioManager) getSystemService(Context.AUDIO_SERVICE) ;
        run = true;
        

        Toast.makeText(this, "A2DP Vol Service Started", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onDestroy() {
        run = false;
		Toast.makeText(this, "A2DP Vol Service Stopped", Toast.LENGTH_LONG).show();
	}
	

	public void onStart() {
		getOldvol();
		run = true;
	}
	
	
	   private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	            if(true) // here is where I need to check to see if this is an a2dp device
	            {
	        	BluetoothDevice bt = (BluetoothDevice) intent.getExtras().get(BluetoothDevice.EXTRA_DEVICE);
	            btConn = bt;
	        	//Toast.makeText(context, bt.getName() + bt.getAddress(), Toast.LENGTH_LONG).show();
	        	getOldvol();
	        	setVolume(am2.getStreamMaxVolume(AudioManager.STREAM_MUSIC), a2dp.Vol.service.this);	            	
	            }
	        	
	          }
	        };
	    
	    private final BroadcastReceiver mReceiver2 = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context2, Intent intent2) {
	        	
	            setVolume(OldVol2,  a2dp.Vol.service.this);
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
	        
	        private double[] getGPS2() {
	        	LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);  
	        	List<String> providers = lm.getProviders(true);
	        
	        	Location l = null;
	        	Location l2 = null;
	        	Location l3 = null;

	        	long deltat = 9999999;
        		long olddt = 9999999;
        		float oldacc = 999999;
        		
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
	        	
	        	double[] gps = new double[8];

	        		gps[0] = l.getLatitude();
	        		gps[1] = l.getLongitude();
	        		gps[2] = l.getAccuracy();
	        		gps[3] = l.getTime();
	        		gps[4] = l3.getLatitude();
	        		gps[5] = l3.getLongitude();
	        		gps[6] = l3.getAccuracy();
	        		gps[7] = l3.getTime();
	        	
	        	return gps;
	        	}
	        
	        void grabGPS()
	        {
/*					lm2 = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
					lm2.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);		*/			
	        	double[] gloc = getGPS2();	            
					DecimalFormat df = new DecimalFormat("#.#"); 				

				try {
					FileOutputStream fos = openFileOutput("My_Last_Location", Context.MODE_WORLD_READABLE);
					Time t = new Time();
					t.set((long)gloc[3]);
					String temp = "http://maps.google.com/maps?q=" + gloc[0] + "," + gloc[1] + "+" +
							"(My Car " + t.format("%D, %r") + " acc=" + df.format(gloc[2]) + ")";
					fos.write(temp.getBytes());
					fos.close();
	            	Toast.makeText(a2dp.Vol.service.this, temp, Toast.LENGTH_LONG).show();
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
	            	Toast.makeText(a2dp.Vol.service.this, temp, Toast.LENGTH_LONG).show();
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
