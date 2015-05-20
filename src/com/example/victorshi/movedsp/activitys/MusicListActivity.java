package com.example.victorshi.movedsp.activitys;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cyberway.frame.adapters.CommonListAdapter;
import com.cyberway.frame.components.RefreshListView;
import com.cyberway.frame.utils.FileUtils;
import com.example.victorshi.movedsp.R;
import com.example.victorshi.movedsp.utils.CustomApplication;
/**
 * mp3获取信号源
 * @author smnan
 *
 */
public class MusicListActivity extends Activity implements OnClickListener {

	static final String path = "/sdcard/moveDsp";
	
	private static final String TAG = "MusicListActivity";
	private Context mContext = null;
	private CustomApplication application = null;
	
	private ImageButton btnBack = null;
	private TextView tvTitle = null;
	private ImageButton btnSure = null;
	
	private RefreshListView rlvMusic = null;
	private Adapter mAdapter = null;
	private List<File> listFile = new ArrayList<File>();
	private List<String> listName = new ArrayList<String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_music_list);
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
		tvTitle.setText("MP3列表");
		btnSure = (ImageButton) this.findViewById(R.id.btnRight);
		btnSure.setImageResource(R.drawable.btn_music_sure);
		rlvMusic = (RefreshListView) this.findViewById(R.id.rlvMP3);
	}

	private void initData() {
		if(!FileUtils.checkFilePathExists(path)) {
			Toast.makeText(MusicListActivity.this, "路径不存在！", Toast.LENGTH_SHORT).show();
			finish();
		}
		else {
			listFile = FileUtils.listPathFiles(path);
			for(int i = 0; i < listFile.size(); i++)
				listName.add(listFile.get(i).getName());
			
			mAdapter = new Adapter(mContext);
			rlvMusic.setAdapter(mAdapter);
			mAdapter.addDatas(listName);
		}
	}

	private void initEvent() {
		btnBack.setOnClickListener(this);
		btnSure.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v == btnBack)
			finish();
		else if (v == btnSure) {
			for(int i = 0; i < mAdapter.isSelected.size(); i++) {
				if(mAdapter.isSelected.get(i)) {
					String lastPath = listName.get(i);
					Intent mIntent = new Intent();
					mIntent.putExtra("path", lastPath);
					this.setResult(0, mIntent);
				}
			}
			finish();
		}
	}

	private class Adapter extends CommonListAdapter {

		private Context context;
		private LayoutInflater mInflater;
		private HashMap<Integer, Boolean> isSelected;
		
		public Adapter(Context context) {
			this.context = context;
			mInflater = LayoutInflater.from(context);
			initMap();
		}

		public void initMap() {
			isSelected = new HashMap<Integer, Boolean>();
			for(int i = 0; i < listFile.size(); i++)
				isSelected.put(i, false);
		}
		
		@Override
		public View bindView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if(convertView == null || convertView.getTag() == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.item_music_select, null);
				holder.tvDetail = (TextView) convertView.findViewById(R.id.tvMusicName);
				holder.cbMusic = (CheckBox) convertView.findViewById(R.id.cbMusic);
				convertView.setTag(holder);
			}
			else
				holder = (ViewHolder) convertView.getTag();
			
			String name = (String) list.get(position);
			holder.tvDetail.setText(name);
			holder.cbMusic.setChecked(isSelected.get(position));
			
			convertView.setOnClickListener(new ItemClickListener(holder, name, position));
			
			return convertView;
		}
		
		class ViewHolder {
			TextView tvDetail;
			CheckBox cbMusic;
		}
		
		private class ItemClickListener implements OnClickListener {

			private ViewHolder holder = null;
			private int position = -1;
			private String Name = null;
			
			public ItemClickListener(ViewHolder holder, String name, int position) {
				this.holder = holder;
				Name = name;
				this.position = position;
			}

			@Override
			public void onClick(View v) {
				initMap();
				if(holder.cbMusic.isChecked()) {
					btnSure.setVisibility(View.GONE);
				}
				else {
					isSelected.put(position, true);
					btnSure.setVisibility(View.VISIBLE);
				}
				mAdapter.notifyDataSetChanged();
			}
			
		}
		
	}
}
