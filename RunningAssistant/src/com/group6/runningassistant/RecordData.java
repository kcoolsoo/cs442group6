package com.group6.runningassistant;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;

public class RecordData extends Activity implements OnClickListener {
    private Chronometer chronometer;
    long timeWhenStopped = 0;
    long t;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.data);
          chronometer = (Chronometer) findViewById(R.id.chronometer1);
          if(t!=0)
        	  display_time();
          else
             chronometer.setText("00:00:00");
          ((Button) findViewById(R.id.btnstart)).setOnClickListener(this);
          ((Button) findViewById(R.id.btnstop)).setOnClickListener(this);
    
     
   
	    chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener()
	    {
 	        @Override
 	        public void onChronometerTick(Chronometer chronometer) {
 	        	
 	                 t = SystemClock.elapsedRealtime() - chronometer.getBase();
 	                display_time();
 	        }
 	    });
    }
    
    @Override
    public void onClick(View v) {
           switch(v.getId()) {
           case R.id.btnstart:
                  chronometer.setBase(SystemClock.elapsedRealtime()+timeWhenStopped);
                  chronometer.start();
                  break;
          case R.id.btnstop:
         	 timeWhenStopped = chronometer.getBase() - SystemClock.elapsedRealtime();   
         	 chronometer.stop();
                 break;
          }
   }
    
    public void display_time()
    {
    	 int h   = (int)(t/3600000);
         int m = (int)(t - h*3600000)/60000;
         int s= (int)(t - h*3600000- m*60000)/1000 ;
         String hh = h < 10 ? "0"+h: h+"";
         String mm = m < 10 ? "0"+m: m+"";
         String ss = s < 10 ? "0"+s: s+"";
         chronometer.setText(hh+":"+mm+":"+ss);
    }
}