package com.group6.runningassistant;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

public class RecordData extends Activity implements OnClickListener {

	private Chronometer chronometer;
	long timeWhenStopped = 0;
	long t;

	private static final int STEPS_MSG = 1;
	private static final int PACE_MSG = 2;
	private static final int DISTANCE_MSG = 3;
	private static final int SPEED_MSG = 4;
	private static final int CALORIES_MSG = 5;

	private static final String TAG = "RecordData";
	private SharedPreferences mSettings;
	private PedometerSettings mPedometerSettings;
	private Utils mUtils;

	private TextView mPaceValueView;
	private TextView mDistanceValueView;
	private TextView mSpeedValueView;
	private TextView mCaloriesValueView;

	private int mPaceValue;
	private float mDistanceValue;
	private float mSpeedValue;
	private int mCaloriesValue;
	private float mDesiredPaceOrSpeed;
	private int mMaintain;
	private boolean mIsMetric;
	private float mMaintainInc;
	private boolean mQuitting = false;

	private boolean mIsRunning;

	private RecordService mService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "[ACTIVITY] onCreate");
		setContentView(R.layout.data);

		mPaceValue = 0;

		chronometer = (Chronometer) findViewById(R.id.chronometer1);
		if (t != 0)
			display_time();
		else
			chronometer.setText("00:00:00");

		((Button) findViewById(R.id.btnstart)).setOnClickListener(this);
		((Button) findViewById(R.id.btnstop)).setOnClickListener(this);
		((Button) findViewById(R.id.btnresume)).setOnClickListener(this);
		((Button) findViewById(R.id.btnreset)).setOnClickListener(this);

		chronometer
				.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
					@Override
					public void onChronometerTick(Chronometer chronometer) {

						t = SystemClock.elapsedRealtime()
								- chronometer.getBase();
						display_time();
					}
				});

		mUtils = Utils.getInstance();
	}
	
    @Override
    protected void onStart() {
        Log.i(TAG, "[ACTIVITY] onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "[ACTIVITY] onResume");
        super.onResume();
        
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        mPedometerSettings = new PedometerSettings(mSettings);
        
        mUtils.setSpeak(mSettings.getBoolean("speak", false));
        
        // Read from preferences if the service was running on the last onPause
        mIsRunning = mPedometerSettings.isServiceRunning();
        
        // Start the service if this is considered to be an application start (last onPause was long ago)
        if (!mIsRunning && mPedometerSettings.isNewStart()) {
            startRunningService();
            bindRunningService();
        }
        else if (mIsRunning) {
            bindRunningService();
        }
        
        mPedometerSettings.clearServiceRunning();

        //mPaceValueView     = (TextView) findViewById(R.id.pace_value);
        mDistanceValueView = (TextView) findViewById(R.id.tv_distance_value);
        mSpeedValueView    = (TextView) findViewById(R.id.tv_speed_value);
       //mCaloriesValueView = (TextView) findViewById(R.id.calories_value);
        //mDesiredPaceView   = (TextView) findViewById(R.id.desired_pace_value);

        mIsMetric = mPedometerSettings.isMetric();
    }
    
    
    @Override
    protected void onPause() {
        Log.i(TAG, "[ACTIVITY] onPause");
        if (mIsRunning) {
            unbindRunningService();
        }
        if (mQuitting) {
            mPedometerSettings.saveServiceRunningWithNullTimestamp(mIsRunning);
        }
        else {
            mPedometerSettings.saveServiceRunningWithTimestamp(mIsRunning);
        }

        super.onPause();
        savePaceSetting();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "[ACTIVITY] onStop");
        super.onStop();
    }

    protected void onDestroy() {
        Log.i(TAG, "[ACTIVITY] onDestroy");
        super.onDestroy();
    }
    
    protected void onRestart() {
        Log.i(TAG, "[ACTIVITY] onRestart");
        super.onDestroy();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle save) {
//        mStepValueView     = (TextView) findViewById(R.id.step_value);
//        if(mStepValueView != null){
//            save.putString("steps",mStepValueView.getText().toString());
//        }
            super.onSaveInstanceState(save);
        
     }
    @Override
    protected void onRestoreInstanceState(Bundle saved) {
//        mStepValueView     = (TextView) findViewById(R.id.step_value);
//        if(mStepValueView != null){
//            mStepValueView.setText(saved.getString("steps","0"));
//        }
        super.onRestoreInstanceState(saved);
    
      }
    
    private void savePaceSetting() {
        mPedometerSettings.savePaceOrSpeedSetting(mMaintain, mDesiredPaceOrSpeed);
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnstart:
			chronometer
					.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
			chronometer.start();
			startRunningService();
			bindRunningService();
			break;
		case R.id.btnstop:
			timeWhenStopped = chronometer.getBase()
					- SystemClock.elapsedRealtime();
			chronometer.stop();
			unbindRunningService();
			stopRunningService();
			break;
		case R.id.btnresume:
			startRunningService();
			bindRunningService();
			break;
		case R.id.btnreset:
			resetValues(true);
			break;
		}
	}

	public void display_time() {
		int h = (int) (t / 3600000);
		int m = (int) (t - h * 3600000) / 60000;
		int s = (int) (t - h * 3600000 - m * 60000) / 1000;
		String hh = h < 10 ? "0" + h : h + "";
		String mm = m < 10 ? "0" + m : m + "";
		String ss = s < 10 ? "0" + s : s + "";
		chronometer.setText(hh + ":" + mm + ":" + ss);
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = ((RecordService.StepBinder) service).getService();

			mService.registerCallback(mCallback);
			mService.reloadSettings();

		}

		public void onServiceDisconnected(ComponentName className) {
			mService = null;
		}
	};

	private void startRunningService() {
		if (!mIsRunning) {
			Log.i(TAG, "[SERVICE] Start");
			mIsRunning = true;
			startService(new Intent(RecordData.this, RecordService.class));
		}
	}

	private void bindRunningService() {
		Log.i(TAG, "[SERVICE] Bind");
		bindService(new Intent(RecordData.this, RecordService.class),
				mConnection, Context.BIND_AUTO_CREATE
						+ Context.BIND_DEBUG_UNBIND);
	}

	private void unbindRunningService() {
		Log.i(TAG, "[SERVICE] Unbind");
		unbindService(mConnection);
	}

	private void stopRunningService() {
		Log.i(TAG, "[SERVICE] Stop");
		if (mService != null) {
			Log.i(TAG, "[SERVICE] stopService");
			stopService(new Intent(RecordData.this, RecordService.class));
		}
		mIsRunning = false;
	}

	private void resetValues(boolean updateDisplay) {
		if (mService != null && mIsRunning) {
			mService.resetValues();
		} else {

//			mPaceValueView.setText("0");
			mDistanceValueView.setText("0");
			mSpeedValueView.setText("0");
//			mCaloriesValueView.setText("0");
			SharedPreferences state = getSharedPreferences("state", 0);
			SharedPreferences.Editor stateEditor = state.edit();
			if (updateDisplay) {
//				stateEditor.putInt("pace", 0);
				stateEditor.putFloat("distance", 0.000f);
				stateEditor.putFloat("speed", 0.00f);
//				stateEditor.putFloat("calories", 0);
				stateEditor.commit();
			}
		}
	}

	private RecordService.ICallback mCallback = new RecordService.ICallback() {
        public void stepsChanged(int value) {
            mHandler.sendMessage(mHandler.obtainMessage(STEPS_MSG, value, 0));
        }
		
		public void paceChanged(int value) {
			mHandler.sendMessage(mHandler.obtainMessage(PACE_MSG, value, 0));
		}

		public void distanceChanged(float value) {
			mHandler.sendMessage(mHandler.obtainMessage(DISTANCE_MSG,
					(int) (value * 1000), 0));
		}

		public void speedChanged(float value) {
			mHandler.sendMessage(mHandler.obtainMessage(SPEED_MSG,
					(int) (value * 1000), 0));
		}

//		public void caloriesChanged(float value) {
//			mHandler.sendMessage(mHandler.obtainMessage(CALORIES_MSG,
//					(int) (value), 0));
//		}
	};

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
//			case PACE_MSG:
//				mPaceValue = msg.arg1;
//				if (mPaceValue <= 0) {
//					mPaceValueView.setText("0");
//				} else {
//					mPaceValueView.setText("" + (int) mPaceValue);
//				}
//				break;
			case DISTANCE_MSG:
				mDistanceValue = ((int) msg.arg1) / 1000f;
				if (mDistanceValue <= 0) {
					mDistanceValueView.setText("0");
				} else {
					mDistanceValueView
							.setText(("" + (mDistanceValue + 0.000001f))
									.substring(0, 5));
				}
				break;
			case SPEED_MSG:
				mSpeedValue = ((int) msg.arg1) / 1000f;
				if (mSpeedValue <= 0) {
					mSpeedValueView.setText("0");
				} else {
					mSpeedValueView.setText(("" + (mSpeedValue + 0.000001f))
							.substring(0, 4));
				}
				break;
//			case CALORIES_MSG:
//				mCaloriesValue = msg.arg1;
//				if (mCaloriesValue <= 0) {
//					mCaloriesValueView.setText("0");
//				} else {
//					mCaloriesValueView.setText("" + (int) mCaloriesValue);
//				}
//				break;
			default:
				super.handleMessage(msg);
			}
		}

	};
}