package com.group6.runningassistant;

import com.google.android.gms.maps.model.LatLng;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;


public class GpsService extends Service{

    
    double  locallat;
    double locallong;
    float predist;
    TextView speedtext;
    private static double latbuffer=0;
    private static double longbuffer=0;
    private static int i,j=0;
    private static float distance=0;
    private LocationListener locationlistener;
    private   LocationManager locationManager;
    private final String TAG = "GPS Service";
    private float mBodyWeight;
    private    Location location;
    private float speed;
    private int PREF_MODE = 0;
    private static final String PREF_NAME_USERPROFILE = "UserProfile";
    private static final String KEY_WEIGHT = "weight";
    private static final String KEY_HEIGHT = "height";
    private static double METRIC_RUNNING_FACTOR = 1.02784823;
    private static double METRIC_WALKING_FACTOR = 0.708;
    private static double RUNNING_SPEED = 5;
    private float mCalories;
    private Chronometer chronometer;
    private Context context;
    long timeWhenStopped = 0;
    long t;
    
    public class GpsBinder extends Binder {
        GpsService getService() {
            return GpsService.this;
        }
    }
    
    private final IBinder mBinder = new GpsBinder();
    
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "[SERVICE] onBind");
        return mBinder;
    }
    
    @Override
    public void onCreate() {
        Log.i(TAG, "[SERVICE] onCreate");
        super.onCreate();
        mBodyWeight = -1.0f;
        mCalories = 0;
        context = getApplicationContext();
        SharedPreferences pref = context.getSharedPreferences(
                PREF_NAME_USERPROFILE, PREF_MODE);
        
        if (pref.getFloat(KEY_WEIGHT, mBodyWeight) > 0f) {
            mBodyWeight = pref.getFloat(KEY_WEIGHT, mBodyWeight);
        }
        if (mBodyWeight < 0f) {
            startActivity(new Intent(GpsService.this,
                    UserProfile.class));
        }
  //     Log.i("Weight",mBodyWeight+"");
        
        //location service
       locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
       
        
     // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Getting the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);
       
        // Getting Current Location
        location = locationManager.getLastKnownLocation(provider);
        
       
     
       
        
        
        locationlistener =  new LocationListener() {
            public void onLocationChanged(Location location) {
                i++;//for test
                // Getting latitude of the current location
                Log.d("Location","changed");
                String ischangeString="";
                double latitude = location.getLatitude(); 
                // Getting longitude of the current location
                double longitude = location.getLongitude();
             
                if(latbuffer==0&&longbuffer==0){
                    latbuffer=latitude;
                    longbuffer=longitude;
                }
                 if((latitude!=latbuffer)||(longitude!=longbuffer)){
                    ischangeString="changed";
                    LatLng preLatLng=new LatLng(latbuffer, longbuffer);
                    predist = distance;
                    distance=distance+distance(latitude, longitude, latbuffer, longbuffer);
                    latbuffer=latitude;
                    longbuffer=longitude;
                   
                    
                }
                else{
                    ischangeString="notchanged";
                    j++; // for test
                }
                
                speed= location.getSpeed()*(float)2.236936;
                
                String provideString = location.getProvider();
                String speedString=""+speed;
                String latitudeString=""+latitude;
                String longtitudeString=""+longitude;
                
                mCalories += (mBodyWeight * ((speed > RUNNING_SPEED) ? METRIC_RUNNING_FACTOR
                        : METRIC_WALKING_FACTOR))
                        // Distance:
                        * (distance - predist) // centimeters
                        / 1000.0; // centimeters/kilometer
                
               // curlocation.setText("lat: "+latitudeString+"long: "+longtitudeString);
                locallat=latitude;
                locallong=longitude;
                if (mCallback != null) {
                   // Log.i("Callback","Not Null!!");
                    mCallback.speedchanged(speed);
                    mCallback.distanchanged(distance);
                    mCallback.positionchanged(latitude+","+longitude);
                    mCallback.caloriechanged(mCalories);
                    
                }
               
            }
            
            @Override
            public void onProviderDisabled(String arg0) {
                // TODO Auto-generated method stub
                Toast toast = Toast.makeText(context, "GPS disabled", Toast.LENGTH_LONG);
                toast.show();
                if (mCallback != null){
                    mCallback.gpsstoped(1);
                }
                
            } 
            @Override
            public void onProviderEnabled(String arg0) {
                // TODO Auto-generated method stub
                
            }
            @Override
            public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
                // TODO Auto-generated method stub      
            }
        
        };
        locationManager.requestLocationUpdates(provider, 1000, 0, locationlistener);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        
        
        super.onStartCommand(intent, flags, startId);
        timeWhenStopped = (long)intent.getIntExtra("stoptime", 0);
        Log.i("StopTimeSerivce",timeWhenStopped+"");
        distance = intent.getFloatExtra("distance", 0);
        Log.i("DistanceSerivce",distance+"");
        mCalories = intent.getFloatExtra("calories", 0);
        Log.i("CalSerivce",mCalories+"");
//        chronometer =new Chronometer( getApplicationContext());
//        chronometer.setText(getResources().getString(R.string.initclock));
//        chronometer.setBase(SystemClock.elapsedRealtime()
//                + timeWhenStopped);
//        chronometer
//        .setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
//            @Override
//            public void onChronometerTick(Chronometer chronometer) {
//
//                t = SystemClock.elapsedRealtime()
//                        - chronometer.getBase();
//                Log.i("TimeINSerivce",t+"");
//            }
//        });
//        chronometer.start();
       
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }
    
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationlistener);
        locationManager = null;
    }
    
    
    public interface ICallback {
        public void speedchanged(float value);
        public void distanchanged(float value);
        public void positionchanged(String posi);
        public void caloriechanged(float value);
        public void gpsstoped(int value);
    }
    private ICallback mCallback;

    public void registerCallback(ICallback cb) {
        mCallback = cb;
    }
    
   
    
    public void reset(){
        distance = 0;
        speed= 0;
    }
  
    
    
    
  //calculate distance
    private float distance (double lat_a, double lng_a, double lat_b, double lng_b ) 
    {
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b-lat_a);
        double lngDiff = Math.toRadians(lng_b-lng_a);
        double a = Math.sin(latDiff /2) * Math.sin(latDiff /2) +
        Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
        Math.sin(lngDiff /2) * Math.sin(lngDiff /2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;
        String dString=distance+"";
        int meterConversion = 1609;
      //  Toast.makeText(this,  dString, Toast.LENGTH_SHORT).show();
        return new Float(distance * meterConversion).floatValue();
        
    }
    
    
    public void resetvalue(){
        t =0;
        mCalories= 0;
        distance = 0;
        speed = 0;
        
    }
    public void setTime(int stoptime){
        t = stoptime;
    }
}
    
   
