package a2dp.Vol;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ProviderList extends ListActivity {
	public static final String KEY_ID = "_id";
	
	public static final int MI_TYPE_STANDARD = 1; //the data key represents an intent string
	public static final int MI_TYPE_APPEND_VIEW = 2; //the data key represents a path to be appended to the provider uri - action is ACTION_VIEW
	public static final int MI_TYPE_CUSTOM = 3; //inserts data into a custom data string, action is still view
	

	public static int PROVIDER_PANDORA = 0;
	public static int PROVIDER_HOMESCREEN = 1;
	public static int PROVIDER_HOMESCREEN2 = 2;
	public static int PROVIDER_GOOGLE_LISTEN = 3;
	public static int PROVIDER_HTC_SENSE = 4;
	
	public static final String[] P_URI_STRINGS = new String[] {
		"content://com.pandora.provider/stations", 
		"content://com.android.launcher.settings/favorites",
		"content://com.android.launcher2.settings/favorites",
		"content://com.google.android.apps.listen.PodcastProvider/item",
		"content://com.htc.launcher.settings/favorites",
	};
	
	public static final String[] P_CUSTOM_DATA_STRINGS = new String[] {
		null, 
		null,
		null,
		"http://listen.googlelabs.com/listen?id=@@",
		null
	};
	
	public static final String[] P_PACKAGE_NAMES = new String[] {
		"com.pandora.android",
		"com.android.launcher",
		"com.android.launcher2",
		"com.google.android.apps.listen",
		"com.htc.launcher"
	};
	
	public static final String[] P_TITLE_KEYS = new String[] {
		"stationName",
		"title",
		"title",
		"title",
		"title"
	};
	
	public static final String[] P_DATA_KEYS = new String[] {
		"stationToken",
		"intent",
		"intent",
		"guid",
		"intent"
	};
	
	
	public static final String[] P_WHERE_KEYS = new String[] {
		null,
		"intent!=\"\"",
		"intent!=\"\"",
		null,
		"intent!=\"\""
	};
	
	public static final int[] P_MI_TYPES = new int[] {
		MI_TYPE_APPEND_VIEW,
		MI_TYPE_STANDARD,
		MI_TYPE_STANDARD,
		MI_TYPE_CUSTOM,
		MI_TYPE_STANDARD
	};
	
	public static final String[] P_WINDOW_TITLES = new String[] {
		"Select a Pandora Favorite...",
		"Select a Shortcut from your Home Screen...",
		"Select a Shortcut from your Home Screen...",
		"Select a Feed from Google's Listen",
		"Select a Shortcut from your Home Screen..."
	};
	
	public static final String[] P_EMPTY_LIST_MSGS = new String[] {
		"It looks like you don't have any Pandora Radio stations set up. This usually means either Pandora is not installed or you haven't logged into it yet. Please try starting Pandora manually and make sure your stations show up, then try again.",
		"It looks like there was an error reading the shortcuts from your home screen (or you don't have any installed).",
		"It looks like there was an error reading the shortcuts from your home screen (or you don't have any installed).",
		"It looks like you don't have any subscriptions set up in Google's Listen. Please close AppAlarm and make sure your subscriptions show up in Listen.",
		"It looks like there was an error reading the shortcuts from your home screen (or you don't have any installed)."
	};
	

	
	public static String EXTRA_PROVIDER = "extra_provider";
	public static String EXTRA_PACKAGE_NAME = "extra_package_name";


	private int mProvider;
	private CursorAdapter mListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.pandora_station_list);
		super.onCreate(savedInstanceState);
		
		mProvider = getIntent().getIntExtra(EXTRA_PROVIDER, 0);
		
		setTitle(P_WINDOW_TITLES[mProvider]);
		((TextView)getListView().getEmptyView()).setText(P_EMPTY_LIST_MSGS[mProvider]);
		
		loadList();
	}
	
	private void loadList() {
		Cursor c = null;
		try {
			c = managedQuery(
				Uri.parse(P_URI_STRINGS[mProvider]), 
				new String[] {KEY_ID, P_TITLE_KEYS[mProvider]}, 
				P_WHERE_KEYS[mProvider], 
				null, 
				P_TITLE_KEYS[mProvider]);
			
			
			if (c == null) {
				if (mProvider == PROVIDER_HOMESCREEN) {
					Log.d("AppAlarm", "Error reading from Launcher1, trying Launcher2");
					mProvider = PROVIDER_HOMESCREEN2;
					loadList();
				} else if (mProvider == PROVIDER_HOMESCREEN2) {
					Log.d("AppAlarm", "Error reading from Launcher2, trying Sense");
					mProvider = PROVIDER_HTC_SENSE;
					loadList();
				}
			} else {
				mListAdapter = new SimpleCursorAdapter(
						this, 
						R.layout.pandora_station_item, 
						c, 
						new String[] {P_TITLE_KEYS[mProvider]}, 
						new int[] {R.id.psi_tv_station_name});
					setListAdapter(mListAdapter);
			}
				
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		setContentView(R.layout.pandora_station_list);
		super.onConfigurationChanged(newConfig);
		try {
			setListAdapter(mListAdapter);
		} catch (Exception e) {}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Cursor c = getContentResolver().query(Uri.parse(P_URI_STRINGS[mProvider]),
				new String [] {P_TITLE_KEYS[mProvider], P_DATA_KEYS[mProvider]},
				KEY_ID + "=" + id,
				null,
				null);
		c.moveToFirst();
		
		String title = c.getString(c.getColumnIndexOrThrow(P_TITLE_KEYS[mProvider]));
		String data = c.getString(c.getColumnIndexOrThrow(P_DATA_KEYS[mProvider]));
		c.close();
		
		Intent rtrIntent = new Intent();
		rtrIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
		if (mProvider != PROVIDER_HOMESCREEN && mProvider != PROVIDER_HOMESCREEN2 && mProvider != PROVIDER_HTC_SENSE) {
			rtrIntent.putExtra(EXTRA_PACKAGE_NAME, P_PACKAGE_NAMES[mProvider]);
		}
		rtrIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, getSelectedIntent(data));
		
		
		setResult(RESULT_OK, rtrIntent);
		finish();
//		startActivity(getSelectedIntent(data));
	}

	
	private Intent getSelectedIntent(String data) {
		Intent nI = null;
		switch(P_MI_TYPES[mProvider]) {
		case MI_TYPE_APPEND_VIEW: 
			nI = new Intent(Intent.ACTION_VIEW);
			nI.setData(Uri.withAppendedPath(Uri.parse(P_URI_STRINGS[mProvider]), data));
			return nI;
		case MI_TYPE_STANDARD:
			try {
				return Intent.getIntent(data);
			} catch (URISyntaxException e) {
				return null;
			}
		case MI_TYPE_CUSTOM:
			nI = new Intent(Intent.ACTION_VIEW);
			String newData = null;
				try {
					newData = P_CUSTOM_DATA_STRINGS[mProvider].replaceAll("@@", URLEncoder.encode(data, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			Toast.makeText(getBaseContext(), newData, Toast.LENGTH_LONG).show();
			nI.setData(Uri.parse(newData));
			return nI;
		}
		return null;
	}
	
//	private class CustomNoIdAdapter extends CursorAdapter {
//
//		public CustomNoIdAdapter(Context context, Cursor c) {
//			super(context, c);
//		}
//
//		@Override
//		public void bindView(View view, Context context, Cursor cursor) {
//			fillView(cursor, view);
//		}
//
//		@Override
//		public View newView(Context context, Cursor cursor, ViewGroup parent) {
//			final LayoutInflater inflater = LayoutInflater.from(context);
//			View v = inflater.inflate(R.layout.pandora_station_item, parent, false);
//			return v;
//		}
//		
//		
//		private void fillView(Cursor cur, View row) {
//			TextView tv = (TextView)row.findViewById(R.id.psi_tv_station_name);
//			tv.setText(cur.getString(cur.getColumnIndexOrThrow(P_TITLE_KEYS[mProvider])));
//			tv.setTag(cur.getString(cur.getColumnIndexOrThrow(P_DATA_KEYS[mProvider])));
//		}
//	}


}
