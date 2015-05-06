package com.example.victorshi.movedsp.model;

import java.util.ArrayList;

import com.cyberway.frame.models.BaseModel;
/**
 * 数据model
 * @author smnan
 *
 */
public class DataModel extends BaseModel {

	private ArrayList<byte[]> bufWave; //时域波形数据
	private ArrayList<byte[]> bufFFt; //频域波形数据
	private int arrSize; //每个数组长度
	private int listSize; //每个list长度
	private ArrayList<float[]> resWave; //绘制时域波形数据
	private ArrayList<float[]> resFFt; //绘制频域波形数据
	
	public ArrayList<byte[]> getBufWave() {
		return bufWave;
	}
	public void setBufWave(ArrayList<byte[]> bufWave) {
		this.bufWave = bufWave;
	}
	
	public ArrayList<byte[]> getBufFFt() {
		return bufFFt;
	}
	public void setBufFFt(ArrayList<byte[]> bufFFt) {
		this.bufFFt = bufFFt;
	}
	
	public int getArrSize() {
		return this.bufWave.get(0).length;
	}
	public void setArrSize(int arrSize) {
		this.arrSize = arrSize;
	}
	
	public int getListSize() {
		return this.bufWave.size();
	}
	public void setListSize(int listSize) {
		this.listSize = listSize;
	}
	
	public ArrayList<float[]> getResWave() {
		return resWave;
	}
	public void setResWave(ArrayList<float[]> resWave) {
		this.resWave = resWave;
	}
	
	public ArrayList<float[]> getResFFt() {
		return resFFt;
	}
	public void setResFFt(ArrayList<float[]> resFFt) {
		this.resFFt = resFFt;
	}
	
}
