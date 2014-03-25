package com.group6.runningassistant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class UserProfile extends Activity{
	
	EditText weight,height,bmi1;
	Button confirm;
	ImageButton calcBMI;
	double wt,ht,bmi;

		
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.user_profile);
		weight=(EditText)findViewById(R.id.value_weight);
		height=(EditText)findViewById(R.id.value_height);
		
		confirm=(Button)findViewById(R.id.btnconfirm);
		calcBMI=(ImageButton)findViewById(R.id.imageButton_BMI);
		
		

	
	calcBMI.setOnClickListener(new OnClickListener(){
		public void onClick(View v){
			
			wt=Double.parseDouble(weight.getText().toString());
			ht=Double.parseDouble(height.getText().toString());
			bmi=(wt*703)/(ht*ht);
			setContentView(R.layout.bmi_chart);
			bmi1=(EditText)findViewById(R.id.value_bmi);
			bmi1.setText(" "+bmi);
			Toast.makeText(UserProfile.this," "+bmi,Toast.LENGTH_LONG).show();
			
		}
	});
	}
	
	

}