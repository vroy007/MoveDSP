package com.example.victorshi.movedsp.activitys;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cyberway.frame.activity.BaseActivity;
import com.cyberway.frame.utils.DeviceUtil;
import com.cyberway.frame.utils.StringUtil;
import com.example.victorshi.movedsp.R;
import com.example.victorshi.movedsp.activitys.toDSP.LowPassActivity;
import com.example.victorshi.movedsp.components.URecorder;
import com.example.victorshi.movedsp.model.DataModel;
import com.example.victorshi.movedsp.utils.CustomApplication;
import com.example.victorshi.movedsp.utils.ViewUtil;
import com.example.victorshi.movedsp.views.VisualizerFFTView;
import com.example.victorshi.movedsp.views.VisualizerView;
/**
 * 傅立叶变换
 * @author smnan
 *
 */
public class MainActivity extends BaseActivity implements OnClickListener{

	private int isAddVisualizer = 0;
	private boolean isAudio = false;
	private static final String AudioPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/moveDsp/record.amr";
	private static String Mp3Path = "/sdcard/moveDsp/";
	
    private static final float VISUALIZER_HEIGHT_DIP = 160f;
	
	private static final String TAG = "MainActivity";
	private Context mContext = null;
	private CustomApplication application = null;
	
	private ImageButton ibtnBack = null;
	private TextView tvTitle = null;
	private ImageButton ibtnAdd = null;
	
	private LinearLayout llFFT = null;
	private TextView tvTips = null;
	
	private LinearLayout llIFT = null;
	private Button btnRecord = null;
	private Button btnStop = null;
	private URecorder mRecorder = null;
	
	private Button btnFFT = null;
	private Button btnStopFFt = null;
	private Button btnPauseFFt = null;
	private Visualizer mVisualizer = null;
	private Equalizer mEqualizer = null;
	private VisualizerView mWaveView = null;
	private VisualizerFFTView mFFtView = null;
	
	private Button btnLow = null;
	private Button btnButter = null;
	private Button btnIIR = null;
	private Button btnFIR = null;
	
	private MediaPlayer mMedia = null;
	private ProgressBar progress = null;
	private TextView tvMediaNow = null;
	private TextView tvMediaAll = null;
	private static int duration = 0; // mp3 duration
	private Handler mHandler = null;
	
	private boolean isfinish = false;
	
	private ArrayList<byte[]> bufWave = null;
	private ArrayList<byte[]> bufFFt = null;
	private DataModel dataModel = null;
	private ViewUtil vUtil = null;
	static int width = 0;
	static int height = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = getApplicationContext();
		application = (CustomApplication) getApplication();
		
		width = DeviceUtil.getScreenPixels(this).widthPixels;
		height = DeviceUtil.getScreenPixels(this).heightPixels / 2;
		
