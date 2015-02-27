package com.example.pedometer;

import android.util.Log;

public class distanceNotifier implements stepListener {

	public interface Listener {
		public void valueChanged(double value);

		public void passValue();
	}

	private Listener mListener;
	// 默认参数
	double mDistance = 0;
	double mStepLength;

	// 构造函数
	public distanceNotifier(Listener listener, double stepLength) {
		mListener = listener;
		mStepLength = stepLength;
		reloadSettings();
	}

	@Override
	public void onStep() {
		// TODO Auto-generated method stub
		// 行走距离增加一个步长
		if (pedometer.pedometerstatus) {
			mDistance += mStepLength / 100.0;
			Log.d("distanceNotifier.onStep", "distance = " + mDistance);
			notifyListener();
		}
	}

	private void notifyListener() {
		// TODO Auto-generated method stub
		mListener.valueChanged(mDistance);
	}

	public void setDistance(float distance) {
		// 重新设置已经行走的距离
		mDistance = distance;
		notifyListener();
	}

	@Override
	public void passValue() {
		// TODO Auto-generated method stub

	}

	// 重新加载数据到stepService中
	public void reloadSettings() {
		notifyListener();
	}

}
