package com.group6.runningassistant;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class Statistics extends Activity {

	String [] menu = {"Distance Graph","Speed Graph","Calories Graph"};
	ListView lvStats;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.statistics);
		final Bundle bundle = getIntent().getExtras();
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
		        android.R.layout.simple_list_item_1, menu);
    lvStats = (ListView) findViewById(R.id.listView_stats);
	lvStats.setAdapter(adapter);
	lvStats.requestFocus();
	lvStats.setOnItemClickListener(new OnItemClickListener(){
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			Intent in = new Intent();
			switch(position)
			{
			case 0: in=new Intent(Statistics.this,DistanceGraph.class);
			break;
			case 1: in=new Intent(Statistics.this,SpeedGraph.class);
			break;
			case 2: in=new Intent(Statistics.this,CalorieGraph.class);
			break;
			
			}
			
			in.putExtras(bundle);
			startActivity(in);
			
		}
	});

	}
	

}
