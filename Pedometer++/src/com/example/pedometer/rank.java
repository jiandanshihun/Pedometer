package com.example.pedometer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class rank extends ActionBarActivity {

	ListView ranklist;
	Handler handler;
	String[] rankName;
	String[] rankSteps;
	private String database_url = "http://104.131.156.81:8888/inquire_rank";

	List<Map<String, Object>> mDataList = new ArrayList<Map<String, Object>>();

	private void setData(String[] names, String steps[]) {
		int count = 1;
		for (String name : names) {
			if (count == 11) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("rank", name);
				map.put("name", "myself");
				map.put("steps", steps[count - 1]);
				mDataList.add(map);
			} else {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("rank", count);
				map.put("name", name);
				map.put("steps", steps[count - 1]);
				mDataList.add(map);
			}
			count++;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rank);
		ranklist = (ListView) findViewById(R.id.ranklist);
		handler = new Handler();

		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpPost httpPost = new HttpPost(database_url);
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("username", InitialUI.name));
				try {
					httpPost.setEntity(new UrlEncodedFormEntity(params,
							HTTP.UTF_8));
				} catch (UnsupportedEncodingException e) {
					Log.d("Unsupported Encoding Exception", "ttttttttttttttt");
					e.printStackTrace();
				}
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpResponse response;
				try {
					response = httpClient.execute(httpPost);
					HttpEntity result = response.getEntity();
					String reqData = EntityUtils.toString(result);
					Log.d("ttttttttttttttttttttt", reqData);
					String res = reqData.substring(2, reqData.length() - 2);
					Log.d("ttttttttttttttttttttt", res);
					String[] tmp = res.split("], \\[");
					rankName = new String[tmp.length];
					rankSteps = new String[tmp.length];
					int ct = 0;
					for (String m : tmp) {
						Log.d("ttttttttttttttttttttt", m);
						String[] pair = m.split(", ");
						Log.d("ttttttttttttttttttttt", pair[0]);
						Log.d("ttttttttttttttttttttt", pair[1]);
						rankName[ct] = pair[0].substring(1,pair[0].length()-1);
						rankSteps[ct] = pair[1];
						ct++;
					}
					mDataList.clear();
					setData(rankName, rankSteps);
					handler.post(new Runnable() {
						@Override
						public void run() {
							SimpleAdapter sa = new SimpleAdapter(
									rank.this, mDataList,
									R.layout.item, new String[] { "rank",
											"name", "steps" }, new int[] {
											R.id.rank, R.id.name,
											R.id.steps });
							ranklist.setAdapter(sa);
						}
					});
				} catch (ClientProtocolException e) {
					// Toast.makeText(getApplicationContext(),
					// "Client Protocol Exception", Toast.LENGTH_SHORT)
					// .show();
					Log.d("Client Protocol Exception", "ttttttttttttttt");
					e.printStackTrace();
				} catch (IOException e) {
					// Toast.makeText(getApplicationContext(), "IO Exception",
					// Toast.LENGTH_SHORT).show();
					Log.d("IO Exception", "ttttttttttttttt");
					e.printStackTrace();
				}
			}
		}).start();

		// need to change
		/*
		 * String[] tmp = { "lamslm", "knkmla", "nklnl" }; setData(tmp);
		 * SimpleAdapter sa = new SimpleAdapter(this, mDataList, R.layout.item,
		 * new String[] { "rank", "name" }, new int[] { R.id.textView1,
		 * R.id.textView2 }); ranklist.setAdapter(sa);
		 */
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
