package com.group6.runningassistant;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class Mysqlview extends Activity {

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mysqlview);
         
        
        
        TextView tv = (TextView) findViewById(R.id.tvsqlinfo);
        tv.setMovementMethod(new ScrollingMovementMethod());
        Storage info = new Storage(this);
        info.write();
        String data = info.getData();
        info.close();
        tv.setText(data);
      
        //Toast.makeText(Mysqlview.this, data,Toast.LENGTH_LONG).show();
         
	}
}
