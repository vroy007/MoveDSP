package com.example.victorshi.movedsp.components;

import java.io.IOException;

import android.content.Context;
import android.media.MediaRecorder;
import android.util.Log;

import com.example.victorshi.movedsp.utils.IVoiceManager;
/**
 * 录音类
 * @author smnan
 *
 */
public class URecorder implements IVoiceManager{

	private static final String TAG = "URecorder";
	private Context context = null;
	private String path = null;
	private MediaRecorder mRecorder = null;
	
	public URecorder(Context context, String path) {
		this.context = context;
		this.path = path;
		mRecorder = new MediaRecorder();
	}

	@Override
	public boolean start() {
		//设置音源为Micphone    
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);    
        //设置封装格式    
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);    
        mRecorder.setOutputFile(path);    
        //设置编码格式    
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);    
    
        try {    
            mRecorder.prepare();    
        } catch (IOException e) {    
            Log.e(TAG, "prepare() failed");    
        }    
        //录音  
        mRecorder.start();    
        return false;
	}

	@Override
	public boolean stop() {
		mRecorder.stop();    
        mRecorder.release();    
        mRecorder = null;   
        return false;
	}

}
