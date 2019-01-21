package a2dp.Vol;

import java.net.URISyntaxException;
import java.util.Objects;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CustomIntentMaker extends Activity {

    private EditText mEtAction, mEtData, mEtType;
    private Button mBtnOk, mBtnCancel, mBtnTest;
    private String mAction, mData, mType;
    private Intent returnIntent;
    private MyApplication application;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_intent);
        setTitle("Custom Intent...");

        mAction = getIntent().getStringExtra("alarm_custom_action");
        mData = getIntent().getStringExtra("alarm_custom_data");
        mType = getIntent().getStringExtra("alarm_custom_type");


        initViews();
        assignListeners();
        //mBtnOk.setClickable(false);
        mBtnOk.setEnabled(false);
        //Bundle extras = getIntent().getExtras();
        this.application = (MyApplication) this.getApplication();

        // Load the data that was sent from the database
        if(mAction != null)mEtAction.setText(mAction);
        if(mData != null)mEtData.setText(mData);
        if(mType != null)mEtType.setText(mType);
    }


    private void initViews() {
        mEtAction = findViewById(R.id.ci_et_action);
        mEtData = findViewById(R.id.ci_et_data);
        mEtType = findViewById(R.id.ci_et_type);

        mBtnOk = findViewById(R.id.ci_btn_ok);
        mBtnCancel = findViewById(R.id.ci_btn_cancel);
        mBtnTest = findViewById(R.id.ci_btn_test);
    }

    private void assignListeners() {
        mBtnOk.setOnClickListener(mBtnOkOnClick);
        mBtnCancel.setOnClickListener(mBtnCancelOnClick);
        mBtnTest.setOnClickListener(mBtnTestOnClick);
    }

    private View.OnClickListener mBtnOkOnClick = new View.OnClickListener() {

        public void onClick(View v) {

            setResult(RESULT_OK, returnIntent);
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
            if (action.length() < 3 ) return;

            Intent i = new Intent();
            if (isShortcutIntent(data)) {
                try {
                    i = Intent.parseUri(data, Intent.URI_INTENT_SCHEME);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            } else {
                if (action != null && !action.equals(""))
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

            // add extra for referrer used for apps like Spotify
            i.putExtra(Intent.EXTRA_REFERRER,
                    Uri.parse("android-app://" + application.getPackageName()));

/*			if (Intent.ACTION_CALL.equals(i.getAction())) {
				AudioManager am = (AudioManager)getBaseContext().getSystemService(AUDIO_SERVICE);
				Objects.requireNonNull(am).setMode(AudioManager.MODE_IN_CALL);
				am.setSpeakerphoneOn(true);
				am.setStreamVolume(AudioManager.STREAM_VOICE_CALL, am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), AudioManager.FLAG_SHOW_UI);

				
			}*/

            //TEST CODE
//			i = new Intent(Intent.ACTION_VIEW);
//			i.setData(Uri.parse("http://woxy.lala.com/stream/vintage-mpg64.pls"));
//			i.setComponent(new ComponentName("com.streamfurious.android.pro", "com.streamfurious.android.activities.PlayerActivity"));
//			i.putExtra(mEtAction.getText().toString(), true);
//			mEtData.setText(i.toUri(Intent.URI_INTENT_SCHEME));

            // Verify that the intent will resolve to an activity
            if (i.resolveActivity(getPackageManager()) != null) {
                returnIntent = i;
                //startActivity(i);
                mBtnOk.setEnabled(true);
            } else {
                Toast.makeText(application, "Invalid Intent",Toast.LENGTH_LONG).show();
            }
/*			try {
				startActivity(i);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}*/

        }

    };

    public static boolean isShortcutIntent(String data) {
        String lcase = data.toLowerCase();
        return lcase.startsWith("intent:") || lcase.contains("#intent");
    }


}
