package com.example.victorshi.movedsp.activitys;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cyberway.frame.activity.BaseActivity;
import com.example.victorshi.movedsp.R;
import com.example.victorshi.movedsp.activitys.toDSP.BandStopActivity;
/**
 * 进入首页
 * @author smnan
 *
 */
public class EnterActivity extends Activity implements OnClickListener{

	private static final String TAG = "EnterActivity";
	
	private Context mContext = null;
	private static long exitTime = 0;
	private ImageButton ibtnBack = null;
	private TextView tvTitle = null;
	private ImageButton ibtnAdd = null;
	private Button btnBandStop = null;
	private Button btnFFt = null;
	private Button btnSoundTouch = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_enter);
		
		mContext = getApplicationContext();
		initView();
		initEvent();
	}

	private void initView() {
		ibtnBack = (ImageButton) this.findViewById(R.id.btnLeft);
		ibtnBack.setVisibility(View.GONE);
		tvTitle = (TextView) this.findViewById(R.id.tvTopTitle);
		tvTitle.setVisibility(View.VISIBLE);
		tvTitle.setText("MoveDSP");
		ibtnAdd = (ImageButton) this.findViewById(R.id.btnRight);
		ibtnAdd.setVisibility(View.GONE);
		
		btnBandStop = (Button) this.findViewById(R.id.btnBandStop);
		btnFFt = (Button) this.findViewById(R.id.btnFFt);
		btnSoundTouch = (Button) this.findViewById(R.id.btnSoundTouch);
	}

	private void initEvent() {
		btnBandStop.setOnClickListener(this);
		btnFFt.setOnClickListener(this);
		btnSoundTouch.setOnClickListener(this);
	}

	private void intent(Class<?> cls) {
		Intent intent = new Intent(this, cls);
		startActivity(intent);
	}
	
	@Override
	public void onClick(View v) {
		if(v == btnBandStop) {
			intent(BandStopActivity.class);
		}else if(v == btnFFt) {
			intent(MainActivity.class);
		}else if(v == btnSoundTouch) {
			
		}
	}
	
	/**
	 * 退出程序
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			if(System.currentTimeMillis() - exitTime > 2000) {
				Toast.makeText(EnterActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();
			}
			else {
				finish();
				System.exit(0);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
