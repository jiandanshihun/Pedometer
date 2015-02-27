package com.example.pedometer;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class calorieNotifier implements stepListener {

	public interface Listener {
		public void valueChanged(double value);

		public void passValue();
	}

	private Listener mListener;

	// 能量消耗参数
	private static double METRIC_RUNNING_FACTOR = 1.02784823;
	private static double METRIC_WALKING_FACTOR = 0.708;

	// 默认参数的值
	private double mCalories = 0.0f;
	boolean mIsRunning;
	double mStepLength;
	double mBodyWeight;

	public calorieNotifier(Listener listener, double bodyweight,
			double steplength, boolean isRunning) {
		mListener = listener;
		mIsRunning = isRunning;
		mBodyWeight = bodyweight;
		mStepLength = steplength;
		// 重新加载设置
		reloadSettings();
	}

	@Override
	public void onStep() {
		// TODO Auto-generated method stub
		// 能量 (cal)= 体重(Kg) * 距离(m) * METRIC_RUNNING_FACTOR
		if (pedometer.pedometerstatus) {
			mCalories += (mBodyWeight * (mIsRunning ? METRIC_RUNNING_FACTOR
					: METRIC_WALKING_FACTOR))
			// Distance:
					* mStepLength / 100.0;// centimeters
			Log.d("CalorieNotifier.onStep", "mCalories = " + mCalories);
			// centimeters/kilometer
			notifyListener();
		}
	}

	@Override
	public void passValue() {
		// TODO Auto-generated method stub

	}

	private void notifyListener() {
		// 发送更新数值的消息到其他类中
		mListener.valueChanged(mCalories);
	}

	public void reloadSettings() {
		notifyListener();
	}

	// 重置数值
	public void resetValues() {
		mCalories = 0;
		notifyListener();
	}

	// 设置步长
	public void setStepLength(float stepLength) {
		mStepLength = stepLength;
	}

	// 设置消耗的能量值
	public void setCalories(float calories) {
		mCalories = calories;
		notifyListener();
	}

}
