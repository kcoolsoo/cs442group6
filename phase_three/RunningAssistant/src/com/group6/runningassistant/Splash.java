package com.group6.runningassistant;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class Splash extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//setContentView(new MYGIFView(this, null));
          InputStream stream =null;
          try{
        	  stream=getAssets().open("run.gif");
          }catch(IOException e){
        	  e.printStackTrace();
          }
          MYGIFView v = new MYGIFView(this,stream);
          setContentView(v);
		new Handler().postDelayed(new Runnable() {
			 
            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent( Splash.this,MainActivity.class);
                startActivity(i);
            	Toast.makeText(Splash.this,"done",Toast.LENGTH_LONG).show();
                // close this activity
                finish();
            }
        }, 3000);
		
	}

}
