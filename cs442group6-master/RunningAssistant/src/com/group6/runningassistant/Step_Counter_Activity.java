package com.group6.runningassistant;



import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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
import android.widget.TextView;



public class Step_Counter_Activity extends Activity {

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
    private boolean mQuitting = false; // Set when user selected Quit from menu, can be used by onPause, onStop, onDestroy

    private boolean mIsRunning;

    private TextView speedtext;
    private TextView distancetext;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "[ACTIVITY] onCreate");
        mStepValue = 0;
        mUtils = Utils.getInstance();
      
        setContentView(R.layout.activity_step__counter_);
        speedtext = (TextView) findViewById(R.id.speedtext);
        distancetext = (TextView) findViewById(R.id.distance);
        Button start = (Button)findViewById(R.id.start);
        Button pause = (Button)findViewById(R.id.pause);
        Button reset = (Button)findViewById(R.id.reset);
        Button quit = (Button)findViewById(R.id.quit);
       
        
        
     
       
        start.setOnClickListener(new OnClickListener() {
              public void onClick(View v) {
                  if(!mIsRunning){
                      startStepService();
                      bindStepService();
                  }
              }      
        });
        pause.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if(mIsRunning){
                    unbindStepService();
                    stopStepService();
                    speedtext.setText(R.string.initspeed);
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
        getMenuInflater().inflate(R.menu.step__counter_, menu);
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
        
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        mPedometerSettings = new PedometerSettings(mSettings);
        
        mUtils.setSpeak(mSettings.getBoolean("speak", false));
        
        // Read from preferences if the service was running on the last onPause
        mIsRunning = mPedometerSettings.isServiceRunning();
      
        
        

        if (mIsRunning) {
            bindStepService();
        }
        
        mPedometerSettings.clearServiceRunning();

        mStepValueView     = (TextView) findViewById(R.id.step_value);
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
        mStepValueView     = (TextView) findViewById(R.id.step_value);
        if(mStepValueView != null){
            save.putString("steps",mStepValueView.getText().toString());
        }
            super.onSaveInstanceState(save);
        
     }
    @Override
    protected void onRestoreInstanceState(Bundle saved) {
        mStepValueView     = (TextView) findViewById(R.id.step_value);
        if(mStepValueView != null){
            mStepValueView.setText(saved.getString("steps","0"));
        }
        super.onRestoreInstanceState(saved);
    
      }
    
    private void savePaceSetting() {
        mPedometerSettings.savePaceOrSpeedSetting(mMaintain, mDesiredPaceOrSpeed);
    }

   

    private void startStepService() {
        if (! mIsRunning) {
            Log.i(TAG, "[SERVICE] Start");
            mIsRunning = true;
            startService(new Intent(Step_Counter_Activity.this,
                    StepService.class));
            startService(new Intent(Step_Counter_Activity.this, 
                    GpsService.class));
           
        }
    }
    
    private void bindStepService() {
        Log.i(TAG, "[SERVICE] Bind");
        bindService(new Intent(Step_Counter_Activity.this, 
                StepService.class), mConnection, Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
        bindService(new Intent(Step_Counter_Activity.this, 
                GpsService.class), gpsConnection, Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
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
            stopService(new Intent(Step_Counter_Activity.this,
                  StepService.class));
            stopService(new Intent(Step_Counter_Activity.this,
                    GpsService.class));
        }
        mIsRunning = false;
    }
    
    private StepService mService;
    private GpsService gpsService;
    
    private void resetValues(boolean updateDisplay) {
        if (mService != null) {
            mService.resetValues();                    
        }
        else {
            mStepValueView.setText("0");
            SharedPreferences state = getSharedPreferences("state", 0);
            SharedPreferences.Editor stateEditor = state.edit();
            if (updateDisplay) {
                stateEditor.putInt("steps", 0);
                stateEditor.commit();
            }
        }
        if (gpsService !=null){
            gpsService.reset();
            speedtext.setText(getResources().getString(R.string.initspeed));
            distancetext.setText(getResources().getString(R.string.initdist));
        }
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
            mHandler.sendMessage(mHandler.obtainMessage(SPEED_MSG, (int)value*1000000, 0));
        }
        @Override
        public void distanchanged(float value) {
            mHandler.sendMessage(mHandler.obtainMessage(DISTANCE_MSG, (int)value*1000000, 0));
            
        }
    };
   
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = ((StepService.StepBinder)service).getService();
            mService.registerCallback(mCallback);
            mService.reloadSettings();
            
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };
    
    
    
    private ServiceConnection gpsConnection = new ServiceConnection(){
        public void onServiceConnected(ComponentName className, IBinder service) {
            gpsService = ((GpsService.GpsBinder)service).getService();
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
                    mStepValue = (int)msg.arg1;
                    mStepValueView.setText( mStepValue+"   "+getResources().getString(R.string.stepunit));
                    break;
                case SPEED_MSG:
                     speedtext.setText(msg.arg1/1000000f+"   "+getResources().getString(R.string.speedunit));
                    break;
                case DISTANCE_MSG:
                     distancetext.setText(msg.arg1/1000000f+"   " + getResources().getString(R.string.distanceunit));
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
        
    };
    
    
    
   
    
}
