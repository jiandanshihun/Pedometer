package com.example.pedometer;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;

@SuppressWarnings("deprecation")
public class Tab extends TabActivity {
	TabHost tab;
	RadioGroup tab_radioGroup;
	RadioButton run, history, rank;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mytab);
		tab = getTabHost();
		run = (RadioButton) findViewById(R.id.radioButton0);
		history = (RadioButton) findViewById(R.id.radioButton1);
		rank = (RadioButton) findViewById(R.id.radioButton2);
		tab.addTab(tab.newTabSpec("run").setIndicator("first")
				.setContent(new Intent(this, pedometer.class)));
		tab.addTab(tab.newTabSpec("history").setIndicator("second")
				.setContent(new Intent(this, history.class)));
		tab.addTab(tab.newTabSpec("rank").setIndicator("third")
				.setContent(new Intent(this, rank.class)));
		tab_radioGroup = (RadioGroup) findViewById(R.id.tab_radiogroup);
		tab_radioGroup
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup arg0, int arg1) {
						// TODO Auto-generated method stub
						switch (arg1) {
						case R.id.radioButton0:
							tab.setCurrentTabByTag("run");
							run.setBackgroundResource(R.drawable.running2);
							history.setBackgroundResource(R.drawable.historying1);
							rank.setBackgroundResource(R.drawable.ranking1);
							break;
						case R.id.radioButton1:
							tab.setCurrentTabByTag("history");
							run.setBackgroundResource(R.drawable.running1);
							history.setBackgroundResource(R.drawable.historying2);
							rank.setBackgroundResource(R.drawable.ranking1);
							break;
						case R.id.radioButton2:
							tab.setCurrentTabByTag("rank");
							run.setBackgroundResource(R.drawable.running1);
							history.setBackgroundResource(R.drawable.historying1);
							rank.setBackgroundResource(R.drawable.ranking2);
							break;
						}
					}

				});
		((RadioButton) tab_radioGroup.getChildAt(0)).toggle();
	}

}
