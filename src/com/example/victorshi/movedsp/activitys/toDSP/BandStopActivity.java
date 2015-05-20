package com.example.victorshi.movedsp.activitys.toDSP;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cyberway.frame.components.DialogUtil;
import com.cyberway.frame.utils.LogUtil;
import com.example.victorshi.movedsp.R;
import com.example.victorshi.movedsp.utils.CustomApplication;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;
/**
 * 50Hz陷波器	 IIR  直接Ⅱ型
 * 参数：
 * 1）阻带上下边界频率：49.9Hz、50.1Hz
 * 2）通带上下边界频率：49Hz、51Hz
 * 3）通带最大衰减：1dB
 * 4）阻带最小衰减：60dB（巴特沃斯）、80dB（切比雪夫）
 * 5）采样率：2000Hz
 * 6）三种类型选择：巴特沃斯、切比雪夫Ⅰ、切比雪夫Ⅱ
 * @author smnan
 *
 */
public class BandStopActivity extends Activity implements OnClickListener{

	private static final String TAG = "BandStopActivity";
	private static final String ResultPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/moveDsp/result.txt";
	private static final int BUTTERWORTH = 0;
	private static final int CHEBYSHEV_1 = 1;
	private static final int CHEBYSHEV_2 = 2;
	//巴特沃斯滤波器参数
	static final double[] BUTTER_B = {
		0.9899359622368,   -7.532029056607,    25.45032400023,   -49.84830016096,
	      61.88022925884,   -49.84830016096,    25.45032400023,   -7.532029056607,
	     0.9899359622368
	};
	static final double[] BUTTER_A = {
		1,   -7.589362101251,    25.57922249866,   -49.97416107749,
	      61.87976151085,   -49.72205392803,    25.32179196493,   -7.475081328367,
	     0.9799732093296
	};
	//切比雪夫Ⅰ型滤波器参数
	static final double[] CHEBY1_B = {
		0.9868997860635,   -5.631695999653,    13.67303316702,   -18.05554931975,
	      13.67303316702,   -5.631695999653,   0.9868997860635
	};
	static final double[] CHEBY1_A = {
		1,   -5.681404918417,    13.73315467712,   -18.05529531047,
	      13.61277811896,   -5.582241090174,   0.9739331100742
	};
	//切比雪夫Ⅱ型滤波器参数
	static final double[] CHEBY2_B = {
		0.9914190616532,   -5.657483687379,    13.73564037492,   -18.13822267769,
	      13.73564037492,   -5.657483687379,   0.9914190616532
	};
	static final double[] CHEBY2_A = {
		1,   -5.690057823487,    13.77506120949,   -18.13808261759,
	      13.69614590785,   -5.625049611379,   0.9829117558093
	};
	
	private Context mContext = null;
	private CustomApplication application = null;
	
	private ImageButton ibtnBack = null;
	private TextView tvTitle = null;
	private ImageButton ibtnAdd = null;
	
	private LinearLayout llup = null;
	private LinearLayout llSource = null;
	private Spinner mSpinner = null;
	private Button btnFilter = null;
	private LinearLayout llResult = null;
	private Button btnSave = null;
	
