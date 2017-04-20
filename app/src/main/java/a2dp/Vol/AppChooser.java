package a2dp.Vol;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class AppChooser extends Activity {
	public static final String EXTRA_PACKAGE_NAME = "package_name";

	
	private ListView mListView;
	private PackageListAdapter mListAdapter;
	private EditText mEtFilter;
	private Button mBtnSearch, mBtnClear; 
	private ProgressDialog mLoadingDialog;
	private String mFilterText;

	
	private List<AppInfoCache> mAppList, mFullAppList;
	
	private PackageManager pm;
	
    public View.OnClickListener mSearchBtnListenerListner = new View.OnClickListener() {

		public void onClick(View v) {
			doListFilter();
		}
    	
    };
    public View.OnClickListener mClearBtnListenerListner = new View.OnClickListener() {

		public void onClick(View v) {
			mEtFilter.setText("");
			doListFilter();
		}
    	
    };

    public EditText.OnEditorActionListener mSearchActionListener = new EditText.OnEditorActionListener() {

		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			doListFilter();
			return false;
		}
    	
    };
    public EditText.OnKeyListener mSearchBoxKeyListener = new EditText.OnKeyListener() {

		public boolean onKey(View arg0, int keycode, KeyEvent arg2) {
			if (keycode == KeyEvent.KEYCODE_ENTER) {
				doListFilter();
				return true;
			} else {
				return false;
			}
		}
    	
    };
    
    
    public void doListFilter() {

		mFilterText = mEtFilter.getText().toString().toLowerCase();
		
		mAppList.clear();
		if (mFilterText.contentEquals("")) {
			for (AppInfoCache appInfo : mFullAppList) {
				mAppList.add(appInfo);
			}
		} else {
			for (AppInfoCache appInfo : mFullAppList) {
				if (appInfo.getAppName().toLowerCase().contains(mFilterText)) {
					mAppList.add(appInfo);
				}
			}
		}
		mListAdapter.notifyDataSetChanged();
		
		
    }
    
    public AdapterView.OnItemClickListener mListItemClickAdapter = new AdapterView.OnItemClickListener() {

		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Intent i = new Intent();
			i.putExtra(EXTRA_PACKAGE_NAME, mAppList.get(position).getPackageName());
			setResult(RESULT_OK, i);
			finish();
			
		}
    	
    };

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_list);
		setTitle("Select an app...");
		
		initAttachViewsToVars();
        
        
        pm = getPackageManager();
	    mLoadingDialog = new ProgressDialog(this);
	    mLoadingDialog.setIndeterminate(true);
	    mLoadingDialog.setMessage("Loading App List...");
	    mLoadingDialog.setCancelable(false);
	    mLoadingDialog.show();
	
	    Thread t = new Thread(mLoadAppLoadAndSortAppList);
	    t.start();

	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		
		setContentView(R.layout.app_list);
		
		initAttachViewsToVars();
	        
	     initAssignListenersAndAdapter();
	     super.onConfigurationChanged(newConfig);
	}

    private void initAttachViewsToVars() {
        mListView = (ListView)findViewById(R.id.m_lv_packages);
        mEtFilter = (EditText)findViewById(R.id.m_et_search);
        mBtnSearch = (Button)findViewById(R.id.m_btn_search);
        mBtnClear = (Button)findViewById(R.id.m_btn_clear);
    }
    
    private void initAssignListenersAndAdapter() {
    	mEtFilter.setText(mFilterText);
	    mEtFilter.setOnEditorActionListener(mSearchActionListener);
	    mEtFilter.setOnKeyListener(mSearchBoxKeyListener);
	    mListView.setAdapter(mListAdapter);
	    mListView.setOnItemClickListener(mListItemClickAdapter);
	    mBtnSearch.setOnClickListener(mSearchBtnListenerListner);
	    mBtnClear.setOnClickListener(mClearBtnListenerListner);
    }

    
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
	        	mFullAppList.add(appInfo);
	        	i+=1;
	        }
	        mListAdapter = new PackageListAdapter(getBaseContext());
	        mHandler.post(mFinishLoadAndSortTask);
		}
    	
    };
    
    private Runnable mFinishLoadAndSortTask = new Runnable() {

		public void run() {
			initAssignListenersAndAdapter();
	        mLoadingDialog.dismiss();
		}
    	
    };
    
    private final Handler mHandler = new Handler();
    
    public class PackageListAdapter extends ArrayAdapter<AppInfoCache> {
    	Context c;

		public PackageListAdapter(Context context) {
			super(context, R.layout.app_list_item, mAppList);
			this.c = context;
		}



		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			final LayoutInflater inflater = LayoutInflater.from(c);
			View v = inflater.inflate(R.layout.app_list_item , parent, false);


			ImageView iv_icon = (ImageView) v.findViewById(R.id.pi_iv_icon);
			TextView tv_name = (TextView) v.findViewById(R.id.pi_tv_name);

			AppInfoCache ai = getItem(position);
			iv_icon.setImageDrawable(ai.getIcon());
			tv_name.setText(ai.getAppName());
			return v;
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
    
    class AppInfoCache {
    	private String app_name, package_name, class_name;
    	private int position;
    	
    	public AppInfoCache(String aName, String pName, String cName) {
    		app_name = aName;
    		package_name = pName;
    		class_name = cName;
    		position = -1;
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
    }
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
}