		initView();
		initData();
		initEvent();
	}

	private void initView() {
		ibtnBack = (ImageButton) this.findViewById(R.id.btnLeft);
		ibtnBack.setVisibility(View.VISIBLE);
		tvTitle = (TextView) this.findViewById(R.id.tvTopTitle);
		tvTitle.setVisibility(View.VISIBLE);
		tvTitle.setText("傅立叶变换");
		ibtnAdd = (ImageButton) this.findViewById(R.id.btnRight);
		ibtnAdd.setVisibility(View.VISIBLE);
		
		progress = (ProgressBar) this.findViewById(R.id.progressBar);
		tvMediaNow = (TextView) this.findViewById(R.id.tvMediaNow);
		tvMediaAll = (TextView) this.findViewById(R.id.tvMediaWhole);
		
		llFFT = (LinearLayout) this.findViewById(R.id.ll_FFT);
		llIFT = (LinearLayout) this.findViewById(R.id.ll_IFT);
		llIFT.setVisibility(View.GONE);
		tvTips = (TextView) this.findViewById(R.id.tvTips);
		btnRecord = (Button) this.findViewById(R.id.btnRecord_begin);
		btnStop = (Button) this.findViewById(R.id.btnRecord_stop);
		
		btnFFT = (Button) this.findViewById(R.id.btnMusic_play);
		btnStopFFt = (Button) this.findViewById(R.id.btnMusic_stop);
		btnPauseFFt = (Button) this.findViewById(R.id.btnMusic_pause);
		btnFFT.setVisibility(View.GONE);
		btnStopFFt.setVisibility(View.GONE);
		btnPauseFFt.setVisibility(View.GONE);
		
		btnLow = (Button) this.findViewById(R.id.btn_LowPass);
		btnButter = (Button) this.findViewById(R.id.btn_ButterWorth);
		btnIIR = (Button) this.findViewById(R.id.btn_IIR);
		btnFIR = (Button) this.findViewById(R.id.btn_FIR);
	}
	
	private void initData() {
        setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
		isfinish = true;
		dataModel = new DataModel();
		bufWave = new ArrayList<byte[]>();
		bufFFt = new ArrayList<byte[]>();
		
		mHandler = new Handler();
	}
	/**
	 * 每隔0.1s刷新一次进度
	 */
	Runnable updateThread = new Runnable() {
		@Override
		public void run() {
			progress.incrementProgressBy(1);
			if(progress.getProgress()%60 < 10)
				tvMediaNow.setText(progress.getProgress()/60 + ":0" + progress.getProgress()%60);
			else
				tvMediaNow.setText(progress.getProgress()/60 + ":" + progress.getProgress()%60);
			mHandler.postDelayed(updateThread, 1000);
		}
	};
	
	private void initEvent() {
		ibtnBack.setOnClickListener(this);
		ibtnAdd.setOnClickListener(this);
		btnFFT.setOnClickListener(this);
		btnStopFFt.setOnClickListener(this);
		btnPauseFFt.setOnClickListener(this);
		btnLow.setOnClickListener(this);
		btnButter.setOnClickListener(this);
		btnIIR.setOnClickListener(this);
		btnFIR.setOnClickListener(this);
		btnRecord.setOnClickListener(this);
		btnStop.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.btnLeft:
				finish();
				break;
			case R.id.btnRight:
				if(hasSDcard()) {
					ResPopupView popView = new ResPopupView(mContext);
					popView.showAsDropDown(ibtnAdd);
				}
				break;
			case R.id.btnMusic_play: {
				//傅里叶变换（边播放边进行）
				tvTips.setVisibility(View.VISIBLE);
//				tvTips.setText("播放中……");
				btnStopFFt.setEnabled(true);
				btnPauseFFt.setEnabled(true);
				if(isfinish) {
					isfinish = false;
					Uri uri = null;
					if(isAudio)
						uri = Uri.parse(AudioPath);
					else
						uri = Uri.parse(Mp3Path);
					mMedia = MediaPlayer.create(this, uri);
					duration = mMedia.getDuration();
					progress.setMax(duration/1000);
					if(duration/1000%60 < 10)
						tvMediaAll.setText(duration/1000/60 + ":0" + duration/1000%60);
					else
						tvMediaAll.setText(duration/1000/60 + ":" + duration/1000%60);
				}
				setupVisualizerFxAndUI();
				mVisualizer.setEnabled(true);
		        mMedia.start();
		        mHandler.post(updateThread);
				break;
			}
			case R.id.btnMusic_stop: {
				//停止播放，停止傅里叶变换
				mHandler.removeCallbacks(updateThread);
				progress.setProgress(0);
				tvMediaNow.setText("0:00");
				isfinish = true;
//				tvTips.setText("停止播放……");
				btnPauseFFt.setEnabled(false);
				mMedia.stop();
				mMedia.release();
				mVisualizer.setEnabled(false);
				saveBufData();
				break;
			}
			case R.id.btnMusic_pause: {
				//暂停播放，暂停傅立叶变换
				mHandler.removeCallbacks(updateThread);
				isfinish = false;
//				tvTips.setText("暂停中……");
				mMedia.pause();
				mVisualizer.setEnabled(false);
				break;
			}
			case R.id.btn_LowPass:
				//低通滤波
				break;
			case R.id.btn_ButterWorth:
				//巴特沃斯滤波
				break;
			case R.id.btn_IIR:
				//IIR滤波
				break;
			case R.id.btn_FIR:
				//FIR滤波
				break;
			case R.id.btnRecord_begin: {
				//开始录音
				tvTips.setText("录音中……");
				mRecorder.start();
				Toast.makeText(MainActivity.this, "开始录音", Toast.LENGTH_SHORT).show();
				break;
			}
			case R.id.btnRecord_stop: {
				//结束录音
				tvTips.setVisibility(View.VISIBLE);
				mRecorder.stop();
				tvTips.setText("record.amr");
				Toast.makeText(MainActivity.this, "结束录音", Toast.LENGTH_SHORT).show();
				btnRecord.setVisibility(View.GONE);
				btnStop.setVisibility(View.GONE);
				progress.setVisibility(View.VISIBLE);
				btnFFT.setVisibility(View.VISIBLE);
				btnStopFFt.setVisibility(View.VISIBLE);
				btnPauseFFt.setVisibility(View.VISIBLE);
				btnFFT.setEnabled(true);
				btnPauseFFt.setEnabled(true);
				btnStopFFt.setEnabled(true);
				break;
			}
		}
	}
	/**
	 * 保存时域和频域波形数据
	 */
	private void saveBufData() {
		if(bufWave.size() > 0) {
			dataModel.setBufWave(bufWave);
		}
		if(bufFFt.size() > 0) {
			dataModel.setBufFFt(bufFFt);
		}
		application.setDataModel(dataModel);
//		bufWave.clear();
//		bufWave = new ArrayList<byte[]>();
//		bufFFt.clear();
//		bufFFt = new ArrayList<byte[]>();
	}

	private void intent (Class<?> cls,int type){
		Intent intent = new Intent(this, cls);
		startActivityForResult(intent, type);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(data != null) {
			switch (requestCode) {
				case 0:
					// MP3获取
					if(!StringUtil.isEmpty(data.getStringExtra("path"))) {
						Mp3Path = "/sdcard/moveDsp/" + data.getStringExtra("path");
						tvTips.setText(data.getStringExtra("path"));
					}
					break;
		
				default:
					break;
			}
		}
		else {
			tvTips.setText("请选择信号源！");
			btnFFT.setEnabled(false);
			btnPauseFFt.setEnabled(false);
			btnStopFFt.setEnabled(false);
		}
	}

	/**
	 * 初始化VisualizerView
	 */
	private void setupVisualizerFxAndUI() {
		if(isAddVisualizer++ == 0) {
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,  
	                (int) (VISUALIZER_HEIGHT_DIP * getResources()  
	                        .getDisplayMetrics().density));
			lp.bottomMargin = 5;
			LinearLayout.LayoutParams tvLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			tvLp.leftMargin = 5;
			tvLp.bottomMargin = 5;
			TextView tvWave = new TextView(this);
			tvWave.setText("时域波形");
			tvWave.setTextSize(18f);
			TextView tvFFt = new TextView(this);
			tvFFt.setText("频域波形");
			tvFFt.setTextSize(18f);
			mWaveView = new VisualizerView(this);
	        mWaveView.setBackgroundColor(Color.BLACK);
	        mFFtView = new VisualizerFFTView(this);
	        mFFtView.setBackgroundColor(Color.BLACK);
	        
	        llFFT.addView(tvWave, tvLp);
	        llFFT.addView(mWaveView, lp);
	        llFFT.addView(tvFFt, tvLp);
	        llFFT.addView(mFFtView, lp);
		}
		
		mMedia.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				mHandler.removeCallbacks(updateThread);
				progress.setProgress(0);
				tvMediaNow.setText("0:00");
				isfinish = true;
