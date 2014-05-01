package com.group6.runningassistant;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.WebView;
import android.widget.Toast;

public class Splash extends Activity{
	WebView wb;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//Toast.makeText(Splash.this,"Loading....",Toast.LENGTH_LONG).show();
		//setContentView(new MYGIFView(this));
		setContentView(R.layout.splash);
		wb=(WebView)findViewById(R.id.webView1);
		wb.loadUrl("file:///android_asset/run.gif");
		new Handler().postDelayed(new Runnable() {
			 
            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
            	Intent i = new Intent( Splash.this,MainActivity.class); 
        		startActivity(i);

                
                // close this activity
                finish();
            }
        }, 3000);
		
		
	}

}
