package com.group6.runningassistant;


import java.sql.Time;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnInitListener  {

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
    private SharedPreferences pref;
    private int PREF_MODE = 0;
    private static final String PREF_NAME_USERPROFILE = "UserProfile";
    private static final String KEY_AGE = "age";
    private static final String KEY_WEIGHT = "weight";
    private static final String KEY_HEIGHT = "height";
    private static ArrayList<String> position = new ArrayList<String>();
    private static ArrayList<String> speedtime = new ArrayList<String>();
    private static ArrayList<String> distancetime = new ArrayList<String>();
    private static ArrayList<String> caltime = new ArrayList<String>();
    
    private TextToSpeech myTTS;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
     
        
        Log.i(TAG, "[ACTIVITY] onCreate");
        currentspeed = 0;
        mStepValue = 0;
        mCalories = 0;
        mDistance = 0;
        mUtils = Utils.getInstance();
        mBodyWeight = -1.0f;
        
        
     // Text to Speech
        Intent checkTTSIntent = new Intent();
	    checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
	    startActivityForResult(checkTTSIntent, 0);
	//end of TTS 
       
           
        Context context = getApplicationContext();
        pref = context.getSharedPreferences(
                PREF_NAME_USERPROFILE, PREF_MODE);
        if (pref.getFloat(KEY_WEIGHT, mBodyWeight) > 0f) {
            mBodyWeight = pref.getFloat(KEY_WEIGHT, mBodyWeight);
             Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // run your code here
                    welcome();
                }
            }, 500);
        }
        else{
        	startActivity(new Intent(MainActivity.this,
                    UserProfile.class));
        }

        setContentView(R.layout.activity_main);
       
        
        speedtext = (TextView) findViewById(R.id.speedtext);
        distancetext = (TextView) findViewById(R.id.distance);
        calorietext = (TextView) findViewById(R.id.calorie);
        avespeedtext = (TextView) findViewById(R.id.avespeedtext);
        Button start = (Button) findViewById(R.id.start);
        Button pause = (Button) findViewById(R.id.pause);
        Button reset = (Button) findViewById(R.id.reset);
        Button quit = (Button) findViewById(R.id.quit);
        Button save1 =(Button) findViewById(R.id.save);
        Button open = (Button) findViewById(R.id.open);
     
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
                            
                           //   Log.i("time ",t+"");
                            display_time();
                            if(t != 0){
                                avespeedtext.setText(dfonedc.format(((float)mDistance *1000f / (float)t))
                                        + "   "
                                        + getResources().getString(
                                                R.string.avespeedunit));
                            }
                            if ((int)(t/1000) % 60 == 0){
                                speedtime.add(currentspeed+"");
                                Log.i("Distance",mDistance+"");
                                distancetime.add(mDistance+"");
                                caltime.add(mCalories+"");
                            }
                        }
                    });
        }


        start.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mBodyWeight = -1.0f;
                if (pref.getFloat(KEY_WEIGHT, mBodyWeight) > 0f) {
                    mBodyWeight = pref.getFloat(KEY_WEIGHT, mBodyWeight);
                }
                if (mBodyWeight < 0f) {
                    startActivity(new Intent(MainActivity.this,
                            UserProfile.class));
                    //finish();
                } else if(checkGPS()){
                    if (!mIsRunning) {
                        startStepService();
                        bindStepService();
                        chronometer.setBase(SystemClock.elapsedRealtime()
                                + timeWhenStopped);
                        chronometer.start();
                    }
                    myTTS.speak("There's nothing to think about. Just run", TextToSpeech.QUEUE_FLUSH, null);
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
                
                myTTS.speak("you ran for"+mDistance+"miles. Good Bye.", TextToSpeech.QUEUE_FLUSH, null);
               // myTTS.speak(" ", TextToSpeech.QUEUE_FLUSH, null);
                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // run your code here
                    	finish();
                    }
                }, 2000);

                

            }
        });
        open.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	Intent i = new Intent(MainActivity.this,Mysqlview.class);
				startActivity(i);
            	//resetValues(true);
            }
        });
        save1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!mIsRunning){
                	Toast.makeText(MainActivity.this,"Saving",Toast.LENGTH_SHORT).show();
        			try{
        			//String price = tot.getText().toString();
        			String mydate = java.text.DateFormat.getDateInstance().format(Calendar.getInstance().getTime());
        			//String name1=name.replaceAll("null\n\t","");
        			Storage entry = new Storage(MainActivity.this);
        			entry.write();
        			entry.createEntry( mydate,""+mDistance,calorietext.getText().toString(),chronometer.getText().toString() );
        			entry.close();
        			Toast.makeText(MainActivity.this,"Record Saved to Database",Toast.LENGTH_SHORT).show();
        			}catch (Exception e){
        				e.printStackTrace();
        				Toast.makeText(MainActivity.this,"error in saving",Toast.LENGTH_SHORT).show();
        			}
                	resetValues(true);
                }else{
                    Toast.makeText(getApplicationContext(),  "Please stop running first", Toast.LENGTH_SHORT).show();
                }
            }
        });
      
     
       
   
	    
	   
    }
 // tts
    private void welcome() {
		// TODO Auto-generated method stub
//	  int hours = new Time(System.currentTimeMillis()).getHours();
//     	//Toast.makeText(MainActivity.this, ""+hours, Toast.LENGTH_LONG).show();
//       if(hours<12)
//       {
//       	myTTS.speak("Good Morning.", TextToSpeech.QUEUE_FLUSH, null);
//       }
//       else
//       {
//       	myTTS.speak("Good Evening.", TextToSpeech.QUEUE_FLUSH, null);
//       }
       //myTTS.speak("Welcome to the running assistant. Enjoy.", TextToSpeech.QUEUE_FLUSH, null);
	}
    
	//start TTS
    protected void onActivityResult(int requestCode, int resultCode, Intent myIntent) {
    	
    			if(requestCode==0)
    			{
    				if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
    	                //the user has the necessary data - create the TTS
    	            myTTS = new TextToSpeech(this, this);
    	            }
    	            else {
    	                    //no data - install it now
    	                Intent installTTSIntent = new Intent();
    	                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
    	                startActivity(installTTSIntent);
    	            }
    				
    			}
    			
    }//stop TTS 
    public void onInit(int initStatus)
	{
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "[ACTIVITY] onStart");
        super.onStart();
        //chronometer.start();//may be wrong here
      
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
        if (mIsRunning) {

            stopStepService();
        }
        mQuitting = true;
        resetValues(true);
        mIsRunning = false;
        mPedometerSettings.saveServiceRunningWithNullTimestamp(mIsRunning);
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        Log.i(TAG, "[ACTIVITY] onRestart");
        super.onRestart();
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
            startService(new Intent(MainActivity.this, StepService.class));
            Intent gpsintent = new Intent(MainActivity.this, GpsService.class);
            gpsintent.putExtra("stoptime", timeWhenStopped);
            gpsintent.putExtra("calorees", mCalories);
            gpsintent.putExtra("distance", mDistance);
            startService(gpsintent);

        }
    }

    private void bindStepService() {
        Log.i(TAG, "[SERVICE] Bind");
        bindService(new Intent(MainActivity.this, StepService.class),
                mConnection, Context.BIND_AUTO_CREATE
                        + Context.BIND_DEBUG_UNBIND);
        Intent gpsintent = new Intent(MainActivity.this, GpsService.class);
        gpsintent.putExtra("stoptime", timeWhenStopped);
        gpsintent.putExtra("calorees", mCalories);
        gpsintent.putExtra("distance", mDistance);
        bindService(gpsintent,
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
            stopService(new Intent(MainActivity.this, StepService.class));
            stopService(new Intent(MainActivity.this, GpsService.class));
        }
        mIsRunning = false;
    }

    private StepService mService;
    private GpsService gpsService;

    private void resetValues(boolean updateDisplay) {
        if (mService != null) {
            mService.resetValues();
        } else {
            mStepValueView.setText("0 "+getResources().getString(R.string.tell_steps_setting));
            SharedPreferences state = getSharedPreferences("state", 0);
            SharedPreferences.Editor stateEditor = state.edit();
            stateEditor.putInt("steps", 0);
            stateEditor.commit();

        }
        if (gpsService != null) {
            gpsService.resetvalue();
        }
        speedtext.setText(getResources().getString(R.string.initspeed));
        mDistance = 0;
        currentspeed = 0;
        mCalories = 0;
        distancetext.setText(getResources().getString(R.string.initdist));
        calorietext.setText(getResources().getString(R.string.initcalorie));
        timeWhenStopped = 0;
        t=0;
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.setText(getResources().getString(R.string.initclock));
        avespeedtext.setText(getResources().getString(R.string.avespeed));
        position.clear();
    }


    private static final int STEPS_MSG = 1;
    private static final int SPEED_MSG = 2;
    private static final int DISTANCE_MSG = 3;
    private static final int POSITION_MSG = 4;
    private static final int CALORIE_MSG = 5;
    private static final int GPS_STOP = 6;

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
                    (int) (value * 1000000), 0));
        }

        @Override
        public void distanchanged(float value) {
            mHandler.sendMessage(mHandler.obtainMessage(DISTANCE_MSG,
                    (int) (value * 1000000), 0));

        }
        @Override 
        public void positionchanged(String posi){
            mHandler.sendMessage(mHandler.obtainMessage(POSITION_MSG,posi));
        }
        @Override 
        public void caloriechanged(float value){
            mHandler.sendMessage(mHandler.obtainMessage(CALORIE_MSG,
                    (int) (value * 1000000),0));
        }
        public void gpsstoped(int value){
            mHandler.sendMessage(mHandler.obtainMessage(GPS_STOP, value, 0));
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
                currentspeed = (float)msg.arg1 / 1000000f;
                speedtext.setText(dfonedc.format(currentspeed) + "   "
                        + getResources().getString(R.string.speedunit));
                break;
            case DISTANCE_MSG:
                float previousdis = mDistance;
                mDistance = (float)msg.arg1 / 1000000f;
//                mCalories += (mBodyWeight * ((currentspeed > RUNNING_SPEED) ? METRIC_RUNNING_FACTOR
//                        : METRIC_WALKING_FACTOR))
//                        // Distance:
//                        * (mDistance - previousdis) // centimeters
//                        / 1000.0; // centimeters/kilometer
                distancetext.setText(dfonedc.format(mDistance) + "   "
                        + getResources().getString(R.string.distanceunit));
                          
                break;
            case POSITION_MSG:
                if (t % 5 == 0 && t != 0){
                    position.add((String)msg.obj);
                }
                break;
            case CALORIE_MSG:
                mCalories = msg.arg1/1000000f;
                calorietext.setText(df.format(mCalories) + "   "
                        + getResources().getString(R.string.calorieunit));
                break;
            case GPS_STOP:
              
                if(msg.arg1 ==1){
                   if(checkGPS()){
                       if (mIsRunning) {
                           unbindStepService();
                           stopStepService();
                           speedtext.setText(R.string.initspeed);
                           timeWhenStopped = chronometer.getBase()
                                   - SystemClock.elapsedRealtime();
                           chronometer.stop();
                       }
                   }
                }
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
    public static ArrayList<String> getArrayList(){
        
        return position;
    }
    
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
        case R.id.action_settings:
            startActivity(new Intent(MainActivity.this,UserProfile.class));
            return true; 
        case R.id.showpath:
            if(checkNetwork()){
                if(!position.isEmpty()){
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList("key", position);
                    Intent passIntent=new Intent(MainActivity.this,MapActivity.class);
                    passIntent.putExtras(bundle);
                    passIntent.putExtra("Showmap", true);
                    startActivity(passIntent);
                }else{
                    Toast.makeText(getApplicationContext(),  "Please Move First", Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        case R.id.findstore:
            if(checkNetwork()){
                Intent passIntent=new Intent(MainActivity.this,MapActivity.class);
                startActivity(passIntent);
            }
            return true;
            
        case R.id.show_report:
        	
        	
        	Bundle bundle = new Bundle();
            bundle.putStringArrayList("speed_key", speedtime);
            bundle.putStringArrayList("distance_key", distancetime);
            bundle.putStringArrayList("cal_key", caltime);
        	Intent passIntent1=new Intent(MainActivity.this,Statistics.class);
            passIntent1.putExtras(bundle);
            if(speedtime.isEmpty()||caltime.isEmpty()||distancetime.isEmpty())
            	 	Toast.makeText(MainActivity.this,"No Data To Display",Toast.LENGTH_LONG).show();
                else
                	startActivity(passIntent1);
            return true;
        
        
        case R.id.feedback:
        	Intent i = new Intent(Intent.ACTION_SEND);
    		i.setType("message/rfc822");
    		i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"nickhil.revu@gmail.com"});
    		i.putExtra(Intent.EXTRA_SUBJECT, "Running Assist Feedback");
    		i.putExtra(Intent.EXTRA_TEXT   , "body of email");
    		try {
    		    startActivity(Intent.createChooser(i, "Send mail..."));
    		    
    		} catch (android.content.ActivityNotFoundException ex) {
    		    Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
    		}
        	
        }
        return false;
    }
    
    private boolean checkGPS(){
        final Context context =this;
        LocationManager lm = null;
        boolean gps_enabled =false;
           if(lm==null)
               lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
           try{
               gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
           }catch(Exception ex){
               Log.i("Check avaliable",ex.toString());
           }
          

          if(!gps_enabled){
               AlertDialog.Builder dialog = new AlertDialog.Builder(context);
               dialog.setMessage(context.getResources().getString(R.string.gps_not_enabled));
               dialog.setPositiveButton(context.getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {

                   @Override
                   public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                       // TODO Auto-generated method stub
                       Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                       context.startActivity(myIntent);
                       //get gps
                   }
               });
               dialog.setNegativeButton(context.getString(R.string.Cancel), new DialogInterface.OnClickListener() {

                   @Override
                   public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                       // TODO Auto-generated method stub

                   }
               });
               dialog.show();

           }
          return gps_enabled;
    }
    
    
    private boolean checkNetwork(){
        final Context context =this;
        boolean network_enabled =false;
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getNetworkInfo(0);
        if (netInfo != null && netInfo.getState()==NetworkInfo.State.CONNECTED) {
            network_enabled= true;
        }else {
            netInfo = cm.getNetworkInfo(1);
            if(netInfo!=null && netInfo.getState()==NetworkInfo.State.CONNECTED)
                network_enabled= true;
        }
        if(!network_enabled){
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setMessage(context.getResources().getString(R.string.network_not_enabled));
            dialog.setPositiveButton(context.getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent( Settings.ACTION_WIFI_SETTINGS);
                    context.startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton(context.getString(R.string.Cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub

                }
            });
            dialog.show();
        }
        return network_enabled;
    }
}




