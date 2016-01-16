package a2dp.Vol;

import java.net.URISyntaxException;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class CustomIntentMaker extends Activity {

	private EditText mEtAction, mEtData, mEtType;
	private Button mBtnOk, mBtnCancel, mBtnTest;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.custom_intent);
		setTitle("Custom Intent...");
		
		initViews();
		assignListeners();
		//mBtnOk.setClickable(false);
		mBtnOk.setEnabled(false);
		//Bundle extras = getIntent().getExtras();
	}
	


	private void initViews() {
		mEtAction = (EditText)findViewById(R.id.ci_et_action);
		mEtData = (EditText)findViewById(R.id.ci_et_data);
		mEtType = (EditText)findViewById(R.id.ci_et_type);
		
		mBtnOk = (Button)findViewById(R.id.ci_btn_ok);
		mBtnCancel = (Button)findViewById(R.id.ci_btn_cancel);
		mBtnTest = (Button)findViewById(R.id.ci_btn_test);
	}
	
	private void assignListeners() {
		mBtnOk.setOnClickListener(mBtnOkOnClick);
		mBtnCancel.setOnClickListener(mBtnCancelOnClick);
		mBtnTest.setOnClickListener(mBtnTestOnClick);
	}
	
	private View.OnClickListener mBtnOkOnClick = new View.OnClickListener() {

		public void onClick(View v) {
			Intent i = new Intent();
			setResult(RESULT_OK, i);
			finish();
		}
		
	};
	private View.OnClickListener mBtnCancelOnClick = new View.OnClickListener() {

		public void onClick(View v) {
			setResult(RESULT_CANCELED);
			finish();
		}
		
	};
	private View.OnClickListener mBtnTestOnClick = new View.OnClickListener() {

		public void onClick(View v) {
			
			
			String action = mEtAction.getText().toString();
			String data = mEtData.getText().toString();
			String type = mEtType.getText().toString();
			if(action.length() < 3 && data.length() < 3 && type.length() < 3) return;
			
			Intent i;
			if (isShortcutIntent(data)) {
				try {
//					i = Intent.parseUri(data, Intent.URI_INTENT_SCHEME);
					i = Intent.getIntent(data);
				} catch (URISyntaxException e) {
					i = new Intent();
					e.printStackTrace();
				}
			} else {
				i = new Intent();
				if(action != null && !action.equals(""))
				i.setAction(action);
				if (!data.equals("")) {
					try {
						i.setData(Uri.parse(data));
					} catch (Exception e) {
						e.printStackTrace();
						return;
					}
				}
				if (!type.equals("")) {
					i.setType(type);
				}
			}
			

			if (Intent.ACTION_CALL.equals(i.getAction())) {
				AudioManager am = (AudioManager)getBaseContext().getSystemService(AUDIO_SERVICE);
				am.setMode(AudioManager.MODE_IN_CALL);
				am.setSpeakerphoneOn(true);
				am.setStreamVolume(AudioManager.STREAM_VOICE_CALL, am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), AudioManager.FLAG_SHOW_UI);

				
			}
			
			//TEST CODE
//			i = new Intent(Intent.ACTION_VIEW);
//			i.setData(Uri.parse("http://woxy.lala.com/stream/vintage-mpg64.pls"));
//			i.setComponent(new ComponentName("com.streamfurious.android.pro", "com.streamfurious.android.activities.PlayerActivity"));
//			i.putExtra(mEtAction.getText().toString(), true);
//			mEtData.setText(i.toUri(Intent.URI_INTENT_SCHEME));
			
			try {
				startActivity(i);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			mBtnOk.setEnabled(true);
		}
		
	};
	public static boolean isShortcutIntent(String data) {
		String lcase = data.toLowerCase();
		return lcase.startsWith("intent:") || lcase.contains("#intent");
	}
	
	

}
