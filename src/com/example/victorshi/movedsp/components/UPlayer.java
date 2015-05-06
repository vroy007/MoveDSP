package com.example.victorshi.movedsp.components;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

/**
 * 播放类
 * @author smnan
 *
 */
public class UPlayer extends MediaPlayer {

	private static final String TAG = "UPlayer";
	private Context context = null;
	private String path = null;
	private MediaPlayer mPlayer = null;
	
	public UPlayer(Context context, String path) {
		this.context = context;
		this.path = path;
		mPlayer = new MediaPlayer();
	}

	@Override
	public void start() {
		try {    
            //设置要播放的文件  
			Uri uri = Uri.parse(path);
			mPlayer = MediaPlayer.create(context, uri);
//            mPlayer.setDataSource(path);  
//            mPlayer.prepare();  
            //播放  
            mPlayer.start();
            mPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					Toast.makeText(context, "播放完毕……", Toast.LENGTH_SHORT).show();
					mPlayer.release();
				}
			});
        }catch(Exception e){  
            Log.e(TAG, "prepare() failed");    
        } 
	}
	
	@Override
	public void setOnCompletionListener(OnCompletionListener listener) {
		super.setOnCompletionListener(listener);
		Toast.makeText(context, "播放完毕……", Toast.LENGTH_SHORT).show();
		mPlayer.release();
	}

	@Override
	public void stop() {
		mPlayer.stop();  
        mPlayer.release();  
        mPlayer = null;
	}

}
