package com.example.pedometer;

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class userSetting extends PreferenceActivity {

	Context ctx;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.config);
		ctx = this;
		ListPreference mLP = (ListPreference) findPreference("userHeight");
		mLP.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method
				Toast.makeText(ctx, "click the preference", Toast.LENGTH_SHORT)
						.show();
				return true;
			}
		});
	}

}
