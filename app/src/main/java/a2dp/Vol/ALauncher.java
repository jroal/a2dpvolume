package a2dp.Vol;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.widget.Toast;

public class ALauncher extends Service {
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	public void onCreate() {

		//Toast.makeText(this, "made it to launcher",Toast.LENGTH_LONG).show();

		try {
			byte[] buff = new byte[250];
			FileInputStream fs = openFileInput("My_Last_Location");
			fs.read(buff);
			fs.close();
			String st = new String(buff).trim();
			// Toast.makeText(this, st, Toast.LENGTH_LONG).show();
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(st));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		} catch (FileNotFoundException e) {
			Toast.makeText(this, "No data", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		} catch (IOException e) {
			Toast.makeText(this, "Some IO issue", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
		super.onCreate();
		this.stopSelf();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
