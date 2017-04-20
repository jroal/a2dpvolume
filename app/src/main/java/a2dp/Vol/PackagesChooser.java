package a2dp.Vol;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class PackagesChooser extends Activity {

	private List<AppInfoCache> mAppList, mFullAppList;
	private PackageManager pm;
	private ListView mListView;
	private PackageListAdapter mListAdapter;
	private ProgressBar pb;

	private MyApplication application;
	private String[] packages;
	String packagelist;
	SharedPreferences preferences;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_packages_chooser);
		// Show the Up button in the action bar.
		setupActionBar();
		
        this.application = (MyApplication) this.getApplication();
        preferences = PreferenceManager
				.getDefaultSharedPreferences(this.application);
        packagelist = preferences.getString("packages", "com.google.android.talk,com.android.email,com.android.calendar");	
        packages = packagelist.split(",");
        pm = getPackageManager();
        pb = (ProgressBar) findViewById(R.id.progressBar1);
        pb.setIndeterminate(true);
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.packages_chooser, menu);
		mListView = (ListView)findViewById(R.id.PackagelistView1);
	    Thread t = new Thread(mLoadAppLoadAndSortAppList);
	    t.start();
	    
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			cleanup();
			//Toast.makeText(application, "cleaning up", Toast.LENGTH_LONG).show();
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
    private final Handler mHandler = new Handler();
    
	   private Runnable mLoadAppLoadAndSortAppList = new Runnable() {

			public void run() {
		        mAppList = new ArrayList<AppInfoCache>();
		        AppInfoCache tmpCache;
		        List<ApplicationInfo> installedApps = pm.getInstalledApplications(0);
		        for (ApplicationInfo appInfo : installedApps) {
	       			tmpCache = new AppInfoCache(appInfo.loadLabel(pm).toString(), appInfo.packageName, appInfo.className);
	       			mAppList.add(tmpCache);
		        }
		        Collections.sort(mAppList, new AlphaComparator());
		        mFullAppList = new ArrayList<AppInfoCache>();
		        int i = 0;
		        for (AppInfoCache appInfo : mAppList) {
		        	appInfo.setPosition(i);
		        	appInfo.setChecked(Arrays.asList(packages).contains(appInfo.getPackageName()));
		        	mFullAppList.add(appInfo);		        	
		        	i+=1;
		        }
		        mListAdapter = new PackageListAdapter(getBaseContext());
		        mHandler.post(mFinishLoadAndSortTask);
			}
	    	
	    };
	    
	    private void initAssignListenersAndAdapter() {
	    	
		    mListView.setAdapter(mListAdapter);

	    }
	    
	    
	    public class PackageListAdapter extends ArrayAdapter<AppInfoCache> {
	    	Context c;

			public PackageListAdapter(Context context) {
				super(context, R.layout.activity_packages_chooser, mAppList);
				this.c = context;
			}


			@Override
			public View getView(int position, View convertView, ViewGroup parent) {

				final LayoutInflater inflater = LayoutInflater.from(c);
				View v = inflater.inflate(R.layout.package_list_item , parent, false);

				ImageView iv_icon = (ImageView) v.findViewById(R.id.pi_iv_icon);
				TextView tv_name = (TextView) v.findViewById(R.id.pi_tv_name);
				final AppInfoCache ai = getItem(position);
				iv_icon.setImageDrawable(ai.getIcon());
				tv_name.setText(ai.getAppName());
				final CheckBox box = (CheckBox) v.findViewById(R.id.checkBox1);
				box.setChecked(ai.isChecked());
				box.setOnCheckedChangeListener(new OnCheckedChangeListener(){

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						ai.setChecked(box.isChecked());
						
					}
				
				});
				return v;
			}
			
		    
	    	
	    }
	    
	    private Runnable mFinishLoadAndSortTask = new Runnable() {

			public void run() {
				initAssignListenersAndAdapter();
		        pb.setVisibility(ProgressBar.GONE);
		        
			}
	    	
	    };
	    
	    class AppInfoCache {
	    	private String app_name, package_name, class_name;
	    	private int position;
	    	private boolean checked;
	    	
	    	public AppInfoCache(String aName, String pName, String cName) {
	    		app_name = aName;
	    		package_name = pName;
	    		class_name = cName;
	    		position = -1;
	    		setChecked(false);
	    	}
	    	
	    	public Drawable getIcon() {
	    		try {
					return pm.getApplicationIcon(package_name);
				} catch (NameNotFoundException e) {
					return null;
				}
	    	}
	    	
	    	
	    	public int getPosition() {
	    		return position;
	    	}
	    	
	    	public void setPosition (int pos) {
	    		position = pos;
	    	};
	    	
	    	
	    	public String getAppName() {
	    		return app_name;
	    	}
	    	
	    	public String getPackageName() {
	    		return package_name;
	    	}
	    	public String getClassName() {
	    		return class_name;
	    	}
	    	
	    	public String toString() {
	    		return app_name;
	    	}

			public boolean isChecked() {
				return checked;
			}

			public void setChecked(boolean checked) {
				this.checked = checked;
			}
	    }
		
	    class AlphaComparator implements Comparator<AppInfoCache> {
	        private final Collator   sCollator = Collator.getInstance();

	        public final int compare(AppInfoCache a, AppInfoCache b) {
	            String ainfo = a.getAppName();
	            String binfo = b.getAppName();
	            return sCollator.compare(ainfo, binfo);
	        }
	    }


	    private void cleanup(){
	    	
	    	packagelist = "";
	    	int i=0;
			if (mFullAppList != null) {
				if (!mFullAppList.isEmpty()) {
                    for (AppInfoCache info : mFullAppList) {
                        if (info.isChecked()) {
                            if (i > 0)
                                packagelist += ",";

                            packagelist += info.getPackageName();
                        }
                        ++i;
                    }
                }
                else{
                    packagelist = "";
                }
			} else {
				packagelist = "";
			}

			SharedPreferences.Editor editor = preferences.edit();
	    	editor.putString("packages", packagelist);
	    	editor.commit();
	    	Intent intent = new Intent();
	    	intent.setAction("a2dp.vol.Reload");
	    	application.sendBroadcast(intent);
	    }

		/* (non-Javadoc)
		 * @see android.app.Activity#onBackPressed()
		 */
		@Override
		public void onBackPressed() {
			cleanup();
			super.onBackPressed();
		}

		
}
