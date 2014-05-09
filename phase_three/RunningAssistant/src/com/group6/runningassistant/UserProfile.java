package com.group6.runningassistant;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;
import android.content.SharedPreferences;

public class UserProfile extends Activity {
	private int flag=0;
	private EditText mEditAge, mEditWeight, mEditHeight, mEditBmi;
	private Button mBtnConfirm, mBtnCancel, mBtnClear;
	private RadioButton mHeigh,mLow;
	private Button mBtnBMI;
	private int mAge;
	private float mWeight, mHeight, mBmi;
	SharedPreferences mSettings;
	private int PREF_MODE = 0;
	private static final String PREF_NAME = "UserProfile";
	private static final String KEY_AGE = "age";
	private static final String KEY_WEIGHT = "weight";
	private static final String KEY_HEIGHT = "height";
	private String sens = "33.75";
    private  SharedPreferences.Editor edit;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_profile);
		
		mAge = -1;
		mWeight = -1f;
		mHeight = -1f;
		
        final Animation animRotate = AnimationUtils.loadAnimation(this, R.anim.anim_rotate);
        mEditAge = (EditText) findViewById(R.id.value_age);
		mEditWeight = (EditText) findViewById(R.id.value_weight);
		mEditHeight = (EditText) findViewById(R.id.value_height);
		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
		Context context = getApplicationContext();
		SharedPreferences pref = context.getSharedPreferences(PREF_NAME,
				PREF_MODE);
		if (pref.getInt(KEY_AGE, mAge) >= 0
				&& pref.getFloat(KEY_WEIGHT, mWeight) >= 0
				&& pref.getFloat(KEY_HEIGHT, mHeight) >= 0) {
			mAge = pref.getInt(KEY_AGE, mAge);
			mWeight = pref.getFloat(KEY_WEIGHT, mWeight);
			mHeight = pref.getFloat(KEY_HEIGHT, mHeight);
			
			mEditAge.setText(Integer.toString(mAge));
			mEditWeight.setText(Float.toString(mWeight));
			mEditHeight.setText(Float.toString(mHeight));
		}

		mBtnClear = (Button) findViewById(R.id.btn_clear);
		mBtnCancel = (Button) findViewById(R.id.btn_cancel);
		mBtnConfirm = (Button) findViewById(R.id.btn_confirm);
		mBtnBMI = (Button) findViewById(R.id.btn_bmi);
		mHeigh = (RadioButton) findViewById(R.id.heigh);
		
		mLow   = (RadioButton) findViewById(R.id.low);
		if(mSettings.getString("sensitivity", "33.75").equals("33.75")){
		    mHeigh.setChecked(true);
		}else{
		    mLow.setChecked(true);
		}
		mBtnClear.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				v.startAnimation(animRotate);
				clearUserProfile();
			}
		});
		
		mBtnCancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				finish();
			}
		});
		
		mBtnConfirm.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				try{
					mAge = Integer.parseInt(mEditAge.getText().toString());
				
				mWeight = Float.parseFloat(mEditWeight.getText().toString());
				mHeight = Float.parseFloat(mEditHeight.getText().toString());
				if(mAge>0 && mWeight>0.0 && mHeight>0.0)
				{
					saveUserProfile(mAge, mWeight, mHeight);
					Toast.makeText(UserProfile.this, "Saved Settings", Toast.LENGTH_LONG).show();
					flag=1;
				    finish();
				}else
				
					{
						Toast.makeText(UserProfile.this, "Please!! Fill Appropriate Date", Toast.LENGTH_LONG).show();
					}
				
				
				}catch(NumberFormatException e)
				{
					Toast.makeText(UserProfile.this, "Please!! Fill Appropriate Date", Toast.LENGTH_LONG).show();
				}
				
			}
		});

		mBtnBMI.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
			    try{
                    mAge = Integer.parseInt(mEditAge.getText().toString());
                
                mWeight = Float.parseFloat(mEditWeight.getText().toString());
                mHeight = Float.parseFloat(mEditHeight.getText().toString());
                if(mAge>0 && mWeight>0.0 && mHeight>0.0)
                {
                    saveUserProfile(mAge, mWeight, mHeight);
                    flag=1;              
                }else
                
                    {
                        Toast.makeText(UserProfile.this, "Please!! Fill Appropriate Date", Toast.LENGTH_LONG).show();
                    }
                
                
                }catch(NumberFormatException e)
                {
                    Toast.makeText(UserProfile.this, "Please!! Fill Appropriate Date", Toast.LENGTH_LONG).show();
                }
			    
			    if (!mEditWeight.getText().toString().trim().equals("") || !mEditHeight.getText().toString().trim().equals("")){
    				mWeight = Float.parseFloat(mEditWeight.getText().toString());
    				mHeight = Float.parseFloat(mEditHeight.getText().toString());
    				mBmi = (mWeight * 703) / (mHeight * mHeight);
    				setContentView(R.layout.bmi_chart);
    				mEditBmi = (EditText) findViewById(R.id.value_bmi);
    				mEditBmi.setText(" " + mBmi);
    				Toast.makeText(UserProfile.this, " " + mBmi, Toast.LENGTH_LONG)
    						.show();
			    }else{
			        Toast.makeText(UserProfile.this, "Please enther some information before " , Toast.LENGTH_SHORT)
                    .show();
			    }
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		Context context = getApplicationContext();
		SharedPreferences pref = context.getSharedPreferences(PREF_NAME,
				PREF_MODE);
		
		
		if (pref.getInt(KEY_AGE, mAge) >= 0
				&& pref.getFloat(KEY_WEIGHT, mWeight) >= 0.0
				&& pref.getFloat(KEY_HEIGHT, mHeight) >= 0.0) {
			mAge = pref.getInt(KEY_AGE, mAge);
			mWeight = pref.getFloat(KEY_WEIGHT, mWeight);
			mHeight = pref.getFloat(KEY_HEIGHT, mHeight);
			
			mEditAge.setText(Integer.toString(mAge));
			mEditWeight.setText(Float.toString(mWeight));
			mEditHeight.setText(Float.toString(mHeight));
		}
		
		
		
	}

	protected void saveUserProfile(int age, float weight, float height) {
		Context context = getApplicationContext();
		SharedPreferences pref = context.getSharedPreferences(PREF_NAME,
				PREF_MODE);

		Editor editor = pref.edit();
		editor.putInt(KEY_AGE, age);
		editor.putFloat(KEY_WEIGHT, weight);
		editor.putFloat(KEY_HEIGHT, height);
		editor.commit();
		edit= mSettings.edit();
        edit.putString("sensitivity", sens);
        edit.commit();
	}
	
	protected void clearUserProfile() {
		Context context = getApplicationContext();
		SharedPreferences pref = context.getSharedPreferences(PREF_NAME,
				PREF_MODE);

		Editor editor = pref.edit();;
		
		editor.remove(KEY_AGE);
		editor.remove(KEY_HEIGHT);
		editor.remove(KEY_WEIGHT);
		editor.clear();
		editor.commit();
		
		mEditAge.setText(null);
		mEditHeight.setText(null);
		mEditWeight.setText(null);
		
		Toast.makeText(getApplicationContext(), "User profile cleared!", Toast.LENGTH_LONG).show();
	}
	
	public void onRadioButtonClicked(View view) {
	    // Is the button now checked?
	    boolean checked = ((RadioButton) view).isChecked();
	    
	    // Check which radio button was clicked
	    switch(view.getId()) {
	        case R.id.heigh:
	            if (checked)
	               sens ="33.75";
	              
	            break;
	        case R.id.low:
	            if (checked)
	               sens ="50.62";
	               
	            break;
	    }
	}

}