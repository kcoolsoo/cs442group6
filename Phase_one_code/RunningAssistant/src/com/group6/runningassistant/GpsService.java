package com.group6.runningassistant;

import com.google.android.gms.maps.model.LatLng;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;


public class GpsService extends Service{

    
    double  locallat;
    double locallong;
    TextView speedtext;
    static double latbuffer=0;
    static double longbuffer=0;
    static int i,j=0;
    static float distance=0;
    private LocationListener locationlistener;
    private   LocationManager locationManager;
    private final String TAG = "GPS Service";
    private    Location location;
    private float speed;
    
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
                
               // curlocation.setText("lat: "+latitudeString+"long: "+longtitudeString);
                locallat=latitude;
                locallong=longitude;
                if (mCallback != null) {
                    Log.i("Callback","Not Null!!");
                    mCallback.speedchanged(speed);
                    mCallback.distanchanged(distance);
                    mCallback.positionchanged(latitude+","+longitude);
                }
               
            }
            
            @Override
            public void onProviderDisabled(String arg0) {
                // TODO Auto-generated method stub
                
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
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationlistener);
        locationManager = null;
    }
    
    
    public interface ICallback {
        public void speedchanged(float value);
        public void distanchanged(float value);
        public void positionchanged(String posi);
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
}
    
   
