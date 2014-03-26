package com.group6.runningassistant;

import java.text.DecimalFormat;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

public class StepCounterActivity extends Activity {

	private static final String TAG = "Running Assistant";
	private SharedPreferences mSettings;
	private PedometerSettings mPedometerSettings;
	private Utils mUtils;
	private TextView mStepValueView;
	private int mStepValue;
	private float mDesiredPaceOrSpeed;
	private int mMaintain;
	private boolean mIsMetric;
	private float mMaintainInc;
	private boolean mQuitting = false; // Set when user selected Quit from menu,
										// can be used by onPause, onStop,
										// onDestroy
	private double mCalories;
	private float mDistance;
	private float currentspeed;
	private boolean mIsRunning;
	private float mBodyWeight;
	private TextView speedtext;
	private TextView distancetext;
	private TextView calorietext;
	private static double METRIC_RUNNING_FACTOR = 1.02784823;
	private static double METRIC_WALKING_FACTOR = 0.708;
	private static double RUNNING_SPEED = 5; // assume if the speed is more than
												// 5 mph, the runner is running
	private DecimalFormat df;
	private DecimalFormat dfonedc;
	private Chronometer chronometer;
	private TextView avespeedtext;
	long timeWhenStopped = 0;
	long t;

	private int PREF_MODE = 0;
	private static final String PREF_NAME_USERPROFILE = "UserProfile";
	private static final String KEY_AGE = "age";
	private static final String KEY_WEIGHT = "weight";
	private static final String KEY_HEIGHT = "height";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "[ACTIVITY] onCreate");

		mStepValue = 0;
		mCalories = 0;
		mDistance = 0;
		mUtils = Utils.getInstance();
		mBodyWeight = -1.0f;

		Context context = getApplicationContext();
		SharedPreferences pref = context.getSharedPreferences(
				PREF_NAME_USERPROFILE, PREF_MODE);
		if (pref.getFloat(KEY_WEIGHT, mBodyWeight) > 0f) {
			mBodyWeight = pref.getFloat(KEY_WEIGHT, mBodyWeight);
		}

		setContentView(R.layout.activitystepcounter);
		speedtext = (TextView) findViewById(R.id.speedtext);
		distancetext = (TextView) findViewById(R.id.distance);
		calorietext = (TextView) findViewById(R.id.calorie);
		avespeedtext = (TextView) findViewById(R.id.avespeedtext);
		Button start = (Button) findViewById(R.id.start);
		Button pause = (Button) findViewById(R.id.pause);
		Button reset = (Button) findViewById(R.id.reset);
		Button quit = (Button) findViewById(R.id.quit);
		df = new DecimalFormat();
		dfonedc = new DecimalFormat();
		df.setMaximumFractionDigits(3);
		df.setMinimumFractionDigits(3);
		dfonedc.setMaximumFractionDigits(1);
		dfonedc.setMinimumFractionDigits(1);
		chronometer = (Chronometer) findViewById(R.id.chronometer1);
		if (t != 0)
			display_time();
		else {
			chronometer.setText(getResources().getString(R.string.initclock));
			chronometer
					.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
						@Override
						public void onChronometerTick(Chronometer chronometer) {

							t = SystemClock.elapsedRealtime()
									- chronometer.getBase();
							if (t % 5 == 0 && t != 0) {
								avespeedtext.setText(dfonedc
										.format((mDistance / t))
										+ "   "
										+ getResources().getString(
												R.string.avespeedunit));
							}
							display_time();
						}
					});
		}

		start.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mBodyWeight < 0f) {
					startActivity(new Intent(StepCounterActivity.this,
							UserProfile.class));
					//finish();
				} else if (!mIsRunning) {
					startStepService();
					bindStepService();
					chronometer.setBase(SystemClock.elapsedRealtime()
							+ timeWhenStopped);
					chronometer.start();
				}
			}
		});
		pause.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mIsRunning) {
					unbindStepService();
					stopStepService();
					speedtext.setText(R.string.initspeed);
					timeWhenStopped = chronometer.getBase()
							- SystemClock.elapsedRealtime();
					chronometer.stop();
				}
			}
		});
		reset.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				resetValues(true);
			}
		});
		quit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mIsRunning) {
					unbindStepService();
					stopStepService();
				}
				mQuitting = true;
				resetValues(true);
				finish();

			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.stepcounter, menu);
		return true;
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
		
		Context context = getApplicationContext();
		SharedPreferences pref = context.getSharedPreferences(
				PREF_NAME_USERPROFILE, PREF_MODE);
		Log.i("<WEIGHT>", Float.toString(pref.getFloat(KEY_WEIGHT, mBodyWeight)));
		if (pref.getFloat(KEY_WEIGHT, mBodyWeight) > 0f) {
			mBodyWeight = pref.getFloat(KEY_WEIGHT, mBodyWeight);
		}
		
		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
		mPedometerSettings = new PedometerSettings(mSettings);

		mUtils.setSpeak(mSettings.getBoolean("speak", false));

		// Read from preferences if the service was running on the last onPause
		mIsRunning = mPedometerSettings.isServiceRunning();
		if (mIsRunning) {
			bindStepService();
		}

		mPedometerSettings.clearServiceRunning();

		mStepValueView = (TextView) findViewById(R.id.step_value);
		mIsMetric = mPedometerSettings.isMetric();
	}

	@Override
	protected void onPause() {
		Log.i(TAG, "[ACTIVITY] onPause");
		if (mIsRunning) {
			unbindStepService();
		}
		if (mQuitting) {
			mPedometerSettings.saveServiceRunningWithNullTimestamp(mIsRunning);
		} else {
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

	@Override
	protected void onDestroy() {
		Log.i(TAG, "[ACTIVITY] onDestroy");
		super.onDestroy();
	}

	@Override
	protected void onRestart() {
		Log.i(TAG, "[ACTIVITY] onRestart");
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle save) {
		mStepValueView = (TextView) findViewById(R.id.step_value);
		if (mStepValueView != null) {
			save.putString("steps", mStepValueView.getText().toString());
		}
		super.onSaveInstanceState(save);

	}

	@Override
	protected void onRestoreInstanceState(Bundle saved) {
		mStepValueView = (TextView) findViewById(R.id.step_value);
		if (mStepValueView != null) {
			mStepValueView.setText(saved.getString("steps", "0"));
		}
		super.onRestoreInstanceState(saved);

	}

	private void savePaceSetting() {
		mPedometerSettings.savePaceOrSpeedSetting(mMaintain,
				mDesiredPaceOrSpeed);
	}

	private void startStepService() {
		if (!mIsRunning) {
			Log.i(TAG, "[SERVICE] Start");
			mIsRunning = true;
			startService(new Intent(StepCounterActivity.this, StepService.class));
			startService(new Intent(StepCounterActivity.this, GpsService.class));

		}
	}

	private void bindStepService() {
		Log.i(TAG, "[SERVICE] Bind");
		bindService(new Intent(StepCounterActivity.this, StepService.class),
				mConnection, Context.BIND_AUTO_CREATE
						+ Context.BIND_DEBUG_UNBIND);
		bindService(new Intent(StepCounterActivity.this, GpsService.class),
				gpsConnection, Context.BIND_AUTO_CREATE
						+ Context.BIND_DEBUG_UNBIND);
	}

	private void unbindStepService() {
		Log.i(TAG, "[SERVICE] Unbind");
		unbindService(mConnection);
		unbindService(gpsConnection);
	}

	private void stopStepService() {
		Log.i(TAG, "[SERVICE] Stop");
		if (mService != null) {
			Log.i(TAG, "[SERVICE] stopService");
			stopService(new Intent(StepCounterActivity.this, StepService.class));
			stopService(new Intent(StepCounterActivity.this, GpsService.class));
		}
		mIsRunning = false;
	}

	private StepService mService;
	private GpsService gpsService;

	private void resetValues(boolean updateDisplay) {
		if (mService != null) {
			mService.resetValues();
		} else {
			mStepValueView.setText("0");
			SharedPreferences state = getSharedPreferences("state", 0);
			SharedPreferences.Editor stateEditor = state.edit();
			stateEditor.putInt("steps", 0);
			stateEditor.commit();

		}
		if (gpsService != null) {
			gpsService.reset();
			speedtext.setText(getResources().getString(R.string.initspeed));
			mDistance = 0;
			currentspeed = 0;
			mCalories = 0;
			distancetext.setText(getResources().getString(R.string.initdist));
			calorietext.setText(getResources().getString(R.string.initcalorie));
		}
		chronometer.setText(getResources().getString(R.string.initclock));
		timeWhenStopped = 0;
	}

	private static final int STEPS_MSG = 1;
	private static final int SPEED_MSG = 2;
	private static final int DISTANCE_MSG = 3;

	// TODO: unite all into 1 type of message
	private StepService.ICallback mCallback = new StepService.ICallback() {
		public void stepsChanged(int value) {
			mHandler.sendMessage(mHandler.obtainMessage(STEPS_MSG, value, 0));
		}
	};

	private GpsService.ICallback gpsCallback = new GpsService.ICallback() {

		@Override
		public void speedchanged(float value) {
			mHandler.sendMessage(mHandler.obtainMessage(SPEED_MSG,
					(int) value * 1000000, 0));
		}

		@Override
		public void distanchanged(float value) {
			mHandler.sendMessage(mHandler.obtainMessage(DISTANCE_MSG,
					(int) value * 1000000, 0));

		}
	};

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = ((StepService.StepBinder) service).getService();
			mService.registerCallback(mCallback);
			mService.reloadSettings();

		}

		public void onServiceDisconnected(ComponentName className) {
			mService = null;
		}
	};

	private ServiceConnection gpsConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			gpsService = ((GpsService.GpsBinder) service).getService();
			gpsService.registerCallback(gpsCallback);

		}

		public void onServiceDisconnected(ComponentName className) {
			gpsService = null;
		}

	};

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case STEPS_MSG:
				mStepValue = (int) msg.arg1;
				mStepValueView.setText(mStepValue + "   "
						+ getResources().getString(R.string.stepunit));
				break;
			case SPEED_MSG:
				currentspeed = msg.arg1 / 1000000f;
				speedtext.setText(currentspeed + "   "
						+ getResources().getString(R.string.speedunit));
				break;
			case DISTANCE_MSG:
				float previousdis = mDistance;
				mDistance = msg.arg1 / 1000000f;
				mCalories += (mBodyWeight * ((currentspeed > RUNNING_SPEED) ? METRIC_RUNNING_FACTOR
						: METRIC_WALKING_FACTOR))
						// Distance:
						* (mDistance - previousdis) // centimeters
						/ 100000.0; // centimeters/kilometer
				distancetext.setText(mDistance + "   "
						+ getResources().getString(R.string.distanceunit));
				calorietext.setText(df.format(mCalories) + "   "
						+ getResources().getString(R.string.calorieunit));
				break;
			default:
				super.handleMessage(msg);
			}
		}

	};

	private void display_time() {
		int h = (int) (t / 3600000);
		int m = (int) (t - h * 3600000) / 60000;
		int s = (int) (t - h * 3600000 - m * 60000) / 1000;
		String hh = h < 10 ? "0" + h : h + "";
		String mm = m < 10 ? "0" + m : m + "";
		String ss = s < 10 ? "0" + s : s + "";
		chronometer.setText(hh + ":" + mm + ":" + ss);
	}

}
