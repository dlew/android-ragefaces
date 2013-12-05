package com.idunnolol.ragefaces;

import android.app.Application;

import com.danlew.utils.Log;

public class RageFacesApp extends Application {

	// Logging tag
	public static final String TAG = "RageFaces";

	@Override
	public void onCreate() {
		super.onCreate();

		Log.configure(TAG, true);
	}
}
