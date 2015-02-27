package com.example.pedometer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.GpsStatus.Listener;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class stepService extends Service {

	private static String url = "";
	private static String responseMsg = "";
	// 各项值的变化的监听器
	// private stepListener mStepListener;
	private static stepNotifier mStepNotifier;
	private static distanceNotifier mDistanceNotifier;
	private static calorieNotifier mCaloriesNotifier;

	private SensorManager mSensorManager;
	private PowerManager.WakeLock wakeLock;
	private NotificationManager mNM;

	// 运动参数
	static public double mDistance; // 移动距离
	static public double mCalories; // 消耗的能量
	static public int mSteps; // 行走的步数
	private double mStepLength = 70; // 用户步长
	private double mWeight = 65; // 用户体重
	private double mHeight = 175; // 用户身高
	private Boolean mMode = true;

	// 传感器事件监听器
	private stepCounter mStepCounter;

	// 从指定sharedPreferenced文件中获取运动参数
	private final String NAME = "pedometer_preferences";
	@SuppressWarnings("deprecation")
	public static int MODE = Context.MODE_WORLD_READABLE
			+ Context.MODE_WORLD_WRITEABLE;
	SharedPreferences mSharedPreferences;

	// 当屏幕亮度变暗时
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) { // 判断接收到的事件
				// Unregisters the listener and registers it again.

			}
		}
	};

	public class StepBinder extends Binder {
		stepService getService() {
			return stepService.this;
		}
	}

	// 定义类接口
	public interface ICallback {
		public void distanceChanged(double value);

		public void stepChanged(int value);

		public void caloriesChanged(double value);
	}

	private ICallback mCallback;

	/**
	 * Receives messages from activity.
	 */
	private final IBinder mBinder = new StepBinder();

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		if(pedometer.clickTimes == 0)
			showNotification();
		mStepCounter = new stepCounter();
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mCallback = pedometer.mCallback;
		// 注册传感器时间监听器
		registSensorEvent();

		// 动态注册广播监听器
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		registerReceiver(mReceiver, filter);

		// 获取配置文件
		if (pedometer.config_way) {
			mSharedPreferences = getSharedPreferences(NAME + InitialUI.name,
					MODE);
		} else {
			mSharedPreferences = getSharedPreferences(NAME, MODE);
		}
		mHeight = Double.valueOf(mSharedPreferences.getString("userWeight",
				"175"));
		mWeight = Double.valueOf(mSharedPreferences.getString("userWeight",
				"60"));
		mStepLength = Double.valueOf(mSharedPreferences.getString(
				"userStepLength", "70.0"));
		mMode = Boolean.valueOf(mSharedPreferences.getString("pedometerMode",
				"true"));

		if (pedometer.clickTimes == 0) {
			pedometer.clickTimes++;
			// 步数变化监听器
			mStepNotifier = new stepNotifier(stepListener);
			// 初始化步数为0
			mStepNotifier.setStep(0);
			// 作为监听器放入stepCounter
			mStepCounter.addListener(mStepNotifier);
			// 距离变化监听器
			mDistanceNotifier = new distanceNotifier(mDistanceListener,
					mStepLength);
			// 初始化为0
			mDistanceNotifier.setDistance(0.0f);
			// 增加监听器
			mStepCounter.addListener(mDistanceNotifier);
			// 能量变化监听器
			mCaloriesNotifier = new calorieNotifier(mCalorieListener, mWeight,
					mStepLength, mMode);
			// 初始化能量值为0
			mCaloriesNotifier.setCalories(0.0f);
			// 增加监听器
			mStepCounter.addListener(mCaloriesNotifier);
		}

	}

	// 服务销毁时，将数据保存到数据库中
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		// 注销广播接收器
		unregisterReceiver(mReceiver);
		// 注销传感器监听事件
		unregistSensorEvent();

		mNM.cancel(R.string.app_name);

		// 将数据存储在数据库中
		// new Thread(new AccesServer(mDistance + "", mSteps + "", mCalories +
		// "", mSpeed + "")).start();

		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	public void registSensorEvent() {
		// 获取加速度计
		Sensor mSensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		// 使用事件监听器mStepCounter监听加速度计mSensor
		mSensorManager.registerListener(mStepCounter, mSensor,
				SensorManager.SENSOR_DELAY_FASTEST);
	}

	public void unregistSensorEvent() {
		// 取消传感器事件监听器对传感器的监听
		mSensorManager.unregisterListener(mStepCounter);
	}

	// 回传数值
	private stepNotifier.Listener stepListener = new stepNotifier.Listener() {

		@Override
		public void valueChanged(int value) {
			// TODO Auto-generated method stub
			
			mSteps = value;
			passValue();
		}

		@Override
		public void passValue() {
			// TODO Auto-generated method stub
			if (mCallback != null) {
				mCallback.stepChanged(mSteps);
			}
		}
	};

	// 从distanceNotifier中取出距离值并传入stepService
	// 参数value就是从distanceNotifier传入的数据
	private distanceNotifier.Listener mDistanceListener = new distanceNotifier.Listener() {
		public void valueChanged(double value) {
			mDistance = value;
			passValue();
		}

		public void passValue() {
			if (mCallback != null) {
				Log.d("stepService.passvalue", "this is");
				mCallback.distanceChanged(mDistance);
			}
		}
	};

	// 实现抽象接口
	private calorieNotifier.Listener mCalorieListener = new calorieNotifier.Listener() {

		@Override
		public void valueChanged(double value) {
			// TODO Auto-generated method stub
			mCalories = value;
			passValue();
		}

		@Override
		public void passValue() {
			// TODO Auto-generated method stub
			Log.d("stepService.passvalue", "this is");
			if (mCallback != null)
				mCallback.caloriesChanged(mCalories); // 传递数值
		}

	};

	// 用户重置数据,各项运动参数全部重置为0
	public static void resetValues() {
		mDistanceNotifier.setDistance(0);
		mCaloriesNotifier.setCalories(0.0f);
		mStepNotifier.setStep(0);
	}

	private void showNotification() {
		Log.d("stepService", "in the show notification");
		CharSequence text = getText(R.string.app_name);
		// 设置通知消息栏的图标
		@SuppressWarnings("deprecation")
		Notification notification = new Notification(R.drawable.icon_small, null,
				System.currentTimeMillis());
		notification.flags = Notification.FLAG_NO_CLEAR
				| Notification.FLAG_ONGOING_EVENT;
		Intent pedometerIntent = new Intent();
		pedometerIntent.setComponent(new ComponentName(this, pedometer.class));
		pedometerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				pedometerIntent, 0);
		// 具体通知的内容
		notification.setLatestEventInfo(this, text,
				"enjoy the happiness of running", contentIntent);

		mNM.notify(R.string.app_name, notification);
	}

	public void reloadSettings() {

		if (mStepCounter != null) {
			mStepCounter.setSensitivity(Float.valueOf(10));
		}
		// 重新加载数据
		// if (mStepDisplayer != null) mStepDisplayer.reloadSettings();
		if (mDistanceNotifier != null)
			mDistanceNotifier.reloadSettings();
		if (mCaloriesNotifier != null)
			mCaloriesNotifier.reloadSettings();
		// if (mSpeedNotifier != null) mSpeedNotifier.reloadSettings();
	}

	public void registerCallback(ICallback cb) {
		mCallback = cb;
		Log.d("in register callback", "mCallback");
		// mStepDisplayer.passValue();
		// mPaceListener.passValue();
	}

	public class AccesServer implements Runnable {
		String distance;
		String steps;
		String calories;
		String speed;

		public AccesServer(String distance, String steps, String calories,
				String speed) {
			this.distance = distance;
			this.steps = steps;
			this.calories = calories;
			this.speed = speed;
		}

		// 将本次计步的信息存入数据库中
		@Override
		public void run() {
			// TODO Auto-generated method stub
			HttpPost request = new HttpPost(url);
			// String responseMsg;
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("distance", distance));
			params.add(new BasicNameValuePair("steps", steps));
			params.add(new BasicNameValuePair("calories", calories));
			params.add(new BasicNameValuePair("speed", speed));
			try {
				request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			HttpClient client = getHttpClient();
			try {
				HttpResponse response = client.execute(request);
				if (response.getStatusLine().getStatusCode() == 200) // 链接成功
				{
					// 获得响应信息
					responseMsg = EntityUtils.toString(response.getEntity());
				}
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// 判断是否请求成功

		}

		// 初始化httpClient，并设置请求超时和数据超时时间
		private HttpClient getHttpClient() {
			// TODO Auto-generated method stub
			BasicHttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 5 * 1000);
			HttpConnectionParams.setSoTimeout(httpParams, 10 * 1000);
			HttpClient client = new DefaultHttpClient(httpParams);
			return client;
		}

	}
}
