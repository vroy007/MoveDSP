package com.example.victorshi.movedsp.activitys.toDSP;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cyberway.frame.activity.BaseActivity;
import com.cyberway.frame.utils.DeviceUtil;
import com.example.victorshi.movedsp.R;
import com.example.victorshi.movedsp.model.DataModel;
import com.example.victorshi.movedsp.utils.CustomApplication;
import com.example.victorshi.movedsp.utils.ViewUtil;
import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;
/**
 * 低通滤波
 * @author smnan
 *
 */
public class LowPassActivity extends BaseActivity implements OnClickListener {

	private static final String TAG = "LowPassActivity";
	private Context mContext = null;
	private CustomApplication application = null;
	private DataModel dataModel = null;
	
	private ImageButton btnBack = null;
	private TextView tvTitle = null;
	private ImageButton btnRight = null;
	
	private LinearLayout llWave = null;
	private LinearLayout llFFt = null;
	
	private ArrayList<float[]> arrWave = null;
	private ArrayList<float[]> arrFFt = null;
	private int arrWaveSize = 0;
	private int arrFFtSize = 0;
	private int listSize = 0;
	private int width = 0;
	private int height = 0;
	
	private ViewUtil vUtil = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_low_pass);
		mContext = getApplicationContext();
		application = (CustomApplication) getApplication();
		
		initView();
		initData();
		initEvent();
	}

	private void initView() {
		btnBack = (ImageButton) this.findViewById(R.id.btnLeft);
		btnBack.setVisibility(View.VISIBLE);
		tvTitle = (TextView) this.findViewById(R.id.tvTopTitle);
		tvTitle.setText("低通滤波");
		btnRight = (ImageButton) this.findViewById(R.id.btnRight);
		llWave = (LinearLayout) this.findViewById(R.id.llWave);
		llFFt = (LinearLayout) this.findViewById(R.id.llFFt);
		width = DeviceUtil.getScreenPixels(this).widthPixels;
		height = DeviceUtil.getScreenPixels(this).heightPixels / 2;
	}

	private void initData() {
		dataModel = application.getDataModel();
		vUtil = new ViewUtil(this, width, height);
		
		arrWave = new ArrayList<float[]>();
		arrFFt = new ArrayList<float[]>();
		dataModel = application.getDataModel();
		arrWave = dataModel.getResWave();
		arrFFt = dataModel.getResFFt();
		
		listSize = dataModel.getListSize();
		if(arrWave != null || arrFFt != null) {
			arrWaveSize = arrWave.get(0).length;
			arrFFtSize = arrFFt.get(0).length;
		}
		
		GraphViewData[] dataWave = new GraphViewData[arrWaveSize * listSize];
		GraphViewData[] dataFFt = new GraphViewData[arrFFtSize * listSize];
		
		for(int i = 0; i < listSize; i++) {
			float[] floatWave = arrWave.get(i);
			for(int j = i * arrWaveSize; j < (i + 1) * arrWaveSize && j < arrWaveSize * listSize; j++) {
				if(j - i * arrWaveSize < arrWaveSize) {
					dataWave[j] = new GraphViewData(j * 0.01, floatWave[j - i * arrWaveSize]);
				}
			}
			float[] floatFFt = arrFFt.get(i);
			for(int j = i * arrFFtSize; j < (i + 1) * arrFFtSize && j < arrFFtSize * listSize; j++) {
				if(j - i * arrFFtSize < arrFFtSize) {
					dataFFt[j] = new GraphViewData(j * 0.01, floatFFt[j - i * arrFFtSize]);
				}
			}
		}
		
		GraphViewSeries WaveSeries = new GraphViewSeries("时域波形", new GraphViewSeriesStyle(Color.rgb(200, 50, 00), 1), dataWave);
		GraphViewSeries FFtSeries = new GraphViewSeries("频域波形", new GraphViewSeriesStyle(Color.rgb(90, 250, 00), 1), dataFFt);
		
		LineGraphView graphLine = new LineGraphView(this, "时域波形");
		graphLine.addSeries(WaveSeries);
		// set legend
//		graphLine.setShowLegend(true);
//		graphLine.setLegendAlign(LegendAlign.BOTTOM);
		// set view port, start=2, size=40
		graphLine.setViewPort(0, listSize * arrWaveSize * 0.01);
		graphLine.setScalable(true);
		llWave.addView(graphLine);
		
		BarGraphView graphBar = new BarGraphView(this, "频域波形");
		graphBar.addSeries(FFtSeries);
		// set legend
//		graphBar.setShowLegend(true);
//		graphBar.setLegendAlign(LegendAlign.BOTTOM);
		// set view port, start=2, size=40
		graphBar.setViewPort(0, listSize * arrWaveSize * 0.01);
		graphBar.setScalable(true);
		llFFt.addView(graphBar);
	}

	private void initEvent() {
		btnBack.setOnClickListener(this);
		btnRight.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v == btnBack)
			finish();
	}

}
