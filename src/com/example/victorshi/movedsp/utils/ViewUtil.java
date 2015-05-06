package com.example.victorshi.movedsp.utils;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;

import com.example.victorshi.movedsp.model.DataModel;

/**
 * 将byte[]转换为绘制图形适用的float[]
 * @author smnan
 *
 */
public class ViewUtil {

	private static final String TAG = "ViewUtil";
	private Context mContext = null;
	private CustomApplication application = null;
	
	private DataModel dataModel = null;
	private ArrayList<byte[]> byteWave = null;
	private ArrayList<byte[]> byteFFt = null;
	
	private ArrayList<float[]> ResWave = null;
	private ArrayList<float[]> ResFFt = null;
	
	private int arrLength = -1;
	private int listSize = -1;
	private int width = 0;
	private int height = 0;
	private float[] mWave;
	private float[] mFFt;
	
	public ViewUtil(Context context, int width, int height) {
		this.mContext = context;
		application = (CustomApplication) ((Activity)mContext).getApplication();
		this.width = width;
		this.height = height;
		initData();
	}

	private void initData() {
		dataModel = application.getDataModel();
		byteWave = dataModel.getBufWave();
		byteFFt = dataModel.getBufFFt();
		listSize = dataModel.getListSize();
		arrLength = dataModel.getArrSize();
		
		ResWave = new ArrayList<float[]>();
		ResFFt = new ArrayList<float[]>();
		
		for(int k = 0; k < listSize; k++) {
			TransWave(k);
			TransFFt(k);
		}
		dataModel.setResWave(ResWave);
		dataModel.setResFFt(ResFFt);
		application.setDataModel(dataModel);
	}
	/**
	 * 绘制时域波形
	 * @param j 
	 */
	private void TransWave(int j) {
		byte[] mByteWave = byteWave.get(j);
		if(mWave == null || mWave.length < arrLength * 2) {
			mWave = new float[arrLength * 2];
		}
		int xOrdinate = arrLength - 1;
		for(int i = 0; i < xOrdinate; i++) {
//			mWave[i * 4] = (float) ((width * i * (j + 1) / xOrdinate + j * width) * 1.0);
			mWave[i * 2] = (float) ((height / 2 + ((byte)(mByteWave[i] + 128)) * height / 2 / 128) * 1.0);
//			mWave[i * 4 + 2] = (float) ((width * (i + 1) * (j + 1) / xOrdinate + j * width) * 1.0);
			mWave[i * 2 + 1] = (float) ((height / 2 + ((byte)(mByteWave[i + 1] + 128)) * height / 2 / 128) * 1.0);
		}
		ResWave.add(mWave);
	}
	/**
	 * 转换为频谱数据
	 * @param fft
	 * @return
	 */
	private byte[] updateFFt(byte[] fft) {
		byte[] model = new byte[fft.length / 2 + 1];  

        model[0] = (byte) Math.abs(fft[0]);
        for (int i = 2, j = 1; j < 48;)  
        {  
            model[j] = (byte) Math.hypot(fft[i], fft[i + 1]);  
            i += 2;  
            j++;  
        }
		return model;
	}
	/**
	 * 绘制频域波形
	 * @param j 
	 */
	private void TransFFt(int j) {
//		final int baseX = width / 48;
		byte[] mByteFFt = null;
		mByteFFt = updateFFt(byteFFt.get(j));
		if(mFFt == null || mFFt.length < arrLength * 2) {
			mFFt = new float[arrLength * 2];
		}
		for(int i = 0; i < 48; i++) {
			if(mByteFFt[i] < 0)
				mByteFFt[i] = 127;
			
//			final int xi = baseX * i * j + baseX / 2;
//			mFFt[i * 4] = xi;
			mFFt[i * 4] = (float) (height * 1.0);
//			mFFt[i * 4 + 2] = xi;
			mFFt[i * 4 + 1] = (float) ((height - mByteFFt[i]) * 1.0);
		}
		ResFFt.add(mFFt);
	}

	public ArrayList<float[]> getResWave() {
		return ResWave;
	}

	public ArrayList<float[]> getResFFt() {
		return ResFFt;
	}

}
