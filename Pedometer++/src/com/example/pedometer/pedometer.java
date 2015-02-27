package com.example.pedometer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class pedometer extends Activity {

	private final String NAME = "pedometer_preferences";
	public static int MODE = Context.MODE_WORLD_READABLE
			+ Context.MODE_WORLD_WRITEABLE;
	public final String DATE_PATH = "date_path";
	public final String provider = LocationManager.NETWORK_PROVIDER;
	SharedPreferences mSharedPreferences;
	SharedPreferences mDate;
	static EditText H_value, W_value, SL_value;
	EditText plansteps;
	RadioGroup Mode_value;
	String mode = "true";
	String username;
	static Boolean config_way = false; // configS_way = false表示使用默认设置
	MapView mMapView = null;
	BaiduMap mMap;
	LocationManager mLM;
	BDLocation mCurLocation = null, mLastLocation = null;
	LocationClient mLocationClient;
	@SuppressWarnings("deprecation")
	Date lastDate; // 设置日期为2014年12月29号，周一
	int PlanSteps;
	static Boolean isFirst = true;
	LatLng startLatLng;
	List<LatLng> pts = new ArrayList<LatLng>();
	Button pause;
	static Boolean pedometerstatus = false;
	Boolean isStopped = false;
	static int clickTimes = 0;
	LatLng begin, end;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub

		// 2、通过手动添加来配置上下文菜单选项
		menu.add(1, 1, 1, "配置运动参数");
		menu.add(1, 2, 2, "自定义用户参数");
		menu.add(1, 3, 3, "退出");

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		// return super.onOptionsItemSelected(item);
		int item_id = item.getItemId();
		if (item_id == 1) {
			Intent i = new Intent(ctx, userSetting.class);
			startActivity(i);
		}
		if (item_id == 2) {
			LayoutInflater mLI = LayoutInflater.from(ctx);
			final View dialogView = mLI.inflate(R.layout.userinfo_dialog, null);
			AlertDialog.Builder mBuilder = new AlertDialog.Builder(ctx);
			mBuilder.setTitle("自定义参数");
			mBuilder.setView(dialogView);

			H_value = (EditText) dialogView.findViewById(R.id.H_value);
			W_value = (EditText) dialogView.findViewWithTag(R.id.W_value);
			SL_value = (EditText) dialogView.findViewById(R.id.SL_value);
			Mode_value = (RadioGroup) dialogView.findViewById(R.id.Mode_value);

			Mode_value
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(RadioGroup group,
								int checkedId) {
							// TODO Auto-generated method stub
							if (checkedId == R.id.radio0) {
								mode = "true";
							}
							if (checkedId == R.id.radio1) {
								mode = "false";
							}
						}
					});
			
			mBuilder.setNegativeButton("确定",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							String HH = ((EditText)dialogView.findViewById(R.id.H_value)).getText().toString();
							String WW = ((EditText)dialogView.findViewById(R.id.W_value)).getText().toString();
							String SL = ((EditText)dialogView.findViewById(R.id.SL_value)).getText().toString();
							// TODO Auto-generated method stub
							mSharedPreferences = getSharedPreferences(NAME
									+ username, MODE);
							Editor e = mSharedPreferences.edit();
							e.putString("userHeight", HH);
							e.putString("userWeight", WW);
							e.putString("Steplength", SL);
							e.putString("pedometerMode", mode);
							e.commit();
							config_way = true;
							dialog.dismiss();
						}

					});
			mBuilder.setPositiveButton("取消", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			});
			mBuilder.show();
		}

		if (item_id == 3) { // 退出
			if (mService != null) {
				// 解除绑定
				unbindService(mConnection);
				// 停止服务
				stopService(new Intent(pedometer.this, stepService.class));
			}
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			startActivity(intent);
			finish();
		}
		return true;
	}

	static TextView dist_value;
	static TextView step_value;
	static TextView calorie_value;
	Button start, stop;
	// MapView mMapView;
	Context ctx;

	Handler locate;
	static Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			Bundle bb = msg.getData();
			String type = bb.getString("type");
			if (type.equals("steps")) {
				int steps = bb.getInt(type, 0);
				step_value.setText(steps + "");
			} else if (type.equals("distance")) {
				double dist = bb.getDouble(type);
				dist_value.setText(("" + (dist + 0.0001)).substring(0, 4));
				Log.d("handler", "in handler distance");
			} else if (type.equals("calorie")) {
				double calorie = bb.getDouble(type);
				Log.d("handler", "calorite = " + calorie);
				calorie_value
						.setText(((calorie + 0.0001) + "").substring(0, 4));
			} else {
				step_value.setText("0");
				dist_value.setText("0.00");
				calorie_value.setText("0.00");
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.pedometer);
		Toast.makeText(this, "hi RUNNER!", Toast.LENGTH_LONG).show();
		ctx = this;

		username = InitialUI.name;
		mLM = (LocationManager) getSystemService(LOCATION_SERVICE);
		mMapView = (MapView) findViewById(R.id.bmapView);

		// mMapView = new MapView(this,new BaiduMapOptions().mapStatus(new
		// MapStatus.Builder().target(center).build()));
		mMap = mMapView.getMap();
		// mMapView.

		mMap.setMyLocationEnabled(true);
		// 构造定位数据

		dist_value = (TextView) findViewById(R.id.distance_value);
		step_value = (TextView) findViewById(R.id.step_value);
		calorie_value = (TextView) findViewById(R.id.calory_value);
		// mMapView = (MapView)findViewById(R.id.bmapView);

		start = (Button) findViewById(R.id.begin);
		stop = (Button) findViewById(R.id.stop);
		pause = (Button) findViewById(R.id.pause);
		start.setOnClickListener(listener);
		stop.setOnClickListener(listener);
		pause.setOnClickListener(listener);

		// LatLng center = new LatLng(12, 12);
		Location location = mLM.getLastKnownLocation(provider);
		double x, y;
		if (location == null) {
			x = 22;
			y = 113;
			Log.d("pedometer", "do not find location!!");
		} else {
			x = location.getLatitude();
			y = location.getLongitude();
			Log.d("pedometer", "find location!!");
			Log.d("pedometer", "x = " + x + " " + "y = " + y);
		}
		LatLng center = new LatLng(x, y);
		CoordinateConverter converter = new CoordinateConverter();
		converter.from(CoordType.GPS);
		converter.coord(center);
		LatLng center2 = converter.convert();
		MyLocationData locData = new MyLocationData.Builder()
				.latitude(center2.latitude).longitude(center2.longitude)
				.build();
		MapStatus mMapStatus = new MapStatus.Builder().target(center2).zoom(18)
				.build();
		// 设置定位数据
		begin = center2;
		mMap.setMyLocationData(locData);
		MapStatusUpdate mMapStatusUpdata = MapStatusUpdateFactory
				.newMapStatus(mMapStatus);
		mMap.animateMapStatus(mMapStatusUpdata);
		mLM.requestLocationUpdates(provider, 0, 0, locationListener);
		// 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
		// BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
		// .fromResource(R.drawable.ic_launcher);
		// MyLocationConfiguration config = new MyLocationConfiguration(
		// com.baidu.mapapi.map.MyLocationConfiguration.LocationMode.NORMAL,
		// true, mCurrentMarker);
		// mMap.setMyLocationConfigeration(config);
		// // 当不需要定位图层时关闭定位图层
		// mMap.setMyLocationEnabled(false);
		Log.d("pedometer", "here");
		setWeekPlan(); // 根据日期判断是否需要设置周计划
	}

	LocationListener locationListener = new LocationListener() {

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onLocationChanged(Location arg0) {
			// TODO Auto-generated method stub
			updateLocation(arg0);
			Log.d("pedometer", "into the onLocationChanged");
		}
	};

	public void updateLocation(Location location) {
		if (location != null) {
			double lng = location.getLongitude();
			double lat = location.getLatitude();
			LatLng point = new LatLng(lat, lng);
			CoordinateConverter converter = new CoordinateConverter();
			converter.from(CoordType.GPS);
			converter.coord(point);
			
			LatLng desLatLng = converter.convert();
			MyLocationData data = new MyLocationData.Builder().latitude(desLatLng.latitude).
					longitude(desLatLng.longitude).build();
			mMap.setMyLocationData(data);
			end = begin;
			begin = desLatLng;
			MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(begin);
			mMap.animateMapStatus(u);
			
			List<LatLng> points = new ArrayList<LatLng>();
			points.add(begin);
			points.add(end);
			OverlayOptions line = new PolylineOptions().width(5).color(0xaaff0000).points(points);
			mMap.addOverlay(line);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (mService != null) {
			// 解除绑定
			unbindService(mConnection);
			// 停止服务
			stopService(new Intent(pedometer.this, stepService.class));
		}
		super.onDestroy();
	}

	private stepService mService;
	// 实现stepService类中的Icallback接口
	public static stepService.ICallback mCallback = new stepService.ICallback() {
		public void stepChanged(int value) {
			Bundle b = new Bundle();
			b.putString("type", "steps");
			b.putInt("steps", value);
			Message msg = new Message();
			msg.setData(b);
			mHandler.sendMessage(msg);
		}

		public void distanceChanged(double value) {
			Bundle b = new Bundle();
			b.putString("type", "distance");
			b.putDouble("distance", value);
			Message msg = new Message();
			msg.setData(b);
			mHandler.sendMessage(msg);
			Log.d("pedometer", "detect distance changed!!");
			Log.d("pedometer", "检测到的距离数值是：" + value);
		}

		public void caloriesChanged(double value) {
			Bundle b = new Bundle();
			b.putString("type", "calorie");
			b.putDouble("calorie", value);
			Message msg = new Message();
			msg.setData(b);
			mHandler.sendMessage(msg);
			Log.d("pedometer", "calorite = " + value);
		}
	};

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {

			// 通过binder获取service对象
			mService = ((stepService.StepBinder) service).getService();

			// 设置接口，主activity与service进行通信
			mService.registerCallback(mCallback);
			Log.d("pedometer", "has register call back");
			mService.reloadSettings();

		}

		public void onServiceDisconnected(ComponentName className) {
			mService = null;
		}
	};

	android.view.View.OnClickListener listener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.begin:
				// 绑定服务，获得service对象
				// 首先启动服务，防止退出的时候服务停止
				Toast.makeText(ctx, "begin counting step", Toast.LENGTH_LONG)
						.show();
				pedometerstatus = true;
				isStopped = false;
				startService(new Intent(pedometer.this, stepService.class));
				bindService(new Intent(pedometer.this, stepService.class),
						mConnection, Context.BIND_AUTO_CREATE
								| Context.BIND_DEBUG_UNBIND);
				break;
			case R.id.stop:
				if (!isStopped) {
					isStopped = true;
					pedometerstatus = false;
					clickTimes = 0;
					pause.setText("暂停");
					Toast.makeText(ctx, "end counting step", Toast.LENGTH_LONG)
							.show();
					// mSteps是计步的步数
					int steps = stepService.mSteps;
					// stepService.mSteps = 0;
					double dist = stepService.mDistance;
					// stepService.mDistance = 0;
					double cal = stepService.mCalories;
					Log.d("pedometer_stop", "after get value");
					// stepService.mCalories = 0;
					mService.resetValues();
					Log.d("pedometer_stop", "after reset values");

					Bundle b = new Bundle();
					b.putString("type", "stop");
					Message msg = new Message();
					msg.setData(b);
					mHandler.sendMessage(msg);
					// 开启新线程存储本次锻炼的步数
					new Thread(new SendDataThread(username, steps, dist, cal))
							.start();
					Log.d("pedometer_stop", "after send data");
					if (mService != null) {
						// 解除绑定
						unbindService(mConnection);
						// 停止服务
						stopService(new Intent(pedometer.this,
								stepService.class));
					}
				}
				break;
			case R.id.pause:
				if (pause.getText().toString().equals("暂停")) {
					if (!isStopped) {
						pause.setText("继续");
						pedometerstatus = false;
					}
				} else {
					pause.setText("暂停");
					pedometerstatus = true;
				}
				break;
			}
		}
	};

	private void resetValues(boolean updateDisplay) {
		if (mService != null) {
			mService.resetValues();
		}
	}

	public int daysBetween(Date date1, Date date2) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date1);
		long time1 = cal.getTimeInMillis();
		cal.setTime(date2);
		long time2 = cal.getTimeInMillis();
		long between_days = (time2 - time1) / (1000 * 3600 * 24);

		return Integer.parseInt(String.valueOf(between_days));
	}

	public void InputPlan() {
		LayoutInflater mLI = LayoutInflater.from(ctx);
		View dialogView = mLI.inflate(R.layout.weekplan_dialog, null);
		AlertDialog.Builder mBuilder = new AlertDialog.Builder(ctx);
		mBuilder.setTitle("设置本周计划");
		mBuilder.setView(dialogView);
		plansteps = (EditText) dialogView.findViewById(R.id.weekplan_input);
		mBuilder.setNegativeButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				PlanSteps = Integer.parseInt(plansteps.getText().toString());
				dialog.dismiss();
			}

		});
		mBuilder.setPositiveButton("取消", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		mBuilder.show();
	}

	public void setWeekPlan() {
		SharedPreferences dateSP = getSharedPreferences(DATE_PATH, MODE);
		Boolean flag = dateSP.getBoolean("thisweek", false);
		int lastY = dateSP.getInt("year", 2014 - 1900);
		int lastM = dateSP.getInt("month", 11);
		int lastD = dateSP.getInt("day", 29);
		// 缺省值为2014-12-29
		lastDate = new Date(lastY, lastM, lastD);
		Date curDate = new Date();
		int interval = daysBetween(lastDate, curDate);
		interval = interval % 7;
		Editor ee = dateSP.edit();
		if (interval >= 7) { // 表示新的一周开始，更新周计划, 吧最近的一个周一设置为周计划的其实日期
			int offset = interval - 7; // 距离最近的星期一的天数
			@SuppressWarnings("deprecation")
			int year = lastDate.getYear();
			@SuppressWarnings("deprecation")
			int month = lastDate.getMonth();
			@SuppressWarnings("deprecation")
			int day = lastDate.getDate() - offset;
			ee.putInt("year", year);
			ee.putInt("month", month);
			ee.putInt("day", day);
			ee.commit();
			InputPlan();
			username = InitialUI.name;
			new Thread(new updateWeekPlan(username, PlanSteps)).start();

		}
	}

	public class updateWeekPlan implements Runnable {
		String username;
		int plan;

		public updateWeekPlan(String username, int plan) {
			this.username = username;
			this.plan = plan;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String url = "http://104.131.156.81:8888/new_week";
			HttpPost request = new HttpPost(url);
			// 参数为名值对
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("username", username));
			params.add(new BasicNameValuePair("plan", plan + ""));

			try {
				request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
				HttpClient client = getHttpClient();
				// 执行请求返回相应
				HttpResponse response = client.execute(request);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public class SendDataThread implements Runnable {
		int steps;
		double cal, dist;
		String username;

		public SendDataThread(String username, int steps, double dist,
				double cal) {
			this.username = username;
			this.steps = steps;
			this.dist = dist;
			this.cal = cal;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			// url of server
			String url = "http://104.131.156.81:8888/update_run_info";
			HttpPost request = new HttpPost(url);
			// 参数为名值对
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("username", username));
			params.add(new BasicNameValuePair("step", steps + ""));
			params.add(new BasicNameValuePair("dis", dist + ""));
			params.add(new BasicNameValuePair("calories", cal + ""));
			try {
				// 设置请求参数项
				request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
				HttpClient client = getHttpClient();
				// 执行请求返回相应
				HttpResponse response = client.execute(request);

				Bundle b = new Bundle();
				b.putString("type", "clear");
				Message msg = new Message();
				msg.setData(b);
				mHandler.sendMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

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