//				tvTips.setText("播放完毕……");
				mp.stop();
				mp.release();
				mVisualizer.setEnabled(false);
				btnStopFFt.setEnabled(false);
				saveBufData();
			}
		});
  
        final int maxCR = Visualizer.getMaxCaptureRate(); 
          
        mVisualizer = new Visualizer(mMedia.getAudioSessionId());  
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        mVisualizer.setDataCaptureListener(  
                new Visualizer.OnDataCaptureListener()  
                {  
                    public void onWaveFormDataCapture(Visualizer visualizer,  
                            byte[] waveform, int samplingRate)  
                    {  
                    	bufWave.add(waveform);
                        mWaveView.updateVisualizer(waveform);  
                    }  
  
                    public void onFftDataCapture(Visualizer visualizer,  
                            byte[] fft, int samplingRate)  
                    {  
                    	bufFFt.add(fft);
                    	mFFtView.updateVisualizer(fft);  
                    }  
                }, maxCR / 2, true, true);
	}
	/**
	 * 检测是否有sd卡
	 */
	public boolean hasSDcard() {
		if(DeviceUtil.isSdcardEnable())
			return true;
		else{
			Toast.makeText(MainActivity.this, "请插入sd卡", Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	/**
	 * 信号源选择popupView
	 */
	class ResPopupView extends PopupWindow implements OnClickListener {

		private TextView tvAudio = null;
		private TextView tvMP3 = null;
		private Context context = null;
		private View view = null;
		
		public ResPopupView(Context context) {
			super(context);
			this.context = context;
			LayoutInflater mInflater = LayoutInflater.from(context);
			view = mInflater.inflate(R.layout.popup_signal_select_item, null);
			tvAudio = (TextView) view.findViewById(R.id.tvAudio);
			tvMP3 = (TextView) view.findViewById(R.id.tvMp3);
			tvAudio.setOnClickListener(this);
			tvMP3.setOnClickListener(this);
			setContentView(view);
			setWidth(LayoutParams.WRAP_CONTENT);
			setHeight(LayoutParams.WRAP_CONTENT);
			setFocusable(true);
			ColorDrawable dw = new ColorDrawable(0xffffff);
			setBackgroundDrawable(dw);
			view.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					int height = view.findViewById(R.id.pop_signal_layout).getTop();
					int top = (int) event.getY();
					if(event.getAction() == MotionEvent.ACTION_UP)
						if(top < height)
							dismiss();
					return true;
				}
			});
		}

		@Override
		public void onClick(View v) {
			isfinish = true;
			dismiss();
			if(v == tvAudio) {
				// 从录音获取信号源
				tvTips.setTextSize(18f);
				tvTips.setText("请开始录音……");
				llIFT.setVisibility(View.VISIBLE);
				progress.setVisibility(View.GONE);
				btnRecord.setVisibility(View.VISIBLE);
				btnStop.setVisibility(View.VISIBLE);
				btnFFT.setVisibility(View.GONE);
				btnStopFFt.setVisibility(View.GONE);
				btnPauseFFt.setVisibility(View.GONE);
				isAudio = true; // 说明是由录音获取信号源
				mRecorder = new URecorder(mContext, AudioPath);
			}
			else if(v == tvMP3) {
				// 从MP3获取信号源
				llIFT.setVisibility(View.VISIBLE);
				btnRecord.setVisibility(View.GONE);
				btnStop.setVisibility(View.GONE);
				btnFFT.setVisibility(View.VISIBLE);
				btnStopFFt.setVisibility(View.VISIBLE);
				btnPauseFFt.setVisibility(View.VISIBLE);
				isAudio = false; // 说明是由本地音乐获取的信号源
				intent(MusicListActivity.class, 0);
			}
			if(mMedia != null)
				mMedia.release();
			if(mVisualizer != null)
				mVisualizer.release();
		}
		
	}
}
