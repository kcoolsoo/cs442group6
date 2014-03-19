package com.group6.runningassistant;



import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void OnClick_Group1(View v) {
		
	}
	
	public void OnClick_Group2(View v) {
		Intent recordtime = new Intent (MainActivity.this,RecordData.class);
		startActivity(recordtime);
		
	}
	
	public void Stepcounter(View v){
	    Intent stepcounterservice = new Intent(MainActivity.this, Step_Counter_Activity.class);
	    startActivity(stepcounterservice);
	}

}
