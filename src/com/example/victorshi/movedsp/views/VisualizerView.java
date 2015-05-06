package com.example.victorshi.movedsp.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
/**
 * 显示时域波形的自定义view
 * @author smnan
 *
 */
public class VisualizerView extends View {

	private byte[] mBytes;  
    private float[] mPoints;  
    private Rect mRect = new Rect();  

    private Paint mForePaint = new Paint();  
	
	public VisualizerView(Context context) {
		super(context);
		init();
	}
	/**
	 * 初始化
	 */
	private void init() {
		mBytes = null;  
        mForePaint.setStrokeWidth(1f);  
        mForePaint.setAntiAlias(true);  
        mForePaint.setColor(Color.GREEN);
	}
	
	public void updateVisualizer(byte[] waveForm)  
    {  
        mBytes = waveForm;  
        invalidate();  
    }
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if (mBytes == null)  
        {  
            return;  
        }  

        if (mPoints == null || mPoints.length < mBytes.length * 4)  
        {  
            mPoints = new float[mBytes.length * 4];  
        }  

        mRect.set(0, 0, getWidth(), getHeight());  

        //绘制波形  
		 for (int i = 0; i < mBytes.length - 1; i++) {  
			 mPoints[i * 4] = mRect.width() * i / (mBytes.length - 1);  
			 mPoints[i * 4 + 1] = mRect.height() / 2  
			 + ((byte) (mBytes[i] + 128)) * (mRect.height() / 2) / 128;  
			 mPoints[i * 4 + 2] = mRect.width() * (i + 1) / (mBytes.length - 1);  
			 mPoints[i * 4 + 3] = mRect.height() / 2  
			 + ((byte) (mBytes[i + 1] + 128)) * (mRect.height() / 2) / 128;  
		 }  
		 
        canvas.drawLines(mPoints, mForePaint);
	}
	
}