	private static int FilterWay = -1;
	private double[] arrRes = new double[270000]; //原信号数组
	private List<double[]> ResData = new ArrayList<double[]>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_band_stop);
		
		mContext = getApplicationContext();
		application = (CustomApplication) getApplication();
		
		initView();
		initData();
		initEvent();
	}

	private void initView() {
		ibtnBack = (ImageButton) this.findViewById(R.id.btnLeft);
		ibtnBack.setVisibility(View.VISIBLE);
		tvTitle = (TextView) this.findViewById(R.id.tvTopTitle);
		tvTitle.setVisibility(View.VISIBLE);
		tvTitle.setText("50Hz陷波器");
		ibtnAdd = (ImageButton) this.findViewById(R.id.btnRight);
		ibtnAdd.setVisibility(View.VISIBLE);
		llup = (LinearLayout) this.findViewById(R.id.llBandStop_up);
		llSource = (LinearLayout) this.findViewById(R.id.llBandStop_Res);
		mSpinner = (Spinner) this.findViewById(R.id.spinner_bandstop);
		btnFilter = (Button) this.findViewById(R.id.btnFilter);
		llResult = (LinearLayout) this.findViewById(R.id.llBandStop_result);
		btnSave = (Button) this.findViewById(R.id.btn_save);
	}

	private void initData() {
		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			Toast.makeText(BandStopActivity.this, "请检查sd卡！", Toast.LENGTH_SHORT).show();
		}
		String[] filterWay = {"巴特沃斯","切比雪夫Ⅰ型","切比雪夫Ⅱ型"};
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, 
				android.R.layout.simple_spinner_item, filterWay);
		adapter.setDropDownViewResource(R.layout.spinner_drop_down_item);
		mSpinner.setAdapter(adapter);
	}

	private void initEvent() {
		ibtnBack.setOnClickListener(this);
		ibtnAdd.setOnClickListener(this);
		btnFilter.setOnClickListener(this);
		btnSave.setOnClickListener(this);
		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				switch(position) {
					case 0:
						FilterWay = BUTTERWORTH;
						break;
					case 1:
						FilterWay = CHEBYSHEV_1;
						break;
					case 2:
						FilterWay = CHEBYSHEV_2;
						break;
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				Toast.makeText(BandStopActivity.this, "请选择滤波器类型！", Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void onClick(View v) {
		if(v == ibtnBack)
			finish();
		else if(v == ibtnAdd) {
			// 添加信号源数据
			RightPop pop = new RightPop(mContext);
			pop.showAsDropDown(ibtnAdd);
		}
		else if(v == btnFilter) {
			// 进行50hz滤波
			if(FilterWay == BUTTERWORTH) {
				//选用巴特沃斯滤波器
				new execFilterTask().execute(BUTTERWORTH);
			}
			else if(FilterWay == CHEBYSHEV_1) {
				//选用切比雪夫Ⅰ型滤波器
				new execFilterTask().execute(CHEBYSHEV_1);
			}
			else if(FilterWay == CHEBYSHEV_2) {
				//选用切比雪夫Ⅱ型滤波器
				new execFilterTask().execute(CHEBYSHEV_2);
			}
		}
		else if(v == btnSave) {
			//保存滤波后的数据
			new execSaveTask().execute("");
		}
	}
	
	/**
	 * 导入信号源popupwindow
	 */
	class RightPop extends PopupWindow implements OnClickListener{

		private Context context = null;
		private TextView tvRes = null;
		private View view = null;
		
		public RightPop(Context context) {
			super(context);
			this.context = context;
			LayoutInflater mInflater = LayoutInflater.from(context);
			view = mInflater.inflate(R.layout.popup_filter_input_item, null);
			tvRes = (TextView) view.findViewById(R.id.tvFilterRes);
			tvRes.setOnClickListener(this);
			setContentView(view);
			setWidth(LayoutParams.WRAP_CONTENT);
			setHeight(LayoutParams.WRAP_CONTENT);
			setFocusable(true);
			ColorDrawable dw = new ColorDrawable(0xffffff);
			setBackgroundDrawable(dw);
			view.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					int height = view.findViewById(R.id.pop_filter_layout).getTop();
					int top = (int) event.getY();
					if(event.getAction() == MotionEvent.ACTION_UP) {
						if(top < height)
							dismiss();
					}
					return true;
				}
			});
		}

		@Override
		public void onClick(View v) {
			dismiss();
			if(v == tvRes) {
				// 启动异步处理机制，添加信号源数据，并转化为float[]形式，画图
				if(llup.getVisibility() == View.GONE)
					new execImportTask().execute("");
				else if(llup.getVisibility() == View.VISIBLE)
					Toast.makeText(BandStopActivity.this, "已成功导入信号源！", Toast.LENGTH_SHORT).show();
			}
		}
		
	}
	/**
	 * 导入resource.txt文件，写入输入流
	 */
	private void importResource() throws UnsupportedEncodingException {
		InputStream inStream = getResources().openRawResource(R.raw.resource);
		InputStreamReader inReader = null;
		inReader = new InputStreamReader(inStream);
		BufferedReader bufReader = new BufferedReader(inReader);
		String data;
		int num = 0;
		try {
			while((data = bufReader.readLine()) != null) {
				Float resData = Float.parseFloat(data);
				arrRes[num++] = resData;
			}
			ResData.add(arrRes); //将信号源数组导入集合
		} catch (IOException e) {
			LogUtil.e(TAG, e.toString());
			e.printStackTrace();
		}
	}
	/**
	 * 在导入原信号文件后，将波形画出
	 */
	private LineGraphView insertSourceView() {
		GraphViewData[] graphData = new GraphViewData[ResData.get(0).length];
		for(int i = 0; i < ResData.get(0).length; i++) {
			graphData[i] = new GraphViewData(i * 0.1, arrRes[i]);
		}
		GraphViewSeries sourceSeries = new GraphViewSeries("原信号波形", new GraphViewSeriesStyle(Color.rgb(200, 50, 00), 1), graphData);
		LineGraphView lineView = new LineGraphView(this, "原信号波形");
		lineView.addSeries(sourceSeries);
		lineView.setViewPort(0, 200);  //一次性只加载十分之一的波形
		lineView.setScrollable(true);
		return lineView;
	}
	/**
	 * 异步加载输入流数据
	 */
	private class execImportTask extends AsyncTask<String, Void, String> {

		private ProgressDialog dialog = null;
		private LineGraphView lineView = null;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = DialogUtil.showDialog(BandStopActivity.this, "正在加载数据","请稍候……");
		}
		
		@Override
		protected String doInBackground(String... params) {
			try {
				importResource();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			lineView = insertSourceView();
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			llup.setVisibility(View.VISIBLE);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT);
			llSource.addView(lineView, lp);
			DialogUtil.dismiss(dialog);
		}
	}
	/**
	 * 画出滤波后的波形信号图
	 * @param result
	 * @return
	 */
	private LineGraphView displayResult(double[] result) {
		
		GraphViewData[] resData = new GraphViewData[ResData.get(0).length];
		for(int i = 0; i < ResData.get(0).length; i++) {
			resData[i] = new GraphViewData(i * 0.1, arrRes[i]);
		}
		GraphViewSeries sourceSeries = new GraphViewSeries("原信号波形", new GraphViewSeriesStyle(Color.rgb(200, 50, 00), 1), resData);
		
		GraphViewData[] graphData = new GraphViewData[result.length];
		for(int i = 0; i < result.length; i++) {
			graphData[i] = new GraphViewData(i * 0.1, result[i]);
		}
		GraphViewSeries resultSeries = new GraphViewSeries("50Hz陷波后波形", new GraphViewSeriesStyle(Color.rgb(90, 250, 00), 2), graphData);
		LineGraphView resView = new LineGraphView(this, "50Hz陷波后波形");
		resView.addSeries(sourceSeries);
		resView.addSeries(resultSeries);
		resView.setShowLegend(true);
		resView.setLegendAlign(LegendAlign.TOP);
		resView.setLegendWidth(300);
		resView.setViewPort(0, 200);
		resView.setScrollable(true);
		return resView;
	}
	/**
	 * 异步进行50Hz带阻滤波
	 */
	private class execFilterTask extends AsyncTask<Integer, Void, String> {

		private ProgressDialog dialog = null;
		private LineGraphView resView = null;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = DialogUtil.showDialog(BandStopActivity.this, "正在进行滤波处理", "请稍候……");
		}

		@Override
		protected String doInBackground(Integer... params) {
			double[] result = null;
			result = DealFilter(params); //滤波函数
			resView = displayResult(result);
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			llResult.setVisibility(View.VISIBLE);
			//移除之前所有子控件，才能重新写入一个新的LineGraphView
			llResult.removeAllViews();
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT);
			llResult.addView(resView, lp);
			DialogUtil.dismiss(dialog);
			btnSave.setVisibility(View.VISIBLE);
		}
	}
	/**
	 * 根据选定滤波器性质进行50Hz陷波操作
	 * @param params
	 */
	public double[] DealFilter(Integer[] params) {
		int type = params[0]; //获取滤波选择的类型
		double[] dealResult = null;
		double x0, y0 = 0;
		double[] w0 = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f}; // 缓冲巴特沃斯滤波器
		double[] w1 = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f}; // 缓冲切比雪夫Ⅰ滤波器
		double[] w2 = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f}; // 缓冲切比雪夫Ⅱ滤波器
		
		dealResult = new double[arrRes.length];
		/**
		 * 巴特沃斯滤波器算法
		 */
		if(type == BUTTERWORTH) {
			for(int i = 0; i < arrRes.length; i++) {
				x0 = arrRes[i]; // 输入信号
				
				w0[0] = BUTTER_A[0] * x0 - BUTTER_A[1] * w0[1] - BUTTER_A[2] * w0[2] - BUTTER_A[3] * w0[3] - BUTTER_A[4] * w0[4]
						- BUTTER_A[5] * w0[5] - BUTTER_A[6] * w0[6] - BUTTER_A[7] * w0[7] - BUTTER_A[8] * w0[8];
				y0 = BUTTER_B[0] * w0[0] + BUTTER_B[1] * w0[1] + BUTTER_B[2] * w0[2] + BUTTER_B[3] * w0[3] + BUTTER_B[4] * w0[4]
						+ BUTTER_B[5] * w0[5] + BUTTER_B[6] * w0[6] + BUTTER_B[7] * w0[7] + BUTTER_B[8] * w0[8];
				
				dealResult[i] = y0;
				
				w0[8] = w0[7];
				w0[7] = w0[6];
				w0[6] = w0[5];
				w0[5] = w0[4];
				w0[4] = w0[3];
				w0[3] = w0[2];
				w0[2] = w0[1];
				w0[1] = w0[0];
			}
		}
		/**
		 * 切比雪夫Ⅰ型滤波器算法
		 */
		else if(type == CHEBYSHEV_1) {
			for(int i = 0; i < arrRes.length; i++) {
				x0 = arrRes[i]; // 输入信号
				
				w1[0] = CHEBY1_A[0] * x0 - CHEBY1_A[1] * w1[1] - CHEBY1_A[2] * w1[2] - CHEBY1_A[3] * w1[3] - CHEBY1_A[4] * w1[4]
						- CHEBY1_A[5] * w1[5] - CHEBY1_A[6] * w1[6];
				y0 = CHEBY1_B[0] * w1[0] + CHEBY1_B[1] * w1[1] + CHEBY1_B[2] * w1[2] + CHEBY1_B[3] * w1[3] + CHEBY1_B[4] * w1[4]
						+ CHEBY1_B[5] * w1[5] + CHEBY1_B[6] * w1[6];
				
				dealResult[i] = y0;
				
				w1[6] = w1[5];
				w1[5] = w1[4];
				w1[4] = w1[3];
				w1[3] = w1[2];
				w1[2] = w1[1];
				w1[1] = w1[0];
			}
		}
		/**
		 * 切比雪夫Ⅱ型滤波器算法
		 */
		else if(type == CHEBYSHEV_2) {
			for(int i = 0; i < arrRes.length; i++) {
				x0 = arrRes[i]; // 输入信号
				
				w2[0] = CHEBY2_A[0] * x0 - CHEBY2_A[1] * w2[1] - CHEBY2_A[2] * w2[2] - CHEBY2_A[3] * w2[3] - CHEBY2_A[4] * w2[4]
						- CHEBY2_A[5] * w2[5] - CHEBY2_A[6] * w2[6];
				y0 = CHEBY2_B[0] * w2[0] + CHEBY2_B[1] * w2[1] + CHEBY2_B[2] * w2[2] + CHEBY2_B[3] * w2[3] + CHEBY2_B[4] * w2[4]
						+ CHEBY2_B[5] * w2[5] + CHEBY2_B[6] * w2[6];
				
				dealResult[i] = y0;
				
				w2[6] = w2[5];
				w2[5] = w2[4];
				w2[4] = w2[3];
				w2[3] = w2[2];
				w2[2] = w2[1];
				w2[1] = w2[0];
			}
		}
		ResData.add(dealResult);
		return dealResult;
	}
	/**
	 * 异步保存滤波后的数据
	 */
	private class execSaveTask extends AsyncTask<String, Void, String> {

		private File file = null;
		private ProgressDialog dialog = null;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = DialogUtil.showDialog(BandStopActivity.this, "正在保存数据", "请稍候……");
			file = new File(ResultPath);
			if(!file.exists())
				try {
					file.createNewFile();
				} catch (IOException e) {
					LogUtil.e(TAG, e.toString());
					e.printStackTrace();
				}
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				double[] result = ResData.get(1);
				for(int i = 0; i < result.length; i++) {
					writer.write(String.valueOf(result[i]));
					writer.write("\r\n");   //自动换行
				}
				writer.flush();
				writer.close();
			} catch (IOException e) {
				LogUtil.e(TAG, e.toString());
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			DialogUtil.dismiss(dialog);
			Toast.makeText(BandStopActivity.this, "保存完毕", Toast.LENGTH_SHORT).show();
		}
	}
}
