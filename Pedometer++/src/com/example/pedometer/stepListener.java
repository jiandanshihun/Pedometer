package com.example.pedometer;

//监听速度，距离，消耗能量的变化
public interface stepListener {
	public void onStep();

	public void passValue();
}
