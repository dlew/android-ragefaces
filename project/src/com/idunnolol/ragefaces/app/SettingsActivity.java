package com.idunnolol.ragefaces.app;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.idunnolol.ragefaces.R;

public class SettingsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
	}
}
