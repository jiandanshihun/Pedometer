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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

public class registerUI extends Activity {

	Button sign;
	EditText username;
	EditText password;
	EditText confirm_password;
	EditText Height, Weight;
	RadioGroup group;
	ProgressDialog mProDialog;
	Context ctx;
	String name, psw;
	String height = "175", weight = "60";
	String sex = "man";
	String responseMsg = "";
	private static String url = "http://104.131.156.81:8888/register";
	private final String store = "userinfo";
	// 存放用户注册信息
	SharedPreferences mSharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		ctx = this;
		username = (EditText) findViewById(R.id.reg_name_in);
		password = (EditText) findViewById(R.id.reg_password_in);
		confirm_password = (EditText) findViewById(R.id.confirm_password_in);
		sign = (Button) findViewById(R.id.sign_in);
		Height = (EditText) findViewById(R.id.height_in);
		Weight = (EditText) findViewById(R.id.weight_in);
		RadioGroup group = (RadioGroup) this.findViewById(R.id.radioGroup1);

		sign.setOnClickListener(signListener);
	}

	OnClickListener signListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			name = username.getText().toString();
			psw = password.getText().toString();
			height = Height.getText().toString();
			weight = Weight.getText().toString();
			mProDialog = ProgressDialog.show(ctx, "", "请稍后", true);
			new Thread(new registRunnable(url, name, psw, height, weight))
					.start();
		}
	};

	OnCheckedChangeListener checkChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			// TODO Auto-generated method stub
			int radioButtonId = group.getCheckedRadioButtonId();
			RadioButton rb = (RadioButton) registerUI.this
					.findViewById(radioButtonId);
			sex = rb.getText().toString();
		}
	};

	// 初始化httpClient，并设置请求超时和数据超时时间
	private HttpClient getHttpClient() {
		// TODO Auto-generated method stub
		BasicHttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 5 * 1000);
		HttpConnectionParams.setSoTimeout(httpParams, 10 * 1000);
		HttpClient client = new DefaultHttpClient(httpParams);
		return client;
	}

	@TargetApi(19)
	Boolean IsUsernameValid(String username) {
		if (username.equals(""))
			return false;
		int len = username.length();
		for (int i = 0; i < len; i++) {
			char ch = username.charAt(i);
			if (Character.isDigit(ch))
				continue;
			if (Character.isAlphabetic(ch))
				continue;
			return false;
		}
		return true;
	}

	public class registRunnable implements Runnable {
		String url;
		String name, psw, height, weight;

		public registRunnable(String url, String name, String psw,
				String height, String weight) {
			this.url = url;
			this.name = name;
			this.psw = psw;
			this.height = height;
			this.weight = weight;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			// 注册信息存在本地sharedPreference文件中
			mSharedPreferences = getSharedPreferences(store,
					MODE_WORLD_WRITEABLE);
			Editor ee = mSharedPreferences.edit();
			ee.putString(name, psw);
			ee.commit();

			// 注册信息发送至数据库
			HttpPost request = new HttpPost(url);
			// String responseMsg;
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("username", name));
			params.add(new BasicNameValuePair("password", psw));
			params.add(new BasicNameValuePair("height", height));
			params.add(new BasicNameValuePair("weight", weight));
			params.add(new BasicNameValuePair("sex", sex));
			// 设置请求参数项
			try {
				request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
				HttpClient client = getHttpClient();
				// 执行请求返回相应
				try {
					HttpResponse response = client.execute(request);
					// 判断是否请求成功
					if (response.getStatusLine().getStatusCode() == 200) // 链接成功
					{
						// 获得响应信息
						responseMsg = EntityUtils
								.toString(response.getEntity());
					}
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Bundle b = new Bundle();
			Message msg = new Message();
			b.putString("result", responseMsg);
			msg.setData(b);
			mhandler.sendMessage(msg);
		}
	}

	Handler mhandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			// super.handleMessage(msg);
			Bundle bundle = msg.getData();
			String responseMsg = bundle.getString("result");
			if (responseMsg.equals("register successful")) {
				Toast.makeText(ctx, "注册成功，3秒之后返回....", Toast.LENGTH_LONG)
						.show();
				try {
					// 延迟3秒
					Thread.sleep(3 * 1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Intent intent = new Intent(ctx, InitialUI.class);
				startActivity(intent);
			} else if (IsUsernameValid(name)) {
				Toast.makeText(ctx, "用户名不合法，请重新输入！！", Toast.LENGTH_LONG).show();
				username.setText("");
			} else if (responseMsg.equals("username is exist")) {
				Toast.makeText(ctx, "用户名已经存在，请重新输入！！", Toast.LENGTH_LONG)
						.show();
				username.setText("");
			}
			mProDialog.dismiss();
		}
	};
}
