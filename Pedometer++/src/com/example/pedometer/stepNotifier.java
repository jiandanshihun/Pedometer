package com.example.pedometer;

import com.example.pedometer.calorieNotifier.Listener;

public class stepNotifier implements stepListener {

	// implemented outside this class
	public interface Listener {
		public void valueChanged(int value);

		public void passValue();
	}

	private Listener mListener;

	int steps = 0;

	public stepNotifier(Listener listener) {
		mListener = listener;
	}

	@Override
	public void onStep() {
		// TODO Auto-generated method stub
		if (pedometer.pedometerstatus) {
			steps++;
			notifyListener();
		}
	}

	@Override
	public void passValue() {
		// TODO Auto-generated method stub

	}

	void notifyListener() {
		mListener.valueChanged(steps);
	}

	void reloadSettings() {
		notifyListener();
	}

	public void resetValues() {
		steps = 0;
		// 更新数值之后通知监听器
		notifyListener();
	}

	public void setStep(int step) {
		this.steps = step;
		notifyListener();
	}
}
