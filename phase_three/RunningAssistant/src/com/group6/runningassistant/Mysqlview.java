package com.group6.runningassistant;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Mysqlview extends Activity {
	 TextView tv;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mysqlview);
         
        
        Button cl = (Button)findViewById(R.id.DBclearbtn);
        cl.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Storage entry = new Storage(Mysqlview.this);
				entry.write();
    			entry.Clear();
    			entry.close();
    			 tv.setText("");
			}
        
        });	
        
        tv = (TextView) findViewById(R.id.tvsqlinfo);
        tv.setMovementMethod(new ScrollingMovementMethod());
        Storage info = new Storage(this);
        info.write();
        String data = info.getData();
        info.close();
        tv.setText(data);
      
        //Toast.makeText(Mysqlview.this, data,Toast.LENGTH_LONG).show();
         
	}
}
