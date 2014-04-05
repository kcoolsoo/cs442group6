package com.group6.runningassistant;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;  

import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;  
import com.google.android.gms.maps.GoogleMap;  
import com.google.android.gms.maps.MapFragment;  
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;  
import com.google.android.gms.maps.model.CameraPosition;  
import com.google.android.gms.maps.model.LatLng;  
import com.google.android.gms.maps.model.MarkerOptions;  
import com.google.android.gms.maps.model.PolylineOptions;

  
import android.R.color;
import android.R.integer;
import android.location.Criteria;  
import android.location.Location;  
import android.location.LocationListener;  
import android.location.LocationManager;  
import android.os.AsyncTask;
import android.os.Bundle;  
import android.app.Activity;  
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;  
import android.content.DialogInterface;
import android.text.StaticLayout;
import android.util.Log;
import android.view.View;  
import android.view.View.OnClickListener;  
import android.widget.Button;  
import android.widget.EditText;  
import android.widget.RadioGroup;  
import android.widget.RadioGroup.OnCheckedChangeListener;  
import android.widget.TextView;
import android.widget.Toast;  
import android.graphics.Color;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public  class MapActivity extends Activity implements LocationListener{
	private Button btn_loc, btn_nav;  
	  private EditText edt_lng, edt_lat;  
	  private RadioGroup rg_mapType;    
	  private CameraPosition cameraPosition;  
	  private MarkerOptions markerOpt;  	 
	  private LocationManager locManager;  
	  private Location location;  
	  private String bestProvider;
	  double  locallat;
	  double locallong;
	  GoogleMap mMap;
	  PolylineOptions polylineOptions;
	  static double latbuffer=0;
	  static double longbuffer=0;
	  static int i,j=0;
	  static float distance=0;
	  public static boolean showpath=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      
    	   super.onCreate(savedInstanceState);
           setContentView(R.layout.activity_map);
        //   btn_nav=(Button)findViewById(R.id.btnclear);
         // btn_nav=(Button)findViewById(R.id.)
           int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
           
           // Showing status
           if(status!=ConnectionResult.SUCCESS){ // Google Play Services are not available
    
               int requestCode = 10;
               Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
               dialog.show();
               }
            mMap=((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
           
           // speedtext=(TextView)findViewById(R.id.speedtext);
            //curlocation=(TextView)findViewById(R.id.curlocation);
            //prelocation=(TextView)findViewById(R.id.prelocation);
           
           // Getting GoogleMap object from the fragment
           

           // Enabling MyLocation Layer of Google Map
           mMap.setMyLocationEnabled(true);
        
           
           // Getting LocationManager object from System Service LOCATION_SERVICE
           LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

           // Creating a criteria object to retrieve provider
           Criteria criteria = new Criteria();

           // Getting the name of the best provider
           String provider = locationManager.getBestProvider(criteria, true);
          
           
           
           // Getting Current Location
           Location location = locationManager.getLastKnownLocation(provider);
           
           if(location!=null){
               onLocationChanged(location);
               
           }
           LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
           mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
           mMap.animateCamera(CameraUpdateFactory.zoomTo(15)); 
          // locationManager.requestLocationUpdates(provider, 1000, 0, this);
           
           if(getIntent().getBooleanExtra("Showmap", false)){
               showpath();
           }else{
               String latString=locallat+"";
               String longString=locallong+"";
               // TODO Auto-generated method stub
               //Toast.makeText(MainActivity.this,"Currentlocation:"+latString+","+longString ,Toast.LENGTH_SHORT ).show();
               StringBuilder stringBuilder=new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");  
               stringBuilder.append("location="+locallat+","+locallong);
               stringBuilder.append("&radius=1000");
               stringBuilder.append("&types=grocery_or_supermarket");
               //stringBuilder.append("&keyword=store or drink station ");
               stringBuilder.append("&sensor=true");
               stringBuilder.append("&key=AIzaSyBrktuxUHngHZ4W3cXMp5mb-QPhjiXvwlY");
               
               // Creating a new non-ui thread task to download json data
               PlacesTask placesTask = new PlacesTask();

               // Invokes the "doInBackground()" method of the class PlaceTask
               placesTask.execute(stringBuilder.toString());
              // mMap.animateCamera(CameraUpdateFactory.zoomTo(14)); 
           }
//           btn_nav.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				String latString=locallat+"";
//				String longString=locallong+"";
//				// TODO Auto-generated method stub
//				//Toast.makeText(MainActivity.this,"Currentlocation:"+latString+","+longString ,Toast.LENGTH_SHORT ).show();
//				StringBuilder stringBuilder=new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");  
//				stringBuilder.append("location="+locallat+","+locallong);
//				stringBuilder.append("&radius=1000");
//				stringBuilder.append("&types=grocery_or_supermarket");
//				//stringBuilder.append("&keyword=store or drink station ");
//				stringBuilder.append("&sensor=true");
//				stringBuilder.append("&key=AIzaSyBrktuxUHngHZ4W3cXMp5mb-QPhjiXvwlY");
//				
//                // Creating a new non-ui thread task to download json data
//                PlacesTask placesTask = new PlacesTask();
//
//                // Invokes the "doInBackground()" method of the class PlaceTask
//                placesTask.execute(stringBuilder.toString());
//               // mMap.animateCamera(CameraUpdateFactory.zoomTo(14)); 
//				
//			}
//		});
    }
    
           public void onLocationChanged(Location location) {
        	   i++;//for test
               // Getting latitude of the current location
        	   String ischangeString="";
               double latitude = location.getLatitude(); 
               // Getting longitude of the current location
               double longitude = location.getLongitude();
               LatLng curLatLng=new LatLng(latitude,longitude);
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
            	   //polylineOptions=new PolylineOptions();
            	   //polylineOptions.color(Color.RED).width(5).add(curLatLng).add(preLatLng);
            	 
            	   //mMap.addPolyline(polylineOptions);
            	   
               }
               else{
            	   ischangeString="notchanged";
            	   j++; // for test
               }
               
               float speed= location.getSpeed()*(float)2.236936;
              
               String provideString = location.getProvider();
               String speedString=""+speed;
               String latitudeString=""+latitude;
               String longtitudeString=""+longitude;
               //speedtext.
              // setText(provideString+speedString+"mph\nlat: "+latitudeString+"\nlong: "+longtitudeString+"\nprelat"+latbuffer+"\nprelong"+longbuffer+ischangeString+"\ni: "+i+j+"\ndistance:"+distance);
               
              // curlocation.setText("lat: "+latitudeString+"long: "+longtitudeString);
               locallat=latitude;
               locallong=longitude;
    }
           public void showpath(){
               try {
                   Bundle bundle = getIntent().getExtras();
                   
                   ArrayList<String> positionArrayList = bundle.getStringArrayList("key");
                   double[] part1 = new double[positionArrayList.size()];
                   double[] part2 = new double[positionArrayList.size()];
                   String[] parts=null;
                   if(positionArrayList!=null){
                       polylineOptions=new PolylineOptions();
                       parts=positionArrayList.get(0).split(",");
                       part1[0]=Double.parseDouble(parts[0]);
                       part2[0]=Double.parseDouble(parts[1]);
                       
                       for(int ind=1;ind<positionArrayList.size()-1;ind++)
                       {
                           Log.d("positionind", positionArrayList.get(ind)+positionArrayList.size()+ind);
                           if(positionArrayList.get(ind).contains(",")){
                            parts=positionArrayList.get(ind).split(",");
                           part1[ind]=Double.parseDouble(parts[0]);
                           part2[ind]=Double.parseDouble(parts[1]);
                           LatLng preLatLng=new LatLng(part1[ind], part2[ind]);
                           LatLng curLatLng=new LatLng(part1[ind-1], part2[ind-1]);
                           
                              polylineOptions.color(Color.BLUE).width(5).add(curLatLng).add(preLatLng);
                              mMap.addPolyline(polylineOptions);
                           }
                       }
                   }
                   else{Log.d("null", "null");}
               } catch (Exception e) {
                   e.printStackTrace();// TODO: handle exception
               }
         }

           public float distance (double lat_a, double lng_a, double lat_b, double lng_b ) 
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
              // Toast.makeText(this,  dString, Toast.LENGTH_SHORT).show();
               return new Float(distance * meterConversion).floatValue();
               
           }
           
           private String downloadUrl(String strUrl) throws IOException{
               String data = "";
               InputStream iStream = null;
               HttpURLConnection urlConnection = null;
               try{
                   URL url = new URL(strUrl);
        
                   // Creating an http connection to communicate with url
                   urlConnection = (HttpURLConnection) url.openConnection();
        
                   // Connecting to url
                   urlConnection.connect();
        
                   // Reading data from url
                   iStream = urlConnection.getInputStream();
        
                   BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
        
                   StringBuffer sb  = new StringBuffer();
        
                   String line = "";
                   while( ( line = br.readLine())  != null){
                       sb.append(line);
                   }
        
                   data = sb.toString();
        
                   br.close();
                   
        
               }catch(Exception e){
                   Log.d("Exception while downloading url", e.toString());
               }finally{
                   iStream.close();
                   urlConnection.disconnect();
               }
        
               return data;
           }
        
           /** A class, to download Google Places */
           private class PlacesTask extends AsyncTask<String, Integer, String>{
        
               String data = null;
        
               // Invoked by execute() method of this object
               @Override
               protected String doInBackground(String... url) {
                   try{
                       data = downloadUrl(url[0]);
                       Log.d("stringbuffer",data);
                   }catch(Exception e){
                       Log.d("Background Task",e.toString());
                   }
                   return data;
               }
        
               // Executed after the complete execution of doInBackground() method
               @Override
               protected void onPostExecute(String result){
                   ParserTask parserTask = new ParserTask();
        
                   // Start parsing the Google places in JSON format
                   // Invokes the "doInBackground()" method of the class ParseTask
                   parserTask.execute(result);
               }
        
           }
        
           /** A class to parse the Google Places in JSON format */
           private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{
        
               JSONObject jObject;
        
               // Invoked by execute() method of this object
               @Override
               protected List<HashMap<String,String>> doInBackground(String... jsonData) {
        
                   List<HashMap<String, String>> places = null;
                   PlaceJSONParser placeJsonParser = new PlaceJSONParser();
        
                   try{
                       jObject = new JSONObject(jsonData[0]);
        
                       /** Getting the parsed data as a List construct */
                       places = placeJsonParser.parse(jObject);
        
                   }catch(Exception e){
                       Log.d("Exception",e.toString());
                   }
                   return places;
               }
        
               // Executed after the complete execution of doInBackground() method
               @Override
               protected void onPostExecute(List<HashMap<String,String>> list){
        
                   // Clears all the existing markers
                   mMap.clear();
        
                   for(int i=0;i<list.size();i++){
        
                       // Creating a marker
                       MarkerOptions markerOptions = new MarkerOptions();
        
                       // Getting a place from the places list
                       HashMap<String, String> hmPlace = list.get(i);
        
                       // Getting latitude of the place
                       double lat = Double.parseDouble(hmPlace.get("lat"));
        
                       // Getting longitude of the place
                       double lng = Double.parseDouble(hmPlace.get("lng"));
        
                       // Getting name
                       String name = hmPlace.get("place_name");
        
                       // Getting vicinity
                       String vicinity = hmPlace.get("vicinity");
        
                       LatLng latLng = new LatLng(lat, lng);
        
                       // Setting the position for the marker
                       markerOptions.position(latLng);
        
                       // Setting the title for the marker.
                       //This will be displayed on taping the marker
                       markerOptions.title(name + " : " + vicinity);
        
                       // Placing a marker on the touched position
                       mMap.addMarker(markerOptions);
                   }
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
}
		