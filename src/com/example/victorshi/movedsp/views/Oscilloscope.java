package com.example.victorshi.movedsp.views;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import com.cyberway.frame.utils.LogUtil;
import com.example.victorshi.movedsp.utils.CustomApplication;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioRecord;
import android.view.SurfaceView;

/**
 * 显示信号源时域波形
 * 示波器surfaceView
 * @author smnan
 *
 */
public class Oscilloscope {

	private static final String TAG = "Oscilloscope";
	private CustomApplication application = null;
	private Context context = null;
	
	private ArrayList<byte[]> inBuf = new ArrayList<byte[]>();  
	private ArrayList<byte[]> saveBuf = new ArrayList<byte[]>();
    private boolean isRecording = false;// 线程控制标记  
  //AudioName裸音频数据文件
  	private static final String AudioName = "/sdcard/record.txt";
  	//NewAudioName可播放的音频文件
  	private static final String NewAudioName = "/sdcard/display.wav";
    /** 
     * X轴缩小的比例 
     */  
    public int rateX = 4;  
    /** 
     * Y轴缩小的比例 
     */  
    public int rateY = 4;  
    /** 
     * Y轴基线 
     */  
    public int baseLine = 0;  
    /**
     * 构造函数
     * @param application 
     */
    public Oscilloscope(Context mContext, CustomApplication application) {
    	context = mContext;
    	this.application = application;
    }
	/** 
     * 初始化 
     */  
    public void initOscilloscope(int rateX, int rateY, int baseLine) {  
        this.rateX = rateX;  
        this.rateY = rateY;  
        this.baseLine = baseLine;
    }
    /** 
     * 开始 
     *  
     * @param recBufSize 
     *            AudioRecord的MinBufferSize 
     */  
    public void Start(AudioRecord audioRecord, int recBufSize, SurfaceView sfv,  
            Paint mPaint) {  
        isRecording = true;  
        new RecordThread(audioRecord, recBufSize).start();// 开始录制线程  
        new DrawThread(sfv, mPaint).start();// 开始绘制线程  
//        copyWaveFile(AudioName, NewAudioName, recBufSize);
    }  
	/** 
     * 停止 
     */  
    public void Stop() {  
        isRecording = false;  
        inBuf.clear();// 清除  
    }
    /** 
     * 负责从MIC保存数据到inBuf 
     *  
     * @author GV 
     *  
     */  
    class RecordThread extends Thread {  
        private int recBufSize;  
        private AudioRecord audioRecord;  
        private File file;
        String str = "";
        public RecordThread(AudioRecord audioRecord, int recBufSize) {  
            this.audioRecord = audioRecord;  
            this.recBufSize = recBufSize;  
            file = new File(AudioName);
            if(file.exists())
            	file.delete();
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				LogUtil.e(TAG, e.toString());
			}
        }  
        public void run() {  
            try {  
            	FileWriter out = new FileWriter(file);
            	byte[] buffer = new byte[recBufSize];
            	saveBuf.clear();
                audioRecord.startRecording();// 开始录制 
                while (isRecording) {
                    // 从MIC保存数据到缓冲区  
                    int bufferReadResult = audioRecord.read(buffer, 0,  
                            recBufSize); 
                    byte[] tmpBuf = new byte[bufferReadResult / rateX];  
                    byte[] allBuf = new byte[bufferReadResult];
                    for (int i = 0, ii = 0; i < tmpBuf.length; i++, ii = i  
                            * rateX) {  
                        tmpBuf[i] = buffer[ii];  
                    }
                    for(int j = 0; j < bufferReadResult; j++) {
                    	allBuf[j] = buffer[j];
                    	str = str + buffer[j] + "\n\t";
                    }
                    synchronized (inBuf) {//  
                        inBuf.add(tmpBuf);// 添加数据  
                    }  
                    synchronized (saveBuf) {
                    	saveBuf.add(allBuf); // 缓存数据
					}
               }
	            audioRecord.stop();
//	            application.setRecWave(saveBuf);
	            out.write(str); // 写出至.txt文件
	            out.close();
            } catch (Throwable t) {  
            }  
        }  
    };  
	/** 
     * 负责绘制inBuf中的数据 
     *  
     * @author GV 
     *  
     */  
    class DrawThread extends Thread {  
        private int oldX = 0;// 上次绘制的X坐标  
        private int oldY = 0;// 上次绘制的Y坐标  
        private SurfaceView sfv;// 画板  
        private int X_index = 0;// 当前画图所在屏幕X轴的坐标  
        private Paint mPaint;// 画笔  
        public DrawThread(SurfaceView sfv, Paint mPaint) {  
            this.sfv = sfv;  
            this.mPaint = mPaint;  
        }  
        public void run() {  
            while (isRecording) {  
                ArrayList<byte[]> buf = new ArrayList<byte[]>();  
                synchronized (inBuf) {  
                    if (inBuf.size() == 0)  
                        continue;  
                    buf = (ArrayList<byte[]>) inBuf.clone();// 保存  
                    inBuf.clear();// 清除  
                }  
                for (int i = 0; i < buf.size(); i++) {  
                	byte[] tmpBuf = buf.get(i);  
                    SimpleDraw(X_index, tmpBuf, rateY, baseLine);// 把缓冲区数据画出来  
                    X_index = X_index + tmpBuf.length;  
                    if (X_index > sfv.getWidth()) {  
                        X_index = 0;  
                    }  
                }  
            }  
        }  
        /** 
         * 绘制指定区域 
         *  
         * @param start 
         *            X轴开始的位置(全屏) 
         * @param buffer 
         *            缓冲区 
         * @param rate 
         *            Y轴数据缩小的比例 
         * @param baseLine 
         *            Y轴基线 
         */  
        void SimpleDraw(int start, byte[] buffer, int rate, int baseLine) {  
            if (start == 0)  
                oldX = 0;  
            Canvas canvas = sfv.getHolder().lockCanvas(  
                    new Rect(start, 0, start + buffer.length, sfv.getHeight()));// 关键:获取画布  
            canvas.drawColor(Color.DKGRAY);// 清除背景  
            int y;  
            for (int i = 0; i < buffer.length; i++) {// 有多少画多少  
                int x = i + start;  
                y = buffer[i] / rate + baseLine;// 调节缩小比例，调节基准线  
                canvas.drawLine(oldX, oldY, x, y, mPaint);  
                oldX = x;
                oldY = y;  
            }  
            sfv.getHolder().unlockCanvasAndPost(canvas);// 解锁画布，提交画好的图像  
        }  
    } 
    /*    *//**
     * 将裸音频转成.wav格式
     *//*
    private void copyWaveFile(String inFilename, String outFilename, int recBufSize) {
    	FileInputStream in = null;
		FileOutputStream out = null;
		long totalAudioLen = 0;
		long totalDataLen = totalAudioLen + 36;
		long longSampleRate = 8000;
		int channels = 1;
		long byteRate = 16 * longSampleRate * channels / 8;
		short[] data = new short[recBufSize];
		try {
			in = new FileInputStream(inFilename);
			out = new FileOutputStream(outFilename);
			totalAudioLen = in.getChannel().size();
			totalDataLen = totalAudioLen + 36;
			WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
					longSampleRate, channels, byteRate);
			while (in.read(data) != -1) {
				out.write(data);
			}
			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    *//**
	 * 这里提供一个头信息。插入这些信息就可以得到可以播放的文件。
	 * 为我为啥插入这44个字节，这个还真没深入研究，不过你随便打开一个wav
	 * 音频的文件，可以发现前面的头文件可以说基本一样哦。每种格式的文件都有
	 * 自己特有的头文件。
	 *//*
    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
			long totalDataLen, long longSampleRate, int channels, long byteRate) throws IOException{

    	byte[] header = new byte[44];
		header[0] = 'R'; // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16; // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1; // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (2 * 16 / 8); // block align
		header[33] = 0;
		header[34] = 16; // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
		out.write(header, 0, 44);
	}*/
}
