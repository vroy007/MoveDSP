package com.example.victorshi.movedsp.activitys;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.cyberway.frame.activity.BaseActivity;
import com.example.victorshi.movedsp.R;

public class FlashScreenActivity extends Activity {

	private Context context;
	
	private Handler handler = new Handler();
	private Runnable task = new Runnable(){
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Intent intent = new Intent(context, EnterActivity.class);
			startActivity(intent);
			finish();
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//设置全屏  
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_flash_screen);
		context = getApplicationContext();
		handler.postDelayed(task, 1000);
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
}